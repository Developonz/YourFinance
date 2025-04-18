package com.example.yourfinance.presentation.ui.fragment.manager

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yourfinance.R
import com.example.yourfinance.databinding.FragmentTransactionAddBinding
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.presentation.ui.adapter.CategoryTransactionAdapter
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel
import com.google.android.material.button.MaterialButton
import dagger.hilt.android.AndroidEntryPoint
// import java.text.NumberFormat // Не используем для установки текста, только для форматирования вывода (если нужно)
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@AndroidEntryPoint
class TransactionAddFragment : Fragment(), CategoryTransactionAdapter.OnItemClickListener {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!

    // Общий ViewModel для всех страниц ViewPager
    private val viewModel: TransactionsViewModel by activityViewModels()

    // Адаптер для категорий (только для страниц Расход/Доход)
    private lateinit var categoryAdapter: CategoryTransactionAdapter

    // Форматтеры и ссылка на кнопку даты
    private val dateButtonFormatterSameYear = DateTimeFormatter.ofPattern("d MMM.", Locale("ru"))
    private val dateButtonFormatterDifferentYear = DateTimeFormatter.ofPattern("d MMM.\nyyyy", Locale("ru"))
    private lateinit var dateButton: MaterialButton

    // Тип транзакции, за который отвечает ЭТОТ экземпляр фрагмента
    private lateinit var instanceTransactionType: TransactionType

    // --- Companion object для передачи аргументов ---
    companion object {
        private const val ARG_TRANSACTION_TYPE = "transaction_type"

        fun newBundle(transactionType: TransactionType): Bundle {
            // Убедись, что TransactionType реализует Serializable
            return Bundle().apply {
                putSerializable(ARG_TRANSACTION_TYPE, transactionType)
            }
        }
    }

    // --- Lifecycle ---
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Получаем тип из аргументов до создания View
        arguments?.let {
            instanceTransactionType = it.getSerializable(ARG_TRANSACTION_TYPE) as? TransactionType
                ?: throw IllegalStateException("TransactionType argument is missing or invalid")
        } ?: throw IllegalStateException("Arguments bundle is null for TransactionAddFragment")
        Log.i("Frag(${instanceTransactionType.name})", "onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("Frag(${instanceTransactionType.name})", "onCreateView")
        _binding = FragmentTransactionAddBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("Frag(${instanceTransactionType.name})", "onViewCreated")

        // Получаем ссылку на кнопку даты внутри GridLayout
        dateButton = binding.keypad.findViewById(R.id.date_button)

        // Настраиваем RecyclerView, если это страница Расход/Доход
        if (instanceTransactionType != TransactionType.REMITTANCE) {
            setupCategoryRecyclerView()
        }

        // Настраиваем обработчики кликов
        setupClickListeners()

        // Начинаем наблюдение за ViewModel
        observeViewModel()

        // Устанавливаем начальную видимость элементов для этого типа фрагмента
        setupInitialUiVisibility()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Frag(${instanceTransactionType.name})", "onDestroyView")
        // Очищаем binding, чтобы избежать утечек памяти
        _binding = null
    }

    // --- Setup ---
    private fun setupCategoryRecyclerView() {
        Log.d("Frag(${instanceTransactionType.name})", "Setting up Category RecyclerView...")
        categoryAdapter = CategoryTransactionAdapter(this) // Передаем себя как слушателя
        binding.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4) // Сетка 4 колонки
            adapter = categoryAdapter
            itemAnimator = null // Отключаем анимации для простоты
        }
    }

    private fun setupClickListeners() {
        Log.d("Frag(${instanceTransactionType.name})", "Setting up Click Listeners...")
        // --- Клавиатура ---
        binding.keypad.findViewById<View>(R.id.key_0).setOnClickListener { viewModel.handleKeypadInput("0") }
        binding.keypad.findViewById<View>(R.id.key_1).setOnClickListener { viewModel.handleKeypadInput("1") }
        binding.keypad.findViewById<View>(R.id.key_2).setOnClickListener { viewModel.handleKeypadInput("2") }
        binding.keypad.findViewById<View>(R.id.key_3).setOnClickListener { viewModel.handleKeypadInput("3") }
        binding.keypad.findViewById<View>(R.id.key_4).setOnClickListener { viewModel.handleKeypadInput("4") }
        binding.keypad.findViewById<View>(R.id.key_5).setOnClickListener { viewModel.handleKeypadInput("5") }
        binding.keypad.findViewById<View>(R.id.key_6).setOnClickListener { viewModel.handleKeypadInput("6") }
        binding.keypad.findViewById<View>(R.id.key_7).setOnClickListener { viewModel.handleKeypadInput("7") }
        binding.keypad.findViewById<View>(R.id.key_8).setOnClickListener { viewModel.handleKeypadInput("8") }
        binding.keypad.findViewById<View>(R.id.key_9).setOnClickListener { viewModel.handleKeypadInput("9") }
        binding.keypad.findViewById<View>(R.id.key_dot).setOnClickListener { viewModel.handleKeypadInput(".") }
        binding.keypad.findViewById<View>(R.id.key_del).setOnClickListener { viewModel.handleKeypadInput("DEL") }
        binding.keypad.findViewById<View>(R.id.key_confirm).setOnClickListener {
            Log.d("Frag(${instanceTransactionType.name})", "Confirm button clicked")
            viewModel.saveTransaction() // ViewModel выполнит валидацию
        }
        // TODO: Добавить обработчики для key_plus и key_minus, если нужно
        // binding.keypad.findViewById<View>(R.id.key_plus).setOnClickListener { /* ... */ }
        // binding.keypad.findViewById<View>(R.id.key_minus).setOnClickListener { /* ... */ }

        // --- Кнопка даты ---
        dateButton.setOnClickListener { showDatePicker() }

        // --- Примечание ---
        binding.noteEditText.addTextChangedListener { editable -> viewModel.setNote(editable.toString()) }

        // --- Выбор счетов для перевода (только для страницы Перевод) ---
        if (instanceTransactionType == TransactionType.REMITTANCE) {
            binding.cardAccountFrom.setOnClickListener { showAccountSelectionDialog(true) }
            binding.cardAccountTo.setOnClickListener { showAccountSelectionDialog(false) }
        }

        // --- Клик по иконке счета/категории в amountDisplayLayout (для Р/Д) ---
        binding.selectedItemIcon.setOnClickListener {
            // Вызываем диалог выбора счета, только если АКТИВНЫЙ тип (в ViewModel) - Расход/Доход
            val activeType = viewModel.currentTransactionType.value
            if (activeType == TransactionType.EXPENSE || activeType == TransactionType.INCOME) {
                Log.d("Frag(${instanceTransactionType.name})", "Selected item icon clicked (type $activeType), showing payment account selection dialog.")
                showPaymentAccountSelectionDialog()
            } else {
                Log.w("Frag(${instanceTransactionType.name})", "Selected item icon clicked, but current VM type is $activeType. Doing nothing.")
            }
        }
    }

    // Устанавливает начальную видимость элементов при создании/пересоздании View
    private fun setupInitialUiVisibility() {
        val isTransferFragment = instanceTransactionType == TransactionType.REMITTANCE
        Log.d("Frag(${instanceTransactionType.name})", "Setting initial UI visibility. Is Transfer Fragment: $isTransferFragment")
        // Показываем RecyclerView ИЛИ блок выбора счетов
        binding.categoryRecyclerView.isVisible = !isTransferFragment
        binding.transferAccountSelector.isVisible = isTransferFragment
        // Нижняя часть (клавиатура, сумма, заметка) изначально скрыта
        binding.amountDisplayLayout.isVisible = false
        binding.noteLayout.isVisible = false
        binding.keypad.isVisible = false
    }

    // --- Observation ---
    private fun observeViewModel() {
        Log.d("Frag(${instanceTransactionType.name})", "Setting up observers.")

        // Наблюдаем за АКТИВНЫМ типом транзакции в ViewModel
        // Это влияет на поведение (например, saveTransaction) и отображение amountDisplayLayout
        viewModel.currentTransactionType.observe(viewLifecycleOwner) { activeType ->
            Log.d("Frag(${instanceTransactionType.name})", "Observed active TransactionType change in VM: $activeType")
            // Обновляем вид amountDisplayLayout, если он видим
            if (binding.amountDisplayLayout.isVisible) {
                updateAmountDisplayLayout()
            }
            // Также может потребоваться обновить состояние кнопок или другие элементы,
            // зависящие от глобально активного типа.
        }

        // Наблюдаем за ВСЕМИ категориями из ViewModel
        viewModel.allCategories.observe(viewLifecycleOwner) { allFullCategories ->
            // Фильтруем и отображаем только если это фрагмент Расход/Доход
            if (instanceTransactionType == TransactionType.EXPENSE || instanceTransactionType == TransactionType.INCOME) {
                Log.d("Frag(${instanceTransactionType.name})", "Observed allCategories update (${allFullCategories?.size ?: 0} items). Filtering for type: $instanceTransactionType")
                val expectedCategoryType = if (instanceTransactionType == TransactionType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
                val relevantCategories = allFullCategories
                    ?.filter { it.category.categoryType == expectedCategoryType }
                    ?.map { it.category }
                    ?: emptyList()

                // Проверяем, инициализирован ли адаптер (на случай быстрых пересозданий)
                if (::categoryAdapter.isInitialized) {
                    categoryAdapter.submitList(relevantCategories)
                    Log.d("Frag(${instanceTransactionType.name})", "Filtered and submitted ${relevantCategories.size} categories.")
                } else {
                    Log.w("Frag(${instanceTransactionType.name})", "CategoryAdapter not initialized when allCategories updated.")
                }
            }
            // Для фрагмента Перевода этот наблюдатель ничего не делает с RecyclerView
        }

        // Наблюдаем за флагом показа секции ввода (управляется выбором категории или счетов)
        viewModel.showInputSection.observe(viewLifecycleOwner) { shouldShowInput ->
            Log.i("Frag(${instanceTransactionType.name})", "Observed showInputSection change in VM: $shouldShowInput")
            // Показываем/скрываем клавиатуру и т.д. только если ЭТОТ фрагмент сейчас АКТИВЕН
            val isActiveFragment = viewModel.currentTransactionType.value == instanceTransactionType
            Log.d("Frag(${instanceTransactionType.name})", "Is this fragment active? $isActiveFragment")

            if (isActiveFragment) {
                Log.d("Frag(${instanceTransactionType.name})", "Updating input section visibility to: $shouldShowInput")
                binding.amountDisplayLayout.isVisible = shouldShowInput
                binding.noteLayout.isVisible = shouldShowInput
                binding.keypad.isVisible = shouldShowInput
                // Обновляем содержимое amountDisplayLayout, если секция показана
                if (shouldShowInput) {
                    updateAmountDisplayLayout()
                }
            } else {
                // Если фрагмент не активен, принудительно скрываем секцию ввода
                if (binding.amountDisplayLayout.isVisible || binding.noteLayout.isVisible || binding.keypad.isVisible) {
                    Log.d("Frag(${instanceTransactionType.name})", "This fragment is INACTIVE, hiding input section.")
                    binding.amountDisplayLayout.isVisible = false
                    binding.noteLayout.isVisible = false
                    binding.keypad.isVisible = false
                }
            }
        }

        // Наблюдаем за выбранными элементами для обновления amountDisplayLayout и карточек перевода
        viewModel.selectedCategory.observe(viewLifecycleOwner) {
            if (binding.amountDisplayLayout.isVisible) updateAmountDisplayLayout()
        }
        viewModel.selectedPaymentAccount.observe(viewLifecycleOwner) {
            Log.d("Frag(${instanceTransactionType.name})", "Observed selectedPaymentAccount change in VM: ${it?.title}")
            if (binding.amountDisplayLayout.isVisible) updateAmountDisplayLayout()
        }
        viewModel.selectedAccountFrom.observe(viewLifecycleOwner) { account ->
            Log.d("Frag(${instanceTransactionType.name})", "Observed selectedAccountFrom change in VM: ${account?.title}")
            // Обновляем текст на карточке (только для фрагмента Перевода)
            if (instanceTransactionType == TransactionType.REMITTANCE) {
                binding.textAccountFrom.text = account?.title ?: "Счет списания"
                // TODO: Update icon binding.imageAccountFrom
                binding.imageAccountFrom.setImageResource(R.drawable.ic_mobile_wallet) // Placeholder
            }
            // Обновляем amountDisplayLayout, если он видим и активен перевод
            if (binding.amountDisplayLayout.isVisible && viewModel.currentTransactionType.value == TransactionType.REMITTANCE) {
                updateAmountDisplayLayout()
            }
        }
        viewModel.selectedAccountTo.observe(viewLifecycleOwner) { account ->
            Log.d("Frag(${instanceTransactionType.name})", "Observed selectedAccountTo change in VM: ${account?.title}")
            // Обновляем текст на карточке (только для фрагмента Перевода)
            if (instanceTransactionType == TransactionType.REMITTANCE) {
                binding.textAccountTo.text = account?.title ?: "Счет зачисления"
                // TODO: Update icon binding.imageAccountTo
                binding.imageAccountTo.setImageResource(R.drawable.ic_mobile_wallet) // Placeholder
            }
            // Обновляем amountDisplayLayout, если он видим и активен перевод
            if (binding.amountDisplayLayout.isVisible && viewModel.currentTransactionType.value == TransactionType.REMITTANCE) {
                updateAmountDisplayLayout()
            }
        }

        // Наблюдаем за суммой, датой, заметкой
        viewModel.amountString.observe(viewLifecycleOwner) { amountStr ->
            binding.amountTextView.text = formatAmountForDisplay(amountStr ?: "0")
        }
        viewModel.date.observe(viewLifecycleOwner) { date -> date?.let { updateDateButtonText(it) } }
        viewModel.note.observe(viewLifecycleOwner) { note ->
            // Обновляем поле ввода, только если текст действительно отличается
            if (binding.noteEditText.text.toString() != note) {
                binding.noteEditText.setText(note ?: "")
            }
        }

        // Наблюдаем за событиями сохранения и ошибок
        viewModel.transactionSavedEvent.observe(viewLifecycleOwner) { saved ->
            // Проверяем, что событие не null и true
            if (saved == true) {
                Log.i("Frag(${instanceTransactionType.name})", "Observed transactionSavedEvent = true")
                // Показываем Toast и закрываем экран (любой экземпляр может это сделать)
                Toast.makeText(requireContext(), "Транзакция сохранена", Toast.LENGTH_SHORT).show()
                viewModel.resetSavedEvent() // Сбрасываем событие в ViewModel
                // Используем findNavController() из самого фрагмента для навигации назад
                findNavController().popBackStack()
            }
        }
        viewModel.errorMessageEvent.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Log.w("Frag(${instanceTransactionType.name})", "Observed errorMessageEvent: $it")
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.resetErrorEvent() // Сбрасываем событие ошибки
            }
        }
    }

    // --- UI Updates & Dialogs ---

    // Обновляет иконку и текст в amountDisplayLayout
    private fun updateAmountDisplayLayout() {
        // Убедимся, что View еще существует (важно при асинхронных обновлениях)
        if (_binding == null) {
            Log.w("Frag(${instanceTransactionType.name})", "updateAmountDisplayLayout called but binding is null!")
            return
        }

        val activeType = viewModel.currentTransactionType.value // Тип АКТИВНОЙ страницы
        val iconView = binding.selectedItemIcon
        val amountTextView = binding.amountTextView

        Log.d("Frag(${instanceTransactionType.name})", "Updating AmountDisplayLayout. Active VM type: $activeType")

        when (activeType) {
            TransactionType.REMITTANCE -> {
                iconView.setImageResource(R.drawable.ic_mobile_wallet) // Иконка перевода
                iconView.isClickable = false // Нельзя кликнуть
            }
            TransactionType.EXPENSE, TransactionType.INCOME -> {
                val selectedAccount = viewModel.selectedPaymentAccount.value
                val selectedCategory = viewModel.selectedCategory.value // Можем использовать для иконки по умолчанию

                // Показываем иконку счета, если он выбран
                if (selectedAccount != null) {
                    // TODO: Получить реальную иконку счета из selectedAccount.iconResId или похожего поля
                    iconView.setImageResource(R.drawable.ic_mobile_wallet) // Placeholder счета
                    Log.d("Frag(${instanceTransactionType.name})", "Displaying account icon for ${selectedAccount.title}")
                }
                // Иначе показываем иконку выбранной категории (если она есть)
                else if (selectedCategory != null) {
                    // TODO: Получить реальную иконку категории из selectedCategory.iconResId
                    iconView.setImageResource(R.drawable.ic_down_arrow) // Placeholder категории
                    Log.d("Frag(${instanceTransactionType.name})", "Displaying category icon for ${selectedCategory.title}")
                }
                // Если ни то, ни другое не выбрано (маловероятно здесь), показываем ??
                else {
                    iconView.setImageResource(R.drawable.ic_checkmark)
                    Log.d("Frag(${instanceTransactionType.name})", "Displaying question mark icon (no account/category selected)")
                }
                iconView.isClickable = true // Можно кликнуть для выбора/смены счета
            }
            null -> { // На всякий случай
                iconView.setImageResource(R.drawable.ic_checkmark)
                iconView.isClickable = false
                Log.w("Frag(${instanceTransactionType.name})", "Active VM type is NULL, displaying question mark")
            }
        }
        // Иконка всегда видима, если сам amountDisplayLayout видим
        iconView.isVisible = true

        // Обновляем текст суммы
        amountTextView.text = formatAmountForDisplay(viewModel.amountString.value ?: "0")
    }

    // Форматирует строку суммы для отображения
    private fun formatAmountForDisplay(amountStr: String): String {
        // Простое отображение "0", если строка пустая или только точка
        return if (amountStr.isEmpty() || amountStr == ".") "0" else amountStr
        // Для форматирования валюты:
        // return try { NumberFormat.getCurrencyInstance(Locale("ru", "RU")).format(amountStr.toDouble()) } catch (e: Exception) { amountStr } // Осторожно с промежуточным вводом
    }

    // Обновляет текст на кнопке даты
    private fun updateDateButtonText(date: LocalDate) {
        // Проверяем, инициализирована ли кнопка и существует ли View
        if (::dateButton.isInitialized && _binding != null) {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val buttonText = when (date) {
                today -> "Сегодня"
                yesterday -> "Вчера"
                else -> if (date.year == today.year) date.format(dateButtonFormatterSameYear) else date.format(dateButtonFormatterDifferentYear)
            }
            dateButton.text = buttonText
        }
    }

    // Показывает диалог выбора даты
    private fun showDatePicker() {
        val currentDate = viewModel.date.value ?: LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                viewModel.setDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            currentDate.year,
            currentDate.monthValue - 1, // Месяцы в DatePickerDialog с 0
            currentDate.dayOfMonth
        )
        datePickerDialog.show()
    }

    // Показывает диалог выбора счета для ПЕРЕВОДА
    private fun showAccountSelectionDialog(isSelectingFromAccount: Boolean) {
        val accounts = viewModel.accountsList.value ?: emptyList()
        if (accounts.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_accounts_available, Toast.LENGTH_SHORT).show()
            return
        }
        val accountNames = accounts.map { it.title }.toTypedArray()
        val title = if (isSelectingFromAccount) "Счет списания" else "Счет зачисления"

        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setItems(accountNames) { dialog, which ->
                val selectedAccount = accounts[which]
                if (isSelectingFromAccount) {
                    viewModel.selectAccountFrom(selectedAccount) // ViewModel проверит на совпадение
                } else {
                    viewModel.selectAccountTo(selectedAccount)   // ViewModel проверит на совпадение
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // Показывает диалог выбора счета для РАСХОДА/ДОХОДА
    private fun showPaymentAccountSelectionDialog() {
        val accounts = viewModel.accountsList.value ?: emptyList()
        if (accounts.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_accounts_available, Toast.LENGTH_SHORT).show()
            return
        }
        val accountNames = accounts.map { it.title }.toTypedArray()

        AlertDialog.Builder(requireContext())
            .setTitle("Выберите счет")
            .setItems(accountNames) { dialog, which ->
                val selectedAccount = accounts[which]
                viewModel.selectPaymentAccount(selectedAccount)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    // --- Category Click Listener Implementation ---
    override fun onItemClick(category: Category) {
        // Проверяем соответствие типа категории типу фрагмента и активному типу в VM
        val expectedCategoryType = if (instanceTransactionType == TransactionType.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME
        val isActiveFragment = viewModel.currentTransactionType.value == instanceTransactionType

        if (category.categoryType == expectedCategoryType && isActiveFragment) {
            Log.i("Frag(${instanceTransactionType.name})", ">>> onItemClick processed for category: ${category.title}")
            viewModel.selectCategory(category) // Сообщаем ViewModel о выборе
        } else {
            // Логируем, почему клик был проигнорирован
            Log.w("Frag(${instanceTransactionType.name})", "onItemClick ignored. Category type: ${category.categoryType}, Expected type: $expectedCategoryType, IsActiveFragment: $isActiveFragment")
        }
    }
} // Конец класса TransactionAddFragment