package com.phishware.android.ui.history

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phishware.android.R
import com.phishware.android.data.model.UrlAnalysisResponse
import com.phishware.android.databinding.ItemHistoryRowBinding
import com.phishware.android.ui.RiskUi

class HistoryAdapter(private val onClick: (UrlAnalysisResponse) -> Unit) :
    RecyclerView.Adapter<HistoryAdapter.VH>() {

    private val items = mutableListOf<UrlAnalysisResponse>()

    fun submitList(newItems: List<UrlAnalysisResponse>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemHistoryRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    class VH(
        private val binding: ItemHistoryRowBinding,
        private val onClick: (UrlAnalysisResponse) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UrlAnalysisResponse) {
            binding.tvDomain.text = item.domain ?: item.originalUrl.orEmpty()
            binding.tvMeta.text = "${"%.0f".format(item.riskScore)}/100 · ${item.analyzedAt.orEmpty()}"
            RiskUi.applyBadge(binding.tvBadge, item.riskLevel, item.riskLevel.orEmpty())
            binding.ivRiskIcon.setImageResource(
                when (item.riskLevel) {
                    "DANGEROUS" -> R.drawable.ic_stat_danger
                    "SUSPICIOUS" -> R.drawable.ic_stat_warning
                    else -> R.drawable.ic_stat_check
                }
            )
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
