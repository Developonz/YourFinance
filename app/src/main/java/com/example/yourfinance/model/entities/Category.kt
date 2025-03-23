package com.example.yourfinance.model.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.yourfinance.utils.StringHelper.Companion.getUpperFirstChar

@Entity
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    private var _title: String = "",
    val categoryType: CategoryType
) {

    enum class CategoryType {
        income,
        expense;
    }

    @ColumnInfo(name = "title")
    var title = getUpperFirstChar(_title)
        set(value) {
            field = getUpperFirstChar(value)
        }
}