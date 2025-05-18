package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentTransactionExpenseIncomeBinding
import com.example.yourfinance.presentation.ui.adapter.CategoryTransactionAdapter
import com.example.yourfinance.presentation.ui.adapter.list_item.CategoryListItem
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseIncomeTransactionFragment : BaseTransactionInputFragment(),
    CategoryTransactionAdapter.OnItemClickListener {

    private var _binding: FragmentTransactionExpenseIncomeBinding? = null
    private val binding get() = _binding!!

    override val viewModel: TransactionManagerViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )
    override val commonInputRoot get() = binding.includeCommonInput.root
    override val amountTextView get() = binding.includeCommonInput.amountTextView
    override val selectedItemIcon: ShapeableImageView
        get() = binding.includeCommonInput.selectedItemIcon
    override val noteEditText get() = binding.includeCommonInput.noteEditText
    override val keypadView get() = binding.includeCommonInput.keypad

    private lateinit var fragmentSpecificCategoryType: CategoryType
    private lateinit var categoryAdapter: CategoryTransactionAdapter

    companion object {
        private const val ARG_CATEGORY_TYPE = "arg_category_type"
        private val DEFAULT_FALLBACK_COLOR = Color.YELLOW

        fun newInstance(categoryType: CategoryType) =
            ExpenseIncomeTransactionFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_CATEGORY_TYPE, categoryType)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        fragmentSpecificCategoryType = arguments
            ?.getSerializable(ARG_CATEGORY_TYPE) as? CategoryType
            ?: CategoryType.EXPENSE
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding =
            FragmentTransactionExpenseIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategoryRecycler()
        observeViewModel()
    }

    private fun setupCategoryRecycler() {
        categoryAdapter = CategoryTransactionAdapter(this)
        binding.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoryAdapter
            itemAnimator = null
        }
    }


    private fun observeViewModel() {
        val observer = Observer<Any> {
            val all = viewModel.allCategories.value.orEmpty()
            val active = viewModel.activeTransactionState.value as? ActiveTransactionState.ExpenseIncomeState
            val curr = viewModel.currentTransactionType.value
            val sel = active
                ?.takeIf { curr == getFragmentTransactionType() }
                ?.selectedCategory

            val list = mutableListOf<CategoryListItem>()
            all.filter { it.categoryType == fragmentSpecificCategoryType }
                .forEach { cat ->
                    list += CategoryListItem.CategoryItem(cat, sel?.id == cat.id)
                    cat.children.forEach { sub ->
                        list += CategoryListItem.SubcategoryItem(sub, sel?.id == sub.id)
                    }
                }
            list += CategoryListItem.SettingsButtonItem
            categoryAdapter.submitList(list)
        }
        with(viewModel) {
            allCategories.observe(viewLifecycleOwner, observer)
            activeTransactionState.observe(viewLifecycleOwner, observer)
            currentTransactionType.observe(viewLifecycleOwner, observer)
        }
    }

    override fun updateAmountDisplayLayout() {
        val iconView = selectedItemIcon
        val active =
            viewModel.activeTransactionState.value as? ActiveTransactionState.ExpenseIncomeState
        if (viewModel.currentTransactionType.value == getFragmentTransactionType() && active != null) {
            val acc = active.selectedPaymentAccount
            val bg = acc?.colorHex ?: DEFAULT_FALLBACK_COLOR
            val ic = acc?.iconResourceId?.let(IconMap::idOf)
                ?: R.drawable.ic_mobile_wallet

            iconView.apply {
                isVisible = true
                setImageResource(ic)
                setBackgroundColor(bg)
                setColorFilter(
                    if (ColorUtils.calculateLuminance(bg) > 0.5)
                        Color.BLACK else Color.WHITE
                )
            }
        } else {
            iconView.isVisible = false
        }
    }

    override fun onIconClick() {
        val accounts = viewModel.accountsList.value.orEmpty()
        if (accounts.isEmpty()) {
            Toast.makeText(
                requireContext(),
                R.string.no_accounts_available,
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        AccountSelectionBottomSheet.newInstance(
            accounts = accounts,
            selectedAccountId = (viewModel.activeTransactionState.value as? ActiveTransactionState.ExpenseIncomeState)
                ?.selectedPaymentAccount?.id,
            onAccountSelectedCallback = { sel ->
                viewModel.selectPaymentAccount(sel)
                updateAmountDisplayLayout()
            },
            onSettingsClickedCallback = {
                viewModel.requestNavigateToAccountSettings()
            }
        ).show(parentFragmentManager, AccountSelectionBottomSheet.TAG)
    }

    override fun onItemClick(item: CategoryListItem) {
        when (item) {
            is CategoryListItem.CategoryItem    -> viewModel.selectCategory(item.category)
            is CategoryListItem.SubcategoryItem -> viewModel.selectCategory(item.subcategory)
            is CategoryListItem.SettingsButtonItem ->
                viewModel.requestNavigateToCategorySettings()
        }
    }

    override fun getFragmentTransactionType() =
        if (fragmentSpecificCategoryType == CategoryType.EXPENSE)
            TransactionType.EXPENSE else TransactionType.INCOME

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
