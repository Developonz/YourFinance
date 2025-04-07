package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.Dao
import androidx.room.Delete // Импортируем аннотацию @Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy // Полезно для insert/update
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

    // --- INSERT ---
    @Insert(onConflict = OnConflictStrategy.REPLACE) // Можно указать стратегию конфликта
    abstract fun insertPaymentTransaction(trans: PaymentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTransferTransaction(trans: TransferEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAccount(acc: MoneyAccountEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertCategory(categoryEntity: CategoryEntity) : Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertSubcategory(subcategoryEntity: SubcategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBudget(budget: BudgetEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertBudgetCategoriesCrossRef(entity: BudgetCategoriesCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllCategory(categoryEntity: List<CategoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAllSubcategory(categoryEntity: List<SubcategoryEntity>)

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFullBudget(budget: BudgetEntity, categories: List<CategoryEntity>, subcategories: List<SubcategoryEntity>) {
        insertBudget(budget)
        // Возможно, стоит очистить старые связи перед вставкой новых, если это обновление
        // deleteAllBudgetCategoriesCrossRefForBudget(budget.id) // Пример
        categories.forEach { category ->
            insertBudgetCategoriesCrossRef(BudgetCategoriesCrossRef(budget.id, category.id))
        }
        insertAllCategory(categories) // Возможно, это не нужно, если категории уже существуют
        insertAllSubcategory(subcategories) // Возможно, это не нужно, если субкатегории уже существуют
    }

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertFullCategory(category: CategoryEntity, subcategories: List<SubcategoryEntity>) {
        val categoryId = insertCategory(category)
        // Убедимся, что у субкатегорий правильный categoryId
        val updatedSubcategories = subcategories.map { it.copy(parentId = categoryId) }
        insertAllSubcategory(updatedSubcategories)
    }

    // --- SELECT ---
    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM PaymentEntity ORDER BY time DESC") // Добавим сортировку для примера
    abstract fun getAllPayment(): LiveData<List<FullPayment>>

    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM TransferEntity ORDER BY time DESC") // Добавим сортировку для примера
    abstract fun getAllTransfer() : LiveData<List<FullTransfer>>

    @Query("SELECT * FROM MoneyAccountEntity ORDER BY title ASC")
    abstract fun getAllAccounts() : LiveData<List<MoneyAccountEntity>>

    @Query("SELECT * FROM CategoryEntity ORDER BY title ASC")
    abstract fun getAllCategory() : LiveData<List<CategoryEntity>>

    @Query("SELECT * FROM SubcategoryEntity ORDER BY title ASC")
    abstract fun getAllSubcategory() : LiveData<List<SubcategoryEntity>>


    @Query("SELECT * FROM MoneyAccountEntity where id = :id")
    abstract suspend fun getAccountById(id: Long) : MoneyAccountEntity?


    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM CategoryEntity ORDER BY title ASC")
    abstract fun getFullAllCategory() : LiveData<List<CategoryWithSubcategories>>

    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM BudgetEntity ORDER BY title ASC")
    abstract fun getAllBudgets() : LiveData<List<BudgetWithCategories>>


    // --- DELETE ---

    // Удаление по объекту
    @Delete
    abstract fun deletePayment(payment: PaymentEntity)

    @Delete
    abstract fun deleteTransfer(transfer: TransferEntity)

    @Delete
    abstract fun deleteAccount(account: MoneyAccountEntity)

    @Delete
    abstract fun deleteCategory(category: CategoryEntity) // Внимание: не удаляет связанные субкатегории автоматически!

    @Delete
    abstract fun deleteSubcategory(subcategory: SubcategoryEntity)

    @Delete
    abstract fun deleteBudget(budget: BudgetEntity) // Внимание: не удаляет связанные BudgetCategoriesCrossRef автоматически!

    @Delete
    abstract fun deleteBudgetCategoryCrossRef(crossRef: BudgetCategoriesCrossRef)

    // --- Удаление по ID (с помощью @Query) ---
    // Замените 'id', 'accountId' и т.д. на реальные имена ваших первичных ключей, если они отличаются

    @Query("DELETE FROM PaymentEntity WHERE id = :paymentId")
    abstract fun deletePaymentById(paymentId: Long)

    @Query("DELETE FROM TransferEntity WHERE id = :transferId")
    abstract fun deleteTransferById(transferId: Long)

    @Query("DELETE FROM MoneyAccountEntity WHERE id = :accountId")
    abstract fun deleteAccountById(accountId: Long)

    @Query("DELETE FROM CategoryEntity WHERE id = :categoryId")
    abstract fun deleteCategoryById(categoryId: Long) // Внимание: не удаляет связанные субкатегории!

    @Query("DELETE FROM SubcategoryEntity WHERE id = :subcategoryId")
    abstract fun deleteSubcategoryById(subcategoryId: Long)

    @Query("DELETE FROM BudgetEntity WHERE id = :budgetId")
    abstract fun deleteBudgetById(budgetId: Long) // Внимание: не удаляет связанные BudgetCategoriesCrossRef!

    // Удаление связей для конкретного бюджета
    @Query("DELETE FROM BudgetCategoriesCrossRef WHERE budgetId = :budgetId")
    abstract fun deleteAllBudgetCategoriesCrossRefForBudget(budgetId: Long)

    // Удаление связей для конкретной категории
    @Query("DELETE FROM BudgetCategoriesCrossRef WHERE categoryId = :categoryId")
    abstract fun deleteAllBudgetCategoriesCrossRefForCategory(categoryId: Long)

    // Удаление всех субкатегорий для данной категории
    @Query("DELETE FROM SubcategoryEntity WHERE parentId = :categoryId")
    abstract fun deleteAllSubcategoriesForCategory(categoryId: Long)


    // --- Комплексное удаление (Пример: Категория + Субкатегории) ---
    // Такую логику лучше выносить в Repository или UseCase, но можно сделать и в DAO через @Transaction

    @Transaction
    open fun deleteCategoryWithSubcategories(categoryId: Long) {
        deleteAllSubcategoriesForCategory(categoryId) // Сначала удаляем зависимые субкатегории
        deleteCategoryById(categoryId)             // Затем удаляем саму категорию
        // Также можно удалить связи BudgetCategoriesCrossRef, если нужно
        // deleteAllBudgetCategoriesCrossRefForCategory(categoryId)
    }

    @Transaction
    open fun deleteBudgetWithRelations(budgetId: Long) {
        deleteAllBudgetCategoriesCrossRefForBudget(budgetId) // Удаляем связи
        deleteBudgetById(budgetId)                         // Удаляем сам бюджет
    }


    @Update
    abstract fun updateAccountById(account: MoneyAccountEntity)
}

//package com.example.yourfinance.data.source
//
//import androidx.lifecycle.LiveData
//import androidx.room.Dao
//import androidx.room.Insert
//import androidx.room.Query
//import androidx.room.Transaction
//import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
//import com.example.yourfinance.data.model.BudgetEntity
//import com.example.yourfinance.data.model.CategoryEntity
//import com.example.yourfinance.data.model.MoneyAccountEntity
//import com.example.yourfinance.data.model.PaymentEntity
//import com.example.yourfinance.data.model.SubcategoryEntity
//import com.example.yourfinance.data.model.TransferEntity
//import com.example.yourfinance.data.model.pojo.BudgetWithCategories
//import com.example.yourfinance.data.model.pojo.CategoryWithSubcategories
//import com.example.yourfinance.data.model.pojo.FullPayment
//import com.example.yourfinance.data.model.pojo.FullTransfer
//
//
//@Dao
//abstract class FinanceDao {
//
//    @Insert
//    abstract fun insertPaymentTransaction(trans: PaymentEntity)
//
//    @Insert
//    abstract fun insertTransferTransaction(trans: TransferEntity)
//
//    @Insert
//    abstract fun insertAccount(acc: MoneyAccountEntity) : Long
//
//    @Insert
//    abstract fun insertCategory(categoryEntity: CategoryEntity) : Long
//
//    @Insert
//    abstract fun insertSubcategory(subcategoryEntity: SubcategoryEntity)
//
//    @Insert
//    abstract fun insertBudget(budget: BudgetEntity)
//
//    @Insert
//    abstract fun insertBudgetCategoriesCrossRef(entity: BudgetCategoriesCrossRef)
//
//    @Insert
//    abstract fun insertAllCategory(categoryEntity: List<CategoryEntity>)
//
//    @Insert
//    abstract fun insertAllSubcategory(categoryEntity: List<SubcategoryEntity>)
//
//    @Transaction
//    @Insert
//    fun insertFullBudget(budget: BudgetEntity, categories: List<CategoryEntity>, subcategories: List<SubcategoryEntity>) {
//        insertBudget(budget)
//        insertAllCategory(categories)
//        insertAllSubcategory(subcategories)
//    }
//
//    @Transaction
//    @Insert
//    fun insertFullCategory(categories: CategoryEntity, subcategories: List<SubcategoryEntity>) {
//        insertCategory(categories)
//        insertAllSubcategory(subcategories)
//    }
//
//    @Query("SELECT * FROM PaymentEntity")
//    abstract fun getAllPayment(): LiveData<List<FullPayment>>
//
//    @Query("SELECT * FROM TransferEntity")
//    abstract fun getAllTransfer() : LiveData<List<FullTransfer>>
//
//    @Query("SELECT * FROM MoneyAccountEntity")
//    abstract fun getAllAccounts() : LiveData<List<MoneyAccountEntity>>
//
//    @Query("SELECT * FROM CategoryEntity")
//    abstract fun getAllCategory() : LiveData<List<CategoryEntity>>
//
//    @Query("SELECT * FROM SubcategoryEntity")
//    abstract fun getAllSubcategory() : LiveData<List<SubcategoryEntity>>
//
//    @Query("SELECT * FROM CategoryEntity")
//    abstract fun getFullAllCategory() : LiveData<List<CategoryWithSubcategories>>
//
//    @Query("SELECT * FROM BudgetEntity")
//    abstract fun getAllBudgets() : LiveData<List<BudgetWithCategories>>
//
//
//
//
//}
