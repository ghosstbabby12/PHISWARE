package com.phishware.android.ui.analysis

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.phishware.android.R
import com.phishware.android.data.model.UrlAnalysisResponse
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.databinding.FragmentUrlAnalysisBinding
import com.phishware.android.viewmodel.UrlAnalysisViewModel
import javax.inject.Inject
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UrlAnalysisFragment : Fragment() {

    private var _binding: FragmentUrlAnalysisBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UrlAnalysisViewModel by viewModels()
    @Inject lateinit var repository: PhishwareRepository
    private var lastUrl: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUrlAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Prefill desde notificacion / ShareReceiver (SMS, QR, correo compartido)
        val prefill = arguments?.getString("prefill_url")
        if (!prefill.isNullOrBlank()) {
            binding.etUrl.setText(prefill)
            viewModel.analyzeUrl(prefill)
            arguments?.remove("prefill_url")
        }

        binding.btnAnalyze.setOnClickListener {
            val url = binding.etUrl.text.toString()
            viewModel.analyzeUrl(url)
        }
        binding.btnReport.setOnClickListener {
            val url = lastUrl ?: binding.etUrl.text.toString()
            if (url.isBlank()) return@setOnClickListener
            viewLifecycleOwner.lifecycleScope.launch {
                repository.reportUrl(url).onSuccess {
                    Snackbar.make(binding.root, "Reporte enviado ✅ (id ${it.id})", Snackbar.LENGTH_LONG).show()
                }.onFailure { Snackbar.make(binding.root, it.message ?: "Error", Snackbar.LENGTH_LONG).show() }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            repository.getDashboard().onSuccess { d ->
                binding.tvDash.text = "📊 ${d.totalAnalyses} análisis · ⛔ ${d.threatsDetected} amenazas · ⭐ Nv.${d.userLevel} (${d.userPoints} pts)"
            }.onFailure { binding.tvDash.text = "📊 Sin conexión" }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.analysisState.collect { state ->
                when (state) {
                    is UrlAnalysisViewModel.AnalysisState.Idle -> {
                        binding.progressBar.visibility = View.GONE
                        binding.cardResult.visibility = View.GONE
                    }
                    is UrlAnalysisViewModel.AnalysisState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.cardResult.visibility = View.GONE
                        binding.btnAnalyze.isEnabled = false
                    }
                    is UrlAnalysisViewModel.AnalysisState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnAnalyze.isEnabled = true
                        showResult(state.result)
                    }
                    is UrlAnalysisViewModel.AnalysisState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.btnAnalyze.isEnabled = true
                        Snackbar.make(binding.root, state.message, Snackbar.LENGTH_LONG).show()
                        viewModel.reset()
                    }
                }
            }
        }
    }

    private fun showResult(result: UrlAnalysisResponse) {
        lastUrl = result.originalUrl ?: binding.etUrl.text.toString()
        binding.cardResult.visibility = View.VISIBLE

        val (colorRes, icon, label) = when (result.riskLevel) {
            "SAFE"       -> Triple(R.color.success_green, "✅", "SEGURO")
            "SUSPICIOUS" -> Triple(R.color.warning_amber, "⚠️", "SOSPECHOSO")
            "DANGEROUS"  -> Triple(R.color.danger_red,    "⛔", "PELIGROSO")
            else         -> Triple(R.color.primary_blue,  "🔍", "ANALIZADO")
        }

        val color = ContextCompat.getColor(requireContext(), colorRes)
        binding.tvRiskLevel.text = "$icon $label"
        binding.tvRiskLevel.setTextColor(color)
        binding.tvRiskMessage.text = result.riskMessage.orEmpty()
        binding.tvDomain.text = "Dominio: ${result.domain.orEmpty()}"
        binding.tvScore.text = "Score de riesgo: ${String.format("%.1f", result.riskScore)}/100"
        binding.tvAnalysisTime.text = "Tiempo: ${result.analysisTimeMs ?: 0}ms"

        val threats = result.threats.orEmpty()
        if (threats.isNotEmpty()) {
            binding.tvThreats.visibility = View.VISIBLE
            binding.tvThreats.text = "⚠ ${threats.size} amenaza(s) detectada(s)"
        } else {
            binding.tvThreats.visibility = View.GONE
        }

        binding.tvRecommendations.text = result.recommendations.orEmpty().joinToString("\n") { "• $it" }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
