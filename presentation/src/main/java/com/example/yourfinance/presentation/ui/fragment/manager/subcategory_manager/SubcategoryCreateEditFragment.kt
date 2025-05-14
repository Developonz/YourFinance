package com.example.yourfinance.presentation.ui.fragment.manager.subcategory_manager

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentSubcategoryCreateEditBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubcategoryCreateEditFragment : Fragment() {
    private var _binding: FragmentSubcategoryCreateEditBinding? = null
    private val binding get() = _binding!!

    private val args: SubcategoryCreateEditFragmentArgs by navArgs()
    private val viewModel: SubcategoryManagerViewModel by viewModels()
    private var subcategoryToEdit: Subcategory? = null

    private var isEditMode = false



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i("TESTS", "sub fragment on view ")
        _binding = FragmentSubcategoryCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.i("TESTS", "sub fragment on view created ")
        setupOptionsMenu()

        isEditMode = args.subcategoryId != -1L


        if (isEditMode) {
            setupEditMode()
        } else {
            setupCreateMode()
        }
    }

    private fun setupEditMode() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.edit_subcategory_title)
        viewLifecycleOwner.lifecycleScope.launch {
            subcategoryToEdit = viewModel.loadSubcategoryById(args.subcategoryId)

            subcategoryToEdit?.let {
                populateUI(it)
            } ?: let {
                Toast.makeText(requireContext(), "Подкатегория не найдена", Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    private fun setupCreateMode() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_subcategory_title)
        setupInitialFocusAndKeyboard()
    }

    private fun populateUI(subcategory: com.example.yourfinance.domain.model.entity.category.Subcategory) {
        binding.titleSubcategory.setText(subcategory.title)
    }

    private fun setupInitialFocusAndKeyboard() {
        binding.titleSubcategory.run {
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
                        saveSubcategory() // Вынесем логику сохранения в отдельный метод
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun saveSubcategory() {
        val name = binding.titleSubcategory.text.toString().trim()

        if (name.isEmpty()) {
            binding.inputLayoutName.error = "Название не может быть пустым"
            return // Выходим, если имя пустое
        } else {
            binding.inputLayoutName.error = null
        }

        hideKeyboard()

        viewLifecycleOwner.lifecycleScope.launch {
            if (isEditMode) {
                subcategoryToEdit?.let {
                    it.title = name // Обновляем только название, цвет и parentId не меняются
                    viewModel.updateSubcategory(it)
                    findNavController().popBackStack()
                } ?: run {
                    Toast.makeText(requireContext(), "Ошибка: Не удалось обновить подкатегорию", Toast.LENGTH_SHORT).show()
                }
            } else {
                // Для создания новой подкатегории нам нужен цвет родителя
                // Предполагаем, что в SubcategoryManagerViewModel есть метод getParentCategoryColor
                val parentColorHex = viewModel.getParentCategoryColor(args.parentId) // Эта функция должна быть в ViewModel


                val newSubcategory = Subcategory(
                    Title(name),
                    args.categoryType,
                    args.parentId,
                    parentColorHex
                )
                viewModel.createSubcategory(newSubcategory)
                findNavController().popBackStack()
            }
        }
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        view?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}