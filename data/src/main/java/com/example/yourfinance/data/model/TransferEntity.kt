package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.yourfinance.domain.model.TransactionType
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = MoneyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["moneyAccFromID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MoneyAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["moneyAccToID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TransferEntity (
    var type: TransactionType,
    var balance: Double,
    var moneyAccFromID: Long,
    var moneyAccToID: Long,
    var note: String = "",
    var date: LocalDate = LocalDate.now(),
    var is_done: Boolean,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
)