package com.example.yourfinance.utils

import java.text.NumberFormat
import java.util.Locale

class StringHelper {
    companion object {
        fun getUpperFirstChar(str: String) : String {
            return str.trim().lowercase().replaceFirstChar { it.uppercase() }
        }

        fun getMoneyStr(balance: Double) : String {
            val formatter = NumberFormat.getCurrencyInstance(Locale.getDefault())
            return formatter.format(balance)
        }
    }
}