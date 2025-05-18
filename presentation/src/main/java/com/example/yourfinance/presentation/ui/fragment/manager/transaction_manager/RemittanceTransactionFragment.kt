package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

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
import androidx.lifecycle.Observer
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentTransactionRemittanceBinding
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.IconMap
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class RemittanceTransactionFragment : BaseTransactionInputFragment() {

    private var _binding: FragmentTransactionRemittanceBinding? = null
    private val binding get() = _binding!!

    override val viewModel: TransactionManagerViewModel
            by viewModels(ownerProducer = { requireParentFragment() })

    override val commonInputRoot: View
        get() = binding.includeCommonInput.root
    override val amountTextView: android.widget.TextView
        get() = binding.includeCommonInput.amountTextView
    override val selectedItemIcon: ShapeableImageView
        get() = binding.includeCommonInput.selectedItemIcon
    override val noteEditText: com.google.android.material.textfield.TextInputEditText
        get() = binding.includeCommonInput.noteEditText
    override val keypadView: View
        get() = binding.includeCommonInput.keypad

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 2
    }

    override fun getFragmentTransactionType(): TransactionType = TransactionType.REMITTANCE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionRemittanceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("Remittance", "onViewCreated")
        setupSpecificClickListeners()
        observeSpecificViewModel()
    }

    private fun setupSpecificClickListeners() {
        binding.includeRemittance.cardAccountFrom.setOnClickListener {
            if (viewModel.accountsList.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.no_accounts_available, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.requestAccountSelectionForRemittanceFrom()
            }
        }
        binding.includeRemittance.cardAccountTo.setOnClickListener {
            if (viewModel.accountsList.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.no_accounts_available, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.requestAccountSelectionForRemittanceTo()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Remittance", "onDestroyView")
        _binding = null
    }

    private fun updateAccountCard(
        account: MoneyAccount?,
        imageView: ShapeableImageView,
        nameView: android.widget.TextView,
        balanceView: android.widget.TextView,
        defaultName: String,
        defaultIconRes: Int = R.drawable.ic_plus,
        defaultBgColor: Int = Color.LTGRAY,
        defaultIconTint: Int = Color.DKGRAY
    ) {
        if (account != null) {
            nameView.text = account.title

            // String? → @DrawableRes Int
            val iconRes = account.iconResourceId
                ?.let { IconMap.idOf(it) }
                ?: R.drawable.ic_mobile_wallet
            imageView.setImageResource(iconRes)

            // colorHex: Int? ARGB
            val bgColor = account.colorHex ?: Color.YELLOW
            imageView.setBackgroundColor(bgColor)
            val tint = if (ColorUtils.calculateLuminance(bgColor) > 0.5) Color.BLACK else Color.WHITE
            imageView.setColorFilter(tint)

            balanceView.text = currencyFormatter.format(account.balance)
            balanceView.isVisible = true
        } else {
            nameView.text = defaultName
            imageView.setImageResource(defaultIconRes)
            imageView.setBackgroundColor(defaultBgColor)
            imageView.setColorFilter(defaultIconTint)
            balanceView.isVisible = false
        }
    }

    private fun observeSpecificViewModel() {
        viewModel.activeTransactionState.observe(viewLifecycleOwner, Observer { state ->
            val isActive = viewModel.currentTransactionType.value == TransactionType.REMITTANCE
            if (!isActive) return@Observer

            if (state is ActiveTransactionState.RemittanceState) {
                updateAccountCard(
                    state.selectedAccountFrom,
                    binding.includeRemittance.imageAccountFrom,
                    binding.includeRemittance.textAccountFrom,
                    binding.includeRemittance.textBalanceAccountFrom,
                    getString(R.string.placeholder_account_from)
                )
                updateAccountCard(
                    state.selectedAccountTo,
                    binding.includeRemittance.imageAccountTo,
                    binding.includeRemittance.textAccountTo,
                    binding.includeRemittance.textBalanceAccountTo,
                    getString(R.string.placeholder_account_to)
                )
            } else {
                // Initial или null
                updateAccountCard(
                    null,
                    binding.includeRemittance.imageAccountFrom,
                    binding.includeRemittance.textAccountFrom,
                    binding.includeRemittance.textBalanceAccountFrom,
                    getString(R.string.placeholder_account_from)
                )
                updateAccountCard(
                    null,
                    binding.includeRemittance.imageAccountTo,
                    binding.includeRemittance.textAccountTo,
                    binding.includeRemittance.textBalanceAccountTo,
                    getString(R.string.placeholder_account_to)
                )
            }

            // Общий ввод суммы
            if (commonInputRoot.isVisible) {
                updateAmountDisplayLayout()
            }
        })
    }

    override fun updateAmountDisplayLayout() {
        // Для Remittance скрываем левый icon
        selectedItemIcon.isVisible = false
    }

    override fun onIconClick() {
        // не используется
    }
}
