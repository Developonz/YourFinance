package com.example.yourfinance.presentation.ui.fragment.manager.category_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.usecase.categories.general.CreateAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.general.DeleteAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.category.FetchCategoriesUseCase
import com.example.yourfinance.domain.usecase.categories.category.LoadCategoryByIdUseCase
import com.example.yourfinance.domain.usecase.categories.general.UpdateAnyCategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagerViewModel @Inject constructor(
    fetchFullCategoriesUseCase: FetchCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteAnyCategoryUseCase,
    private val loadCategoryByIdUseCase: LoadCategoryByIdUseCase,
//    private val loadFullCategoryByIdUseCase: LoadFullCategoryByIdUseCase,
    private val updateCategoryUseCase: UpdateAnyCategoryUseCase,
    private val createCategoryUseCase: CreateAnyCategoryUseCase,
) : ViewModel() {

    val allCategories: LiveData<List<Category>> = fetchFullCategoriesUseCase()

    private val _currentTransactionType = MutableLiveData(TransactionType.EXPENSE)
    private val _selectedCategoryType = MutableLiveData(CategoryType.EXPENSE)
    val selectedCategoryType: LiveData<CategoryType> get() = _selectedCategoryType

    val filteredCategories: LiveData<List<Category>> = MediatorLiveData<List<Category>>().apply {
        fun update() {
            val currentType = when (_currentTransactionType.value) {
                TransactionType.EXPENSE -> CategoryType.EXPENSE
                TransactionType.INCOME -> CategoryType.INCOME
                else -> null
            }
            value = if (currentType != null) {
                allCategories.value?.filter { it.categoryType == currentType } ?: emptyList()
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

    fun deleteCategory(categoryToDelete: Category) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteCategoryUseCase(categoryToDelete.id)
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
            createCategoryUseCase(category)
        }
    }

}