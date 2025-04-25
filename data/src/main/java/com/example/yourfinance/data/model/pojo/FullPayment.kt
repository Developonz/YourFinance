package com.example.yourfinance.data.model.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity

data class FullPayment (
    @Embedded val payment: PaymentEntity,
    @Relation(
        parentColumn = "moneyAccID",
        entityColumn = "id"
    )
    val moneyAcc: MoneyAccountEntity,

    @Relation(
        parentColumn = "categoryID",
        entityColumn = "id"
    )
    val category: CategoryEntity
)