package com.example.yourfinance.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.databinding.SubcategoryItemBinding
import com.example.yourfinance.domain.model.entity.category.Subcategory


class SubcategoryAdapter(
    val deleteClick: (subcategory: Subcategory) -> Unit,
    val editClick: (subcategory: Subcategory) -> Unit
) : ListAdapter<Subcategory, SubcategoryAdapter.SubcategoryViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Subcategory>() {
            override fun areItemsTheSame(
                oldItem: Subcategory,
                newItem: Subcategory
            ): Boolean {
                return when {
                    oldItem.id == newItem.id -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: Subcategory,
                newItem: Subcategory
            ): Boolean {
                return oldItem.title == newItem.title
            }
        }
    }

    class SubcategoryViewHolder(private val binding: SubcategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Subcategory,
            deleteClick: (subcategory: Subcategory) -> Unit,
            editClick: (subcategory: Subcategory) -> Unit)
        {
            binding.subcategoryTitle.text = item.title

            binding.imageDelete.setOnClickListener {
                deleteClick(item)
            }

            binding.root.setOnClickListener {
                editClick(item)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return SubcategoryViewHolder(SubcategoryItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        holder.bind(getItem(position), deleteClick, editClick)
    }
}
