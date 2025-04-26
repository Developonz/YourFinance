package com.example.yourfinance.domain.usecase.categories.general

import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class DeleteAnyCategoryUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(id: Long) {
        categoryRepository.deleteCategory(id)
    }
}