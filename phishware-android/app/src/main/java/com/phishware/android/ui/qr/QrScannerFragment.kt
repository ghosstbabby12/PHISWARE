package com.phishware.android.ui.qr

import android.Manifest
import android.animation.ValueAnimator
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.phishware.android.R
import com.phishware.android.data.model.UrlAnalysisResponse
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.databinding.FragmentQrScannerBinding
import com.phishware.android.databinding.ItemStatCardBinding
import com.phishware.android.notifications.NotificationHelper
import com.phishware.android.security.UrlExtractor
import com.phishware.android.ui.RiskUi
import com.phishware.android.viewmodel.UrlAnalysisViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import javax.inject.Inject

/**
 * Flujo QR:
 * 1. Cámara o galería → decodifica QR.
 * 2. En cámara NO actúa al primer fotograma: el mismo QR debe mantenerse estable
 *    [HOLD_MS] milisegundos (y [MIN_HITS] lecturas seguidas) antes de fijarlo.
 * 3. Si trae URL → pausa el escáner y analiza una sola vez.
 * 4. Si no trae URL (WiFi, texto, vCard) → lo muestra sin error.
 * 5. Abajo, estadísticas del usuario (mismo endpoint que el dashboard web).
 */
@AndroidEntryPoint
class QrScannerFragment : Fragment() {

    private var _binding: FragmentQrScannerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: UrlAnalysisViewModel by viewModels()
    @Inject lateinit var notifications: NotificationHelper
    @Inject lateinit var repository: PhishwareRepository

    private var cameraExecutor: ExecutorService? = null
    private var camera: Camera? = null
    private var torchOn = false
    private var scanner: BarcodeScanner? = null
    private var scanningPaused = false
    private var lastValue: String? = null
    private var lastUrl: String? = null

    // Estabilidad del QR en cámara
    private var candidate: String? = null
    private var candidateSince = 0L
    private var candidateHits = 0
    private var lastSeenAt = 0L

    // Estadísticas
    private var sessionScans = 0

    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) decodeFromGallery(uri)
    }
    private val reqCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { ok ->
        if (!isAdded || _binding == null) return@registerForActivityResult
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
        scanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
        setStatus(getString(R.string.qr_point))
        updateSessionChip()
        setupStatCards()
        loadStats()

        binding.btnGallery.setOnClickListener { pickImage.launch("image/*") }
        binding.btnRescan.setOnClickListener { resumeScanning() }
        binding.btnTorch.setOnClickListener {
            torchOn = !torchOn
            camera?.cameraControl?.enableTorch(torchOn)
            binding.btnTorch.alpha = if (torchOn) 1f else 0.7f
        }
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

        if (hasCameraPermission()) startCamera()
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
                        sessionScans++
                        updateSessionChip()
                        loadStats()
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
                camera = cameraProvider.bindToLifecycle(
                    viewLifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                )
                torchOn = false
                binding.btnTorch.alpha = 0.7f
                binding.btnTorch.visibility =
                    if (camera?.cameraInfo?.hasFlashUnit() == true) View.VISIBLE else View.GONE
                resetCandidate()
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
        camera = null
    }

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    private fun processFrame(proxy: ImageProxy) {
        try {
            val client = scanner
            if (scanningPaused || client == null) { proxy.close(); return }
            val media = proxy.image ?: run { proxy.close(); return }
            val image = InputImage.fromMediaImage(media, proxy.imageInfo.rotationDegrees)
            client.process(image)
                .addOnSuccessListener { codes ->
                    // El listener corre en el hilo principal.
                    if (!isAdded || _binding == null || scanningPaused) return@addOnSuccessListener
                    onFrameRead(codes.firstOrNull()?.rawValue)
                }
                .addOnCompleteListener { proxy.close() }
        } catch (_: Exception) { try { proxy.close() } catch (_: Exception) { } }
    }

    /**
     * Un QR solo se fija cuando se ve igual durante [HOLD_MS] ms y en [MIN_HITS] lecturas
     * seguidas. Si desaparece más de [GRACE_MS] ms, o cambia, el conteo empieza de nuevo.
     */
    private fun onFrameRead(value: String?) {
        val now = SystemClock.elapsedRealtime()
        if (value.isNullOrBlank()) {
            if (candidate != null && now - lastSeenAt > GRACE_MS) resetCandidate()
            return
        }
        lastSeenAt = now
        if (value != candidate) {
            candidate = value
            candidateSince = now
            candidateHits = 1
            binding.scanOverlay.progress = 0.05f
            setStatus(getString(R.string.qr_hold))
            return
        }
        candidateHits++
        val progress = ((now - candidateSince).toFloat() / HOLD_MS).coerceAtMost(1f)
        binding.scanOverlay.progress = progress
        if (progress >= 1f && candidateHits >= MIN_HITS) lockOn(value)
    }

    private fun resetCandidate() {
        candidate = null
        candidateHits = 0
        candidateSince = 0L
        if (_binding != null) {
            binding.scanOverlay.progress = 0f
            binding.scanOverlay.locked = false
        }
    }

    private fun lockOn(value: String) {
        binding.scanOverlay.locked = true
        binding.root.performHapticFeedback(
            if (Build.VERSION.SDK_INT >= 30) HapticFeedbackConstants.CONFIRM
            else HapticFeedbackConstants.LONG_PRESS
        )
        onQrDecoded(value)
    }

    private fun decodeFromGallery(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(requireContext(), uri)
            val client = scanner ?: return
            setStatus(getString(R.string.qr_reading_img))
            client.process(image)
                .addOnSuccessListener { codes ->
                    if (!isAdded || _binding == null) return@addOnSuccessListener
                    val v = codes.firstOrNull()?.rawValue
                    if (v.isNullOrBlank()) {
                        setStatus(getString(R.string.qr_no_qr_img_status))
                        Snackbar.make(binding.root, getString(R.string.qr_no_qr_img), Snackbar.LENGTH_LONG).show()
                        return@addOnSuccessListener
                    }
                    binding.scanOverlay.locked = true
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
        binding.scanOverlay.locked = true
        stopCamera()
        setStatus(getString(R.string.qr_detected))
        binding.btnRescan.visibility = View.VISIBLE
        viewModel.analyzeUrl(url)
    }

    /** QR sin URL (texto, WiFi, vCard…): se muestra, no es error. */
    private fun showNonUrlResult(raw: String?) {
        scanningPaused = true
        binding.scanOverlay.locked = true
        stopCamera()
        binding.cardResult.visibility = View.VISIBLE
        binding.tvRiskLevel.background = null
        binding.tvRiskLevel.setTextColor(resources.getColor(R.color.proton_primary, null))
        binding.tvRiskLevel.text = getString(R.string.qr_no_url_title)
        binding.tvResultDomain.visibility = View.GONE
        binding.barRisk.visibility = View.GONE
        binding.tvDetail.text = (raw?.take(300) ?: "") + "\n\n" + getString(R.string.qr_no_url_detail)
        binding.btnOpenAnalysis.visibility = View.GONE
        binding.btnRescan.visibility = View.VISIBLE
        setStatus(getString(R.string.qr_read_no_link))
    }

    private fun resumeScanning() {
        lastValue = null
        lastUrl = null
        scanningPaused = false
        resetCandidate()
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
        RiskUi.applyBadge(binding.tvRiskLevel, r.riskLevel, label)

        binding.tvResultDomain.visibility = if (r.domain.isNullOrBlank()) View.GONE else View.VISIBLE
        binding.tvResultDomain.text = r.domain.orEmpty()

        val color = Color.parseColor(RiskUi.colorFor(r.riskLevel))
        binding.barRisk.visibility = View.VISIBLE
        binding.barRisk.progressTintList = ColorStateList.valueOf(color)
        animateProgress(binding.barRisk, r.riskScore.toInt().coerceIn(0, 100))

        val tips = r.recommendations.orEmpty().take(2).joinToString("\n") { "• $it" }
        binding.tvDetail.text = listOf(
            getString(R.string.qr_score_fmt, r.riskScore.toInt()),
            r.riskMessage.orEmpty(),
            tips
        ).filter { it.isNotBlank() }.joinToString("\n")
        setStatus(getString(R.string.qr_analysis_ready_fmt, label))

        if (r.riskLevel == "DANGEROUS" || r.riskLevel == "SUSPICIOUS") {
            try {
                val top = r.threats?.firstOrNull()
                notifications.showThreat(
                    url = r.originalUrl ?: lastUrl ?: "",
                    domain = r.domain ?: "",
                    riskLevel = r.riskLevel ?: "SUSPICIOUS",
                    score = r.riskScore,
                    firstTip = r.recommendations?.firstOrNull(),
                    principle = top?.persuasionPrinciple,
                    microlessonSlug = top?.microlessonSlug,
                    sealBrand = r.officialSeal?.takeIf { it.official }?.brand
                )
            } catch (_: Exception) { }
        }
    }

    private fun setStatus(msg: String) {
        if (!isAdded || _binding == null) return
        binding.tvStatus.text = msg
    }

    // ---------- estadísticas ----------

    private fun setupStatCards() {
        bindStatStatic(binding.statTotal, R.string.dash_total, R.drawable.ic_analyze, "#790D2B")
        // El total no es una proporción: igual que en la web, sin barra.
        binding.statTotal.barStat.visibility = View.GONE
        bindStatStatic(binding.statSafe, R.string.dash_safe, R.drawable.ic_stat_check, RiskUi.SAFE)
        bindStatStatic(binding.statSusp, R.string.dash_susp, R.drawable.ic_stat_warning, RiskUi.SUSPICIOUS)
        bindStatStatic(binding.statDanger, R.string.dash_danger, R.drawable.ic_stat_danger, RiskUi.DANGEROUS)
    }

    private fun bindStatStatic(card: ItemStatCardBinding, label: Int, icon: Int, color: String) {
        card.tvStatLabel.setText(label)
        card.ivStatIcon.setImageResource(icon)
        ImageViewCompat.setImageTintList(card.ivStatIcon, ColorStateList.valueOf(Color.parseColor(color)))
        card.barStat.progressTintList = ColorStateList.valueOf(Color.parseColor(color))
    }

    private fun loadStats() {
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getDashboard().onSuccess { d ->
                if (!isAdded || _binding == null) return@onSuccess
                binding.tvStatsOffline.visibility = View.GONE
                val total = d.totalAnalyses
                fun pct(n: Long) = if (total > 0) ((n * 100) / total).toInt() else 0
                setStat(binding.statTotal, total, 0, getString(R.string.qr_stat_accumulated))
                setStat(binding.statSafe, d.safeUrls, pct(d.safeUrls), getString(R.string.qr_stat_no_risk))
                setStat(binding.statSusp, d.suspiciousUrls, pct(d.suspiciousUrls), getString(R.string.qr_stat_review))
                setStat(
                    binding.statDanger, d.dangerousUrls, pct(d.dangerousUrls),
                    getString(R.string.qr_stat_plus_susp_fmt, d.suspiciousUrls.toInt())
                )
            }.onFailure {
                if (isAdded && _binding != null) binding.tvStatsOffline.visibility = View.VISIBLE
            }
        }
    }

    private fun setStat(card: ItemStatCardBinding, value: Long, ratio: Int, sub: String) {
        card.tvStatSub.text = sub
        countUp(card.tvStatValue, value.toInt())
        animateProgress(card.barStat, ratio.coerceIn(0, 100))
    }

    private fun updateSessionChip() {
        binding.tvSession.text = getString(R.string.qr_session_fmt, sessionScans)
    }

    /** Cuenta desde el valor mostrado hasta el nuevo con easing (igual que el StatCard de la web). */
    private fun countUp(view: TextView, to: Int) {
        val from = (view.tag as? Int) ?: 0
        view.tag = to
        ValueAnimator.ofInt(from, to).apply {
            duration = 800
            interpolator = DecelerateInterpolator()
            addUpdateListener { view.text = it.animatedValue.toString() }
            start()
        }
    }

    private fun animateProgress(bar: android.widget.ProgressBar, to: Int) {
        ValueAnimator.ofInt(bar.progress, to).apply {
            duration = 900
            interpolator = DecelerateInterpolator()
            addUpdateListener { bar.progress = it.animatedValue as Int }
            start()
        }
    }

    override fun onDestroyView() {
        stopCamera()
        try { scanner?.close() } catch (_: Exception) { }
        scanner = null
        super.onDestroyView()
        _binding = null
    }

    private companion object {
        /** Tiempo que el mismo QR debe mantenerse en cuadro antes de fijarlo. */
        const val HOLD_MS = 800L
        /** Lecturas consecutivas mínimas del mismo contenido. */
        const val MIN_HITS = 4
        /** Cuánto se tolera perder el QR de vista sin reiniciar el conteo. */
        const val GRACE_MS = 400L
    }
}
