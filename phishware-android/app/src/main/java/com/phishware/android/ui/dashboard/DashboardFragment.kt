package com.phishware.android.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.phishware.android.R
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var repository: PhishwareRepository
    @Inject lateinit var session: SessionManager

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnGoAnalyze.setOnClickListener { runCatching { findNavController().navigate(R.id.url_analysis_fragment) } }
        binding.btnGoQr.setOnClickListener { runCatching { findNavController().navigate(R.id.qr_scanner_fragment) } }
        binding.btnGoHistory.setOnClickListener { runCatching { findNavController().navigate(R.id.history_fragment) } }
        load()
    }

    override fun onResume() { super.onResume(); load() }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            val name = try { session.username.first().orEmpty() } catch (_: Exception) { "" }
            binding.tvGreeting.text = "${getString(R.string.dash_hello)}${if (name.isNotBlank()) ", $name" else ""}"
            repository.getDashboard().onSuccess { d ->
                binding.tvTotal.text = d.totalAnalyses.toString()
                binding.tvLevel.text = getString(R.string.dash_level_fmt, d.userLevel, d.userPoints)
                binding.tvDanger.text = d.dangerousUrls.toString()
                binding.tvSusp.text = d.suspiciousUrls.toString()
                binding.tvSafe.text = d.safeUrls.toString()
                binding.tvScore.text = if (d.totalAnalyses > 0L) "${d.threatsDetected}" else "0"
                binding.tvUsername.text = getString(R.string.dash_threats_fmt, d.threatsDetected, d.unreadAlerts)
            }.onFailure {
                binding.tvUsername.text = getString(R.string.dash_no_conn)
            }
            repository.getHistory(0, 3).onSuccess { page ->
                binding.tvRecent.text = if (page.content.isEmpty()) getString(R.string.dash_recent_empty)
                else page.content.joinToString("\n\n") {
                    val icon = when (it.riskLevel) { "SAFE" -> "✅"; "SUSPICIOUS" -> "⚠️"; "DANGEROUS" -> "⛔"; else -> "🔍" }
                    "$icon ${it.domain ?: it.originalUrl}"
                }
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
