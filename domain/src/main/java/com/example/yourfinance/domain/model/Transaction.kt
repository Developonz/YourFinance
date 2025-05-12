package com.example.yourfinance.domain.model

import java.time.LocalDate
import java.time.LocalTime

abstract class Transaction(
    open val id: Long,
    open var type: TransactionType,
    open var balance: Double,
    open var date: LocalDate = LocalDate.now(),
    open var note: String = ""
)