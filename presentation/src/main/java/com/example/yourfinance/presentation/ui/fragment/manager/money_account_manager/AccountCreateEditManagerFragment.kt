
package com.example.yourfinance.presentation.ui.fragment.manager.money_account_manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentAccountCreateEditManagerBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.ui.adapter.ColorPickerAdapter
import com.example.yourfinance.presentation.ui.adapter.IconItem // Используем существующий IconItem
import com.example.yourfinance.presentation.ui.adapter.SingleIconListAdapter // Наш новый адаптер
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@AndroidEntryPoint
class AccountCreateEditManagerFragment : Fragment() {

    private var _binding: FragmentAccountCreateEditManagerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MoneyAccountManagerViewModel by viewModels()
    private val args: AccountCreateEditManagerFragmentArgs by navArgs()

    private var currentAccountId: Long = -1L
    private var isEditMode = false
    private var accountToEdit: MoneyAccount? = null

    // Поля для управления выбором иконки и цвета
    private var selectedIconResId: Int? = null
    private lateinit var selectedColorHex: String
    private lateinit var availableColors: List<String>

    private lateinit var iconListAdapter: SingleIconListAdapter
    private lateinit var colorSelectorAdapter: ColorPickerAdapter

    // ID ресурса массива, содержащего все иконки для счетов
    private val accountIconArrayResId: Int = R.array.account_icons // Убедись, что этот массив существует в arrays.xml

    companion object {
        private const val GRID_LAYOUT_COLUMNS_COLORS = 6
        private const val COLOR_PANEL_ANIMATION_DURATION = 150L
        private const val DEFAULT_FALLBACK_COLOR = "#03A9F4" // Синий по умолчанию для счетов
        private const val ICON_GRID_COLUMNS = 5 // Количество колонок для сетки иконок
    }

    private val amountFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountCreateEditManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAvailableColors()
        // Устанавливаем цвет по умолчанию, selectedIconResId будет установлен позже
        selectedColorHex = availableColors.firstOrNull() ?: DEFAULT_FALLBACK_COLOR

        currentAccountId = args.accountId
        isEditMode = currentAccountId != -1L

        setupIconRecyclerView()
        setupColorSelectorRecyclerView()

        if (isEditMode) {
            setupEditMode() // Загрузит данные, включая selectedIconResId и selectedColorHex
        } else {
            setupCreateMode() // Установит значения по умолчанию для нового счета
        }

        setupListeners()
        setupAmountEditTextListeners()
    }

    private fun loadAvailableColors() {
        availableColors = try {
            requireContext().resources.getStringArray(R.array.available_colors_category_account).toList()
        } catch (e: Exception) {
            Log.e("AccountCreateEdit", "Failed to load colors from resources", e)
            listOf(DEFAULT_FALLBACK_COLOR)
        }
        if (availableColors.isEmpty()) {
            Log.w("AccountCreateEdit", "Loaded color list is empty, using fallback.")
            availableColors = listOf(DEFAULT_FALLBACK_COLOR)
        }
    }

    private fun setupIconRecyclerView() {
        // selectedIconResId здесь может быть null (особенно при создании), адаптер это учтет
        iconListAdapter = SingleIconListAdapter(
            requireContext(),
            selectedColorHex, // Начальный цвет для отрисовки выделения (если иконка выбрана)
            selectedIconResId  // Начальная выбранная иконка (если есть)
        ) { clickedIconItem ->
            // Этот код выполнится при клике на иконку в адаптере
            val oldSelectedIcon = selectedIconResId
            selectedIconResId = clickedIconItem.resourceId
            if (oldSelectedIcon != selectedIconResId) {
                iconListAdapter.setSelectedIcon(selectedIconResId) // Сообщаем адаптеру о новом выборе
                updateIconPreviewImage()
            }
        }

        binding.recyclerViewAccountIcons.apply {
            layoutManager = GridLayoutManager(requireContext(), ICON_GRID_COLUMNS)
            adapter = iconListAdapter
            itemAnimator = null // Отключаем анимацию для простоты и производительности
        }
        // Загружаем список иконок в адаптер
        loadIconsIntoAdapter()
    }

    private fun loadIconsIntoAdapter() {
        val iconsList = mutableListOf<IconItem>()
        val currentResources = requireContext().resources
        var typedArray: TypedArray? = null
        try {
            typedArray = currentResources.obtainTypedArray(accountIconArrayResId)
            for (i in 0 until typedArray.length()) {
                val resId = typedArray.getResourceId(i, 0)
                if (resId != 0) {
                    val iconName: String = try {
                        // Имя ресурса используется как fallback или для отладки, можно не использовать в UI
                        currentResources.getResourceEntryName(resId)
                    } catch (e: android.content.res.Resources.NotFoundException) {
                        Log.e("AccountCreateEdit", "Resource name not found for ID: $resId", e)
                        "icon_id_$resId" // Фоллбэк имя
                    }
                    iconsList.add(IconItem(resId, iconName))
                }
            }
        } catch (e: android.content.res.Resources.NotFoundException) {
            Log.e("AccountCreateEdit", "Icon array resource (R.array.all_account_icons) not found.", e)
        } finally {
            typedArray?.recycle()
        }
        iconListAdapter.submitList(iconsList)
    }

    private fun loadAndSetDefaultIconForCreation() {
        // Устанавливаем иконку по умолчанию только если это режим создания и иконка еще не выбрана
        if (!isEditMode && selectedIconResId == null) {
            val currentResources = requireContext().resources
            var typedArray: TypedArray? = null
            try {
                typedArray = currentResources.obtainTypedArray(accountIconArrayResId)
                if (typedArray.length() > 0) {
                    val defaultIconResId = typedArray.getResourceId(0, 0) // Берем первую иконку из массива
                    if (defaultIconResId != 0) {
                        selectedIconResId = defaultIconResId
                        // Уведомляем адаптер о выборе, если он уже инициализирован
                        if (::iconListAdapter.isInitialized) {
                            iconListAdapter.setSelectedIcon(selectedIconResId)
                        }
                        updateIconPreviewImage()
                    }
                } else {
                    Log.w("AccountCreateEdit", "Account icon array (R.array.all_account_icons) is empty. No default icon set.")
                }
            } catch (e: Exception) {
                Log.e("AccountCreateEdit", "Error loading default icon from R.array.all_account_icons", e)
            } finally {
                typedArray?.recycle()
            }
        }
    }

    private fun setupColorSelectorRecyclerView() {
        colorSelectorAdapter = ColorPickerAdapter(availableColors) { newlySelectedColorHex ->
            selectedColorHex = newlySelectedColorHex
            updateColorIndicator()
            updateIconPreviewBackground()
            // Если иконка выбрана, обновить ее цвет в превью и цвет выделения в списке
            if (selectedIconResId != null) {
                updateIconPreviewImage()
                if (::iconListAdapter.isInitialized) {
                    iconListAdapter.setSelectedColor(selectedColorHex)
                }
            }
            toggleColorSelectorPanel(show = false) // Скрыть панель после выбора
        }
        binding.recyclerViewColorSelectorAccount.apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_LAYOUT_COLUMNS_COLORS)
            adapter = colorSelectorAdapter
        }
    }

    private fun updateColorIndicator() {
        if (_binding == null) return
        try {
            val colorInt = Color.parseColor(selectedColorHex)
            val bgDrawable = binding.viewColorIndicatorAccount.background as? GradientDrawable
            bgDrawable?.setColor(colorInt)
            if (ColorUtils.calculateLuminance(colorInt) > 0.9) {
                bgDrawable?.setStroke(2, Color.LTGRAY)
            } else {
                bgDrawable?.setStroke(0, Color.TRANSPARENT)
            }
        } catch (e: IllegalArgumentException){
            Log.e("AccountCreateEdit", "Invalid color hex for indicator: $selectedColorHex", e)
            binding.viewColorIndicatorAccount.setBackgroundColor(Color.GRAY) // Фоллбэк
        }
    }

    private fun updateIconPreviewBackground() {
        if (_binding == null) return
        try {
            val color = Color.parseColor(selectedColorHex)
            binding.cardIconPreviewWrapperAccount.setCardBackgroundColor(color)
            applyStrokeToCardIfNecessary(selectedColorHex, binding.cardIconPreviewWrapperAccount)
        } catch (e: IllegalArgumentException){
            Log.e("AccountCreateEdit", "Invalid color hex for preview bg: $selectedColorHex", e)
            binding.cardIconPreviewWrapperAccount.setCardBackgroundColor(Color.GRAY) // Фоллбэк
        }
    }

    private fun updateIconPreviewImage() {
        if (_binding == null) return
        selectedIconResId?.let { resId ->
            binding.imageViewSelectedIconPreviewAccount.setImageResource(resId)
            try {
                val color = Color.parseColor(selectedColorHex)
                val iconTintColor = if (ColorUtils.calculateLuminance(color) > 0.5) Color.BLACK else Color.WHITE
                binding.imageViewSelectedIconPreviewAccount.setColorFilter(iconTintColor)
                binding.imageViewSelectedIconPreviewAccount.visibility = View.VISIBLE
            } catch (e: IllegalArgumentException) {
                Log.e("AccountCreateEdit", "Invalid color hex for icon tint: $selectedColorHex", e)
                binding.imageViewSelectedIconPreviewAccount.setColorFilter(Color.WHITE) // Фоллбэк тинт
                binding.imageViewSelectedIconPreviewAccount.visibility = View.VISIBLE
            }
        } ?: run {
            binding.imageViewSelectedIconPreviewAccount.visibility = View.GONE
        }
    }

    private fun applyStrokeToCardIfNecessary(colorHex: String, targetView: MaterialCardView) {
        try {
            val colorInt = Color.parseColor(colorHex)
            if (ColorUtils.calculateLuminance(colorInt) > 0.9) {
                targetView.strokeWidth = 3 // Используй dp или конвертируй в px, если нужно
                targetView.strokeColor = Color.LTGRAY
            } else {
                targetView.strokeWidth = 0
            }
        } catch (e: IllegalArgumentException) {
            Log.e("AccountCreateEdit", "Invalid color hex for stroke: $colorHex", e)
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun toggleColorSelectorPanel(show: Boolean? = null) {
        if (_binding == null) return
        val panel = binding.cardColorSelectorPanelAccount
        val arrow = binding.imageViewColorArrowAccount
        val shouldShow = show ?: (panel.visibility == View.GONE)

        TransitionManager.beginDelayedTransition(binding.fixedFieldsContainer, AutoTransition().apply { duration = COLOR_PANEL_ANIMATION_DURATION })

        if (shouldShow) {
            panel.visibility = View.VISIBLE
            arrow.animate().rotation(180f).setDuration(COLOR_PANEL_ANIMATION_DURATION).start()
        } else {
            panel.visibility = View.GONE
            arrow.animate().rotation(0f).setDuration(COLOR_PANEL_ANIMATION_DURATION).start()
        }
    }

    private fun setupListeners() {
        binding.buttonConfirmAccount.setOnClickListener {
            saveAccountAndNavigateBack()
        }
        binding.colorSelectorContainerAccount.setOnClickListener {
            toggleColorSelectorPanel()
        }
    }

    private fun setupEditMode() {
        binding.buttonConfirmAccount.text = getString(R.string.save_changes)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.edit_account_title)

        viewLifecycleOwner.lifecycleScope.launch {
            accountToEdit = viewModel.loadAccountById(currentAccountId)
            // Проверяем, что View еще существует
            if (view == null || _binding == null) return@launch

            accountToEdit?.let { account ->
                populateUI(account)
                selectedIconResId = account.iconResourceId
                selectedColorHex = account.colorHex ?: availableColors.firstOrNull() ?: DEFAULT_FALLBACK_COLOR

                // Обновляем все UI элементы, связанные с иконкой и цветом
                updateColorIndicator()
                updateIconPreviewBackground()
                updateIconPreviewImage()

                // Уведомляем адаптеры об актуальных значениях
                if (::iconListAdapter.isInitialized) {
                    iconListAdapter.setSelectedColor(selectedColorHex) // Установить цвет для выделения
                    iconListAdapter.setSelectedIcon(selectedIconResId)   // Установить выбранную иконку
                }

            } ?: run {
                Toast.makeText(context, "Счет не найден", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupCreateMode() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_account_title)
        binding.buttonConfirmAccount.text = getString(R.string.create_account)

        // selectedColorHex уже установлен по умолчанию
        // Иконка по умолчанию будет загружена здесь
        loadAndSetDefaultIconForCreation()

        // Обновляем UI с цветом по умолчанию
        updateColorIndicator()
        updateIconPreviewBackground()
        // updateIconPreviewImage() вызовется из loadAndSetDefaultIconForCreation, если иконка найдена

        // Устанавливаем цвет и (возможно уже выбранную) иконку в адаптере
        if (::iconListAdapter.isInitialized) {
            iconListAdapter.setSelectedColor(selectedColorHex)
            iconListAdapter.setSelectedIcon(selectedIconResId) // selectedIconResId мог быть установлен в loadAndSetDefaultIcon
        }
        setupInitialFocusAndKeyboard()
    }

    private fun populateUI(account: MoneyAccount) {
        if (_binding == null) return
        binding.editTextAccountName.setText(account.title)
        binding.editTextAmount.setText(amountFormat.format(account.startBalance))
        binding.switchExclude.isChecked = account.excluded
        // Иконка и цвет будут установлены в setupEditMode() через соответствующие методы
    }

    private fun setupAmountEditTextListeners() {
        if (_binding == null) return
        val editTextAmount = binding.editTextAmount
        editTextAmount.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val currentText = editTextAmount.text.toString()
                if (currentText.isEmpty()) editTextAmount.setText("0")

                val separators = listOf('.', ',')
                if (currentText.isNotEmpty() && currentText.lastOrNull() in separators) {
                    editTextAmount.setText(currentText.substring(0, currentText.length - 1))
                }
            }
        }
        editTextAmount.addTextChangedListener(createAmountTextWatcher(editTextAmount))
    }

    private fun createAmountTextWatcher(editText: EditText): TextWatcher {
        return object : TextWatcher {
            private var isUpdating = false
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return
                val originalText = s?.toString() ?: ""
                var processedText = originalText
                val separators = listOf('.', ',')

                if (processedText.startsWith("0") && processedText.length > 1) {
                    val secondChar = processedText[1]
                    if (secondChar !in separators && secondChar.isDigit()) {
                        processedText = processedText.substring(1)
                    }
                } else if (separators.any { processedText.startsWith(it) }) {
                    processedText = "0$processedText" // Если начинается с точки/запятой, добавляем 0
                }

                val firstSeparatorIndex = processedText.indexOfFirst { it in separators }
                if (firstSeparatorIndex != -1) {
                    val prefix = processedText.substring(0, firstSeparatorIndex + 1)
                    val suffix = processedText.substring(firstSeparatorIndex + 1).replace(".", "").replace(",", "")
                    processedText = prefix + suffix
                }

                if (processedText != originalText) {
                    isUpdating = true
                    val selectionStart = editText.selectionStart
                    val lengthDiff = originalText.length - processedText.length
                    editText.setText(processedText)
                    val newCursorPos = selectionStart - lengthDiff
                    editText.setSelection(maxOf(0, minOf(newCursorPos, processedText.length)))
                    isUpdating = false
                }
            }
        }
    }

    private fun saveAccountAndNavigateBack() {
        if (_binding == null) return
        val accountName = binding.editTextAccountName.text.toString().trim()
        val amountString = binding.editTextAmount.text.toString().replace(',', '.') // Заменяем запятую на точку для парсинга
        val exclude = binding.switchExclude.isChecked

        if (!validateAccountName(accountName)) return
        val amount = parseAmount(amountString) ?: return



        hideKeyboard()

        val accountToSave = if (isEditMode && accountToEdit != null) {
            accountToEdit!!.copy(
                _title = Title(accountName),
                startBalance = amount,
                balance = accountToEdit!!.balance + (amount - accountToEdit!!.startBalance), // Пересчет баланса
                excluded = exclude,
                iconResourceId = selectedIconResId,
                colorHex = selectedColorHex
            ).also { viewModel.updateAccount(it) }
        } else {
            MoneyAccount(
                _title = Title(accountName),
                startBalance = amount,
                balance = amount, // Для нового счета баланс равен стартовому
                excluded = exclude,
                iconResourceId = selectedIconResId,
                colorHex = selectedColorHex
            ).also { viewModel.createAccount(it) }
        }
        findNavController().popBackStack()
    }

    private fun validateAccountName(accountName: String): Boolean {
        if (_binding == null) return false
        return if (accountName.isEmpty()) {
            binding.textInputLayoutAccountName.error = getString(R.string.error_name_empty) // Используй общий строковый ресурс
            binding.editTextAccountName.requestFocus()
            false
        } else {
            binding.textInputLayoutAccountName.error = null
            true
        }
    }

    private fun parseAmount(amountString: String): Double? {
        if (_binding == null) return null
        return try {
            val cleanAmountString = amountString.ifEmpty { "0" } // Если строка пустая, считаем 0
            val amount = cleanAmountString.toDouble()
            if (amount < 0) { // Опционально: проверка на отрицательную сумму
                binding.textInputLayoutAmount.error = getString(R.string.error_negative_amount)
                binding.editTextAmount.requestFocus()
                return null
            }
            binding.textInputLayoutAmount.error = null
            amount
        } catch (e: NumberFormatException) {
            binding.textInputLayoutAmount.error = getString(R.string.error_invalid_amount_format)
            binding.editTextAmount.requestFocus()
            null
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val currentFocus = activity?.currentFocus ?: view // Получаем текущий фокус или view фрагмента
        currentFocus?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun setupInitialFocusAndKeyboard() {
        if (_binding == null) return
        binding.editTextAccountName.run {
            post { // Даем время на отрисовку UI
                if (_binding != null) { // Проверка на случай уничтожения View до выполнения post
                    requestFocus()
                    val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                    imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Важно для предотвращения утечек памяти
    }
}