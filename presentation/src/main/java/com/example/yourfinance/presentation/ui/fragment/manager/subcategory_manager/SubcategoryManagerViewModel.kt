package com.example.yourfinance.presentation.ui.fragment.manager.subcategory_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.usecase.categories.category.FetchCategoriesUseCase
import com.example.yourfinance.domain.usecase.categories.general.CreateAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.general.DeleteAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.general.UpdateAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.subcategory.LoadSubcategoryByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubcategoryManagerViewModel @Inject constructor(
    fetchFullCategoriesUseCase: FetchCategoriesUseCase,
    private val loadSubcategoryByIdUseCase: LoadSubcategoryByIdUseCase,
    private val updateICategoryUseCase: UpdateAnyCategoryUseCase,
    private val createSubcategoryUseCase: CreateAnyCategoryUseCase,
    private val deleteSubcategoryUseCase: DeleteAnyCategoryUseCase
) : ViewModel() {

    val allCategories: LiveData<List<Category>> = fetchFullCategoriesUseCase()


    suspend fun loadSubcategoryById(subcategoryId: Long): com.example.yourfinance.domain.model.entity.category.Subcategory? = loadSubcategoryByIdUseCase(subcategoryId)
    fun updateSubcategory(subcategory: com.example.yourfinance.domain.model.entity.category.Subcategory) {
        viewModelScope.launch {
            updateICategoryUseCase(subcategory)
        }
    }

    fun createSubcategory(subcategory: com.example.yourfinance.domain.model.entity.category.Subcategory) {
        viewModelScope.launch {
            createSubcategoryUseCase(subcategory)
        }
    }

    fun deleteSubcategory(subcategory: com.example.yourfinance.domain.model.entity.category.Subcategory) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteSubcategoryUseCase(subcategory.id)
        }
    }


    suspend fun getParentCategoryColor(parentId: Long): String? {
        if (parentId == 1L) return "#FF0000" // Красный для теста
        if (parentId == 2L) return "#00FF00" // Зеленый для теста
        return "#0000FF"
    }
}