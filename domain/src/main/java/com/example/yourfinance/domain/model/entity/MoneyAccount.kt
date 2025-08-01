package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate
import com.example.yourfinance.domain.model.Title
import java.math.BigDecimal

//TODO: switch Double to BigDecimal
data class MoneyAccount(
    private var _title: Title,
    var startBalance: BigDecimal,
    var balance: BigDecimal = startBalance,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now(),
    var iconResourceId: String? = null,
    var colorHex: Int? = null,
    val id: Long = 0
) {
    var title: String
        get() = _title.value
        set(value) { _title = Title(value) }
}
