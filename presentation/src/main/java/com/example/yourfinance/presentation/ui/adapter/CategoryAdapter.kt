package com.example.yourfinance.presentation.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemCategoryBinding

class CategoryAdapter(
    private val deleteClick: (Category) -> Unit,
    private val editClick: (Category) -> Unit,
    private val editSubcategories: (Category) -> Unit
) : ListAdapter<Category, CategoryAdapter.CategoryViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Category>() {
            override fun areItemsTheSame(old: Category, new: Category) = old.id == new.id
            override fun areContentsTheSame(old: Category, new: Category) = old == new

            // Фолбек-цвет, если в категории цвет не задан
            private val DEFAULT_COLOR: Int = Color.parseColor("#FFEB3B")
        }
    }

    class CategoryViewHolder(private val binding: ItemCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: Category,
            onDelete: (Category) -> Unit,
            onEdit: (Category) -> Unit,
            onEditSubs: (Category) -> Unit
        ) {
            // 1) Получаем ResId по имени
            val resId = item.iconResourceId
                ?.let { IconMap.idOf(it) }
                ?: R.drawable.ic_checkmark

            // 2) Берём цвет из Int? или ставим дефолт
            val color = item.colorHex ?: Color.parseColor("#FFEB3B")

            // Устанавливаем иконку и фон
            binding.categoryImage.setImageResource(resId)
            binding.categoryImage.setBackgroundColor(color)

            // Контрастная подложка для иконки
            val tint = if (ColorUtils.calculateLuminance(color) > 0.5) Color.BLACK else Color.WHITE
            binding.categoryImage.setColorFilter(tint)

            // Текстовые поля
            binding.categoryTitle.text = item.title
            binding.countSubcategories.text =
                binding.root.context.getString(
                    R.string.subcategories_count_format,
                    item.children.size
                )

            // Обработчики кликов
            binding.imageDelete.setOnClickListener { onDelete(item) }
            binding.imageEdit.setOnClickListener { onEdit(item) }
            binding.root.setOnClickListener { onEditSubs(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position), deleteClick, editClick, editSubcategories)
    }
}
