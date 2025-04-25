package com.example.yourfinance.presentation.ui.adapter.list_item

import com.example.yourfinance.domain.model.Transaction
import java.time.LocalDate

sealed class TransactionListItem {
    data class Header(val date: LocalDate, val balance: Double) : TransactionListItem()
    data class TransactionItem(val transaction: Transaction) : TransactionListItem()
    object EmptyItem : TransactionListItem()
}
