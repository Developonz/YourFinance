package com.example.yourfinance.presentation.ui.fragment.manager.category_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.CategoryType
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



import androidx.lifecycle.map

@HiltViewModel
class CategoryManagerViewModel @Inject constructor(
    fetchCategoriesUseCase: FetchCategoriesUseCase,
    private val deleteCategoryUseCase: DeleteAnyCategoryUseCase,
    private val loadCategoryByIdUseCase: LoadCategoryByIdUseCase,
    private val updateCategoryUseCase: UpdateAnyCategoryUseCase,
    private val createCategoryUseCase: CreateAnyCategoryUseCase,
) : ViewModel() {

    val allCategories: LiveData<List<Category>> = fetchCategoriesUseCase()

    // LiveData для текущего выбранного типа (обновляется по смене вкладки)
    private val _selectedCategoryType = MutableLiveData(CategoryType.EXPENSE)
    val selectedCategoryType: LiveData<CategoryType> get() = _selectedCategoryType

    // LiveData для списка категорий Расходов
    val expenseCategories: LiveData<List<Category>> = allCategories.map { list ->
        list.filter { it.categoryType == CategoryType.EXPENSE }
    }

    // LiveData для списка категорий Доходов
    val incomeCategories: LiveData<List<Category>> = allCategories.map { list ->
        list.filter { it.categoryType == CategoryType.INCOME }
    }


    fun setSelectedCategoryType(selectedType: CategoryType) {
        if (_selectedCategoryType.value != selectedType) {
            _selectedCategoryType.value = selectedType
        }
    }

    fun deleteCategory(categoryId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteCategoryUseCase(categoryId)
        }
    }

    suspend fun loadCategoryById(categoryId: Long): Category? = loadCategoryByIdUseCase(categoryId)

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