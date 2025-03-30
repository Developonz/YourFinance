package com.example.yourfinance.data.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.yourfinance.MainApplication
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar
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


    val categoryEntity : CategoryEntity? get() = fetchCategory()
    val moneyAccount get() = fetchAccount()

    constructor(
        note: String,
        type: TransactionType,
        balance: Double,
        moneyAccID: Long,
        categoryID: Long,
        date: LocalDate = LocalDate.now(),
        time: LocalTime = LocalTime.now(),
    ) : this (type, balance, moneyAccID, categoryID, date, time) {
        this.note = getUpperFirstChar(note)
    }

    private fun fetchCategory() : CategoryEntity? {
        return MainApplication.repository.getCategory(categoryID)
    }

    private fun fetchAccount() : MoneyAccountEntity? {
        return MainApplication.repository.getAccount(categoryID)
    }
}