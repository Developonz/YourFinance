package com.example.yourfinance.data.model

data class BackupWrapper(
    val moneyAccounts: List<MoneyAccountEntity>,
    val categories: List<CategoryEntity>,
    val payments: List<PaymentEntity>,
    val transfers: List<TransferEntity>
)