package com.example.yourfinance.domain.usecase.category

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class FetchFullCategoriesUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    operator fun invoke() : LiveData<List<FullCategory>> {
        return categoryRepository.getAllCategoriesWithSubcategories()
    }
}