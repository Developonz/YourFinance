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
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemIconGroupHeaderBinding
import com.example.yourfinance.presentation.databinding.ItemIconSelectableBinding

class IconGroupAdapter(
    private val context: Context,
    private var currentSelectedColorHex: String, // Только для цвета выделения
    private val onIconClick: (IconItem) -> Unit
) : ListAdapter<DisplayableItem, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_CONTENT = 1
    }

    fun setSelectedColor(colorHex: String) {
        val oldColor = currentSelectedColorHex
        currentSelectedColorHex = colorHex
        if (oldColor != colorHex) {
            // Перерисовать только видимые выделенные элементы, если цвет изменился
            // Это более сложная логика, для простоты можно переотправить список из фрагмента,
            // или найти выделенный элемент и вызвать notifyItemChanged для него.
            // Простейший вариант - фрагмент сам пересоздаст список и вызовет submitList
            // Если же элементов много, то лучше найти индекс выделенного элемента и обновить его.
            currentList.forEachIndexed { index, item ->
                if (item is DisplayableItem.ContentItem && item.isSelected) {
                    notifyItemChanged(index)
                }
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is DisplayableItem.HeaderItem -> VIEW_TYPE_HEADER
            is DisplayableItem.ContentItem -> VIEW_TYPE_CONTENT
            // getItem(position) может вернуть null, если список пуст или позиция некорректна (редко с ListAdapter)
            null -> throw IllegalStateException("Item at position $position is null")
        }
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
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = getItem(position)
        when (holder) {
            is HeaderViewHolder -> {
                holder.bind(item as DisplayableItem.HeaderItem)
            }
            is IconViewHolder -> {
                holder.bind(item as DisplayableItem.ContentItem)
            }
        }
    }

    inner class HeaderViewHolder(private val binding: ItemIconGroupHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(headerItem: DisplayableItem.HeaderItem) {
            binding.textViewUnifiedHeaderTitle.text = headerItem.title
        }
    }

    inner class IconViewHolder(private val binding: ItemIconSelectableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(contentItem: DisplayableItem.ContentItem) {
            val iconItem = contentItem.iconItem
            binding.imageViewIconSelectableItem.setImageResource(iconItem.resourceId)

            if (contentItem.isSelected) {
                try {
                    val color = Color.parseColor(currentSelectedColorHex)
                    binding.cardIconSelectableRoot.setCardBackgroundColor(color)
                    // Логика контрастного цвета для иконки из вашего InnerIconAdapter
                    val iconTintColor = if (ColorUtils.calculateLuminance(color) > 0.5) Color.BLACK else Color.WHITE
                    binding.imageViewIconSelectableItem.setColorFilter(iconTintColor)
                } catch (e: IllegalArgumentException) {
                    // Фоллбэк, если цвет невалидный
                    binding.cardIconSelectableRoot.setCardBackgroundColor(
                        ContextCompat.getColor(context, R.color.default_icon_background) // Убедитесь, что этот цвет есть
                    )
                    binding.imageViewIconSelectableItem.setColorFilter(Color.WHITE)
                }
            } else {
                binding.cardIconSelectableRoot.setCardBackgroundColor(
                    ContextCompat.getColor(context, R.color.default_icon_background)
                )
                binding.imageViewIconSelectableItem.setColorFilter(Color.WHITE) // Цвет по умолчанию для невыбранных иконок
            }

            binding.root.setOnClickListener {
                onIconClick(iconItem)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<DisplayableItem>() {
        override fun areItemsTheSame(oldItem: DisplayableItem, newItem: DisplayableItem): Boolean {
            return oldItem.diffId == newItem.diffId
        }

        override fun areContentsTheSame(oldItem: DisplayableItem, newItem: DisplayableItem): Boolean {
            return oldItem == newItem // Data классы корректно реализуют equals()
        }
    }
}