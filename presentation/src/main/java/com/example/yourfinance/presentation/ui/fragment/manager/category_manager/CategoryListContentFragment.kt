package com.example.yourfinance.presentation.ui.fragment.manager.category_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.presentation.databinding.FragmentCategoryListContentBinding
import com.example.yourfinance.presentation.ui.adapter.CategoryAdapter
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryListContentFragment : Fragment() {

    companion object {
        private const val ARG_CATEGORY_TYPE = "category_type"

        fun newInstance(categoryType: CategoryType): CategoryListContentFragment {
            return CategoryListContentFragment().apply {
                arguments = bundleOf(ARG_CATEGORY_TYPE to categoryType)
            }
        }
    }

    private var _binding: FragmentCategoryListContentBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryManagerViewModel by viewModels({requireParentFragment()})

    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var categoryType: CategoryType

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            categoryType = it.getSerializable(ARG_CATEGORY_TYPE) as? CategoryType
                ?: throw IllegalArgumentException("CategoryType must be provided")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryListContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            deleteClick = { categoryToDelete ->
                viewModel.deleteCategory(categoryToDelete.id)
            },
            editClick = { categoryToEdit ->
                val action = CategoryManagerFragmentDirections.actionCategoriesFragmentToCreateEditCategoriesFragment(categoryId = categoryToEdit.id)
                requireParentFragment().findNavController().navigate(action)

            },
            editSubcategories = { parentCategory ->
                val action = CategoryManagerFragmentDirections.actionCategoriesFragmentToSubcategoriesFragment(categoryId = parentCategory.id, categoryType = parentCategory.categoryType)
                requireParentFragment().findNavController().navigate(action)
            }
        )

        binding.recyclerViewCategoryList.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {

        val liveDataToObserve = if (categoryType == CategoryType.EXPENSE) {
            viewModel.expenseCategories
        } else {
            viewModel.incomeCategories
        }

        liveDataToObserve.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewCategoryList.adapter = null
        _binding = null
    }


}