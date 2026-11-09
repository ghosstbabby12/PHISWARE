package com.phishware.android.ui.education

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phishware.android.data.model.EducationDto
import com.phishware.android.databinding.ItemEducationRowBinding

class EducationAdapter(
    private val onRead: (EducationDto) -> Unit,
    private val onQuiz: (EducationDto) -> Unit
) : RecyclerView.Adapter<EducationAdapter.VH>() {

    private val items = mutableListOf<EducationDto>()

    fun submitList(newItems: List<EducationDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEducationRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onRead, onQuiz)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount() = items.size

    class VH(
        private val binding: ItemEducationRowBinding,
        private val onRead: (EducationDto) -> Unit,
        private val onQuiz: (EducationDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EducationDto) {
            binding.tvCategory.text = item.category?.uppercase().orEmpty().ifEmpty { "PHISHWARE" }
            binding.tvTitle.text = item.title
            binding.tvSummary.text = item.summary.orEmpty()
            binding.root.setOnClickListener { onRead(item) }
            binding.btnRead.setOnClickListener { onRead(item) }
            binding.btnQuiz.setOnClickListener { onQuiz(item) }
        }
    }
}
