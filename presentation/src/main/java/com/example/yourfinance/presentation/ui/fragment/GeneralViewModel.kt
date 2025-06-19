package com.example.yourfinance.presentation.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.liveData
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
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.usecase.CalculatePeriodBalancesUseCase
import com.example.yourfinance.domain.usecase.PeriodBalances
import com.example.yourfinance.domain.usecase.PieChartSliceData
import com.example.yourfinance.domain.usecase.PreparePieChartDataUseCase
import com.example.yourfinance.domain.usecase.budget.CalculateBudgetDetailsUseCase
import com.example.yourfinance.domain.usecase.categories.category.FetchCategoriesUseCase
import com.example.yourfinance.domain.usecase.transaction.DeleteTransactionUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.math.BigDecimal


@HiltViewModel
class GeneralViewModel @Inject constructor(
    private val fetchTransactionsUseCase: FetchTransactionsUseCase,
    fetchMoneyAccountsUseCase: FetchMoneyAccountsUseCase,
    fetchBudgetsUseCase: FetchBudgetsUseCase,
    private val calculateBudgetDetailsUseCase: CalculateBudgetDetailsUseCase,
    private val deleteTransactionUseCase: DeleteTransactionUseCase,
    private val calculatePeriodBalancesUseCase: CalculatePeriodBalancesUseCase,
    private val preparePieChartDataUseCase: PreparePieChartDataUseCase
) : ViewModel() {


    private val _selectedPeriod = MutableLiveData<PeriodSelection>()
    val selectedPeriod: LiveData<PeriodSelection> get() = _selectedPeriod


    val transactionsList: LiveData<List<Transaction>> = _selectedPeriod.switchMap { selection ->
        fetchTransactionsUseCase(selection.startDate, selection.endDate)
    }

    val accountsList: LiveData<List<MoneyAccount>> = fetchMoneyAccountsUseCase()
    val rawBudgetsList: LiveData<List<Budget>> = fetchBudgetsUseCase()



    val budgetsWithDetails = MediatorLiveData<List<Budget>>()

    // Это поле оставляем как есть, если оно не конфликтовало
    private val _currentChartDisplayTypeLiveData = MutableLiveData<TransactionType>(TransactionType.EXPENSE)
    val currentChartDisplayTypeLiveData: LiveData<TransactionType> get() = _currentChartDisplayTypeLiveData

    private val _pieChartData = MediatorLiveData<List<PieChartSliceData>>()
    val pieChartData: LiveData<List<PieChartSliceData>> get() = _pieChartData

    private val _periodBalances = MediatorLiveData<PeriodBalances>()
    val periodBalances: LiveData<PeriodBalances> get() = _periodBalances

    private var statisticsUpdateJob: Job? = null

    init {
        val initialPeriod = Period.MONTHLY
        val (start, end) = calculateDatesForStandardPeriod(initialPeriod, LocalDate.now())
        _selectedPeriod.value = PeriodSelection(initialPeriod, start, end) // Используем _selectedPeriod

        _pieChartData.addSource(transactionsList) { transactions ->
            updatePieChartDataInternal(transactions, _currentChartDisplayTypeLiveData.value)
        }
        _pieChartData.addSource(_currentChartDisplayTypeLiveData) { type ->
            updatePieChartDataInternal(transactionsList.value, type)
        }

        // periodBalances теперь зависит от _selectedPeriod
        _periodBalances.addSource(_selectedPeriod) { selection ->
            updatePeriodBalancesInternal(selection, accountsList.value)
        }
        _periodBalances.addSource(accountsList) { accounts ->
            updatePeriodBalancesInternal(_selectedPeriod.value, accounts)
        }


        fun updateBudgets() {
            val budgets = rawBudgetsList.value
            // Мы используем transactionsList как триггер, так как он зависит от периода
            if (budgets == null) return

            viewModelScope.launch {
                // Вызываем правильный UseCase без лишних параметров
                val detailedBudgets = budgets.map { budget ->
                    calculateBudgetDetailsUseCase.invoke(budget)
                }
                budgetsWithDetails.value = detailedBudgets
            }
        }

        budgetsWithDetails.addSource(rawBudgetsList) { updateBudgets() }
        budgetsWithDetails.addSource(transactionsList) { updateBudgets() }
    }

    private fun updatePieChartDataInternal(transactions: List<Transaction>?, type: TransactionType?) {
        if (transactions == null || type == null) {
            return
        }
        viewModelScope.launch {
            _pieChartData.value = preparePieChartDataUseCase.execute(transactions, type, 5)
        }
    }

    private fun updatePeriodBalancesInternal(selection: PeriodSelection?, accounts: List<MoneyAccount>?) {
        if (selection == null || accounts == null) {
            _periodBalances.value = PeriodBalances(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)
            return
        }
        statisticsUpdateJob?.cancel()
        statisticsUpdateJob = viewModelScope.launch {
            val balances = calculatePeriodBalancesUseCase.execute(
                selection.startDate,
                selection.endDate,
                accounts
            )
            _periodBalances.value = balances
        }
    }

    fun deleteTransaction(transactionToDelete: Transaction) {
        viewModelScope.launch {
            deleteTransactionUseCase(transactionToDelete)
        }
    }

    fun setPeriod(period: Period, customStartDate: LocalDate? = null, customEndDate: LocalDate? = null) {
        val newSelection = when (period) {
            Period.CUSTOM -> {
                if (customStartDate != null && customEndDate != null && customStartDate.isAfter(customEndDate)) {
                    PeriodSelection(period, customEndDate, customStartDate)
                } else {
                    PeriodSelection(period, customStartDate, customEndDate)
                }
            }
            Period.ALL -> PeriodSelection(period, null, null)
            else -> {
                val (startDate, endDate) = calculateDatesForStandardPeriod(period, LocalDate.now())
                PeriodSelection(period, startDate, endDate)
            }
        }
        if (_selectedPeriod.value != newSelection) { // Используем _selectedPeriod
            _selectedPeriod.value = newSelection // Используем _selectedPeriod
        }
    }

    private fun calculateDatesForStandardPeriod(period: Period, referenceDate: LocalDate): Pair<LocalDate?, LocalDate?> {
        var calculatedStartDate: LocalDate? = null
        var calculatedEndDate: LocalDate? = null
        when (period) {
            Period.DAILY -> {
                calculatedStartDate = referenceDate
                calculatedEndDate = referenceDate
            }
            Period.WEEKLY -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                calculatedStartDate = referenceDate.with(weekFields.dayOfWeek(), 1)
                calculatedEndDate = calculatedStartDate.plusDays(6)
            }
            Period.MONTHLY -> {
                calculatedStartDate = referenceDate.withDayOfMonth(1)
                calculatedEndDate = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth())
            }
            Period.QUARTERLY -> {
                val currentMonth = referenceDate.monthValue
                val quarterStartMonth = when {
                    currentMonth <= 3 -> 1; currentMonth <= 6 -> 4
                    currentMonth <= 9 -> 7; else -> 10
                }
                calculatedStartDate = LocalDate.of(referenceDate.year, quarterStartMonth, 1)
                calculatedEndDate = calculatedStartDate.plusMonths(2).withDayOfMonth(calculatedStartDate.plusMonths(2).lengthOfMonth())
            }
            Period.ANNUALLY -> {
                calculatedStartDate = referenceDate.withDayOfYear(1)
                calculatedEndDate = referenceDate.withDayOfYear(referenceDate.lengthOfYear())
            }
            else -> { }
        }
        return Pair(calculatedStartDate, calculatedEndDate)
    }

    fun nextPeriod() {
        val currentSelection = _selectedPeriod.value ?: return // Используем _selectedPeriod
        if (currentSelection.periodType == Period.ALL || currentSelection.periodType == Period.CUSTOM) return

        val currentStartDate = currentSelection.startDate ?: LocalDate.now()
        val newReferenceDate = calculateShiftedDate(currentStartDate, currentSelection.periodType, 1)
        val (startDate, endDate) = calculateDatesForStandardPeriod(currentSelection.periodType, newReferenceDate)
        _selectedPeriod.value = PeriodSelection(currentSelection.periodType, startDate, endDate) // Используем _selectedPeriod
    }

    fun previousPeriod() {
        val currentSelection = _selectedPeriod.value ?: return // Используем _selectedPeriod
        if (currentSelection.periodType == Period.ALL || currentSelection.periodType == Period.CUSTOM) return

        val currentStartDate = currentSelection.startDate ?: LocalDate.now()
        val newReferenceDate = calculateShiftedDate(currentStartDate, currentSelection.periodType, -1)
        val (startDate, endDate) = calculateDatesForStandardPeriod(currentSelection.periodType, newReferenceDate)
        _selectedPeriod.value = PeriodSelection(currentSelection.periodType, startDate, endDate) // Используем _selectedPeriod
    }

    private fun calculateShiftedDate(referenceDate: LocalDate, period: Period, amount: Long): LocalDate {
        return when (period) {
            Period.DAILY      -> referenceDate.plusDays(amount)
            Period.WEEKLY     -> referenceDate.plusWeeks(amount)
            Period.MONTHLY    -> referenceDate.plusMonths(amount)
            Period.QUARTERLY  -> referenceDate.plusMonths(amount * 3)
            Period.ANNUALLY   -> referenceDate.plusYears(amount)
            else              -> referenceDate
        }
    }

    fun setChartDisplayType(type: TransactionType) {
        if (_currentChartDisplayTypeLiveData.value != type) {
            _currentChartDisplayTypeLiveData.value = type
        }
    }
}

data class PeriodSelection(
    val periodType: Period,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
