// AccountSelectionBottomSheet.kt
package com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.databinding.BottomSheetAccountSelectionBinding
import com.example.yourfinance.presentation.ui.adapter.AccountSelectionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AccountSelectionBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetAccountSelectionBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: AccountSelectionAdapter
    private var list: List<MoneyAccount> = emptyList()
    private var initId: Long? = null
    private var onSelect: ((MoneyAccount) -> Unit)? = null
    private var onSettings: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, c: ViewGroup?, s: Bundle?) =
        BottomSheetAccountSelectionBinding.inflate(inflater, c, false).also { _binding = it }.root

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        adapter = AccountSelectionAdapter { acc ->
            onSelect?.invoke(acc)
            dismiss()
        }
        binding.recyclerViewAccountsBottomSheet.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@AccountSelectionBottomSheet.adapter
        }
        adapter.submitList(list)
        initId?.let { selId ->
            val idx = list.indexOfFirst { it.id == selId }
            if (idx >= 0) {
                adapter.setSelectedAccountId(selId)
                (binding.recyclerViewAccountsBottomSheet.layoutManager as LinearLayoutManager)
                    .scrollToPositionWithOffset(idx, 0)
            }
        }
        binding.buttonAccountSettings.setOnClickListener {
            onSettings?.invoke()
            dismiss()
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
            selectedAccountId: Long?,
            onAccountSelectedCallback: (MoneyAccount) -> Unit,
            onSettingsClickedCallback: () -> Unit
        ) = AccountSelectionBottomSheet().apply {
            list = accounts
            initId = selectedAccountId
            onSelect = onAccountSelectedCallback
            onSettings = onSettingsClickedCallback
        }
    }
}
