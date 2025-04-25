package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.ICategoryData

/**
 * Представляет текущее состояние ввода для АКТИВНОГО типа транзакции.
 * Используется в ViewModel для управления специфичными для типа выбранными элементами.
 */
sealed interface ActiveTransactionState {

    data class ExpenseIncomeState(
        val selectedCategory: ICategoryData? = null,
        val selectedPaymentAccount: MoneyAccount? = null
    ) : ActiveTransactionState

    data class RemittanceState(
        val selectedAccountFrom: MoneyAccount? = null,
        val selectedAccountTo: MoneyAccount? = null
    ) : ActiveTransactionState

    object InitialState : ActiveTransactionState
}