package com.example.yourfinance.domain.model

import java.util.Locale

enum class PeriodLite(val description: String) {
    WEEKLY("еженедельный"),
    MONTHLY("ежемесячный"),
    QUARTERLY("ежеквартальный"),
    ANNUALLY("ежегодный");

    val upperDescription: String
        get() = description.uppercase(Locale.ROOT)
}