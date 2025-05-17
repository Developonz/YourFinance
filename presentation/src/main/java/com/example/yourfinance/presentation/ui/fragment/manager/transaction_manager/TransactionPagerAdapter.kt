package com.example.yourfinance.presentation.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yourfinance.domain.model.CategoryType // Убедитесь, что импорт правильный
import com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager.ExpenseIncomeTransactionFragment // Новый фрагмент
import com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager.RemittanceTransactionFragment

class TransactionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ExpenseIncomeTransactionFragment.newInstance(CategoryType.EXPENSE)
            1 -> ExpenseIncomeTransactionFragment.newInstance(CategoryType.INCOME)
            2 -> RemittanceTransactionFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}