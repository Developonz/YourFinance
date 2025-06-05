package com.example.yourfinance.presentation.ui.fragment // или com.example.yourfinance.presentation.ui.viewmodel

import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.backup.ExportImportDomainData
import com.example.yourfinance.domain.model.common.Result // ваш Result класс
import com.example.yourfinance.domain.usecase.backup.ExportDataUseCase
import com.example.yourfinance.domain.usecase.backup.ImportDataUseCase
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.FileOutputStream
import java.io.InputStreamReader
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject



sealed class BackupState {
    object Idle : BackupState()
    object InProgress : BackupState()
    data class Success(val message: String) : BackupState()
    data class Error(val message: String) : BackupState()
}

// Адаптеры для LocalDate и LocalTime для Gson
class LocalDateAdapter : JsonSerializer<LocalDate>, JsonDeserializer<LocalDate> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    override fun serialize(src: LocalDate?, typeOfSrc: Type?, context: com.google.gson.JsonSerializationContext?): JsonElement {
        return com.google.gson.JsonPrimitive(src?.format(formatter))
    }
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: com.google.gson.JsonDeserializationContext?): LocalDate? { // Можно возвращать nullable, если это допустимо
        return if (json == null || json.isJsonNull || json.asString.isNullOrEmpty()) {
            null // или LocalDate.now() или выбросить исключение, в зависимости от бизнес-логики
        } else {
            LocalDate.parse(json.asString, formatter)
        }
    }
}

class LocalTimeAdapter : JsonSerializer<LocalTime>, JsonDeserializer<LocalTime> {
    private val formatter = DateTimeFormatter.ISO_LOCAL_TIME
    override fun serialize(src: LocalTime?, typeOfSrc: Type?, context: com.google.gson.JsonSerializationContext?): JsonElement {
        return com.google.gson.JsonPrimitive(src?.format(formatter))
    }
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: com.google.gson.JsonDeserializationContext?): LocalTime {
        return LocalTime.parse(json?.asString, formatter)
    }
}

class TitleAdapter : JsonSerializer<Title>, JsonDeserializer<Title> {
    override fun serialize(src: Title?, typeOfSrc: Type?, context: com.google.gson.JsonSerializationContext?): JsonElement {
        // При сериализации берем внутреннее значение value
        return JsonPrimitive(src?.value)
    }
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: com.google.gson.JsonDeserializationContext?): Title {
        return Title(json?.asString ?: "") // Если json или asString null, будет Title("")
    }
}

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val exportDataUseCase: ExportDataUseCase,
    private val importDataUseCase: ImportDataUseCase
    // Можно инжектировать ApplicationContext, если он нужен для ContentResolver
    // private val applicationContext: android.content.Context
) : ViewModel() {

    private val _backupState = MutableLiveData<BackupState>(BackupState.Idle)
    val backupState: LiveData<BackupState> = _backupState

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .registerTypeAdapter(Title::class.java, TitleAdapter())
            .setPrettyPrinting()
            .create()
    }

    fun exportData(uri: Uri, contentResolver: android.content.ContentResolver) {
        _backupState.value = BackupState.InProgress
        viewModelScope.launch {
            when (val exportResult = exportDataUseCase()) {
                is Result.Success -> {
                    try {
                        val jsonString = gson.toJson(exportResult.data)
                        withContext(Dispatchers.IO) {
                            contentResolver.openOutputStream(uri)?.use { outputStream ->
                                outputStream.write(jsonString.toByteArray())
                            }
                        }
                        _backupState.postValue(BackupState.Success("Экспорт успешно завершен!"))
                    } catch (e: Exception) {
                        _backupState.postValue(BackupState.Error("Ошибка записи файла: ${e.localizedMessage}"))
                    }
                }
                is Result.Error -> {
                    _backupState.postValue(BackupState.Error(exportResult.message ?: "Ошибка экспорта данных"))
                }
                else -> { /* Handle Loading if you have it */ }
            }
        }
    }

    fun importData(uri: Uri, contentResolver: android.content.ContentResolver) {
        _backupState.value = BackupState.InProgress
        // Явно запускаем всю операцию импорта в IO контексте
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val jsonString = contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).use { reader ->
                        reader.readText() // Более простой способ прочитать всё содержимое
                    }
                } ?: "" // Если openInputStream null, возвращаем пустую строку

                Log.d("BACKUP_DEBUG", "Import JSON: $jsonString")

                if (jsonString.isBlank()) {
                    _backupState.postValue(BackupState.Error("Ошибка импорта: файл пуст"))
                    return@launch // Выходим из корутины
                }

                // Десериализация может быть ресурсоемкой
                val importDomainData: ExportImportDomainData = try {
                    gson.fromJson(jsonString, ExportImportDomainData::class.java)
                } catch (e: JsonSyntaxException) {
                    Log.e("BACKUP_DEBUG", "Import GSON JsonSyntaxException: ", e)
                    _backupState.postValue(BackupState.Error("Ошибка импорта: неверный формат JSON. ${e.localizedMessage}"))
                    return@launch
                } catch (e: Exception) { // Другие ошибки Gson при десериализации
                    Log.e("BACKUP_DEBUG", "Import GSON General Deserialization Exception: ", e)
                    _backupState.postValue(BackupState.Error("Ошибка десериализации данных: ${e.localizedMessage}"))
                    return@launch
                }

                // importDataUseCase() - suspend функция, будет выполняться в текущем Dispatchers.IO контексте
                when (val importResult = importDataUseCase(importDomainData)) {
                    is Result.Success -> {
                        _backupState.postValue(BackupState.Success("Импорт успешно завершен!"))
                    }
                    is Result.Error -> {
                        _backupState.postValue(BackupState.Error(importResult.message ?: "Ошибка импорта данных"))
                    }
                    else -> { /* Handle Loading */ }
                }

            } catch (e: Exception) { // Отлов ошибок чтения файла или других непредвиденных ошибок
                Log.e("BACKUP_DEBUG", "General Import Error in ViewModel: ", e)
                _backupState.postValue(BackupState.Error("Ошибка чтения файла или импорта: ${e.localizedMessage}"))
            }
        }
    }

    fun resetBackupState() {
        _backupState.value = BackupState.Idle
    }
}