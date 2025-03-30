package com.example.yourfinance.data.entities.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.data.entities.MoneyAccountEntity
import com.example.yourfinance.data.entities.TransferEntity

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