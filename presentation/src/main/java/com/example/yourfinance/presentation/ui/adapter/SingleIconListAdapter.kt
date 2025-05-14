package com.example.yourfinance.presentation.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.R // Убедись, что R импортирован правильно
import com.example.yourfinance.presentation.databinding.ItemIconSelectableBinding // Используем тот же layout, что и для категорий

class SingleIconListAdapter(
    private val context: Context,
    private var currentSelectedColorHex: String,
    private var selectedIconResId: Int?,
    private val onIconClick: (IconItem) -> Unit
) : ListAdapter<IconItem, SingleIconListAdapter.IconViewHolder>(IconItemDiffCallback()) {

    fun setSelectedIcon(iconResId: Int?) {
        val oldSelected = selectedIconResId
        selectedIconResId = iconResId
        if (oldSelected != iconResId) {
            // Обновить старую и новую выбранные иконки
            currentList.forEachIndexed { index, item ->
                if (item.resourceId == oldSelected || item.resourceId == selectedIconResId) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    fun setSelectedColor(colorHex: String) {
        val oldColor = currentSelectedColorHex
        currentSelectedColorHex = colorHex
        if (oldColor != colorHex) {
            // Перерисовать только выделенный элемент, если цвет изменился
            currentList.forEachIndexed { index, item ->
                if (item.resourceId == selectedIconResId) {
                    notifyItemChanged(index)
                    return@forEachIndexed // Выходим, так как только одна иконка может быть выбрана
                }
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding = ItemIconSelectableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IconViewHolder(private val binding: ItemIconSelectableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(iconItem: IconItem) {
            binding.imageViewIconSelectableItem.setImageResource(iconItem.resourceId)
            val isSelected = iconItem.resourceId == selectedIconResId

            if (isSelected) {
                try {
                    val color = Color.parseColor(currentSelectedColorHex)
                    binding.cardIconSelectableRoot.setCardBackgroundColor(color)
                    val iconTintColor = if (ColorUtils.calculateLuminance(color) > 0.5) Color.BLACK else Color.WHITE
                    binding.imageViewIconSelectableItem.setColorFilter(iconTintColor)
                } catch (e: IllegalArgumentException) {
                    binding.cardIconSelectableRoot.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.teal_200) // Нужен цвет для ошибки
                    )
                    binding.imageViewIconSelectableItem.setColorFilter(Color.WHITE)
                }
            } else {
                binding.cardIconSelectableRoot.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.default_icon_background) // Стандартный фон невыбранной иконки
                )
                binding.imageViewIconSelectableItem.setColorFilter(Color.WHITE) // Стандартный цвет невыбранной иконки
            }

            binding.root.setOnClickListener {
                onIconClick(iconItem)
            }
        }
    }

    class IconItemDiffCallback : DiffUtil.ItemCallback<IconItem>() {
        override fun areItemsTheSame(oldItem: IconItem, newItem: IconItem): Boolean {
            return oldItem.resourceId == newItem.resourceId
        }

        override fun areContentsTheSame(oldItem: IconItem, newItem: IconItem): Boolean {
            return oldItem == newItem
        }
    }
}