package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.data.model.pojo.FullTransfer
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.Transfer

fun FullTransfer.toDomain(): Transfer {
    return Transfer(
        id = this.transfer.id,
        balance = this.transfer.balance,
        moneyAccFrom = this.moneyAccFrom.toDomain(),
        moneyAccTo = this.moneyAccTo.toDomain(),
        _note = Title(this.transfer.note),
        is_done = this.transfer.is_done,
        date = this.transfer.date,
    )
}

fun Transfer.toData(): TransferEntity {
    return TransferEntity(
        id = this.id,
        type = this.type,
        balance = this.balance,
        moneyAccFromID = this.moneyAccFrom.id,
        moneyAccToID = this.moneyAccTo.id,
        note = this.note,
        is_done = this.is_done,
        date = this.date,
    )
}

