package com.example.yourfinance.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemCategorySelectionBinding
import com.example.yourfinance.presentation.ui.fragment.manager.budget_manager.SelectableCategoryItem

class CategorySelectionAdapter(initialSelectedIds: Set<Long>) :
    ListAdapter<SelectableCategoryItem, CategorySelectionAdapter.CategoryViewHolder>(DiffCallback) {

    private val selectedIds = initialSelectedIds.toMutableSet()

    fun getSelectedCategoryIds(): List<Long> = selectedIds.toList()

    private fun handleSelectionChange(item: SelectableCategoryItem) {
        if (item is SelectableCategoryItem.AllCategories) {
            // Если кликнули на "Все категории"
            // Если он уже выбран, снимаем выбор. Если не выбран - выбираем его и очищаем все остальное.
            if (selectedIds.contains(item.id)) {
                selectedIds.clear()
            } else {
                selectedIds.clear()
                selectedIds.add(item.id)
            }
        } else {
            // Если кликнули на любую другую категорию
            // 1. Убираем "Все категории" из выбранных, так как выбор стал конкретным.
            selectedIds.remove(SelectableCategoryItem.AllCategories.id)

            // 2. Применяем логику выбора для конкретного элемента
            val isChecked = selectedIds.contains(item.id)
            if (item is SelectableCategoryItem.Parent) {
                // Каскадный выбор
                if (!isChecked) {
                    selectedIds.add(item.id)
                    item.category.children.forEach { selectedIds.add(it.id) }
                } else {
                    selectedIds.remove(item.id)
                    item.category.children.forEach { selectedIds.remove(it.id) }
                }
            } else if (item is SelectableCategoryItem.Child) {
                // Обычный выбор для подкатегории
                if (!isChecked) selectedIds.add(item.id) else selectedIds.remove(item.id)
                // Обновляем состояние родителя
                updateParentState(item.subcategory)
            }
        }
        // Перерисовываем весь список, чтобы обновить все чекбоксы
        notifyDataSetChanged()
    }

    private fun updateParentState(subcategory: Subcategory) {
        val parentItem = currentList.find { it is SelectableCategoryItem.Parent && it.category.id == subcategory.parentId } as? SelectableCategoryItem.Parent
        parentItem?.let {
            val allChildrenSelected = it.category.children.all { child -> selectedIds.contains(child.id) }
            if (allChildrenSelected) {
                selectedIds.add(it.id)
            } else {
                selectedIds.remove(it.id)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategorySelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, selectedIds.contains(item.id)) {
            handleSelectionChange(item)
        }
    }

    class CategoryViewHolder(private val binding: ItemCategorySelectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectableCategoryItem, isChecked: Boolean, onClick: () -> Unit) {
            binding.categoryName.text = item.title
            binding.checkbox.isChecked = isChecked

            when (item) {
                is SelectableCategoryItem.AllCategories -> {
                    binding.indentSpace.visibility = View.GONE
                    binding.separator.visibility = View.VISIBLE
                    binding.categoryIcon.setImageResource(R.drawable.ic_infinity) // Уникальная иконка
                }
                is SelectableCategoryItem.Parent -> {
                    binding.indentSpace.visibility = View.GONE
                    binding.separator.visibility = View.GONE
                    item.category.iconResourceId?.let { binding.categoryIcon.setImageResource(IconMap.idOf(it)) }
                }
                else -> {
                    binding.indentSpace.visibility = View.VISIBLE
                    binding.separator.visibility = View.GONE
                    binding.categoryIcon.setImageResource(R.drawable.ic_arrow_sub_down)
                }
            }
            // Весь элемент кликабельный
            itemView.setOnClickListener { onClick() }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SelectableCategoryItem>() {
        override fun areItemsTheSame(oldItem: SelectableCategoryItem, newItem: SelectableCategoryItem) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SelectableCategoryItem, newItem: SelectableCategoryItem) = oldItem == newItem
    }
}