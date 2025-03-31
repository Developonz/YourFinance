package com.example.yourfinance.data.mapper


import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.pojo.FullPayment
import com.example.yourfinance.domain.model.entity.Payment

fun FullPayment.toDomain(): Payment {
    return Payment(
        id = this.payment.id,
        type = this.payment.type,
        balance = this.payment.balance,
        moneyAccount = this.moneyAcc.toDomain(),
        category = this.category.toDomain(),
        _note = this.payment.note,
        date = this.payment.date,
        time = this.payment.time
    )
}

fun Payment.toData(): PaymentEntity {
    return PaymentEntity(
        type = this.type,
        balance = this.balance,
        moneyAccID = this.moneyAccount.id,
        categoryID = this.category.id,
        note = this.note,
        date = this.date,
        time = this.time
    )
}