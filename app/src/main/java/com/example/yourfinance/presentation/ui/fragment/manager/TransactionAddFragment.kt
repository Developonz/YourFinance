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
import androidx.fragment.app.Fragment // ИЗМЕНЕНИЕ: Наследуемся от Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController // Импорт для навигации назад
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yourfinance.R
import com.example.yourfinance.databinding.FragmentTransactionAddBinding
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.presentation.ui.adapter.CategoryTransactionAdapter
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale



@AndroidEntryPoint
class TransactionAddFragment : Fragment() {

    private var _binding: FragmentTransactionAddBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by activityViewModels()
    private lateinit var categoryAdapter: CategoryTransactionAdapter
    private val dateButtonFormatterSameYear = DateTimeFormatter.ofPattern("d MMM.", Locale("ru"))
    private val dateButtonFormatterDifferentYear = DateTimeFormatter.ofPattern("d MMM.\nyyyy", Locale("ru"))
    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU"))
    private lateinit var dateButton: MaterialButton

    // УДАЛЕНО: Метод getTheme() больше не нужен
    // override fun getTheme(): Int { ... }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("TESTTR", "test0")
        _binding = FragmentTransactionAddBinding.inflate(inflater, container, false)
        Log.i("TESTTR", "test1")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateButton = binding.keypad.findViewById(R.id.date_button)

        Log.i("TESTTR", "test1")

        setupTabs()
        Log.i("TESTTR", "test2")
        setupCategoryRecyclerView()
        Log.i("TESTTR", "test3")
        setupClickListeners()
        Log.i("TESTTR", "test4")
        observeViewModel()
        Log.i("TESTTR", "test5")

        viewModel.setTransactionType(TransactionType.EXPENSE)
        Log.i("TESTTR", "test6")
        binding.amountDisplayLayout.isVisible = false
        Log.i("TESTTR", "test7")

        // Ваша Activity теперь должна настроить Toolbar для этого фрагмента,
        // если вам нужен заголовок или кнопка "назад" в Toolbar.
        // Например, в Activity:
        // navController.addOnDestinationChangedListener { _, destination, _ ->
        //    if (destination.id == R.id.transactionAddFragment) {
        //        supportActionBar?.title = "Добавить транзакцию"
        //        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Показать кнопку назад
        //    } else {
        //         supportActionBar?.setDisplayHomeAsUpEnabled(false) // Скрыть для других
        //    }
        // }
        // И обработать нажатие кнопки назад в onOptionsItemSelected
    }

    // --- Методы setupTabs, setupCategoryRecyclerView, setupClickListeners, isSelectionValid,
    // --- showDatePicker, showAccountSelectionDialog остаются БЕЗ ИЗМЕНЕНИЙ ---

    private fun setupTabs() {
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Расход"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Доход"))
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Перевод"))

        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val type = when (tab?.position) {
                    0 -> TransactionType.EXPENSE
                    1 -> TransactionType.INCOME
                    2 -> TransactionType.REMITTANCE
                    else -> TransactionType.EXPENSE
                }
                viewModel.setTransactionType(type)
                binding.amountDisplayLayout.isVisible = false
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupCategoryRecyclerView() {
        categoryAdapter = CategoryTransactionAdapter()
        binding.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoryAdapter
            /* ВАЖНО: Установить OnItemClickListener */
            /*
            categoryAdapter.setOnItemClickListener(object : CategoryTransactionAdapter.OnItemClickListener {
                override fun onItemClick(category: Category) {
                    viewModel.selectCategory(category)
                    binding.amountDisplayLayout.isVisible = true
                    updateAmountDisplayLayout()
                }
            })
            */
        }
    }

    private fun setupClickListeners() {
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
        // binding.keypad.findViewById<View>(R.id.key_plus).setOnClickListener { /* TODO */ }
        // binding.keypad.findViewById<View>(R.id.key_minus).setOnClickListener { /* TODO */ }
        binding.keypad.findViewById<View>(R.id.key_confirm).setOnClickListener {
            if (isSelectionValid()) {
                viewModel.saveTransaction()
            } else {
                Toast.makeText(requireContext(), "Выберите категорию или счета", Toast.LENGTH_SHORT).show()
            }
        }

        binding.noteEditText.addTextChangedListener { editable ->
            viewModel.setNote(editable.toString())
        }

        dateButton.setOnClickListener {
            showDatePicker()
        }

        binding.cardAccountFrom.setOnClickListener {
            showAccountSelectionDialog(true)
        }
        binding.cardAccountTo.setOnClickListener {
            showAccountSelectionDialog(false)
        }
    }

    private fun isSelectionValid(): Boolean {
        return when (viewModel.currentTransactionType.value) {
            TransactionType.INCOME, TransactionType.EXPENSE -> viewModel.selectedCategory.value != null
            TransactionType.REMITTANCE -> viewModel.selectedAccountFrom.value != null && viewModel.selectedAccountTo.value != null
            null -> false
        }
    }

    private fun showDatePicker() {
        val currentDate = viewModel.date.value ?: LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                viewModel.setDate(selectedDate)
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        )
        datePickerDialog.show()
    }

    private fun showAccountSelectionDialog(isSelectingFromAccount: Boolean) {
        val accounts = viewModel.accountsList.value ?: emptyList()
        if (accounts.isEmpty()) {
            Toast.makeText(requireContext(), "Нет доступных счетов", Toast.LENGTH_SHORT).show()
            return
        }
        val accountNames = accounts.map { it.title }.toTypedArray()
        AlertDialog.Builder(requireContext())
            .setTitle(if (isSelectingFromAccount) "Счет списания" else "Счет зачисления")
            .setItems(accountNames) { dialog, which ->
                val selectedAccount = accounts[which]
                if (isSelectingFromAccount) {
                    if (selectedAccount.id == viewModel.selectedAccountTo.value?.id) {
                        Toast.makeText(requireContext(), "Счета должны отличаться", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.selectAccountFrom(selectedAccount)
                        if (viewModel.selectedAccountTo.value != null) {
                            binding.amountDisplayLayout.isVisible = true
                            updateAmountDisplayLayout()
                        }
                    }
                } else {
                    if (selectedAccount.id == viewModel.selectedAccountFrom.value?.id) {
                        Toast.makeText(requireContext(), "Счета должны отличаться", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.selectAccountTo(selectedAccount)
                        if (viewModel.selectedAccountFrom.value != null) {
                            binding.amountDisplayLayout.isVisible = true
                            updateAmountDisplayLayout()
                        }
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeViewModel() {
        viewModel.currentTransactionType.observe(viewLifecycleOwner, Observer { type ->
            type?.let { updateUiForTransactionType(it) }
        })

        viewModel.filteredCategories.observe(viewLifecycleOwner, Observer { fullCategories ->
            val categories = fullCategories.map { it.category }
            if (::categoryAdapter.isInitialized) {
                categoryAdapter.submitList(categories)
            }
        })

        viewModel.selectedCategory.observe(viewLifecycleOwner, Observer { category ->
            if (viewModel.currentTransactionType.value != TransactionType.REMITTANCE) {
                binding.amountDisplayLayout.isVisible = category != null
                updateAmountDisplayLayout()
            }
        })

        viewModel.selectedAccountFrom.observe(viewLifecycleOwner, Observer { account ->
            binding.textAccountFrom.text = account?.title ?: "Выбрать"
            if (viewModel.currentTransactionType.value == TransactionType.REMITTANCE) {
                binding.amountDisplayLayout.isVisible = account != null && viewModel.selectedAccountTo.value != null
                updateAmountDisplayLayout()
            }
        })

        viewModel.selectedAccountTo.observe(viewLifecycleOwner, Observer { account ->
            binding.textAccountTo.text = account?.title ?: "Выбрать"
            if (viewModel.currentTransactionType.value == TransactionType.REMITTANCE) {
                binding.amountDisplayLayout.isVisible = account != null && viewModel.selectedAccountFrom.value != null
                updateAmountDisplayLayout()
            }
        })

        viewModel.amountString.observe(viewLifecycleOwner, Observer { amountStr ->
            binding.amountTextView.text = formatAmountForDisplay(amountStr ?: "0")
        })

        viewModel.date.observe(viewLifecycleOwner, Observer { date ->
            date?.let { updateDateButtonText(it) }
        })

        viewModel.note.observe(viewLifecycleOwner, Observer { note ->
            if (binding.noteEditText.text.toString() != note) {
                binding.noteEditText.setText(note ?: "")
            }
        })

        viewModel.transactionSavedEvent.observe(viewLifecycleOwner, Observer { saved ->
            if (saved == true) {
                Toast.makeText(requireContext(), "Транзакция сохранена", Toast.LENGTH_SHORT).show()
                viewModel.resetSavedEvent()
                // ИЗМЕНЕНИЕ: Вместо dismiss() используем навигацию назад
                findNavController().popBackStack()
            }
        })
    }

    private fun updateDateButtonText(date: LocalDate) {
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        val buttonText = when (date) {
            today -> "Сегодня"
            yesterday -> "Вчера"
            else -> if (date.year == today.year) date.format(dateButtonFormatterSameYear) else date.format(dateButtonFormatterDifferentYear)
        }
        if (::dateButton.isInitialized) dateButton.text = buttonText
    }

    private fun updateAmountDisplayLayout() {
        if (!binding.amountDisplayLayout.isVisible) return
        val currentType = viewModel.currentTransactionType.value
        if (currentType == TransactionType.REMITTANCE) {
            binding.selectedItemIcon.setImageResource(R.drawable.ic_down_arrow)
        } else {
            val category = viewModel.selectedCategory.value
            if (category != null) binding.selectedItemIcon.setImageResource(R.drawable.ic_down_arrow) // TODO: Get real icon
            else binding.selectedItemIcon.setImageResource(R.drawable.ic_down_arrow)
        }
        binding.amountTextView.text = formatAmountForDisplay(viewModel.amountString.value ?: "0")
    }

    private fun formatAmountForDisplay(amountStr: String): String = if (amountStr.isEmpty() || amountStr == ".") "0" else amountStr

    private fun updateUiForTransactionType(type: TransactionType) {
        val isTransfer = type == TransactionType.REMITTANCE
        binding.categoryRecyclerView.isVisible = !isTransfer
        binding.transferAccountSelector.isVisible = isTransfer
        val targetTabPosition = when(type) { TransactionType.EXPENSE -> 0; TransactionType.INCOME -> 1; TransactionType.REMITTANCE -> 2 }
        if (binding.tabLayout.selectedTabPosition != targetTabPosition) binding.tabLayout.getTabAt(targetTabPosition)?.select()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }


}