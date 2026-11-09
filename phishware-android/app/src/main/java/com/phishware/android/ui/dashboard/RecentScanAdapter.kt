package com.phishware.android.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phishware.android.data.model.UrlAnalysisResponse
import com.phishware.android.databinding.ItemRecentScanBinding
import com.phishware.android.ui.RiskUi

class RecentScanAdapter : RecyclerView.Adapter<RecentScanAdapter.VH>() {

    private val items = mutableListOf<UrlAnalysisResponse>()

    fun submitList(newItems: List<UrlAnalysisResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemRecentScanBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    class VH(private val binding: ItemRecentScanBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UrlAnalysisResponse) {
            binding.tvDomain.text = item.domain ?: item.originalUrl.orEmpty()
            val label = when (item.riskLevel) {
                "SAFE" -> "SAFE"
                "SUSPICIOUS" -> "SUSPICIOUS"
                "DANGEROUS" -> "DANGEROUS"
                else -> item.riskLevel.orEmpty()
            }
            RiskUi.applyBadge(binding.tvBadge, item.riskLevel, label)
            binding.ivRiskIcon.setImageResource(
                when (item.riskLevel) {
                    "DANGEROUS" -> com.phishware.android.R.drawable.ic_stat_danger
                    "SUSPICIOUS" -> com.phishware.android.R.drawable.ic_stat_warning
                    else -> com.phishware.android.R.drawable.ic_stat_check
                }
            )
        }
    }
}
