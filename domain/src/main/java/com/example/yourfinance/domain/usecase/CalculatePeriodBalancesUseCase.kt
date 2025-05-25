package com.example.yourfinance.domain.usecase

import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.repository.TransactionRepository
import java.time.LocalDate
import javax.inject.Inject

data class PeriodBalances(
    val startBalance: Double,
    val endBalance: Double,
    val netChange: Double
)

class CalculatePeriodBalancesUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    suspend fun execute(periodStartDate: LocalDate?,
                        periodEndDate: LocalDate?,
                        allAccounts: List<MoneyAccount>): PeriodBalances {
        val includedAccounts = allAccounts.filter { !it.excluded }
        val excludedAccountIds = allAccounts.filter { it.excluded }.map { it.id }

        var sumOfInitialBalancesOfIncludedAccounts = 0.0
        var balanceChangesBeforePeriodStart = 0.0

        if (periodStartDate == null) { // Период "Всего времени"
            sumOfInitialBalancesOfIncludedAccounts = includedAccounts.sumOf { it.startBalance }
            balanceChangesBeforePeriodStart = 0.0
        } else {
            sumOfInitialBalancesOfIncludedAccounts = includedAccounts
                .filter { it.dateCreation < periodStartDate }
                .sumOf { it.startBalance }

            balanceChangesBeforePeriodStart = transactionRepository.getBalanceBeforeDate(periodStartDate, excludedAccountIds)
        }

        val startBalance = sumOfInitialBalancesOfIncludedAccounts + balanceChangesBeforePeriodStart

        val netChangeDuringPeriod = transactionRepository.getNetChangeBetweenDates(
            periodStartDate,
            periodEndDate,
            excludedAccountIds)
        val endBalance = startBalance + netChangeDuringPeriod + includedAccounts
            .filter { it.dateCreation >= periodStartDate && it.dateCreation <= periodEndDate}
            .sumOf { it.startBalance }

        return PeriodBalances(
            startBalance = startBalance,
            endBalance = endBalance,
            netChange = netChangeDuringPeriod
        )
    }
}