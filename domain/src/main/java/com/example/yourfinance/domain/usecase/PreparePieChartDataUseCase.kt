package com.example.yourfinance.domain.usecase

import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.Payment
import javax.inject.Inject

data class PieChartSliceData(
    val categoryName: String,
    val amount: Double,
    val percentage: Double,
    val totalAmountForPeriod: Double
)


class PreparePieChartDataUseCase @Inject constructor() {

    fun execute(
        transactions: List<Transaction>,
        targetType: TransactionType,
        topN: Int = 5
    ): List<PieChartSliceData> {
        val relevantPayments = transactions
            .filterIsInstance<Payment>()
            .filter { it.type == targetType }

        if (relevantPayments.isEmpty()) {
            return emptyList()
        }

        val categorySums = relevantPayments
            .groupBy { it.category.title }
            .mapValues { entry -> entry.value.sumOf { payment -> payment.balance } }

        val sortedCategories = categorySums.toList().sortedByDescending { it.second }
        val pieSlices = mutableListOf<PieChartSliceData>()
        val totalAmountForTypeAndPeriod = relevantPayments.sumOf { it.balance }

        if (totalAmountForTypeAndPeriod == 0.0) {
            return emptyList()
        }

        val topCategories = sortedCategories.take(topN)
        topCategories.forEach { (categoryName, sum) ->
            pieSlices.add(
                PieChartSliceData(
                    categoryName = categoryName,
                    amount = sum,
                    percentage = (sum / totalAmountForTypeAndPeriod) * 100.0,
                    totalAmountForPeriod = totalAmountForTypeAndPeriod
                )
            )
        }

        if (sortedCategories.size > topN) {
            val otherSum = sortedCategories.drop(topN).sumOf { it.second }
            if (otherSum > 0.001) { // Небольшой порог для суммы "Другое"
                pieSlices.add(
                    PieChartSliceData(
                        categoryName = "Другое",
                        amount = otherSum,
                        percentage = (otherSum / totalAmountForTypeAndPeriod) * 100.0,
                        totalAmountForPeriod = totalAmountForTypeAndPeriod
                    )
                )
            }
        }
        return pieSlices
    }
}