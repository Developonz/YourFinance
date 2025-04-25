package com.example.yourfinance.presentation.ui.adapter.wallet_page

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.databinding.BudgetItemBinding
import com.example.yourfinance.databinding.BudgetItemCreateBinding
import com.example.yourfinance.databinding.BudgetItemEmptyListBinding
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.presentation.ui.adapter.list_item.BudgetListItem
import com.example.yourfinance.util.StringHelper



class WalletBudgetAdapter : ListAdapter<BudgetListItem, RecyclerView.ViewHolder>(
    DIFF_CALLBACK
) {
    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BudgetListItem>() {
            override fun areItemsTheSame(
                oldItem: BudgetListItem,
                newItem: BudgetListItem
            ): Boolean {
                return when {
                    oldItem is BudgetListItem.BudgetItem && newItem is BudgetListItem.BudgetItem -> oldItem.budget.id == newItem.budget.id
                    oldItem is BudgetListItem.CreateBudget && newItem is BudgetListItem.CreateBudget -> true
                    oldItem is BudgetListItem.EmptyList && newItem is BudgetListItem.EmptyList -> true
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: BudgetListItem,
                newItem: BudgetListItem
            ): Boolean {
                return (oldItem is BudgetListItem.BudgetItem && newItem is BudgetListItem.BudgetItem && oldItem.budget == newItem.budget) ||
                        (oldItem is BudgetListItem.CreateBudget && newItem is BudgetListItem.CreateBudget) ||
                        (oldItem is BudgetListItem.EmptyList && newItem is BudgetListItem.EmptyList)
            }
        }

        const val BUDGET = 0
        const val EMPTY = 1
        const val NEW = 2
    }

    class BudgetItemViewHolder(private val binding: BudgetItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Budget) {
            binding.textTitle.text = item.title
            binding.textBalance.text = StringHelper.getMoneyStr(item.balance)
            binding.textPeriod.text = item.period.upperDescription
        }
    }

    class EmptyListViewHolder(private val binding: BudgetItemEmptyListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {}
    }

    class CreateBudgetViewHolder(private val binding: BudgetItemCreateBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind() {}
    }

    override fun getItemViewType(position: Int): Int {
        return when(val item = getItem(position)) {
            is BudgetListItem.BudgetItem -> BUDGET
            is BudgetListItem.EmptyList -> EMPTY
            is BudgetListItem.CreateBudget -> NEW
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when(viewType) {
            BUDGET -> BudgetItemViewHolder(BudgetItemBinding.inflate(inflater, parent, false))
            EMPTY -> EmptyListViewHolder(BudgetItemEmptyListBinding.inflate(inflater, parent, false))
            NEW -> CreateBudgetViewHolder(BudgetItemCreateBinding.inflate(inflater, parent, false))
            else -> EmptyListViewHolder(BudgetItemEmptyListBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(val item = getItem(position)) {
            is BudgetListItem.BudgetItem -> (holder as BudgetItemViewHolder).bind(item.budget)
            is BudgetListItem.EmptyList -> (holder as EmptyListViewHolder).bind()
            is BudgetListItem.CreateBudget -> (holder as CreateBudgetViewHolder).bind()
        }
    }
}
