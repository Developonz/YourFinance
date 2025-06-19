package com.example.yourfinance.presentation.ui.fragment.manager.budget_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.presentation.databinding.FragmentCategorySelectionBinding
import com.example.yourfinance.presentation.ui.adapter.CategorySelectionAdapter
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategorySelectionBottomSheetFragment : BottomSheetDialogFragment() {

    private var _binding: FragmentCategorySelectionBinding? = null
    private val binding get() = _binding!!

    private val viewModel: BudgetManagerViewModel by viewModels({ requireActivity() })
    private lateinit var adapter: CategorySelectionAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCategorySelectionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialSelectedIds = arguments?.getLongArray(ARG_SELECTED_IDS)?.toSet() ?: emptySet()
        setupRecyclerView(initialSelectedIds)
        observeViewModel()

        binding.buttonSave.setOnClickListener {
            val resultIds = adapter.getSelectedCategoryIds().toLongArray()
            setFragmentResult(REQUEST_KEY, bundleOf(RESULT_IDS to resultIds))
            dismiss()
        }
    }

    private fun setupRecyclerView(initialSelectedIds: Set<Long>) {
        adapter = CategorySelectionAdapter(initialSelectedIds)
        binding.recyclerViewCategories.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewCategories.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.selectableCategories.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "CategorySelectionBottomSheet"
        const val REQUEST_KEY = "category_selection_request"
        const val RESULT_IDS = "result_ids"
        private const val ARG_SELECTED_IDS = "selected_ids"

        fun newInstance(selectedIds: List<Long>): CategorySelectionBottomSheetFragment {
            return CategorySelectionBottomSheetFragment().apply {
                arguments = bundleOf(ARG_SELECTED_IDS to selectedIds.toLongArray())
            }
        }
    }
}