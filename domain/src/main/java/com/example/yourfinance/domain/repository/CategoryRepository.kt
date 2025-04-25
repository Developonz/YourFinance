package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.Subcategory

interface CategoryRepository {
    fun getAllCategory(): LiveData<List<Category>>

    suspend fun insertCategory(category: Category) : Long
    fun getAllCategoriesWithSubcategories(): LiveData<List<Category>>
    suspend fun deleteCategory(category: Category)
    suspend fun loadCategoryById(categoryId: Long): Category?
    suspend fun updateCategory(category: Category)
    suspend fun loadFullCategoryById(categoryId: Long): Category?
    suspend fun loadSubcategory(subcategoryId: Long): Subcategory?
    suspend fun updateSubcategory(subcategory: Subcategory)
    suspend fun insertSubcategory(subcategory: Subcategory)
    suspend fun deleteSubcategory(subcategory: Subcategory)
}