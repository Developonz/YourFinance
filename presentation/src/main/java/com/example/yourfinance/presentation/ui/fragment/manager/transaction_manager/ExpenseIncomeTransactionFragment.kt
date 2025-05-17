package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentTransactionExpenseIncomeBinding
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.presentation.ui.adapter.CategoryTransactionAdapter
import com.example.yourfinance.presentation.ui.adapter.list_item.CategoryListItem
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseIncomeTransactionFragment : BaseTransactionInputFragment(), CategoryTransactionAdapter.OnItemClickListener {

    private var _binding: FragmentTransactionExpenseIncomeBinding? = null
    private val binding get() = _binding!!

    // ViewModel получается от родительского фрагмента (TransactionContainerFragment)
    override val viewModel: TransactionManagerViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var categoryAdapter: CategoryTransactionAdapter
    private lateinit var fragmentSpecificCategoryType: CategoryType

    override val commonInputRoot: View get() = binding.includeCommonInput.root
    override val amountTextView: android.widget.TextView get() = binding.includeCommonInput.amountTextView
    override val selectedItemIcon: ShapeableImageView get() = binding.includeCommonInput.selectedItemIcon // Уточнили тип
    override val noteEditText: com.google.android.material.textfield.TextInputEditText get() = binding.includeCommonInput.noteEditText
    override val keypadView: View get() = binding.includeCommonInput.keypad

    companion object {
        private const val ARG_CATEGORY_TYPE = "arg_category_type"

        fun newInstance(categoryType: CategoryType): ExpenseIncomeTransactionFragment {
            val fragment = ExpenseIncomeTransactionFragment()
            val args = Bundle()
            args.putSerializable(ARG_CATEGORY_TYPE, categoryType)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            fragmentSpecificCategoryType = it.getSerializable(ARG_CATEGORY_TYPE) as CategoryType
        }
        if (!::fragmentSpecificCategoryType.isInitialized) {
            Log.e("FragEI", "CategoryType not provided to fragment! Defaulting to EXPENSE.")
            fragmentSpecificCategoryType = CategoryType.EXPENSE // Безопасное значение по умолчанию
        }
        Log.i("Frag(${getFragmentTransactionType().name})", "onCreate - Specific Category Type: $fragmentSpecificCategoryType")
    }

    override fun getFragmentTransactionType(): TransactionType {
        return when (fragmentSpecificCategoryType) {
            CategoryType.EXPENSE -> TransactionType.EXPENSE
            CategoryType.INCOME -> TransactionType.INCOME
            // else -> throw IllegalStateException("Unexpected category type: $fragmentSpecificCategoryType")
            // Лучше обработать непредвиденный случай, если такое возможно из-за ошибок сериализации
            else -> {
                Log.w("FragEI", "Unexpected category type $fragmentSpecificCategoryType in getFragmentTransactionType. Defaulting to EXPENSE.")
                TransactionType.EXPENSE
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionExpenseIncomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("Frag(${getFragmentTransactionType().name})", "onViewCreated. Specific Type: $fragmentSpecificCategoryType")
        setupCategoryRecyclerView()
        observeSpecificViewModel() // Общие наблюдатели уже настроены в super.onViewCreated
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Frag(${getFragmentTransactionType().name})", "onDestroyView")
        _binding = null
    }

    private fun setupCategoryRecyclerView() {
        Log.d("Frag(${getFragmentTransactionType().name})", "Setting up Category RecyclerView...")
        categoryAdapter = CategoryTransactionAdapter(this)
        binding.categoryRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 4) // Например, 4 колонки
            adapter = categoryAdapter
            itemAnimator = null // Отключаем анимации для производительности, если не нужны
        }
        (binding.categoryRecyclerView.layoutManager as GridLayoutManager).spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                // Даем кнопке настроек всю ширину, если она есть и это нужно
                // В данном случае, все элементы занимают 1 ячейку, так что это не обязательно
                return 1 // Все элементы занимают 1 ячейку
            }
        }
    }

    private fun observeSpecificViewModel() {
        Log.d("Frag(${getFragmentTransactionType().name})", "Setting up specific observers for $fragmentSpecificCategoryType.")

        // Наблюдаем за состоянием, чтобы знать, что выбрано
        val relevantStateObserver = Observer<Any> { _ -> // Объединяем наблюдатели
            val allFullCategoriesList = viewModel.allCategories.value
            val activeTxState = viewModel.activeTransactionState.value

            if (allFullCategoriesList != null) {
                val selectedData = if (activeTxState is ActiveTransactionState.ExpenseIncomeState &&
                    viewModel.currentTransactionType.value == getFragmentTransactionType()) { // Учитываем тип транзакции
                    activeTxState.selectedCategory
                } else null

                val relevantCategories = allFullCategoriesList
                    .filter { it.categoryType == fragmentSpecificCategoryType }

                val displayList = mutableListOf<CategoryListItem>()
                relevantCategories.forEach { category ->
                    // Проверяем, является ли текущая категория или одна из ее подкатегорий выбранной
                    val isCatSelected = selectedData != null && selectedData.id == category.id && selectedData.categoryType == category.categoryType
                    displayList.add(CategoryListItem.CategoryItem(category, isCatSelected))

                    // Если подкатегории тоже отображаются в этом списке
                    category.children.forEach { subcategory ->
                        val isSubSelected = selectedData != null && selectedData.id == subcategory.id && selectedData.categoryType == subcategory.categoryType
                        displayList.add(CategoryListItem.SubcategoryItem(subcategory, isSubSelected))
                    }
                }
                displayList.add(CategoryListItem.SettingsButtonItem) // Добавляем кнопку настроек

                if (::categoryAdapter.isInitialized) {
                    categoryAdapter.submitList(displayList)
                    Log.d("Frag(${getFragmentTransactionType().name})", "Submitted ${displayList.size} items of type $fragmentSpecificCategoryType to adapter. Selected: ${selectedData?.title}")
                } else {
                    Log.w("Frag(${getFragmentTransactionType().name})", "CategoryAdapter not initialized when data arrived.")
                }
            }
        }

        viewModel.allCategories.observe(viewLifecycleOwner, relevantStateObserver)
        viewModel.activeTransactionState.observe(viewLifecycleOwner, relevantStateObserver)
        viewModel.currentTransactionType.observe(viewLifecycleOwner, relevantStateObserver)
    }


    override fun updateAmountDisplayLayout() {
        if (_binding == null) {
            Log.w("Frag(${getFragmentTransactionType().name})", "updateAmountDisplayLayout called but binding is null!")
            return
        }

        val iconView = selectedItemIcon // Уже имеет тип ShapeableImageView
        // Log.d("Frag(${getFragmentTransactionType().name})", "Updating AmountDisplayLayout icon and background.")

        val currentVmActiveState = viewModel.activeTransactionState.value
        val isCurrentFragmentActiveByType = viewModel.currentTransactionType.value == getFragmentTransactionType()

        if (isCurrentFragmentActiveByType && currentVmActiveState is ActiveTransactionState.ExpenseIncomeState) {
            val state = currentVmActiveState
            iconView.isVisible = true
            iconView.isClickable = true

            if (state.selectedPaymentAccount != null) {
                val account = state.selectedPaymentAccount
                iconView.setImageResource(account.iconResourceId ?: R.drawable.ic_mobile_wallet) // Иконка счета

                val colorHex = account.colorHex ?: "#FFFF00" // Желтый по умолчанию, если нет цвета
                var parsedColor = Color.YELLOW
                try {
                    parsedColor = Color.parseColor(colorHex)
                } catch (e: IllegalArgumentException) {
                    Log.w("Frag(${getFragmentTransactionType().name})", "Invalid color hex for account: $colorHex. Using default yellow.")
                }
                iconView.setBackgroundColor(parsedColor) // Используем setBackgroundColor
                val iconTintColorForSrc = if (ColorUtils.calculateLuminance(parsedColor) > 0.5) Color.BLACK else Color.WHITE
                iconView.setColorFilter(iconTintColorForSrc) // Используем setColorFilter

            } else {
                // Нет выбранного счета - показываем иконку "кошелек" и желтый фон по умолчанию
                iconView.setImageResource(R.drawable.ic_mobile_wallet) // Иконка по умолчанию для выбора счета
                iconView.setBackgroundColor(Color.YELLOW) // Желтый фон
                iconView.setColorFilter(Color.BLACK) // Черная иконка на желтом фоне
            }
        } else if (isCurrentFragmentActiveByType && (currentVmActiveState is ActiveTransactionState.InitialState || currentVmActiveState == null) && commonInputRoot.isVisible) {
            // Начальное состояние или нет состояния для Расход/Доход - показываем иконку "кошелек" и желтый фон
            iconView.isVisible = true
            iconView.isClickable = true
            iconView.setImageResource(R.drawable.ic_mobile_wallet) // Иконка по умолчанию для выбора счета
            iconView.setBackgroundColor(Color.YELLOW) // Желтый фон
            iconView.setColorFilter(Color.BLACK) // Черная иконка на желтом фоне
        }
        else {
            // Если фрагмент не активен по типу или другие условия не выполнены
            // Log.d("Frag(${getFragmentTransactionType().name})", "Icon view hidden or conditions not met. ActiveByType: $isCurrentFragmentActiveByType, commonInputRoot.isVisible: ${commonInputRoot.isVisible}, state: $currentVmActiveState")
            iconView.isVisible = false // Скрываем иконку, если не должно быть видно
        }
    }


    override fun onIconClick() {
        Log.d("Frag(${getFragmentTransactionType().name})", "selectedItemIcon clicked. Requesting AccountSelectionBottomSheet via ViewModel.")
        if (viewModel.accountsList.value.isNullOrEmpty()) {
            Toast.makeText(requireContext(), R.string.no_accounts_available, Toast.LENGTH_SHORT).show()
            return
        }
        viewModel.requestAccountSelectionForExpenseIncome()
    }

    override fun onItemClick(item: CategoryListItem) {
        when (item) {
            is CategoryListItem.CategoryItem -> {
                val categoryData = item.category // Уже Category, который реализует ICategoryData
                if (categoryData.categoryType == fragmentSpecificCategoryType) {
                    Log.i("Frag(${getFragmentTransactionType().name})", ">>> onItemClick processed for category: ${categoryData.title} (Type: ${categoryData.categoryType})")
                    viewModel.selectCategory(categoryData)
                } else {
                    Log.w("Frag(${getFragmentTransactionType().name})", "onItemClick ignored. Category type: ${categoryData.categoryType}, Expected fragment type: $fragmentSpecificCategoryType")
                }
            }
            is CategoryListItem.SubcategoryItem -> {
                // Если у вас есть подкатегории и они обрабатываются так же:
                val subcategoryData = item.subcategory // Уже Subcategory, который реализует ICategoryData
                if (subcategoryData.categoryType == fragmentSpecificCategoryType) {
                    Log.i("Frag(${getFragmentTransactionType().name})", ">>> onItemClick processed for subcategory: ${subcategoryData.title} (Type: ${subcategoryData.categoryType})")
                    viewModel.selectCategory(subcategoryData) // ViewModel ожидает ICategoryData
                } else {
                    Log.w("Frag(${getFragmentTransactionType().name})", "onItemClick ignored. Subcategory type: ${subcategoryData.categoryType}, Expected fragment type: $fragmentSpecificCategoryType")
                }
            }
            is CategoryListItem.SettingsButtonItem -> {
                Log.i("Frag(${getFragmentTransactionType().name})", ">>> SettingsItem clicked. Requesting navigation to category management via ViewModel.")
                viewModel.requestNavigateToCategorySettings()
            }
        }
    }
}