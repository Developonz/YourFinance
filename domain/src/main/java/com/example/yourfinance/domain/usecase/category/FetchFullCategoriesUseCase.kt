package com.example.yourfinance.domain.usecase.category

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class FetchFullCategoriesUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    operator fun invoke() : LiveData<List<Category>> {
        return categoryRepository.getAllCategoriesWithSubcategories()
    }
}