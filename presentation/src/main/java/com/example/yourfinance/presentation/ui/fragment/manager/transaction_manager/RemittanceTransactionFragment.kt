package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentTransactionRemittanceBinding
import com.example.yourfinance.domain.model.TransactionType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RemittanceTransactionFragment : BaseTransactionInputFragment() {

    private var _binding: FragmentTransactionRemittanceBinding? = null
    private val binding get() = _binding!!

    override val viewModel: TransactionManagerViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override val commonInputRoot: View get() = binding.includeCommonInput.root
    override val amountTextView: android.widget.TextView get() = binding.includeCommonInput.amountTextView
    override val selectedItemIcon: android.widget.ImageView get() = binding.includeCommonInput.selectedItemIcon
    override val noteEditText: com.google.android.material.textfield.TextInputEditText get() = binding.includeCommonInput.noteEditText
    override val keypadView: View get() = binding.includeCommonInput.keypad


    override fun getFragmentTransactionType() = TransactionType.REMITTANCE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionRemittanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("Frag(${getFragmentTransactionType().name})", "onViewCreated")

        setupSpecificClickListeners()
        observeSpecificViewModel()
    }

    protected open fun setupSpecificClickListeners() {
        binding.includeRemittance.cardAccountFrom.setOnClickListener {
            showAccountSelectionBottomSheet(isSelectingFromAccount = true)
        }
        binding.includeRemittance.cardAccountTo.setOnClickListener {
            showAccountSelectionBottomSheet(isSelectingFromAccount = false)
        }
    }

    private fun showAccountSelectionBottomSheet(isSelectingFromAccount: Boolean) {
        val accounts = viewModel.accountsList.value ?: emptyList()
        if (accounts.isEmpty()) {
            Toast.makeText(requireContext(), R.string.no_accounts_available, Toast.LENGTH_SHORT).show()
            return
        }

        val currentRemittanceState = viewModel.activeTransactionState.value as? ActiveTransactionState.RemittanceState
        val currentSelectedAccountId = if (isSelectingFromAccount) {
            currentRemittanceState?.selectedAccountFrom?.id
        } else {
            currentRemittanceState?.selectedAccountTo?.id
        }

        val title = if (isSelectingFromAccount) "Счет списания" else "Счет зачисления"
        // Можно передать title в BottomSheet, если добавить туда поле для заголовка,
        // но в нашем текущем bottom_sheet_account_selection.xml заголовок статичный.

        val bottomSheet = AccountSelectionBottomSheet.newInstance(
            accounts = accounts,
            selectedAccountId = currentSelectedAccountId,
            onAccountSelectedCallback = { selectedAccount ->
                if (isSelectingFromAccount) {
                    viewModel.selectAccountFrom(selectedAccount)
                } else {
                    viewModel.selectAccountTo(selectedAccount)
                }
            },
            onSettingsClickedCallback = {
                // TODO: Заменить R.id.action_to_account_management на ваш реальный action ID
                try {
                    // Пример навигации. Замените на ваш action_id.
                    // findNavController().navigate(R.id.action_transactionContainerFragment_to_moneyAccountManagerFragment)
                    Toast.makeText(context, "Переход к настройкам счетов (TODO)", Toast.LENGTH_SHORT).show()
                    Log.d("Frag(${getFragmentTransactionType().name})","Переход к настройкам счетов (TODO: implement navigation)")
                } catch (e: Exception) {
                    Log.e("Frag(${getFragmentTransactionType().name})", "Navigation to account settings failed", e)
                    Toast.makeText(context, "Ошибка навигации к настройкам счетов", Toast.LENGTH_SHORT).show()
                }
            }
        )
        bottomSheet.show(parentFragmentManager, AccountSelectionBottomSheet.TAG)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Frag(${getFragmentTransactionType().name})", "onDestroyView")
        _binding = null
    }

    // --- Observation ---
    protected open fun observeSpecificViewModel() {
        Log.d("Frag(${getFragmentTransactionType().name})", "Setting up specific observers.")

        viewModel.activeTransactionState.observe(viewLifecycleOwner, Observer { state ->
            Log.d("Frag(${getFragmentTransactionType().name})", "Observed activeTransactionState change: $state")

            if (state is ActiveTransactionState.RemittanceState) {
                Log.d("Frag(${getFragmentTransactionType().name})", "Updating UI for Remittance State.")
                binding.includeRemittance.textAccountFrom.text = state.selectedAccountFrom?.title ?: "Счет списания"
//                binding.includeRemittance.imageAccountFrom.setImageResource(state.selectedAccountFrom?.iconResId ?: R.drawable.ic_mobile_wallet) // ВЕРНУЛ ВАШ ВРЕМЕННЫЙ КОД
                binding.includeRemittance.imageAccountFrom.setImageResource(R.drawable.ic_mobile_wallet) // ВЕРНУЛ ВАШ ВРЕМЕННЫЙ КОД
                binding.includeRemittance.textAccountTo.text = state.selectedAccountTo?.title ?: "Счет зачисления"
//                binding.includeRemittance.imageAccountTo.setImageResource(state.selectedAccountTo?.iconResId ?: R.drawable.ic_mobile_wallet) // ВЕРНУЛ ВАШ ВРЕМЕННЫЙ КОД
                binding.includeRemittance.imageAccountTo.setImageResource(R.drawable.ic_mobile_wallet) // ВЕРНУЛ ВАШ ВРЕМЕННЫЙ КОД
                if (commonInputRoot.isVisible) {
                    updateAmountDisplayLayout()
                }
            } else if (state is ActiveTransactionState.InitialState) {
                binding.includeRemittance.textAccountFrom.text = "Счет списания"
                binding.includeRemittance.textAccountTo.text = "Счет зачисления"
                binding.includeRemittance.imageAccountFrom.setImageResource(R.drawable.ic_mobile_wallet)
                binding.includeRemittance.imageAccountTo.setImageResource(R.drawable.ic_mobile_wallet)
                if (commonInputRoot.isVisible) {
                    updateAmountDisplayLayout()
                }
                Log.d("Frag(${getFragmentTransactionType().name})", "Active state is InitialState.")
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
            is ActiveTransactionState.RemittanceState -> {
                val state = viewModel.activeTransactionState.value as ActiveTransactionState.RemittanceState
                iconView.setImageResource(R.drawable.ic_mobile_wallet)
                iconView.isClickable = false
                iconView.isVisible = true
            }
            ActiveTransactionState.InitialState -> {
                if (commonInputRoot.isVisible) {
                    iconView.setImageResource(R.drawable.ic_mobile_wallet)
                    iconView.isClickable = false
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
                Log.w("Frag(${getFragmentTransactionType().name})", "Ignoring non-Remittance state: ${viewModel.activeTransactionState.value}")
            }
        }
    }

    override fun onIconClick() {
        Log.d("Frag(${getFragmentTransactionType().name})", "Icon click ignored.")
    }
}