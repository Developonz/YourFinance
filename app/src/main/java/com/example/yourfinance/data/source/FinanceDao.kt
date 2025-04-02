package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.SubcategoryEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.data.model.pojo.BudgetWithCategories
import com.example.yourfinance.data.model.pojo.CategoryWithSubcategories
import com.example.yourfinance.data.model.pojo.FullPayment
import com.example.yourfinance.data.model.pojo.FullTransfer


@Dao
abstract class FinanceDao {

    @Insert
    abstract fun insertPaymentTransaction(trans: PaymentEntity)

    @Insert
    abstract fun insertTransferTransaction(trans: TransferEntity)

    @Insert
    abstract fun insertAccount(acc: MoneyAccountEntity) : Long

    @Insert
    abstract fun insertCategory(categoryEntity: CategoryEntity) : Long

    @Insert
    abstract fun insertSubcategory(subcategoryEntity: SubcategoryEntity)

    @Insert
    abstract fun insertBudget(budget: BudgetEntity)

    @Insert
    abstract fun insertBudgetCategoriesCrossRef(entity: BudgetCategoriesCrossRef)

    @Insert
    abstract fun insertAllCategory(categoryEntity: List<CategoryEntity>)

    @Insert
    abstract fun insertAllSubcategory(categoryEntity: List<SubcategoryEntity>)

    @Transaction
    @Insert
    fun insertFullBudget(budget: BudgetEntity, categories: List<CategoryEntity>, subcategories: List<SubcategoryEntity>) {
        insertBudget(budget)
        insertAllCategory(categories)
        insertAllSubcategory(subcategories)
    }

    @Transaction
    @Insert
    fun insertFullCategory(categories: CategoryEntity, subcategories: List<SubcategoryEntity>) {
        insertCategory(categories)
        insertAllSubcategory(subcategories)
    }

    @Query("SELECT * FROM PaymentEntity")
    abstract fun getAllPayment(): LiveData<List<FullPayment>>

    @Query("SELECT * FROM TransferEntity")
    abstract fun getAllTransfer() : LiveData<List<FullTransfer>>

    @Query("SELECT * FROM MoneyAccountEntity")
    abstract fun getAllAccounts() : LiveData<List<MoneyAccountEntity>>

    @Query("SELECT * FROM CategoryEntity")
    abstract fun getAllCategory() : LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM SubcategoryEntity")
    abstract fun getAllSubcategory() : LiveData<List<SubcategoryEntity>>

    @Query("SELECT * FROM CategoryEntity")
    abstract fun getFullAllCategory() : LiveData<List<CategoryWithSubcategories>>

    @Query("SELECT * FROM BudgetEntity")
    abstract fun getAllBudgets() : LiveData<List<BudgetWithCategories>>




}
