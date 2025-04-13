package com.example.yourfinance.presentation.ui.fragment.manager

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.yourfinance.databinding.FragmentSubcategoryManagerBinding
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.presentation.ui.adapter.CategoryAdapter
import com.example.yourfinance.presentation.ui.adapter.SubcategoryAdapter
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubcategoryManagerFragment : Fragment() {

    private var _binding: FragmentSubcategoryManagerBinding? = null
    private val viewModel: TransactionsViewModel by activityViewModels()
    private val binding get() = _binding!!
    private val args: SubcategoryManagerFragmentArgs by navArgs()
    private var adapter = SubcategoryAdapter(
        deleteClick = {subcategory ->
            viewModel.deleteSabcategory(subcategory)
        },
        editClick = {subcategory ->
            Log.i("TESTS", "navigate " + subcategory.id)
            val action = SubcategoryManagerFragmentDirections.actionSubcategoriesFragmentToSubcategoriesCreateEditFragment(subcategoryId = subcategory.id)
            findNavController().navigate(action)
        }
    )


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSubcategoryManagerBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observeViewModel()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.buttonAddSubcategory.setOnClickListener {
            val action = SubcategoryManagerFragmentDirections.actionSubcategoriesFragmentToSubcategoriesCreateEditFragment(parentId = args.categoryId, categoryType = args.categoryType)
            findNavController().navigate(action)
        }
    }

    private fun setupRecyclerView() {

        binding.recyclerViewSubcategories.let {
            it.adapter = adapter
            it.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun observeViewModel() {
        viewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            categories.filter { it.category.id == args.categoryId }.firstOrNull()?.let {
                adapter.submitList(it.subcategories)
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}