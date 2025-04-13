package com.example.yourfinance.presentation.ui.fragment.manager



import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.yourfinance.R
import com.example.yourfinance.databinding.FragmentCategoryCreateEditBinding
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel

import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryCreateEditFragment : Fragment() {

    private var _binding: FragmentCategoryCreateEditBinding? = null
    private val binding get() = _binding!!

    private val args: CategoryCreateEditFragmentArgs by navArgs()
    private val viewModel: TransactionsViewModel by viewModels()
    private var categoryToEdit: Category? = null

    private var isEditMode = false

    private var currentSelectedTypeInCreateMode: CategoryType = CategoryType.EXPENSE


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupOptionsMenu()
        isEditMode = args.categoryId != -1L

        setupSpinners()
        setupListeners()

        if (isEditMode) {
            setupEditMode()
        } else {
            setupCreateMode()
        }
    }

    private fun setupEditMode() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.edit_category_title)
        binding.tabLayoutCategoryTypeCreateEdit.visibility = View.GONE
        viewLifecycleOwner.lifecycleScope.launch {
            categoryToEdit = viewModel.loadCategoryById(args.categoryId)

            categoryToEdit?.let {
                populateUI(it)
            } ?: let {
                Toast.makeText(requireContext(), "Категория не найдена", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupCreateMode() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_category_title)
        binding.tabLayoutCategoryTypeCreateEdit.visibility = View.VISIBLE
        currentSelectedTypeInCreateMode = args.categoryType
        setupTabs(currentSelectedTypeInCreateMode)
        setupInitialFocusAndKeyboard()
    }

    private fun populateUI(category: Category) {
        binding.titleCategory.setText(category.title)
    }

    private fun setupInitialFocusAndKeyboard() {
        binding.titleCategory.run {
            post {
                requestFocus()
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }


    private fun setupOptionsMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.confirm_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    R.id.action_save -> {
                        val name = binding.titleCategory.text.toString().trim()

                        if (name.isEmpty()) {
                            binding.inputLayoutName.error = "Название не может быть пустым"
                            return true
                        } else {
                            binding.inputLayoutName.error = null
                        }

                        if (isEditMode) {
                            categoryToEdit?.run {
                                title = name
                                viewModel.updateCategory(this)
                            }
                        } else {
                            viewModel.createCategory(Category(name, currentSelectedTypeInCreateMode))
                        }
                        findNavController().popBackStack()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }




    private fun setupSpinners() {
        val colors = listOf("Голубой", "Зеленый", "Красный", "Желтый")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, colors).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerColor.adapter = adapter
        // TODO: Listener для спиннера и обновление card_color_selector
    }


    private fun setupTabs(initialType: CategoryType) {
        binding.tabLayoutCategoryTypeCreateEdit.apply {
            clearOnTabSelectedListeners()
            removeAllTabs()
            addTab(newTab().setText("Расход"))
            addTab(newTab().setText("Доход"))


            val initialIndex = if (initialType == CategoryType.EXPENSE) 0 else 1
            post {
                getTabAt(initialIndex)?.select()
            }


            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    currentSelectedTypeInCreateMode = when (tab?.position) {
                        0 -> CategoryType.EXPENSE
                        1 -> CategoryType.INCOME
                        else -> CategoryType.EXPENSE
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setupListeners() {

        binding.buttonIconSelector.setOnClickListener {
            Toast.makeText(context, "Выбор иконки (не реализовано)", Toast.LENGTH_SHORT).show()
            // TODO: Логика выбора иконки (результат нужно будет где-то сохранить локально или читать перед сохранением)
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}