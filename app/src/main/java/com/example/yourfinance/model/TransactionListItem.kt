package com.example.yourfinance.model

import java.time.LocalDate

sealed class TransactionListItem {
    data class Header(val date: LocalDate, val balance: Double) : TransactionListItem()
    data class TransactionItem(val transaction: Transaction) : TransactionListItem()
    object EmptyItem : TransactionListItem()
}
