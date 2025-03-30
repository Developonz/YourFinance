package com.example.yourfinance.domain.model.entities

import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate
import java.time.LocalTime


data class Payment(
    override var type: TransactionType,
    override var balance: Double,
    var moneyAccount: MoneyAccount,
    var category : Category,
    override var date: LocalDate = LocalDate.now(),
    override var time: LocalTime = LocalTime.now(),
    override val id: Long,
) : Transaction(id, type, balance, date, time) {


    override var note = ""
        set(value) {
            field = getUpperFirstChar(value)
        }



    constructor(
        note: String,
        type: TransactionType,
        balance: Double,
        moneyAcc: MoneyAccount,
        category: Category,
        date: LocalDate = LocalDate.now(),
        time: LocalTime = LocalTime.now(),
        id: Long
    ) : this (type, balance, moneyAcc, category, date, time, id) {
        this.note = getUpperFirstChar(note)
    }


}