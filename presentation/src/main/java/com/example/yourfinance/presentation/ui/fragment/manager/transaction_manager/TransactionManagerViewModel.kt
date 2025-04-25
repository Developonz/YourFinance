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
import com.example.yourfinance.domain.usecase.category.FetchFullCategoriesUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.FetchMoneyAccountsUseCase
import com.example.yourfinance.domain.usecase.transaction.CreatePaymentUseCase
import com.example.yourfinance.domain.usecase.transaction.CreateTransferUseCase
import com.example.yourfinance.domain.usecase.transaction.LoadPaymentByIdUseCase
import com.example.yourfinance.domain.usecase.transaction.LoadTransferByIdUseCase
import com.example.yourfinance.domain.usecase.transaction.UpdatePaymentUseCase
import com.example.yourfinance.domain.usecase.transaction.UpdateTransferUseCase
import com.example.yourfinance.domain.usecase.transaction.DeleteTransactionByIdAndTypeUseCase
import com.example.yourfinance.util.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeParseException
import javax.inject.Inject

// Убедитесь, что ActiveTransactionState определен в этом же файле или импортирован

@HiltViewModel
class TransactionManagerViewModel @Inject constructor(
    fetchMoneyAccountsUseCase: FetchMoneyAccountsUseCase,
    fetchFullCategoriesUseCase: FetchFullCategoriesUseCase,
    private val createPaymentUseCase: CreatePaymentUseCase,
    private val createTransferUseCase: CreateTransferUseCase,
    private val loadPaymentByIdUseCase: LoadPaymentByIdUseCase,
    private val loadTransferByIdUseCase: LoadTransferByIdUseCase,
    private val updatePaymentUseCase: UpdatePaymentUseCase,
    private val updateTransferUseCase: UpdateTransferUseCase,
    private val deleteTransactionUseCase: DeleteTransactionByIdAndTypeUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // --- Режим редактирования ---
    private val transactionId: Long = savedStateHandle.get<Long>("transactionId") ?: -1L
    // Получаем тип как Int (не nullable благодаря defaultValue) и преобразуем в TransactionType?
    private val initialTransactionType: TransactionType? = savedStateHandle.get<Int>("transactionTypeInt").let { typeInt ->
        if (typeInt != -1) { // Проверяем только Int значение, null-проверка на typeInt не нужна
            try {
                // Возвращаем enum константу по ее ordinal
                TransactionType.entries.getOrNull(typeInt!!) // Безопасное получение по индексу
            } catch (e: Exception) { // Ловим IndexOutOfBoundsException или другие возможные ошибки
                Log.e("ViewModel", "Invalid TransactionType ordinal from arguments: $typeInt", e)
                null // Неверный порядковый номер
            }
        } else {
            null // Дефолтное значение (-1)
        }
    }
    // Режим редактирования ТОЛЬКО если есть и ID (> -1) и Тип (не null, т.е. Int != -1)
    val isEditing: Boolean = transactionId != -1L && initialTransactionType != null

    private val _loadedTransactionType = MutableLiveData<TransactionType?>(null)
    val loadedTransactionType: LiveData<TransactionType?> = _loadedTransactionType // Фактический тип загруженной транзакции

    private var loadedTransactionTime: LocalTime? = null
    // ---------------------------


    val accountsList: LiveData<List<MoneyAccount>> = fetchMoneyAccountsUseCase()
    val allCategories: LiveData<List<Category>> = fetchFullCategoriesUseCase()


    // --- Состояние для экрана добавления/редактирования транзакции ---
    // Начальный тип определяется либо из аргументов (режим редактирования), либо по умолчанию (Расход)
    private val _currentTransactionType = MutableLiveData(initialTransactionType ?: TransactionType.EXPENSE)
    val currentTransactionType: LiveData<TransactionType> = _currentTransactionType

    private val _activeTransactionState = MutableLiveData<ActiveTransactionState>(ActiveTransactionState.InitialState)
    val activeTransactionState: LiveData<ActiveTransactionState> = _activeTransactionState

    private val _amountString = MutableLiveData("0")
    val amountString: LiveData<String> = _amountString

    private val _note = MutableLiveData("")
    val note: LiveData<String> = _note

    private val _date = MutableLiveData(LocalDate.now())
    val date: LiveData<LocalDate> = _date

    private val _hasOperatorInAmount = MutableLiveData(false)
    val hasOperatorInAmount: LiveData<Boolean> = _hasOperatorInAmount


    val showInputSection: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        value = isEditing // В режиме редактирования изначально true (будет видно после загрузки)
        fun update() {
            if (isEditing) {
                value = true
                return
            }

            // Логика для режима создания
            val requiredElementsSelected = when (_activeTransactionState.value) {
                is ActiveTransactionState.ExpenseIncomeState -> {
                    val state = _activeTransactionState.value as ActiveTransactionState.ExpenseIncomeState
                    state.selectedCategory != null // ИСПРАВЛЕНО: Достаточно выбрать только категорию
                }
                is ActiveTransactionState.RemittanceState -> {
                    val state = _activeTransactionState.value as ActiveTransactionState.RemittanceState
                    state.selectedAccountFrom != null && state.selectedAccountTo != null
                }
                ActiveTransactionState.InitialState, null -> false
            }

            val shouldShow = requiredElementsSelected

            if (value != shouldShow) {
                value = shouldShow
                Log.d("ViewModel", "showInputSection.update() called. IsEditing: $isEditing, Active Type: ${_currentTransactionType.value}, State: ${_activeTransactionState.value}. RequiredSelected: $requiredElementsSelected. Result: $value")
            }
        }
        addSource(_currentTransactionType) { update() }
        addSource(_activeTransactionState) { update() }
        // addSource(_amountString) { update() } // УДАЛЕНО
    }

    private val _isLoading = MutableLiveData(true)
    val isLoading: LiveData<Boolean> = _isLoading


    private val _transactionSavedEvent = SingleLiveEvent<Boolean>()
    val transactionSavedEvent: LiveData<Boolean> = _transactionSavedEvent

    private val _errorMessageEvent = SingleLiveEvent<String>()
    val errorMessageEvent: LiveData<String> = _errorMessageEvent

    private val _criticalErrorEvent = SingleLiveEvent<String>()
    val criticalErrorEvent: LiveData<String> = _criticalErrorEvent


    init {
        Log.d("ViewModel", "ViewModel init. IsEditing: $isEditing, ID: $transactionId, Initial Type: $initialTransactionType")
        if (isEditing) {
            if (transactionId != -1L && initialTransactionType != null) {
                loadTransaction(transactionId, initialTransactionType) // <-- Передаем тип при загрузке (теперь TransactionType)
            } else {
                // Если ID или Тип отсутствуют в режиме редактирования (неверные аргументы), считаем это ошибкой.
                val msg = "Неверные аргументы для редактирования транзакции (ID: $transactionId, Type Int: ${savedStateHandle.get<Int>("transactionTypeInt")})."
                Log.e("ViewModel", msg)
                _criticalErrorEvent.postValue(msg)
                _isLoading.value = false
            }
        } else {
            _isLoading.value = false
        }
    }

    // Метод loadTransaction теперь принимает ID и ТИП (TransactionType)
    private fun loadTransaction(id: Long, type: TransactionType) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Загружаем только соответствующий тип
                val payment = if (type != TransactionType.REMITTANCE) loadPaymentByIdUseCase(id) else null
                val transfer = if (type == TransactionType.REMITTANCE) loadTransferByIdUseCase(id) else null

                when {
                    payment != null -> {
                        Log.d("ViewModel", "Loaded Payment: $payment")
                        _currentTransactionType.value = payment.type // Должен совпадать с type
                        _loadedTransactionType.value = payment.type // Используется контейнером
                        loadedTransactionTime = payment.time

                        _activeTransactionState.value = ActiveTransactionState.ExpenseIncomeState(
                            selectedCategory = payment.category,
                            selectedPaymentAccount = payment.moneyAccount
                        )
                        _amountString.value = if (payment.balance != 0.0) formatDoubleToString(payment.balance) else "0"
                        _note.value = payment.note
                        _date.value = payment.date
                        _hasOperatorInAmount.value = false
                    }
                    transfer != null -> {
                        Log.d("ViewModel", "Loaded Transfer: $transfer")
                        _currentTransactionType.value = TransactionType.REMITTANCE // Должен совпадать с type
                        _loadedTransactionType.value = TransactionType.REMITTANCE // Используется контейнером
                        loadedTransactionTime = transfer.time

                        _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                            selectedAccountFrom = transfer.moneyAccFrom,
                            selectedAccountTo = transfer.moneyAccTo
                        )
                        _amountString.value = if (transfer.balance != 0.0) formatDoubleToString(transfer.balance) else "0"
                        _note.value = transfer.note
                        _date.value = transfer.date
                        _hasOperatorInAmount.value = false
                    }
                    else -> {
                        // Транзакция не найдена (ID+Type не совпали)
                        val msg = "Транзакция с ID $id и типом $type не найдена."
                        Log.e("ViewModel", msg)
                        _criticalErrorEvent.postValue(msg)
                    }
                }
            } catch (e: Exception) {
                val msg = "Ошибка загрузки транзакции с ID $id и типом $type: ${e.localizedMessage}"
                Log.e("ViewModel", msg, e)
                if (e is DateTimeParseException) {
                    _criticalErrorEvent.postValue("Ошибка при парсинге даты/времени транзакции.")
                } else {
                    _criticalErrorEvent.postValue("Не удалось загрузить транзакцию.")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun formatDoubleToString(value: Double): String {
        return if (value % 1.0 == 0.0) value.toLong().toString() else value.toString().replace('.', ',')
    }


    private fun evaluateExpression(expression: String): Double? {
        if (expression.isEmpty()) return null

        val cleanExpression = expression.trimEnd('+', '-', '.')
        if (cleanExpression.isEmpty()) return null

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

            var result = tokens.first().toDoubleOrNull() ?: return null

            var i = 1
            while (i < tokens.size - 1) {
                val operator = tokens[i]
                val nextNumberString = tokens[i+1]
                val nextNumber = nextNumberString.toDoubleOrNull() ?: return null

                when (operator) {
                    "+" -> result += nextNumber
                    "-" -> result -= nextNumber
                    else -> return null
                }
                i += 2
            }
            return result

        } catch (e: Exception) {
            Log.e("ViewModel", "Error evaluating expression: $expression", e)
            return null
        }
    }


    fun handleKeypadInput(key: String) {
        val currentAmount = _amountString.value ?: "0"
        var newAmount = currentAmount

        when (key) {
            "DEL" -> {
                newAmount = if (currentAmount.isNotEmpty()) {
                    currentAmount.dropLast(1).ifEmpty { "0" }
                } else {
                    "0"
                }
            }
            "." -> {
                val parts = currentAmount.split('+', '-').filter { it.isNotEmpty() }
                val lastPart = parts.lastOrNull() ?: ""

                if (!lastPart.contains('.') && (lastPart.isNotEmpty() || currentAmount == "0")) {
                    newAmount = if (currentAmount.isEmpty()) "0." else "$currentAmount."
                } else {
                    return
                }
            }
            "+", "-" -> {
                if (currentAmount.isEmpty() || currentAmount.last().let { it == '+' || it == '-' }) {
                    return
                }
                newAmount = currentAmount + key
            }
            else -> { // Цифры
                val parts = currentAmount.split('+', '-').filter { it.isNotEmpty() }
                val lastPart = parts.lastOrNull() ?: ""

                if (lastPart.contains('.') && lastPart.substringAfter('.').length >= 2) {
                    return
                }
                if (currentAmount == "0" && key == "0") {
                    return
                }
                if (currentAmount == "0" && key != ".") {
                    newAmount = key
                } else if (currentAmount.length < 15) {
                    newAmount = currentAmount + key
                } else {
                    return
                }
            }
        }

        if (_amountString.value != newAmount) {
            _amountString.value = newAmount
            _hasOperatorInAmount.value = newAmount.contains('+') || newAmount.contains('-')
        }
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

    // Устанавливается хост-компонентом (TransactionContainerFragment) при смене страницы ViewPager
    // Этот метод теперь ВСЕГДА сбрасывает activeTransactionState при смене типа
    fun setTransactionType(type: TransactionType) {
        // Проверяем, если тип действительно меняется
        if (_currentTransactionType.value != type) {
            Log.d("ViewModel", "setTransactionType changing from ${_currentTransactionType.value} to $type. Is Editing: $isEditing")
            _currentTransactionType.value = type

            // ВСЕГДА сбрасываем activeTransactionState к InitialState при смене типа
            // Это гарантирует, что специфичные для типа элементы UI на новой вкладке будут очищены.
            resetActiveInputState(type)

            // Общие поля (сумма, дата, заметка) не сбрасываются при свайпе.

        } else {
            Log.d("ViewModel", "setTransactionType called with the same type: $type. Is Editing: $isEditing")
            // Если тип тот же, ничего не делаем.
        }
    }


    // НОВЫЙ МЕТОД: Вычисляет выражение и обновляет строку суммы (используется кнопкой "=")
    fun evaluateAndDisplayAmount() {
        val currentAmount = _amountString.value ?: "0"
        val evaluatedAmount = evaluateExpression(currentAmount)

        if (evaluatedAmount != null) {
            val formattedResult = formatDoubleToString(evaluatedAmount)
            _amountString.value = formattedResult
            _hasOperatorInAmount.value = false
            Log.d("ViewModel", "Evaluated '$currentAmount' to '$formattedResult'")
        } else {
            Log.w("ViewModel", "Evaluation failed for expression: '$currentAmount'")
            _errorMessageEvent.value = "Неверное выражение"
        }
    }


    private fun resetActiveInputState(newType: TransactionType) {
        Log.i("ViewModel", "Resetting active input state for new type: $newType")
        // Устанавливаем activeTransactionState в InitialState для нового типа
        _activeTransactionState.value = ActiveTransactionState.InitialState // <-- ИСПРАВЛЕНО: Всегда InitialState

        // Reset operator indicator based on current amount string (which wasn't reset)
        _hasOperatorInAmount.value = _amountString.value?.contains('+') == true || _amountString.value?.contains('-') == true
    }

    private fun resetAllStateForNewTransaction() {
        Log.i("ViewModel", "Resetting ALL state after save.")
        _activeTransactionState.value = ActiveTransactionState.InitialState
        _amountString.value = "0"
        _note.value = ""
        _date.value = LocalDate.now()
        loadedTransactionTime = null
        _hasOperatorInAmount.value = false // Сбрасываем индикатор оператора при полном сбросе
    }

    fun resetSavedEvent() { }
    fun resetErrorEvent() { }
    fun resetCriticalErrorEvent() { }

    private fun isSelectionValid(): Boolean {
        val type = _currentTransactionType.value
        val amount = _amountString.value?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
        var errorMsg: String? = null

        if (amount <= 0) {
            errorMsg = "Введите сумму больше нуля"
        } else {
            when (_activeTransactionState.value) {
                is ActiveTransactionState.ExpenseIncomeState -> {
                    val state = _activeTransactionState.value as ActiveTransactionState.ExpenseIncomeState
                    if (state.selectedCategory == null) errorMsg = "Выберите категорию"
                    else if (state.selectedPaymentAccount == null) errorMsg = "Выберите счет"
                }
                is ActiveTransactionState.RemittanceState -> {
                    val state = _activeTransactionState.value as ActiveTransactionState.RemittanceState
                    if (state.selectedAccountFrom == null) errorMsg = "Выберите счет списания"
                    else if (state.selectedAccountTo == null) errorMsg = "Выберите счет зачисления"
                }
                ActiveTransactionState.InitialState, null -> {
                    errorMsg = when(type) {
                        TransactionType.EXPENSE, TransactionType.INCOME -> "Выберите категорию и счет"
                        TransactionType.REMITTANCE -> "Выберите счета"
                        null -> "Не выбран тип транзакции"
                    }
                }
            }
        }

        if (errorMsg != null) {
            _errorMessageEvent.value = errorMsg!!
            return false
        }
        return true
    }

    fun saveTransaction() {
        Log.d("ViewModel", "saveTransaction called. IsEditing: $isEditing, ID: $transactionId, Initial Type: $initialTransactionType")

        val finalAmountString = _amountString.value ?: "0"
        val evaluatedAmount = evaluateExpression(finalAmountString)

        if (evaluatedAmount == null) {
            _errorMessageEvent.value = "Неверная сумма"
            return
        }

        val validAmount = evaluatedAmount

        // При сохранении, валидация требует, чтобы все необходимые поля для *текущего* типа были заполнены.
        if (!isSelectionValid()) {
            return
        }

        val transactionDate = _date.value!!
        val transactionTime = if (isEditing && loadedTransactionTime != null) loadedTransactionTime!! else LocalTime.now()
        val transactionNote = _note.value ?: ""
        val currentActiveType = _currentTransactionType.value!! // Тип, который выбрал пользователь сейчас (или загружен)

        val currentActiveState = _activeTransactionState.value!! // Состояние, соответствующее currentActiveType

        viewModelScope.launch {
            try {
                if (isEditing) {
                    val originalType = initialTransactionType // Тип из аргументов (не null в режиме редактирования)

                    // Проверяем, изменился ли тип транзакции при редактировании
                    if (currentActiveType != originalType) {
                        Log.d("ViewModel", "Attempting to CHANGE transaction type: $originalType -> $currentActiveType. Deleting original and creating new.")
                        // 1. Удаляем старую транзакцию по ID и исходному типу.
                        val deleteSuccess = deleteTransactionUseCase(transactionId, originalType!!) // originalType не null в режиме редактирования
                        if (!deleteSuccess) {
                            Log.e("ViewModel", "Failed to delete original transaction $originalType with ID $transactionId during type change.")
                            _errorMessageEvent.postValue("Ошибка при изменении типа транзакции (не удалось удалить старую).")
                            return@launch
                        }
                        Log.d("ViewModel", "Original transaction deleted. Proceeding with creating new.")

                        // 2. Создаем новую транзакцию с новым типом и текущими данными.
                        when (currentActiveType) { // Используем currentActiveType для определения, что создавать
                            TransactionType.EXPENSE, TransactionType.INCOME -> {
                                if (currentActiveState is ActiveTransactionState.ExpenseIncomeState) {
                                    val payment = Payment(
                                        // ID сбрасывается при создании новой записи, будет сгенерирован новый
                                        type = currentActiveType, // НОВЫЙ ТИП
                                        balance = validAmount,
                                        moneyAccount = currentActiveState.selectedPaymentAccount!!,
                                        category = currentActiveState.selectedCategory!!,
                                        _note = Title(transactionNote),
                                        date = transactionDate,
                                        time = transactionTime
                                    )
                                    createPaymentUseCase(payment) // Создаем как новую
                                    Log.d("ViewModel", "Created new Payment after type change: $payment")
                                } else {
                                    // Несоответствие текущего типа и состояния activeState - внутренняя ошибка
                                    Log.e("ViewModel", "Type changed to EXPENSE/INCOME but activeState is not ExpenseIncomeState!")
                                    _errorMessageEvent.postValue("Внутренняя ошибка состояния UI.")
                                    return@launch
                                }
                            }
                            TransactionType.REMITTANCE -> {
                                if (currentActiveState is ActiveTransactionState.RemittanceState) {
                                    val transfer = Transfer(
                                        // ID сбрасывается при создании новой записи
                                        type = TransactionType.REMITTANCE, // НОВЫЙ ТИП (всегда REMITTANCE)
                                        balance = validAmount,
                                        moneyAccFrom = currentActiveState.selectedAccountFrom!!,
                                        moneyAccTo = currentActiveState.selectedAccountTo!!,
                                        _note = Title(transactionNote),
                                        date = transactionDate,
                                        time = transactionTime
                                    )
                                    createTransferUseCase(transfer) // Создаем как новую
                                    Log.d("ViewModel", "Created new Transfer after type change: $transfer")
                                } else {
                                    // Несоответствие текущего типа и состояния activeState - внутренняя ошибка
                                    Log.e("ViewModel", "Type changed to REMITTANCE but activeState is not RemittanceState!")
                                    _errorMessageEvent.postValue("Внутренняя ошибка состояния UI.")
                                    return@launch
                                }
                            }
                        }
                        Log.i("ViewModel", "Transaction type changed and new transaction created successfully!")
                        _transactionSavedEvent.postValue(true) // Успех!

                    } else {
                        // Тип транзакции НЕ был изменен. Просто обновляем существующую запись по ИСХОДНОМУ ID.
                        Log.d("ViewModel", "Attempting to UPDATE transaction of type: $currentActiveType, ID: $transactionId (Type not changed)")
                        when (currentActiveType) { // Используем currentActiveType для определения, что обновлять
                            TransactionType.EXPENSE, TransactionType.INCOME -> {
                                if (currentActiveState is ActiveTransactionState.ExpenseIncomeState) {
                                    val payment = Payment(
                                        id = transactionId, // Сохраняем ИСХОДНЫЙ ID
                                        type = currentActiveType, // ТОТ ЖЕ ТИП, ЧТО ИСХОДНЫЙ
                                        balance = validAmount,
                                        moneyAccount = currentActiveState.selectedPaymentAccount!!,
                                        category = currentActiveState.selectedCategory!!,
                                        _note = Title(transactionNote),
                                        date = transactionDate,
                                        time = transactionTime
                                    )
                                    updatePaymentUseCase(payment) // Обновляем
                                    Log.d("ViewModel", "Updating Payment: $payment")
                                } else {
                                    Log.e("ViewModel", "Current type is EXPENSE/INCOME but activeState is not ExpenseIncomeState!")
                                    _errorMessageEvent.postValue("Внутренняя ошибка состояния UI.")
                                    return@launch
                                }
                            }
                            TransactionType.REMITTANCE -> {
                                if (currentActiveState is ActiveTransactionState.RemittanceState) {
                                    val transfer = Transfer(
                                        id = transactionId, // Сохраняем ИСХОДНЫЙ ID
                                        type = TransactionType.REMITTANCE, // ТОТ ЖЕ ТИП
                                        balance = validAmount,
                                        moneyAccFrom = currentActiveState.selectedAccountFrom!!,
                                        moneyAccTo = currentActiveState.selectedAccountTo!!,
                                        _note = Title(transactionNote),
                                        date = transactionDate,
                                        time = transactionTime
                                    )
                                    updateTransferUseCase(transfer) // Обновляем
                                    Log.d("ViewModel", "Updating Transfer: $transfer")
                                } else {
                                    Log.e("ViewModel", "Current type is REMITTANCE but activeState is not RemittanceState!")
                                    _errorMessageEvent.postValue("Внутренняя ошибка состояния UI.")
                                    return@launch
                                }
                            }
                        }
                        Log.i("ViewModel", "Transaction updated successfully!")
                        _transactionSavedEvent.postValue(true)
                    }

                } else { // Режим создания
                    Log.d("ViewModel", "Attempting to CREATE transaction of type: $currentActiveType")
                    when (currentActiveState) {
                        is ActiveTransactionState.ExpenseIncomeState -> {
                            val payment = Payment(
                                type = currentActiveType,
                                balance = validAmount,
                                moneyAccount = currentActiveState.selectedPaymentAccount!!,
                                category = currentActiveState.selectedCategory!!,
                                _note = Title(transactionNote),
                                date = transactionDate,
                                time = LocalTime.now()
                            )
                            createPaymentUseCase(payment)
                            Log.d("ViewModel", "Inserting Payment: $payment")
                        }
                        is ActiveTransactionState.RemittanceState -> {
                            val transfer = Transfer(
                                type = TransactionType.REMITTANCE,
                                balance = validAmount,
                                moneyAccFrom = currentActiveState.selectedAccountFrom!!,
                                moneyAccTo = currentActiveState.selectedAccountTo!!,
                                _note = Title(transactionNote),
                                date = transactionDate,
                                time = LocalTime.now()
                            )
                            createTransferUseCase(transfer)
                            Log.d("ViewModel", "Inserting Transfer: $transfer")
                        }
                        ActiveTransactionState.InitialState -> { /* Should be impossible due to validation */ }
                    }
                    Log.i("ViewModel", "Transaction created successfully!")
                    _transactionSavedEvent.postValue(true)
                    resetAllStateForNewTransaction()
                }

            } catch (e: Exception) {
                Log.e("ViewModel", "Error saving/updating transaction", e)
                _errorMessageEvent.postValue("Ошибка сохранения: ${e.localizedMessage}")
            }
        }
    }

    fun selectCategory(category: Category) {
        val currentType = _currentTransactionType.value
        val expectedCategoryType = when(currentType) {
            TransactionType.EXPENSE -> CategoryType.EXPENSE
            TransactionType.INCOME -> CategoryType.INCOME
            else -> null
        }

        val currentState = _activeTransactionState.value

        if (currentType != TransactionType.REMITTANCE && category.categoryType == expectedCategoryType && currentState != null) {
            Log.d("ViewModel", "selectCategory: ${category.title}")
            if (currentState is ActiveTransactionState.ExpenseIncomeState) {
                _activeTransactionState.value = currentState.copy(selectedCategory = category)
            } else {
                Log.w("ViewModel", "selectCategory called but active state was not ExpenseIncomeState. Resetting state.")
                _activeTransactionState.value = ActiveTransactionState.ExpenseIncomeState(selectedCategory = category)
            }
        } else {
            Log.w("ViewModel", "selectCategory: ignored. Current type: $currentType, Category type: ${category.categoryType}, Expected: $expectedCategoryType, State: $currentState")
        }
    }

    fun selectPaymentAccount(account: MoneyAccount) {
        val currentType = _currentTransactionType.value
        if (currentType == TransactionType.EXPENSE || currentType == TransactionType.INCOME) {
            Log.d("ViewModel", "selectPaymentAccount: ${account.title}")
            val currentState = _activeTransactionState.value
            if (currentState is ActiveTransactionState.ExpenseIncomeState) {
                _activeTransactionState.value = currentState.copy(selectedPaymentAccount = account)
            } else {
                Log.w("ViewModel", "selectPaymentAccount called but active state was not ExpenseIncomeState. Resetting state.")
                _activeTransactionState.value = ActiveTransactionState.ExpenseIncomeState(selectedPaymentAccount = account)
            }
        } else {
            Log.w("ViewModel", "selectPaymentAccount: ignored. Current type is $currentType")
        }
    }

    fun selectAccountFrom(account: MoneyAccount) {
        val currentType = _currentTransactionType.value
        if (currentType == TransactionType.REMITTANCE) {
            Log.d("ViewModel", "selectAccountFrom: ${account.title}")
            val currentState = _activeTransactionState.value as? ActiveTransactionState.RemittanceState

            val currentToAccount = currentState?.selectedAccountTo

            if (account.id == currentToAccount?.id) {
                _errorMessageEvent.value = "Счета должны отличаться"
                return
            }

            _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                selectedAccountFrom = account,
                selectedAccountTo = currentToAccount
            )
            Log.d("ViewModel", "selectAccountFrom: set state to ${_activeTransactionState.value}")
        } else {
            Log.w("ViewModel", "selectAccountFrom: ignored. Current type is $currentType")
        }
    }

    fun selectAccountTo(account: MoneyAccount) {
        val currentType = _currentTransactionType.value
        if (currentType == TransactionType.REMITTANCE) {
            Log.d("ViewModel", "selectAccountTo: ${account.title}")
            val currentState = _activeTransactionState.value as? ActiveTransactionState.RemittanceState

            val currentFromAccount = currentState?.selectedAccountFrom

            if (account.id == currentFromAccount?.id) {
                _errorMessageEvent.value = "Счета должны отличаться"
                return
            }

            _activeTransactionState.value = ActiveTransactionState.RemittanceState(
                selectedAccountFrom = currentFromAccount,
                selectedAccountTo = account
            )
            Log.d("ViewModel", "selectAccountTo: set state to ${_activeTransactionState.value}")
        } else {
            Log.w("ViewModel", "selectAccountTo: ignored. Current type is $currentType")
        }
    }
}