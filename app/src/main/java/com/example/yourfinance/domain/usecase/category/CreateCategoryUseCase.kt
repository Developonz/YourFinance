package com.example.yourfinance.domain.usecase.category

import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateCategoryUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(fullCategory: FullCategory) {
        categoryRepository.insertCategory(fullCategory)
    }
}