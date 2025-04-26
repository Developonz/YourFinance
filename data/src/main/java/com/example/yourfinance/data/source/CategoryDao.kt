package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.pojo.CategoryWithSubcategories

@Dao
abstract class CategoryDao {
    @Insert
    abstract fun insertCategory(categoryEntity: CategoryEntity) : Long

    @Query("SELECT * FROM CategoryEntity")
    abstract fun fetchBaseCategories() : LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM CategoryEntity WHERE parentId = :parentId")
    abstract fun fetchSubcategoriesByParent(parentId: Long) : LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM CategoryEntity WHERE parentId IS null")
    abstract fun fetchCategories() : LiveData<List<CategoryWithSubcategories>>


    @Query("SELECT * FROM CategoryEntity WHERE id = :categoryId")
    abstract suspend fun loadBaseCategoryById(categoryId: Long): CategoryEntity?

    @Query("SELECT * FROM CategoryEntity WHERE parentId IS not null AND id = :subcategoryId")
    abstract suspend fun loadSubcategoryById(subcategoryId: Long): CategoryEntity?

    @Query("SELECT * FROM CategoryEntity WHERE parentId IS null AND id = :categoryId")
    abstract fun loadCategoryById(categoryId: Long): CategoryWithSubcategories?


    @Update
    abstract fun updateCategory(category: CategoryEntity)


    // TODO: изменить запрос для мягкого удаления
    @Query("DELETE FROM CategoryEntity WHERE id = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long)

}