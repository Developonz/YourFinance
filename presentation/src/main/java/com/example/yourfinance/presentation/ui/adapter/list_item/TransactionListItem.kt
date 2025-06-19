package com.example.yourfinance.presentation.ui.adapter.list_item

import com.example.yourfinance.domain.model.Transaction
import java.math.BigDecimal
import java.time.LocalDate

sealed class TransactionListItem {
    data class Header(val date: LocalDate, val balance: BigDecimal) : TransactionListItem()
    data class TransactionItem(val transaction: Transaction) : TransactionListItem()
    object EmptyItem : TransactionListItem()
}
