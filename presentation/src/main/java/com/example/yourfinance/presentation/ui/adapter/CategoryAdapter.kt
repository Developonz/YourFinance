package com.example.yourfinance.presentation.ui.adapter


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.presentation.databinding.ItemCategoryBinding


class CategoryAdapter(
    val deleteClick: (category: Category) -> Unit,
    val editClick: (category: Category) -> Unit,
    val editSubcategories: (category: Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(
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
                return oldItem == newItem
            }
        }
    }

    class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: Category,
            deleteClick: (acc: Category) -> Unit,
            editClick: (acc: Category) -> Unit,
            editSubcategories: (acc: Category) -> Unit)
        {
            binding.categoryTitle.text = item.title
            binding.countSubcategories.text = "Подкатегорий: " + item.children.size

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
        return CategoryViewHolder(ItemCategoryBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), deleteClick, editClick, editSubcategories)
    }
}
