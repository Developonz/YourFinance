package com.example.yourfinance.view.activity

import android.os.Bundle
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.yourfinance.MainApplication
import com.example.yourfinance.R
import com.example.yourfinance.databinding.ActivityMainBinding
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.data.entities.CategoryEntity
import com.example.yourfinance.data.entities.MoneyAccountEntity
import com.example.yourfinance.data.entities.PaymentEntity
import com.example.yourfinance.view.viewmodel.TransactionsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: TransactionsViewModel by viewModels()

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
        val dao = MainApplication.database.getFinanceDao()
        CoroutineScope(Dispatchers.IO).launch {
            dao.insertCategory(CategoryEntity("Зарплата", CategoryEntity.CategoryType.income))
            dao.insertCategory(CategoryEntity("Стипендия", CategoryEntity.CategoryType.income))
            dao.insertAccount(MoneyAccountEntity("Сбер"))
            dao.insertPaymentTransaction(PaymentEntity( Transaction.TransactionType.income,500.0, 1, 1))
            dao.insertPaymentTransaction(PaymentEntity(Transaction.TransactionType.income,6600.0, 1, 2))
        }
    }

}