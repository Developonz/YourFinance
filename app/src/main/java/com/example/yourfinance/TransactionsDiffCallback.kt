package com.example.yourfinance

import androidx.recyclerview.widget.DiffUtil
import com.example.yourfinance.model.Transactions

class TransactionsDiffCallback(
    private val oldList: List<Transactions>,
    private val newList: List<Transactions>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    // Сравниваем элементы по их уникальному идентификатору
    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val newTr = newList[newItemPosition].getTransaction()
        val oldTr = oldList[oldItemPosition].getTransaction()
        return newTr.id == oldTr.id && newTr::class == oldTr::class
    }

    // Проверяем, совпадают ли данные полностью
    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
