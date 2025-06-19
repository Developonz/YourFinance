package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.Title
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime


data class Transfer (
    override var balance: BigDecimal,
    var moneyAccFrom: MoneyAccount,
    var moneyAccTo: MoneyAccount,
    private var _note: Title,
    override var date: LocalDate = LocalDate.now(),
    override var is_done: Boolean = true,
    override val id: Long = 0
) : Transaction(id, TransactionType.REMITTANCE, balance, date, is_done, _note.value) {
    override var note: String
        get() = _note.value
        set(value) { _note = Title(value) }
    override var type: TransactionType = TransactionType.REMITTANCE
}