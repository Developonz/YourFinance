package com.example.yourfinance.data.source

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.yourfinance.data.mapper.toDataFuture
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
    abstract fun insertTransferTransaction(trans: TransferEntity)

    @Transaction // Добавляем @Transaction для POJO с @Relation
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
            //TODO: также в кэше начального баланса изменить
            deletePaymentInternal(payment)
        } else {
            val sum = if (payment.type == TransactionType.INCOME) payment.balance else -payment.balance
            //TODO: также в кэше начального баланса изменить
            deletePaymentInternal(payment)
        }
    }

    @Delete
    abstract fun deleteTransfer(transfer: TransferEntity)

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

        val oldPayment = loadPaymentById(newPayment.id)
        oldPayment?.let {

            val account = dataBase.getMoneyAccountDao().getAccountById(newPayment.moneyAccID)
            val count = dataBase.getFutureTransactionDao().loadCountFuturePaymentTransaction(newPayment.id)
            if (count == 0) {
                if (newPayment.date > LocalDate.now()) {
                    account?.let {
                        val sum = if (oldPayment.payment.type == TransactionType.INCOME) oldPayment.payment.balance else -oldPayment.payment.balance
                        it.balance -= sum
                        dataBase.getMoneyAccountDao().updateAccount(it)
                    }
                    //TODO: также в кэше начального баланса изменить
                } else {
                    account?.let {
                        val diff = newPayment.balance - oldPayment.payment.balance
                        it.balance += diff
                        dataBase.getMoneyAccountDao().updateAccount(it)
                    }
                    //TODO: также в кэше начального баланса изменить
                }
            } else {
                if (newPayment.date > LocalDate.now()) {
                    //TODO: в кэше начального баланса изменить
                } else {
                    dataBase.getFutureTransactionDao().deleteFuturePaymentTransaction(newPayment.id)
                    account?.let {
                        val sum = if (newPayment.type == TransactionType.INCOME) newPayment.balance else -newPayment.balance
                        it.balance += sum
                        dataBase.getMoneyAccountDao().updateAccount(it)
                    }
                    //TODO: также в кэше начального баланса изменить
                }
            }

            updatePaymentInternal(newPayment)



        }


    }

    @Update
    abstract fun updateTransfer(transfer: TransferEntity)
}