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
import com.phishware.android.databinding.FragmentUrlAnalysisBinding
import com.phishware.android.viewmodel.UrlAnalysisViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UrlAnalysisFragment : Fragment() {

    private var _binding: FragmentUrlAnalysisBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UrlAnalysisViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentUrlAnalysisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnAnalyze.setOnClickListener {
            val url = binding.etUrl.text.toString()
            viewModel.analyzeUrl(url)
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
        binding.tvRiskMessage.text = result.riskMessage
        binding.tvDomain.text = "Dominio: ${result.domain}"
        binding.tvScore.text = "Score de riesgo: ${String.format("%.1f", result.riskScore)}/100"
        binding.tvAnalysisTime.text = "Tiempo: ${result.analysisTimeMs}ms"

        if (result.threats.isNotEmpty()) {
            binding.tvThreats.visibility = View.VISIBLE
            binding.tvThreats.text = "⚠ ${result.threats.size} amenaza(s) detectada(s)"
        } else {
            binding.tvThreats.visibility = View.GONE
        }

        val recommendations = result.recommendations.joinToString("\n") { "• $it" }
        binding.tvRecommendations.text = recommendations
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
