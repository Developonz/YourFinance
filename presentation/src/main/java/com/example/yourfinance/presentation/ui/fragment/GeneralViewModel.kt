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


    // --- МЕТОДЫ ДЛЯ УСТАНОВКИ ПЕРИОДА ---

    // Основной метод для установки периода извне (из BottomSheet или инициализации)
    fun setPeriod(period: Period, customStartDate: LocalDate? = null, customEndDate: LocalDate? = null) {
        when (period) {
            Period.CUSTOM -> {
                // Обработка пользовательского периода
                handleCustomPeriodSelection(period, customStartDate, customEndDate)
            }
            Period.ALL -> {
                // Обработка выбора "Все время"
                _selectedPeriod.value = PeriodSelection(period, null, null)
            }
            else -> {
                // Для стандартных периодов (Daily, Weekly и т.д.)
                // устанавливаем период относительно ТЕКУЩЕЙ ДАТЫ
                setStandardPeriodBasedOnDate(period, LocalDate.now())
            }
        }
    }

    // Обработка выбора пользовательского периода
    private fun handleCustomPeriodSelection(period: Period, startDate: LocalDate?, endDate: LocalDate?) {
        if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
            // Если начальная дата после конечной, меняем их местами
            _selectedPeriod.value = PeriodSelection(period, endDate, startDate)
        } else {
            _selectedPeriod.value = PeriodSelection(period, startDate, endDate)
        }
    }

    // Установка стандартного периода относительно заданной даты (referenceDate)
    // Используется как для setPeriod, так и для навигации next/previousPeriod
    private fun setStandardPeriodBasedOnDate(period: Period, referenceDate: LocalDate) {
        if (period == Period.ALL || period == Period.CUSTOM) return // Не обрабатываем здесь

        var calculatedStartDate: LocalDate? = null
        var calculatedEndDate: LocalDate? = null

        when (period) {
            Period.DAILY -> {
                calculatedStartDate = referenceDate
                calculatedEndDate = referenceDate
            }
            Period.WEEKLY -> {
                val weekFields = WeekFields.of(Locale.getDefault()) // Или ваш предпочтительный Locale
                calculatedStartDate = referenceDate.with(weekFields.dayOfWeek(), 1) // Первый день недели
                calculatedEndDate = calculatedStartDate.plusDays(6) // Последний день недели
            }
            Period.MONTHLY -> {
                calculatedStartDate = referenceDate.withDayOfMonth(1)
                calculatedEndDate = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth())
            }
            Period.QUARTERLY -> {
                val currentMonth = referenceDate.monthValue
                val quarterStartMonth = when {
                    currentMonth <= 3 -> 1
                    currentMonth <= 6 -> 4
                    currentMonth <= 9 -> 7
                    else -> 10
                }
                calculatedStartDate = LocalDate.of(referenceDate.year, quarterStartMonth, 1)
                calculatedEndDate = calculatedStartDate.plusMonths(2).withDayOfMonth(calculatedStartDate.plusMonths(2).lengthOfMonth())
            }
            Period.ANNUALLY -> {
                calculatedStartDate = referenceDate.withDayOfYear(1)
                calculatedEndDate = referenceDate.withDayOfYear(referenceDate.lengthOfYear())
            }
            // ALL и CUSTOM уже исключены
            else -> {} // Добавлено для полноты when, хотя и не должно сюда попасть
        }
        _selectedPeriod.value = PeriodSelection(period, calculatedStartDate, calculatedEndDate)
    }


    // --- МЕТОДЫ ДЛЯ НАВИГАЦИИ ПО ПЕРИОДУ ---

    // Функция для перехода к следующему периоду
    fun nextPeriod() {
        val currentSelection = _selectedPeriod.value ?: return // Выходим, если нет текущего выбора
        // Навигация не работает для ALL и CUSTOM
        if (currentSelection.periodType == Period.ALL || currentSelection.periodType == Period.CUSTOM) return

        // Используем текущую начальную дату как точку отсчета
        val currentStartDate = currentSelection.startDate ?: LocalDate.now()
        // Рассчитываем новую опорную дату
        val newReferenceDate = calculateShiftedDate(currentStartDate, currentSelection.periodType, 1)
        // Пересчитываем стандартный период на основе новой опорной даты
        setStandardPeriodBasedOnDate(currentSelection.periodType, newReferenceDate)
    }

    // Функция для перехода к предыдущему периоду
    fun previousPeriod() {
        val currentSelection = _selectedPeriod.value ?: return // Выходим, если нет текущего выбора
        // Навигация не работает для ALL и CUSTOM
        if (currentSelection.periodType == Period.ALL || currentSelection.periodType == Period.CUSTOM) return

        // Используем текущую начальную дату как точку отсчета
        val currentStartDate = currentSelection.startDate ?: LocalDate.now()
        // Рассчитываем новую опорную дату
        val newReferenceDate = calculateShiftedDate(currentStartDate, currentSelection.periodType, -1)
        // Пересчитываем стандартный период на основе новой опорной даты
        setStandardPeriodBasedOnDate(currentSelection.periodType, newReferenceDate)
    }

    // Вспомогательная функция для расчета смещенной даты для навигации
    private fun calculateShiftedDate(referenceDate: LocalDate, period: Period, amount: Long): LocalDate {
        return when (period) {
            Period.DAILY      -> referenceDate.plusDays(amount)
            Period.WEEKLY     -> referenceDate.plusWeeks(amount)
            Period.MONTHLY    -> referenceDate.plusMonths(amount)
            Period.QUARTERLY  -> referenceDate.plusMonths(amount * 3)
            Period.ANNUALLY   -> referenceDate.plusYears(amount)
            else              -> referenceDate // Не должно произойти для ALL и CUSTOM
        }
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
