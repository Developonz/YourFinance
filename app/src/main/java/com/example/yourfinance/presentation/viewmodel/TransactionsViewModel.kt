package com.example.yourfinance.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.BudgetRepository
import com.example.yourfinance.domain.repository.CategoryRepository
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import com.example.yourfinance.domain.repository.TransactionRepository
// import com.example.yourfinance.util.SingleLiveEvent // Используй SingleLiveEvent или аналог для событий
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: MoneyAccountRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    // --- Основные LiveData ---
    val transactionsList: LiveData<List<Transaction>> = transactionRepository.getAllTransactions()
    val accountsList: LiveData<List<MoneyAccount>> = accountRepository.getAllAccounts()
    val budgetsList: LiveData<List<Budget>> = budgetRepository.getAllBudgets()
    // Предоставляем ВСЕ категории, фрагменты сами отфильтруют
    val allCategories: LiveData<List<FullCategory>> = categoryRepository.getAllCategoriesWithSubcategories()

    // --- Состояние для экрана добавления транзакции ---
    private val _currentTransactionType = MutableLiveData(TransactionType.EXPENSE) // Активный тип (управляется ViewPager)
    val currentTransactionType: LiveData<TransactionType> = _currentTransactionType

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    private val _selectedPaymentAccount = MutableLiveData<MoneyAccount?>() // Счет для Расхода/Дохода
    val selectedPaymentAccount: LiveData<MoneyAccount?> = _selectedPaymentAccount

    private val _selectedAccountFrom = MutableLiveData<MoneyAccount?>() // Счет "Откуда" для Перевода
    val selectedAccountFrom: LiveData<MoneyAccount?> = _selectedAccountFrom

    private val _selectedAccountTo = MutableLiveData<MoneyAccount?>() // Счет "Куда" для Перевода
    val selectedAccountTo: LiveData<MoneyAccount?> = _selectedAccountTo

    private val _amountString = MutableLiveData("0") // Сумма как строка
    val amountString: LiveData<String> = _amountString

    private val _note = MutableLiveData("") // Примечание
    val note: LiveData<String> = _note

    private val _date = MutableLiveData(LocalDate.now()) // Дата
    val date: LiveData<LocalDate> = _date

    // --- LiveData для управления видимостью секции ввода (зависит от выбора) ---
    val showInputSection: LiveData<Boolean> = MediatorLiveData<Boolean>().apply {
        value = false // Начальное значение
        fun update() {
            val type = _currentTransactionType.value
            val categorySelected = _selectedCategory.value != null
            val accountFromSelected = _selectedAccountFrom.value != null
            val accountToSelected = _selectedAccountTo.value != null

            // Показываем ввод: для Р/Д - если выбрана категория, для Перевода - если оба счета
            val shouldShow = when (type) {
                TransactionType.EXPENSE, TransactionType.INCOME -> categorySelected
                TransactionType.REMITTANCE -> accountFromSelected && accountToSelected
                null -> false
            }
            if (value != shouldShow) {
                value = shouldShow
            }
            Log.d("ViewModel", "showInputSection.update() called. Active Type: $type, CatSelected: $categorySelected, AccFrom: $accountFromSelected, AccTo: $accountToSelected. Result: $value")
        }
        // Источники, влияющие на показ клавиатуры
        addSource(_currentTransactionType) { update() } // Тип важен для логики when
        addSource(_selectedCategory) { update() }       // Выбор категории важен для Р/Д
        addSource(_selectedAccountFrom) { update() }    // Выбор счета "Откуда" важен для Перевода
        addSource(_selectedAccountTo) { update() }      // Выбор счета "Куда" важен для Перевода
        // _selectedPaymentAccount НЕ влияет на показ секции ввода
    }

    // --- LiveData для событий ---
    private val _transactionSavedEvent = MutableLiveData<Boolean?>() // Используй SingleLiveEvent! null - нет события
    val transactionSavedEvent: LiveData<Boolean?> = _transactionSavedEvent

    private val _errorMessageEvent = MutableLiveData<String?>() // Используй SingleLiveEvent! null - нет события
    val errorMessageEvent: LiveData<String?> = _errorMessageEvent

    // --- Фильтрованные категории ---
    val filteredCategories: LiveData<List<FullCategory>> = MediatorLiveData<List<FullCategory>>().apply {
        fun update() {
            val currentType = when (_currentTransactionType.value) {
                TransactionType.EXPENSE -> CategoryType.EXPENSE
                TransactionType.INCOME -> CategoryType.INCOME
                else -> null
            }
            value = if (currentType != null) {
                allCategories.value?.filter { it.category.categoryType == currentType } ?: emptyList()
            } else {
                emptyList()
            }
        }
        addSource(allCategories) { update() }
        addSource(_currentTransactionType) { update() }
    }

    // --- Функции для UI (вызываются из Fragment) ---
    // Устанавливается хост-компонентом при смене страницы ViewPager
    fun setTransactionType(type: TransactionType) {
        if (_currentTransactionType.value != type) {
            Log.d("ViewModel", "setTransactionType changing from ${_currentTransactionType.value} to $type")
            _currentTransactionType.value = type
            clearInputState() // Сбрасываем состояние при смене АКТИВНОГО типа
        } else {
            Log.d("ViewModel", "setTransactionType called with the same type: $type")
        }
    }

    // Вызывается при клике на категорию в активном фрагменте
    fun selectCategory(category: Category) {
        Log.d("ViewModel", "selectCategory: ${category.title}")
        _selectedCategory.value = category
        // Если ранее был выбран счет для Р/Д, сбрасываем его? Реши сам.
        // _selectedPaymentAccount.value = null
        // showInputSection обновится автоматически
    }

    // Вызывается при выборе счета в диалоге для Р/Д
    fun selectPaymentAccount(account: MoneyAccount) {
        Log.d("ViewModel", "selectPaymentAccount: ${account.title}")
        _selectedPaymentAccount.value = account
    }

    // Вызывается при выборе счета "Откуда" для Перевода
    fun selectAccountFrom(account: MoneyAccount) {
        Log.d("ViewModel", "selectAccountFrom: ${account.title}")
        if (account.id == _selectedAccountTo.value?.id) {
            _errorMessageEvent.value = "Счета должны отличаться"
            return
        }
        _selectedAccountFrom.value = account
    }

    // Вызывается при выборе счета "Куда" для Перевода
    fun selectAccountTo(account: MoneyAccount) {
        Log.d("ViewModel", "selectAccountTo: ${account.title}")
        if (account.id == _selectedAccountFrom.value?.id) {
            _errorMessageEvent.value = "Счета должны отличаться"
            return
        }
        _selectedAccountTo.value = account
    }

    // Обработка нажатий кнопок клавиатуры
    fun handleKeypadInput(key: String) {
        val currentAmount = _amountString.value ?: "0"
        var newAmount = currentAmount

        when (key) {
            "DEL" -> {
                newAmount = if (currentAmount.length > 1) currentAmount.dropLast(1) else "0"
            }
            "." -> {
                if (!currentAmount.contains('.')) newAmount = "$currentAmount."
            }
            else -> { // Цифры
                if (currentAmount == "0" && key != ".") {
                    newAmount = key
                } else if (currentAmount.contains('.') && currentAmount.substringAfter('.').length >= 2) {
                    // Ограничение 2 знака после точки - ничего не делаем
                } else if (currentAmount.length < 15) { // Ограничение общей длины (опционально)
                    newAmount = currentAmount + key
                }
            }
        }

        if (_amountString.value != newAmount) {
            Log.d("ViewModel", "handleKeypadInput: '$key', amount changing from '$currentAmount' to '$newAmount'")
            _amountString.value = newAmount
        }
    }

    // Установка примечания
    fun setNote(text: String) {
        if (_note.value != text) {
            _note.value = text
        }
    }

    // Установка даты
    fun setDate(newDate: LocalDate) {
        if (_date.value != newDate) {
            Log.d("ViewModel", "setDate: $newDate")
            _date.value = newDate
        }
    }

    // Сброс состояния ввода (вызывается при смене типа транзакции)
    fun clearInputState() {
        _selectedCategory.value = null
        _selectedPaymentAccount.value = null
        _selectedAccountFrom.value = null
        _selectedAccountTo.value = null
        _amountString.value = "0"
        _note.value = ""
        // Дату можно не сбрасывать, если пользователь хочет сохранить ее между типами
        // _date.value = LocalDate.now()
        Log.i("ViewModel", "Input state cleared")
        // showInputSection обновится автоматически
    }

    // Сброс события сохранения (вызывается из Fragment после обработки)
    fun resetSavedEvent() {
        if (_transactionSavedEvent.value == true) {
            _transactionSavedEvent.value = null // Сбрасываем в null
        }
    }

    // Сброс события ошибки (вызывается из Fragment после обработки)
    fun resetErrorEvent() {
        if (_errorMessageEvent.value != null) {
            _errorMessageEvent.value = null
        }
    }

    // --- Проверка валидности перед сохранением ---
    private fun isSelectionValid(): Boolean {
        val type = _currentTransactionType.value
        val amount = _amountString.value?.toDoubleOrNull() ?: 0.0
        var errorMsg: String? = null

        if (amount <= 0) {
            errorMsg = "Введите сумму"
        } else {
            when (type) {
                TransactionType.INCOME, TransactionType.EXPENSE -> {
                    if (_selectedCategory.value == null) errorMsg = "Выберите категорию"
                    else if (_selectedPaymentAccount.value == null) errorMsg = "Выберите счет"
                }
                TransactionType.REMITTANCE -> {
                    if (_selectedAccountFrom.value == null) errorMsg = "Выберите счет списания"
                    else if (_selectedAccountTo.value == null) errorMsg = "Выберите счет зачисления"
                }
                null -> errorMsg = "Не выбран тип транзакции" // Маловероятно
            }
        }

        if (errorMsg != null) {
            Log.w("ViewModel", "Validation failed: $errorMsg")
            _errorMessageEvent.value = errorMsg // Устанавливаем ошибку для отображения
            return false
        }
        return true
    }

    // --- Функция сохранения ---
    fun saveTransaction() {
        Log.d("ViewModel", "saveTransaction called")
        if (!isSelectionValid()) {
            return // Ошибка уже установлена в isSelectionValid()
        }

        // Здесь мы уверены, что все поля non-null благодаря isSelectionValid()
        val amount = _amountString.value!!.toDouble()
        val transactionDate = _date.value!!
        val transactionTime = LocalTime.now()
        val transactionNote = _note.value ?: ""
        val activeType = _currentTransactionType.value!!

        viewModelScope.launch {
            try {
                Log.d("ViewModel", "Attempting to save transaction of type: $activeType")
                when (activeType) {
                    TransactionType.EXPENSE, TransactionType.INCOME -> {
                        val payment = Payment(
                            type = activeType,
                            balance = amount,
                            moneyAccount = _selectedPaymentAccount.value!!,
                            category = _selectedCategory.value!!,
                            _note = transactionNote,
                            date = transactionDate,
                            time = transactionTime
                        )
                        Log.d("ViewModel", "Inserting Payment: $payment")
                        transactionRepository.insertPayment(payment)
                        _transactionSavedEvent.postValue(true)
                        clearInputState() // Очищаем после успешного сохранения
                    }
                    TransactionType.REMITTANCE -> {
                        val transfer = Transfer(
                            type = TransactionType.REMITTANCE, // Всегда REMITTANCE
                            balance = amount,
                            moneyAccFrom = _selectedAccountFrom.value!!,
                            moneyAccTo = _selectedAccountTo.value!!,
                            _note = transactionNote,
                            date = transactionDate,
                            time = transactionTime
                        )
                        Log.d("ViewModel", "Inserting Transfer: $transfer")
                        transactionRepository.insertTransfer(transfer)
                        _transactionSavedEvent.postValue(true)
                        clearInputState() // Очищаем после успешного сохранения
                    }
                }
                Log.i("ViewModel", "Transaction saved successfully!")
            } catch (e: Exception) {
                Log.e("ViewModel", "Error saving transaction", e)
                _errorMessageEvent.postValue("Ошибка сохранения: ${e.localizedMessage}")
            }
        }
    }

    // --- Старые методы (оставляем, если нужны в других местах) ---
    val combinedList = MediatorLiveData<Pair<List<Transaction>, List<MoneyAccount>>>().apply {
        addSource(transactionsList) { transactions -> value = transactions to (accountsList.value ?: emptyList()) }
        addSource(accountsList) { accounts -> value = (transactionsList.value ?: emptyList()) to accounts }
    }
    fun deleteAccount(acc: MoneyAccount) { viewModelScope.launch { accountRepository.deleteAccount(acc) } }
    fun createAccount(acc: MoneyAccount) { viewModelScope.launch { accountRepository.insertAccount(acc) } }
    fun updateAccount(account: MoneyAccount) { viewModelScope.launch { accountRepository.updateAccount(account) } }
    suspend fun getAccountById(id: Long): MoneyAccount? = accountRepository.getAccountById(id)
    fun deleteCategory(categoryToDelete: FullCategory) { viewModelScope.launch(Dispatchers.IO) { categoryRepository.deleteCategory(categoryToDelete.category) } }
    suspend fun loadCategoryById(categoryId: Long): Category? = categoryRepository.loadCategoryById(categoryId)
    suspend fun loadFullCategoryById(categoryId: Long): FullCategory? = categoryRepository.loadFullCategoryById(categoryId)
    fun updateCategory(category: Category) { viewModelScope.launch { categoryRepository.updateCategory(category) } }
    fun createCategory(category: Category) { viewModelScope.launch { categoryRepository.insertCategory(FullCategory(category)) } }
    suspend fun loadSubcategoryById(subcategoryId: Long): Subcategory? = categoryRepository.loadSubcategory(subcategoryId)
    fun updateSubcategory(subcategory: Subcategory) { viewModelScope.launch { categoryRepository.updateSubcategory(subcategory) } }
    fun createSubcategory(subcategory: Subcategory) { viewModelScope.launch { categoryRepository.insertSubcategory(subcategory) } }
    fun deleteSabcategory(subcategory: Subcategory) { viewModelScope.launch(Dispatchers.IO) { categoryRepository.deleteSubcategory(subcategory) } }

    // Управление CategoryType (если используется в других фрагментах)
    private val _selectedCategoryType = MutableLiveData<CategoryType>(CategoryType.EXPENSE)
    val selectedCategoryType: LiveData<CategoryType> get() = _selectedCategoryType
    fun setSelectedCategoryType(selectedType: CategoryType) { if (_selectedCategoryType.value != selectedType) { _selectedCategoryType.value = selectedType } }
}