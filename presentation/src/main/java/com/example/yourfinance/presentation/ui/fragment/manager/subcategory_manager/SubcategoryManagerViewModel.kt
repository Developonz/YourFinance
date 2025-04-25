package com.example.yourfinance.presentation.ui.fragment.manager.subcategory_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.usecase.category.FetchFullCategoriesUseCase
import com.example.yourfinance.domain.usecase.subcategory.LoadSubcategoryByIdUseCase
import com.example.yourfinance.domain.usecase.subcategory.CreateSubcategoryUseCase
import com.example.yourfinance.domain.usecase.subcategory.DeleteSubcategoryUseCase
import com.example.yourfinance.domain.usecase.subcategory.UpdateSubcategoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubcategoryManagerViewModel @Inject constructor(
    fetchFullCategoriesUseCase: FetchFullCategoriesUseCase,
    private val loadSubcategoryByIdUseCase: LoadSubcategoryByIdUseCase,
    private val updateSubcategoryUseCase: UpdateSubcategoryUseCase,
    private val createSubcategoryUseCase: CreateSubcategoryUseCase,
    private val deleteSubcategoryUseCase: DeleteSubcategoryUseCase
) : ViewModel() {

    val allCategories: LiveData<List<FullCategory>> = fetchFullCategoriesUseCase()


    suspend fun loadSubcategoryById(subcategoryId: Long): Subcategory? = loadSubcategoryByIdUseCase(subcategoryId)
    fun updateSubcategory(subcategory: Subcategory) {
        viewModelScope.launch {
            updateSubcategoryUseCase(subcategory)
        }
    }

    fun createSubcategory(subcategory: Subcategory) {
        viewModelScope.launch {
            createSubcategoryUseCase(subcategory)
        }
    }

    fun deleteSubcategory(subcategory: Subcategory) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteSubcategoryUseCase(subcategory)
        }
    }


}