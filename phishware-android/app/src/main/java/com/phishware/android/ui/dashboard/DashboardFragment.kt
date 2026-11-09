package com.phishware.android.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
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
    private val recentAdapter = RecentScanAdapter()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentDashboardBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvRecent.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.rvRecent.adapter = recentAdapter

        binding.btnGoAnalyze.setOnClickListener { runCatching { findNavController().navigate(R.id.url_analysis_fragment) } }
        binding.btnGoQr.setOnClickListener { runCatching { findNavController().navigate(R.id.qr_scanner_fragment) } }
        binding.btnGoHistory.setOnClickListener { runCatching { findNavController().navigate(R.id.history_fragment) } }
        binding.searchPill.setOnClickListener { runCatching { findNavController().navigate(R.id.url_analysis_fragment) } }
        binding.btnAlerts.setOnClickListener { runCatching { findNavController().navigate(R.id.alerts_fragment) } }
        binding.tvViewAll.setOnClickListener { runCatching { findNavController().navigate(R.id.history_fragment) } }
        binding.swipeRefresh.setOnRefreshListener { load() }
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
                val safePct = if (d.totalAnalyses > 0L) ((d.safeUrls * 100) / d.totalAnalyses).toInt() else 100
                binding.tvScore.text = safePct.toString()
                binding.progressScore.progress = safePct
                binding.tvUsername.text = getString(R.string.dash_threats_fmt, d.threatsDetected, d.unreadAlerts)
            }.onFailure {
                binding.tvUsername.text = getString(R.string.dash_no_conn)
            }
            repository.getHistory(0, 8).onSuccess { page ->
                binding.swipeRefresh.isRefreshing = false
                if (page.content.isEmpty()) {
                    binding.rvRecent.visibility = View.GONE
                    binding.tvRecentEmpty.visibility = View.VISIBLE
                    binding.tvRecentEmpty.text = getString(R.string.dash_recent_empty)
                } else {
                    binding.rvRecent.visibility = View.VISIBLE
                    binding.tvRecentEmpty.visibility = View.GONE
                    recentAdapter.submitList(page.content)
                }
            }.onFailure {
                binding.swipeRefresh.isRefreshing = false
                binding.rvRecent.visibility = View.GONE
                binding.tvRecentEmpty.visibility = View.VISIBLE
                binding.tvRecentEmpty.text = getString(R.string.dash_no_conn)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
