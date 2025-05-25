package com.example.yourfinance.presentation.ui.adapter

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemAccountManagerListBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.StringHelper

class MoneyAccountAdapter(
    private val deleteClick: (acc: MoneyAccount) -> Unit,
    private val defaultClick: (acc: MoneyAccount) -> Unit,
    private val editClick: (acc: MoneyAccount) -> Unit
) : ListAdapter<MoneyAccount, MoneyAccountAdapter.MoneyAccountViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MoneyAccount>() {
            override fun areItemsTheSame(oldItem: MoneyAccount, newItem: MoneyAccount): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: MoneyAccount, newItem: MoneyAccount): Boolean =
                oldItem == newItem
        }
    }

    inner class MoneyAccountViewHolder(
        private val binding: ItemAccountManagerListBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(
            item: MoneyAccount,
            deleteClick: (acc: MoneyAccount) -> Unit,
            editClick: (acc: MoneyAccount) -> Unit
        ) {
            val iconKey = item.iconResourceId
            val iconResId = iconKey
                ?.let { IconMap.idOf(it) }
                ?: R.drawable.ic_mobile_wallet
            binding.accountImage.setImageResource(iconResId)

            item.colorHex?.let { colorInt ->
                // задаём tint фона
                binding.accountImage.backgroundTintList =
                    ColorStateList.valueOf(colorInt)
                val iconTint = if (ColorUtils.calculateLuminance(colorInt) > 0.5f)
                    Color.BLACK
                else
                    Color.WHITE
                binding.accountImage.imageTintList =
                    ColorStateList.valueOf(iconTint)
            } ?: run {
                binding.accountImage.backgroundTintList =
                    ColorStateList.valueOf(
                        binding.root.context.getColor(R.color.default_icon_background)
                    )
                binding.accountImage.imageTintList =
                    ColorStateList.valueOf(Color.BLACK)
            }

            // 3) Текст
            binding.accountTitle.text = item.title
            binding.textAccountBalance.text =
                StringHelper.getMoneyStr(item.balance)

            if (item.default) {
                binding.imagePin.imageTintList = ColorStateList.valueOf(Color.YELLOW)
            }

            // 4) Клики
            binding.imageDelete.setOnClickListener { deleteClick(item) }
            binding.root.setOnClickListener { editClick(item) }
            binding.imagePin.setOnClickListener {
                defaultClick(item)
                binding.imagePin.imageTintList = ColorStateList.valueOf(Color.YELLOW)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoneyAccountViewHolder {
        val binding = ItemAccountManagerListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MoneyAccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MoneyAccountViewHolder, position: Int) {
        holder.bind(getItem(position), deleteClick, editClick)
    }
}
