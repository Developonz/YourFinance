// Файл: data/src/main/java/com/example/yourfinance/data/repository/BackupRepositoryImpl.kt
package com.example.yourfinance.data.repository

import android.util.Log
import com.example.yourfinance.data.model.BackupWrapper
import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.FuturePaymentEntity
import com.example.yourfinance.data.model.FutureTransferEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.data.source.FinanceDataBase
import com.example.yourfinance.domain.model.common.Result
import com.example.yourfinance.domain.repository.BackupRepository
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonSerializer
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.Exception
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * Реализация BackupRepository:
 *   - exportJson() читает из Room ‒> запаковывает в BackupWrapper ‒> сериализует в JSON.
 *   - importJson() десериализует JSON в BackupWrapper ‒> очищает БД ‒> восстанавливает записи.
 */
class BackupRepositoryImpl @Inject constructor(
    private val db: FinanceDataBase
) : BackupRepository {

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

    // Gson с адаптерами, если у вас есть LocalDate/LocalTime в Entity
    private val gson: Gson by lazy {
        GsonBuilder()
            // Если у вас есть LocalDate/LocalTime в PaymentEntity/TransferEntity,
            // необходимо зарегистрировать соответствующие адаптеры:
             .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            // .registerTypeAdapter(LocalTime::class.java, LocalTimeAdapter())
            .setPrettyPrinting()
            .serializeNulls()
            .create()
    }

    override suspend fun exportJson(): Result<String> {
        return try {
            // 1) Считываем всё из базы
            val moneyAccounts: List<MoneyAccountEntity> =
                db.getMoneyAccountDao().getAllAccountsForExport()

            val categories: List<CategoryEntity> =
                db.getCategoryDao().getAllCategoriesForExport()

            val payments: List<PaymentEntity> =
                db.getTransactionDao().getAllPaymentsForExport()

            val transfers: List<TransferEntity> =
                db.getTransactionDao().getAllTransfersForExport()


            // 2) Собираем в единый объект
            val wrapper = BackupWrapper(
                moneyAccounts    = moneyAccounts,
                categories       = categories,
                payments         = payments,
                transfers        = transfers
            )

            // 3) Сериализуем в JSON
            val jsonString = gson.toJson(wrapper)

            Result.Success(jsonString)
        } catch (e: Exception) {
            Result.Error(e, "Ошибка при сборе данных для экспорта: ${e.localizedMessage}")
        }
    }

    override suspend fun importJson(json: String): Result<Unit> {
        return try {
            // Весь импорт делаем в IO-контексте
            withContext(Dispatchers.IO) {
                // 1) Парсим JSON в BackupWrapper
                val wrapper: BackupWrapper = try {
                    gson.fromJson(json, BackupWrapper::class.java)
                } catch (e: JsonSyntaxException) {
                    throw IllegalArgumentException("Неверный формат JSON: ${e.localizedMessage}")
                }

                // 2) Запускаем транзакцию, чистим БД и вставляем все сущности
                db.runInTransaction {
                    // 2.1) Чистим все таблицы. (Этот метод автоматически очищает все таблицы.)
                    db.clearAllTables()

                    // 2.2) Счета (MoneyAccountEntity)
                    wrapper.moneyAccounts.forEach { account ->
                        db.getMoneyAccountDao().insertAccount(account.copy())
                        // Используем copy(), чтобы убедиться, что мы даём Room «свежий» объект
                    }

                    // 2.3) Категории (CategoryEntity)
                    //      Сначала корневые (parentId == null), затем дочерние
                    val roots = wrapper.categories.filter { it.parentId == null }
                    val children = wrapper.categories.filter { it.parentId != null }

                    roots.forEach { category ->
                        db.getCategoryDao().insertCategoryInternal(category.copy())
                    }
                    children.forEach { category ->
                        db.getCategoryDao().insertCategoryInternal(category.copy())
                    }

                    // 2.4) Платежи (PaymentEntity)
                    wrapper.payments.forEach { payment ->
                        db.getTransactionDao().insertPaymentTransactionInternal(payment.copy())
                    }

                    // 2.5) Переводы (TransferEntity)
                    wrapper.transfers.forEach { transfer ->
                        db.getTransactionDao().insertTransferTransactionInternal(transfer.copy())
                    }

                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Log.e("BACKUP_REPO_IMPORT", "Error in importJson", e)
            Result.Error(e, "Ошибка импорта данных: ${e.localizedMessage}")
        }
    }
}
