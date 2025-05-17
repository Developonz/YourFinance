package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.presentation.databinding.BottomSheetAccountSelectionBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.ui.adapter.AccountSelectionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AccountSelectionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAccountSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var accountsAdapter: AccountSelectionAdapter

    // Аргументы и коллбэки
    private var accountsList: List<MoneyAccount> = emptyList()
    private var initiallySelectedAccountId: Long? = null
    private var onAccountSelected: ((MoneyAccount) -> Unit)? = null
    private var onSettingsClicked: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetAccountSelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        loadArguments() // Загружаем аргументы после создания View

        binding.buttonAccountSettings.setOnClickListener {
            onSettingsClicked?.invoke()
            dismiss()
        }
        // Если бы была кнопка отмены:
        // binding.buttonCancelBottomSheet.setOnClickListener { dismiss() }

        // Установка заголовка, если нужно его менять
        // binding.textBottomSheetTitle.text = "Нужный заголовок"
    }

    private fun loadArguments() {
        // Устанавливаем данные в адаптер
        accountsAdapter.submitList(accountsList)

        // Определяем, какой счет выбрать по умолчанию
        val accountToSelectInitially = initiallySelectedAccountId?.let { id ->
            accountsList.find { it.id == id }
        } ?: accountsList.find { it.default } // Сначала ищем переданный, потом дефолтный

        accountToSelectInitially?.let {
            accountsAdapter.setSelectedAccountId(it.id)
            // Прокрутка к выбранному элементу, если он не виден
            val position = accountsList.indexOf(it)
            if (position != -1) {
                (binding.recyclerViewAccountsBottomSheet.layoutManager as? LinearLayoutManager)?.scrollToPositionWithOffset(position, 0)
            }
        }
    }


    private fun setupRecyclerView() {
        accountsAdapter = AccountSelectionAdapter { selectedAccount ->
            onAccountSelected?.invoke(selectedAccount)
            dismiss()
        }
        binding.recyclerViewAccountsBottomSheet.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = accountsAdapter
            // itemAnimator = null // Если не нужны анимации по умолчанию
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AccountSelectionBottomSheet"

        fun newInstance(
            accounts: List<MoneyAccount>,
            selectedAccountId: Long? = null, // ID счета, который должен быть выбран при открытии
            onAccountSelectedCallback: (MoneyAccount) -> Unit,
            onSettingsClickedCallback: () -> Unit
        ): AccountSelectionBottomSheet {
            return AccountSelectionBottomSheet().apply {
                this.accountsList = accounts
                this.initiallySelectedAccountId = selectedAccountId
                this.onAccountSelected = onAccountSelectedCallback
                this.onSettingsClicked = onSettingsClickedCallback
            }
        }
    }
}