package com.example.yourfinance.presentation.ui.adapter.list_item

import com.example.yourfinance.domain.model.entity.MoneyAccount

sealed class AccountListItem {
    class Account(val account: MoneyAccount) : AccountListItem()

    data object Empty : AccountListItem()

    data object NewAccount : AccountListItem()
}