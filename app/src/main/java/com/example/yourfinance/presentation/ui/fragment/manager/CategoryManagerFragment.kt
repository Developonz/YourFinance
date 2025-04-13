package com.example.yourfinance.presentation.ui.fragment.manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.databinding.FragmentCategoryManagerBinding
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.presentation.ui.adapter.CategoryAdapter
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class CategoryManagerFragment : Fragment() {

    private var _binding: FragmentCategoryManagerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TransactionsViewModel by activityViewModels()

    private lateinit var categoryAdapter: CategoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryManagerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabs()
        observeViewModel()


        binding.buttonAddCategory.setOnClickListener {

            val currentType = viewModel.selectedCategoryType.value ?: CategoryType.EXPENSE
            val action = CategoryManagerFragmentDirections.actionCategoriesFragmentToCreateEditCategoriesFragment(categoryType = currentType)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {
        categoryAdapter = CategoryAdapter(
            deleteClick = { categoryToDelete ->
                viewModel.deleteCategory(categoryToDelete)
            },
            editClick = { categoryToEdit ->
                val action = CategoryManagerFragmentDirections.actionCategoriesFragmentToCreateEditCategoriesFragment(categoryId = categoryToEdit.category.id)
                findNavController().navigate(action)
            },
            editSubcategories = {parentCategory ->
                val action = CategoryManagerFragmentDirections.actionCategoriesFragmentToSubcategoriesFragment(categoryId = parentCategory.category.id, categoryType = parentCategory.category.categoryType)
                findNavController().navigate(action)
            }
        )

        binding.recyclerViewCategories.apply {
            adapter = categoryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupTabs() {
        binding.tabLayoutCategoryType.apply {
            clearOnTabSelectedListeners()
            removeAllTabs()

            addTab(newTab().setText("Расход"))
            addTab(newTab().setText("Доход"))


            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    val selectedType = when (tab?.position) {
                        0 -> CategoryType.EXPENSE
                        1 -> CategoryType.INCOME
                        else -> CategoryType.EXPENSE
                    }
                    viewModel.setSelectedCategoryType(selectedType)
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {  }
                override fun onTabReselected(tab: TabLayout.Tab?) {  }
            })
        }
    }

    private fun observeViewModel() {
        viewModel.filteredCategories.observe(viewLifecycleOwner) { categories ->
            categoryAdapter.submitList(categories)
        }

        viewModel.selectedCategoryType.observe(viewLifecycleOwner) { type ->
            val tabIndex = if (type == CategoryType.EXPENSE) 0 else 1

            if (binding.tabLayoutCategoryType.selectedTabPosition != tabIndex) {
                binding.tabLayoutCategoryType.post {
                    binding.tabLayoutCategoryType.getTabAt(tabIndex)?.select()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.recyclerViewCategories.adapter = null
        _binding = null
    }
}