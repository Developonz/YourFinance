package com.example.yourfinance.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.yourfinance.model.Transaction
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = MoneyAccount::class,
            parentColumns = ["id"],
            childColumns = ["moneyAccID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Payment(
    override var type: TransactionType,
    override var balance: Double,
    var moneyAccID: Long,
    var categoryID: Long,
    override var date: LocalDate = LocalDate.now(),
    override var time: LocalTime = LocalTime.now(),
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
) : Transaction(id, type, balance, date, time) {

    @ColumnInfo(name = "note")
    override var note = ""
        set(value) {
            field = getUpperFirstChar(value)
        }

    constructor(
        note: String,
        type: TransactionType,
        balance: Double,
        date: LocalDate = LocalDate.now(),
        time: LocalTime = LocalTime.now(),
        moneyAccID: Long,
        categoryID: Long
    ) : this (type, balance, moneyAccID, categoryID, date, time) {
        this.note = getUpperFirstChar(note)
    }
}