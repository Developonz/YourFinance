package com.example.yourfinance.presentation.ui.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemTransactionCategoryBinding
import com.example.yourfinance.presentation.databinding.ItemTransactionSubcategoryBinding
import com.example.yourfinance.presentation.ui.adapter.list_item.CategoryListItem
import com.google.android.material.imageview.ShapeableImageView

class CategoryTransactionAdapter(
    private val listener: OnItemClickListener
) : ListAdapter<CategoryListItem, RecyclerView.ViewHolder>(
    CategoryListItemDiff()
) {

    interface OnItemClickListener {
        fun onItemClick(item: CategoryListItem)
    }

    @ColorRes private val unselectedBgRes = R.color.default_icon_background
    private val unselectedTint = android.graphics.Color.WHITE

    private fun setAppearance(
        iv: ShapeableImageView,
        @ColorInt bgColor: Int?,
        @ColorRes selBgRes: Int,
        isSelected: Boolean
    ) {
        val ctx = iv.context
        val selBg = ContextCompat.getColor(ctx, selBgRes)
        val unselBg = ContextCompat.getColor(ctx, unselectedBgRes)
        val bg = if (isSelected) bgColor ?: selBg else unselBg
        iv.setBackgroundColor(bg)
        iv.setColorFilter(
            if (isSelected && ColorUtils.calculateLuminance(bg) > 0.5)
                android.graphics.Color.BLACK else unselectedTint
        )
    }

    override fun getItemViewType(position: Int) = getItem(position).viewType

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        when (viewType) {
            CategoryListItem.VIEW_TYPE_CATEGORY,
            CategoryListItem.VIEW_TYPE_SETTINGS_BUTTON ->
                CategoryVH(ItemTransactionCategoryBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            CategoryListItem.VIEW_TYPE_SUBCATEGORY ->
                SubVH(ItemTransactionSubcategoryBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false))
            else -> error("Unknown viewType")
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        val item = getItem(pos)
        when (holder) {
            is CategoryVH -> if (item is CategoryListItem.CategoryItem) holder.bindCategory(item) else holder.bindSettings()
            is SubVH      -> holder.bind(item as CategoryListItem.SubcategoryItem)
        }
        holder.itemView.setOnClickListener { listener.onItemClick(item) }
    }

    inner class CategoryVH(val b: ItemTransactionCategoryBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bindCategory(item: CategoryListItem.CategoryItem) {
            val c = item.category
            b.imageViewCategoryIcon.setImageResource(c.iconResourceId?.let(IconMap::idOf) ?: R.drawable.ic_checkmark)
            setAppearance(b.imageViewCategoryIcon, c.colorHex, R.color.yellow, item.isSelected)
            b.titleCategory.apply {
                text = c.title
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        }

        fun bindSettings() {
            b.imageViewCategoryIcon.setImageResource(R.drawable.ic_settings)
            setAppearance(b.imageViewCategoryIcon, null, R.color.default_icon_background, false)
            b.titleCategory.apply {
                text = b.root.context.getString(R.string.settings_text)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        }
    }

    inner class SubVH(val b: ItemTransactionSubcategoryBinding) :
        RecyclerView.ViewHolder(b.root) {

        fun bind(item: CategoryListItem.SubcategoryItem) {
            val s = item.subcategory
            setAppearance(b.imageViewSubcategoryIcon, s.colorHex, R.color.yellow, item.isSelected)
            b.titleSubcategory.apply {
                text = s.title
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
            }
        }
    }

    class CategoryListItemDiff : DiffUtil.ItemCallback<CategoryListItem>() {
        override fun areItemsTheSame(old: CategoryListItem, new: CategoryListItem) =
            old.id == new.id
        override fun areContentsTheSame(old: CategoryListItem, new: CategoryListItem) =
            old == new
    }
}
