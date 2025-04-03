package com.example.yourfinance.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.databinding.FragmentWalletBinding
import com.example.yourfinance.presentation.ui.adapter.AccountListItem
import com.example.yourfinance.presentation.ui.adapter.EmptyAdapter
import com.example.yourfinance.presentation.ui.adapter.SectionHeaderAdapter
import com.example.yourfinance.presentation.ui.adapter.WalletAccountsAdapter
import com.example.yourfinance.presentation.ui.adapter.WalletBalanceAdapter
import com.example.yourfinance.presentation.ui.adapter.WalletBudgetAdapter
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel


class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionsViewModel by activityViewModels()
    private val balanceAdapter = WalletBalanceAdapter()
    private val accountsAdapter = WalletAccountsAdapter()
    private val budgetsAdapter = WalletBudgetAdapter()
    private lateinit var concatAdapter: ConcatAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWalletBinding.inflate(inflater, container, false)
        setupConcatAdapter()
        setupRecyclerView()
        setupObservers()
        return binding.root
    }

    private fun setupObservers() {
        viewModel.accountsList.observe(viewLifecycleOwner) {accounts ->
            Log.i("TESTDB", "wallet fragment observer ${accounts.size}")
            balanceAdapter.update(accounts)
            val list: MutableList<AccountListItem> = mutableListOf()
            accounts.forEach({
                list.add(AccountListItem.Account(it))
            })
            if (accounts.size % 2 == 0 ) {
                list.add(AccountListItem.NewAccount)
                list.add(AccountListItem.Empty)
            } else {
                list.add(AccountListItem.NewAccount)
            }

            accountsAdapter.submitList(list)
        }

        viewModel.budgetsList.observe(viewLifecycleOwner) {budgets ->
            Log.i("TESTDB", "wallet fragment observer ${budgets.size}")
            budgetsAdapter.submitList(budgets)
        }
    }

    private fun setupConcatAdapter() {
        val config = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true)
            .build()

        concatAdapter = ConcatAdapter(
            config,
            balanceAdapter,
            SectionHeaderAdapter("Счета"),
            accountsAdapter,
            SectionHeaderAdapter("Бюджеты"),
            budgetsAdapter,
            EmptyAdapter()
        )
    }


    private fun setupRecyclerView() {
        val spanCount = 2
        val layoutManager = GridLayoutManager(requireContext(), spanCount)

        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val adapters = concatAdapter.adapters
                var itemsProcessed = 0
                for (adapter in adapters) {
                    if (position < itemsProcessed + adapter.itemCount) {
                        return when (adapter) {
                            is WalletAccountsAdapter -> 1
                            else -> spanCount
                        }
                    }
                    itemsProcessed += adapter.itemCount
                }
                return spanCount
            }
        }
        binding.walletList.layoutManager = layoutManager
        binding.walletList.adapter = concatAdapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}