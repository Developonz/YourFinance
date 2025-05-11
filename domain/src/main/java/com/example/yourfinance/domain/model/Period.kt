package com.example.yourfinance.domain.model

import java.util.Locale

enum class Period(val description: String) {
    DAILY("ежедневно"),
    WEEKLY("еженедельно"),
    MONTHLY("ежемесячно"),
    QUARTERLY("ежеквартально"),
    ANNUALLY("ежегодно"),
    ALL("все"),
    CUSTOM("пользовательский");

    val upperDescription: String
        get() = description.uppercase(Locale.ROOT)
}