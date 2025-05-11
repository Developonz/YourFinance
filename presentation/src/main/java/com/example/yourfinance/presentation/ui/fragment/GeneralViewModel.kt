package com.example.yourfinance.presentation.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.domain.model.Period
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.usecase.budget.FetchBudgetsUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.FetchMoneyAccountsUseCase
import com.example.yourfinance.domain.usecase.transaction.FetchTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import java.time.temporal.WeekFields
import java.util.Locale
import androidx.lifecycle.switchMap

@HiltViewModel
class GeneralViewModel @Inject constructor(
    fetchTransactionsUseCase: FetchTransactionsUseCase,
    fetchMoneyAccountsUseCase: FetchMoneyAccountsUseCase,
    fetchBudgetsUseCase: FetchBudgetsUseCase,

) : ViewModel() {

    private val _selectedPeriod = MutableLiveData<PeriodSelection>()
    val selectedPeriod: LiveData<PeriodSelection> get() = _selectedPeriod

    val transactionsList: LiveData<List<Transaction>> = _selectedPeriod.switchMap { selection ->
        fetchTransactionsUseCase(selection.startDate, selection.endDate)
    }
    val accountsList: LiveData<List<MoneyAccount>> = fetchMoneyAccountsUseCase()
    val budgetsList: LiveData<List<Budget>> = fetchBudgetsUseCase()


    // Метод для установки нового периода из UI
    fun setPeriod(period: Period, customStartDate: LocalDate? = null, customEndDate: LocalDate? = null) {
        val today = LocalDate.now()
        var startDate: LocalDate? = null
        var endDate: LocalDate? = null

        when (period) {
            Period.DAILY -> {
                startDate = today
                endDate = today
            }
            Period.WEEKLY -> {
                val weekFields = WeekFields.of(Locale.getDefault()) // или Locale.MONDAY_FIRST если нужно
                startDate = today.with(weekFields.dayOfWeek(), 1) // Первый день недели
                endDate = startDate.plusDays(6) // Последний день недели
            }
            Period.MONTHLY -> {
                startDate = today.withDayOfMonth(1)
                endDate = today.withDayOfMonth(today.lengthOfMonth())
            }
            Period.QUARTERLY -> {
                val currentMonth = today.monthValue
                val quarterStartMonth = when {
                    currentMonth <= 3 -> 1
                    currentMonth <= 6 -> 4
                    currentMonth <= 9 -> 7
                    else -> 10
                }
                startDate = LocalDate.of(today.year, quarterStartMonth, 1)
                endDate = startDate.plusMonths(2).withDayOfMonth(startDate.plusMonths(2).lengthOfMonth())
            }
            Period.ANNUALLY -> {
                startDate = today.withDayOfYear(1)
                endDate = today.withDayOfYear(today.lengthOfYear())
            }
            Period.ALL -> {
                // startDate и endDate остаются null
            }
            Period.CUSTOM -> {
                // Если Period.CUSTOM, используем переданные customStartDate и customEndDate
                // Валидация дат (например, startDate не позже endDate) должна быть сделана перед вызовом этого метода
                // или здесь
                if (customStartDate != null && customEndDate != null && customStartDate.isAfter(customEndDate)) {
                    // Обработка некорректного интервала, например, поменять их местами или не обновлять
                    _selectedPeriod.value = PeriodSelection(period, customEndDate, customStartDate)
                    return
                }
                _selectedPeriod.value = PeriodSelection(period, customStartDate, customEndDate)
                return // Важно, чтобы не перезаписать значения ниже стандартными расчетами
            }
        }
        _selectedPeriod.value = PeriodSelection(period, startDate, endDate)
    }


    // Инициализация: по умолчанию, например, текущий месяц или "Все".
    // На скриншоте у вас "еженедельно" (09-15 марта), так что можно установить его
    init {
        // Установим начальный период, например, текущая неделя.
        setPeriod(Period.WEEKLY) // Вы можете изменить это на Period.MONTHLY или Period.ALL
    }
}

data class PeriodSelection(
    val periodType: Period,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
