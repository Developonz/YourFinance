package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate
import com.example.yourfinance.domain.model.Title

//TODO: switch Double to BigDecimal
data class MoneyAccount(
    private var _title: Title,
    var startBalance: Double,
    var balance: Double = startBalance,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now(),
    val id: Long = 0
) {
    var title: String
        get() = _title.value
        set(value) { _title = Title(value) }
}
