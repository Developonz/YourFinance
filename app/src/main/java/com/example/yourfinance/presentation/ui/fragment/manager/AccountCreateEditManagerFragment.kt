package com.example.yourfinance.presentation.ui.fragment.manager

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.yourfinance.R
import com.example.yourfinance.databinding.FragmentAccountCreateEditManagerBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

class AccountCreateEditManagerFragment : Fragment() {

    private var _binding: FragmentAccountCreateEditManagerBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionsViewModel by activityViewModels()
    private val args: AccountCreateEditManagerFragmentArgs by navArgs()

    private var currentAccountId: Long = -1L
    private var isEditMode = false
    private var accountToEdit: MoneyAccount? = null


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

        currentAccountId = args.accountId
        isEditMode = currentAccountId != -1L

        setupAmountEditTextListeners()
        setupConfirmButtonListener()

        if (isEditMode) {
            setupEditMode()
        } else {
            setupCreateMode()
        }
    }

    private fun setupEditMode() {
        binding.buttonConfirmAccount.text = getString(R.string.save_changes)

        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.edit_account_title)

        viewLifecycleOwner.lifecycleScope.launch {
            val account = viewModel.getAccountById(currentAccountId)
            if (view != null) {
                if (account != null) {
                    accountToEdit = account
                    populateUI(account)
                } else {
                    Toast.makeText(context, "Счет не найден", Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun setupCreateMode() {
        binding.buttonConfirmAccount.text = getString(R.string.create_account)
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_account_title)
        setupInitialFocusAndKeyboard()
    }


    /**
     * Заполняет поля ввода данными из существующего счета.
     */
    private fun populateUI(account: MoneyAccount) {
        binding.editTextAccountName.setText(account.title)
        binding.editTextAmount.setText(amountFormat.format(account.startBalance))
        binding.switchExclude.isChecked = account.excluded
        binding.editTextAccountName.clearFocus()
        binding.editTextAmount.clearFocus()
        hideKeyboard()
    }

    /**
     * Sets up the focus change listener and text watcher for the amount input field.
     */
    private fun setupAmountEditTextListeners() {
        val editTextAmount = binding.editTextAmount

        editTextAmount.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val currentText = editTextAmount.text.toString()
                if (currentText.isEmpty()) editTextAmount.setText("0")

                val separators = listOf('.', ',')
                if (currentText.isNotEmpty() && currentText[currentText.length-1] in separators) {
                    editTextAmount.setText(currentText.substring(0, currentText.length - 1))
                }
            }
        }

        editTextAmount.addTextChangedListener(createAmountTextWatcher(editTextAmount))
    }

    /**
     * Creates and returns a TextWatcher for the amount input field.
     * Handles preventing leading zeros (unless followed by a decimal separator)
     * and ensures only one decimal separator is present.
     */
    private fun createAmountTextWatcher(editText: EditText): TextWatcher {
        return object : TextWatcher {
            private var isUpdating = false // Flag to prevent recursion

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (isUpdating) return

                val originalText = s?.toString() ?: ""
                var processedText = originalText
                val separators = listOf('.', ',')

                if (processedText.startsWith("0") && processedText.length > 1) {
                    val secondChar = processedText[1]
                    if (secondChar != '.' && secondChar != ',') {
                        if (secondChar.isDigit()) {
                            processedText = processedText.substring(1)
                        }
                    }
                } else if (separators.any { processedText.startsWith(it) }) {
                    processedText = ""
                }

                val firstSeparatorIndex = processedText.indexOfFirst { it in separators }
                if (firstSeparatorIndex != -1) {
                    val prefix = processedText.substring(0, firstSeparatorIndex + 1)
                    val suffix = processedText.substring(firstSeparatorIndex + 1)
                        .replace(".", "")
                        .replace(",", "")
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

    /**
     * Sets up the click listener for the confirm button.
     * Handles input validation and account creation OR update.
     */
    private fun setupConfirmButtonListener() {
        binding.buttonConfirmAccount.setOnClickListener {
            val accountName = binding.editTextAccountName.text.toString().trim()
            val amountString = binding.editTextAmount.text.toString()
            val exclude = binding.switchExclude.isChecked

            if (!validateAccountName(accountName)) return@setOnClickListener
            val amount = parseAmount(amountString) ?: return@setOnClickListener

            saveAccountAndNavigateBack(accountName, amount, exclude)
        }
    }

    /**
     * Validates the account name input field.
     * Sets an error if empty and returns false, otherwise clears error and returns true.
     */
    private fun validateAccountName(accountName: String): Boolean {
        return if (accountName.isEmpty()) {
            binding.textInputLayoutAccountName.error = "Название не может быть пустым"
            binding.editTextAccountName.requestFocus()
            false
        } else {
            binding.textInputLayoutAccountName.error = null
            true
        }
    }

    /**
     * Parses the amount string into a Double.
     * Handles potential NumberFormatException and sets an error on the layout.
     * Returns the parsed amount or null if parsing fails.
     */
    private fun parseAmount(amountString: String): Double? {
        var amount = 0.0
        return try {
            val cleanAmountString = amountString.replace(',', '.')
            if (cleanAmountString.isNotEmpty() && cleanAmountString != ".") {
                amount = cleanAmountString.toDouble()
            }
            binding.textInputLayoutAmount.error = null
            amount
        } catch (e: NumberFormatException) {
            binding.textInputLayoutAmount.error = "Неверный формат суммы"
            binding.editTextAmount.requestFocus()
            null
        }
    }

    /**
     * Hides the soft keyboard.
     */
    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        val token = binding.root.windowToken
        token?.let { imm?.hideSoftInputFromWindow(it, 0) }
    }

    /**
     * Creates OR updates the MoneyAccount using the ViewModel and navigates back.
     */
    private fun saveAccountAndNavigateBack(accountName: String, amount: Double, exclude: Boolean) {
        hideKeyboard()
        if (isEditMode && accountToEdit != null) {
            val updatedAccount = accountToEdit!!.copy(
                _title = accountName,
                startBalance = amount,
                balance = accountToEdit!!.balance + (amount - accountToEdit!!.startBalance),// Или возможно нужно пересчитать текущий баланс? Зависит от логики. Пока меняем стартовый.
                excluded = exclude
            )
            viewModel.updateAccount(updatedAccount)
        } else {
            val newAccount = MoneyAccount(
                _title = accountName,
                startBalance = amount,
                excluded = exclude
            )
            viewModel.createAccount(newAccount)
        }
        findNavController().popBackStack()
    }


    /**
     * Requests focus on the account name field and shows the soft keyboard.
     */
    private fun setupInitialFocusAndKeyboard() {
        binding.editTextAccountName.run {
            post {
                requestFocus()
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
