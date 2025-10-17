package com.example.regenx.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.regenx.databinding.ItemGrievanceBinding
import com.example.regenx.models.Grievance

class GrievanceAdapter : RecyclerView.Adapter<GrievanceAdapter.GrievanceViewHolder>() {
    private var items: List<Grievance> = emptyList()

    fun submitList(list: List<Grievance>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GrievanceViewHolder {
        val binding = ItemGrievanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GrievanceViewHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: GrievanceViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class GrievanceViewHolder(private val binding: ItemGrievanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Grievance) {
            binding.tvDescription.text = item.description
            binding.tvStatus.text = item.status
        }
    }
}
