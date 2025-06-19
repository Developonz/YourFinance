package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yourfinance.domain.model.PeriodLite
import java.math.BigDecimal

@Entity
data class BudgetEntity(
    var title: String,
    var budgetLimit: BigDecimal,
    var period: PeriodLite,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)