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
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    override var type: TransactionType,
    override var balance: Double,
    private var _note: String = "",
    override var date: LocalDate = LocalDate.now(),
    override var time: LocalTime = LocalTime.now(),

    var moneyAccID: Long,
    var categoryID: Long
) : Transaction(id, type, balance, _note, date, time) {

    @ColumnInfo(name = "note")
    override var note = getUpperFirstChar(_note)
        set(value) {
            field = getUpperFirstChar(value)
        }
}