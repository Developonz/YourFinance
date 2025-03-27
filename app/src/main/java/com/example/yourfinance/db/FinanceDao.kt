package com.example.yourfinance.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.yourfinance.model.entities.Category
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment
import com.example.yourfinance.model.entities.Transfer
import com.example.yourfinance.model.pojo.FullTransfer
import com.example.yourfinance.model.pojo.FullPayment

@Dao
abstract class FinanceDao {
    @Transaction
    @Insert
    abstract fun insertPaymentTransaction(trans: Payment)
    @Transaction
    @Insert
    abstract fun insertTransferTransaction(trans: Transfer)
    @Transaction
    @Insert
    abstract fun insertAccount(acc: MoneyAccount)
    @Transaction
    @Insert
    abstract fun insertCategory(category: Category)

    @Transaction
    @Query("SELECT * FROM Payment")
    abstract fun getAllPayment(): LiveData<List<FullPayment>>


    @Transaction
    @Query("SELECT * FROM Transfer")
    abstract fun getAllTransfer() : List<FullTransfer>

    @Query("SELECT * FROM Category")
    abstract fun getAllCategory() : List<Category>

    @Query("SELECT * FROM MoneyAccount")
    abstract fun getAllAccounts() : List<MoneyAccount>

}
