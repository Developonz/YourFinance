package com.example.yourfinance.model.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.model.Transaction
import com.example.yourfinance.model.Transactions
import com.example.yourfinance.model.entities.Category
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment

data class FullPayment (
    @Embedded val payment: Payment,
    @Relation(
        parentColumn = "moneyAccID",
        entityColumn = "id"
    )
    val moneyAcc: MoneyAccount,

    @Relation(
        parentColumn = "categoryID",
        entityColumn = "id"
    )
    val category: Category
)
