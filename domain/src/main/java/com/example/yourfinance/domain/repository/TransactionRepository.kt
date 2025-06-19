package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import java.math.BigDecimal
import java.time.LocalDate


interface TransactionRepository {
    fun fetchTransactions(startDate: LocalDate?, endDate: LocalDate?): LiveData<List<Transaction>>

    suspend fun createPayment(payment: Payment) : Long

    suspend fun createTransfer(transfer: Transfer) : Long

    suspend fun loadPaymentById(id: Long): Payment?

    suspend fun loadTransferById(id: Long): Transfer?

    suspend fun updatePayment(payment: Payment)

    suspend fun updateTransfer(transfer: Transfer)

    suspend fun deleteTransaction(transaction: Transaction)

    suspend fun getBalanceBeforeDate(periodEndDate: LocalDate, excludedAccountIds: List<Long>) : BigDecimal

    suspend fun getNetChangeBetweenDates(periodStartDate: LocalDate?,
                                         periodEndDate: LocalDate?,
                                         excludedAccountIds: List<Long>): BigDecimal

    suspend fun getSpentAmountForCategories(categoryIds: List<Long>, startDate: LocalDate, endDate: LocalDate): BigDecimal
}