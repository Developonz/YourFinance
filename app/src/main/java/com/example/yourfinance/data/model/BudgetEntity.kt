package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yourfinance.domain.model.PeriodLite

@Entity
data class BudgetEntity(
    var title: String,
    var balance: String,
    var period: PeriodLite,
    @PrimaryKey(autoGenerate = true)
    val id: Long
)