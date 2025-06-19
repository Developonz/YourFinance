package com.example.yourfinance.data.source

import androidx.room.TypeConverter
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.PeriodLite
import com.example.yourfinance.domain.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalTime

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): Long = date.toEpochDay()

    @TypeConverter
    fun toLocalDate(date: Long): LocalDate = LocalDate.ofEpochDay(date)

    @TypeConverter
    fun fromCategoryType(type: CategoryType): Int = type.ordinal

    @TypeConverter
    fun toCategoryType(value: Int): CategoryType = CategoryType.entries[value]

    @TypeConverter
    fun fromPeriodLiteType(type: PeriodLite): Int = type.ordinal

    @TypeConverter
    fun toPeriodLiteType(value: Int): PeriodLite = PeriodLite.entries[value]

    @TypeConverter
    fun fromTransactionType(type: TransactionType): Int = type.ordinal

    @TypeConverter
    fun toTransactionType(value: Int): TransactionType = TransactionType.entries[value]

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toPlainString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

}