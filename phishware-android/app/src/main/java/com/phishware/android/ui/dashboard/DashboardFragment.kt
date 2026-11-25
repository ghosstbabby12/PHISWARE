package com.phishware.android.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.phishware.android.R
import com.phishware.android.data.local.SessionManager
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.databinding.FragmentDashboardBinding
import com.phishware.android.databinding.ItemQuickActionBinding
import com.phishware.android.ui.RiskUi
import com.phishware.android.ui.StatCards
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

        StatCards.bindStatic(binding.statTotal, R.string.dash_total, R.drawable.ic_analyze, androidx.core.content.ContextCompat.getColor(requireContext(), R.color.proton_primary), false)
        StatCards.bindStatic(binding.statSafe, R.string.dash_safe, R.drawable.ic_stat_check, RiskUi.SAFE)
        StatCards.bindStatic(binding.statSusp, R.string.dash_susp, R.drawable.ic_stat_warning, RiskUi.SUSPICIOUS)
        StatCards.bindStatic(binding.statDanger, R.string.dash_danger, R.drawable.ic_stat_danger, RiskUi.DANGEROUS)

        // Destinos del dock inferior: se cambian con la propia pestaña (si no, Inicio queda con la pantalla apilada)
        setupQuickAction(binding.qaAnalyze, R.drawable.ic_analyze, R.string.dash_go_analyze, getString(R.string.dash_qa_analyze_d)) { openTab(R.id.url_analysis_fragment) }
        setupQuickAction(binding.qaQr, R.drawable.ic_qr, R.string.qr_title, getString(R.string.dash_qa_qr_d)) { go(R.id.qr_scanner_fragment) }
        setupQuickAction(binding.qaAlerts, R.drawable.ic_alert, R.string.alerts_title, getString(R.string.dash_qa_alerts_d)) { openTab(R.id.alerts_fragment) }
        setupQuickAction(binding.qaHistory, R.drawable.ic_history, R.string.dash_go_history, getString(R.string.dash_qa_history_d)) { go(R.id.history_fragment) }

        binding.searchPill.setOnClickListener { openTab(R.id.url_analysis_fragment) }
        binding.btnAlerts.setOnClickListener { openTab(R.id.alerts_fragment) }
        binding.tvViewAll.setOnClickListener { go(R.id.history_fragment) }
        binding.btnKeepLearning.setOnClickListener { openTab(R.id.education_fragment) }
        binding.swipeRefresh.setOnRefreshListener { load() }
        load()
    }

    /** Pantallas secundarias (QR, historial): se apilan sobre Inicio y "atrás" vuelve aquí. */
    private fun go(destination: Int) {
        runCatching { findNavController().navigate(destination) }
    }

    /** Pestañas del dock: se seleccionan en el dock para que el estado de cada pestaña quede coherente. */
    private fun openTab(destination: Int) {
        val dock = requireActivity().findViewById<BottomNavigationView>(R.id.bottomNavigation)
        if (dock != null) dock.selectedItemId = destination else go(destination)
    }

    private fun setupQuickAction(
        card: ItemQuickActionBinding, @DrawableRes icon: Int, @StringRes title: Int, desc: String, onClick: () -> Unit
    ) {
        card.ivQaIcon.setImageResource(icon)
        card.tvQaTitle.setText(title)
        card.tvQaDesc.text = desc
        card.root.setOnClickListener { onClick() }
    }

    override fun onResume() { super.onResume(); load() }

    private fun load() {
        viewLifecycleOwner.lifecycleScope.launch {
            val name = try { session.username.first().orEmpty() } catch (_: Exception) { "" }
            if (_binding == null) return@launch
            val hello = getString(R.string.dash_hello)
            binding.tvGreeting.text = "$hello${if (name.isNotBlank()) ", $name" else ""}"
            binding.tvHeroTitle.text = "$hello${if (name.isNotBlank()) ", $name" else ""}."

            repository.getDashboard().onSuccess { d ->
                if (_binding == null) return@onSuccess
                val total = d.totalAnalyses
                fun pct(n: Long) = if (total > 0) ((n * 100) / total).toInt() else 0
                val safePct = if (total > 0L) pct(d.safeUrls) else 100

                binding.tvScore.text = "$safePct%"
                binding.ringScore.setPercent(safePct)

                val calm = d.threatsDetected == 0L && d.unreadAlerts == 0L
                binding.tvHeroStatus.text = if (calm) getString(R.string.dash_calm)
                else getString(R.string.dash_threats_fmt, d.threatsDetected.toInt(), d.unreadAlerts.toInt())
                binding.tvHeroSub.text = if (total == 0L) getString(R.string.dash_recent_empty)
                else getString(R.string.dash_total_fmt, total.toInt())
                binding.tvUsername.text = getString(R.string.dash_threats_fmt, d.threatsDetected.toInt(), d.unreadAlerts.toInt())
                binding.tvLevel.text = getString(R.string.dash_level_fmt, d.userLevel, d.userPoints)

                StatCards.setValue(binding.statTotal, total, 0, getString(R.string.qr_stat_accumulated))
                StatCards.setValue(binding.statSafe, d.safeUrls, pct(d.safeUrls), getString(R.string.qr_stat_no_risk))
                StatCards.setValue(binding.statSusp, d.suspiciousUrls, pct(d.suspiciousUrls), getString(R.string.qr_stat_review))
                StatCards.setValue(
                    binding.statDanger, d.dangerousUrls, pct(d.dangerousUrls),
                    getString(R.string.qr_stat_plus_susp_fmt, d.suspiciousUrls.toInt())
                )

                binding.qaAlerts.tvQaDesc.text =
                    if (d.unreadAlerts > 0) getString(R.string.dash_qa_alerts_unread_fmt, d.unreadAlerts.toInt())
                    else getString(R.string.dash_qa_alerts_d)

                // Siguiente nivel: mismo cálculo que la web (100 · 2^nivel)
                val next = 100.0 * Math.pow(2.0, d.userLevel.toDouble())
                val levelPct = ((d.userPoints / next) * 100).toInt().coerceIn(0, 100)
                binding.tvLevelBig.text = getString(R.string.dash_level_big_fmt, d.userLevel)
                binding.tvLevelPoints.text = getString(R.string.dash_points_fmt, d.userPoints)
                StatCards.animateProgress(binding.barLevel, levelPct)
            }.onFailure {
                if (_binding == null) return@onFailure
                binding.tvUsername.text = getString(R.string.dash_no_conn)
                binding.tvHeroStatus.text = getString(R.string.dash_no_conn)
            }

            repository.getHistory(0, 8).onSuccess { page ->
                if (_binding == null) return@onSuccess
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
                if (_binding == null) return@onFailure
                binding.swipeRefresh.isRefreshing = false
                binding.rvRecent.visibility = View.GONE
                binding.tvRecentEmpty.visibility = View.VISIBLE
                binding.tvRecentEmpty.text = getString(R.string.dash_no_conn)
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
