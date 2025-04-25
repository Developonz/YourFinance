package com.example.yourfinance.domain.usecase.subcategory

import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.CategoryRepository
import javax.inject.Inject

class LoadSubcategoryByIdUseCase @Inject constructor(private val categoryRepository: CategoryRepository) {
    suspend operator fun invoke(id: Long) : com.example.yourfinance.domain.model.entity.category.Subcategory? {
        return categoryRepository.loadSubcategory(id)
    }
}