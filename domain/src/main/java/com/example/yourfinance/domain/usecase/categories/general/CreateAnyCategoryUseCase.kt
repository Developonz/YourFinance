package com.example.yourfinance.domain.usecase.categories.general

import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateAnyCategoryUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(category: ICategoryData) : Long {
        return categoryRepository.insertCategory(category)
    }
}