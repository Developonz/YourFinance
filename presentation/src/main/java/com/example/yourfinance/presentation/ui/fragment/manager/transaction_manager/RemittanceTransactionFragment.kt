package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

// import android.content.res.ColorStateList // Заменим на setBackgroundColor
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
// import androidx.navigation.fragment.findNavController // Навигация через ViewModel
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentTransactionRemittanceBinding
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.google.android.material.imageview.ShapeableImageView
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale

@AndroidEntryPoint
class RemittanceTransactionFragment : BaseTransactionInputFragment() {

    private var _binding: FragmentTransactionRemittanceBinding? = null
    private val binding get() = _binding!!

    override val viewModel: TransactionManagerViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override val commonInputRoot: View get() = binding.includeCommonInput.root
    override val amountTextView: android.widget.TextView get() = binding.includeCommonInput.amountTextView
    override val selectedItemIcon: ShapeableImageView get() = binding.includeCommonInput.selectedItemIcon // Уточнили тип
    override val noteEditText: com.google.android.material.textfield.TextInputEditText get() = binding.includeCommonInput.noteEditText
    override val keypadView: View get() = binding.includeCommonInput.keypad

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale("ru", "RU")).apply {
        minimumFractionDigits = 0 // Обычно баланс целыми или до 2 знаков
        maximumFractionDigits = 2
    }

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

    private fun setupSpecificClickListeners() { // Сделал private, т.к. вызывается только из onViewCreated
        binding.includeRemittance.cardAccountFrom.setOnClickListener {
            if (viewModel.accountsList.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.no_accounts_available, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.requestAccountSelectionForRemittanceFrom()
        }
        binding.includeRemittance.cardAccountTo.setOnClickListener {
            if (viewModel.accountsList.value.isNullOrEmpty()) {
                Toast.makeText(requireContext(), R.string.no_accounts_available, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.requestAccountSelectionForRemittanceTo()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i("Frag(${getFragmentTransactionType().name})", "onDestroyView")
        _binding = null
    }

    private fun updateAccountCard(
        account: MoneyAccount?,
        imageView: ShapeableImageView,
        nameTextView: android.widget.TextView,
        balanceTextView: android.widget.TextView,
        defaultName: String,
        defaultIconResId: Int = R.drawable.ic_plus, // Иконка плюса для выбора
        defaultBackgroundColor: Int = Color.LTGRAY, // Светло-серый фон для выбора
        defaultIconTintColor: Int = Color.DKGRAY   // Темно-серая иконка для выбора
    ) {
        if (account != null) {
            nameTextView.text = account.title
            imageView.setImageResource(account.iconResourceId ?: R.drawable.ic_mobile_wallet) // Иконка счета

            val colorHex = account.colorHex ?: "#FFFF00" // Желтый по умолчанию, если нет цвета
            var parsedColor = Color.YELLOW // Fallback color
            try {
                parsedColor = Color.parseColor(colorHex)
            } catch (e: IllegalArgumentException) {
                Log.w("FragRemit", "Invalid color hex: $colorHex for account ${account.title}. Using default yellow.")
            }

            imageView.setBackgroundColor(parsedColor) // Устанавливаем цвет фона
            val iconTintColor = if (ColorUtils.calculateLuminance(parsedColor) > 0.5) Color.BLACK else Color.WHITE
            imageView.setColorFilter(iconTintColor) // Устанавливаем цвет самой иконки (src)

            balanceTextView.text = currencyFormatter.format(account.balance)
            balanceTextView.isVisible = true
        } else {
            nameTextView.text = defaultName
            imageView.setImageResource(defaultIconResId)
            imageView.setBackgroundColor(defaultBackgroundColor)
            imageView.setColorFilter(defaultIconTintColor)
            balanceTextView.isVisible = false
        }
    }


    private fun observeSpecificViewModel() { // Сделал private
        Log.d("Frag(${getFragmentTransactionType().name})", "Setting up specific observers.")

        viewModel.activeTransactionState.observe(viewLifecycleOwner, Observer { state ->
            Log.d("Frag(${getFragmentTransactionType().name})", "Observed activeTransactionState change: $state")
            val isActiveRemittance = viewModel.currentTransactionType.value == TransactionType.REMITTANCE

            if (isActiveRemittance) { // Обновляем UI только если это текущая активная вкладка
                if (state is ActiveTransactionState.RemittanceState) {
                    Log.d("Frag(${getFragmentTransactionType().name})", "Updating UI for Remittance State.")
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
                } else if (state is ActiveTransactionState.InitialState || state == null) {
                    // Если это InitialState И текущий тип - Перевод, то отображаем плейсхолдеры
                    Log.d("Frag(${getFragmentTransactionType().name})", "Updating UI for InitialState (Remittance Active).")
                    updateAccountCard(null, binding.includeRemittance.imageAccountFrom, binding.includeRemittance.textAccountFrom, binding.includeRemittance.textBalanceAccountFrom, getString(R.string.placeholder_account_from))
                    updateAccountCard(null, binding.includeRemittance.imageAccountTo, binding.includeRemittance.textAccountTo, binding.includeRemittance.textBalanceAccountTo, getString(R.string.placeholder_account_to))
                }
                // Обновление общего layout'а ввода суммы, если он видим
                if (commonInputRoot.isVisible) {
                    updateAmountDisplayLayout()
                }
            } else {
                // Состояние изменилось, но это не для текущего активного фрагмента "Перевод"
                // Можно ничего не делать, или скрыть/очистить специфичные для перевода UI элементы, если они видны
                Log.d("Frag(${getFragmentTransactionType().name})", "State change ignored, not active Remittance tab.")
            }
        })
    }

    override fun updateAmountDisplayLayout() {
        if (_binding == null) {
            Log.w("Frag(${getFragmentTransactionType().name})", "updateAmountDisplayLayout called but binding is null!")
            return
        }
        // Иконка selectedItemIcon из common_input здесь НЕ НУЖНА для переводов
        val iconView = selectedItemIcon // Это ImageView из include_common_input
        val isActiveFragment = viewModel.currentTransactionType.value == getFragmentTransactionType()
        val shouldShowCommonInput = commonInputRoot.isVisible

        if (isActiveFragment && shouldShowCommonInput) {
            iconView.isVisible = false // Скрываем иконку слева от суммы для Переводов
            // Log.d("Frag(${getFragmentTransactionType().name})", "selectedItemIcon in common_input hidden for Remittance.")
        } else {
            iconView.isVisible = false // Также скрываем, если панель ввода не видна или фрагмент неактивен
        }
    }

    override fun onIconClick() {
        // Для переводов клик по этой иконке (которая должна быть скрыта) не обрабатывается
        Log.d("Frag(${getFragmentTransactionType().name})", "Icon click ignored for Remittance.")
    }

    // Метод showAccountSelectionBottomSheet больше не нужен здесь, т.к. показ инициируется через ViewModel
    // private fun showAccountSelectionBottomSheet(isSelectingFromAccount: Boolean) { ... }
}