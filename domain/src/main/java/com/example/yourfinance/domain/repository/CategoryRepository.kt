package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.domain.model.entity.category.Subcategory

interface CategoryRepository {
    suspend fun insertCategory(category: ICategoryData)

    suspend fun loadCategoryById(categoryId: Long): Category?
    suspend fun loadSubcategoryById(subcategoryId: Long): Subcategory?

    fun fetchCategories(): LiveData<List<Category>>
    fun fetchSubcategoriesByParent(parentId: Long): LiveData<List<Subcategory>>

    suspend fun updateCategory(category: ICategoryData)

    suspend fun deleteCategory(id: Long)
}