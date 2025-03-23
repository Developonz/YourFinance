package com.example.yourfinance.model.entities
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar
import java.time.LocalDate

@Entity
data class MoneyAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    private var _title: String = "",
    var balance: Double = 0.0,
    var excluded: Boolean = false,
    var default: Boolean = false,
    var used: Boolean = true,
    val dateCreation: LocalDate = LocalDate.now()
) {

    @ColumnInfo(name = "title")
    var title = getUpperFirstChar(_title)
        set(value) {
            field = getUpperFirstChar(value)
        }
}

