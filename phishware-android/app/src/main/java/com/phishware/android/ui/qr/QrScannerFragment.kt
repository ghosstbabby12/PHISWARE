package com.phishware.android.ui.qr

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.phishware.android.R
import com.phishware.android.data.model.UrlAnalysisResponse
import com.phishware.android.databinding.FragmentQrScannerBinding
import com.phishware.android.notifications.NotificationHelper
import com.phishware.android.security.UrlExtractor
import com.phishware.android.viewmodel.UrlAnalysisViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Flujo QR terminado:
 * 1. Cámara o galería → decodifica QR.
 * 2. Si trae URL → pausa el escáner y analiza una sola vez.
 * 3. Si no trae URL (WiFi, texto, vCard) → lo muestra sin error.
 * 4. Resultado con acciones: ver en Análisis / escanear otro.
 */
@AndroidEntryPoint
class QrScannerFragment : Fragment() {

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UrlAnalysisViewModel by viewModels()
    @Inject lateinit var notifications: NotificationHelper

    private var cameraExecutor: ExecutorService? = null
    private var scanningPaused = false
    private var lastValue: String? = null
    private var lastUrl: String? = null

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) decodeFromGallery(uri)
    }
    private val reqCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!isAdded) return@registerForActivityResult
        if (ok) startCamera()
        else {
            setStatus(getString(R.string.qr_no_perm_full))
            Snackbar.make(binding.root, getString(R.string.qr_no_cam_gallery), Snackbar.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentQrScannerBinding.inflate(inflater, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cameraExecutor = Executors.newSingleThreadExecutor()
        setStatus(getString(R.string.qr_point))

        binding.btnGallery.setOnClickListener { pickImage.launch("image/*") }
        binding.btnRescan.setOnClickListener { resumeScanning() }
        binding.btnOpenAnalysis.setOnClickListener {
            val url = lastUrl ?: UrlExtractor.extractUrls(binding.etQrText.text.toString()).firstOrNull()
            if (url.isNullOrBlank()) {
                Snackbar.make(binding.root, getString(R.string.qr_no_url_toast), Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(
                R.id.url_analysis_fragment,
                Bundle().apply { putString("prefill_url", url) }
            )
        }
        binding.btnAnalyzeQr.setOnClickListener {
            val raw = binding.etQrText.text.toString().ifBlank { lastValue }
            val urls = UrlExtractor.extractUrls(raw)
            if (urls.isEmpty()) {
                showNonUrlResult(raw)
                return@setOnClickListener
            }
            onQrUrl(urls.first())
        }

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        ) startCamera()
        else reqCamera.launch(Manifest.permission.CAMERA)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.analysisState.collect { state ->
                if (!isAdded || _binding == null) return@collect
                when (state) {
                    is UrlAnalysisViewModel.AnalysisState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.cardResult.visibility = View.GONE
                        setStatus(getString(R.string.qr_analyzing_url))
                    }
                    is UrlAnalysisViewModel.AnalysisState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        showResult(state.result)
                        viewModel.reset()
                    }
                    is UrlAnalysisViewModel.AnalysisState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        setStatus(getString(R.string.qr_analysis_fail))
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        viewModel.reset()
                    }
                    else -> { binding.progressBar.visibility = View.GONE }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Si vuelve al fragment y tenía cámara, reanudar solo si no hay resultado pendiente
        if (!scanningPaused && cameraExecutor?.isShutdown != false) {
            cameraExecutor = Executors.newSingleThreadExecutor()
            if (hasCameraPermission()) startCamera()
        }
    }

    override fun onPause() {
        super.onPause()
        stopCamera()
    }

    // ---------- cámara ----------

    private fun hasCameraPermission(): Boolean =
        ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED

    private fun startCamera() {
        if (!isAdded || scanningPaused) return
        val provider = ProcessCameraProvider.getInstance(requireContext())
        provider.addListener({
            if (!isAdded || _binding == null) return@addListener
            try {
                val cameraProvider = provider.get()
                cameraProvider.unbindAll()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                analysis.setAnalyzer(cameraExecutor!!) { proxy -> processFrame(proxy) }
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                )
                setStatus(getString(R.string.qr_point))
            } catch (_: Exception) {
                if (isAdded && _binding != null)
                    Snackbar.make(binding.root, getString(R.string.qr_no_cam_start), Snackbar.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    private fun stopCamera() {
        try {
            if (isAdded) {
                ProcessCameraProvider.getInstance(requireContext()).get().unbindAll()
            }
        } catch (_: Exception) { }
        try { cameraExecutor?.shutdownNow() } catch (_: Exception) { }
        cameraExecutor = null
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processFrame(proxy: ImageProxy) {
        try {
            if (scanningPaused) { proxy.close(); return }
            val media = proxy.image ?: run { proxy.close(); return }
            val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
            BarcodeScanning.getClient().process(image)
                .addOnSuccessListener { codes ->
                    val v = codes.firstOrNull()?.rawValue
                    if (!v.isNullOrBlank() && v != lastValue) {
                        lastValue = v
                        if (isAdded) {
                            requireActivity().runOnUiThread { onQrDecoded(v) }
                        }
                    }
                }
                .addOnCompleteListener { proxy.close() }
                .addOnFailureListener { proxy.close() }
        } catch (_: Exception) { try { proxy.close() } catch (_: Exception) { } }
    }

    private fun decodeFromGallery(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(requireContext(), uri)
            setStatus(getString(R.string.qr_reading_img))
            BarcodeScanning.getClient().process(image)
                .addOnSuccessListener { codes ->
                    if (!isAdded || _binding == null) return@addOnSuccessListener
                    val v = codes.firstOrNull()?.rawValue
                    if (v.isNullOrBlank()) {
                        setStatus(getString(R.string.qr_no_qr_img_status))
                        Snackbar.make(binding.root, getString(R.string.qr_no_qr_img), Snackbar.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }
                    onQrDecoded(v)
                }
                .addOnFailureListener {
                    if (isAdded && _binding != null) {
                        setStatus(getString(R.string.qr_img_fail_status))
                        Snackbar.make(binding.root, getString(R.string.qr_img_fail), Snackbar.LENGTH_LONG).show()
                    }
                }
        } catch (_: Exception) {
            if (isAdded && _binding != null)
                Snackbar.make(binding.root, getString(R.string.qr_img_fail), Snackbar.LENGTH_LONG).show()
        }
    }

    // ---------- flujo ----------

    /** Punto único de entrada tras decodificar (cámara o galería). */
    private fun onQrDecoded(value: String) {
        if (!isAdded || _binding == null) return
        lastValue = value
        binding.etQrText.setText(value)
        val urls = UrlExtractor.extractUrls(value)
        if (urls.isEmpty()) {
            showNonUrlResult(value)
            return
        }
        onQrUrl(urls.first())
    }

    /** QR con URL: pausa el escáner y analiza una sola vez. */
    private fun onQrUrl(url: String) {
        lastUrl = url
        scanningPaused = true
        stopCamera()
        setStatus(getString(R.string.qr_detected))
        binding.btnRescan.visibility = View.VISIBLE
        viewModel.analyzeUrl(url)
    }

    /** QR sin URL (texto, WiFi, vCard…): se muestra, no es error. */
    private fun showNonUrlResult(raw: String?) {
        scanningPaused = true
        stopCamera()
        binding.cardResult.visibility = View.VISIBLE
        binding.tvRiskLevel.text = getString(R.string.qr_no_url_title)
        binding.tvDetail.text = (raw?.take(300) ?: "") + "\n\n" + getString(R.string.qr_no_url_detail)
        binding.btnOpenAnalysis.visibility = View.GONE
        binding.btnRescan.visibility = View.VISIBLE
        setStatus(getString(R.string.qr_read_no_link))
    }

    private fun resumeScanning() {
        lastValue = null
        lastUrl = null
        scanningPaused = false
        binding.cardResult.visibility = View.GONE
        binding.btnRescan.visibility = View.GONE
        binding.btnOpenAnalysis.visibility = View.VISIBLE
        binding.etQrText.setText("")
        viewModel.reset()
        if (cameraExecutor == null || cameraExecutor!!.isShutdown) {
            cameraExecutor = Executors.newSingleThreadExecutor()
        }
        if (hasCameraPermission()) startCamera()
        else reqCamera.launch(Manifest.permission.CAMERA)
        setStatus(getString(R.string.qr_point))
    }

    private fun showResult(r: UrlAnalysisResponse) {
        if (!isAdded || _binding == null) return
        binding.cardResult.visibility = View.VISIBLE
        binding.btnRescan.visibility = View.VISIBLE
        binding.btnOpenAnalysis.visibility = View.VISIBLE
        val label = when (r.riskLevel) {
            "SAFE" -> getString(R.string.qr_status_safe)
            "SUSPICIOUS" -> getString(R.string.qr_status_susp)
            "DANGEROUS" -> getString(R.string.qr_status_dang)
            else -> getString(R.string.analysis_analyzed)
        }
        binding.tvRiskLevel.text = "$label · ${"%.0f".format(r.riskScore)}/100"
        binding.tvDetail.text = "${r.domain.orEmpty()}\n${r.riskMessage.orEmpty()}\n" +
            r.recommendations.orEmpty().take(2).joinToString("\n") { "• $it" }
        setStatus(getString(R.string.qr_analysis_ready_fmt, label))
        if (r.riskLevel == "DANGEROUS" || r.riskLevel == "SUSPICIOUS") {
            try {
                notifications.showThreat(
                    r.originalUrl ?: lastUrl ?: "", r.domain ?: "",
                    r.riskLevel ?: "SUSPICIOUS", r.riskScore, r.recommendations?.firstOrNull()
                )
            } catch (_: Exception) { }
        }
    }

    private fun setStatus(msg: String) {
        if (!isAdded || _binding == null) return
        binding.tvStatus.text = msg
    }

    override fun onDestroyView() {
        stopCamera()
        super.onDestroyView()
        _binding = null
    }
}
