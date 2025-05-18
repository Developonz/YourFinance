package com.example.yourfinance.presentation.ui.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemIconSelectableBinding
import com.example.yourfinance.presentation.IconMap


class SingleIconListAdapter(
    private val context: Context,
    @ColorInt private var currentSelectedColor: Int,
    private var selectedIconName: String?,
    private val onIconClick: (IconItem) -> Unit
) : ListAdapter<IconItem, SingleIconListAdapter.IconViewHolder>(IconItemDiffCallback()) {

    fun setSelectedIcon(iconName: String?) {
        val old = selectedIconName
        selectedIconName = iconName
        if (old != iconName) {
            currentList.forEachIndexed { index, item ->
                if (item.resourceId == old || item.resourceId == selectedIconName) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    fun setSelectedColor(@ColorInt color: Int) {
        val old = currentSelectedColor
        currentSelectedColor = color
        if (old != color) {
            currentList.indexOfFirst { it.resourceId == selectedIconName }
                .takeIf { it >= 0 }
                ?.let { notifyItemChanged(it) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding = ItemIconSelectableBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class IconViewHolder(
        private val binding: ItemIconSelectableBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: IconItem) {
            // Получаем настоящий ID через генераторную мапу
            val resId = IconMap.idOf(item.resourceId)
            binding.imageViewIconSelectableItem.setImageResource(resId)

            val isSelected = item.resourceId == selectedIconName
            if (isSelected) {
                // если в item.colorHex хранится цвет
                binding.cardIconSelectableRoot.setCardBackgroundColor(
                    item.colorHex ?: currentSelectedColor
                )
                val tint = if (ColorUtils.calculateLuminance(
                        item.colorHex ?: currentSelectedColor
                    ) > 0.5
                ) Color.BLACK else Color.WHITE
                binding.imageViewIconSelectableItem.setColorFilter(tint)
            } else {
                binding.cardIconSelectableRoot.setCardBackgroundColor(
                    context.getColor(R.color.default_icon_background)
                )
                binding.imageViewIconSelectableItem.setColorFilter(Color.WHITE)
            }

            binding.root.setOnClickListener { onIconClick(item) }
        }
    }

    class IconItemDiffCallback : DiffUtil.ItemCallback<IconItem>() {
        override fun areItemsTheSame(old: IconItem, new: IconItem): Boolean =
            old.resourceId == new.resourceId

        override fun areContentsTheSame(old: IconItem, new: IconItem): Boolean =
            old == new
    }
}
