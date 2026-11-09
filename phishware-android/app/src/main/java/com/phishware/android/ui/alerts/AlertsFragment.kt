package com.phishware.android.ui.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.phishware.android.R
import com.phishware.android.data.repository.PhishwareRepository
import com.phishware.android.databinding.FragmentAlertsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Alertas generadas por SMS/QR/correo o analisis manual (backend /alerts). */
@AndroidEntryPoint
class AlertsFragment : Fragment() {
    private var _binding: FragmentAlertsBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var repository: PhishwareRepository
    private val adapter = AlertAdapter { alert ->
        if (!alert.isRead) {
            lifecycleScope.launch { repository.markAlertRead(alert.id); load() }
        }
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(alert.title)
            .setMessage(
                alert.message + (if (alert.recommendations.isNotEmpty()) "\n\n" + alert.recommendations.joinToString("\n") { "• $it" } else "")
            )
            .setPositiveButton(getString(R.string.common_close), null)
            .show()
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvAlerts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAlerts.adapter = adapter
        binding.btnReadAll.setOnClickListener {
            lifecycleScope.launch {
                repository.markAllAlertsRead()
                load()
            }
        }
        binding.swipeRefresh.setOnRefreshListener { load() }
        load()
    }

    private fun load() {
        lifecycleScope.launch {
            repository.getUnreadAlertCount().onSuccess { binding.tvUnread.text = getString(R.string.alerts_unread_fmt, it) }
            val res = repository.getAlerts(0, 30)
            binding.swipeRefresh.isRefreshing = false
            res.onSuccess { page ->
                if (page.content.isEmpty()) {
                    binding.rvAlerts.visibility = View.GONE
                    binding.tvEmpty.visibility = View.VISIBLE
                    binding.tvEmpty.text = getString(R.string.alerts_empty)
                } else {
                    binding.rvAlerts.visibility = View.VISIBLE
                    binding.tvEmpty.visibility = View.GONE
                    adapter.submitList(page.content)
                }
            }.onFailure {
                binding.rvAlerts.visibility = View.GONE
                binding.tvEmpty.visibility = View.VISIBLE
                binding.tvEmpty.text = getString(R.string.alerts_error_fmt, it.message ?: "")
            }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
