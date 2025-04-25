package com.example.yourfinance.presentation.ui.fragment.manager.category_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.usecase.category.CreateCategoryUseCase
import com.example.yourfinance.domain.usecase.category.DeleteCategoryUseCase
import com.example.yourfinance.domain.usecase.category.FetchFullCategoriesUseCase
import com.example.yourfinance.domain.usecase.category.LoadCategoryByIdUseCase
import com.example.yourfinance.domain.usecase.category.UpdateCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagerViewModel @Inject constructor(
    fetchFullCategoriesUseCase: FetchFullCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    private val loadCategoryByIdUseCase: LoadCategoryByIdUseCase,
//    private val loadFullCategoryByIdUseCase: LoadFullCategoryByIdUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
) : ViewModel() {

    val allCategories: LiveData<List<FullCategory>> = fetchFullCategoriesUseCase()

    private val _currentTransactionType = MutableLiveData(TransactionType.EXPENSE)
    private val _selectedCategoryType = MutableLiveData(CategoryType.EXPENSE)
    val selectedCategoryType: LiveData<CategoryType> get() = _selectedCategoryType

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



    fun setSelectedCategoryType(selectedType: CategoryType) {
        if (_selectedCategoryType.value != selectedType) {
            _selectedCategoryType.value = selectedType
        }
    }

    fun deleteCategory(categoryToDelete: FullCategory) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteCategoryUseCase(categoryToDelete.category)
        }

    }
    suspend fun loadCategoryById(categoryId: Long): Category? = loadCategoryByIdUseCase(categoryId)

//    suspend fun loadFullCategoryById(categoryId: Long): FullCategory? = loadFullCategoryByIdUseCase(categoryId)

    fun updateCategory(category: Category) {
        viewModelScope.launch {
            updateCategoryUseCase(category)
        }
    }

    fun createCategory(category: Category) {
        viewModelScope.launch {
            createCategoryUseCase(FullCategory(category))
        }
    }

}