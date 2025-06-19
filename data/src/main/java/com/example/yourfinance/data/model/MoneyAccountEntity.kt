package com.example.yourfinance.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal
import java.time.LocalDate

@Entity
data class MoneyAccountEntity(
    var title: String = "",
    var startBalance: BigDecimal = BigDecimal.ZERO,
    var balance: BigDecimal = startBalance,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now(),
    var iconResourceId: String? = null,
    var colorHex: Int? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)

