package com.example.yourfinance.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.databinding.EmptyPlaceBinding
import com.example.yourfinance.databinding.HeaderItemBinding
import com.example.yourfinance.databinding.TransactionItemBinding
import com.example.yourfinance.model.Transaction
import com.example.yourfinance.model.Transactions
import com.example.yourfinance.model.entities.Payment
import com.example.yourfinance.model.pojo.FullPayment
import com.example.yourfinance.model.pojo.FullTransfer

class TransactionsListRecycleViewAdapter(var transactions: List<Transactions>) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    companion object {
        const val HEADER = 0
        const val TRANSACTION = 1
        const val EMPTY = 2

        class TransactionViewHolder(binding: TransactionItemBinding) : RecyclerView.ViewHolder(binding.root) {
            val title: TextView = binding.title
            val price: TextView = binding.price
            val account: TextView = binding.account
            val time: TextView = binding.time
        }

        class HeaderViewHolder(binding: HeaderItemBinding) : RecyclerView.ViewHolder(binding.root) {

        }

        class EmptyViewHolder(binding: EmptyPlaceBinding) : RecyclerView.ViewHolder(binding.root) {

        }
    }



    fun updateData(newTransactions: List<Transactions>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }



    override fun getItemViewType(position: Int): Int {
        if (position == transactions.size) return EMPTY
        return TRANSACTION
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflatter: LayoutInflater = LayoutInflater.from(parent.context)
        if (viewType == TRANSACTION) {
            return TransactionViewHolder(TransactionItemBinding.inflate(inflatter, parent, false))
        } else if (viewType == EMPTY) {
            return EmptyViewHolder(EmptyPlaceBinding.inflate(inflatter,parent,false))
        }
        else {
            return HeaderViewHolder(HeaderItemBinding.inflate(inflatter, parent,false))
        }
    }

    override fun getItemCount(): Int {
        return transactions.size + 1
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is TransactionViewHolder) {
            val transaction = transactions[position]
            holder.title.text = when (transaction) {
                is FullPayment -> {
                    if (transaction.payment.note.isNotEmpty()) transaction.payment.note
                    else transaction.category.title
                }
                is FullTransfer -> {
                    if (transaction.transfer.note.isNotEmpty()) transaction.transfer.note
                    else "Перевод"
                }
                else -> ""
            }
            holder.account.text = when (transaction) {
                is FullPayment -> {
                    if (transaction.moneyAcc.title.isNotEmpty()) transaction.moneyAcc.title
                    else "Неизвестно"
                }
                is FullTransfer -> {
                    if (transaction.moneyAccFrom.title.isNotEmpty() && transaction.moneyAccTo.title.isNotEmpty()) transaction.moneyAccFrom.title + " -> " + transaction.moneyAccTo.title
                    else "Неизвестно"
                }
                else -> "Неизвестно"
            }

            holder.price.text = when (transaction) {
                is FullPayment -> {
                    transaction.payment.balance.toString()
                }
                is FullTransfer -> {
                    transaction.transfer.balance.toString()
                }
                else -> "X руб."
            }

            val color = when (transaction) {
                is FullPayment -> {
                    if (transaction.payment.type == Transaction.TransactionType.income) Color.GREEN
                    else Color.RED
                }
                else ->Color.BLACK
            }
            holder.price.setTextColor(color)

            holder.time.text = when (transaction) {
                is FullPayment -> {
                    transaction.payment.time.toString()
                }
                is FullTransfer -> {
                    transaction.transfer.time.toString()
                }
                else -> "hh:mm"
            }
        }
    }



}