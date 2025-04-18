package com.example.yourfinance.presentation.ui.adapter // Или другое подходящее место

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.presentation.ui.fragment.manager.TransactionAddFragment // Твой фрагмент

// Адаптер для ViewPager2
class TransactionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3 // Три страницы: Расход, Доход, Перевод

    override fun createFragment(position: Int): Fragment {
        // Создаем экземпляр TransactionAddFragment и передаем ему тип через аргументы
        val fragment = TransactionAddFragment()
        fragment.arguments = TransactionAddFragment.newBundle(
            when (position) {
                0 -> TransactionType.EXPENSE
                1 -> TransactionType.INCOME
                2 -> TransactionType.REMITTANCE
                else -> throw IllegalStateException("Invalid position: $position")
            }
        )
        return fragment
    }
}