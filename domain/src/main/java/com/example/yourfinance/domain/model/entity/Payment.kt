package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime


data class Payment(
    override var type: TransactionType,
    override var balance: BigDecimal,
    var moneyAccount: MoneyAccount,
    var category : ICategoryData,
    private var _note: Title,
    override var date: LocalDate = LocalDate.now(),
    override var is_done: Boolean = true,
    override val id: Long = 0
) : Transaction(id, type, balance, date, is_done, _note.value) {
    override var note: String
        get() = _note.value
        set(value) { _note = Title(value) }
}