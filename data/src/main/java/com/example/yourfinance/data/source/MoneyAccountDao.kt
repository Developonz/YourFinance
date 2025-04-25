package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.yourfinance.data.model.MoneyAccountEntity

@Dao
abstract class MoneyAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertAccount(acc: MoneyAccountEntity) : Long

    @Query("SELECT * FROM MoneyAccountEntity ORDER BY title ASC")
    abstract fun getAllAccounts() : LiveData<List<MoneyAccountEntity>>

    @Query("SELECT * FROM MoneyAccountEntity where id = :id")
    abstract suspend fun getAccountById(id: Long) : MoneyAccountEntity?

    @Delete
    abstract fun deleteAccount(account: MoneyAccountEntity)

    @Query("DELETE FROM MoneyAccountEntity WHERE id = :accountId")
    abstract fun deleteAccountById(accountId: Long)

    @Update
    abstract fun updateAccount(account: MoneyAccountEntity)
}