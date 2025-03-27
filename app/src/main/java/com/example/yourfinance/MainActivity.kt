package com.example.yourfinance

import android.os.Bundle
import android.util.Log
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.room.Room
import com.example.yourfinance.databinding.ActivityMainBinding
import com.example.yourfinance.db.FinanceDataBase
import com.example.yourfinance.model.Transaction
import com.example.yourfinance.model.entities.Category
import com.example.yourfinance.model.entities.MoneyAccount
import com.example.yourfinance.model.entities.Payment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var bottomAppBarHeight: Int = 0
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)




//        val dao = MainApplication.database.getFinanceDao()
//
//        CoroutineScope(Dispatchers.IO).launch {
////            dao.insertCategory(Category("Зарплата", Category.CategoryType.income))
////            dao.insertCategory(Category("Стипендия", Category.CategoryType.income))
////            dao.insertAccount(MoneyAccount("Сбер"))
//            dao.insertPaymentTransaction(Payment( Transaction.TransactionType.income,500.0, 1, 1))
//            dao.insertPaymentTransaction(Payment(Transaction.TransactionType.income,6600.0, 1, 2))
//
//        }





        val navView: BottomNavigationView = binding.navView
        navView.background = null
        navView.menu.getItem(2).isEnabled = false
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transactions, R.id.navigation_calendar, R.id.navigation_statistic, R.id.navigation_wallet
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


    }
}