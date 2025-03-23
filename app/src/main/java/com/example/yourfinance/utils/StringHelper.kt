package com.example.yourfinance.utils

class StringHelper {
    companion object {
        fun getUpperFirstChar(str: String) : String {
            return str.trim().lowercase().replaceFirstChar { it.uppercase() }
        }
    }
}