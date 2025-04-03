package com.example.yourfinance.presentation.ui.activity

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.room.util.copy
import com.example.yourfinance.MainApplication
import com.example.yourfinance.R
import com.example.yourfinance.databinding.ActivityMainBinding
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.source.FinanceDao
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.repository.TransactionRepository
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.repository.CategoryRepository
import com.example.yourfinance.domain.repository.MoneyAccountRepository

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: TransactionsViewModel by viewModels()

    @Inject
    lateinit var transactionRepository: TransactionRepository

    @Inject
    lateinit var categoryRepository: CategoryRepository

    @Inject
    lateinit var moneyAccountRepository: MoneyAccountRepository

    @Inject
    lateinit var dao: FinanceDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navView.background = null
        navView.menu.getItem(2).isEnabled = false
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Настраиваем ActionBar
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transactions,
                R.id.navigation_calendar,
                R.id.navigation_statistic,
                R.id.navigation_wallet
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)

        NavigationUI.setupWithNavController(navView, navController)

        binding.fab.setOnClickListener({
            daoInsert()
        })

    }


    private fun daoInsert() {
        CoroutineScope(Dispatchers.IO).launch {
            var category = Category("Зарплата", CategoryType.income)
            var id = categoryRepository.insertCategory(FullCategory(category))
            category = Category("Зарплата", CategoryType.income, id)

            var acc = MoneyAccount("альфа", 5000.0)
            id = moneyAccountRepository.insertAccount(acc)
            acc = MoneyAccount("альфа", id = id)
            transactionRepository.insertPayment(Payment( TransactionType.income,500.0, acc, category))
        }
    }

}