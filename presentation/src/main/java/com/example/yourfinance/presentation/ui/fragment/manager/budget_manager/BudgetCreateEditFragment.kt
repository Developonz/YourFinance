package com.example.yourfinance.presentation.ui.fragment.manager.budget_manager

import android.os.Bundle
import android.view.*
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.yourfinance.domain.model.PeriodLite
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.category.BaseCategory
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentBudgetCreateEditBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.math.BigDecimal

@AndroidEntryPoint
class BudgetCreateEditFragment : Fragment() {

    private var _binding: FragmentBudgetCreateEditBinding? = null
    private val binding get() = _binding!!

    private val args: BudgetCreateEditFragmentArgs by navArgs()
    private val viewModel: BudgetManagerViewModel by viewModels({ requireActivity() })

    private var isEditMode = false
    private var budgetToEdit: Budget? = null
    private var selectedCategoryIds = mutableSetOf<Long>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetCreateEditBinding.inflate(inflater, container, false)
        (activity as? AppCompatActivity)?.supportActionBar?.setDisplayHomeAsUpEnabled(true)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isEditMode = args.budgetId != -1L

        setupPeriodSpinner()
        setupListeners()

        if (isEditMode) {
            loadBudgetForEdit()
        } else {
            (activity as? AppCompatActivity)?.supportActionBar?.title = "Добавить бюджет"
            updateCategoriesTextView()
        }

        parentFragmentManager.setFragmentResultListener(CategorySelectionBottomSheetFragment.REQUEST_KEY, viewLifecycleOwner) { _, bundle ->
            val resultIds = bundle.getLongArray(CategorySelectionBottomSheetFragment.RESULT_IDS) ?: longArrayOf()
            selectedCategoryIds.clear()
            selectedCategoryIds.addAll(resultIds.toList())
            updateCategoriesTextView()
        }
    }

    private fun setupListeners() {
        binding.categoriesLayout.setOnClickListener {
            CategorySelectionBottomSheetFragment.newInstance(selectedCategoryIds.toList())
                .show(parentFragmentManager, CategorySelectionBottomSheetFragment.TAG)
        }
    }

    private fun loadBudgetForEdit() {
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Редактировать бюджет"
        lifecycleScope.launch {
            budgetToEdit = viewModel.loadBudget(args.budgetId)
            budgetToEdit?.let { budget ->
                binding.tietBudgetName.setText(budget.title)
                binding.tietBudgetAmount.setText(budget.budgetLimit.toString().removeSuffix(".0"))
                binding.periodSpinner.setSelection(PeriodLite.values().indexOf(budget.period))
                selectedCategoryIds.clear()

                if (budget.categories.isEmpty()) {
                    selectedCategoryIds.add(SelectableCategoryItem.AllCategories.id)
                } else {
                    selectedCategoryIds.addAll(budget.categories.map { it.id })
                }
                updateCategoriesTextView()
            }
        }
    }

    private fun updateCategoriesTextView() {
        if (selectedCategoryIds.isEmpty()) {
            binding.categoriesValue.text = "Выберите категории"
            return
        }

        if (selectedCategoryIds.contains(SelectableCategoryItem.AllCategories.id)) {
            binding.categoriesValue.text = "Все категории"
            return
        }

        val allCategories = viewModel.expenseCategories.value
        if (allCategories == null) {
            binding.categoriesValue.text = "Загрузка..."
            return
        }

        val allFlatCategories = allCategories.flatMap { cat ->
            listOf(cat.toBaseCategory()) + cat.children.map { it.toBaseCategory() }
        }
        val selectedNames = allFlatCategories
            .filter { selectedCategoryIds.contains(it.id) }
            .map { it.title }
        binding.categoriesValue.text = selectedNames.joinToString(", ")
    }

    private fun com.example.yourfinance.domain.model.entity.category.ICategoryData.toBaseCategory(): BaseCategory {
        return BaseCategory(Title(this.title), this.categoryType, this.id, this.iconResourceId, this.colorHex)
    }

    private fun setupPeriodSpinner() {
        val periods = PeriodLite.values().map { it.description }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, periods)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.periodSpinner.adapter = adapter
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.confirm_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_save -> { saveBudget(); true }
            android.R.id.home -> { findNavController().popBackStack(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun saveBudget() {
        val title = binding.tietBudgetName.text.toString().trim()
        val amountStr = binding.tietBudgetAmount.text.toString().trim()

        if (title.isEmpty() || amountStr.isEmpty()) {
            Toast.makeText(context, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            Toast.makeText(context, "Сумма должна быть больше нуля", Toast.LENGTH_SHORT).show()
            return
        }
        if (selectedCategoryIds.isEmpty()) {
            Toast.makeText(context, "Пожалуйста, выберите категории для бюджета", Toast.LENGTH_LONG).show()
            return
        }

        val selectedPeriod = PeriodLite.values()[binding.periodSpinner.selectedItemPosition]
        val categoriesToSave = mutableListOf<BaseCategory>()

        if (!selectedCategoryIds.contains(SelectableCategoryItem.AllCategories.id)) {
            viewModel.expenseCategories.value?.let { allCategories ->
                val allFlatCategories = allCategories.flatMap { cat ->
                    listOf(cat.toBaseCategory()) + cat.children.map { it.toBaseCategory() }
                }
                categoriesToSave.addAll(allFlatCategories.filter { selectedCategoryIds.contains(it.id) })
            }
        }

        val budget = Budget(
            id = if(isEditMode) budgetToEdit!!.id else 0,
            _title = Title(title),
            budgetLimit = BigDecimal(amount),
            period = selectedPeriod,
            categories = categoriesToSave
        )

        if (isEditMode) {
            viewModel.updateBudget(budget)
        } else {
            viewModel.createBudget(budget)
        }
        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}