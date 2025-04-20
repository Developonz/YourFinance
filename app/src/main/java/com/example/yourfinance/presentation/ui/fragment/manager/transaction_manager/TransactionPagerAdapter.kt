package com.example.yourfinance.presentation.ui.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager.ExpenseTransactionFragment
import com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager.IncomeTransactionFragment
import com.example.yourfinance.presentation.ui.fragment.manager.transaction_manager.RemittanceTransactionFragment

class TransactionPagerAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> ExpenseTransactionFragment()
            1 -> IncomeTransactionFragment()
            2 -> RemittanceTransactionFragment()
            else -> throw IllegalStateException("Invalid position: $position")
        }
    }
}