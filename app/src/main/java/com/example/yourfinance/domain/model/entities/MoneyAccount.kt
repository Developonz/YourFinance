package com.example.yourfinance.domain.model.entities

import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate


data class MoneyAccount(
    var balance: Double = 0.0,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now(),
    val id: Long
) {

    var title = ""
        set(value) {
            field = getUpperFirstChar(value)
        }

    constructor(
        title: String,
        balance: Double = 0.0,
        excluded: Boolean = false,
        default: Boolean = false,
        used: Boolean = true,
        dateCreation: LocalDate = LocalDate.now(),
        id: Long
    ) :
            this(balance, excluded, default, used, dateCreation, id) {
                this.title = getUpperFirstChar(title)
            }
}

