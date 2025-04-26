package com.example.yourfinance.domain.usecase.categories.category

import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class LoadCategoryByIdUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(id: Long) : Category? {
        return categoryRepository.loadCategoryById(id)
    }
}