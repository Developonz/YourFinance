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
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemIconGroupHeaderBinding
import com.example.yourfinance.presentation.databinding.ItemIconSelectableBinding



class IconGroupAdapter(
    private val context: Context,
    private var currentSelectedColor: Int,
    private val onIconClick: (IconItem) -> Unit
) : ListAdapter<DisplayableItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        const val VIEW_TYPE_HEADER  = 0
        const val VIEW_TYPE_CONTENT = 1
    }

    /** Обновить цвет выделения и перерисовать только выбранные items */
    fun setSelectedColor(color: Int) {
        val old = currentSelectedColor
        currentSelectedColor = color
        if (old != color) {
            currentList.forEachIndexed { index, item ->
                if (item is DisplayableItem.ContentItem && item.isSelected) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is DisplayableItem.HeaderItem  -> VIEW_TYPE_HEADER
            is DisplayableItem.ContentItem -> VIEW_TYPE_CONTENT
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemIconGroupHeaderBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_CONTENT -> {
                val binding = ItemIconSelectableBinding.inflate(inflater, parent, false)
                IconViewHolder(binding)
            }
            else -> error("Invalid viewType $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as DisplayableItem.HeaderItem)
            is IconViewHolder   -> holder.bind(getItem(position) as DisplayableItem.ContentItem)
        }
    }

    inner class HeaderViewHolder(
        private val binding: ItemIconGroupHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DisplayableItem.HeaderItem) {
            binding.textViewUnifiedHeaderTitle.text = item.title
        }
    }

    inner class IconViewHolder(
        private val binding: ItemIconSelectableBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DisplayableItem.ContentItem) {
            val iconItem = item.iconItem
            // резолвим строковый ключ в Int-ресурс
            val resId = IconMap.idOf(iconItem.resourceId)
            binding.imageViewIconSelectableItem.setImageResource(resId)

            if (item.isSelected) {
                // фон выбранного цвета
                binding.cardIconSelectableRoot.setCardBackgroundColor(currentSelectedColor)
                // контрастный tint
                val tint = if (ColorUtils.calculateLuminance(currentSelectedColor) > 0.5)
                    Color.BLACK else Color.WHITE
                binding.imageViewIconSelectableItem.setColorFilter(tint)
            } else {
                // дефолтный фон + белая иконка
                binding.cardIconSelectableRoot.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.default_icon_background)
                )
                binding.imageViewIconSelectableItem.setColorFilter(Color.WHITE)
            }

            binding.root.setOnClickListener {
                onIconClick(iconItem)
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<DisplayableItem>() {
        override fun areItemsTheSame(old: DisplayableItem, new: DisplayableItem): Boolean =
            old.diffId == new.diffId

        override fun areContentsTheSame(old: DisplayableItem, new: DisplayableItem): Boolean =
            old == new
    }
}
