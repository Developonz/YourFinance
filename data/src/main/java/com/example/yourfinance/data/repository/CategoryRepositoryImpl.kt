package com.example.yourfinance.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.yourfinance.data.mapper.toData
import com.example.yourfinance.data.mapper.toDomainCategory
import com.example.yourfinance.data.mapper.toDomainSubcategory
import com.example.yourfinance.data.source.CategoryDao
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(private val dao: CategoryDao) : CategoryRepository{

    override suspend fun insertCategory(category: ICategoryData) : Long {
        val entityToInsert = when (category) {
            is Category -> category.toData()
            is Subcategory -> category.toData()
            else -> return 0
        }

        return withContext(Dispatchers.IO) {
            dao.insertCategory(entityToInsert)
        }
    }

    override suspend fun loadCategoryById(categoryId: Long): Category? {
        return withContext(Dispatchers.IO) { dao.loadCategoryById(categoryId)?.toDomainCategory() }
    }

    override suspend fun loadSubcategoryById(subcategoryId: Long): Subcategory? {
        return withContext(Dispatchers.IO) { dao.loadSubcategoryById(subcategoryId)?.toDomainSubcategory() }
    }

    override fun fetchCategories(): LiveData<List<Category>> {
        return dao.fetchCategories().map { it ->
            it.map {
                it.toDomainCategory()
            }
        }
    }

    override fun fetchSubcategoriesByParent(parentId: Long): LiveData<List<Subcategory>> {
        return dao.fetchSubcategoriesByParent(parentId).map { it ->
            it.map {
                it.toDomainSubcategory()
            }
        }
    }

    override suspend fun updateCategory(category: ICategoryData) {
        val entityToUpdate = when (category) {
            is Category -> category.toData()
            is Subcategory -> category.toData()
            else -> return
        }
        withContext(Dispatchers.IO) {
            dao.updateCategory(entityToUpdate)
        }
    }

    override suspend fun deleteCategory(id: Long) {
        dao.deleteCategoryById(id)
    }

}