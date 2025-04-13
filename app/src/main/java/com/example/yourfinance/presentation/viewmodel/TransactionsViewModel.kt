package com.example.yourfinance.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.TransactionType // Убедитесь, что импорт есть
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Payment // Убедитесь, что импорт есть
import com.example.yourfinance.domain.model.entity.Transfer // Убедитесь, что импорт есть
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.BudgetRepository
import com.example.yourfinance.domain.repository.CategoryRepository
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import com.example.yourfinance.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime // Добавим для времени
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: MoneyAccountRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    val transactionsList: LiveData<List<Transaction>> = transactionRepository.getAllTransactions()
    val accountsList: LiveData<List<MoneyAccount>> = accountRepository.getAllAccounts()
    val budgetsList: LiveData<List<Budget>> = budgetRepository.getAllBudgets()
    val allCategories: LiveData<List<FullCategory>> = categoryRepository.getAllCategoriesWithSubcategories()

    // --- Состояние для экрана добавления транзакции ---

    private val _currentTransactionType = MutableLiveData(TransactionType.EXPENSE) // По умолчанию Расход
    val currentTransactionType: LiveData<TransactionType> = _currentTransactionType

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    private val _selectedAccountFrom = MutableLiveData<MoneyAccount?>()
    val selectedAccountFrom: LiveData<MoneyAccount?> = _selectedAccountFrom

    private val _selectedAccountTo = MutableLiveData<MoneyAccount?>()
    val selectedAccountTo: LiveData<MoneyAccount?> = _selectedAccountTo

    private val _amountString = MutableLiveData("0") // Сумма как строка для удобного ввода
    val amountString: LiveData<String> = _amountString

    private val _note = MutableLiveData("")
    val note: LiveData<String> = _note

    private val _date = MutableLiveData(LocalDate.now())
    val date: LiveData<LocalDate> = _date

    // --- LiveData для управления видимостью UI ---
    private val _showInputSection = MutableLiveData(false)
    val showInputSection: LiveData<Boolean> = _showInputSection

    // --- LiveData для навигации после сохранения ---
    private val _transactionSavedEvent = MutableLiveData<Boolean>()
    val transactionSavedEvent: LiveData<Boolean> = _transactionSavedEvent


    private val _selectedCategoryType = MutableLiveData<CategoryType>(CategoryType.EXPENSE)




    val selectedCategoryType: LiveData<CategoryType>
        get() = _selectedCategoryType


    fun setSelectedCategoryType(selectedType: CategoryType) {
        if (_selectedCategoryType.value != selectedType) {
            _selectedCategoryType.value = selectedType
        }
    }

    // ------------- //

    // --- Фильтрованные категории ---
    val filteredCategories: LiveData<List<FullCategory>> = MediatorLiveData<List<FullCategory>>().apply {
        // Функция для обновления фильтрованного списка
        fun update() {
            val currentType = when (_currentTransactionType.value) {
                TransactionType.EXPENSE -> CategoryType.EXPENSE
                TransactionType.INCOME -> CategoryType.INCOME
                else -> null // Для переводов категории не нужны в этом контексте
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
    // ------------- //


    // --- Функции для UI ---

    fun setTransactionType(type: TransactionType) {
        if (_currentTransactionType.value != type) {
            _currentTransactionType.value = type
            clearInputState() // Сбрасываем состояние при смене типа
        }
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        _showInputSection.value = true // Показываем секцию ввода
    }

    fun selectAccountFrom(account: MoneyAccount) {
        _selectedAccountFrom.value = account
        checkTransferReady()
    }

    fun selectAccountTo(account: MoneyAccount) {
        _selectedAccountTo.value = account
        checkTransferReady()
    }

    private fun checkTransferReady() {
        if (_selectedAccountFrom.value != null && _selectedAccountTo.value != null) {
            _showInputSection.value = true // Показываем секцию ввода для перевода
        }
    }

    fun handleKeypadInput(key: String) {
        val currentAmount = _amountString.value ?: "0"
        when (key) {
            "DEL" -> {
                _amountString.value = if (currentAmount.length > 1) {
                    currentAmount.dropLast(1)
                } else {
                    "0"
                }
            }
            "." -> {
                if (!currentAmount.contains('.')) {
                    _amountString.value = "$currentAmount."
                }
            }
            else -> { // Цифры
                if (currentAmount == "0") {
                    _amountString.value = key
                } else {
                    // Ограничение на длину или количество знаков после запятой, если нужно
                    _amountString.value = currentAmount + key
                }
            }
        }
    }

    fun setNote(text: String) {
        _note.value = text
    }

    fun setDate(newDate: LocalDate) {
        _date.value = newDate
    }

    fun clearInputState() {
        _selectedCategory.value = null
        _selectedAccountFrom.value = null
        _selectedAccountTo.value = null
        _amountString.value = "0"
        _note.value = ""
        _date.value = LocalDate.now()
        _showInputSection.value = false
    }

    fun resetSavedEvent() {
        _transactionSavedEvent.value = false
    }

    // --- Функции для сохранения ---
    fun saveTransaction() {
        val amount = _amountString.value?.toDoubleOrNull() ?: 0.0
        val transactionDate = _date.value ?: LocalDate.now()
        val transactionTime = LocalTime.now() // Текущее время
        val transactionNote = _note.value ?: ""

        if (amount <= 0) {
            Log.e("ViewModel", "Amount must be positive")
            // TODO: Показать ошибку пользователю (например, через LiveData)
            return
        }

        viewModelScope.launch {
            try {
                when (_currentTransactionType.value) {
                    TransactionType.EXPENSE, TransactionType.INCOME -> {
                        val category = _selectedCategory.value
                        // Нужен счет по умолчанию или выбор счета
                        // Пока возьмем первый попавшийся счет для примера
                        // В РЕАЛЬНОСТИ: нужно дать пользователю выбрать счет!
                        val account = accountsList.value?.firstOrNull()
                        if (category != null && account != null) {
                            val payment = Payment(
                                type = _currentTransactionType.value!!,
                                balance = amount,
                                moneyAccount = account, // ЗАМЕНИТЬ НА ВЫБРАННЫЙ СЧЕТ
                                category = category,
                                _note = transactionNote,
                                date = transactionDate,
                                time = transactionTime
                            )
                            transactionRepository.insertPayment(payment)
                            _transactionSavedEvent.postValue(true) // Уведомляем об успехе
                        } else {
                            Log.e("ViewModel", "Category or Account not selected for Payment")
                            // TODO: Показать ошибку
                        }
                    }
                    TransactionType.REMITTANCE -> {
                        val accFrom = _selectedAccountFrom.value
                        val accTo = _selectedAccountTo.value
                        if (accFrom != null && accTo != null) {
                            if (accFrom.id == accTo.id) {
                                Log.e("ViewModel", "Cannot transfer to the same account")
                                // TODO: Показать ошибку
                                return@launch
                            }
                            val transfer = Transfer(
                                type = TransactionType.REMITTANCE,
                                balance = amount,
                                moneyAccFrom = accFrom,
                                moneyAccTo = accTo,
                                _note = transactionNote,
                                date = transactionDate,
                                time = transactionTime
                            )
                            transactionRepository.insertTransfer(transfer)
                            _transactionSavedEvent.postValue(true) // Уведомляем об успехе
                        } else {
                            Log.e("ViewModel", "Accounts not selected for Transfer")
                            // TODO: Показать ошибку
                        }
                    }
                    null -> {
                        Log.e("ViewModel", "Transaction type is null")
                        // TODO: Показать ошибку
                    }
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Error saving transaction", e)
                // TODO: Показать ошибку пользователю
                _transactionSavedEvent.postValue(false) // Уведомляем о неудаче (опционально)
            } finally {
                // Очищаем состояние после попытки сохранения (даже если была ошибка)
                // или только при успехе, в зависимости от UX
                // clearInputState() // Раскомментировать, если нужно очищать всегда
            }
        }
    }

    // --- Старые функции (оставляем или удаляем по необходимости) ---
    val combinedList = MediatorLiveData<Pair<List<Transaction>, List<MoneyAccount>>>().apply {
        addSource(transactionsList) { transactions ->
            value = transactions to (accountsList.value ?: emptyList())
        }
        addSource(accountsList) { accounts ->
            value = (transactionsList.value ?: emptyList()) to accounts
        }
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
}