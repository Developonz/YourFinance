package com.example.yourfinance.presentation.ui.util

import android.util.Log

class AmountInputProcessor(initialAmount: String = "0") {

    private var currentAmountString: String
    private var hasOperator: Boolean = false

    init {
        this.currentAmountString = "0"
        this.hasOperator = false
        if (initialAmount != "0") {
            this.currentAmountString = initialAmount
            this.hasOperator = initialAmount.contains('+') || initialAmount.contains('-')
        }
    }

    fun getAmountString(): String = currentAmountString
    fun hasOperator(): Boolean = hasOperator

    fun processKey(key: String) {
        var newAmount = currentAmountString
        when (key) {
            "DEL" -> {
                newAmount = if (currentAmountString.isNotEmpty()) {
                    currentAmountString.dropLast(1).ifEmpty { "0" }
                } else {
                    "0"
                }
            }
            "." -> {
                val parts = currentAmountString.split('+', '-').filter { it.isNotEmpty() }
                val lastPart = parts.lastOrNull() ?: ""

                if (!lastPart.contains('.') && (lastPart.isNotEmpty() || currentAmountString == "0")) {
                    newAmount = if (currentAmountString.isEmpty() || (currentAmountString == "0" && !currentAmountString.contains("."))) "0." else "$currentAmountString."
                } else {
                    return
                }
            }
            "+", "-" -> {
                if (currentAmountString.isEmpty() || currentAmountString == "0" || currentAmountString.last().let { it == '+' || it == '-' || it == '.' }) {
                    return
                }
                newAmount = currentAmountString + key
            }
            else -> { // Цифры
                val parts = currentAmountString.split('+', '-').filter { it.isNotEmpty() }
                val lastPart = parts.lastOrNull() ?: ""

                if (lastPart.contains('.') && lastPart.substringAfter('.').length >= 2) {
                    return
                }
                if (currentAmountString == "0" && key == "0") {
                    return
                }
                if (currentAmountString == "0" && key != ".") {
                    newAmount = key
                } else if (currentAmountString.length < 15) {
                    newAmount = currentAmountString + key
                } else {
                    return
                }
            }
        }
        currentAmountString = newAmount
        hasOperator = currentAmountString.contains('+') || currentAmountString.contains('-')
    }

    // Сделаем internal, чтобы ViewModel мог его использовать для валидации
    internal fun evaluateExpressionOnly(expression: String): Double? {
        if (expression.isEmpty()) return null

        val cleanExpression = expression.trimEnd('+', '-', '.')
        if (cleanExpression.isEmpty() || cleanExpression == "0.") return 0.0

        try {
            val tokens = mutableListOf<String>()
            var currentToken = ""
            for (char in cleanExpression) {
                if (char == '+' || char == '-') {
                    if (currentToken.isNotEmpty()) {
                        tokens.add(currentToken.replace(',', '.'))
                        currentToken = ""
                    }
                    tokens.add(char.toString())
                } else {
                    currentToken += char
                }
            }
            if (currentToken.isNotEmpty()) {
                tokens.add(currentToken.replace(',', '.'))
            }

            if (tokens.isEmpty()) return null

            // Если первый токен не число (например, начинается с оператора без числа перед ним), это ошибка
            // кроме случая, когда это единственный токен и он число
            if (tokens.size > 1 && tokens.first().toDoubleOrNull() == null && !tokens.first().matches(Regex("-?\\d+(\\.\\d+)?"))) {
                // Проверяем, если первый токен просто "-", а следующий число, это валидно для начала
                if (!(tokens.first() == "-" && tokens.size > 1 && tokens[1].toDoubleOrNull() != null)) {
                    Log.w("AmountInputProcessor", "Expression starts with an invalid token or operator without preceding number: ${tokens.first()}")
                    // return null // Убрал, т.к. логика ниже должна это обработать или дать toDoubleOrNull() == null
                }
            }


            var result = tokens.first().replace(',', '.').toDoubleOrNull() // Сразу пытаемся преобразовать
            if (result == null && tokens.first() == "-" && tokens.size > 1) { // Случай "-5"
                val combinedFirst = tokens.first() + tokens[1].replace(',', '.')
                result = combinedFirst.toDoubleOrNull()
                if (result != null) {
                    // Удаляем первые два элемента, так как они объединены
                    if (tokens.size >= 2) {
                        tokens.removeAt(0)
                        tokens.removeAt(0)
                    } else { // Маловероятно, но для безопасности
                        tokens.clear()
                    }
                    // Вставляем объединенный результат в начало (если еще есть операторы)
                    if (tokens.isNotEmpty()) {
                        tokens.add(0, result.toString()) // результат уже Double
                    } else {
                        // Если больше нет токенов, это и есть результат
                        return result
                    }
                } else {
                    return null // Не удалось сформировать число даже с начальным минусом
                }
            } else if (result == null) {
                return null // Первый токен не является числом
            }


            var i = 1 // Индекс оператора
            // Если после обработки начального минуса result уже был конечным, tokens может быть пуст
            while (i < tokens.size - 1) {
                val operator = tokens[i]
                val nextNumberString = tokens[i+1].replace(',', '.')
                val nextNumber = nextNumberString.toDoubleOrNull() ?: return null

                when (operator) {
                    "+" -> result = result!! + nextNumber // result уже не null
                    "-" -> result = result!! - nextNumber
                    else -> return null
                }
                i += 2
            }
            return result
        } catch (e: Exception) {
            Log.e("AmountInputProcessor", "Error evaluating expression: $expression", e)
            return null
        }
    }


    fun evaluateAndSetResult(): Boolean {
        if (!hasOperator && !currentAmountString.endsWith(".")) {
            val number = currentAmountString.replace(',', '.').toDoubleOrNull()
            return number != null
        }
        // Если заканчивается на оператор или точку, это невалидное выражение для '='
        if (currentAmountString.endsWith("+") || currentAmountString.endsWith("-") || currentAmountString.endsWith(".")) {
            return false
        }

        val evaluatedAmount = evaluateExpressionOnly(currentAmountString)
        return if (evaluatedAmount != null) {
            currentAmountString = formatDoubleToString(evaluatedAmount)
            hasOperator = false
            true
        } else {
            false
        }
    }

    private fun formatDoubleToString(value: Double): String {
        val roundedValue = (value * 1000).toLong() / 1000.0 // Округляем до 3х для большей точности перед конвертацией
        val stringValue = String.format(java.util.Locale.US, "%.2f", roundedValue) // Форматируем до 2 знаков
        val result = stringValue.replace('.', ',')
        // Убираем ",00" если число целое
        return if (result.endsWith(",00")) result.dropLast(3) else result
    }

    fun reset(newInitialAmount: String = "0") {
        currentAmountString = "0"
        hasOperator = false
        if (newInitialAmount != "0") {
            this.currentAmountString = newInitialAmount
            this.hasOperator = newInitialAmount.contains('+') || newInitialAmount.contains('-')
        }
    }
}