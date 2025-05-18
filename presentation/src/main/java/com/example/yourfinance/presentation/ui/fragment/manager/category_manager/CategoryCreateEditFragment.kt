package com.example.yourfinance.presentation.ui.fragment.manager.category_manager

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import androidx.annotation.ColorInt
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
import com.example.yourfinance.presentation.IconMap
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

    // Имя drawable (ключ для IconMap)
    private var selectedIconKey: String? = null

    // ARGB-цвет
    @ColorInt private var selectedColor: Int = DEFAULT_FALLBACK_COLOR

    private lateinit var availableColors: List<Int>
    private lateinit var iconGroupAdapter: IconGroupAdapter
    private lateinit var colorSelectorAdapter: ColorPickerAdapter

    private data class IconGroupInfo(
        @StringRes val titleRes: Int,
        @ArrayRes  val namesRes: Int
    )

    private val iconGroupDefinitions = listOf(
        IconGroupInfo(R.string.category_group_food,          R.array.category_icons_food),
        IconGroupInfo(R.string.category_group_entertainment, R.array.category_icons_entertainment)
        // … другие группы …
    )

    companion object {
        private const val GRID_LAYOUT_COLUMNS_COLORS     = 6
        private const val ICON_GRID_COLUMNS              = 5
        private const val COLOR_PANEL_ANIMATION_DURATION = 150L
        private val DEFAULT_FALLBACK_COLOR: Int          = Color.parseColor("#FFEB3B")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCategoryCreateEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // 1) Загрузить цвета из string-array
        loadAvailableColors()
        selectedColor = availableColors.firstOrNull() ?: DEFAULT_FALLBACK_COLOR

        // 2) Определить режим (Create/Edit)
        isEditMode = args.categoryId != -1L

        // 3) Menu + RecyclerViews
        setupOptionsMenu()
        setupIconRecyclerView()
        setupColorSelectorRecyclerView()

        // 4) Инициализация в зависимости от режима
        if (isEditMode) setupEditMode() else setupCreateMode()

        // 5) Подготовка и показ групп иконок
        loadAndDisplayIconGroups()

        // 6) Листенеры
        setupListeners()
    }

    private fun loadAvailableColors() {
        availableColors = try {
            // читаем string-array, а не int-array
            requireContext().resources.getStringArray(R.array.available_colors_category_account)
                .mapNotNull { hex ->
                    try { Color.parseColor(hex) }
                    catch (_: IllegalArgumentException) { null }
                }
        } catch (e: Resources.NotFoundException) {
            Log.e("CategoryEdit", "colors-array not found", e)
            emptyList()
        }.ifEmpty {
            listOf(DEFAULT_FALLBACK_COLOR)
        }
    }

    private fun setupOptionsMenu() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, inflater: MenuInflater) {
                inflater.inflate(R.menu.confirm_menu, menu)
            }
            override fun onMenuItemSelected(item: MenuItem): Boolean =
                if (item.itemId == R.id.action_save) { saveCategory(); true } else false
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupIconRecyclerView() {
        iconGroupAdapter = IconGroupAdapter(requireContext(), selectedColor) { clicked ->
            if (selectedIconKey != clicked.resourceId) {
                selectedIconKey = clicked.resourceId
                updateIconPreview()
                refreshIconGroups()
            }
        }
        val lm = GridLayoutManager(requireContext(), ICON_GRID_COLUMNS).apply {
            spanSizeLookup = object: GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int) = when (iconGroupAdapter.currentList[position]) {
                    is DisplayableItem.HeaderItem  -> ICON_GRID_COLUMNS
                    is DisplayableItem.ContentItem -> 1
                }
            }
        }
        binding.recyclerViewIconGroups.apply {
            layoutManager = lm
            adapter = iconGroupAdapter
            itemAnimator = null
        }
    }

    private fun setupColorSelectorRecyclerView() {
        colorSelectorAdapter = ColorPickerAdapter(availableColors) { color ->
            selectedColor = color
            updateColorIndicator()
            updateIconPreviewBackground()
            updateIconPreview()
            refreshIconGroups()
            toggleColorPanel(false)
        }
        binding.recyclerViewColorSelector.apply {
            layoutManager = GridLayoutManager(requireContext(), GRID_LAYOUT_COLUMNS_COLORS)
            adapter = colorSelectorAdapter
        }
    }

    private fun loadAndDisplayIconGroups() {
        val groups = buildIconGroups()
        if (!isEditMode && selectedIconKey == null) {
            selectedIconKey = groups.firstOrNull()?.icons?.firstOrNull()?.resourceId
        }
        updateIconPreview()
        refreshIconGroups(groups)
    }

    private fun buildIconGroups(): List<IconGroup> {
        val res = requireContext().resources
        return iconGroupDefinitions.mapNotNull { info ->
            val title = getString(info.titleRes)
            val keys = try { res.getStringArray(info.namesRes) } catch (_: Exception) { emptyArray() }
            val icons = keys.filter { it.isNotBlank() }.map { k -> IconItem(resourceId = k, name = k) }
            IconGroup(title, icons).takeIf { it.icons.isNotEmpty() }
        }
    }

    private fun refreshIconGroups(groups: List<IconGroup> = buildIconGroups()) {
        val items = mutableListOf<DisplayableItem>()
        groups.forEach { g ->
            items += DisplayableItem.HeaderItem(g.groupName)
            g.icons.forEach { icon ->
                items += DisplayableItem.ContentItem(icon, icon.resourceId == selectedIconKey)
            }
        }
        iconGroupAdapter.setSelectedColor(selectedColor)
        iconGroupAdapter.submitList(items)
    }

    private fun updateColorIndicator() {
        (binding.viewColorIndicator.background as? GradientDrawable)?.apply {
            setColor(selectedColor)
            setStroke(
                if (ColorUtils.calculateLuminance(selectedColor) > 0.9) 2 else 0,
                Color.LTGRAY
            )
        }
    }

    private fun updateIconPreviewBackground() {
        binding.cardIconPreviewWrapper.setCardBackgroundColor(selectedColor)
        (binding.cardIconPreviewWrapper as MaterialCardView).apply {
            strokeWidth = if (ColorUtils.calculateLuminance(selectedColor) > 0.9) 3 else 0
            strokeColor = Color.LTGRAY
        }
    }

    private fun updateIconPreview() {
        binding.imageViewSelectedIconPreview.apply {
            selectedIconKey?.let { key ->
                setImageResource(IconMap.idOf(key))
                val tint = if (ColorUtils.calculateLuminance(selectedColor) > 0.5) Color.BLACK else Color.WHITE
                setColorFilter(tint)
                visibility = View.VISIBLE
            } ?: run {
                visibility = View.GONE
            }
        }
    }

    private fun setupCreateMode() {
        (requireActivity() as AppCompatActivity).supportActionBar
            ?.title = getString(R.string.add_category_title)
        binding.tabLayoutCategoryTypeCreateEdit.visibility = View.VISIBLE
        // настраиваем табы
        binding.tabLayoutCategoryTypeCreateEdit.apply {
            removeAllTabs()
            addTab(newTab().setText(R.string.category_type_expense))
            addTab(newTab().setText(R.string.category_type_income))
            getTabAt(if (currentSelectedTypeInCreateMode == CategoryType.EXPENSE) 0 else 1)
                ?.select()
            addOnTabSelectedListener(object: TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    currentSelectedTypeInCreateMode =
                        if (tab.position == 0) CategoryType.EXPENSE else CategoryType.INCOME
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
        updateColorIndicator()
        updateIconPreviewBackground()
        setupInitialFocus()
    }

    private fun setupEditMode() {
        (requireActivity() as AppCompatActivity).supportActionBar
            ?.title = getString(R.string.edit_category_title)
        binding.tabLayoutCategoryTypeCreateEdit.visibility = View.GONE

        lifecycleScope.launch {
            categoryToEdit = viewModel.loadCategoryById(args.categoryId)
            categoryToEdit?.let { cat ->
                binding.titleCategory.setText(cat.title)
                selectedIconKey = cat.iconResourceId
                selectedColor    = cat.colorHex ?: DEFAULT_FALLBACK_COLOR

                updateColorIndicator()
                updateIconPreviewBackground()
                updateIconPreview()
                refreshIconGroups()
            } ?: run {
                Toast.makeText(requireContext(),
                    getString(R.string.error_category_not_found),
                    Toast.LENGTH_SHORT).show()
                findNavController().popBackStack()
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun toggleColorPanel(show: Boolean? = null) {
        val panel = binding.cardColorSelectorPanel
        val arrow = binding.imageViewColorArrow
        val expand = show ?: (panel.visibility == View.GONE)
        TransitionManager.beginDelayedTransition(
            binding.contentLayout, AutoTransition().apply { duration = COLOR_PANEL_ANIMATION_DURATION }
        )
        if (expand) {
            panel.visibility = View.VISIBLE
            arrow.animate().rotation(180f).start()
            binding.nestedScrollView.post { binding.nestedScrollView.smoothScrollTo(0, panel.bottom) }
        } else {
            panel.visibility = View.GONE
            arrow.animate().rotation(0f).start()
        }
    }

    private fun setupListeners() {
        binding.colorSelectorContainer.setOnClickListener { toggleColorPanel() }
    }

    private fun setupInitialFocus() {
        binding.titleCategory.post {
            binding.titleCategory.requestFocus()
            (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
                ?.showSoftInput(binding.titleCategory, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    private fun saveCategory() {
        val name = binding.titleCategory.text.toString().trim()
        if (name.isEmpty()) {
            binding.inputLayoutName.error = getString(R.string.error_category_name_empty)
            return
        }
        if (selectedIconKey.isNullOrBlank()) {
            Toast.makeText(requireContext(),
                getString(R.string.error_select_icon_for_category),
                Toast.LENGTH_SHORT).show()
            return
        }

        (requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(view?.windowToken, 0)

        if (isEditMode) {
            categoryToEdit!!.apply {
                title          = name
                iconResourceId = selectedIconKey
                colorHex       = selectedColor
                viewModel.updateCategory(this)
            }
        } else {
            Category(
                title          = Title(name),
                categoryType   = currentSelectedTypeInCreateMode,
                iconResourceId = selectedIconKey,
                colorHex       = selectedColor
            ).also { viewModel.createCategory(it) }
        }

        findNavController().popBackStack()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
