package com.example.yourfinance.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.databinding.BudgetItemBinding
import com.example.yourfinance.databinding.SectionsHeaderBinding
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.utils.StringHelper


class WalletBudgetAdapter : ListAdapter<Budget, WalletBudgetAdapter.BudgetViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Budget>() {
            override fun areItemsTheSame(
                oldItem: Budget,
                newItem: Budget
            ): Boolean {
                return when {
                    oldItem.id == newItem.id -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: Budget,
                newItem: Budget
            ): Boolean {
                return oldItem == newItem
            }
        }
    }

    class BudgetViewHolder(private val binding: BudgetItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Budget) {
            binding.textTitle.text = item.title
            binding.textBalance.text = StringHelper.getMoneyStr(item.balance)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return BudgetViewHolder(BudgetItemBinding.inflate(inflater, parent, false))
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
