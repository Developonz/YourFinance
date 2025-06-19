package com.example.yourfinance.presentation.ui.adapter.wallet_page

import android.content.Context
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.StringHelper
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ItemWalletBudgetAddBinding
import com.example.yourfinance.presentation.databinding.ItemWalletBudgetBinding
import com.example.yourfinance.presentation.databinding.ItemWalletBudgetEmptyBinding
import com.example.yourfinance.presentation.ui.adapter.list_item.BudgetListItem
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.Locale

class WalletBudgetAdapter(
    private val onBudgetClick: (Long) -> Unit,
    private val onAddClick: () -> Unit
) : ListAdapter<BudgetListItem, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BudgetListItem>() {
            override fun areItemsTheSame(oldItem: BudgetListItem, newItem: BudgetListItem): Boolean {
                return when {
                    oldItem is BudgetListItem.BudgetItem && newItem is BudgetListItem.BudgetItem -> oldItem.budget.id == newItem.budget.id
                    oldItem is BudgetListItem.CreateBudget && newItem is BudgetListItem.CreateBudget -> true
                    oldItem is BudgetListItem.EmptyList && newItem is BudgetListItem.EmptyList -> true
                    else -> false
                }
            }
            override fun areContentsTheSame(oldItem: BudgetListItem, newItem: BudgetListItem): Boolean = oldItem == newItem
        }
        const val VIEW_TYPE_BUDGET = 0
        const val VIEW_TYPE_CREATE = 1
        const val VIEW_TYPE_EMPTY = 2
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is BudgetListItem.BudgetItem -> VIEW_TYPE_BUDGET
            is BudgetListItem.CreateBudget -> VIEW_TYPE_CREATE
            is BudgetListItem.EmptyList -> VIEW_TYPE_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_BUDGET -> BudgetItemViewHolder(ItemWalletBudgetBinding.inflate(inflater, parent, false), onBudgetClick)
            VIEW_TYPE_CREATE -> CreateBudgetViewHolder(ItemWalletBudgetAddBinding.inflate(inflater, parent, false), onAddClick)
            VIEW_TYPE_EMPTY -> EmptyListViewHolder(ItemWalletBudgetEmptyBinding.inflate(inflater, parent, false), onAddClick)
            else -> throw IllegalArgumentException("Unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BudgetItemViewHolder) {
            val item = getItem(position) as BudgetListItem.BudgetItem
            holder.bind(item.budget)
        }
    }

    class BudgetItemViewHolder(
        private val binding: ItemWalletBudgetBinding,
        private val onBudgetClick: (Long) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        private val context: Context = itemView.context
        private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM", Locale("ru"))

        fun bind(budget: Budget) {
            binding.textTitle.text = budget.title
            binding.textRemainingValue.text = "${StringHelper.getMoneyStr(budget.remaining)} осталось"
            binding.textTotalValue.text = "из ${StringHelper.getMoneyStr(budget.budgetLimit)}"

            val periodText = if (budget.startDate != null) "${budget.startDate!!.format(dateFormatter)} - ${budget.endDate!!.format(dateFormatter)}" else budget.period.description
            binding.textPeriod.text = periodText

            val progress = (budget.remaining.divide(budget.budgetLimit, 2, RoundingMode.HALF_UP) * BigDecimal("100.00")).toInt()
            binding.progressBar.progress = progress
            binding.progressBar.progressTintList = ColorStateList.valueOf(getProgressColor(progress))

            itemView.setOnClickListener { onBudgetClick(budget.id) }
        }

        private fun getProgressColor(progress: Int): Int {
            return when {
                progress > 50 -> ContextCompat.getColor(context, R.color.green_positive)
                progress > 20 -> ContextCompat.getColor(context, R.color.yellow_warning)
                else -> ContextCompat.getColor(context, R.color.red_negative)
            }
        }
    }

    class CreateBudgetViewHolder(binding: ItemWalletBudgetAddBinding, onAddClick: () -> Unit) : RecyclerView.ViewHolder(binding.root) {
        init { itemView.setOnClickListener { onAddClick() } }
    }

    class EmptyListViewHolder(binding: ItemWalletBudgetEmptyBinding, onAddClick: () -> Unit) : RecyclerView.ViewHolder(binding.root) {
        init { itemView.setOnClickListener { onAddClick() } }
    }
}