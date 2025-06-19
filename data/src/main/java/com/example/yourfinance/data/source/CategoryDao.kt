package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.lifecycle.switchMap
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.yourfinance.data.mapper.toDomainCategory
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.pojo.CategoryWithSubcategories

@Dao
abstract class CategoryDao {
    @Insert
    abstract fun insertCategoryInternal(categoryEntity: CategoryEntity) : Long

    open fun insertCategory(categoryEntity: CategoryEntity) : Long {
        if (categoryEntity.parentId != null) {
            val parentEntity = if (categoryEntity.parentId != 0L)
                loadBaseCategoryById(categoryEntity.parentId) else null
            categoryEntity.colorHex = parentEntity?.colorHex
            categoryEntity.iconResourceId = parentEntity?.iconResourceId
        }
        return insertCategoryInternal(categoryEntity)
    }

    @Query("SELECT * FROM CategoryEntity")
    abstract fun fetchBaseCategories() : LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM CategoryEntity WHERE parentId = :parentId")
    abstract fun fetchSubcategoriesByParent(parentId: Long) : LiveData<List<CategoryEntity>>



    @Query("SELECT * FROM CategoryEntity WHERE parentId IS null")
    abstract fun fetchCategories() : LiveData<List<CategoryWithSubcategories>>



    @Query("SELECT * FROM CategoryEntity WHERE id = :categoryId")
    abstract fun loadBaseCategoryById(categoryId: Long): CategoryEntity?

    @Query("SELECT * FROM CategoryEntity WHERE parentId IS not null AND id = :subcategoryId")
    abstract suspend fun loadSubcategoryById(subcategoryId: Long): CategoryEntity?

    @Query("SELECT * FROM CategoryEntity WHERE parentId IS null AND id = :categoryId")
    abstract fun loadCategoryById(categoryId: Long): CategoryWithSubcategories?


    @Update
    abstract fun updateCategory(category: CategoryEntity)


    // TODO: изменить запрос для мягкого удаления
    @Query("DELETE FROM CategoryEntity WHERE id = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long)

    @Query("SELECT * FROM CategoryEntity")
    abstract suspend fun getAllCategoriesForExport(): List<CategoryEntity>

    @Query("DELETE FROM CategoryEntity")
    abstract suspend fun clearAll()

    @Transaction
    @Query("SELECT * FROM CategoryEntity WHERE parentId IS NULL AND categoryType = 1")
    abstract suspend fun fetchAllExpenseCategoriesWithSubcategories(): List<CategoryWithSubcategories>


    @Query("SELECT * FROM CategoryEntity WHERE categoryType = 1")
    abstract suspend fun getAllExpenseEntities(): List<CategoryEntity>

}