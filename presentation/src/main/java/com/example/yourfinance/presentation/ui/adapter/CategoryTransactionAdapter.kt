package com.example.yourfinance.presentation.ui.adapter

import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemTransactionCategoryBinding
import com.example.yourfinance.presentation.databinding.ItemTransactionSubcategoryBinding
import com.example.yourfinance.presentation.ui.adapter.list_item.CategoryListItem
import com.google.android.material.imageview.ShapeableImageView // Убедимся, что импорт есть

class CategoryTransactionAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<CategoryListItem, RecyclerView.ViewHolder>(
    CategoryListItemDiffCallback()
) {

    interface OnItemClickListener {
        fun onItemClick(item: CategoryListItem)
    }

    // Цвет по умолчанию для невыбранных элементов (темно-серый, например)
    private val defaultUnselectedColorHex = "#424242" // Material Grey 800
    // Цвет по умолчанию для иконки на невыбранном фоне
    private val defaultUnselectedIconTintColor = Color.parseColor("#BDBDBD") // Material Grey 400


    // Общий метод для установки фона и цвета иконки
    private fun setIconAppearance(
        imageView: ShapeableImageView,
        hexColorString: String?,         // Оригинальный цвет элемента
        defaultHexColorIfNull: String,   // Цвет по умолчанию, если у элемента нет своего
        isSelected: Boolean
    ) {
        val actualHexColor: String
        var iconTintColor: Int

        if (isSelected) {
            actualHexColor = hexColorString ?: defaultHexColorIfNull
            val parsedColor = try { Color.parseColor(actualHexColor) } catch (e: IllegalArgumentException) { Color.parseColor(defaultHexColorIfNull) }
            imageView.setBackgroundColor(parsedColor)
            val luminance = ColorUtils.calculateLuminance(parsedColor)
            iconTintColor = if (luminance > 0.5) Color.BLACK else Color.WHITE
        } else {
            // Не выбранный элемент - используем приглушенный цвет
            actualHexColor = defaultUnselectedColorHex
            imageView.setBackgroundColor(Color.parseColor(actualHexColor))
            iconTintColor = defaultUnselectedIconTintColor // Для приглушенного фона - светлая иконка
        }
        imageView.setColorFilter(iconTintColor)
    }


    class CategoryTypeViewHolder(private val binding: ItemTransactionCategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindCategory(item: CategoryListItem.CategoryItem, listener: OnItemClickListener, adapter: CategoryTransactionAdapter) {
            val category = item.category
            binding.imageViewCategoryIcon.setImageResource(category.iconResourceId ?: R.drawable.ic_checkmark)
            adapter.setIconAppearance(
                binding.imageViewCategoryIcon,
                category.colorHex,
                adapter.defaultUnselectedColorHex,
                item.isSelected
            )
            binding.titleCategory.text = category.title
            binding.root.setOnClickListener {
                listener.onItemClick(item)
            }
        }

        fun bindSettingsButton(item: CategoryListItem.SettingsButtonItem, listener: OnItemClickListener, adapter: CategoryTransactionAdapter) {
            binding.imageViewCategoryIcon.setImageResource(R.drawable.ic_settings)
            // Для кнопки настроек всегда свой стиль
            val settingsButtonColor = "#F0F0F0" // Светлый фон для кнопки настроек
            val parsedColor = Color.parseColor(settingsButtonColor)
            binding.imageViewCategoryIcon.setBackgroundColor(parsedColor)
            val luminance = ColorUtils.calculateLuminance(parsedColor)
            val iconTintColor = if (luminance > 0.5) Color.BLACK else Color.WHITE
            binding.imageViewCategoryIcon.setColorFilter(iconTintColor)

            binding.titleCategory.text = itemView.context.getString(R.string.settings_text)
            binding.root.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    class SubcategoryViewHolder(private val binding: ItemTransactionSubcategoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategoryListItem.SubcategoryItem, listener: OnItemClickListener, adapter: CategoryTransactionAdapter) {
            val subcategory = item.subcategory
            val imageView = binding.root.findViewById<ShapeableImageView>(R.id.imageViewSubcategoryIcon)
            imageView?.let {
                adapter.setIconAppearance(
                    it,
                    subcategory.colorHex,
                    adapter.defaultUnselectedColorHex,
                    item.isSelected
                )
            }

            binding.titleSubcategory.text = subcategory.title
            binding.root.setOnClickListener {
                listener.onItemClick(item)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return getItem(position).viewType
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            CategoryListItem.VIEW_TYPE_CATEGORY -> { // Кнопка настроек теперь имеет свой bind
                CategoryTypeViewHolder(
                    ItemTransactionCategoryBinding.inflate(inflater, parent, false)
                )
            }
            CategoryListItem.VIEW_TYPE_SETTINGS_BUTTON -> { // Отдельный кейс для ясности, хотя ViewHolder тот же
                CategoryTypeViewHolder(
                    ItemTransactionCategoryBinding.inflate(inflater, parent, false)
                )
            }
            CategoryListItem.VIEW_TYPE_SUBCATEGORY -> {
                SubcategoryViewHolder(
                    ItemTransactionSubcategoryBinding.inflate(inflater, parent, false)
                )
            }
            else -> throw IllegalArgumentException("Unknown viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CategoryListItem.CategoryItem -> (holder as CategoryTypeViewHolder).bindCategory(item, listener, this)
            is CategoryListItem.SettingsButtonItem -> (holder as CategoryTypeViewHolder).bindSettingsButton(item, listener, this)
            is CategoryListItem.SubcategoryItem -> (holder as SubcategoryViewHolder).bind(item, listener, this)
        }
    }
}

class CategoryListItemDiffCallback : DiffUtil.ItemCallback<CategoryListItem>() {
    override fun areItemsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: CategoryListItem, newItem: CategoryListItem): Boolean {
        // Теперь DiffUtil будет также учитывать изменение isSelected
        return oldItem == newItem
    }
}