package com.example.yourfinance.presentation.ui.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.example.yourfinance.domain.model.Period
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.usecase.PieChartSliceData
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentStatisticBinding
import com.example.yourfinance.util.StringHelper
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.math.BigDecimal
import java.text.NumberFormat
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@AndroidEntryPoint
class StatisticFragment : Fragment() {

    private var _binding: FragmentStatisticBinding? = null
    private val binding get() = _binding!!

    private val viewModel: GeneralViewModel by activityViewModels()

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    private val russianLocale = Locale("ru", "RU")

    private val dayMonthYearFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy г.", russianLocale)
    private val dayFormatter = DateTimeFormatter.ofPattern("dd", russianLocale)
    private val monthDayFormatter = DateTimeFormatter.ofPattern("d MMMM", russianLocale)
    private val monthYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy г.", russianLocale)
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy г.", russianLocale)
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", russianLocale)

    private val chartColors = ArrayList<Int>()
    private var currentCenterTextFromClick: CharSequence? = null
    private var defaultCenterTextBuilder: SpannableStringBuilder = SpannableStringBuilder()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatisticBinding.inflate(inflater, container, false)
        initializeChartColors()
        return binding.root
    }

    private fun initializeChartColors() {
        chartColors.clear()
        val tempColors = mutableListOf<Int>()
        tempColors.addAll(ColorTemplate.MATERIAL_COLORS.toList())
        tempColors.addAll(ColorTemplate.JOYFUL_COLORS.toList())
        tempColors.addAll(ColorTemplate.COLORFUL_COLORS.toList())
        tempColors.addAll(ColorTemplate.LIBERTY_COLORS.toList())
        tempColors.addAll(ColorTemplate.PASTEL_COLORS.toList())
        chartColors.addAll(tempColors.distinct())
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPieChartAppearance()
        setupTabs()
        setupClickListeners()
        setupOptionsMenu()
        observeViewModelData()
    }

    private fun setupPieChartAppearance() {
        binding.pieChartStatistic.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            holeRadius = 65f
            transparentCircleRadius = 70f
            setDrawCenterText(true)

            rotationAngle = 0f
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            legend.isEnabled = false
            setDrawEntryLabels(false)

            setNoDataText("")


            setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                override fun onValueSelected(e: Entry?, h: Highlight?) {
                    if (e is PieEntry) {
                        val categoryName = e.label ?: ""
                        val value = e.value
                        val builder = SpannableStringBuilder()
                        val line1 = truncateText(categoryName, 20)
                        builder.append(line1)
                        builder.setSpan(RelativeSizeSpan(1.0f), 0, line1.length, 0)
                        builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_100)), 0, line1.length, 0)
                        builder.append("\n")
                        val line2 = currencyFormatter.format(value)
                        builder.append(line2)
                        builder.setSpan(RelativeSizeSpan(0.8f), builder.length - line2.length, builder.length, 0)
                        builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_100)), builder.length - line2.length, builder.length, 0)

                        currentCenterTextFromClick = builder
                        centerText = currentCenterTextFromClick
                    }
                }
                override fun onNothingSelected() {
                    currentCenterTextFromClick = null
                    centerText = defaultCenterTextBuilder
                }
            })
        }
        binding.tvChartTitleStatistic.setTextColor(if (isNightModeActive()) Color.WHITE else Color.BLACK)
    }

    private fun isNightModeActive(): Boolean {
        val nightModeFlags = requireContext().resources.configuration.uiMode and
                android.content.res.Configuration.UI_MODE_NIGHT_MASK
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES
    }

    private fun setupTabs() {
        viewModel.currentChartDisplayTypeLiveData.value?.let { currentType ->
            val initialTabIndex = if (currentType == TransactionType.INCOME) 1 else 0
            if (binding.tabLayoutStatistic.selectedTabPosition != initialTabIndex) {
                binding.tabLayoutStatistic.getTabAt(initialTabIndex)?.select()
            }
            updateChartTitle(currentType)
        }

        binding.tabLayoutStatistic.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedType = when (tab?.position) {
                    0 -> TransactionType.EXPENSE
                    1 -> TransactionType.INCOME
                    else -> TransactionType.EXPENSE
                }
                viewModel.setChartDisplayType(selectedType)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun updateChartTitle(type: TransactionType) {
        binding.tvChartTitleStatistic.text = if (type == TransactionType.EXPENSE) {
            getString(R.string.chart_title_expenses)
        } else {
            getString(R.string.chart_title_incomes)
        }
    }

    private fun setupClickListeners() {
        binding.buttonPreviousPeriodStatistic.setOnClickListener {
            viewModel.previousPeriod()
        }
        binding.buttonNextPeriodStatistic.setOnClickListener {
            viewModel.nextPeriod()
        }
    }

    private fun setupOptionsMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.statistic_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_select_period -> {
                        PeriodSelectionBottomSheetDialogFragment.newInstance().show(
                            childFragmentManager,
                            PeriodSelectionBottomSheetDialogFragment.TAG
                        )
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun observeViewModelData() {
        viewModel.selectedPeriod.observe(viewLifecycleOwner) { periodSelection ->
            binding.textCurrentPeriodStatistic.text = formatPeriodForDisplay(periodSelection)
            val showNavigationArrows = periodSelection.periodType != Period.ALL && periodSelection.periodType != Period.CUSTOM
            binding.buttonPreviousPeriodStatistic.isVisible = showNavigationArrows
            binding.buttonNextPeriodStatistic.isVisible = showNavigationArrows
        }

        viewModel.periodBalances.observe(viewLifecycleOwner) { balances ->
            balances?.let {
                binding.tvStartBalanceStatistic.text = currencyFormatter.format(it.startBalance)
                binding.tvEndBalanceStatistic.text = currencyFormatter.format(it.endBalance)
            } ?: run {
                binding.tvStartBalanceStatistic.text = currencyFormatter.format(0.0)
                binding.tvEndBalanceStatistic.text = currencyFormatter.format(0.0)
            }
        }

        viewModel.transactionsList.observe(viewLifecycleOwner) { transactions ->
            var incomeSum = BigDecimal.ZERO
            var expenseSum = BigDecimal.ZERO
            transactions.filterIsInstance<Payment>().forEach { payment ->
                if (payment.type == TransactionType.INCOME) {
                    incomeSum += payment.balance
                } else {
                    expenseSum += payment.balance
                }
            }
            binding.incomeBalanceStatistic.text = StringHelper.getMoneyStr(incomeSum)
            binding.expenseBalanceStatistic.text = StringHelper.getMoneyStr(expenseSum)
        }


        viewModel.pieChartData.observe(viewLifecycleOwner) { pieSlices ->
            val currentDisplayType = viewModel.currentChartDisplayTypeLiveData.value ?: TransactionType.EXPENSE
            updatePieChartDisplay(pieSlices, currentDisplayType)
            updateChartTitle(currentDisplayType)
        }

        viewModel.currentChartDisplayTypeLiveData.observe(viewLifecycleOwner) { type ->
            val currentTabIndex = binding.tabLayoutStatistic.selectedTabPosition
            val newTabIndex = if (type == TransactionType.INCOME) 1 else 0
            if (currentTabIndex != newTabIndex) {
                binding.tabLayoutStatistic.getTabAt(newTabIndex)?.select()
            }
            viewModel.pieChartData.value?.let { currentData ->
                updatePieChartDisplay(currentData, type)
            } ?: updatePieChartDisplay(null, type)
            updateChartTitle(type)
        }
    }

    private fun calculateTotalForTypeWhenNoSlices(type: TransactionType): BigDecimal {
        val transactions = viewModel.transactionsList.value ?: return BigDecimal.ZERO
        return transactions
            .filterIsInstance<Payment>()
            .filter { it.type == type }
            .sumOf { it.balance }
    }


    private fun updatePieChartDisplay(
        pieSlices: List<PieChartSliceData>?,
        type: TransactionType
    ) {
        val chart = binding.pieChartStatistic
        binding.legendContainerStatistic.removeAllViews()

        val centerTextLabelBase = if (type == TransactionType.EXPENSE) getString(R.string.expenses_tab_label) else getString(R.string.incomes_tab_label)
        defaultCenterTextBuilder = SpannableStringBuilder()

        if (pieSlices == null || pieSlices.isEmpty()) {
            binding.tvNoDataMessageStatistic.isVisible = true
            binding.chartAndLegendContainer.isVisible = false

            val totalForType = calculateTotalForTypeWhenNoSlices(type)
            val line1Default = truncateText(centerTextLabelBase, 20)
            defaultCenterTextBuilder.append(line1Default)
            defaultCenterTextBuilder.setSpan(RelativeSizeSpan(1.0f), 0, line1Default.length, 0)
            defaultCenterTextBuilder.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_100)), 0, line1Default.length, 0)
            defaultCenterTextBuilder.append("\n")
            val line2Default = currencyFormatter.format(totalForType)
            defaultCenterTextBuilder.append(line2Default)
            defaultCenterTextBuilder.setSpan(RelativeSizeSpan(0.8f), defaultCenterTextBuilder.length - line2Default.length, defaultCenterTextBuilder.length, 0)
            defaultCenterTextBuilder.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_100)), defaultCenterTextBuilder.length - line2Default.length, defaultCenterTextBuilder.length, 0)


            if (currentCenterTextFromClick == null) {
                chart.centerText = defaultCenterTextBuilder
            }
            chart.data = null
            chart.invalidate()
            return
        }

        binding.tvNoDataMessageStatistic.isVisible = false
        binding.chartAndLegendContainer.isVisible = true

        val entries = ArrayList<PieEntry>()
        val totalAmountForTypeAndPeriod = pieSlices.firstOrNull()?.totalAmountForPeriod ?: 0.0
        val sliceColors = chartColors.take(pieSlices.size)

        pieSlices.forEachIndexed { index, slice ->
            entries.add(PieEntry(slice.amount.toFloat(), slice.categoryName))

            val legendItemView = LayoutInflater.from(context).inflate(R.layout.legend_item_statistic, binding.legendContainerStatistic, false)
            val colorMarker = legendItemView.findViewById<ImageView>(R.id.legend_item_color_marker)
            val categoryNameTv = legendItemView.findViewById<TextView>(R.id.legend_item_category_name)
            val categoryPercentageTv = legendItemView.findViewById<TextView>(R.id.legend_item_category_percentage)

            val markerColor = sliceColors.getOrElse(index) { Color.LTGRAY }
            val wrappedDrawable = DrawableCompat.wrap(ContextCompat.getDrawable(requireContext(), R.drawable.legend_marker_circle)!!.mutate())
            DrawableCompat.setTint(wrappedDrawable, markerColor)
            colorMarker.setImageDrawable(wrappedDrawable)

            categoryNameTv.text = slice.categoryName
            categoryNameTv.setTextColor(if (isNightModeActive()) Color.WHITE else Color.DKGRAY)
            categoryPercentageTv.text = String.format(russianLocale, "(%.1f%%)", slice.percentage)
            categoryPercentageTv.setTextColor(if (isNightModeActive()) Color.LTGRAY else Color.GRAY)

            binding.legendContainerStatistic.addView(legendItemView)
        }

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawIcons(false)
        dataSet.sliceSpace = 2f
        dataSet.iconsOffset = MPPointF(0f, 40f)
        dataSet.selectionShift = 8f
        dataSet.colors = sliceColors

        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 10f
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueFormatter = PercentFormatter(chart)
        dataSet.yValuePosition = PieDataSet.ValuePosition.INSIDE_SLICE
        dataSet.setValueLineColor(Color.TRANSPARENT)


        val data = PieData(dataSet)
        chart.data = data
        chart.highlightValues(null)


        val line1Total = truncateText(centerTextLabelBase, 20)
        defaultCenterTextBuilder.append(line1Total)
        defaultCenterTextBuilder.setSpan(RelativeSizeSpan(1.0f), 0, line1Total.length, 0)
        defaultCenterTextBuilder.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_100)), 0, line1Total.length, 0)
        defaultCenterTextBuilder.append("\n")
        val line2Total = currencyFormatter.format(totalAmountForTypeAndPeriod)
        defaultCenterTextBuilder.append(line2Total)
        defaultCenterTextBuilder.setSpan(RelativeSizeSpan(0.8f), defaultCenterTextBuilder.length - line2Total.length, defaultCenterTextBuilder.length, 0)
        defaultCenterTextBuilder.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.grey_100)), defaultCenterTextBuilder.length - line2Total.length, defaultCenterTextBuilder.length, 0)


        if (currentCenterTextFromClick == null) {
            chart.centerText = defaultCenterTextBuilder
        }

        chart.invalidate()
        chart.animateY(1000, Easing.EaseInOutQuad)
    }

    private fun truncateText(text: String, maxLength: Int): String {
        return if (text.length > maxLength) {
            text.substring(0, maxLength - 3) + "..."
        } else {
            text
        }
    }

    private fun formatPeriodForDisplay(periodSelection: PeriodSelection): String {
        val startDate = periodSelection.startDate
        val endDate = periodSelection.endDate

        return when (periodSelection.periodType) {
            Period.DAILY -> startDate?.format(dayMonthYearFormatter) ?: getString(R.string.period_not_selected)
            Period.WEEKLY -> {
                if (startDate != null && endDate != null) {
                    if (startDate.month == endDate.month && startDate.year == endDate.year) {
                        "${startDate.format(dayFormatter)}–${endDate.format(dayMonthYearFormatter)}"
                    } else if (startDate.year == endDate.year) {
                        "${startDate.format(monthDayFormatter)} – ${endDate.format(dayMonthYearFormatter)}"
                    } else {
                        "${startDate.format(dayMonthYearFormatter)} – ${endDate.format(dayMonthYearFormatter)}"
                    }
                } else getString(R.string.period_week_not_selected)
            }
            Period.MONTHLY -> startDate?.let { YearMonth.from(it).format(monthYearFormatter) } ?: getString(R.string.period_month_not_selected)
            Period.QUARTERLY -> {
                startDate?.let {
                    val year = it.year
                    val firstMonthOfQuarter = Month.of(((it.monthValue - 1) / 3) * 3 + 1)
                    val lastMonthOfQuarter = firstMonthOfQuarter.plus(2)
                    val firstMonthName = firstMonthOfQuarter.getDisplayName(TextStyle.FULL_STANDALONE, russianLocale).replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(russianLocale) else char.toString() }
                    val lastMonthName = lastMonthOfQuarter.getDisplayName(TextStyle.FULL_STANDALONE, russianLocale).replaceFirstChar { char -> if (char.isLowerCase()) char.titlecase(russianLocale) else char.toString() }
                    "$firstMonthName - $lastMonthName $year г."
                } ?: getString(R.string.period_quarter_not_selected)
            }
            Period.ANNUALLY -> startDate?.format(yearFormatter) ?: getString(R.string.period_year_not_selected)
            Period.ALL -> getString(R.string.period_all_time)
            Period.CUSTOM -> {
                if (startDate != null && endDate != null) {
                    "${startDate.format(shortDateFormatter)} – ${endDate.format(shortDateFormatter)}"
                } else getString(R.string.period_range_not_selected)
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        binding.pieChartStatistic.data?.clearValues()
        binding.pieChartStatistic.clear()
        _binding = null
    }
}