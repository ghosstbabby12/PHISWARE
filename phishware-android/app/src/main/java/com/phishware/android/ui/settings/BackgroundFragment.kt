package com.phishware.android.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.phishware.android.R
import com.phishware.android.data.local.BackgroundDetection
import com.phishware.android.data.local.BackgroundLog
import com.phishware.android.databinding.FragmentBackgroundBinding
import com.phishware.android.databinding.ItemHistoryRowBinding
import com.phishware.android.ui.RiskUi
import dagger.hilt.android.AndroidEntryPoint
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

/** Enlaces que PHISHWARE revisó solo, sin que el usuario abriera la app (WhatsApp, Instagram, SMS...). */
@AndroidEntryPoint
class BackgroundFragment : Fragment() {
    private var _binding: FragmentBackgroundBinding? = null
    private val binding get() = _binding!!
    @Inject lateinit var log: BackgroundLog

    private val adapter = Adapter { d ->
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(d.domain)
            .setMessage(
                "${d.riskLevel} · ${"%.0f".format(d.score)}/100\n" +
                    getString(R.string.bg_detected_in, d.source) + "\n\n${d.url}" +
                    (d.tip?.let { "\n\n• $it" } ?: "")
            )
            .setPositiveButton(getString(R.string.common_close), null)
            .show()
    }

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View {
        _binding = FragmentBackgroundBinding.inflate(i, c, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvBackground.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBackground.adapter = adapter
        binding.btnClear.setOnClickListener { log.clear(); refresh() }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val items = log.load()
        adapter.submit(items)
        val threats = items.count { it.riskLevel != "SAFE" }
        binding.tvSummary.text = getString(R.string.bg_summary, items.size, threats)
        binding.tvEmpty.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        binding.btnClear.visibility = if (items.isEmpty()) View.GONE else View.VISIBLE
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private class Adapter(val onClick: (BackgroundDetection) -> Unit) : RecyclerView.Adapter<Adapter.VH>() {
        private val items = mutableListOf<BackgroundDetection>()
        fun submit(l: List<BackgroundDetection>) { items.clear(); items.addAll(l); notifyDataSetChanged() }
        override fun onCreateViewHolder(p: ViewGroup, t: Int) =
            VH(ItemHistoryRowBinding.inflate(LayoutInflater.from(p.context), p, false))
        override fun getItemCount() = items.size
        override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos], onClick)

        class VH(private val b: ItemHistoryRowBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(d: BackgroundDetection, onClick: (BackgroundDetection) -> Unit) {
                b.tvDomain.text = d.domain
                val time = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(d.timestamp))
                b.tvMeta.text = "${d.source} · ${"%.0f".format(d.score)}/100 · $time"
                RiskUi.applyBadge(b.tvBadge, d.riskLevel, d.riskLevel)
                b.ivRiskIcon.setImageResource(
                    when (d.riskLevel) {
                        "DANGEROUS" -> R.drawable.ic_stat_danger
                        "SUSPICIOUS" -> R.drawable.ic_stat_warning
                        else -> R.drawable.ic_stat_check
                    }
                )
                b.root.setOnClickListener { onClick(d) }
            }
        }
    }
}
