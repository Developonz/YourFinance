package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.DelegateUpperCaseFirstChar
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.util.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate
import java.time.LocalTime


data class Transfer (
    override var type: TransactionType,
    override var balance: Double,
    var moneyAccFrom: MoneyAccount,
    var moneyAccTo: MoneyAccount,
    private var _note: Title,
    override var date: LocalDate = LocalDate.now(),
    override var time: LocalTime = LocalTime.now(),
    override val id: Long = 0
) : Transaction(id, type, balance, date, time, _note.value) {
    override var note: String
        get() = _note.value
        set(value) { _note = Title(value) }
}