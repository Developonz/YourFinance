package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.ColorUtils
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.lifecycle.Observer
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentTransactionExpenseIncomeBinding // Будет создано/переименовано
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.presentation.ui.adapter.CategoryTransactionAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ExpenseIncomeTransactionFragment : BaseTransactionInputFragment(), CategoryTransactionAdapter.OnItemClickListener {

    private var _binding: FragmentTransactionExpenseIncomeBinding? = null
    private val binding get() = _binding!!

    override val viewModel: TransactionManagerViewModel by viewModels(ownerProducer = { requireParentFragment() })

    private lateinit var categoryAdapter: CategoryTransactionAdapter

    // Аргумент для определения типа фрагмента (Расход/Доход)
    private lateinit var fragmentSpecificCategoryType: CategoryType

    override val commonInputRoot: View get() = binding.includeCommonInput.root
    override val amountTextView: android.widget.TextView get() = binding.includeCommonInput.amountTextView
    override val selectedItemIcon: android.widget.ImageView get() = binding.includeCommonInput.selectedItemIcon
    override val noteEditText: com.google.android.material.textfield.TextInputEditText get() = binding.includeCommonInput.noteEditText
    override val keypadView: View get() = binding.includeCommonInput.keypad

    companion object {
        private const val ARG_CATEGORY_TYPE = "arg_category_type"

        fun newInstance(categoryType: CategoryType): ExpenseIncomeTransactionFragment {
            val fragment = ExpenseIncomeTransactionFragment()
            val args = Bundle()
            args.putSerializable(ARG_CATEGORY_TYPE, categoryType) // CategoryType должен быть Serializable
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
            // Обработка ошибки, если тип не был передан. Маловероятно при использовании newInstance.
            Log.e("FragEI", "CategoryType not provided to fragment!")
            // Можно установить дефолтное значение или выбросить исключение
            fragmentSpecificCategoryType = CategoryType.EXPENSE // Как запасной вариант
        }
        Log.i("Frag(${getFragmentTransactionType().name})", "onCreate - Specific Category Type: $fragmentSpecificCategoryType")
    }

    override fun getFragmentTransactionType(): TransactionType {
        return when (fragmentSpecificCategoryType) {
            CategoryType.EXPENSE -> TransactionType.EXPENSE
            CategoryType.INCOME -> TransactionType.INCOME
            // Другие типы категорий здесь не должны появляться для этого фрагмента
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
        observeSpecificViewModel()
        // setupSpecificClickListeners() - пока нет специфичных, но метод есть в Base. Если понадобятся, можно переопределить.
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Frag(${getFragmentTransactionType().name})", "onDestroyView")
        _binding = null
    }

    private fun setupCategoryRecyclerView() {
        Log.d("Frag(${getFragmentTransactionType().name})", "Setting up Category RecyclerView...")
        categoryAdapter = CategoryTransactionAdapter(this) // 'this' передается как OnItemClickListener
        binding.categoryRecyclerView.apply { // Предполагаем, что RecyclerView теперь напрямую в FragmentTransactionExpenseIncomeBinding
            layoutManager = GridLayoutManager(requireContext(), 4)
            adapter = categoryAdapter
            itemAnimator = null
        }
    }

    private fun observeSpecificViewModel() {
        Log.d("Frag(${getFragmentTransactionType().name})", "Setting up specific observers for $fragmentSpecificCategoryType.")

        viewModel.activeTransactionState.observe(viewLifecycleOwner, Observer { state ->
            Log.d("Frag(${getFragmentTransactionType().name})", "Observed activeTransactionState change: $state")
            // Обновление UI для состояния Расход/Доход (если commonInputRoot виден)
            if (viewModel.currentTransactionType.value == getFragmentTransactionType()) {
                if (commonInputRoot.isVisible) {
                    updateAmountDisplayLayout()
                }
            }
        })

        viewModel.allCategories.observe(viewLifecycleOwner, Observer { allFullCategories ->
            val relevantCategories = allFullCategories
                ?.filter { it.categoryType == fragmentSpecificCategoryType } // Фильтруем по типу фрагмента
                ?: emptyList()

            val displayList = mutableListOf<ICategoryData>()
            relevantCategories.forEach { category ->
                displayList.add(category)
                displayList.addAll(category.children) // Добавляем подкатегории
            }

            if (::categoryAdapter.isInitialized) {
                categoryAdapter.submitList(displayList)
                Log.d("Frag(${getFragmentTransactionType().name})", "Submitted ${displayList.size} items of type $fragmentSpecificCategoryType to adapter.")
            } else {
                Log.w("Frag(${getFragmentTransactionType().name})", "CategoryAdapter not initialized when categories arrived.")
            }
        })
    }

    override fun updateAmountDisplayLayout() {
        if (_binding == null) {
            Log.w("Frag(${getFragmentTransactionType().name})", "updateAmountDisplayLayout called but binding is null!")
            return
        }

        val iconView = selectedItemIcon as com.google.android.material.imageview.ShapeableImageView // Кастуем к ShapeableImageView
        Log.d("Frag(${getFragmentTransactionType().name})", "Updating AmountDisplayLayout icon and background.")

        val currentVmActiveState = viewModel.activeTransactionState.value
        val isCurrentFragmentActiveByType = viewModel.currentTransactionType.value == getFragmentTransactionType()

        if (isCurrentFragmentActiveByType && currentVmActiveState is ActiveTransactionState.ExpenseIncomeState) {
            val state = currentVmActiveState

            iconView.isVisible = true
            iconView.isClickable = true

            if (state.selectedPaymentAccount != null) {
                val account = state.selectedPaymentAccount
                Log.d("Frag(${getFragmentTransactionType().name})", "Displaying account icon for ${account.title}")
                iconView.setImageResource(account.iconResourceId ?: R.drawable.ic_mobile_wallet)

                account.colorHex?.let { hex ->
                    try {
                        val color = Color.parseColor(hex)
                        // Устанавливаем цвет фона для ShapeableImageView
                        iconView.backgroundTintList = ColorStateList.valueOf(color)

                        val iconTintColorForSrc = if (ColorUtils.calculateLuminance(color) > 0.5) Color.BLACK else Color.WHITE
                        iconView.setColorFilter(iconTintColorForSrc) // Тинт для самой иконки (src)
                    } catch (e: IllegalArgumentException) {
                        Log.w("Frag(${getFragmentTransactionType().name})", "Invalid color hex for account: $hex")
                        iconView.backgroundTintList = ColorStateList.valueOf(Color.YELLOW) // Дефолтный желтый фон
                        iconView.clearColorFilter()
                        iconView.setImageResource(R.drawable.ic_mobile_wallet)
                    }
                } ?: run {
                    Log.d("Frag(${getFragmentTransactionType().name})", "Account ${account.title} has no colorHex. Using default yellow background.")
                    iconView.backgroundTintList = ColorStateList.valueOf(Color.YELLOW) // Дефолтный желтый фон
                    iconView.clearColorFilter()
                    iconView.setImageResource(R.drawable.ic_mobile_wallet)
                }
            } else {
                Log.d("Frag(${getFragmentTransactionType().name})", "No payment account selected. Displaying default icon and yellow background.")
                iconView.setImageResource(R.drawable.ic_mobile_wallet)
                iconView.backgroundTintList = ColorStateList.valueOf(Color.YELLOW) // Дефолтный желтый фон
                iconView.clearColorFilter()
            }

        } else if (isCurrentFragmentActiveByType && currentVmActiveState is ActiveTransactionState.InitialState && commonInputRoot.isVisible) {
            Log.d("Frag(${getFragmentTransactionType().name})", "InitialState. Displaying default icon and yellow background.")
            iconView.isVisible = true
            iconView.isClickable = true
            iconView.setImageResource(R.drawable.ic_mobile_wallet)
            iconView.backgroundTintList = ColorStateList.valueOf(Color.YELLOW) // Дефолтный желтый фон
            iconView.clearColorFilter()
        } else {
            Log.d("Frag(${getFragmentTransactionType().name})", "Icon view hidden. isCurrentFragmentActiveByType: $isCurrentFragmentActiveByType, commonInputRoot.isVisible: ${commonInputRoot.isVisible}, state: $currentVmActiveState")
            iconView.isVisible = false
            iconView.backgroundTintList = null // Сбрасываем тинт фона
            iconView.clearColorFilter()
        }
    }

    override fun onIconClick() {
        // Здесь будет логика показа BottomSheetDialog для выбора счета (Шаг 2)
        // Пока оставим вызов старого метода, если он есть, или просто лог
        Log.d("Frag(${getFragmentTransactionType().name})", "selectedItemIcon clicked. Current impl: showPaymentAccountSelectionDialog (old)")
        showPaymentAccountSelectionDialog() // Пока оставляем старый диалог
    }

    override fun onItemClick(category: ICategoryData) {
        // Проверяем, что тип категории соответствует типу фрагмента
        if (category.categoryType == fragmentSpecificCategoryType) {
            Log.i("Frag(${getFragmentTransactionType().name})", ">>> onItemClick processed for category: ${category.title} (Type: ${category.categoryType})")
            viewModel.selectCategory(category)
        } else {
            Log.w("Frag(${getFragmentTransactionType().name})", "onItemClick ignored. Category type: ${category.categoryType}, Expected fragment type: $fragmentSpecificCategoryType")
        }
    }
}