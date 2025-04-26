package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.pojo.BudgetWithCategories

@Dao
abstract class BudgetDao() {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBudgetCategoriesCrossRef(entity: BudgetCategoriesCrossRef)



    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM BudgetEntity ORDER BY title ASC")
    abstract fun getAllBudgets() : LiveData<List<BudgetWithCategories>>



    @Delete
    abstract fun deleteBudget(budget: BudgetEntity) // Внимание: не удаляет связанные BudgetCategoriesCrossRef автоматически!

    @Delete
    abstract fun deleteBudgetCategoryCrossRef(crossRef: BudgetCategoriesCrossRef)


    @Query("DELETE FROM BudgetEntity WHERE id = :budgetId")
    abstract fun deleteBudgetById(budgetId: Long) // Внимание: не удаляет связанные BudgetCategoriesCrossRef!

    // Удаление связей для конкретного бюджета
    @Query("DELETE FROM BudgetCategoriesCrossRef WHERE budgetId = :budgetId")
    abstract fun deleteAllBudgetCategoriesCrossRefForBudget(budgetId: Long)

    // Удаление связей для конкретной категории
    @Query("DELETE FROM BudgetCategoriesCrossRef WHERE categoryId = :categoryId")
    abstract fun deleteAllBudgetCategoriesCrossRefForCategory(categoryId: Long)



    @Transaction
    open suspend fun deleteBudgetWithRelations(budgetId: Long) {
        deleteAllBudgetCategoriesCrossRefForBudget(budgetId)
        deleteBudgetById(budgetId)
    }
}