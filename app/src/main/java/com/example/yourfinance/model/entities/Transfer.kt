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
            childColumns = ["moneyAccFromID"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MoneyAccount::class,
            parentColumns = ["id"],
            childColumns = ["moneyAccToID"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Transfer (
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    override var type: TransactionType,
    override var balance: Double,

    private var _note: String = "",
    override var date: LocalDate = LocalDate.now(),
    override var time: LocalTime = LocalTime.now(),

    var moneyAccFromID: Long,
    var moneyAccToID: Long
) : Transaction(id, type, balance, _note, date, time) {

    @ColumnInfo(name = "note")
    override var note = getUpperFirstChar(_note)
        set(value) {
            field = getUpperFirstChar(value)
        }
}