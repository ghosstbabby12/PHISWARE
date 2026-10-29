package com.phishware.android.ui.alerts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
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

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentAlertsBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnReadAll.setOnClickListener {
            lifecycleScope.launch {
                repository.markAllAlertsRead()
                load()
            }
        }
        load()
    }

    private fun load() {
        binding.tvList.text = getString(R.string.alerts_loading)
        lifecycleScope.launch {
            repository.getUnreadAlertCount().onSuccess { binding.tvUnread.text = getString(R.string.alerts_unread_fmt, it) }
            repository.getAlerts(0, 20)
                .onSuccess { page ->
                    if (page.content.isEmpty()) binding.tvList.text = getString(R.string.alerts_empty)
                    else binding.tvList.text = page.content.joinToString("\n\n") { a ->
                        val icon = when (a.severity) {
                            "CRITICAL" -> "⛔"; "HIGH" -> "🔴"; "MEDIUM" -> "🟠"; else -> "🟡"
                        }
                        val read = if (a.isRead) "" else getString(R.string.alerts_new)
                        "$icon [${a.severity}]${read}\n${a.title}\n${a.message.take(160)}"
                    }
                }
                .onFailure { binding.tvList.text = getString(R.string.alerts_error_fmt, it.message ?: "") }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
