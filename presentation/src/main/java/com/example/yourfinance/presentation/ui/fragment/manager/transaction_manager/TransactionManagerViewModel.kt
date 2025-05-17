package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.util.Log
import androidx.lifecycle.*
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.domain.usecase.categories.category.FetchCategoriesUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.FetchMoneyAccountsUseCase
import com.example.yourfinance.domain.usecase.transaction.CreatePaymentUseCase
import com.example.yourfinance.domain.usecase.transaction.CreateTransferUseCase
import com.example.yourfinance.domain.usecase.transaction.LoadPaymentByIdUseCase
import com.example.yourfinance.domain.usecase.transaction.LoadTransferByIdUseCase
import com.example.yourfinance.domain.usecase.transaction.UpdatePaymentUseCase
import com.example.yourfinance.domain.usecase.transaction.UpdateTransferUseCase
import com.example.yourfinance.domain.usecase.transaction.DeleteTransactionByIdAndTypeUseCase
import com.example.yourfinance.presentation.ui.util.AmountInputProcessor
import com.example.yourfinance.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeParseException
import javax.inject.Inject


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
    val allCategories: LiveData<List<Category>> = fetchFullCategoriesUseCase()

    private val _currentTransactionType = MutableLiveData(initialTransactionType ?: TransactionType.EXPENSE)
    val currentTransactionType: LiveData<TransactionType> = _currentTransactionType

    // Store the account selected in Expense/Income to be shared
    private val _sharedPaymentAccount = MutableLiveData<MoneyAccount?>(null)
    // No public LiveData for _sharedPaymentAccount needed for now, used internally.

    private val _activeTransactionState = MutableLiveData<ActiveTransactionState>(ActiveTransactionState.InitialState)
    val activeTransactionState: LiveData<ActiveTransactionState> = _activeTransactionState

    private val amountProcessor = AmountInputProcessor()

    private val _amountString = MutableLiveData(amountProcessor.getAmountString())
    val amountString: LiveData<String> = _amountString

    private val _hasOperatorInAmount = MutableLiveData(amountProcessor.hasOperator())
    val hasOperatorInAmount: LiveData<Boolean> = _hasOperatorInAmount

    private fun updateAmountLivedata() {
        _amountString.value = amountProcessor.getAmountString()
        _hasOperatorInAmount.value = amountProcessor.hasOperator()
    }

    private val _note = MutableLiveData("")
    val note: LiveData<String> = _note

    private val _date = MutableLiveData(LocalDate.now())
    val date: LiveData<LocalDate> = _date

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

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
                Log.d("ViewModel", "showInputSection.update() called. IsEditing: $isEditing, Active Type: ${_currentTransactionType.value}, State: ${_activeTransactionState.value}. RequiredSelected: $requiredElementsSelected. Result: $value")
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

    init {
        Log.d("ViewModel", "ViewModel init. IsEditing: $isEditing, ID: $transactionId, Initial Type: $initialTransactionType")
        if (isEditing) {
            // _isLoading.value = true // Will be set at the start of loadTransaction
            if (initialTransactionType != null) {
                loadTransaction(transactionId, initialTransactionType)
            } else {
                val msg = "Критическая ошибка: Режим редактирования, но тип транзакции не определен."
                Log.e("ViewModel", msg)
                _criticalErrorEvent.postValue(msg)
                _isLoading.value = false // Ensure isLoading is reset
            }
        } else {
            _isLoading.value = false
            // For new transactions, _sharedPaymentAccount starts as null.
            // resetActiveInputState will use this null value initially.
            _currentTransactionType.value = TransactionType.EXPENSE // Default to EXPENSE
            resetActiveInputState(TransactionType.EXPENSE) // Initialize state for EXPENSE
            updateAmountLivedata() // Ensure amount is initialized correctly
        }
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
                        _sharedPaymentAccount.value = payment.moneyAccount // <--- ВАЖНО: Инициализируем общий счет
                        Log.d("ViewModel", "Shared account set from loaded Payment: ${payment.moneyAccount.title}")
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
                        _sharedPaymentAccount.value = transfer.moneyAccFrom // <--- ВАЖНО: Инициализируем общий счет (счет "Откуда")
                        Log.d("ViewModel", "Shared account set from loaded Transfer (From): ${transfer.moneyAccFrom.title}")
                        _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                            selectedAccountFrom = transfer.moneyAccFrom,
                            selectedAccountTo = transfer.moneyAccTo
                        )
                        loadedAmountString = if (transfer.balance != 0.0) formatDoubleToStringForLoad(transfer.balance) else "0"
                        _note.value = transfer.note
                        _date.value = transfer.date
                    }
                    else -> {
                        // ... обработка ошибки ...
                        return@launch
                    }
                }
                amountProcessor.reset(loadedAmountString)
                updateAmountLivedata()
            } catch (e: Exception) {
                // ... обработка ошибки ...
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
            // Amount and other common state are reset after resetActiveInputState
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
        Log.d("ViewModel", "Resetting active input state for type: $newType. Shared account is: ${_sharedPaymentAccount.value?.title}")
        when (newType) {
            TransactionType.EXPENSE, TransactionType.INCOME -> {
                _activeTransactionState.value = ActiveTransactionState.ExpenseIncomeState(
                    selectedCategory = null, // Категория всегда сбрасывается
                    selectedPaymentAccount = _sharedPaymentAccount.value // Используем общий счет
                )
            }
            TransactionType.REMITTANCE -> {
                // Для "Куда" берем текущее значение, если оно не совпадает с новым "Откуда"
                val currentRemittanceState = _activeTransactionState.value as? ActiveTransactionState.RemittanceState
                val accountTo = if (_sharedPaymentAccount.value?.id == currentRemittanceState?.selectedAccountTo?.id) {
                    null // Если общий счет (новый "Откуда") совпадает с текущим "Куда", сбрасываем "Куда"
                } else {
                    currentRemittanceState?.selectedAccountTo // Иначе сохраняем текущий "Куда"
                }
                _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                    selectedAccountFrom = _sharedPaymentAccount.value, // Используем общий счет для "Откуда"
                    selectedAccountTo = accountTo
                )
            }
        }
        // amountProcessor.reset("0") и updateAmountLivedata() вызываются в setTransactionType после этого.
        // Также _note и _date сбрасываются в resetAllStateForNewTransaction, что хорошо
    }

    private fun resetAllStateForNewTransaction() {
        // When a transaction is saved, _sharedPaymentAccount should persist for the next new transaction.
        // So, we don't reset _sharedPaymentAccount here.
        // We reset _activeTransactionState based on current _currentTransactionType and _sharedPaymentAccount.
        val currentType = _currentTransactionType.value ?: TransactionType.EXPENSE // Fallback if somehow null
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
                errorMsg = when(_currentTransactionType.value) {
                    TransactionType.EXPENSE, TransactionType.INCOME -> "Выберите категорию и счет"
                    TransactionType.REMITTANCE -> "Выберите счета"
                    null -> "Не выбран тип транзакции"
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
            ActiveTransactionState.InitialState -> return
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
                    } else { _errorMessageEvent.postValue("Внутренняя ошибка состояния UI (E/I)."); return }
                }
                TransactionType.REMITTANCE -> {
                    if (activeState is ActiveTransactionState.RemittanceState) {
                        val transfer = Transfer(
                            id = transactionId, type = TransactionType.REMITTANCE, balance = validAmount,
                            moneyAccFrom = activeState.selectedAccountFrom!!, moneyAccTo = activeState.selectedAccountTo!!,
                            _note = Title(note), date = date
                        )
                        updateTransferUseCase(transfer)
                    } else { _errorMessageEvent.postValue("Внутренняя ошибка состояния UI (Remit)."); return }
                }
            }
            _transactionSavedEvent.postValue(true)
            // For editing, we probably don't want to reset all state, but rather keep the edited state visible
            // or navigate back. The current _transactionSavedEvent handles navigation.
            // If we want to allow further edits, we shouldn't call resetAllStateForNewTransaction().
            // The current flow implies navigating back, so resetAllStateForNewTransaction() is fine if we stay on the screen.
            // However, since we navigate back, it's not strictly necessary to call it here for editing.
            // Let's keep it consistent with new transactions for now, as the navigation back will clear the screen context.
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
            // STAGE I: Use _sharedPaymentAccount if set, otherwise preserve existing from ExpenseIncomeState, or null
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
            _sharedPaymentAccount.value = account // Обновляем общий счет
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
            _sharedPaymentAccount.value = account // Обновляем общий счет
            Log.d("ViewModel", "Shared account updated by selectAccountFrom: ${account.title}")
        }
    }

    // selectAccountTo - не должен влиять на _sharedPaymentAccount, так как это счет "Куда"
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
}