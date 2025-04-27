package com.example.yourfinance.presentation.ui.fragment.manager.category_manager


import android.annotation.SuppressLint // Для OnTouchListener
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.GestureDetector
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentCategoryCreateEditBinding
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.category.Category
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class CategoryCreateEditFragment : Fragment() {

    private var _binding: FragmentCategoryCreateEditBinding? = null
    private val binding get() = _binding!!

    private val args: CategoryCreateEditFragmentArgs by navArgs()
    private val viewModel: CategoryManagerViewModel by viewModels()
    private var categoryToEdit: Category? = null

    private var isEditMode = false
    private var currentSelectedTypeInCreateMode: CategoryType = CategoryType.EXPENSE

    private lateinit var gestureDetector: GestureDetector

    // Константы для определения свайпа
    companion object {
        private const val SWIPE_MIN_DISTANCE = 120
        private const val SWIPE_THRESHOLD_VELOCITY = 200
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryCreateEditBinding.inflate(inflater, container, false)
        gestureDetector = GestureDetector(requireContext(), SwipeGestureListener())
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility") // Добавляем аннотацию для setOnTouchListener
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

            binding.contentLayout.setOnTouchListener { v, event ->
                v.performClick()
                gestureDetector.onTouchEvent(event)
                true
            }
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
                        saveCategory()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    private fun setupSpinners() {
        val colors = listOf("Голубой", "Зеленый", "Красный", "Желтый") // Пример
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, colors).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinnerColor.adapter = adapter

    }


    private fun setupTabs(initialType: CategoryType) {
        binding.tabLayoutCategoryTypeCreateEdit.apply {
            clearOnTabSelectedListeners()
            removeAllTabs()
            addTab(newTab().setText("Расход"))
            addTab(newTab().setText("Доход"))

            // Устанавливаем начальную вкладку
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
            // TODO: Логика выбора иконки
        }
    }

    private fun saveCategory() {
        val name = binding.titleCategory.text.toString().trim()

        if (name.isEmpty()) {
            binding.inputLayoutName.error = "Название не может быть пустым"
            return // Выходим, если имя пустое
        } else {
            binding.inputLayoutName.error = null
        }

        // TODO: Получить выбранный цвет и иконку, когда они будут реализованы

        if (isEditMode) {
            categoryToEdit?.let {
                it.title = name
                // it.color = selectedColor
                // it.icon = selectedIcon
                viewModel.updateCategory(it)
            } ?: run {
                Toast.makeText(requireContext(), "Ошибка: Не удалось обновить категорию", Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            val newCategory = Category(
                title = Title(name),
                categoryType = currentSelectedTypeInCreateMode
                // color = selectedColor,
                // icon = selectedIcon
            )
            viewModel.createCategory(newCategory)
        }

        hideKeyboard()
        findNavController().popBackStack()
    }

    private fun hideKeyboard() {
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        view?.let { imm?.hideSoftInputFromWindow(it.windowToken, 0) }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class SwipeGestureListener : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent): Boolean {
            return true
        }

        override fun onFling(
            e1: MotionEvent?,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            if (isEditMode || e1 == null) {
                return false
            }

            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y

            // Убеждаемся, что это горизонтальный свайп, а не вертикальный или диагональный
            if (abs(diffX) > abs(diffY)) {
                // Убеждаемся, что свайп достаточно длинный и быстрый
                if (abs(diffX) > SWIPE_MIN_DISTANCE && abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    if (diffX > 0) {
                        switchToPreviousTab()
                    } else {
                        switchToNextTab()
                    }
                    return true
                }
            }
            return false
        }
    }

    private fun switchToNextTab() {
        val currentTab = binding.tabLayoutCategoryTypeCreateEdit.selectedTabPosition
        val nextTab = currentTab + 1
        if (nextTab < binding.tabLayoutCategoryTypeCreateEdit.tabCount) {
            binding.tabLayoutCategoryTypeCreateEdit.getTabAt(nextTab)?.select()
        }
    }

    private fun switchToPreviousTab() {
        val currentTab = binding.tabLayoutCategoryTypeCreateEdit.selectedTabPosition
        val previousTab = currentTab - 1
        if (previousTab >= 0) {
            binding.tabLayoutCategoryTypeCreateEdit.getTabAt(previousTab)?.select()
        }
    }
}