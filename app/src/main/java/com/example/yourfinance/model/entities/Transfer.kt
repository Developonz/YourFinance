package com.example.yourfinance.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.example.yourfinance.MainApplication
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
    override var type: TransactionType,
    override var balance: Double,
    var moneyAccFromID: Long,
    var moneyAccToID: Long,
    override var date: LocalDate = LocalDate.now(),
    override var time: LocalTime = LocalTime.now(),
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0
) : Transaction(id, type, balance, date, time) {

    @ColumnInfo(name = "note")
    override var note = ""
        set(value) {
            field = getUpperFirstChar(value)
        }

    val moneyAccFrom get() = fetchMoneyAccFrom()
    val moneyAccTo get() = fetchMoneyAccTo()


    constructor(
        note: String,
        type: TransactionType,
        balance: Double,
        moneyAccFromID: Long,
        moneyAccToID: Long,
        date: LocalDate = LocalDate.now(),
        time: LocalTime = LocalTime.now(),
    ) : this (type, balance, moneyAccFromID, moneyAccToID, date, time) {
        this.note = getUpperFirstChar(note)
    }

    private fun fetchMoneyAccFrom() : MoneyAccount? {
        return MainApplication.repository.getAccount(moneyAccFromID)
    }

    private fun fetchMoneyAccTo() : MoneyAccount? {
        return MainApplication.repository.getAccount(moneyAccToID)
    }
}