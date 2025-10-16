package com.example.yourfinance.presentation.ui.activity

//imports
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import androidx.navigation.fragment.NavHostFragment
import com.example.yourfinance.presentation.R
import com.example.yourfinance.presentation.databinding.ActivityMainBinding


@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView.apply {
            background = null
            menu.getItem(3).isEnabled = false
        }


        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_transactions,
                R.id.navigation_shopping_list,
                R.id.navigation_statistic,
                R.id.navigation_wallet
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        NavigationUI.setupWithNavController(navView, navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.transactionContainerFragment,
                R.id.accountManagerFragment,
                R.id.budgetManagerFragment,
                R.id.settingsFragment,
                R.id.categoriesFragment,
                R.id.subcategoriesFragment,
                R.id.subcategoryCreateEditFragment,
                R.id.categoryCreateEditFragment,
                R.id.accountCreateEditManager,
                R.id.budgetCreateEditFragment-> {
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fab.visibility = View.GONE
                }
                else -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fab.visibility = View.VISIBLE
                }
            }
        }

        binding.fab.setOnClickListener {
            navController.navigate(R.id.transactionContainerFragment)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp()
    }
}