package com.example.yourfinance.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.databinding.FragmentTransactionsBinding
import com.example.yourfinance.model.Transaction
import com.example.yourfinance.model.TransactionListItem
import com.example.yourfinance.model.adapters.TransactionsRecyclerViewListAdapter
import com.example.yourfinance.model.entities.Payment
import com.example.yourfinance.utils.StringHelper.Companion.getMoneyStr
import com.example.yourfinance.viewmodel.TransactionsViewModel


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
//        binding.transactionsList.itemAnimator = null
    }

    private fun setupObservers() {
        viewModel.transactionsList.observe(viewLifecycleOwner) { list ->
            // Группируем транзакции по дате и сортируем группы по убыванию даты
            val groupedTransactions = list.groupBy { it.date }
                .toSortedMap(compareByDescending { it })

            val items = mutableListOf<TransactionListItem>()

            var income = 0.0
            var expense = 0.0

            groupedTransactions.forEach { (date, transactions) ->
                // Вычисляем баланс для группы транзакций этого дня:
                val balance = transactions.filterIsInstance<Payment>()
                    .sumOf { if (it.type == Transaction.TransactionType.income) it.balance else -it.balance }



                // Добавляем заголовок с датой и балансом
                items.add(TransactionListItem.Header(date, balance))

                // Добавляем сами транзакции
                transactions.forEach { transaction ->
                    items.add(TransactionListItem.TransactionItem(transaction))
                }
            }

            list.filterIsInstance<Payment>().forEach({
                if (it.type == Transaction.TransactionType.income) {
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