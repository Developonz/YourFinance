package com.example.yourfinance.presentation.ui.fragment // или com.example.yourfinance.presentation.ui.viewmodel


import android.net.Uri
import android.util.Log
import androidx.lifecycle.*
import com.example.yourfinance.domain.model.common.Result
import com.example.yourfinance.domain.usecase.backup.ExportDataUseCase
import com.example.yourfinance.domain.usecase.backup.ImportDataUseCase
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

sealed class BackupState {
    object Idle : BackupState()
    object InProgress : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportUseCase: ExportDataUseCase,
    private val importUseCase: ImportDataUseCase
) : ViewModel() {

    private val _backupState = MutableLiveData<BackupState>(BackupState.Idle)
    val backupState: LiveData<BackupState> = _backupState

    /**
     * Экспортируем JSON в файл.
     * Здесь exportUseCase возвращает просто JSON-строку.
     */
    fun exportData(uri: Uri, contentResolver: android.content.ContentResolver) {
        _backupState.value = BackupState.InProgress

        viewModelScope.launch {
            when (val result = exportUseCase()) {
                is Result.Success -> {
                    // result.data — это JSON-строка
                    try {
                        val jsonString = result.data
                        withContext(Dispatchers.IO) {
                            contentResolver.openOutputStream(uri)?.use { out ->
                                out.write(jsonString.toByteArray())
                            } ?: throw IllegalStateException("Нельзя открыть OutputStream для записи")
                        }
                        _backupState.postValue(BackupState.Success("Экспорт успешно завершён"))
                    } catch (e: Exception) {
                        _backupState.postValue(BackupState.Error("Ошибка при записи файла: ${e.localizedMessage}"))
                    }
                }
                is Result.Error -> {
                    _backupState.postValue(BackupState.Error(result.message ?: "Неизвестная ошибка экспорта"))
                }
            }
        }
    }

    /**
     * Импортируем JSON из файла.
     * Сначала читаем строку, затем передаём её в importUseCase(jsonString).
     */
    fun importData(uri: Uri, contentResolver: android.content.ContentResolver) {
        _backupState.value = BackupState.InProgress

        viewModelScope.launch {
            val jsonString: String = try {
                withContext(Dispatchers.IO) {
                    contentResolver.openInputStream(uri)?.use { inputStream ->
                        BufferedReader(InputStreamReader(inputStream)).use { it.readText() }
                    } ?: ""
                }
            } catch (e: Exception) {
                _backupState.postValue(BackupState.Error("Не удалось прочитать файл: ${e.localizedMessage}"))
                return@launch
            }

            if (jsonString.isBlank()) {
                _backupState.postValue(BackupState.Error("Ошибка импорта: файл пуст"))
                return@launch
            }

            // Если JSON синтаксически неверен, ImportDataUseCase или Repository,
            // при попытке gson.fromJson(...) выдаст JsonSyntaxException → поймаем ниже.
            when (val importResult = importUseCase(jsonString)) {
                is Result.Success -> {
                    _backupState.postValue(BackupState.Success("Импорт успешно завершён"))
                }
                is Result.Error -> {
                    _backupState.postValue(BackupState.Error(importResult.message ?: "Ошибка импорта данных"))
                }
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }
}
