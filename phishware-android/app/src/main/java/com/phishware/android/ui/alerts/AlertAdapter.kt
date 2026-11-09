package com.phishware.android.ui.alerts

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phishware.android.data.remote.AlertDto
import com.phishware.android.databinding.ItemAlertRowBinding
import com.phishware.android.ui.RiskUi

class AlertAdapter(private val onClick: (AlertDto) -> Unit) : RecyclerView.Adapter<AlertAdapter.VH>() {

    private val items = mutableListOf<AlertDto>()

    fun submitList(newItems: List<AlertDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemAlertRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    class VH(
        private val binding: ItemAlertRowBinding,
        private val onClick: (AlertDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AlertDto) {
            binding.tvTitle.text = item.title
            binding.tvMessage.text = item.message
            binding.tvTime.text = item.createdAt
            binding.dotUnread.visibility = if (item.isRead) View.GONE else View.VISIBLE
            RiskUi.applyBadge(binding.tvBadge, item.severity, item.severity)
            binding.vSeverity.backgroundTintList =
                android.content.res.ColorStateList.valueOf(Color.parseColor(RiskUi.colorFor(item.severity)))
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
