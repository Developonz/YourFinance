package com.example.yourfinance.presentation.ui.fragment.manager.budget_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.presentation.databinding.FragmentBudgetManagerBinding
import com.example.yourfinance.presentation.ui.adapter.BudgetManagerAdapter
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class BudgetManagerFragment : Fragment() {

    private var _binding: FragmentBudgetManagerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetManagerViewModel by viewModels()
    private lateinit var budgetAdapter: BudgetManagerAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetManagerAdapter(
            onEditClick = { budget ->
                val action = BudgetManagerFragmentDirections.actionBudgetManagerFragmentToBudgetCreateEditFragment(budgetId = budget.id)
                findNavController().navigate(action)
            },
            onDeleteClick = { budget ->
                showDeleteConfirmationDialog(budget.id)
            }
        )
        binding.recyclerViewBudgets.apply {
            adapter = budgetAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupListeners() {
        binding.buttonAddBudget.setOnClickListener {
            val action = BudgetManagerFragmentDirections.actionBudgetManagerFragmentToBudgetCreateEditFragment()
            findNavController().navigate(action)
        }
    }

    private fun observeViewModel() {
        // Подписываемся на сгруппированную LiveData
        viewModel.groupedBudgets.observe(viewLifecycleOwner) { items ->
            budgetAdapter.submitList(items)
            binding.emptyView.visibility = if (items.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun showDeleteConfirmationDialog(budgetId: Long) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Удаление бюджета")
            .setMessage("Вы уверены, что хотите удалить этот бюджет? Это действие необратимо.")
            .setNegativeButton("Отмена", null)
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteBudget(budgetId)
            }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}