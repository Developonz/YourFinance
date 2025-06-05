package com.example.yourfinance.data.model.backup

import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
import com.example.yourfinance.data.model.BudgetEntity
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.FuturePaymentEntity
import com.example.yourfinance.data.model.FutureTransferEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity

data class ExportImportData(
    val moneyAccounts: List<MoneyAccountEntity>,
    val categories: List<CategoryEntity>,
    val payments: List<PaymentEntity>,
    val transfers: List<TransferEntity>,
    val budgets: List<BudgetEntity>,
    val budgetCategoryCrossRefs: List<BudgetCategoriesCrossRef>,
    val futurePayments: List<FuturePaymentEntity>,
    val futureTransfers: List<FutureTransferEntity>
)