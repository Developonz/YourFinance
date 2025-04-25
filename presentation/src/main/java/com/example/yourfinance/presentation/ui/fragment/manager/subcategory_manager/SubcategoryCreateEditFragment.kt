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
import com.example.yourfinance.R
import com.example.yourfinance.databinding.FragmentSubcategoryCreateEditBinding
import com.example.yourfinance.domain.model.entity.category.Subcategory
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SubcategoryCreateEditFragment : Fragment() {
    private var _binding: FragmentSubcategoryCreateEditBinding? = null
    private val binding get() = _binding!!

    private val args: SubcategoryCreateEditFragmentArgs by navArgs()
    private val viewModel: SubcategoryManagerViewModel by viewModels()
    private var subcategoryToEdit: com.example.yourfinance.domain.model.entity.category.Subcategory? = null

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
                        val name = binding.titleSubcategory.text.toString().trim()

                        if (name.isEmpty()) {
                            binding.inputLayoutName.error = "Название не может быть пустым"
                            return true
                        } else {
                            binding.inputLayoutName.error = null
                        }
                        Log.i("TESTD", " create subcategory1 ")
                        if (isEditMode) {
                            subcategoryToEdit?.run {
                                title = name
                                viewModel.updateSubcategory(this)
                            }
                        } else {
                            Log.i("TESTD", " create subcategory2 " + args.parentId)
                            viewModel.createSubcategory(
                                com.example.yourfinance.domain.model.entity.category.Subcategory(
                                    title = name,
                                    categoryType = args.categoryType,
                                    parentId = args.parentId
                                )
                            )
                        }
                        findNavController().popBackStack()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}