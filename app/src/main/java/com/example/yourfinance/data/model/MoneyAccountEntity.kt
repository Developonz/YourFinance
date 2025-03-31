package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity
data class MoneyAccountEntity(
    var title: String = "",
    var balance: Double = 0.0,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now(),
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)

