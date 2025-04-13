package com.example.yourfinance.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.MoneyAccount
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
import javax.inject.Inject

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val transactionRepository: TransactionRepository,
    private val accountRepository: MoneyAccountRepository,
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {
    val transactionsList: LiveData<List<Transaction>> = transactionRepository.getAllTransactions()

    val accountsList: LiveData<List<MoneyAccount>>  = accountRepository.getAllAccounts()

    val budgetsList: LiveData<List<Budget>> = budgetRepository.getAllBudgets()

    val allCategories: LiveData<List<FullCategory>> = categoryRepository.getAllCategoriesWithSubcategories()

    private val _selectedCategoryType = MutableLiveData<CategoryType>(CategoryType.EXPENSE)




    val selectedCategoryType: LiveData<CategoryType>
        get() = _selectedCategoryType


    val filteredCategories: LiveData<List<FullCategory>> = MediatorLiveData<List<FullCategory>>().apply {

        addSource(allCategories) { categories ->
            value = categories?.filter { it.category.categoryType == _selectedCategoryType.value } ?: emptyList()
        }

        addSource(_selectedCategoryType) { type ->
            value = allCategories.value?.filter { it.category.categoryType == type } ?: emptyList()
        }
    }



    val combinedList = MediatorLiveData<Pair<List<Transaction>, List<MoneyAccount>>>().apply {
        addSource(transactionsList) { transactions ->
            value = transactions to (accountsList.value ?: emptyList())
        }
        addSource(accountsList) { accounts ->
            value = (transactionsList.value ?: emptyList()) to accounts
        }
    }


    fun deleteAccount(acc: MoneyAccount) {
        viewModelScope.launch {
            accountRepository.deleteAccount(acc)
        }
    }

    fun createAccount(acc: MoneyAccount) {
        viewModelScope.launch {
            accountRepository.insertAccount(acc)
        }
    }

    fun updateAccount(account: MoneyAccount) {
        viewModelScope.launch {
            accountRepository.updateAccount(account)
        }
    }

    suspend fun getAccountById(id: Long): MoneyAccount? {
        return accountRepository.getAccountById(id)
    }

    fun setSelectedCategoryType(selectedType: CategoryType) {
        if (_selectedCategoryType.value != selectedType) {
            _selectedCategoryType.value = selectedType
        }
    }

    fun deleteCategory(categoryToDelete: FullCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.deleteCategory(categoryToDelete.category)
        }
    }

    suspend fun loadCategoryById(categoryId: Long) : Category?{

        return categoryRepository.loadCategoryById(categoryId)
    }

    suspend fun loadFullCategoryById(categoryId: Long) : FullCategory?{
        return categoryRepository.loadFullCategoryById(categoryId)
    }

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.updateCategory(category)
        }
    }

    fun createCategory(category: Category) {
        viewModelScope.launch {
            categoryRepository.insertCategory(FullCategory(category))
        }
    }

    suspend fun loadSubcategoryById(subcategoryId: Long): Subcategory? {
        return categoryRepository.loadSubcategory(subcategoryId)
    }

    fun updateSubcategory(subcategory: Subcategory) {
        viewModelScope.launch {
            categoryRepository.updateSubcategory(subcategory)
        }
    }

    fun createSubcategory(subcategory: Subcategory) {
        viewModelScope.launch {
            categoryRepository.insertSubcategory(subcategory)
        }
    }

    fun deleteSabcategory(subcategory: Subcategory) {
        viewModelScope.launch(Dispatchers.IO) {
            categoryRepository.deleteSubcategory(subcategory)
        }
    }
}

