package com.example.yourfinance.presentation.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.databinding.CategoryItemBinding
import com.example.yourfinance.databinding.ItemCategoryTransactionBinding
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.FullCategory


class CategoryTransactionAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<Category, CategoryTransactionAdapter.CategoryViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(
                oldItem: Category,
                newItem: Category
            ): Boolean {
                return when {
                    oldItem.id == newItem.id -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: Category,
                newItem: Category
            ): Boolean {
                return oldItem.title == newItem.title
            }
        }
    }

    interface OnItemClickListener {
        fun onItemClick(category: Category)
    }

    class CategoryViewHolder(private val binding: ItemCategoryTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Category, listener: OnItemClickListener)
        {
            binding.titleCategory.text = item.title
            binding.root.setOnClickListener {
                listener.onItemClick(item)
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CategoryViewHolder(ItemCategoryTransactionBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), listener)
    }
}
