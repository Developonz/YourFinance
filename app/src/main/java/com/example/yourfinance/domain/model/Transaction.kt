package com.example.yourfinance.domain.model

import java.time.LocalDate
import java.time.LocalTime

abstract class Transaction(
    open val id: Long,
    open var type: TransactionType,
    open var balance: Double,
    open var date: LocalDate = LocalDate.now(),
    open var time: LocalTime = LocalTime.now(),
    open var note: String = ""
) {

    enum class TransactionType {
        income,
        expense,
        remittance;
    }
}