package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.yourfinance.data.mapper.toDataFuture
import com.example.yourfinance.data.mapper.toDataFutureTransfer
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.data.model.pojo.FullPayment
import com.example.yourfinance.data.model.pojo.FullTransfer
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.category.Subcategory
import java.time.LocalDate

@Dao
abstract class TransactionDao(private val dataBase: FinanceDataBase) {


    @Insert
    abstract fun insertPaymentTransactionInternal(payment: PaymentEntity) : Long

    @Transaction
    open suspend fun insertPaymentTransaction(payment: PaymentEntity) {
        val paymentId = insertPaymentTransactionInternal(payment)
        val account = dataBase.getMoneyAccountDao().getAccountById(payment.moneyAccID)
        account?.let {
            if (payment.date <= LocalDate.now()) {
                it.balance += if (payment.type == TransactionType.INCOME) payment.balance else -payment.balance
                dataBase.getMoneyAccountDao().updateAccount(it)
            } else {
                val paymentWithGeneratedId = payment.copy(id = paymentId)
                dataBase.getFutureTransactionDao().insertFuturePaymentTransaction(paymentWithGeneratedId.toDataFuture())
            }
        }

    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTransferTransactionInternal(trans: TransferEntity): Long

    @Transaction
    open suspend fun insertTransferTransaction(transfer: TransferEntity) {
        val transferId = insertTransferTransactionInternal(transfer)

        if (transfer.date <= LocalDate.now()) {
            val accountFrom = dataBase.getMoneyAccountDao().getAccountById(transfer.moneyAccFromID)
            val accountTo = dataBase.getMoneyAccountDao().getAccountById(transfer.moneyAccToID)

            accountFrom?.let { from ->
                accountTo?.let { to ->
                    from.balance -= transfer.balance
                    dataBase.getMoneyAccountDao().updateAccount(from)

                    to.balance += transfer.balance
                    dataBase.getMoneyAccountDao().updateAccount(to)
                }
            }

        } else {
            val transferWithGeneratedId = transfer.copy(id = transferId)
            dataBase.getFutureTransactionDao().insertFutureTransferTransaction(transferWithGeneratedId.toDataFutureTransfer())
        }
    }

    @Transaction
    @Query("SELECT * FROM PaymentEntity where date >= :startDate and date <= :endDate")
    abstract fun getAllPaymentWithDateRange(startDate: LocalDate?, endDate: LocalDate?): LiveData<List<FullPayment>>

    @Transaction
    @Query("SELECT * FROM TransferEntity where date >= :startDate and date <= :endDate")
    abstract fun getAllTransferWithDateRange(startDate: LocalDate?, endDate: LocalDate?) : LiveData<List<FullTransfer>>

    @Transaction // Добавляем @Transaction для POJO с @Relation
    @Query("SELECT * FROM PaymentEntity")
    abstract fun getAllPayment(): LiveData<List<FullPayment>>

    @Transaction
    @Query("SELECT * FROM TransferEntity")
    abstract fun getAllTransfer() : LiveData<List<FullTransfer>>

    @Query("""
        SELECT COALESCE(SUM(
            CASE type
                WHEN 0 THEN balance
                WHEN 1 THEN -balance
                ELSE 0.0
            END
        ), 0.0)
        FROM PaymentEntity
        WHERE date < :exclusiveEndDate AND moneyAccID NOT IN (:excludedAccountIds)
    """)
    abstract suspend fun getBalanceBeforeDate(exclusiveEndDate: LocalDate, excludedAccountIds: List<Long>): Double

    @Query("""
        SELECT COALESCE(SUM(
            CASE type
                WHEN 0 THEN balance
                WHEN 1 THEN -balance
                ELSE 0.0
            END
        ), 0.0)
        FROM PaymentEntity
        WHERE (:startDate IS NULL OR date >= :startDate) AND (:endDate IS NULL OR date <= :endDate)
        AND moneyAccID NOT IN (:excludedAccountIds)
    """)
    abstract suspend fun getNetChangeBetweenDates(startDate: LocalDate?, endDate: LocalDate?, excludedAccountIds: List<Long>): Double

    @Delete
    abstract fun deletePaymentInternal(payment: PaymentEntity)

    @Transaction
    open suspend fun deletePayment(payment: PaymentEntity) {
        val count = dataBase.getFutureTransactionDao().loadCountFuturePaymentTransaction(payment.id)
        if (count == 0) {
            val sum = if (payment.type == TransactionType.INCOME) payment.balance else -payment.balance
            val account = dataBase.getMoneyAccountDao().getAccountById(payment.moneyAccID)
            account?.let {
                it.balance -= sum
                dataBase.getMoneyAccountDao().updateAccount(it)
            }
            deletePaymentInternal(payment)
        } else {
            deletePaymentInternal(payment)
        }
    }

    @Delete
    abstract fun deleteTransferInternal(transfer: TransferEntity)

    @Transaction
    open suspend fun deleteTransfer(transfer: TransferEntity) {
        val isFuture = dataBase.getFutureTransactionDao().loadCountFutureTransferTransaction(transfer.id) > 0

        if (!isFuture && transfer.date <= LocalDate.now()) {
            val accountFrom = dataBase.getMoneyAccountDao().getAccountById(transfer.moneyAccFromID)
            val accountTo = dataBase.getMoneyAccountDao().getAccountById(transfer.moneyAccToID)

            accountFrom?.let { from ->
                accountTo?.let { to ->
                    to.balance -= transfer.balance
                    dataBase.getMoneyAccountDao().updateAccount(to)

                    from.balance += transfer.balance
                    dataBase.getMoneyAccountDao().updateAccount(from)
                }
            }
        }

        if (isFuture) {
            dataBase.getFutureTransactionDao().deleteFutureTransferTransaction(transfer.id)
        }
        deleteTransferInternal(transfer)
    }

    @Query("DELETE FROM PaymentEntity WHERE id = :paymentId")
    abstract fun deletePaymentById(paymentId: Long)

    @Query("DELETE FROM TransferEntity WHERE id = :transferId")
    abstract fun deleteTransferById(transferId: Long)

    @Query("SELECT * FROM PaymentEntity where id = :id")
    abstract suspend fun loadPaymentById(id: Long): FullPayment?

    @Query("SELECT * FROM TransferEntity where id = :id")
    abstract suspend fun loadTransferById(id: Long): FullTransfer?

    @Update
    abstract fun updatePaymentInternal(payment: PaymentEntity)

    @Transaction
    open suspend fun updatePayment(newPayment: PaymentEntity) {
        val account = dataBase.getMoneyAccountDao().getAccountById(newPayment.moneyAccID)
        val count = dataBase.getFutureTransactionDao().loadCountFuturePaymentTransaction(newPayment.id)
        if (count == 0) {
            val oldPayment = loadPaymentById(newPayment.id)
            oldPayment?.let {
                if (newPayment.date > LocalDate.now()) {
                    account?.let {
                        val sum =
                            if (oldPayment.payment.type == TransactionType.INCOME) oldPayment.payment.balance else -oldPayment.payment.balance
                        it.balance -= sum
                        dataBase.getMoneyAccountDao().updateAccount(it)
                        dataBase.getFutureTransactionDao().insertFuturePaymentTransaction(newPayment.toDataFuture())
                    }
                } else {
                    account?.let {
                        val diff = newPayment.balance - oldPayment.payment.balance
                        it.balance += diff
                        dataBase.getMoneyAccountDao().updateAccount(it)
                    }
                }
            }
        } else {
            if (newPayment.date <= LocalDate.now()) {
                dataBase.getFutureTransactionDao().deleteFuturePaymentTransaction(newPayment.id)
                account?.let {
                    val sum = if (newPayment.type == TransactionType.INCOME) newPayment.balance else -newPayment.balance
                    it.balance += sum
                    dataBase.getMoneyAccountDao().updateAccount(it)
                }
            }
        }
        updatePaymentInternal(newPayment)
    }

    @Update
    abstract fun updateTransferInternal(transfer: TransferEntity)

    @Transaction
    open suspend fun updateTransfer(newTransfer: TransferEntity) {
        val oldTransferFull = loadTransferById(newTransfer.id) ?: return

        val oldTransfer = oldTransferFull.transfer
        val accountDao = dataBase.getMoneyAccountDao()
        val futureDao = dataBase.getFutureTransactionDao()

        val oldWasFuture = futureDao.loadCountFutureTransferTransaction(oldTransfer.id) > 0
        val newIsFuture = newTransfer.date > LocalDate.now()

        // 1. Откатить старый перевод, если он влиял на баланс
        if (!oldWasFuture && oldTransfer.date <= LocalDate.now()) {
            val oldAccFrom = accountDao.getAccountById(oldTransfer.moneyAccFromID)
            val oldAccTo = accountDao.getAccountById(oldTransfer.moneyAccToID)

            oldAccFrom?.let {
                it.balance += oldTransfer.balance
                accountDao.updateAccount(it)
            }
            oldAccTo?.let {
                it.balance -= oldTransfer.balance
                accountDao.updateAccount(it)
            }
        } else if (oldWasFuture) {
            futureDao.deleteFutureTransferTransaction(oldTransfer.id)
        }

        if (!newIsFuture) {
            val newAccFrom = accountDao.getAccountById(newTransfer.moneyAccFromID)
            val newAccTo = accountDao.getAccountById(newTransfer.moneyAccToID)

            newAccFrom?.let {
                it.balance -= newTransfer.balance
                accountDao.updateAccount(it)
            }
            newAccTo?.let {
                it.balance += newTransfer.balance
                accountDao.updateAccount(it)
            }
        } else {
            futureDao.insertFutureTransferTransaction(newTransfer.toDataFutureTransfer())
        }

        updateTransferInternal(newTransfer)
    }
}