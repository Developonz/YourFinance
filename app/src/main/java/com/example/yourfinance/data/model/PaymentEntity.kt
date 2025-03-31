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
            childColumns = ["moneyAccID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PaymentEntity(
    var type: TransactionType,
    var balance: Double,
    var moneyAccID: Long,
    var categoryID: Long,
    var note: String = "",
    var date: LocalDate = LocalDate.now(),
    var time: LocalTime = LocalTime.now(),
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
)