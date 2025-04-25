package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.SubcategoryEntity
import com.example.yourfinance.data.model.pojo.CategoryWithSubcategories

@Dao
abstract class CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCategory(categoryEntity: CategoryEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSubcategory(subcategoryEntity: SubcategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllCategory(categoryEntity: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllSubcategory(categoryEntity: List<SubcategoryEntity>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFullCategory(category: CategoryEntity, subcategories: List<SubcategoryEntity>) {
        val categoryId = insertCategory(category)
        // Убедимся, что у субкатегорий правильный categoryId
        val updatedSubcategories = subcategories.map { it.copy(parentId = categoryId) }
        insertAllSubcategory(updatedSubcategories)
    }

    @Query("SELECT * FROM CategoryEntity ORDER BY title ASC")
    abstract fun getAllCategory() : LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM SubcategoryEntity ORDER BY title ASC")
    abstract fun getAllSubcategory() : LiveData<List<SubcategoryEntity>>

    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM CategoryEntity ORDER BY title ASC")
    abstract fun getFullAllCategory() : LiveData<List<CategoryWithSubcategories>>

    @Delete
    abstract fun deleteCategory(category: CategoryEntity) // Внимание: не удаляет связанные субкатегории автоматически!

    @Delete
    abstract fun deleteSubcategory(subcategory: SubcategoryEntity)

    @Query("DELETE FROM CategoryEntity WHERE id = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long) // Внимание: не удаляет связанные субкатегории!

    @Query("DELETE FROM SubcategoryEntity WHERE id = :subcategoryId")
    abstract fun deleteSubcategoryById(subcategoryId: Long)


    // Удаление всех субкатегорий для данной категории
    @Query("DELETE FROM SubcategoryEntity WHERE parentId = :categoryId")
    abstract fun deleteAllSubcategoriesForCategory(categoryId: Long)


    // --- Комплексное удаление (Пример: Категория + Субкатегории) ---
    // Такую логику лучше выносить в Repository или UseCase, но можно сделать и в DAO через @Transaction

    @Transaction
    open suspend fun deleteCategoryWithSubcategories(categoryId: Long) {
        deleteAllSubcategoriesForCategory(categoryId)
        deleteCategoryById(categoryId)
        // Также можно удалить связи BudgetCategoriesCrossRef, если нужно
        // deleteAllBudgetCategoriesCrossRefForCategory(categoryId)
    }


    @Query("Select * from CategoryEntity where id = :categoryId")
    abstract suspend fun getCategoryById(categoryId: Long): CategoryEntity?

    @Update
    abstract fun updateCategory(category: CategoryEntity)

    @Transaction
    @Query("SELECT * FROM CategoryEntity where id = :categoryId")
    abstract fun getFullCategoryById(categoryId: Long): CategoryWithSubcategories?

    @Transaction
    @Query("SELECT * FROM SubcategoryEntity where id = :subcategoryId")
    abstract suspend fun getSubcategory(subcategoryId: Long): SubcategoryEntity?

    @Update
    abstract fun updateSubcategory(subcategory: SubcategoryEntity)
}