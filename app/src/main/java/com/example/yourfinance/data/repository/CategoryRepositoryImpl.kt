package com.example.yourfinance.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.map
import com.example.yourfinance.data.mapper.toData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.data.source.CategoryDao
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(private val dao: CategoryDao) : CategoryRepository{
    override fun getAllCategory(): LiveData<List<FullCategory>> {
        val mediator = MediatorLiveData<List<FullCategory>>()
        val categories = dao.getFullAllCategory()
        mediator.addSource(categories) {
            mediator.value = (categories.value ?: emptyList()).map {it.toDomain()}
        }
        return mediator
    }

    override suspend fun insertCategory(category: FullCategory) : Long {
        var id: Long = 0
        withContext(Dispatchers.IO) {
            id = dao.insertCategory(category.category.toData())
        }
        return id
    }

    override fun getAllCategoriesWithSubcategories(): LiveData<List<FullCategory>> {
        return dao.getFullAllCategory().map {
            it.map {
                it.toDomain()
            }
        }
    }

    override suspend fun deleteCategory(category: Category) {
        dao.deleteCategoryWithSubcategories(category.id)
    }

    override suspend fun loadCategoryById(categoryId: Long): Category? {
        return dao.getCategoryById(categoryId)?.toDomain()
    }

    override suspend fun updateCategory(category: Category) {
        withContext(Dispatchers.IO) {
            dao.updateCategory(category.toData())
        }
    }

    override suspend fun loadFullCategoryById(categoryId: Long): FullCategory? {
        return dao.getFullCategoryById(categoryId)?.toDomain()
    }

    override suspend fun deleteSubcategory(subcategory: Subcategory) {
        dao.deleteSubcategory(subcategory.toData())
    }

    override suspend fun loadSubcategory(subcategoryId: Long): Subcategory? {
        return dao.getSubcategory(subcategoryId)?.toDomain()
    }

    override suspend fun updateSubcategory(subcategory: Subcategory) {
        withContext(Dispatchers.IO) {
            dao.updateSubcategory(subcategory.toData())
        }
    }

    override suspend fun insertSubcategory(subcategory: Subcategory) {
        withContext(Dispatchers.IO) {
            dao.insertSubcategory(subcategory.toData())
        }
    }
}