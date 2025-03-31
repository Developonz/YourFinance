package com.example.yourfinance.data.model.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.TransferEntity

data class FullTransfer(
    @Embedded
    val transfer: TransferEntity,
    @Relation(
        parentColumn = "moneyAccFromID",
        entityColumn = "id"
    )
    val moneyAccFrom: MoneyAccountEntity,
    @Relation(
        parentColumn = "moneyAccToID",
        entityColumn = "id"
    )
    val moneyAccTo: MoneyAccountEntity
)