package com.example.yourfinance.model.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate

@Entity
data class MoneyAccount(
    var balance: Double = 0.0,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now(),
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0
) {

    @ColumnInfo(name = "title")
    var title = ""
        set(value) {
            field = getUpperFirstChar(value)
        }

    constructor(
        title: String,
        balance: Double = 0.0,
        excluded: Boolean = false,
        default: Boolean = false,
        used: Boolean = true,
        dateCreation: LocalDate = LocalDate.now()) :
            this(balance, excluded, default, used, dateCreation) {
                this.title = getUpperFirstChar(title)
            }
}

