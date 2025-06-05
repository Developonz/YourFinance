package com.example.yourfinance.data.repository

import android.util.Log
import com.example.yourfinance.data.mapper.* // Все ваши мапперы
import com.example.yourfinance.data.model.BudgetCategoriesCrossRef
import com.example.yourfinance.data.model.FuturePaymentEntity
import com.example.yourfinance.data.model.FutureTransferEntity
import com.example.yourfinance.data.source.* // Все ваши DAO
import com.example.yourfinance.domain.model.backup.ExportImportDomainData
import com.example.yourfinance.domain.model.entity.category.BaseCategory
import com.example.yourfinance.domain.repository.BackupRepository
import com.example.yourfinance.domain.model.common.Result
import javax.inject.Inject

import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.backup.PaymentForBackup
import com.example.yourfinance.domain.model.backup.TransferForBackup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackupRepositoryImpl @Inject constructor(
    private val db: FinanceDataBase,
    private val moneyAccountDao: MoneyAccountDao,
    private val categoryDao: CategoryDao,
    private val transactionDao: TransactionDao,
) : BackupRepository {

    override suspend fun getAllDataForExport(): Result<ExportImportDomainData> {
        return try {
            // 1. Категории
            val allCategoryEntities = categoryDao.getAllCategoriesForExport()
            val subcategoriesMap = allCategoryEntities
                .filter { it.parentId != null }
                .groupBy { it.parentId!! } // Группируем подкатегории по ID родителя

            val domainCategories = allCategoryEntities
                .filter { it.parentId == null } // Берем только корневые категории
                .map { parentEntity ->
                    val domainSubcategories = subcategoriesMap[parentEntity.id]
                        ?.map { subEntity -> subEntity.toDomainSubcategory() } // Маппим CategoryEntity в доменную Subcategory
                        ?: emptyList()
                    // Создаем доменную Category с ее подкатегориями
                    com.example.yourfinance.domain.model.entity.category.Category(
                        title = Title(parentEntity.title), // Используем Title.invoke()
                        categoryType = parentEntity.categoryType,
                        id = parentEntity.id, // Старый ID
                        iconResourceId = parentEntity.iconResourceId,
                        colorHex = parentEntity.colorHex,
                        children = domainSubcategories.toMutableList()
                    )
                }

            // 2. Счета
            val moneyAccounts = moneyAccountDao.getAllAccountsForExport().map { it.toDomain() }

            // 3. Платежи (преобразуем в PaymentForBackup)
            val paymentsForBackup = transactionDao.getAllPaymentsForExport().map { fullPayment ->
                PaymentForBackup(
                    id = fullPayment.payment.id,
                    type = fullPayment.payment.type,
                    balance = fullPayment.payment.balance,
                    moneyAccountId = fullPayment.payment.moneyAccID, // ID счета из PaymentEntity
                    categoryId = fullPayment.payment.categoryID,     // ID категории из PaymentEntity
                    note = fullPayment.payment.note,                 // Заметка как строка
                    date = fullPayment.payment.date
                )
            }

            // 4. Переводы
            val transfersForBackup = transactionDao.getAllTransfersForExport().map { fullTransfer ->
                // fullTransfer.transfer это TransferEntity
                TransferForBackup(
                    id = fullTransfer.transfer.id,
                    type = fullTransfer.transfer.type, // Должен быть TransactionType.REMITTANCE
                    balance = fullTransfer.transfer.balance,
                    moneyAccFromId = fullTransfer.transfer.moneyAccFromID, // ID из TransferEntity
                    moneyAccToId = fullTransfer.transfer.moneyAccToID,     // ID из TransferEntity
                    note = fullTransfer.transfer.note,                     // Заметка как строка
                    date = fullTransfer.transfer.date
                )
            }

            // 5. Будущие транзакции (ID)
            val futurePaymentIds = db.getFutureTransactionDao().getAllFuturePaymentsForExport().map { it.id }
            val futureTransferIds = db.getFutureTransactionDao().getAllFutureTransfersForExport().map { it.id }

            // (Бюджеты по-прежнему не включены в ExportImportDomainData, но логика их сбора была бы здесь)

            Result.Success(
                ExportImportDomainData(
                    moneyAccounts = moneyAccounts,
                    categories = domainCategories,
                    payments = paymentsForBackup,
                    transfers = transfersForBackup,
                    futurePaymentIds = futurePaymentIds,
                    futureTransferIds = futureTransferIds
                )
            )
        } catch (e: Exception) {
            Result.Error(e, "Ошибка получения данных для экспорта: ${e.localizedMessage}")
        }
    }

    override suspend fun importAllData(data: ExportImportDomainData): Result<Unit> {
        // Весь блок импорта выполняется в IO контексте, заданном в SettingsViewModel
        // или если этот метод вызывается из другого suspend контекста на IO.
        // Для дополнительной гарантии можно обернуть в withContext(Dispatchers.IO),
        // но если вызывающая сторона уже на IO, это будет избыточно.
        // `db.runInTransaction` сама по себе должна быть main-safe, если DAO методы suspend.
        return try {
            // Явно указываем Dispatchers.IO для всей операции с базой данных
            withContext(Dispatchers.IO) {
                db.runInTransaction {
                    // 0. Очистка всех таблиц
                    // Убедитесь, что все DAO-методы внутри clearAllTablesForImport являются suspend
                    db.clearAllTables()

                    // 1. Импорт счетов (MoneyAccount)
                    val oldToNewMoneyAccountIds = mutableMapOf<Long, Long>()
                    data.moneyAccounts.forEach { domainAcc ->
                        val oldId = domainAcc.id
                        val newId = moneyAccountDao.insertAccount(domainAcc.toData().copy(id = 0)) // suspend
                        oldToNewMoneyAccountIds[oldId] = newId
                    }

                    // 2. Импорт категорий (Category и Subcategory)
                    val oldToNewCategoryIds = mutableMapOf<Long, Long>()
                    data.categories.forEach { domainCategory ->
                        val oldParentId = domainCategory.id
                        val newParentId = categoryDao.insertCategoryInternal( // suspend
                            domainCategory.toData().copy(id = 0, parentId = null)
                        )
                        oldToNewCategoryIds[oldParentId] = newParentId
                        domainCategory.children.forEach { domainSubcategory ->
                            val oldSubId = domainSubcategory.id
                            val newSubId = categoryDao.insertCategoryInternal( // suspend
                                domainSubcategory.toData().copy(id = 0, parentId = newParentId)
                            )
                            oldToNewCategoryIds[oldSubId] = newSubId
                        }
                    }

                    // 3. Импорт платежей (PaymentForBackup -> PaymentEntity)
                    val oldToNewPaymentIds = mutableMapOf<Long, Long>()
                    data.payments.forEach { paymentForBackup ->
                        val oldPaymentId = paymentForBackup.id
                        val newMoneyAccId = oldToNewMoneyAccountIds[paymentForBackup.moneyAccountId]
                        val newCategoryId = oldToNewCategoryIds[paymentForBackup.categoryId]
                        if (newMoneyAccId != null && newCategoryId != null) {
                            val paymentEntity = PaymentEntity(
                                id = 0,
                                type = paymentForBackup.type,
                                balance = paymentForBackup.balance,
                                moneyAccID = newMoneyAccId,
                                categoryID = newCategoryId,
                                note = paymentForBackup.note,
                                date = paymentForBackup.date
                            )
                            val newPaymentEntityId = transactionDao.insertPaymentTransactionInternal(paymentEntity) // suspend
                            oldToNewPaymentIds[oldPaymentId] = newPaymentEntityId
                        } else {
                            Log.w("BACKUP_REPO_IMPORT", "Пропуск импорта платежа (старый ID: $oldPaymentId): не найден новый ID для счета или категории.")
                        }
                    }

                    // 4. Импорт переводов (TransferForBackup -> TransferEntity)
                    val oldToNewTransferIds = mutableMapOf<Long, Long>()
                    data.transfers.forEach { transferForBackup ->
                        val oldTransferId = transferForBackup.id
                        val newMoneyAccFromId = oldToNewMoneyAccountIds[transferForBackup.moneyAccFromId]
                        val newMoneyAccToId = oldToNewMoneyAccountIds[transferForBackup.moneyAccToId]
                        if (newMoneyAccFromId != null && newMoneyAccToId != null) {
                            val transferEntity = TransferEntity(
                                id = 0,
                                type = transferForBackup.type,
                                balance = transferForBackup.balance,
                                moneyAccFromID = newMoneyAccFromId,
                                moneyAccToID = newMoneyAccToId,
                                note = transferForBackup.note,
                                date = transferForBackup.date
                            )
                            val newTransferEntityId = transactionDao.insertTransferTransactionInternal(transferEntity) // suspend
                            oldToNewTransferIds[oldTransferId] = newTransferEntityId
                        } else {
                            Log.w("BACKUP_REPO_IMPORT", "Пропуск импорта перевода (старый ID: $oldTransferId): не найден новый ID для счета 'От' или 'Кому'.")
                        }
                    }

                    // 5. Импорт будущих платежей
                    data.futurePaymentIds.forEach { oldFuturePaymentId ->
                        val newPaymentId = oldToNewPaymentIds[oldFuturePaymentId]
                        if (newPaymentId != null) {
                            db.getFutureTransactionDao().insertFuturePaymentTransaction( // suspend
                                FuturePaymentEntity(id = newPaymentId)
                            )
                        } else {
                            Log.w("BACKUP_REPO_IMPORT", "Пропуск импорта FuturePayment (старый ID платежа: $oldFuturePaymentId): оригинальный платеж не был импортирован.")
                        }
                    }

                    // 6. Импорт будущих переводов
                    data.futureTransferIds.forEach { oldFutureTransferId ->
                        val newTransferId = oldToNewTransferIds[oldFutureTransferId]
                        if (newTransferId != null) {
                            db.getFutureTransactionDao().insertFutureTransferTransaction( // suspend
                                FutureTransferEntity(id = newTransferId)
                            )
                        } else {
                            Log.w("BACKUP_REPO_IMPORT", "Пропуск импорта FutureTransfer (старый ID перевода: $oldFutureTransferId): оригинальный перевод не был импортирован.")
                        }
                    }
                } // Конец db.runInTransaction
                Result.Success(Unit) // Возвращается из withContext(Dispatchers.IO)
            } // Конец withContext(Dispatchers.IO)
        } catch (e: Exception) {
            Log.e("BACKUP_REPO_IMPORT", "Error in importAllData", e)
            Result.Error(e, "Ошибка импорта данных: ${e.localizedMessage}")
        }
    }
}

