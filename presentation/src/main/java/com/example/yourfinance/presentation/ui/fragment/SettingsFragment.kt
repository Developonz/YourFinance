// Файл: presentation/src/main/java/com/example/yourfinance/presentation/ui/fragment/SettingsFragment.kt
package com.example.yourfinance.presentation.ui.fragment

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.graphics.drawable.DrawableCompat.applyTheme
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.yourfinance.presentation.R
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@AndroidEntryPoint
class SettingsFragment : PreferenceFragmentCompat() {

    private val viewModel: SettingsViewModel by viewModels()

    private val exportFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri: Uri ->
                viewModel.exportData(uri, requireActivity().contentResolver)
            }
        }
    }

    private val importFileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.also { uri: Uri ->
                showImportConfirmationDialog(uri)
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // Навигация
        findPreference<Preference>("pref_accounts")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_accountManagerFragment)
            true
        }
        findPreference<Preference>("pref_categories")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_categoriesFragment)
            true
        }
        findPreference<Preference>("pref_budgets")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_budgetManagerFragment)
            true
        }

        // Тема
        findPreference<ListPreference>("pref_theme")?.setOnPreferenceChangeListener { _, newValue ->
            applyTheme(newValue.toString())
            true
        }

        // Экспорт
        findPreference<Preference>("pref_export")?.setOnPreferenceClickListener {
            initiateExport()
            true
        }
        // Импорт
        findPreference<Preference>("pref_import")?.setOnPreferenceClickListener {
            initiateImport()
            true
        }


        // Версия
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            findPreference<Preference>("pref_version")?.summary = pInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            findPreference<Preference>("pref_version")?.summary = "N/A"
        }



    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun applyTheme(themeValue: String) {
        when (themeValue) {
            "light"  -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark"   -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }


    private fun observeViewModel() {
        viewModel.backupState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is BackupState.Idle -> { /* ничего не показываем */ }
                is BackupState.InProgress -> {
                    Toast.makeText(context, "Выполняется операция...", Toast.LENGTH_SHORT).show()
                }
                is BackupState.Success -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetBackupState()
                }
                is BackupState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                    viewModel.resetBackupState()
                }
            }
        }
    }

    private fun initiateExport() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "yourfinance_backup_$timestamp.json"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        exportFileLauncher.launch(intent)
    }

    private fun initiateImport() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        importFileLauncher.launch(intent)
    }

    private fun showImportConfirmationDialog(uri: Uri) {
        AlertDialog.Builder(requireContext())
            .setTitle("Импорт данных")
            .setMessage(
                "ВНИМАНИЕ! Все текущие данные будут удалены и заменены данными из файла. " +
                        "Это действие необратимо. Продолжить?"
            )
            .setPositiveButton("Импортировать") { dialog, _ ->
                viewModel.importData(uri, requireActivity().contentResolver)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }
}
