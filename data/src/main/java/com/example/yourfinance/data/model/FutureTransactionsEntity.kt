package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(
        parentColumns = ["id"],
        childColumns = ["id"],
        entity = PaymentEntity::class,
        onDelete = ForeignKey.CASCADE
    )
])
class FutureTransactionsEntity (
    @PrimaryKey
    val id: Long = 0
)