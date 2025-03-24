package com.example.yourfinance.model.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment

data class FullPayment(
    @Embedded val payment: Payment,
    @Relation(
        parentColumn = "moneyAccID",
        entityColumn = "id"
    )
    val moneyAcc: MoneyAccount
)
