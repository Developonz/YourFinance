package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.pojo.BudgetWithCategories

@Dao
abstract class BudgetDao() {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBudgetInternal(budget: BudgetEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertBudgetCategoriesCrossRef(entity: BudgetCategoriesCrossRef)

    @Update
    abstract suspend fun updateBudgetInternal(budget: BudgetEntity)

    @Transaction
    @Query("SELECT * FROM BudgetEntity ORDER BY title ASC")
    abstract fun getAllBudgets(): LiveData<List<BudgetWithCategories>>

    @Transaction
    @Query("SELECT * FROM BudgetEntity WHERE id = :budgetId")
    abstract suspend fun loadBudgetById(budgetId: Long): BudgetWithCategories?

    @Query("DELETE FROM BudgetCategoriesCrossRef WHERE budgetId = :budgetId")
    abstract suspend fun deleteAllBudgetCategoriesCrossRefForBudget(budgetId: Long)

    @Query("DELETE FROM BudgetEntity WHERE id = :budgetId")
    abstract suspend fun deleteBudgetById(budgetId: Long)

    @Transaction
    open suspend fun insertBudgetWithCategories(budget: BudgetEntity, categoryIds: List<Long>) {
        val budgetId = insertBudgetInternal(budget)
        if (categoryIds.isNotEmpty()) {
            val crossRefs = categoryIds.map { categoryId ->
                BudgetCategoriesCrossRef(budgetId = budgetId, categoryId = categoryId)
            }
            crossRefs.forEach { insertBudgetCategoriesCrossRef(it) }
        }
    }

    @Transaction
    open suspend fun updateBudgetWithCategories(budget: BudgetEntity, categoryIds: List<Long>) {
        updateBudgetInternal(budget)
        deleteAllBudgetCategoriesCrossRefForBudget(budget.id)
        if (categoryIds.isNotEmpty()) {
            val crossRefs = categoryIds.map { categoryId ->
                BudgetCategoriesCrossRef(budgetId = budget.id, categoryId = categoryId)
            }
            crossRefs.forEach { insertBudgetCategoriesCrossRef(it) }
        }
    }

    @Transaction
    open suspend fun deleteBudgetWithRelations(budgetId: Long) {
        deleteAllBudgetCategoriesCrossRefForBudget(budgetId)
        deleteBudgetById(budgetId)
    }

    // Методы для экспорта, если они вам нужны
    @Query("SELECT * FROM BudgetEntity")
    abstract suspend fun getAllBudgetsForExport(): List<BudgetWithCategories>

    @Query("SELECT * FROM BudgetCategoriesCrossRef")
    abstract suspend fun getAllBudgetCategoriesCrossRefForExport(): List<BudgetCategoriesCrossRef>

    @Query("DELETE FROM BudgetEntity")
    abstract suspend fun clearAllBudgets()

    @Query("DELETE FROM BudgetCategoriesCrossRef")
    abstract suspend fun clearAllBudgetCategoriesCrossRef()
}