package com.example.yourfinance.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class StringHelper {
    companion object {
        fun getUpperFirstChar(str: String): String {
            return str.trim().lowercase().replaceFirstChar { it.uppercase() }
        }

        fun getMoneyStr(balance: Double): String {
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            return formatter.format(balance)
        }

        fun getTime(time: LocalTime): String {
            val formatter = DateTimeFormatter.ofPattern("HH:mm")
            return time.format(formatter)
        }

        // Форматирование дня месяца (например, "15")
        fun getDayOfMonthStr(date: LocalDate): String {
            return date.dayOfMonth.toString()
        }

        // Форматирование дня недели (например, "понедельник")
        fun getDayOfWeekStr(date: LocalDate): String {
            return date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("ru"))
        }

        // Форматирование месяца и года (например, "январь 2025")
        fun getMonthYearStr(date: LocalDate): String {
            val formatter = DateTimeFormatter.ofPattern("LLLL yyyy", Locale("ru"))
            return date.format(formatter)
        }
    }
}
