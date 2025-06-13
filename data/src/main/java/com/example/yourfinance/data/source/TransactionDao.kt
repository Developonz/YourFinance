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
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.data.model.pojo.FullPayment
import com.example.yourfinance.data.model.pojo.FullTransfer
import com.example.yourfinance.domain.model.TransactionType
import java.time.LocalDate

@Dao
abstract class TransactionDao(private val dataBase: FinanceDataBase) {


    @Insert
    abstract fun insertPaymentTransactionInternal(payment: PaymentEntity) : Long

    @Transaction
    open suspend fun insertPaymentTransaction(payment: PaymentEntity) : Long {
        payment.is_done = payment.date <= LocalDate.now()
        val paymentId = insertPaymentTransactionInternal(payment)
        if (payment.is_done) {
            val account = dataBase.getMoneyAccountDao().getAccountById(payment.moneyAccID)
            account?.let {
                it.balance += if (payment.type == TransactionType.INCOME) payment.balance else -payment.balance
                dataBase.getMoneyAccountDao().updateAccount(it)
            }
        }
        return paymentId
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertTransferTransactionInternal(trans: TransferEntity): Long

    @Transaction
    open suspend fun insertTransferTransaction(transfer: TransferEntity) : Long {
        transfer.is_done = transfer.date <= LocalDate.now()
        val transferId = insertTransferTransactionInternal(transfer)
        if (transfer.is_done) {
            val accountFrom =
                dataBase.getMoneyAccountDao().getAccountById(transfer.moneyAccFromID)
            val accountTo = dataBase.getMoneyAccountDao().getAccountById(transfer.moneyAccToID)

            accountFrom?.let { from ->
                accountTo?.let { to ->
                    from.balance -= transfer.balance
                    dataBase.getMoneyAccountDao().updateAccount(from)

                    to.balance += transfer.balance
                    dataBase.getMoneyAccountDao().updateAccount(to)
                }
            }
        }
        return transferId
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
        if (payment.is_done) {
            val account = dataBase.getMoneyAccountDao().getAccountById(payment.moneyAccID)
            account?.let {
                val sum = if (payment.type == TransactionType.INCOME) payment.balance else -payment.balance
                it.balance -= sum
                dataBase.getMoneyAccountDao().updateAccount(it)
            }
        }
        deletePaymentInternal(payment)
    }

    @Delete
    abstract fun deleteTransferInternal(transfer: TransferEntity)

    @Transaction
    open suspend fun deleteTransfer(transfer: TransferEntity) {
        if (transfer.is_done) {
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
        val dao = dataBase.getMoneyAccountDao()

        // 1) Загрузить старую запись
        val old = loadPaymentById(newPayment.id)?.payment ?: return

        // 2) Загрузить старый счёт
        val oldAccount = dao.getAccountById(old.moneyAccID) ?: return

        // 3) Откатить старую операцию, если она была проведена
        if (old.is_done) {
            oldAccount.balance -= if (old.type == TransactionType.INCOME) old.balance else - old.balance
        }
        dao.updateAccount(oldAccount)

        // 4) Загрузить новый счёт
        val newAccount = dao.getAccountById(newPayment.moneyAccID) ?: return

        // 5) Обновить флаг is_done
        newPayment.is_done = newPayment.date <= LocalDate.now()

        // 6) Применить новую операцию, если она теперь проведена
        if (newPayment.is_done) {
            newAccount.balance += if (newPayment.type == TransactionType.INCOME) newPayment.balance else -newPayment.balance
        }
        dao.updateAccount(newAccount)

        // 7) Обновить сам платёж
        updatePaymentInternal(newPayment)
    }


    @Update
    abstract fun updateTransferInternal(transfer: TransferEntity)

    @Transaction
    open suspend fun updateTransfer(newTransfer: TransferEntity) {
        val dao = dataBase.getMoneyAccountDao()

        // 1) Загрузить старую запись
        val old = loadTransferById(newTransfer.id)?.transfer ?: return

        // 2) Загрузить старые аккаунты
        val oldFrom = dao.getAccountById(old.moneyAccFromID) ?: return
        val oldTo   = dao.getAccountById(old.moneyAccToID)   ?: return

        // 3) Откатить старую операцию, если она была проведена
        if (old.is_done) {
            oldFrom.balance += old.balance      // возвращаем деньги на from
            oldTo.balance   -= old.balance      // снимаем с to
        }
        dao.updateAccount(oldFrom)
        dao.updateAccount(oldTo)

        // 4) Загрузить новые аккаунты
        val newFrom = dao.getAccountById(newTransfer.moneyAccFromID) ?: return
        val newTo   = dao.getAccountById(newTransfer.moneyAccToID)   ?: return

        // 5) Обновить флаг is_done
        newTransfer.is_done = newTransfer.date <= LocalDate.now()

        // 6) Применить новую операцию, если она теперь проведена
        if (newTransfer.is_done) {
            newFrom.balance -= newTransfer.balance
            newTo.balance   += newTransfer.balance
        }
        dao.updateAccount(newFrom)
        dao.updateAccount(newTo)

        // 7) Обновить сам перевод
        updateTransferInternal(newTransfer)
    }


    @Query("SELECT * FROM PaymentEntity")
    abstract suspend fun getAllPaymentsForExport(): List<PaymentEntity>

    @Query("SELECT * FROM TransferEntity")
    abstract suspend fun getAllTransfersForExport(): List<TransferEntity>

    @Query("DELETE FROM PaymentEntity")
    abstract suspend fun clearAllPayments()

    @Query("DELETE FROM TransferEntity")
    abstract suspend fun clearAllTransfers()
}