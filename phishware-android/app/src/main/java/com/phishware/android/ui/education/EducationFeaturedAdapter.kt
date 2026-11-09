package com.phishware.android.ui.education

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.phishware.android.data.model.EducationDto
import com.phishware.android.databinding.ItemEducationFeaturedBinding

class EducationFeaturedAdapter(private val onClick: (EducationDto) -> Unit) :
    RecyclerView.Adapter<EducationFeaturedAdapter.VH>() {

    private val items = mutableListOf<EducationDto>()

    fun submitList(newItems: List<EducationDto>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemEducationFeaturedBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position], position)

    override fun getItemCount() = items.size

    class VH(
        private val binding: ItemEducationFeaturedBinding,
        private val onClick: (EducationDto) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: EducationDto, position: Int) {
            binding.tvRank.text = (position + 1).toString()
            binding.tvCategory.text = item.category?.uppercase().orEmpty().ifEmpty { "PHISHWARE" }
            binding.tvTitle.text = item.title
            binding.root.setOnClickListener { onClick(item) }
        }
    }
}
