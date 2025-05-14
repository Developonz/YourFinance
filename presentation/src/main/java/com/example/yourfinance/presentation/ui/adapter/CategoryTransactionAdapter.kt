package com.example.yourfinance.presentation.ui.adapter


import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.databinding.ItemTransactionCategoryBinding
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.presentation.databinding.ItemTransactionSubcategoryBinding


class CategoryTransactionAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<ICategoryData, RecyclerView.ViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ICategoryData>() {
            override fun areItemsTheSame(
                oldItem: ICategoryData,
                newItem: ICategoryData
            ): Boolean {
                return when {
                    oldItem.id == newItem.id -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: ICategoryData,
                newItem: ICategoryData
            ): Boolean {
                return oldItem.title == newItem.title &&
                        oldItem.iconResourceId == newItem.iconResourceId &&
                        oldItem.colorHex == newItem.colorHex
            }
        }

        val CATEGORY = 1
        val SUBCATEGORY = 2
    }

    interface OnItemClickListener {
        fun onItemClick(category: ICategoryData)
    }

    class CategoryViewHolder(private val binding: ItemTransactionCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Category, listener: OnItemClickListener)
        {
            binding.imageViewCategoryIcon.setImageResource(item.iconResourceId!!)
            binding.imageViewCategoryIcon.setBackgroundColor(Color.parseColor(item.colorHex))
            val iconTintColor = if (ColorUtils.calculateLuminance(Color.parseColor(item.colorHex)) > 0.5) Color.BLACK else Color.WHITE
            binding.imageViewCategoryIcon.setColorFilter(iconTintColor)


            binding.titleCategory.text = item.title
            binding.root.setOnClickListener {
                listener.onItemClick(item)
            }

        }
    }

    class SubcategoryViewHolder(private val binding: ItemTransactionSubcategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Subcategory, listener: OnItemClickListener)
        {
            binding.imageViewSubcategoryIcon.setBackgroundColor(Color.parseColor(item.colorHex))

            binding.titleSubcategory.text = item.title
            binding.root.setOnClickListener {
                listener.onItemClick(item)
            }

        }
    }

    override fun getItemViewType(position: Int): Int {
        return when(getItem(position)) {
            is Category -> CATEGORY
            is Subcategory -> SUBCATEGORY
            else -> 0
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        if (viewType == CATEGORY) {
            return CategoryViewHolder(
                ItemTransactionCategoryBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
        } else {
            return SubcategoryViewHolder(
                ItemTransactionSubcategoryBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = getItem(position)) {
            is Category -> (holder as CategoryViewHolder).bind(item, listener)
            is Subcategory -> (holder as SubcategoryViewHolder).bind(item, listener)
        }
    }
}
