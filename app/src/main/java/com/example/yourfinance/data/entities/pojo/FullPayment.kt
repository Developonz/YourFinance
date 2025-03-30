package com.example.yourfinance.data.entities.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.data.entities.CategoryEntity
import com.example.yourfinance.data.entities.MoneyAccountEntity
import com.example.yourfinance.data.entities.PaymentEntity

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