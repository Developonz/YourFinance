package com.example.yourfinance.domain.usecase.subcategory

import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class CreateSubcategoryUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(subcategory: Subcategory) {
        categoryRepository.insertSubcategory(subcategory)
    }
}