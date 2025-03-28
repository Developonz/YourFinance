package com.example.yourfinance.model.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.databinding.EmptyPlaceBinding
import com.example.yourfinance.databinding.HeaderTransactionsBinding
import com.example.yourfinance.databinding.TransactionItemBinding
import com.example.yourfinance.model.Transaction
import com.example.yourfinance.model.entities.Payment
import com.example.yourfinance.model.entities.Transfer
import com.example.yourfinance.model.TransactionListItem
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

class TransactionsRecyclerViewListAdapter : ListAdapter<TransactionListItem, RecyclerView.ViewHolder>(
    DIFF_CALLBACK
) {

    companion object {
        private const val VIEW_TYPE_HEADER = 0
        private const val VIEW_TYPE_TRANSACTION = 1
        private const val VIEW_TYPE_Empty = 2

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


    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is TransactionListItem.Header -> VIEW_TYPE_HEADER
            is TransactionListItem.TransactionItem -> VIEW_TYPE_TRANSACTION
            else -> VIEW_TYPE_Empty
        }
    }

    // ViewHolder для заголовка
    inner class HeaderViewHolder(private val binding: HeaderTransactionsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(header: TransactionListItem.Header) {
            binding.dayOfMounth.text = header.date.dayOfMonth.toString()
            binding.dayOfWeek.text = header.date.dayOfWeek.getDisplayName(java.time.format.TextStyle.FULL, Locale("ru"))
            binding.mounth.text = header.date.format(DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru")))
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.balance.text = formatter.format(header.balance)
        }
    }

    // ViewHolder для транзакции
    inner class TransactionViewHolder(private val binding: TransactionItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TransactionListItem.TransactionItem) {
            val transaction = item.transaction
            binding.title.text = when (transaction) {
                is Payment -> if (transaction.note.isNotEmpty()) transaction.note else transaction.category.title
                is Transfer -> if (transaction.note.isNotEmpty()) transaction.note else "Перевод"
                else -> ""
            }
            binding.account.text = when (transaction) {
                is Payment -> if (transaction.moneyAcc.title.isNotEmpty()) transaction.moneyAcc.title else "Неизвестно"
                is Transfer -> if (transaction.moneyAccFrom.title.isNotEmpty() && transaction.moneyAccTo.title.isNotEmpty())
                    "${transaction.moneyAccFrom.title} -> ${transaction.moneyAccTo.title}" else "Неизвестно"
                else -> "Неизвестно"
            }
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            binding.price.text = formatter.format(transaction.balance)
            val color = when (transaction) {
                is Payment -> if (transaction.type == Transaction.TransactionType.income) Color.GREEN else Color.RED
                else -> Color.BLACK
            }
            binding.price.setTextColor(color)
            binding.time.text = transaction.time.toString()
        }
    }

    inner class EmptyViewHolder(binding: EmptyPlaceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind() {}
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> {
                val binding = HeaderTransactionsBinding.inflate(inflater, parent, false)
                HeaderViewHolder(binding)
            }
            VIEW_TYPE_TRANSACTION -> {
                val binding = TransactionItemBinding.inflate(inflater, parent, false)
                TransactionViewHolder(binding)
            }
            else -> {
                val binding = EmptyPlaceBinding.inflate(inflater, parent, false)
                EmptyViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is TransactionListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is TransactionListItem.TransactionItem -> (holder as TransactionViewHolder).bind(item)
            is TransactionListItem.EmptyItem -> (holder as EmptyViewHolder).bind()
        }
    }
}
