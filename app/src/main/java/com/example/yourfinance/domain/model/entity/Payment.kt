package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate
import java.time.LocalTime


data class Payment(
    override var type: TransactionType,
    override var balance: Double,
    var moneyAccount: MoneyAccount,
    var category : Category,
    private var _note: String = "",
    override var date: LocalDate = LocalDate.now(),
    override var time: LocalTime = LocalTime.now(),
    override val id: Long = 0
) : Transaction(id, type, balance, date, time, _note) {

    override var note
        get() = _note
        set(value) {
            _note = getUpperFirstChar(value)
        }
    init {
        note = note
    }
}