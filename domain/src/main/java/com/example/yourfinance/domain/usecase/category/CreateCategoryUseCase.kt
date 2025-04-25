package com.example.yourfinance.domain.usecase.category

import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(fullCategory: Category) {
        categoryRepository.insertCategory(fullCategory)
    }
}