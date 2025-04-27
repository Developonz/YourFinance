package com.example.yourfinance.presentation.ui.fragment.manager.category_manager

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.adapter.FragmentStateAdapter // Импорт для адаптера ViewPager2
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.presentation.databinding.FragmentCategoryManagerBinding
import com.google.android.material.tabs.TabLayout // Импорт для TabLayout
import com.google.android.material.tabs.TabLayoutMediator // Импорт для связывания TabLayout и ViewPager2
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CategoryManagerFragment : Fragment() {

    private var _binding: FragmentCategoryManagerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CategoryManagerViewModel by viewModels()

    private lateinit var categoriesPagerAdapter: CategoriesPagerAdapter

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
        setupViewPagerAndTabs()
        setupAddButton()
    }

    private fun setupViewPagerAndTabs() {
        categoriesPagerAdapter = CategoriesPagerAdapter(this)
        binding.viewPagerCategories.adapter = categoriesPagerAdapter

        // Связываем TabLayout и ViewPager2
        TabLayoutMediator(binding.tabLayoutCategoryType, binding.viewPagerCategories) { tab, position ->
            tab.text = when (position) {
                0 -> "Расход"
                1 -> "Доход"
                else -> null
            }
        }.attach()

        binding.tabLayoutCategoryType.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedType = when (tab?.position) {
                    0 -> CategoryType.EXPENSE
                    1 -> CategoryType.INCOME
                    else -> CategoryType.EXPENSE
                }
                viewModel.setSelectedCategoryType(selectedType)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupAddButton() {
        binding.buttonAddCategory.setOnClickListener {
            val currentType = viewModel.selectedCategoryType.value ?: CategoryType.EXPENSE
            val action = CategoryManagerFragmentDirections.actionCategoriesFragmentToCreateEditCategoriesFragment(categoryType = currentType)
            findNavController().navigate(action)
        }
    }

    // Адаптер для ViewPager2
    private inner class CategoriesPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> CategoryListContentFragment.newInstance(CategoryType.EXPENSE)
                1 -> CategoryListContentFragment.newInstance(CategoryType.INCOME)
                else -> throw IllegalStateException("Invalid position: $position")
            }
        }
    }

    override fun onDestroyView() {
        binding.viewPagerCategories.adapter = null
        super.onDestroyView()
        _binding = null
    }
}