package com.example.yourfinance.utils

import androidx.room.TypeConverter
import com.example.yourfinance.model.entities.Category.CategoryType
import com.example.yourfinance.model.Transaction.TransactionType
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(date: Long): LocalDate = LocalDate.ofEpochDay(date)

    @TypeConverter
    fun fromLocalTime(time: LocalTime): Long = time.toSecondOfDay().toLong()

    @TypeConverter
    fun toLocalTime(value: Long): LocalTime = LocalTime.ofSecondOfDay(value)

    @TypeConverter
    fun fromCategoryType(type: CategoryType): Int = type.ordinal

    @TypeConverter
    fun toCategoryType(value: Int): CategoryType = CategoryType.entries[value]

    @TypeConverter
    fun fromTransactionType(type: TransactionType): Int = type.ordinal

    @TypeConverter
    fun toTransactionType(value: Int): TransactionType = TransactionType.entries[value]

}