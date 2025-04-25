package com.example.yourfinance.presentation.ui.adapter.wallet_page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.databinding.SectionsHeaderBinding


class SectionHeaderAdapter(
    val title: String,
    private val onHeaderClick: (sectionTitle: String) -> Unit
): RecyclerView.Adapter<SectionHeaderAdapter.HeaderViewHolder>() {

    class HeaderViewHolder(
        private val binding: SectionsHeaderBinding,
        private val onHeaderClick: (sectionTitle: String) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(title: String) {
            binding.sectionHeader.text = title
            binding.sectionHeader.setOnClickListener({
                onHeaderClick(title)
            })

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeaderViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return HeaderViewHolder(SectionsHeaderBinding.inflate(inflater, parent, false), onHeaderClick)
    }

    override fun getItemCount(): Int {
        return 1
    }

    override fun onBindViewHolder(holder: HeaderViewHolder, position: Int) {
        holder.bind(title)
    }
}