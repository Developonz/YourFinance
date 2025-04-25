package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate

//TODO: switch Double to BigDecimal
data class MoneyAccount(
    private var _title: String,
    var startBalance: Double,
    var balance: Double = startBalance,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now(),
    val id: Long = 0
) {

    var title: String
        get() = _title
        set(value) {
            _title = getUpperFirstChar(value)
        }
    init {
        title = title
    }
}

