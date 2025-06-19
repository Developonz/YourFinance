package com.example.yourfinance.presentation.ui.adapter

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
import com.example.yourfinance.presentation.databinding.ItemBudgetGroupHeaderBinding
import com.example.yourfinance.presentation.databinding.ItemBudgetManageBinding
import com.example.yourfinance.presentation.ui.fragment.manager.budget_manager.BudgetManagerListItem
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.format.DateTimeFormatter
import java.util.Locale

class BudgetManagerAdapter(
    private val onEditClick: (Budget) -> Unit,
    private val onDeleteClick: (Budget) -> Unit
) : ListAdapter<BudgetManagerListItem, RecyclerView.ViewHolder>(DiffCallback) {

    companion object {
        const val VIEW_TYPE_HEADER = 0
        const val VIEW_TYPE_ITEM = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is BudgetManagerListItem.HeaderItem -> VIEW_TYPE_HEADER
            is BudgetManagerListItem.BudgetItem -> VIEW_TYPE_ITEM
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder(ItemBudgetGroupHeaderBinding.inflate(inflater, parent, false))
            VIEW_TYPE_ITEM -> BudgetItemViewHolder(ItemBudgetManageBinding.inflate(inflater, parent, false), onEditClick, onDeleteClick)
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is BudgetManagerListItem.HeaderItem -> (holder as HeaderViewHolder).bind(item)
            is BudgetManagerListItem.BudgetItem -> (holder as BudgetItemViewHolder).bind(item.budget)
        }
    }

    class HeaderViewHolder(private val binding: ItemBudgetGroupHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(header: BudgetManagerListItem.HeaderItem) {
            binding.headerTitle.text = header.period.description.replaceFirstChar { it.uppercase() } + " Бюджет"
            binding.headerSubtitle.text = "Итого: ${StringHelper.getMoneyStr(header.totalSpent)} / ${StringHelper.getMoneyStr(header.totalBalance)}"
        }
    }

    class BudgetItemViewHolder(
        private val binding: ItemBudgetManageBinding,
        private val onEditClick: (Budget) -> Unit,
        private val onDeleteClick: (Budget) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale("ru"))
        private val context: Context = itemView.context

        fun bind(budget: Budget) {
            binding.textTitle.text = budget.title
            val dates = if (budget.startDate != null && budget.endDate != null) {
                "${budget.startDate!!.format(dateFormatter)} - ${budget.endDate!!.format(dateFormatter)}"
            } else {
                budget.period.description
            }
            binding.textPeriod.text = dates

            val progress = (budget.remaining.divide(budget.budgetLimit, 2, RoundingMode.HALF_UP) * BigDecimal("100.00")).toInt()
            binding.progressBar.progress = progress
            binding.progressBar.progressTintList = ColorStateList.valueOf(getProgressColor(progress))

            binding.textRemainingValue.text = StringHelper.getMoneyStr(budget.remaining)
            binding.textTotalValue.text = "из ${StringHelper.getMoneyStr(budget.budgetLimit)}"

            binding.buttonEdit.setOnClickListener { onEditClick(budget) }
            binding.buttonDelete.setOnClickListener { onDeleteClick(budget) }
        }

        private fun getProgressColor(progress: Int): Int {
            return when {
                progress > 50 -> ContextCompat.getColor(context, R.color.green_positive)
                progress > 20 -> ContextCompat.getColor(context, R.color.yellow_warning)
                else -> ContextCompat.getColor(context, R.color.red_negative)
            }
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<BudgetManagerListItem>() {
        override fun areItemsTheSame(oldItem: BudgetManagerListItem, newItem: BudgetManagerListItem): Boolean {
            return when {
                oldItem is BudgetManagerListItem.HeaderItem && newItem is BudgetManagerListItem.HeaderItem -> oldItem.period == newItem.period
                oldItem is BudgetManagerListItem.BudgetItem && newItem is BudgetManagerListItem.BudgetItem -> oldItem.budget.id == newItem.budget.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: BudgetManagerListItem, newItem: BudgetManagerListItem): Boolean {
            return oldItem == newItem
        }
    }
}