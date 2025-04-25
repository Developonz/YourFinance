package com.example.yourfinance.domain.usecase.moneyaccount

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import javax.inject.Inject

class FetchMoneyAccountsUseCase @Inject constructor(private val moneyAccountRepository: MoneyAccountRepository) {
    operator fun invoke() : LiveData<List<MoneyAccount>> {
        return moneyAccountRepository.getAllAccounts()
    }
}