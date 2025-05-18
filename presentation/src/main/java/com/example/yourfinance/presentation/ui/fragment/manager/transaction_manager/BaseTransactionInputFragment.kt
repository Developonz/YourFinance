// BaseTransactionInputFragment.kt
package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import com.example.yourfinance.presentation.R
import com.example.yourfinance.domain.model.TransactionType

abstract class BaseTransactionInputFragment : Fragment() {

    protected abstract val viewModel: TransactionManagerViewModel

    protected val dateButtonFormatterSameYear = DateTimeFormatter.ofPattern("d MMM.", Locale("ru"))
    protected val dateButtonFormatterDifferentYear = DateTimeFormatter.ofPattern("d MMM.\nyyyy", Locale("ru"))
    protected var dateButton: MaterialButton? = null

    protected abstract val commonInputRoot: View
    protected abstract val amountTextView: android.widget.TextView
    protected abstract val selectedItemIcon: android.widget.ImageView
    protected abstract val noteEditText: TextInputEditText
    protected abstract val keypadView: View

    protected lateinit var confirmButton: MaterialButton

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dateButton = keypadView.findViewById(R.id.date_button)
        confirmButton = keypadView.findViewById(R.id.key_confirm)

        setupCommonClickListeners()
        observeCommonViewModel()
    }

    protected open fun setupCommonClickListeners() {
        keypadView.findViewById<View>(R.id.key_0).setOnClickListener { viewModel.handleKeypadInput("0") }
        keypadView.findViewById<View>(R.id.key_1).setOnClickListener { viewModel.handleKeypadInput("1") }
        keypadView.findViewById<View>(R.id.key_2).setOnClickListener { viewModel.handleKeypadInput("2") }
        keypadView.findViewById<View>(R.id.key_3).setOnClickListener { viewModel.handleKeypadInput("3") }
        keypadView.findViewById<View>(R.id.key_4).setOnClickListener { viewModel.handleKeypadInput("4") }
        keypadView.findViewById<View>(R.id.key_5).setOnClickListener { viewModel.handleKeypadInput("5") }
        keypadView.findViewById<View>(R.id.key_6).setOnClickListener { viewModel.handleKeypadInput("6") }
        keypadView.findViewById<View>(R.id.key_7).setOnClickListener { viewModel.handleKeypadInput("7") }
        keypadView.findViewById<View>(R.id.key_8).setOnClickListener { viewModel.handleKeypadInput("8") }
        keypadView.findViewById<View>(R.id.key_9).setOnClickListener { viewModel.handleKeypadInput("9") }
        keypadView.findViewById<View>(R.id.key_dot).setOnClickListener { viewModel.handleKeypadInput(".") }
        keypadView.findViewById<View>(R.id.key_del).setOnClickListener { viewModel.handleKeypadInput("DEL") }
        keypadView.findViewById<View>(R.id.key_plus).setOnClickListener { viewModel.handleKeypadInput("+") }
        keypadView.findViewById<View>(R.id.key_minus).setOnClickListener { viewModel.handleKeypadInput("-") }

        if (::confirmButton.isInitialized) {
            confirmButton.setOnClickListener {
                val hasOperator = viewModel.hasOperatorInAmount.value == true
                Log.d("BaseFrag", "Confirm button clicked. Has operator: $hasOperator")
                if (hasOperator) {
                    viewModel.evaluateAndDisplayAmount()
                } else {
                    viewModel.saveTransaction()
                }
            }
        } else {
            Log.e("BaseFrag", "confirmButton not initialized when setting click listener.")
        }

        dateButton?.setOnClickListener { showDatePicker() }
        noteEditText.addTextChangedListener { editable -> viewModel.setNote(editable.toString()) }
        selectedItemIcon.setOnClickListener { onIconClick() }
    }

    protected open fun observeCommonViewModel() {
        Log.d("BaseFrag", "Setting up common observers.")

        viewModel.showInputSection.observe(viewLifecycleOwner, Observer { shouldShowInput ->
            val isActiveFragmentAccordingToVM = viewModel.currentTransactionType.value == getFragmentTransactionType()
            if (isActiveFragmentAccordingToVM) {
                commonInputRoot.isVisible = shouldShowInput
                if (shouldShowInput) {
                    updateAmountDisplayLayout()
                    amountTextView.text = formatAmountForDisplay(viewModel.amountString.value ?: "0")
                    viewModel.date.value?.let { updateDateButtonText(it) }
                }
            } else {
                if (commonInputRoot.isVisible) {
                    commonInputRoot.isVisible = false
                }
            }
        })

        viewModel.hasOperatorInAmount.observe(viewLifecycleOwner, Observer { hasOperator ->
            if (::confirmButton.isInitialized) {
                if (hasOperator) {
                    confirmButton.text = "="
                    confirmButton.icon = null
                } else {
                    confirmButton.text = ""
                    confirmButton.setIconResource(R.drawable.ic_checkmark)
                }
            } else {
                Log.w("BaseFrag", "confirmButton not initialized when hasOperatorInAmount changed.")
            }
        })

        viewModel.amountString.observe(viewLifecycleOwner, Observer { amountStr ->
            if (commonInputRoot.isVisible) {
                amountTextView.text = formatAmountForDisplay(amountStr ?: "0")
            }
        })

        viewModel.date.observe(viewLifecycleOwner, Observer { date ->
            if (commonInputRoot.isVisible) {
                date?.let { updateDateButtonText(it) }
            }
        })

        viewModel.note.observe(viewLifecycleOwner, Observer { note ->
            if (commonInputRoot.isVisible) {
                if (noteEditText.text.toString() != note) {
                    noteEditText.setText(note ?: "")
                }
            }
        })

        viewModel.accountsList.observe(viewLifecycleOwner, Observer {
            Log.d("BaseFrag", "Observed accountsList update: ${it?.size ?: 0} accounts.")
        })

        viewModel.errorMessageEvent.observe(viewLifecycleOwner, Observer { errorMessage ->
            Log.w("BaseFrag", "Observed errorMessageEvent: $errorMessage")
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
        })

        viewModel.criticalErrorEvent.observe(viewLifecycleOwner, Observer { errorMessage ->
            Log.e("BaseFrag", "Observed criticalErrorEvent: $errorMessage")
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            findNavController().navigateUp()
        })
    }

    protected abstract fun getFragmentTransactionType(): TransactionType
    protected abstract fun updateAmountDisplayLayout()
    protected abstract fun onIconClick()

    protected open fun formatAmountForDisplay(amountStr: String): String {
        return if (amountStr.isEmpty() || amountStr == "." || amountStr == "0.") "0" else amountStr.replace(",", ".")
    }

    protected open fun updateDateButtonText(date: LocalDate) {
        if (dateButton != null) {
            val today = LocalDate.now()
            val yesterday = today.minusDays(1)
            val buttonText = when (date) {
                today -> "Сегодня"
                yesterday -> "Вчера"
                else -> if (date.year == today.year) date.format(dateButtonFormatterSameYear) else date.format(dateButtonFormatterDifferentYear)
            }
            dateButton?.text = buttonText
        }
    }

    protected open fun showDatePicker() {
        val currentDate = viewModel.date.value ?: LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                viewModel.setDate(LocalDate.of(year, month + 1, dayOfMonth))
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        )
        datePickerDialog.show()
    }

    protected fun showAccountSelectionDialog(isSelectingFromAccount: Boolean) {
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
                    viewModel.selectAccountFrom(selectedAccount)
                } else {
                    viewModel.selectAccountTo(selectedAccount)
                }
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
