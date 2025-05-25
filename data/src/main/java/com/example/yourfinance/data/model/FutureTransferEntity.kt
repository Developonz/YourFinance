package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(foreignKeys = [
    ForeignKey(
        parentColumns = ["id"],
        childColumns = ["id"],
        entity = TransferEntity::class,
        onDelete = ForeignKey.CASCADE,
        deferred = true
    )
])
data class FutureTransferEntity (
    @PrimaryKey
    val id: Long = 0
)