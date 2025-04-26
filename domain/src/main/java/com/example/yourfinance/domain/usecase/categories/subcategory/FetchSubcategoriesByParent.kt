package com.example.yourfinance.domain.usecase.categories.subcategory

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class FetchSubcategoriesByParent @Inject constructor(private val categoryRepository: CategoryRepository) {
    operator fun invoke(parentId: Long) : LiveData<List<Subcategory>> {
        return categoryRepository.fetchSubcategoriesByParent(parentId)
    }
}