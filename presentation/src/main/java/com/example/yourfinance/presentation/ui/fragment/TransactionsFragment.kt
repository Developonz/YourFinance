package com.example.yourfinance.presentation.ui.fragment

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible // Удобный import для visibility
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.yourfinance.domain.model.Period
import com.example.yourfinance.presentation.databinding.FragmentTransactionsListBinding
import com.example.yourfinance.presentation.ui.adapter.list_item.TransactionListItem
import com.example.yourfinance.presentation.ui.adapter.TransactionsRecyclerViewListAdapter
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.Transaction // Убедитесь, что Transaction импортирован
import com.example.yourfinance.presentation.R
import com.example.yourfinance.util.StringHelper.Companion.getMoneyStr
import java.math.BigDecimal
// Импорты для дат и форматирования
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.IsoFields
import java.util.Locale

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsListBinding? = null
    private val binding get() = _binding!! // Non-null assertion operator for guaranteed access after onCreateView

    // Получаем ViewModel, общую для нескольких фрагментов/Activity
    private val viewModel: GeneralViewModel by activityViewModels()

    // Адаптер для RecyclerView
    private val adapter = TransactionsRecyclerViewListAdapter(editClick = { transaction ->
        // Обработка клика для редактирования транзакции
        val action = TransactionsFragmentDirections.actionTransactionsToTransactionContainerFragment(transaction.id, transaction.type.ordinal)
        findNavController().navigate(action)
    })

    // Форматтеры для отображения дат периода на русском языке
    private val dayMonthYearFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy г.", Locale("ru"))
    private val dayFormatter = DateTimeFormatter.ofPattern("dd")
    private val monthDayFormatter = DateTimeFormatter.ofPattern("d MMMM", Locale("ru")) // Формат "17 мая"
    private val monthYearFormatter = DateTimeFormatter.ofPattern("LLLL yyyy г.", Locale("ru")) // LLLL для полного названия месяца в нужном падеже
    private val yearFormatter = DateTimeFormatter.ofPattern("yyyy г.")
    private val shortDateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy") // Для Custom периода


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        setupClickListeners() // Настройка обработчиков кликов на стрелки
        setupOptionsMenu() // Настройка меню в AppBar
        setupSwipeToDelete(requireContext())
    }

    // Настройка RecyclerView для списка транзакций
    private fun setupRecyclerView() {
        binding.transactionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsList.adapter = adapter
        binding.transactionsList.setHasFixedSize(true) // Оптимизация, если размер элементов не меняется
        binding.transactionsList.itemAnimator = null // Отключаем анимацию для улучшения производительности при частых обновлениях
    }

    // Настройка наблюдателей за LiveData из ViewModel
    private fun setupObservers() {
        // Наблюдатель за списком транзакций
        viewModel.transactionsList.observe(viewLifecycleOwner) { transactionsList ->
            Log.i("TESTDB", "TransactionsFragment: transactionsList observer triggered")

            // Обновление сумм дохода и расхода
            var income = BigDecimal.ZERO
            var expense = BigDecimal.ZERO
            transactionsList.filterIsInstance<Payment>().forEach { payment ->
                if (payment.type == TransactionType.INCOME) {
                    income += payment.balance
                } else {
                    expense += payment.balance
                }
            }
            binding.incomeBalance.text = getMoneyStr(income) // Используем ваш StringHelper
            binding.expenseBalance.text = getMoneyStr(expense)

            // Подготовка списка элементов для RecyclerView (заголовки + транзакции)
            val itemsForAdapter = prepareListItems(transactionsList)
            adapter.submitList(itemsForAdapter) // Передаем обновленный список в адаптер
        }

        // Наблюдатель за выбранным периодом
        viewModel.selectedPeriod.observe(viewLifecycleOwner) { periodSelection ->
            Log.i("TESTDB", "TransactionsFragment: selectedPeriod observer triggered - ${periodSelection.periodType}")
            // Обновляем текст текущего периода в навигационной панели
            binding.textCurrentPeriod.text = formatPeriodForDisplay(periodSelection)

            // Управляем видимостью стрелок навигации по периоду
            val showNavigationArrows = periodSelection.periodType != Period.ALL && periodSelection.periodType != Period.CUSTOM
            binding.buttonPreviousPeriod.isVisible = showNavigationArrows
            binding.buttonNextPeriod.isVisible = showNavigationArrows
        }
    }

    // Функция для подготовки списка элементов (заголовки + транзакции) для RecyclerView
    private fun prepareListItems(transactions: List<Transaction>): List<TransactionListItem> {
        // Группируем транзакции по дате
        val grouped = transactions
            .groupBy { it.date }
            // Сортируем сами группы по дате (от новых к старым)
            .toSortedMap(compareByDescending { it })

        val items = mutableListOf<TransactionListItem>()
        // Добавляем заголовок и элементы для каждой даты
        grouped.forEach { (date, dailyTransactions) ->
            val dailyBalance = dailyTransactions.filterIsInstance<Payment>()
                .sumOf { if (it.type == TransactionType.INCOME) it.balance else -it.balance }
            items.add(TransactionListItem.Header(date, dailyBalance)) // Добавляем заголовок
            dailyTransactions.forEach { transaction ->
                items.add(TransactionListItem.TransactionItem(transaction)) // Добавляем транзакцию
            }
        }
        items.add(TransactionListItem.EmptyItem) // Добавляем пустой элемент в конец для отступа от BottomNav
        return items
    }

    // Настройка обработчиков кликов на кнопки навигации по периоду
    private fun setupClickListeners() {
        binding.buttonPreviousPeriod.setOnClickListener {
            viewModel.previousPeriod() // Вызываем метод ViewModel для перехода к предыдущему периоду
        }
        binding.buttonNextPeriod.setOnClickListener {
            viewModel.nextPeriod() // Вызываем метод ViewModel для перехода к следующему периоду
        }
    }

    // Форматирование отображения выбранного периода для TextView
    private fun formatPeriodForDisplay(periodSelection: PeriodSelection): String {
        val startDate = periodSelection.startDate
        val endDate = periodSelection.endDate

        return when (periodSelection.periodType) {
            Period.DAILY -> startDate?.format(dayMonthYearFormatter) ?: getString(R.string.period_not_selected)
            Period.WEEKLY -> {
                if (startDate != null && endDate != null) {
                    // Если неделя в одном месяце и году
                    if (startDate.month == endDate.month && startDate.year == endDate.year) {
                        "${startDate.format(dayFormatter)}–${endDate.format(dayMonthYearFormatter)}" // Формат "11–17 мая 2025 г."
                    } else if (startDate.year == endDate.year) { // Если в разных месяцах одного года
                        "${startDate.format(monthDayFormatter)} – ${endDate.format(dayMonthYearFormatter)}" // Формат "28 апреля – 4 мая 2025 г."
                    } else { // Если в разных годах
                        "${startDate.format(dayMonthYearFormatter)} – ${endDate.format(dayMonthYearFormatter)}" // Полные даты
                    }
                } else getString(R.string.period_week_not_selected)
            }
            Period.MONTHLY -> startDate?.let { YearMonth.from(it).format(monthYearFormatter) } ?: getString(R.string.period_month_not_selected)
            Period.QUARTERLY -> {
                startDate?.let {
                    val year = it.year
                    // Получаем первый и последний месяц квартала
                    val firstMonthOfQuarter = Month.of(((it.monthValue - 1) / 3) * 3 + 1)
                    val lastMonthOfQuarter = firstMonthOfQuarter.plus(2)
                    // Получаем названия месяцев на русском (в именительном падеже)
                    val firstMonthName = firstMonthOfQuarter.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }
                    val lastMonthName = lastMonthOfQuarter.getDisplayName(TextStyle.FULL_STANDALONE, Locale("ru")).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("ru")) else it.toString() }

                    "$firstMonthName - $lastMonthName $year г." // Формат "Апрель - Июнь 2025 г."

                } ?: getString(R.string.period_quarter_not_selected) // Используйте вашу строку ресурса
            }
            Period.ANNUALLY -> startDate?.format(yearFormatter) ?: getString(R.string.period_year_not_selected)
            Period.ALL -> getString(R.string.period_all_time) // Строка "Все время"
            Period.CUSTOM -> {
                if (startDate != null && endDate != null) {
                    "${startDate.format(shortDateFormatter)} – ${endDate.format(shortDateFormatter)}" // Формат "01.05.2025 - 31.05.2025"
                } else getString(R.string.period_range_not_selected)
            }
        }
    }

    // Настройка Options Menu в AppBar
    private fun setupOptionsMenu() {
        val menuHost: MenuHost = requireActivity() // Получаем хост меню из Activity
        menuHost.addMenuProvider(object : MenuProvider {
            // Создание меню
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.transactions_menu, menu) // Загружаем ваше меню
            }

            // Обработка выбора элемента меню
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    // Если выбран пункт "Выбрать период"
                    R.id.action_select_period -> {
                        // Показываем BottomSheet для выбора периода
                        PeriodSelectionBottomSheetDialogFragment.newInstance().show(
                            childFragmentManager, // Важно использовать childFragmentManager при вызове из фрагмента
                            PeriodSelectionBottomSheetDialogFragment.TAG
                        )
                        true // Сообщаем, что событие обработано
                    }
                    // Другие пункты меню (если будут)
                    // R.id.action_another_item -> { ... true }
                    else -> false // Сообщаем, что событие не обработано этим MenuProvider
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED) // Привязываем к жизненному циклу фрагмента
    }

    private fun setupSwipeToDelete(context: Context) { // Принимаем Context
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            0,
            ItemTouchHelper.LEFT
        ) {
            // --- Настройки для отрисовки фона и текста ---
            private val backgroundPaint = Paint().apply { color = Color.RED } // Красный фон
            private val textPaint = Paint().apply {
                color = Color.WHITE // Белый текст
                textSize = 40f // Размер текста (подберите нужный)
                textAlign = Paint.Align.RIGHT // Выравнивание по правому краю
                isAntiAlias = true
            }
            private val deleteText = context.getString(R.string.delete) // Получаем строку "Удалить" из ресурсов
            private val textPadding = 60f // Отступ текста от правого края фона

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = adapter.currentList[position]
                    if (item is TransactionListItem.TransactionItem) {
                        val transactionToDelete = item.transaction
                        viewModel.deleteTransaction(transactionToDelete)
                        Log.d("SwipeToDelete", "Swiped item at position $position, requesting delete for transaction ${transactionToDelete.id}")
                        Toast.makeText(requireContext(), R.string.transaction_deleted_toast, Toast.LENGTH_SHORT).show()
                    } else {
                        Log.d("SwipeToDelete", "Swiped item at position $position is not a TransactionItem, ignoring.")
                        adapter.notifyItemChanged(position) // Обновляем, чтобы сбросить вид
                    }
                } else {
                    Log.d("SwipeToDelete", "Swiped item at invalid position $position, ignoring.")
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                val position = viewHolder.adapterPosition
                if (position == RecyclerView.NO_POSITION) return 0
                val item = adapter.currentList.getOrNull(position)
                return if (item is TransactionListItem.TransactionItem) {
                    super.getSwipeDirs(recyclerView, viewHolder)
                } else {
                    0
                }
            }

            // --- Переопределение onChildDraw для кастомной отрисовки ---
            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                // Сначала вызываем стандартную отрисовку, чтобы элемент двигался
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                // Рисуем фон и текст только при активном свайпе влево
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE && dX < 0) {
                    val itemView = viewHolder.itemView
                    val backgroundRect = RectF(
                        itemView.right + dX, // Левая граница фона следует за сдвигом
                        itemView.top.toFloat(),
                        itemView.right.toFloat(), // Правая граница фона - изначальная правая граница элемента
                        itemView.bottom.toFloat()
                    )

                    // Рисуем красный фон
                    c.drawRect(backgroundRect, backgroundPaint)

                    // Рассчитываем позицию текста
                    // Вертикально по центру элемента
                    val textY = itemView.top + (itemView.height / 2) - ((textPaint.descent() + textPaint.ascent()) / 2)
                    // Горизонтально - отступ от правого края элемента
                    val textX = itemView.right.toFloat() - textPadding

                    // Рисуем текст "Удалить", только если свайп достаточно большой, чтобы текст поместился
                    if (kotlin.math.abs(dX) > textPadding * 1.5) { // Условие можно настроить
                        c.drawText(deleteText, textX, textY, textPaint)
                    }
                }
            }
            // --- Конец переопределения onChildDraw ---
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(binding.transactionsList)
        Log.d("SwipeToDelete", "ItemTouchHelper attached to RecyclerView")
    }



    // Очистка ссылки на binding при уничтожении View фрагмента
    override fun onDestroyView() {
        super.onDestroyView()
        binding.transactionsList.adapter = null // Отвязываем адаптер от RecyclerView
        _binding = null // Обнуляем binding
    }
}