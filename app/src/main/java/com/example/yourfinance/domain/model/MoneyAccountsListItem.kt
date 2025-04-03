package com.example.yourfinance.domain.model

import com.example.yourfinance.domain.model.entity.MoneyAccount

sealed class MoneyAccountsListItem {
    object Header : MoneyAccountsListItem()
    data class AccountItem(val acc: MoneyAccount) : MoneyAccountsListItem()
}