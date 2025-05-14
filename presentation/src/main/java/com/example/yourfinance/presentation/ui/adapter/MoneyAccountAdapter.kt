package com.example.yourfinance.presentation.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.databinding.ItemAccountManagerListBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.StringHelper




class MoneyAccountAdapter(
    val deleteClick: (acc: MoneyAccount) -> Unit,
    val editClick: (acc: MoneyAccount) -> Unit
) : ListAdapter<MoneyAccount, MoneyAccountAdapter.MoneyAccountViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MoneyAccount>() {
            override fun areItemsTheSame(
                oldItem: MoneyAccount,
                newItem: MoneyAccount
            ): Boolean {
                return when {
                    oldItem.id == newItem.id -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: MoneyAccount,
                newItem: MoneyAccount
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class MoneyAccountViewHolder(private val binding: ItemAccountManagerListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(
            item: MoneyAccount,
            deleteClick: (acc: MoneyAccount) -> Unit,
            editClick: (acc: MoneyAccount) -> Unit)
        {
            binding.accountImage.setImageResource(item.iconResourceId!!)
            binding.accountImage.setBackgroundColor(Color.parseColor(item.colorHex))
            val iconTintColor = if (ColorUtils.calculateLuminance(Color.parseColor(item.colorHex)) > 0.5) Color.BLACK else Color.WHITE
            binding.accountImage.setColorFilter(iconTintColor)


            binding.accountTitle.text = item.title
            binding.textAccountBalance.text = StringHelper.getMoneyStr(item.balance)

            binding.imageDelete.setOnClickListener {
                deleteClick(item)
            }

            binding.root.setOnClickListener {
                editClick(item)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoneyAccountViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MoneyAccountViewHolder(ItemAccountManagerListBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: MoneyAccountViewHolder, position: Int) {
        holder.bind(getItem(position), deleteClick, editClick)
    }
}
