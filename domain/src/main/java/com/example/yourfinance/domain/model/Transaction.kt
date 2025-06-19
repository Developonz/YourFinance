package com.example.yourfinance.domain.model

import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

abstract class Transaction(
    open val id: Long,
    open var type: TransactionType,
    open var balance: BigDecimal,
    open var date: LocalDate = LocalDate.now(),
    open var is_done: Boolean,
    open var note: String = ""
)