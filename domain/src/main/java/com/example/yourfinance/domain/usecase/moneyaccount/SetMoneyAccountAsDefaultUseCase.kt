package com.example.yourfinance.domain.usecase.moneyaccount

import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import javax.inject.Inject

class SetMoneyAccountAsDefaultUseCase @Inject constructor(private val accountRepository: MoneyAccountRepository) {
    suspend operator fun invoke(accountId: Long) {
        accountRepository.setDefaultAccount(accountId = accountId)
    }
}