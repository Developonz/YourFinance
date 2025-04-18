package com.example.yourfinance.domain.usecase.moneyaccount

import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import javax.inject.Inject

class LoadMoneyAccountByIdUseCase @Inject constructor(private val accountRepository: MoneyAccountRepository) {
    suspend operator fun invoke(id: Long) : MoneyAccount? {
        return accountRepository.getAccountById(id)
    }
}