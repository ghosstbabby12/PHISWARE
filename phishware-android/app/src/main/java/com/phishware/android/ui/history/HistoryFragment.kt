package com.phishware.android.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.databinding.FragmentHistoryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HistoryFragment : Fragment() {
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var repository: PhishwareRepository
    private var onlyDanger = false

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentHistoryBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnFilterDanger.setOnClickListener {
            onlyDanger = !onlyDanger
            binding.btnFilterDanger.text = if (onlyDanger) "Ver todas" else "Solo peligrosas"
            load()
        }
        binding.btnRefresh.setOnClickListener { load() }
        load()
    }

    private fun load() {
        binding.tvList.text = "Cargando…"
        lifecycleScope.launch {
            val res = repository.getHistory(0, 20, if (onlyDanger) "DANGEROUS" else null)
            res.onSuccess { page ->
                if (page.content.isEmpty()) binding.tvList.text = "Sin análisis todavía."
                else binding.tvList.text = page.content.joinToString("\n\n") { r ->
                    val icon = when (r.riskLevel) {
                        "SAFE" -> "✅"; "SUSPICIOUS" -> "⚠️"; "DANGEROUS" -> "⛔"; else -> "🔍"
                    }
                    "$icon [${r.riskLevel}] ${"%.0f".format(r.riskScore)}/100\n${r.domain ?: r.originalUrl}\n${r.analyzedAt ?: ""}"
                }
            }.onFailure { binding.tvList.text = "Error: ${it.message}" }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
