package com.example.yourfinance.domain.model

@JvmInline
value class Title private constructor(val value: String) {
    companion object {
        operator fun invoke(raw: String): Title =
            Title(raw.trim().replaceFirstChar { it.uppercaseChar() })
    }

    override fun toString(): String {
        return value
    }
}
