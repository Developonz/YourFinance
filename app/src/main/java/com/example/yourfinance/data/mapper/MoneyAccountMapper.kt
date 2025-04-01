package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.domain.model.entity.MoneyAccount

fun MoneyAccountEntity.toDomain(): MoneyAccount {
    return MoneyAccount(
        balance = this.balance,
        excluded = this.excluded,
        _title = this.title,
        default = this.default,
        used = this.used,
        dateCreation = this.dateCreation,
        id = this.id
    )
}

fun MoneyAccount.toData(): MoneyAccountEntity {
    return MoneyAccountEntity(
        balance = this.balance,
        excluded = this.excluded,
        title = this.title,
        default = this.default,
        used = this.used,
        dateCreation = this.dateCreation
    )
}