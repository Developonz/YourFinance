package com.example.yourfinance.presentation.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.presentation.databinding.ItemEmptyPlaceBinding
import com.example.yourfinance.presentation.databinding.ItemHeaderTransactionsBinding
import com.example.yourfinance.presentation.databinding.ItemTransactionBinding
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.presentation.ui.adapter.list_item.TransactionListItem
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.StringHelper
import java.time.LocalDate


class TransactionsRecyclerViewListAdapter(val editClick: (transaction: Transaction) -> Unit) : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(
    DIFF_CALLBACK
) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val VIEW_TYPE_EMPTY = 2

        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<TransactionListItem>() {
            override fun areItemsTheSame(
                oldItem: TransactionListItem,
                newItem: TransactionListItem
            ): Boolean {
                return when {
                    oldItem is TransactionListItem.Header && newItem is TransactionListItem.Header ->
                        oldItem.date == newItem.date
                    oldItem is TransactionListItem.TransactionItem && newItem is TransactionListItem.TransactionItem ->
                        oldItem.transaction.id == newItem.transaction.id &&
                                oldItem.transaction.javaClass == newItem.transaction.javaClass
                    else -> false
                }
            }

            override fun areContentsTheSame(
                oldItem: TransactionListItem,
                newItem: TransactionListItem
            ): Boolean {
                return oldItem == newItem
            }
        }
    }


    // ViewHolder для заголовка
    class HeaderViewHolder(private val binding: ItemHeaderTransactionsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: TransactionListItem.Header) {
            binding.dayOfMounth.text = StringHelper.getDayOfMonthStr(header.date)
            binding.dayOfWeek.text = if (header.date <= LocalDate.now()) StringHelper.getDayOfWeekStr(header.date) else "Будущая"
            binding.mounth.text = StringHelper.getMonthYearStr(header.date)
            binding.balance.text = StringHelper.getMoneyStr(header.balance)
        }
    }

    // ViewHolder для транзакции
    class TransactionViewHolder(private val binding: ItemTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionListItem.TransactionItem, editClick: (transaction: Transaction) -> Unit) {
            val transaction = item.transaction
            if (transaction is Payment) {
                if (transaction.category.iconResourceId != null) {
                    binding.transactionImage.setImageResource(transaction.category.iconResourceId!!)
                    val iconTintColor =
                        if (ColorUtils.calculateLuminance(Color.parseColor(transaction.category.colorHex)) > 0.5) Color.BLACK else Color.WHITE
                    binding.transactionImage.setColorFilter(iconTintColor)
                }
                binding.transactionImage.setBackgroundColor(Color.parseColor(transaction.category.colorHex))
            }


            binding.title.text = when (transaction) {
                is Payment -> transaction.note.ifEmpty { transaction.category.title }
                is Transfer -> transaction.note.ifEmpty { "Перевод" }
                else -> ""
            }
            binding.account.text = when (transaction) {
                is Payment -> {
                    val acc = transaction.moneyAccount
                    acc.title.ifEmpty { "Неизвестно" }
                }
                is Transfer -> {
                    val accFrom = transaction.moneyAccFrom
                    val accTo = transaction.moneyAccTo
                    if (accFrom.title.isNotEmpty() && accTo.title.isNotEmpty())
                        "${accFrom.title} -> ${accTo.title}" else "Неизвестно"
                }
                else -> "Неизвестно"
            }
            binding.price.text = StringHelper.getMoneyStr(transaction.balance)
            if (transaction is Payment) {
                binding.price.setTextColor(if (transaction.type == TransactionType.INCOME)
                    Color.GREEN else Color.RED)
            }

            binding.root.setOnClickListener {
                editClick(item.transaction)
            }
        }
    }

    class EmptyViewHolder(binding: ItemEmptyPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {}
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.Header -> VIEW_TYPE_HEADER
            is TransactionListItem.TransactionItem -> VIEW_TYPE_TRANSACTION
            else -> VIEW_TYPE_EMPTY
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = ItemHeaderTransactionsBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_TRANSACTION -> {
                val binding = ItemTransactionBinding.inflate(inflater, parent, false)
                TransactionViewHolder(binding)
            }
            else -> {
                val binding = ItemEmptyPlaceBinding.inflate(inflater, parent, false)
                EmptyViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TransactionListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item, editClick)
            is TransactionListItem.EmptyItem -> (holder as EmptyViewHolder).bind()
        }
    }
}
