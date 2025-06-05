package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
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

    @Transaction
    open suspend fun setDefaultAccount(accountId: Long) {
        clearDefaultFlags()
        setDefaultForAccountInternal(accountId)
    }

    @Query("UPDATE MoneyAccountEntity SET `default` = 0")
    abstract suspend fun clearDefaultFlags()

    @Query("UPDATE MoneyAccountEntity SET `default` = 1 WHERE id = :accountId")
    abstract suspend fun setDefaultForAccountInternal(accountId: Long)

    @Query("SELECT * FROM MoneyAccountEntity")
    abstract suspend fun getAllAccountsForExport(): List<MoneyAccountEntity>

    @Query("DELETE FROM MoneyAccountEntity")
    abstract suspend fun clearAll()
}