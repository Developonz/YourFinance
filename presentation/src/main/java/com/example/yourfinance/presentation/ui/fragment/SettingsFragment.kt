package com.example.yourfinance.presentation.ui.fragment


import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.findNavController
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.example.yourfinance.presentation.R

//TODO: сделать прослойку PreferenceDataStore

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.preferences, rootKey)

        // --- Обработчики нажатий для навигации ---

        findPreference<Preference>("pref_accounts")?.setOnPreferenceClickListener {
            // Переход к управлению счетами
            findNavController().navigate(R.id.action_settingsFragment_to_accountManagerFragment)
            true // true означает, что клик обработан
        }

        findPreference<Preference>("pref_categories")?.setOnPreferenceClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_categoriesFragment)
            true
        }

        findPreference<Preference>("pref_budgets")?.setOnPreferenceClickListener {
            // Переход к управлению бюджетами
            findNavController().navigate(R.id.action_settingsFragment_to_budgetManagerFragment)
            true
        }

        findPreference<Preference>("pref_export")?.setOnPreferenceClickListener {
            // TODO: Логика экспорта
            Toast.makeText(context, "Функция Экспорта еще не реализована", Toast.LENGTH_SHORT).show()
            true
        }

        findPreference<Preference>("pref_import")?.setOnPreferenceClickListener {
            // TODO: Логика импорта
            Toast.makeText(context, "Функция Импорта еще не реализована", Toast.LENGTH_SHORT).show()
            true
        }

        // --- Обработка изменений для ListPreference ---

        findPreference<ListPreference>("pref_theme")?.setOnPreferenceChangeListener { preference, newValue ->
            applyTheme(newValue.toString())
            true // true означает, что изменение принято и будет сохранено
        }

        // --- Установка динамических значений ---

        // Установка версии приложения
        try {
            val pInfo = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0)
            val version = pInfo.versionName
            findPreference<Preference>("pref_version")?.summary = version
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            findPreference<Preference>("pref_version")?.summary = "N/A"
        }

        // Можно добавить обработчик для pref_first_day_month, если нужно что-то делать сразу после выбора
        findPreference<ListPreference>("pref_first_day_month")?.setOnPreferenceChangeListener { preference, newValue ->
            // TODO: Сохранить или использовать новое значение первого дня месяца
            // Например, обновить ViewModel или репозиторий
            Toast.makeText(context, "Выбран первый день месяца: $newValue", Toast.LENGTH_SHORT).show()
            true
        }

    }

    // Функция для применения темы
    private fun applyTheme(themeValue: String) {
        when (themeValue) {
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
}