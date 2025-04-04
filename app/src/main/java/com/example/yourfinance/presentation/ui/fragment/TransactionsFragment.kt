package com.example.yourfinance.presentation.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.databinding.FragmentTransactionsBinding
import com.example.yourfinance.presentation.ui.adapter.list_item.TransactionListItem
import com.example.yourfinance.presentation.ui.adapter.TransactionsRecyclerViewListAdapter
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.utils.StringHelper.Companion.getMoneyStr
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel


class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: TransactionsViewModel by activityViewModels()
    private val adapter = TransactionsRecyclerViewListAdapter()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupObservers()
        return binding.root
    }

    private fun setupRecyclerView() {
        binding.transactionsList.layoutManager = LinearLayoutManager(requireContext())
        binding.transactionsList.adapter = adapter
        binding.transactionsList.setHasFixedSize(true)
        binding.transactionsList.itemAnimator = null
    }

    private fun setupObservers() {
        viewModel.transactionsList.observe(viewLifecycleOwner) { list ->
            Log.i("TESTDB", "transaction fragment observer")
            // Группируем транзакции по дате и сортируем группы по убыванию даты
            val groupedTransactions = list
                // Группируем транзакции по дате
                .groupBy { it.date }
                // Для каждой группы сортируем транзакции по времени (в порядке убывания)
                .mapValues { (_, transactions) ->
                    transactions.sortedByDescending { it.time }
                }
                // Сортируем группы по дате в порядке убывания
                .toSortedMap(compareByDescending { it })

            val items = mutableListOf<TransactionListItem>()

            groupedTransactions.forEach { (date, transactions) ->
                val balance = transactions.filterIsInstance<Payment>()
                    .sumOf { if (it.type == TransactionType.INCOME) it.balance else -it.balance }

                items.add(TransactionListItem.Header(date, balance))
                transactions.forEach { transaction ->
                    items.add(TransactionListItem.TransactionItem(transaction))
                }
            }

            var income = 0.0
            var expense = 0.0

            list.filterIsInstance<Payment>().forEach({
                if (it.type == TransactionType.INCOME) {
                    income += it.balance
                } else {
                    expense += it.balance
                }
            })

            binding.incomeBalance.text = getMoneyStr(income)
            binding.expenseBalance.text = getMoneyStr(expense)

            items.add(TransactionListItem.EmptyItem)
            adapter.submitList(items)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}