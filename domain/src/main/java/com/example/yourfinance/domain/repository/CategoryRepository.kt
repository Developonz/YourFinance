package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.model.entity.category.Subcategory

interface CategoryRepository {
    fun getAllCategory(): LiveData<List<FullCategory>>

    suspend fun insertCategory(category: FullCategory) : Long
    fun getAllCategoriesWithSubcategories(): LiveData<List<FullCategory>>
    suspend fun deleteCategory(category: Category)
    suspend fun loadCategoryById(categoryId: Long): Category?
    suspend fun updateCategory(category: Category)
    suspend fun loadFullCategoryById(categoryId: Long): FullCategory?
    suspend fun loadSubcategory(subcategoryId: Long): com.example.yourfinance.domain.model.entity.category.Subcategory?
    suspend fun updateSubcategory(subcategory: com.example.yourfinance.domain.model.entity.category.Subcategory)
    suspend fun insertSubcategory(subcategory: com.example.yourfinance.domain.model.entity.category.Subcategory)
    suspend fun deleteSubcategory(subcategory: com.example.yourfinance.domain.model.entity.category.Subcategory)
}