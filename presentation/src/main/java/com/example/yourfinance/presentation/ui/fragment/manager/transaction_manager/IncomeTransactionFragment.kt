package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.Observer
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentTransactionIncomeBinding
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.presentation.ui.adapter.CategoryTransactionAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class IncomeTransactionFragment : BaseTransactionInputFragment(), CategoryTransactionAdapter.OnItemClickListener {

    private var _binding: FragmentTransactionIncomeBinding? = null
    private val binding get() = _binding!!

    override val viewModel: TransactionManagerViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var categoryAdapter: CategoryTransactionAdapter

    override val commonInputRoot: View get() = binding.includeCommonInput.root
    override val amountTextView: android.widget.TextView get() = binding.includeCommonInput.amountTextView
    override val selectedItemIcon: android.widget.ImageView get() = binding.includeCommonInput.selectedItemIcon
    override val noteEditText: com.google.android.material.textfield.TextInputEditText get() = binding.includeCommonInput.noteEditText
    override val keypadView: View get() = binding.includeCommonInput.keypad


    override fun getFragmentTransactionType() = TransactionType.INCOME

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("Frag(${getFragmentTransactionType().name})", "onViewCreated")

        setupCategoryRecyclerView()
        setupSpecificClickListeners()
        observeSpecificViewModel()
    }

    protected open fun setupSpecificClickListeners() { /* Нет специфичных кликов */ }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Frag(${getFragmentTransactionType().name})", "onDestroyView")
        _binding = null
    }

    // --- Setup ---
    private fun setupCategoryRecyclerView() {
        Log.d("Frag(${getFragmentTransactionType().name})", "Setting up Category RecyclerView...")
        categoryAdapter = CategoryTransactionAdapter(this)
        binding.includeExpenseIncome.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoryAdapter
            itemAnimator = null
        }
    }

    // --- Observation ---
    protected open fun observeSpecificViewModel() {
        Log.d("Frag(${getFragmentTransactionType().name})", "Setting up specific observers.")

        viewModel.activeTransactionState.observe(viewLifecycleOwner, Observer { state ->
            Log.d("Frag(${getFragmentTransactionType().name})", "Observed activeTransactionState change: $state")

            if (state is ActiveTransactionState.ExpenseIncomeState) {
                Log.d("Frag(${getFragmentTransactionType().name})", "Updating UI for Expense/Income State.")
                if (commonInputRoot.isVisible) {
                    updateAmountDisplayLayout()
                }
            } else if (state is ActiveTransactionState.InitialState) {
                if (commonInputRoot.isVisible) {
                    updateAmountDisplayLayout()
                }
                Log.d("Frag(${getFragmentTransactionType().name})", "Active state is InitialState.")
            }
        })

        viewModel.allCategories.observe(viewLifecycleOwner, Observer { allFullCategories ->
            val expectedCategoryType = CategoryType.INCOME
            val relevantCategories = allFullCategories
                ?.filter { it.categoryType == expectedCategoryType }
                ?.map { it }
                ?: emptyList()

            if (::categoryAdapter.isInitialized) {
                categoryAdapter.submitList(relevantCategories)
            } else {
                Log.w("Frag(${getFragmentTransactionType().name})", "CategoryAdapter not initialized.")
            }
        })
    }

    // --- UI Updates & Dialogs ---

    override fun updateAmountDisplayLayout() {
        if (_binding == null) {
            Log.w("Frag(${getFragmentTransactionType().name})", "updateAmountDisplayLayout called but binding is null!")
            return
        }

        val iconView = selectedItemIcon

        Log.d("Frag(${getFragmentTransactionType().name})", "Updating AmountDisplayLayout icon.")

        when (viewModel.activeTransactionState.value) {
            is ActiveTransactionState.ExpenseIncomeState -> {
                val state = viewModel.activeTransactionState.value as ActiveTransactionState.ExpenseIncomeState
                if (state.selectedPaymentAccount != null) {
//                    iconView.setImageResource(state.selectedPaymentAccount.iconResId ?: R.drawable.ic_mobile_wallet)
                    iconView.setImageResource(R.drawable.ic_mobile_wallet)
                    Log.d("Frag(${getFragmentTransactionType().name})", "Displaying account icon for ${state.selectedPaymentAccount.title}")
                } else if (state.selectedCategory != null) {
//                    iconView.setImageResource(state.selectedCategory.iconResId ?: R.drawable.ic_down_arrow)
                    iconView.setImageResource(R.drawable.ic_mobile_wallet)
                    Log.d("Frag(${getFragmentTransactionType().name})", "Displaying category icon for ${state.selectedCategory.title}")
                } else {
                    iconView.setImageResource(R.drawable.ic_mobile_wallet)
                    Log.d("Frag(${getFragmentTransactionType().name})", "Displaying default icon (no account/category selected)")
                }
                iconView.isClickable = true
                iconView.isVisible = true

            }
            ActiveTransactionState.InitialState -> {
                if (commonInputRoot.isVisible) {
                    iconView.setImageResource(R.drawable.ic_mobile_wallet)
                    iconView.isClickable = true
                    iconView.isVisible = true
                } else {
                    iconView.isVisible = false
                }
            }
            null -> {
                Log.w("Frag(${getFragmentTransactionType().name})", "Active state is null.")
                if (commonInputRoot.isVisible) {
                    iconView.setImageResource(R.drawable.ic_mobile_wallet)
                    iconView.isClickable = false
                    iconView.isVisible = true
                } else {
                    iconView.isVisible = false
                }
            }
            else -> {
                Log.w("Frag(${getFragmentTransactionType().name})", "Ignoring non-Expense/Income state: ${viewModel.activeTransactionState.value}")
            }
        }
    }

    override fun onIconClick() {
        showPaymentAccountSelectionDialog()
    }

    override fun onItemClick(category: Category) {
        val expectedCategoryType = CategoryType.INCOME

        if (category.categoryType == expectedCategoryType) {
            Log.i("Frag(${getFragmentTransactionType().name})", ">>> onItemClick processed for category: ${category.title}")
            viewModel.selectCategory(category)
        } else {
            Log.w("Frag(${getFragmentTransactionType().name})", "onItemClick ignored. Category type: ${category.categoryType}, Expected type: $expectedCategoryType")
        }
    }
}