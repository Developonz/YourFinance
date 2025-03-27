package com.example.yourfinance.model.pojo

import androidx.room.Embedded
import androidx.room.Relation
import com.example.yourfinance.model.Transactions
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Transfer

data class FullTransfer(
    @Embedded
    val transfer: Transfer,
    @Relation(
        parentColumn = "moneyAccFromID",
        entityColumn = "id"
    )
    val moneyAccFrom: MoneyAccount,
    @Relation(
        parentColumn = "moneyAccToID",
        entityColumn = "id"
    )
    val moneyAccTo: MoneyAccount
) : Transactions() {
    override fun getTransactionId(): Long {
        return transfer.id
    }
}