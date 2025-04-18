package com.example.yourfinance.presentation.ui.activity

import android.os.Bundle
import android.view.MenuItem // Добавь для onSupportNavigateUp
import android.view.View
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController // Импорт NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.yourfinance.R
import com.example.yourfinance.databinding.ActivityMainBinding
import com.example.yourfinance.data.source.FinanceDao
// ... другие импорты
import com.example.yourfinance.presentation.viewmodel.TransactionsViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
// ... остальные импорты

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TransactionsViewModel by viewModels()
    private lateinit var navController: NavController // Сохраним ссылку для onSupportNavigateUp
    private lateinit var appBarConfiguration: AppBarConfiguration // Сохраним для onSupportNavigateUp

    // ... (Injects) ...

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navView.background = null
        navView.menu.getItem(2).isEnabled = false // Средний элемент неактивен

        // Находим NavController
        navController = findNavController(R.id.nav_host_fragment_activity_main)

        // Настраиваем AppBarConfiguration с Top-level destinations
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transactions,
                R.id.navigation_calendar,
                R.id.navigation_statistic,
                R.id.navigation_wallet
                // НЕ добавляем transactionContainerFragment сюда, чтобы у него была стрелка "Назад"
            )
        )

        // Настраиваем ActionBar с NavController и AppBarConfiguration
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Связываем BottomNavigationView с NavController
        NavigationUI.setupWithNavController(navView, navController)

        // Слушатель смены destination для скрытия/показа BottomAppBar и FAB
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                // Добавляем новый контейнер в список экранов, где нужно скрыть панель
                R.id.transactionContainerFragment, // <-- ДОБАВЛЕНО
                R.id.accountManagerFragment,
                R.id.budgetManagerFragment,
                R.id.settingsFragment,
                R.id.categoriesFragment,
                R.id.subcategoriesFragment,
                R.id.subcategoryCreateEditFragment,
                R.id.categoryCreateEditFragment,
                    // R.id.transactionAddFragment, // <-- УДАЛЕНО (заменено контейнером)
                R.id.accountCreateEditManager -> {
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fab.visibility = View.GONE
                }
                else -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fab.visibility = View.VISIBLE
                }
            }
        }

        // Обновляем OnClickListener для FAB
        binding.fab.setOnClickListener {
            // Навигируем на НОВЫЙ контейнер
            navController.navigate(R.id.transactionContainerFragment) // <-- ИЗМЕНЕНО
        }
    }

    // Обработка нажатия системной кнопки "Назад" и стрелки "Назад" в ActionBar
    override fun onSupportNavigateUp(): Boolean {
        // Используем NavController для навигации назад или к "домашнему" экрану
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }

    // --- Дополнительные поля и методы Activity, если они были ---
    // @Inject lateinit var transactionRepository: TransactionRepository
    // ... и так далее ...
}