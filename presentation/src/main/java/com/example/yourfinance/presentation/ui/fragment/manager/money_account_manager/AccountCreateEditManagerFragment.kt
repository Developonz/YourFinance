package com.example.yourfinance.presentation.ui.fragment.manager.money_account_manager

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.IconMap
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentAccountCreateEditManagerBinding
import com.example.yourfinance.presentation.ui.adapter.ColorPickerAdapter
import com.example.yourfinance.presentation.ui.adapter.IconItem
import com.example.yourfinance.presentation.ui.adapter.SingleIconListAdapter
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

    private var isEditMode = false
    private var accountToEdit: MoneyAccount? = null

    /** Строковый ключ drawable */
    private var selectedIconKey: String? = null

    /** ARGB-цвет */
    @ColorInt private var selectedColor: Int = Color.parseColor("#03A9F4")

    private lateinit var availableColors: List<Int>
    private lateinit var iconListAdapter: SingleIconListAdapter
    private lateinit var colorSelectorAdapter: ColorPickerAdapter

    private val accountIconArrayResId = R.array.account_icons

    companion object {
        private const val GRID_LAYOUT_COLUMNS_COLORS     = 6
        private const val COLOR_PANEL_ANIMATION_DURATION = 150L
        private const val ICON_GRID_COLUMNS              = 5
        private val DEFAULT_FALLBACK_COLOR: Int          = Color.parseColor("#03A9F4")
    }

    private val amountFormat = DecimalFormat("#.##", DecimalFormatSymbols(Locale.US))

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountCreateEditManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1) цвет как string-array → parseColor
        loadAvailableColors()
        selectedColor = availableColors.firstOrNull() ?: DEFAULT_FALLBACK_COLOR

        // 2) режим
        isEditMode = args.accountId != -1L

        // 3) иконки + цвета
        setupIconRecyclerView()
        setupColorSelectorRecyclerView()

        // 4) Create/Edit
        if (isEditMode) setupEditMode() else setupCreateMode()

        // 5) остальное
        setupAmountEditTextListeners()
        setupListeners()
    }

    private fun loadAvailableColors() {
        availableColors = try {
            requireContext().resources.getStringArray(R.array.available_colors_category_account)
                .mapNotNull {
                    try { Color.parseColor(it) } catch (_: Exception) { null }
                }
        } catch (e: Exception) {
            Log.e("AccountCreateEdit", "Error loading colors", e)
            emptyList()
        }.ifEmpty { listOf(DEFAULT_FALLBACK_COLOR) }
    }

    private fun setupIconRecyclerView() {
        iconListAdapter = SingleIconListAdapter(
            requireContext(),
            selectedColor,
            selectedIconKey
        ) { clicked ->
            val old = selectedIconKey
            selectedIconKey = clicked.resourceId
            if (old != selectedIconKey) {
                iconListAdapter.setSelectedIcon(selectedIconKey)
                updateIconPreviewImage()
            }
        }
        binding.recyclerViewAccountIcons.apply {
            layoutManager = GridLayoutManager(requireContext(), ICON_GRID_COLUMNS)
            adapter = iconListAdapter
            itemAnimator = null
        }
        loadIconsIntoAdapter()
    }

    private fun loadIconsIntoAdapter() {
        val list = mutableListOf<IconItem>()
        requireContext().resources.obtainTypedArray(accountIconArrayResId).use { ta ->
            for (i in 0 until ta.length()) {
                val drawableId = ta.getResourceId(i, 0)
                if (drawableId != 0) {
                    val key = try {
                        requireContext().resources.getResourceEntryName(drawableId)
                    } catch (_: Exception) {
                        "icon_$drawableId"
                    }
                    list += IconItem(resourceId = key, name = key)
                }
            }
        }
        iconListAdapter.submitList(list)
    }

    private fun setupCreateMode() {
        (requireActivity() as? AppCompatActivity)
            ?.supportActionBar?.title = getString(R.string.add_account_title)
        binding.buttonConfirmAccount.text = getString(R.string.create_account)

        // дефолтная иконка = первая
        if (selectedIconKey == null) {
            requireContext().resources.obtainTypedArray(accountIconArrayResId).use { ta ->
                if (ta.length() > 0) {
                    val id0 = ta.getResourceId(0, 0)
                    selectedIconKey = requireContext().resources.getResourceEntryName(id0)
                }
            }
        }

        iconListAdapter.setSelectedIcon(selectedIconKey)
        updateColorIndicator()
        updateIconPreviewBackground()
        updateIconPreviewImage()
        setupInitialFocus()
    }

    private fun setupEditMode() {
        binding.buttonConfirmAccount.text = getString(R.string.save_changes)
        (requireActivity() as? AppCompatActivity)
            ?.supportActionBar?.title = getString(R.string.edit_account_title)

        lifecycleScope.launch {
            accountToEdit = viewModel.loadAccountById(args.accountId)
            accountToEdit?.let { acc ->
                binding.editTextAccountName.setText(acc.title)
                binding.editTextAmount.setText(amountFormat.format(acc.startBalance))
                binding.switchExclude.isChecked = acc.excluded

                selectedIconKey = acc.iconResourceId
                selectedColor    = acc.colorHex ?: DEFAULT_FALLBACK_COLOR

                iconListAdapter.setSelectedIcon(selectedIconKey)
                iconListAdapter.setSelectedColor(selectedColor)

                updateColorIndicator()
                updateIconPreviewBackground()
                updateIconPreviewImage()
            } ?: run {
                Toast.makeText(requireContext(), "Account not found", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupColorSelectorRecyclerView() {
        colorSelectorAdapter = ColorPickerAdapter(availableColors) { color ->
            selectedColor = color
            updateColorIndicator()
            updateIconPreviewBackground()
            if (selectedIconKey != null) {
                updateIconPreviewImage()
                iconListAdapter.setSelectedColor(selectedColor)
            }
            toggleColorPanel(false)
        }
        binding.recyclerViewColorSelectorAccount.apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_LAYOUT_COLUMNS_COLORS)
            adapter = colorSelectorAdapter
        }
    }

    private fun updateColorIndicator() {
        (binding.viewColorIndicatorAccount.background as? GradientDrawable)?.apply {
            setColor(selectedColor)
            setStroke(
                if (ColorUtils.calculateLuminance(selectedColor) > 0.9) 2 else 0,
                Color.LTGRAY
            )
        }
    }

    private fun updateIconPreviewBackground() {
        binding.cardIconPreviewWrapperAccount.setCardBackgroundColor(selectedColor)
        applyStrokeToCard(binding.cardIconPreviewWrapperAccount, selectedColor)
    }

    private fun updateIconPreviewImage() {
        binding.imageViewSelectedIconPreviewAccount.apply {
            selectedIconKey?.let { key ->
                setImageResource(IconMap.idOf(key))
                val tint = if (ColorUtils.calculateLuminance(selectedColor) > 0.5) Color.BLACK else Color.WHITE
                setColorFilter(tint)
                visibility = View.VISIBLE
            } ?: run {
                visibility = View.GONE
            }
        }
    }

    private fun applyStrokeToCard(card: MaterialCardView, @ColorInt color: Int) {
        if (ColorUtils.calculateLuminance(color) > 0.9) {
            card.strokeWidth = 3
            card.strokeColor = Color.LTGRAY
        } else {
            card.strokeWidth = 0
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun toggleColorPanel(show: Boolean? = null) {
        val panel = binding.cardColorSelectorPanelAccount
        val arrow = binding.imageViewColorArrowAccount
        val expand = show ?: (panel.visibility == View.GONE)
        TransitionManager.beginDelayedTransition(
            binding.fixedFieldsContainer, AutoTransition().apply { duration = COLOR_PANEL_ANIMATION_DURATION }
        )
        if (expand) {
            panel.visibility = View.VISIBLE
            arrow.animate().rotation(180f).start()
        } else {
            panel.visibility = View.GONE
            arrow.animate().rotation(0f).start()
        }
    }

    private fun setupAmountEditTextListeners() {
        val et = binding.editTextAmount
        et.onFocusChangeListener = View.OnFocusChangeListener { _, has ->
            if (!has) {
                var txt = et.text.toString()
                if (txt.isEmpty()) txt = "0"
                if (txt.lastOrNull() in listOf('.', ',')) txt = txt.dropLast(1)
                et.setText(txt)
            }
        }
        et.addTextChangedListener(object : TextWatcher {
            var updating = false
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, a: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (updating) return
                var orig = s.toString()
                var proc = orig
                if (proc.startsWith("0") && proc.length > 1 && proc[1].isDigit()) proc = proc.drop(1)
                if (proc.startsWith('.') || proc.startsWith(',')) proc = "0$proc"
                val idx = proc.indexOfFirst { it=='.' || it==',' }
                if (idx>=0) {
                    val pre = proc.substring(0, idx+1)
                    val suf = proc.substring(idx+1).replace(".", "").replace(",", "")
                    proc = pre + suf
                }
                if (proc != orig) {
                    updating = true
                    val sel = et.selectionStart
                    val diff = orig.length - proc.length
                    et.setText(proc)
                    et.setSelection((sel - diff).coerceIn(0, proc.length))
                    updating = false
                }
            }
        })
    }

    private fun setupListeners() {
        binding.buttonConfirmAccount.setOnClickListener { saveAndExit() }
        binding.colorSelectorContainerAccount.setOnClickListener { toggleColorPanel() }
    }

    private fun saveAndExit() {
        val name = binding.editTextAccountName.text.toString().trim()
        val amtStr = binding.editTextAmount.text.toString().replace(',', '.')
        val exclude = binding.switchExclude.isChecked

        if (name.isEmpty()) {
            binding.textInputLayoutAccountName.error = getString(R.string.error_name_empty)
            binding.editTextAccountName.requestFocus()
            return
        }
        val amount = amtStr.toDoubleOrNull() ?: run {
            binding.textInputLayoutAmount.error = getString(R.string.error_invalid_amount_format)
            binding.editTextAmount.requestFocus()
            return
        }
        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(view?.windowToken, 0)

        if (isEditMode && accountToEdit != null) {
            accountToEdit!!.apply {
                title           = name
                startBalance    = amount
                balance         = startBalance + (amount - this.startBalance)
                excluded        = exclude
                iconResourceId  = selectedIconKey
                colorHex        = selectedColor
                viewModel.updateAccount(this)
            }
        } else {
            MoneyAccount(
                _title           = Title(name),
                startBalance    = amount,
                balance         = amount,
                excluded        = exclude,
                iconResourceId  = selectedIconKey,
                colorHex        = selectedColor
            ).also { viewModel.createAccount(it) }
        }
        findNavController().popBackStack()
    }

    private fun setupInitialFocus() {
        binding.editTextAccountName.post {
            binding.editTextAccountName.requestFocus()
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showSoftInput(binding.editTextAccountName, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
