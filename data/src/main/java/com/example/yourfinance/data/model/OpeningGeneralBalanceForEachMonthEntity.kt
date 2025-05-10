package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
class OpeningGeneralBalanceForEachMonthEntity (
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val year: Int,
    val month: Int,
    val openingBalance: Double
)