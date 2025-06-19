package com.example.yourfinance.domain.model.entity

import com.example.yourfinance.domain.model.PeriodLite
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.category.BaseCategory
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.IsoFields
import java.time.temporal.WeekFields
import java.util.Locale

data class Budget(
    private var _title: Title,
    var budgetLimit: BigDecimal,
    var period: PeriodLite,
    val categories: MutableList<BaseCategory> = mutableListOf(),
    val id: Long = 0,

    var spent: BigDecimal = BigDecimal.ZERO,
    var startDate: LocalDate? = null,
    var endDate: LocalDate? = null
) {
    var title: String
        get() = _title.value
        set(value) { _title = Title(value) }


    val remaining: BigDecimal
        get() = budgetLimit - spent

    val progress: Float
        get() = if (budgetLimit > BigDecimal.ZERO) (spent.divide(budgetLimit, 2, RoundingMode.HALF_UP) * BigDecimal("100.00")).toFloat() else 0f

    fun calculateCurrentPeriodDates(referenceDate: LocalDate = LocalDate.now()): Pair<LocalDate, LocalDate> {
        val start: LocalDate
        val end: LocalDate
        when (period) {
            PeriodLite.WEEKLY -> {
                val weekFields = WeekFields.of(Locale.getDefault())
                start = referenceDate.with(weekFields.dayOfWeek(), 1)
                end = start.plusDays(6)
            }
            PeriodLite.MONTHLY -> {
                start = referenceDate.withDayOfMonth(1)
                end = referenceDate.withDayOfMonth(referenceDate.lengthOfMonth())
            }
            PeriodLite.QUARTERLY -> {
                val currentQuarter = referenceDate.get(IsoFields.QUARTER_OF_YEAR)
                val firstMonthOfQuarter = (currentQuarter - 1) * 3 + 1
                start = LocalDate.of(referenceDate.year, firstMonthOfQuarter, 1)
                end = start.plusMonths(2).withDayOfMonth(start.plusMonths(2).lengthOfMonth())
            }
            PeriodLite.ANNUALLY -> {
                start = referenceDate.withDayOfYear(1)
                end = referenceDate.withDayOfYear(referenceDate.lengthOfYear())
            }
        }
        return start to end
    }
}