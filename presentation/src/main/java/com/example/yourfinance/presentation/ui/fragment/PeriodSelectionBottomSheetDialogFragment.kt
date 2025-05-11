package com.example.yourfinance.presentation.ui.fragment // Убедитесь, что это ваш правильный пакет

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.domain.model.Period
import com.example.yourfinance.presentation.databinding.BottomSheetFragmentPeriodSelectionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.datepicker.MaterialDatePicker
import com.example.yourfinance.presentation.ui.adapter.PeriodSelectionAdapter // Ваш адаптер
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset // Важно для UTC манипуляций

class PeriodSelectionBottomSheetDialogFragment : BottomSheetDialogFragment() {

    private var _binding: BottomSheetFragmentPeriodSelectionBinding? = null
    private val binding get() = _binding!!

    private val generalViewModel: GeneralViewModel by activityViewModels()
    private lateinit var periodAdapter: PeriodSelectionAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetFragmentPeriodSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val allPeriods = Period.values().toList()
        // Получаем текущий активный тип периода для инициализации адаптера
        val currentActivePeriodType = generalViewModel.selectedPeriod.value?.periodType ?: Period.WEEKLY // Значение по умолчанию

        periodAdapter = PeriodSelectionAdapter(allPeriods, currentActivePeriodType) { selectedPeriod ->
            if (selectedPeriod == Period.CUSTOM) {
                showDateRangePicker()
                // Не закрываем BottomSheet сразу, ждем выбора дат в пикере
            } else {
                generalViewModel.setPeriod(selectedPeriod)
                dismiss() // Закрыть BottomSheet после выбора стандартного периода
            }
        }
        binding.periodsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = periodAdapter
        }
    }

    private fun observeViewModel() {
        generalViewModel.selectedPeriod.observe(viewLifecycleOwner) { periodSelection ->
            // Обновляем выделение в адаптере, если оно изменилось из ViewModel
            if (::periodAdapter.isInitialized) {
                periodAdapter.updateSelectedPeriod(periodSelection.periodType)
            }
        }
    }

    private fun showDateRangePicker() {
        val currentPeriodSelection = generalViewModel.selectedPeriod.value

        // Готовим пару миллисекунд для .setSelection() MaterialDatePicker
        // Эти миллисекунды должны соответствовать 00:00:00 UTC для выбранных дат
        val selectionPairForPicker: androidx.core.util.Pair<Long, Long>

        if (currentPeriodSelection?.periodType == Period.CUSTOM &&
            currentPeriodSelection.startDate != null &&
            currentPeriodSelection.endDate != null) {
            // Если есть сохраненный пользовательский период, используем его даты
            val startMillisUtc = currentPeriodSelection.startDate
                .atStartOfDay(ZoneOffset.UTC) // LocalDate + 00:00 в UTC
                .toInstant()
                .toEpochMilli()
            val endMillisUtc = currentPeriodSelection.endDate
                .atStartOfDay(ZoneOffset.UTC) // LocalDate + 00:00 в UTC
                .toInstant()
                .toEpochMilli()
            selectionPairForPicker = androidx.core.util.Pair(startMillisUtc, endMillisUtc)
        } else {
            // Значения по умолчанию, если нет сохраненного пользовательского периода
            // или если текущий период не CUSTOM.
            // Установим, например, текущий день для начала и конца диапазона по умолчанию.
            val now = LocalDate.now()
            val defaultStartMillisUtc = now.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            // Для диапазона из одного дня можно установить одинаковые значения.
            // Либо можно установить, например, начало и конец текущего месяца:
            // val firstDayOfMonthUtcMillis = now.withDayOfMonth(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            // val lastDayOfMonthUtcMillis = now.withDayOfMonth(now.lengthOfMonth()).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            // selectionPairForPicker = androidx.core.util.Pair(firstDayOfMonthUtcMillis, lastDayOfMonthUtcMillis)
            selectionPairForPicker = androidx.core.util.Pair(defaultStartMillisUtc, defaultStartMillisUtc)
        }

        val datePicker = MaterialDatePicker.Builder.dateRangePicker()
            .setTitleText("Выберите диапазон дат")
            .setSelection(selectionPairForPicker) // Устанавливаем подготовленные UTC миллисекунды
            .build()

        datePicker.addOnPositiveButtonClickListener { selectedMillisPair ->
            val startDateMillisUtc = selectedMillisPair.first
            val endDateMillisUtc = selectedMillisPair.second

            if (startDateMillisUtc != null && endDateMillisUtc != null) {
                // Конвертируем UTC миллисекунды (представляющие начало дня в UTC)
                // в локальные LocalDate (в системном часовом поясе)
                val startDate = Instant.ofEpochMilli(startDateMillisUtc)
                    .atZone(ZoneId.systemDefault()) // Конвертация в системный часовой пояс
                    .toLocalDate()
                val endDate = Instant.ofEpochMilli(endDateMillisUtc)
                    .atZone(ZoneId.systemDefault()) // Конвертация в системный часовой пояс
                    .toLocalDate()

                generalViewModel.setPeriod(Period.CUSTOM, startDate, endDate)
            }
            dismiss() // Закрыть BottomSheet после выбора дат в пикере
        }

        datePicker.addOnNegativeButtonClickListener {
            // Пользователь нажал "Отмена" в DatePicker.
            // BottomSheet остается открытым, чтобы пользователь мог выбрать другой стандартный период
            // или попробовать еще раз выбрать пользовательский.
            // Если нужно закрывать BottomSheet и здесь, добавьте dismiss().
        }

        datePicker.addOnCancelListener {
            // Пользователь отменил DatePicker (например, свайпом или кнопкой "Назад").
            // Аналогично NegativeButtonClickListener.
        }

        // Показываем DatePicker
        // Используем childFragmentManager, так как DatePicker показывается из другого Fragment (BottomSheetDialogFragment)
        datePicker.show(childFragmentManager, "DATE_RANGE_PICKER_TAG")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Очищаем ссылку на биндинг для предотвращения утечек памяти
    }

    companion object {
        const val TAG = "PeriodSelectionBottomSheet" // TAG для вызова BottomSheet
        fun newInstance(): PeriodSelectionBottomSheetDialogFragment {
            return PeriodSelectionBottomSheetDialogFragment()
        }
    }
}