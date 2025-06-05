package com.example.yourfinance.domain.model.backup

import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.model.entity.category.Category // Ваша основная модель категории с подкатегориями
import java.time.LocalDate

// Если нужно отдельно хранить связи Budget-Category, можно добавить data class для них
// Но проще, если Budget уже содержит список BaseCategory

// Новый класс для представления платежа в файле бэкапа
data class PaymentForBackup(
    val id: Long, // Старый ID для маппинга
    val type: TransactionType,
    val balance: Double,
    val moneyAccountId: Long, // ID счета, к которому привязан платеж
    val categoryId: Long,     // ID категории (основной или подкатегории)
    val note: String,         // Заметка как простая строка
    val date: LocalDate
)

data class TransferForBackup(
    val id: Long, // Старый ID для маппинга
    val type: TransactionType, // Должен быть REMITTANCE
    val balance: Double,
    val moneyAccFromId: Long, // ID счета-источника
    val moneyAccToId: Long,   // ID счета-получателя
    val note: String,         // Заметка как простая строка
    val date: LocalDate
)

data class ExportImportDomainData(
    val moneyAccounts: List<MoneyAccount>,
    val categories: List<Category>, // Список корневых категорий с их подкатегориями
    val payments: List<PaymentForBackup>, // Используем новый класс
    val transfers: List<TransferForBackup>,
    val futurePaymentIds: List<Long>,
    val futureTransferIds: List<Long>
    // Бюджеты пока не включены, но если будут, их модель также должна быть простой для сериализации
)