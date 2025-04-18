package com.example.yourfinance.domain.usecase.category

import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class LoadFullCategoryByIdUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(id: Long) : FullCategory? {
        return categoryRepository.loadFullCategoryById(id)
    }
}