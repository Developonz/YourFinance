package com.example.yourfinance.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class MoneyAccountEntity(
    var title: String = "",
    var startBalance: Double = 0.0,
    var balance: Double = startBalance,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now(),
    var iconResourceId: Int? = null,
    var colorHex: String? = null,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)

