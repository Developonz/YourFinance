package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.yourfinance.domain.model.TransactionType

@Entity(foreignKeys = [
    ForeignKey(
        parentColumns = ["id"],
        childColumns = ["id"],
        entity = PaymentEntity::class,
        onDelete = ForeignKey.CASCADE,
        deferred = true
    )
])
data class FuturePaymentEntity (
    @PrimaryKey
    val id: Long = 0
)