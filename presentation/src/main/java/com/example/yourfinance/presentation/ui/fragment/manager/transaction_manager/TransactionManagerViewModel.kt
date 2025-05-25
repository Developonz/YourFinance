// TransactionManagerViewModel.kt
package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.util.Log
import androidx.lifecycle.*
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.domain.usecase.categories.category.FetchCategoriesUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.FetchMoneyAccountsUseCase
import com.example.yourfinance.domain.usecase.transaction.*
import com.example.yourfinance.presentation.ui.util.AmountInputProcessor
import com.example.yourfinance.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ShowAccountSelectionSheetRequest(
    val accounts: List<MoneyAccount>,
    val selectedId: Long?,
    val isForExpenseIncome: Boolean,
    val isForAccountFrom: Boolean? = null
)

@HiltViewModel
class TransactionManagerViewModel @Inject constructor(
    fetchMoneyAccountsUseCase: FetchMoneyAccountsUseCase,
    fetchFullCategoriesUseCase: FetchCategoriesUseCase,
    private val createPaymentUseCase: CreatePaymentUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val loadPaymentByIdUseCase: LoadPaymentByIdUseCase,
    private val loadTransferByIdUseCase: LoadTransferByIdUseCase,
    private val updatePaymentUseCase: UpdatePaymentUseCase,
    private val updateTransferUseCase: UpdateTransferUseCase,
    private val deleteTransactionUseCase: DeleteTransactionByIdAndTypeUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L
    private val initialTransactionType: TransactionType? = savedStateHandle.get<Int>("transactionTypeInt").let { typeInt ->
        if (typeInt != null && typeInt != -1) {
            try {
                TransactionType.entries.getOrNull(typeInt)
            } catch (e: Exception) {
                Log.e("ViewModel", "Invalid TransactionType ordinal from arguments: $typeInt", e)
                null
            }
        } else {
            null
        }
    }
    val isEditing: Boolean = transactionId != -1L && initialTransactionType != null

    private val _loadedTransactionType = MutableLiveData<TransactionType?>(null)
    val loadedTransactionType: LiveData<TransactionType?> = _loadedTransactionType

    val accountsList: LiveData<List<MoneyAccount>> = fetchMoneyAccountsUseCase()
    val allCategories: LiveData<List<com.example.yourfinance.domain.model.entity.category.Category>> = fetchFullCategoriesUseCase()

    private val _currentTransactionType = MutableLiveData(initialTransactionType ?: TransactionType.EXPENSE)
    val currentTransactionType: LiveData<TransactionType> = _currentTransactionType

    private val _sharedPaymentAccount = MutableLiveData<MoneyAccount?>(null)

    private val _activeTransactionState = MutableLiveData<ActiveTransactionState>(ActiveTransactionState.InitialState)
    val activeTransactionState: LiveData<ActiveTransactionState> = _activeTransactionState

    private val amountProcessor = AmountInputProcessor()

    private val _amountString = MutableLiveData(amountProcessor.getAmountString())
    val amountString: LiveData<String> = _amountString

    private val _hasOperatorInAmount = MutableLiveData(amountProcessor.hasOperator())
    val hasOperatorInAmount: LiveData<Boolean> = _hasOperatorInAmount

    private val _note = MutableLiveData("")
    val note: LiveData<String> = _note

    private val _date = MutableLiveData(LocalDate.now())
    val date: LiveData<LocalDate> = _date

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var initialAccountSetupDone = false
    private var accountsObserver: Observer<List<MoneyAccount>>? = null

    val showInputSection: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        value = false
        fun update() {
            if (isEditing) {
                val editingReady = _isLoading.value == false && _activeTransactionState.value !is ActiveTransactionState.InitialState
                if (value != editingReady) value = editingReady
                return
            }
            val requiredElementsSelected = when (val currentState = _activeTransactionState.value) {
                is ActiveTransactionState.ExpenseIncomeState -> currentState.selectedCategory != null
                is ActiveTransactionState.RemittanceState -> currentState.selectedAccountFrom != null && currentState.selectedAccountTo != null
                ActiveTransactionState.InitialState, null -> false
            }
            if (value != requiredElementsSelected) {
                value = requiredElementsSelected
                Log.d("ViewModel", "showInputSection.update() called. RequiredSelected: $requiredElementsSelected. Result: $value")
            }
        }
        addSource(_currentTransactionType) { update() }
        addSource(_activeTransactionState) { update() }
        addSource(_isLoading) { update() }
    }

    private val _transactionSavedEvent = SingleLiveEvent<Boolean>()
    val transactionSavedEvent: LiveData<Boolean> = _transactionSavedEvent

    private val _errorMessageEvent = SingleLiveEvent<String>()
    val errorMessageEvent: LiveData<String> = _errorMessageEvent

    private val _criticalErrorEvent = SingleLiveEvent<String>()
    val criticalErrorEvent: LiveData<String> = _criticalErrorEvent

    // Навигационные события
    private val _navigateToAccountSettingsEvent = SingleLiveEvent<Unit>()
    val navigateToAccountSettingsEvent: LiveData<Unit> = _navigateToAccountSettingsEvent

    private val _navigateToCategorySettingsEvent = SingleLiveEvent<Unit>()
    val navigateToCategorySettingsEvent: LiveData<Unit> = _navigateToCategorySettingsEvent

    private val _showAccountSelectionSheetEvent = SingleLiveEvent<ShowAccountSelectionSheetRequest>()
    val showAccountSelectionSheetEvent: LiveData<ShowAccountSelectionSheetRequest> = _showAccountSelectionSheetEvent

    init {
        Log.d("ViewModel", "ViewModel init. IsEditing: $isEditing, ID: $transactionId, Initial Type: $initialTransactionType")
        if (isEditing) {
            if (initialTransactionType != null) {
                loadTransaction(transactionId, initialTransactionType)
            } else {
                val msg = "Критическая ошибка: Режим редактирования, но тип транзакции не определен."
                Log.e("ViewModel", msg)
                _criticalErrorEvent.postValue(msg)
                _isLoading.value = false
            }
        } else {
            _isLoading.value = false
            _currentTransactionType.value = TransactionType.EXPENSE
            resetActiveInputState(TransactionType.EXPENSE)
            updateAmountLivedata()

            accountsObserver = Observer { accounts ->
                if (!initialAccountSetupDone && accounts != null) {
                    val defaultAccount = accounts.firstOrNull { it.default && it.used }
                    if (defaultAccount != null) {
                        _sharedPaymentAccount.value = defaultAccount
                        Log.d("ViewModel", "Default account set: ${defaultAccount.title}")
                        resetActiveInputState(_currentTransactionType.value!!)
                    }
                    initialAccountSetupDone = true
                    accountsList.removeObserver(accountsObserver!!)
                    accountsObserver = null
                } else if (initialAccountSetupDone && accountsObserver != null) {
                    accountsList.removeObserver(accountsObserver!!)
                    accountsObserver = null
                }
            }
            accountsList.observeForever(accountsObserver!!)
        }
    }

    private fun updateAmountLivedata() {
        _amountString.value = amountProcessor.getAmountString()
        _hasOperatorInAmount.value = amountProcessor.hasOperator()
    }

    private fun loadTransaction(id: Long, type: TransactionType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val payment = if (type != TransactionType.REMITTANCE) loadPaymentByIdUseCase(id) else null
                val transfer = if (type == TransactionType.REMITTANCE) loadTransferByIdUseCase(id) else null
                var loadedAmountString = "0"

                when {
                    payment != null -> {
                        _currentTransactionType.value = payment.type
                        _loadedTransactionType.value = payment.type
                        _sharedPaymentAccount.value = payment.moneyAccount
                        _activeTransactionState.value = ActiveTransactionState.ExpenseIncomeState(
                            selectedCategory = payment.category,
                            selectedPaymentAccount = payment.moneyAccount
                        )
                        loadedAmountString = if (payment.balance != 0.0) formatDoubleToStringForLoad(payment.balance) else "0"
                        _note.value = payment.note
                        _date.value = payment.date
                    }
                    transfer != null -> {
                        _currentTransactionType.value = TransactionType.REMITTANCE
                        _loadedTransactionType.value = TransactionType.REMITTANCE
                        _sharedPaymentAccount.value = transfer.moneyAccFrom
                        _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                            selectedAccountFrom = transfer.moneyAccFrom,
                            selectedAccountTo = transfer.moneyAccTo
                        )
                        loadedAmountString = if (transfer.balance != 0.0) formatDoubleToStringForLoad(transfer.balance) else "0"
                        _note.value = transfer.note
                        _date.value = transfer.date
                    }
                    else -> {
                        Log.e("ViewModel", "Failed to load transaction id=$id, type=$type. Neither payment nor transfer found.")
                        _criticalErrorEvent.postValue("Не удалось загрузить транзакцию для редактирования.")
                        _isLoading.value = false
                        return@launch
                    }
                }
                amountProcessor.reset(loadedAmountString)
                updateAmountLivedata()
            } catch (e: Exception) {
                Log.e("ViewModel", "Error loading transaction", e)
                _criticalErrorEvent.postValue("Ошибка при загрузке транзакции: ${e.localizedMessage}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatDoubleToStringForLoad(value: Double): String {
        val roundedValue = (value * 100).toLong() / 100.0
        val stringRepresentation = if (roundedValue % 1.0 == 0.0) roundedValue.toLong().toString() else roundedValue.toString()
        return stringRepresentation.replace('.', ',')
    }

    fun handleKeypadInput(key: String) {
        amountProcessor.processKey(key)
        updateAmountLivedata()
    }

    fun setNote(text: String) {
        if (_note.value != text) {
            _note.value = text
        }
    }

    fun setDate(newDate: LocalDate) {
        if (_date.value != newDate) {
            _date.value = newDate
        }
    }

    fun setTransactionType(type: TransactionType) {
        if (_currentTransactionType.value != type) {
            _currentTransactionType.value = type
            resetActiveInputState(type)
            amountProcessor.reset("0")
            updateAmountLivedata()
        }
    }

    fun evaluateAndDisplayAmount() {
        if (amountProcessor.evaluateAndSetResult()) {
            updateAmountLivedata()
        } else {
            _errorMessageEvent.value = "Неверное выражение"
        }
    }

    private fun resetActiveInputState(newType: TransactionType) {
        when (newType) {
            TransactionType.EXPENSE, TransactionType.INCOME -> {
                _activeTransactionState.value = ActiveTransactionState.ExpenseIncomeState(
                    selectedCategory = null,
                    selectedPaymentAccount = _sharedPaymentAccount.value
                )
            }
            TransactionType.REMITTANCE -> {
                val currentRemittanceState = _activeTransactionState.value as? ActiveTransactionState.RemittanceState
                val accountTo = if (_sharedPaymentAccount.value?.id == currentRemittanceState?.selectedAccountTo?.id) {
                    null
                } else {
                    currentRemittanceState?.selectedAccountTo
                }
                _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                    selectedAccountFrom = _sharedPaymentAccount.value,
                    selectedAccountTo = accountTo
                )
            }
        }
    }

    private fun resetAllStateForNewTransaction() {
        val currentType = _currentTransactionType.value ?: TransactionType.EXPENSE
        resetActiveInputState(currentType)
        amountProcessor.reset("0")
        updateAmountLivedata()
        _note.value = ""
        _date.value = LocalDate.now()
    }

    private fun validateTransactionInput(currentAmountStringFromProcessor: String): Double? {
        val evaluatedAmount = amountProcessor.evaluateExpressionOnly(currentAmountStringFromProcessor)

        if (evaluatedAmount == null) {
            _errorMessageEvent.value = "Неверная сумма"
            return null
        }
        if (evaluatedAmount <= 0) {
            _errorMessageEvent.value = "Введите сумму больше нуля"
            return null
        }

        var errorMsg: String? = null
        when (val state = _activeTransactionState.value) {
            is ActiveTransactionState.ExpenseIncomeState -> {
                if (state.selectedCategory == null) errorMsg = "Выберите категорию"
                else if (state.selectedPaymentAccount == null) errorMsg = "Выберите счет"
            }
            is ActiveTransactionState.RemittanceState -> {
                if (state.selectedAccountFrom == null) errorMsg = "Выберите счет списания"
                else if (state.selectedAccountTo == null) errorMsg = "Выберите счет зачисления"
                else if (state.selectedAccountFrom.id == state.selectedAccountTo.id) errorMsg = "Счета должны отличаться"
            }
            ActiveTransactionState.InitialState, null -> {
                errorMsg = when (_currentTransactionType.value) {
                    TransactionType.EXPENSE, TransactionType.INCOME -> "Выберите категорию и счет"
                    TransactionType.REMITTANCE -> "Выберите счета"
                    else -> "Не выбран тип транзакции"
                }
            }
        }

        if (errorMsg != null) {
            _errorMessageEvent.value = errorMsg!!
            return null
        }
        return evaluatedAmount
    }

    fun saveTransaction() {
        if (amountProcessor.hasOperator()) {
            if (!amountProcessor.evaluateAndSetResult()) {
                _errorMessageEvent.value = "Неверное выражение для суммы"
                updateAmountLivedata()
                return
            }
            updateAmountLivedata()
        }

        val validAmount = validateTransactionInput(amountProcessor.getAmountString())
        if (validAmount == null) {
            return
        }

        val transactionDate = _date.value!!
        val transactionNote = _note.value ?: ""
        val currentActiveType = _currentTransactionType.value!!
        val currentActiveState = _activeTransactionState.value!!

        viewModelScope.launch {
            try {
                if (isEditing) {
                    performSaveExistingTransaction(transactionId, initialTransactionType!!, validAmount, transactionDate, transactionNote, currentActiveState, currentActiveType)
                } else {
                    performSaveNewTransaction(validAmount, transactionDate, transactionNote, currentActiveState, currentActiveType)
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error in saveTransaction coroutine", e)
                _errorMessageEvent.postValue("Ошибка сохранения: ${e.localizedMessage}")
            }
        }
    }

    private suspend fun performSaveNewTransaction(
        validAmount: Double, date: LocalDate, note: String,
        activeState: ActiveTransactionState, type: TransactionType
    ) {
        when (activeState) {
            is ActiveTransactionState.ExpenseIncomeState -> {
                val payment = Payment(
                    type = type, balance = validAmount, moneyAccount = activeState.selectedPaymentAccount!!,
                    category = activeState.selectedCategory!!, _note = Title(note), date = date
                )
                createPaymentUseCase(payment)
            }
            is ActiveTransactionState.RemittanceState -> {
                val transfer = Transfer(
                    type = TransactionType.REMITTANCE, balance = validAmount, moneyAccFrom = activeState.selectedAccountFrom!!,
                    moneyAccTo = activeState.selectedAccountTo!!, _note = Title(note), date = date
                )
                createTransferUseCase(transfer)
            }
            ActiveTransactionState.InitialState -> {
                Log.e("ViewModel", "Attempted to save new transaction from InitialState. This should not happen.")
                _errorMessageEvent.postValue("Внутренняя ошибка: Попытка сохранения из начального состояния.")
                return
            }
        }
        _transactionSavedEvent.postValue(true)
        resetAllStateForNewTransaction()
    }

    private suspend fun performSaveExistingTransaction(
        transactionId: Long, originalType: TransactionType, validAmount: Double, date: LocalDate, note: String,
        activeState: ActiveTransactionState, currentSelectedType: TransactionType
    ) {
        if (currentSelectedType != originalType) {
            val deleteSuccess = deleteTransactionUseCase(transactionId, originalType)
            if (!deleteSuccess) {
                _errorMessageEvent.postValue("Ошибка при изменении типа транзакции (не удалось удалить старую).")
                return
            }
            performSaveNewTransaction(validAmount, date, note, activeState, currentSelectedType)
        } else {
            when (currentSelectedType) {
                TransactionType.EXPENSE, TransactionType.INCOME -> {
                    if (activeState is ActiveTransactionState.ExpenseIncomeState) {
                        val payment = Payment(
                            id = transactionId, type = currentSelectedType, balance = validAmount,
                            moneyAccount = activeState.selectedPaymentAccount!!, category = activeState.selectedCategory!!,
                            _note = Title(note), date = date
                        )
                        updatePaymentUseCase(payment)
                    } else {
                        _errorMessageEvent.postValue("Внутренняя ошибка состояния UI (E/I).")
                        return
                    }
                }
                TransactionType.REMITTANCE -> {
                    if (activeState is ActiveTransactionState.RemittanceState) {
                        val transfer = Transfer(
                            id = transactionId, type = TransactionType.REMITTANCE, balance = validAmount,
                            moneyAccFrom = activeState.selectedAccountFrom!!, moneyAccTo = activeState.selectedAccountTo!!,
                            _note = Title(note), date = date
                        )
                        updateTransferUseCase(transfer)
                    } else {
                        _errorMessageEvent.postValue("Внутренняя ошибка состояния UI (Remit).")
                        return
                    }
                }
            }
            _transactionSavedEvent.postValue(true)
            resetAllStateForNewTransaction()
        }
    }

    fun selectCategory(category: ICategoryData) {
        val currentVmType = _currentTransactionType.value
        val expectedCategoryType = when(currentVmType) {
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.INCOME -> CategoryType.INCOME
            else -> null
        }
        if (currentVmType != TransactionType.REMITTANCE && category.categoryType == expectedCategoryType) {
            val currentState = _activeTransactionState.value
            val paymentAccountToSet = _sharedPaymentAccount.value ?:
            (if (currentState is ActiveTransactionState.ExpenseIncomeState) currentState.selectedPaymentAccount else null)

            _activeTransactionState.value = ActiveTransactionState.ExpenseIncomeState(
                selectedCategory = category,
                selectedPaymentAccount = paymentAccountToSet
            )
        }
    }

    fun selectPaymentAccount(account: MoneyAccount) {
        val currentType = _currentTransactionType.value
        if (currentType == TransactionType.EXPENSE || currentType == TransactionType.INCOME) {
            val currentState = _activeTransactionState.value
            val currentCategory = if (currentState is ActiveTransactionState.ExpenseIncomeState) currentState.selectedCategory else null
            _activeTransactionState.value = ActiveTransactionState.ExpenseIncomeState(
                selectedCategory = currentCategory,
                selectedPaymentAccount = account
            )
            _sharedPaymentAccount.value = account
            Log.d("ViewModel", "Shared account updated by selectPaymentAccount: ${account.title}")
        }
    }

    fun selectAccountFrom(account: MoneyAccount) {
        val currentType = _currentTransactionType.value
        if (currentType == TransactionType.REMITTANCE) {
            val currentState = _activeTransactionState.value as? ActiveTransactionState.RemittanceState
            val currentToAccount = currentState?.selectedAccountTo
            if (account.id == currentToAccount?.id && currentToAccount != null) {
                _errorMessageEvent.value = "Счета должны отличаться"
                return
            }
            _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                selectedAccountFrom = account,
                selectedAccountTo = currentToAccount
            )
            _sharedPaymentAccount.value = account
            Log.d("ViewModel", "Shared account updated by selectAccountFrom: ${account.title}")
        }
    }

    fun selectAccountTo(account: MoneyAccount) {
        val currentType = _currentTransactionType.value
        if (currentType == TransactionType.REMITTANCE) {
            val currentState = _activeTransactionState.value as? ActiveTransactionState.RemittanceState
            val currentFromAccount = currentState?.selectedAccountFrom
            if (account.id == currentFromAccount?.id && currentFromAccount != null) {
                _errorMessageEvent.value = "Счета должны отличаться"
                return
            }
            _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                selectedAccountFrom = currentFromAccount,
                selectedAccountTo = account
            )
        }
    }

    fun requestNavigateToAccountSettings() {
        _navigateToAccountSettingsEvent.call()
    }

    fun requestNavigateToCategorySettings() {
        _navigateToCategorySettingsEvent.call()
    }

    fun requestAccountSelectionForExpenseIncome() {
        val currentSelectedId = (_activeTransactionState.value as? ActiveTransactionState.ExpenseIncomeState)
            ?.selectedPaymentAccount?.id
        _showAccountSelectionSheetEvent.value = ShowAccountSelectionSheetRequest(
            accounts = accountsList.value ?: emptyList(),
            selectedId = currentSelectedId,
            isForExpenseIncome = true,
            isForAccountFrom = null
        )
    }

    fun requestAccountSelectionForRemittanceFrom() {
        val currentSelectedId = (_activeTransactionState.value as? ActiveTransactionState.RemittanceState)
            ?.selectedAccountFrom?.id
        _showAccountSelectionSheetEvent.value = ShowAccountSelectionSheetRequest(
            accounts = accountsList.value ?: emptyList(),
            selectedId = currentSelectedId,
            isForExpenseIncome = false,
            isForAccountFrom = true
        )
    }

    fun requestAccountSelectionForRemittanceTo() {
        val currentSelectedId = (_activeTransactionState.value as? ActiveTransactionState.RemittanceState)
            ?.selectedAccountTo?.id
        _showAccountSelectionSheetEvent.value = ShowAccountSelectionSheetRequest(
            accounts = accountsList.value ?: emptyList(),
            selectedId = currentSelectedId,
            isForExpenseIncome = false,
            isForAccountFrom = false
        )
    }
}
