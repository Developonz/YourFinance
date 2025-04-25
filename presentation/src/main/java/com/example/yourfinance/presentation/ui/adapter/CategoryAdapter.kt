package com.example.yourfinance.presentation.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.presentation.databinding.CategoryItemBinding


class CategoryAdapter(
    val deleteClick: (category: FullCategory) -> Unit,
    val editClick: (category: FullCategory) -> Unit,
    val editSubcategories: (category: FullCategory) -> Unit
) : ListAdapter<FullCategory, CategoryAdapter.CategoryViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<FullCategory>() {
            override fun areItemsTheSame(
                oldItem: FullCategory,
                newItem: FullCategory
            ): Boolean {
                return when {
                    oldItem.category.id == newItem.category.id -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: FullCategory,
                newItem: FullCategory
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class CategoryViewHolder(private val binding: CategoryItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: FullCategory,
            deleteClick: (acc: FullCategory) -> Unit,
            editClick: (acc: FullCategory) -> Unit,
            editSubcategories: (acc: FullCategory) -> Unit)
        {
            binding.categoryTitle.text = item.category.title
            binding.countSubcategories.text = "Подкатегорий: " + item.subcategories.size

            binding.imageDelete.setOnClickListener {
                deleteClick(item)
            }

            binding.imageEdit.setOnClickListener {
                editClick(item)
            }

            binding.root.setOnClickListener {
                editSubcategories(item)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return CategoryViewHolder(CategoryItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), deleteClick, editClick, editSubcategories)
    }
}
