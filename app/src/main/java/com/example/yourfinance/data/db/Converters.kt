package com.example.yourfinance.data.db

import androidx.room.TypeConverter
import com.example.yourfinance.data.entities.CategoryEntity.CategoryType
import com.example.yourfinance.domain.model.Transaction.TransactionType
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