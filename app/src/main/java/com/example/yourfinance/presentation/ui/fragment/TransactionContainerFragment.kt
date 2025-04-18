package com.example.yourfinance.presentation.ui.fragment // Или другое подходящее место

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels // Используем тот же shared ViewModel
import androidx.viewpager2.widget.ViewPager2
import com.example.yourfinance.databinding.FragmentTransactionContainerBinding // Создай этот binding
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.presentation.ui.adapter.TransactionPagerAdapter
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel // Импорт ViewModel
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Если используешь Hilt здесь
class TransactionContainerFragment : Fragment() {

    private var _binding: FragmentTransactionContainerBinding? = null
    private val binding get() = _binding!!

    // Получаем доступ к общему ViewModel
    private val viewModel: TransactionsViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionContainerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPagerAndTabs()
    }

    private fun setupViewPagerAndTabs() {
        val viewPager = binding.viewPager
        val tabLayout = binding.tabLayoutContainer

        // Создаем адаптер, передавая ЭТОТ хост-фрагмент (или requireActivity(), если адаптер в Activity)
        val adapter = TransactionPagerAdapter(this)
        viewPager.adapter = adapter

        // Устанавливаем offscreenPageLimit для пре-инициализации
        // Для 3 страниц limit = 2 (текущая + 1 слева + 1 справа)
        viewPager.offscreenPageLimit = 2
        Log.d("ContainerFrag", "ViewPager offscreenPageLimit set to ${viewPager.offscreenPageLimit}")


        // Связываем TabLayout и ViewPager2
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Расход"
                1 -> "Доход"
                2 -> "Перевод"
                else -> null
            }
        }.attach()

        // Слушатель смены страниц для обновления ТИПА в ViewModel
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                val selectedType = when (position) {
                    0 -> TransactionType.EXPENSE
                    1 -> TransactionType.INCOME
                    2 -> TransactionType.REMITTANCE
                    else -> viewModel.currentTransactionType.value ?: TransactionType.EXPENSE // Fallback
                }
                Log.i("ContainerFrag", "Page selected: $position, setting ViewModel type to: $selectedType")
                // Устанавливаем тип в общем ViewModel ТОЛЬКО при смене страницы
                viewModel.setTransactionType(selectedType)
            }
        })

        // Установим начальную страницу, если нужно (например, если ViewModel уже содержит тип)
        val initialPosition = when (viewModel.currentTransactionType.value) {
            TransactionType.INCOME -> 1
            TransactionType.REMITTANCE -> 2
            else -> 0 // EXPENSE или null
        }
        if (viewPager.currentItem != initialPosition) {
            Log.d("ContainerFrag", "Setting initial ViewPager position to $initialPosition based on ViewModel")
            viewPager.setCurrentItem(initialPosition, false) // Устанавливаем без анимации
        } else {
            // Если начальная позиция уже 0, нужно явно установить тип в VM,
            // т.к. onPageSelected не сработает при первом показе
            if (initialPosition == 0 && viewModel.currentTransactionType.value != TransactionType.EXPENSE) {
                Log.d("ContainerFrag", "Initial position is 0, explicitly setting VM type to EXPENSE")
                viewModel.setTransactionType(TransactionType.EXPENSE)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Отсоединяем слушатель ViewPager, чтобы избежать утечек
        // binding.viewPager.unregisterOnPageChangeCallback(...) // Нужно сохранить ссылку на callback
        _binding = null
    }
}