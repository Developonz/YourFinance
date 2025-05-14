package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.MoneyAccount

fun MoneyAccountEntity.toDomain(): MoneyAccount {
    return MoneyAccount(
        startBalance = this.startBalance,
        balance = this.balance,
        excluded = this.excluded,
        _title = Title(this.title),
        default = this.default,
        used = this.used,
        dateCreation = this.dateCreation,
        iconResourceId = this.iconResourceId,
        colorHex = this.colorHex,
        id = this.id
    )
}

fun MoneyAccount.toData(): MoneyAccountEntity {
    return MoneyAccountEntity(
        id = this.id,
        startBalance = this.startBalance,
        balance = this.balance,
        excluded = this.excluded,
        title = this.title,
        default = this.default,
        used = this.used,
        iconResourceId = this.iconResourceId,
        colorHex = this.colorHex,
        dateCreation = this.dateCreation
    )
}