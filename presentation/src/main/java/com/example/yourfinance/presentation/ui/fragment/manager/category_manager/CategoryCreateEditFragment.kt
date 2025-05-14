package com.example.yourfinance.presentation.ui.fragment.manager.category_manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.ColorUtils
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.FragmentCategoryCreateEditBinding
import com.example.yourfinance.presentation.ui.adapter.ColorPickerAdapter
import com.example.yourfinance.presentation.ui.adapter.DisplayableItem
import com.example.yourfinance.presentation.ui.adapter.IconGroup
import com.example.yourfinance.presentation.ui.adapter.IconGroupAdapter
import com.example.yourfinance.presentation.ui.adapter.IconItem
import com.google.android.material.card.MaterialCardView
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class CategoryCreateEditFragment : Fragment() {

    private var _binding: FragmentCategoryCreateEditBinding? = null
    private val binding get() = _binding!!

    private val args: CategoryCreateEditFragmentArgs by navArgs()
    private val viewModel: CategoryManagerViewModel by viewModels()
    private var categoryToEdit: Category? = null

    private var isEditMode = false
    private var currentSelectedTypeInCreateMode: CategoryType = CategoryType.EXPENSE

    private lateinit var iconGroupAdapter: IconGroupAdapter
    private lateinit var colorSelectorAdapter: ColorPickerAdapter

    private var selectedIconResId: Int? = null
    private lateinit var selectedColorHex: String

    private lateinit var availableColors: List<String>

    // Данные для загрузки групп иконок
    private data class IconGroupInfo(
        @StringRes val groupNameResId: Int,
        @ArrayRes val iconArrayResId: Int // ID ресурса <array> из XML
    )

    private val iconGroupDefinitions = listOf(
        IconGroupInfo(R.string.category_group_food, R.array.category_icons_food),
        IconGroupInfo(R.string.category_group_entertainment, R.array.category_icons_entertainment)
        // TODO: Добавить сюда другие группы иконок по аналогии,
    )

    companion object {
        private const val GRID_LAYOUT_COLUMNS_COLORS = 6
        private const val COLOR_PANEL_ANIMATION_DURATION = 150L
        private const val DEFAULT_FALLBACK_COLOR = "#FFEB3B" // Желтый, если ресурсы не загрузятся
        private const val ICON_GRID_COLUMNS = 5 // Для GridLayoutManager и SpanSizeLookup
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadAvailableColors()
        selectedColorHex = availableColors.firstOrNull() ?: DEFAULT_FALLBACK_COLOR

        isEditMode = args.categoryId != -1L

        setupOptionsMenu()
        setupIconRecyclerView()
        setupColorSelectorRecyclerView()

        if (isEditMode) {
            setupEditMode()
        } else {
            setupCreateMode()
        }

        loadAndPrepareIconDisplayableItems()
        setupListeners()
    }

    private fun loadAvailableColors() {
        availableColors = try {
            requireContext().resources.getStringArray(R.array.available_colors_category_account).toList()
        } catch (e: Exception) {
            Log.e("CategoryCreateEdit", "Failed to load colors from resources", e)
            listOf(DEFAULT_FALLBACK_COLOR)
        }
        if (availableColors.isEmpty()) {
            Log.w("CategoryCreateEdit", "Loaded color list is empty, using fallback.")
            availableColors = listOf(DEFAULT_FALLBACK_COLOR)
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

    private fun setupIconRecyclerView() {
        iconGroupAdapter = IconGroupAdapter(
            requireContext(),
            selectedColorHex
        ) { clickedIconItem ->
            val oldSelectedIcon = selectedIconResId
            if (oldSelectedIcon != clickedIconItem.resourceId) {
                selectedIconResId = clickedIconItem.resourceId
                updateIconPreviewImage()
                refreshIconDisplayForAdapter()
            }
        }

        val layoutManager = GridLayoutManager(requireContext(), ICON_GRID_COLUMNS)
        layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                if (position < 0 || position >= iconGroupAdapter.currentList.size) {
                    return ICON_GRID_COLUMNS
                }
                return when (iconGroupAdapter.getItemViewType(position)) {
                    IconGroupAdapter.VIEW_TYPE_HEADER -> ICON_GRID_COLUMNS
                    IconGroupAdapter.VIEW_TYPE_CONTENT -> 1
                    else -> 1
                }
            }
        }

        binding.recyclerViewIconGroups.apply {
            this.layoutManager = layoutManager
            adapter = iconGroupAdapter
            itemAnimator = null
        }
    }

    private fun loadAndPrepareIconDisplayableItems() {
        val iconGroupsData: List<IconGroup> = loadIconGroupsInternal()

        if (!isEditMode && selectedIconResId == null) {
            val firstIconFromLoadedData = iconGroupsData.firstOrNull()?.icons?.firstOrNull()
            firstIconFromLoadedData?.let {
                selectedIconResId = it.resourceId
                if (_binding != null) { // Проверка на случай быстрого уничтожения view
                    updateIconPreviewImage()
                }
            }
        }
        refreshIconDisplayForAdapter(iconGroupsData)
    }

    private fun setupColorSelectorRecyclerView() {
        colorSelectorAdapter = ColorPickerAdapter(availableColors) { newlySelectedColorHex ->
            selectedColorHex = newlySelectedColorHex
            updateColorIndicator()
            updateIconPreviewBackground()
            updateIconPreviewImage()
            refreshIconDisplayForAdapter()
            toggleColorSelectorPanel(show = false)
        }
        binding.recyclerViewColorSelector.apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_LAYOUT_COLUMNS_COLORS)
            adapter = colorSelectorAdapter
        }
    }

    private fun refreshIconDisplayForAdapter(currentIconGroups: List<IconGroup>? = null) {
        if (_binding == null) return

        val groupsToDisplay = currentIconGroups ?: loadIconGroupsInternal()

        val displayableItems = mutableListOf<DisplayableItem>()
        groupsToDisplay.forEach { group ->
            if (group.icons.isNotEmpty()) {
                displayableItems.add(DisplayableItem.HeaderItem(group.groupName))
                group.icons.forEach { icon ->
                    displayableItems.add(
                        DisplayableItem.ContentItem(
                            iconItem = icon,
                            isSelected = (icon.resourceId == selectedIconResId)
                        )
                    )
                }
            }
        }
        iconGroupAdapter.setSelectedColor(selectedColorHex)
        iconGroupAdapter.submitList(displayableItems)
    }

    private fun loadIconGroupsInternal(): List<IconGroup> {
        val iconGroupsResult = mutableListOf<IconGroup>()
        val currentResources = requireContext().resources

        for (groupInfo in iconGroupDefinitions) {
            val groupName = getString(groupInfo.groupNameResId)
            val iconsList = mutableListOf<IconItem>()
            var typedArray: TypedArray? = null

            try {
                typedArray = currentResources.obtainTypedArray(groupInfo.iconArrayResId)
                for (i in 0 until typedArray.length()) {
                    val resId = typedArray.getResourceId(i, 0)
                    if (resId != 0) {
                        val iconName: String = try {
                            currentResources.getResourceEntryName(resId)
                        } catch (e: android.content.res.Resources.NotFoundException) {
                            Log.e("CategoryCreateEdit", "Resource name not found for ID: $resId. Using fallback name.", e)
                            "icon_id_$resId"
                        }
                        iconsList.add(IconItem(resId, iconName))
                    }
                }
            } catch (e: android.content.res.Resources.NotFoundException) {
                Log.e("CategoryCreateEdit", "Icon array resource not found for group: $groupName (ArrayResId: ${groupInfo.iconArrayResId})", e)
            } finally {
                typedArray?.recycle()
            }

            if (iconsList.isNotEmpty()) {
                iconGroupsResult.add(IconGroup(groupName, iconsList))
            }
        }
        return iconGroupsResult
    }

    private fun updateColorIndicator() {
        val colorInt = Color.parseColor(selectedColorHex)
        val bgDrawable = binding.viewColorIndicator.background as? GradientDrawable
        bgDrawable?.setColor(colorInt)
        if (ColorUtils.calculateLuminance(colorInt) > 0.9) {
            bgDrawable?.setStroke(2, Color.LTGRAY)
        } else {
            bgDrawable?.setStroke(0, Color.TRANSPARENT)
        }
    }

    private fun updateIconPreviewBackground() {
        binding.cardIconPreviewWrapper.setCardBackgroundColor(Color.parseColor(selectedColorHex))
        applyStrokeToCardIfNecessary(selectedColorHex, binding.cardIconPreviewWrapper)
    }

    private fun updateIconPreviewImage() {
        selectedIconResId?.let { resId ->
            binding.imageViewSelectedIconPreview.setImageResource(resId)
            val iconTintColor = if (ColorUtils.calculateLuminance(Color.parseColor(selectedColorHex)) > 0.5) Color.BLACK else Color.WHITE
            binding.imageViewSelectedIconPreview.setColorFilter(iconTintColor)
            binding.imageViewSelectedIconPreview.visibility = View.VISIBLE
        } ?: run {
            binding.imageViewSelectedIconPreview.visibility = View.GONE
        }
    }

    private fun applyStrokeToCardIfNecessary(colorHex: String, targetView: MaterialCardView) {
        val colorInt = Color.parseColor(colorHex)
        if (ColorUtils.calculateLuminance(colorInt) > 0.9) { // >0.9 для очень светлых цветов
            targetView.strokeWidth = 3 // dp, если не указано явно в пикселях
            targetView.strokeColor = Color.LTGRAY
        } else {
            targetView.strokeWidth = 0
        }
    }

    private fun setupEditMode() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.edit_category_title)
        binding.tabLayoutCategoryTypeCreateEdit.visibility = View.GONE
        viewLifecycleOwner.lifecycleScope.launch {
            categoryToEdit = viewModel.loadCategoryById(args.categoryId)
            categoryToEdit?.let { category ->
                binding.titleCategory.setText(category.title)
                selectedIconResId = category.iconResourceId
                selectedColorHex = category.colorHex ?: availableColors.firstOrNull() ?: DEFAULT_FALLBACK_COLOR
                currentSelectedTypeInCreateMode = category.categoryType // Сохраняем для логики, хотя табы скрыты

                updateColorIndicator()
                updateIconPreviewBackground()
                updateIconPreviewImage()
                // Данные для адаптера уже будут загружены и обновлены через loadAndPrepareIconDisplayableItems,
                // который вызывается после setupEditMode
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.error_category_not_found), Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
                return@launch // Выход из корутины, если категория не найдена
            }
            if (_binding != null) refreshIconDisplayForAdapter() // Обновляем адаптер после загрузки данных категории
        }
    }

    private fun setupCreateMode() {
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.title = getString(R.string.add_category_title)
        binding.tabLayoutCategoryTypeCreateEdit.visibility = View.VISIBLE
        currentSelectedTypeInCreateMode = args.categoryType
        setupTabs(currentSelectedTypeInCreateMode)

        updateColorIndicator()
        updateIconPreviewBackground()
        // updateIconPreviewImage() будет вызван из loadAndPrepareIconDisplayableItems, если иконка по умолчанию выбрана
        // refreshIconDisplayForAdapter() также будет вызван из loadAndPrepareIconDisplayableItems

        setupInitialFocusAndKeyboard()
    }

    private fun setupInitialFocusAndKeyboard() {
        binding.titleCategory.run {
            post { // post для гарантии, что view уже добавлено и имеет размеры
                requestFocus()
                val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT)
            }
        }
    }

    private fun setupTabs(initialType: CategoryType) {
        binding.tabLayoutCategoryTypeCreateEdit.apply {
            clearOnTabSelectedListeners()
            removeAllTabs()
            addTab(newTab().setText(getString(R.string.category_type_expense)))
            addTab(newTab().setText(getString(R.string.category_type_income)))

            val initialIndex = if (initialType == CategoryType.EXPENSE) 0 else 1
            post { getTabAt(initialIndex)?.select() } // post для выбора после добавления вкладок

            addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    currentSelectedTypeInCreateMode = when (tab?.position) {
                        0 -> CategoryType.EXPENSE
                        1 -> CategoryType.INCOME
                        else -> CategoryType.EXPENSE // Фоллбэк
                    }
                }
                override fun onTabUnselected(tab: TabLayout.Tab?) {}
                override fun onTabReselected(tab: TabLayout.Tab?) {}
            })
        }
    }

    private fun setupListeners() {
        binding.colorSelectorContainer.setOnClickListener {
            toggleColorSelectorPanel()
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun toggleColorSelectorPanel(show: Boolean? = null) {
        val panel = binding.cardColorSelectorPanel
        val arrow = binding.imageViewColorArrow
        val shouldShow = show ?: (panel.visibility == View.GONE)

        TransitionManager.beginDelayedTransition(binding.contentLayout, AutoTransition().apply { duration = COLOR_PANEL_ANIMATION_DURATION })

        if (shouldShow) {
            panel.visibility = View.VISIBLE
            arrow.animate().rotation(180f).setDuration(COLOR_PANEL_ANIMATION_DURATION).start()
            // Прокрутка к панели выбора цвета, если она открывается
            binding.nestedScrollView.post {
                binding.nestedScrollView.smoothScrollTo(0, panel.bottom)
            }
        } else {
            panel.visibility = View.GONE
            arrow.animate().rotation(0f).setDuration(COLOR_PANEL_ANIMATION_DURATION).start()
        }
    }

    private fun saveCategory() {
        val name = binding.titleCategory.text.toString().trim()

        if (name.isEmpty()) {
            binding.inputLayoutName.error = getString(R.string.error_category_name_empty)
            return
        } else {
            binding.inputLayoutName.error = null
        }

        if (selectedIconResId == null) { // Проверяем для обоих режимов, но для edit иконка уже должна быть
            Toast.makeText(context, getString(R.string.error_select_icon_for_category), Toast.LENGTH_SHORT).show()
            return
        }

        hideKeyboard()

        val categoryToSave: Category
        if (isEditMode) {
            categoryToEdit?.let {
                it.title = name // Используем Title(name) если это value class, иначе просто name
                it.iconResourceId = selectedIconResId
                it.colorHex = selectedColorHex
                // categoryType не меняем в режиме редактирования через этот UI
                categoryToSave = it
                viewModel.updateCategory(categoryToSave)
            } ?: run {
                Toast.makeText(requireContext(), getString(R.string.error_failed_to_update_category), Toast.LENGTH_SHORT).show()
                return
            }
        } else {
            categoryToSave = Category(
                title = Title(name),
                categoryType = currentSelectedTypeInCreateMode,
                iconResourceId = selectedIconResId,
                colorHex = selectedColorHex
            )
            viewModel.createCategory(categoryToSave)
        }
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
}