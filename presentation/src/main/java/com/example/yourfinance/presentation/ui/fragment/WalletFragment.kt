package com.example.yourfinance.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.GridLayoutManager
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentWalletBinding
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.presentation.ui.adapter.list_item.AccountListItem
import com.example.yourfinance.presentation.ui.adapter.list_item.BudgetListItem
import com.example.yourfinance.presentation.ui.adapter.wallet_page.EmptyAdapter
import com.example.yourfinance.presentation.ui.adapter.wallet_page.SectionHeaderAdapter
import com.example.yourfinance.presentation.ui.adapter.wallet_page.WalletAccountsAdapter
import com.example.yourfinance.presentation.ui.adapter.wallet_page.WalletBalanceAdapter
import com.example.yourfinance.presentation.ui.adapter.wallet_page.WalletBudgetAdapter


class WalletFragment : Fragment() {

    private var _binding: FragmentWalletBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GeneralViewModel by activityViewModels()
    private val balanceAdapter = WalletBalanceAdapter()
    private val budgetsAdapter = WalletBudgetAdapter()
    private lateinit var concatAdapter: ConcatAdapter
    private val newAccountClick = {
        val action = WalletFragmentDirections.actionWalletToAccountCreateManager()
        findNavController().navigate(action)
    }
    private val editClick = { acc: MoneyAccount ->
        val action = WalletFragmentDirections.actionWalletToAccountCreateManager(acc.id)
        findNavController().navigate(action)
    }
    private val accountsAdapter = WalletAccountsAdapter(newAccountClick, editClick)

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOptionsMenu()
    }

    private fun setupOptionsMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.wallet_fragment_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        findNavController().navigate(R.id.action_wallet_to_settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupObservers() {
        viewModel.accountsList.observe(viewLifecycleOwner) {accounts ->
            Log.i("TESTDB", "wallet fragment observer ${accounts.size}")
            balanceAdapter.update(accounts)
            val list: MutableList<AccountListItem> = mutableListOf()
            accounts.forEach {
                list.add(AccountListItem.Account(it))
            }
            list.add(AccountListItem.NewAccount)
            if (accounts.size % 2 == 0 ) list.add(AccountListItem.Empty)
            accountsAdapter.submitList(list)
        }

        viewModel.budgetsList.observe(viewLifecycleOwner) {budgets ->
            Log.i("TESTDB", "wallet fragment observer ${budgets.size}")
            val list: MutableList<BudgetListItem> = mutableListOf()
            if (budgets.isNotEmpty()) {
                budgets.forEach {
                    list.add(BudgetListItem.BudgetItem(it))
                }
                list.add(BudgetListItem.CreateBudget)
            } else {
                list.add(BudgetListItem.EmptyList)
            }
            budgetsAdapter.submitList(list)
        }
    }

    private fun setupConcatAdapter() {
        val config = ConcatAdapter.Config.Builder()
            .setIsolateViewTypes(true)
            .build()

        concatAdapter = ConcatAdapter(
            config,
            balanceAdapter,
            SectionHeaderAdapter("СЧЕТА", onHeaderClick),
            accountsAdapter,
            SectionHeaderAdapter("БЮДЖЕТЫ", onHeaderClick),
            budgetsAdapter,
            EmptyAdapter()
        )
    }

    val onHeaderClick = { sectionId: String ->
        when (sectionId) { // Используйте переданный идентификатор
            "СЧЕТА" -> findNavController().navigate(R.id.action_wallet_to_accountManager)
            "БЮДЖЕТЫ" -> findNavController().navigate(R.id.action_wallet_to_budgetManager)
        }
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