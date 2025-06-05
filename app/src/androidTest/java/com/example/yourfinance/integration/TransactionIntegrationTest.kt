package com.example.yourfinance.integration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.example.yourfinance.data.repository.CategoryRepositoryImpl
import com.example.yourfinance.data.repository.MoneyAccountRepositoryImpl
import com.example.yourfinance.data.repository.TransactionRepositoryImpl
import com.example.yourfinance.data.source.*
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.model.entity.category.BaseCategory
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.repository.CategoryRepository
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import com.example.yourfinance.domain.repository.TransactionRepository
import com.example.yourfinance.domain.usecase.categories.general.CreateAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.CreateMoneyAccountUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.LoadMoneyAccountByIdUseCase
import com.example.yourfinance.domain.usecase.transaction.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class TransactionIntegrationTest {

    @get:Rule(order = 0)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FinanceDataBase

    private lateinit var moneyAccountDao: MoneyAccountDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao
    private lateinit var futureTransactionDao: FutureTransactionDao

    private lateinit var moneyAccountRepository: MoneyAccountRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var createMoneyAccountUseCase: CreateMoneyAccountUseCase
    private lateinit var createCategoryUseCase: CreateAnyCategoryUseCase
    private lateinit var loadMoneyAccountByIdUseCase: LoadMoneyAccountByIdUseCase

    private lateinit var createPaymentUseCase: CreatePaymentUseCase
    private lateinit var createTransferUseCase: CreateTransferUseCase
    private lateinit var fetchTransactionsUseCase: FetchTransactionsUseCase
    private lateinit var loadPaymentByIdUseCase: LoadPaymentByIdUseCase
    private lateinit var loadTransferByIdUseCase: LoadTransferByIdUseCase
    private lateinit var updatePaymentUseCase: UpdatePaymentUseCase
    private lateinit var updateTransferUseCase: UpdateTransferUseCase
    private lateinit var deleteTransactionUseCase: DeleteTransactionUseCase

    private lateinit var account1: MoneyAccount
    private lateinit var account2: MoneyAccount
    private lateinit var incomeCategory: BaseCategory
    private lateinit var expenseCategory: BaseCategory

    private val initialBalanceAccount1 = 1000.0
    private val initialBalanceAccount2 = 500.0


    @Before
    fun setup() = runTest {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FinanceDataBase::class.java
        )
            .allowMainThreadQueries()
            .build()

        moneyAccountDao = database.getMoneyAccountDao()
        categoryDao = database.getCategoryDao()
        transactionDao = database.getTransactionDao()
        futureTransactionDao = database.getFutureTransactionDao()

        moneyAccountRepository = MoneyAccountRepositoryImpl(moneyAccountDao)
        categoryRepository = CategoryRepositoryImpl(categoryDao)
        transactionRepository = TransactionRepositoryImpl(transactionDao)

        createMoneyAccountUseCase = CreateMoneyAccountUseCase(moneyAccountRepository)
        createCategoryUseCase = CreateAnyCategoryUseCase(categoryRepository)
        loadMoneyAccountByIdUseCase = LoadMoneyAccountByIdUseCase(moneyAccountRepository)

        createPaymentUseCase = CreatePaymentUseCase(transactionRepository)
        createTransferUseCase = CreateTransferUseCase(transactionRepository)
        fetchTransactionsUseCase = FetchTransactionsUseCase(transactionRepository)
        loadPaymentByIdUseCase = LoadPaymentByIdUseCase(transactionRepository)
        loadTransferByIdUseCase = LoadTransferByIdUseCase(transactionRepository)
        updatePaymentUseCase = UpdatePaymentUseCase(transactionRepository)
        updateTransferUseCase = UpdateTransferUseCase(transactionRepository)
        deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)

        val account1Id = createMoneyAccountUseCase(
            MoneyAccount(
                _title = Title("Основной Кошелек"),
                startBalance = initialBalanceAccount1,
                balance = initialBalanceAccount1
            )
        )
        val account2Id = createMoneyAccountUseCase(
            MoneyAccount(
                _title = Title("Сберегательный Счет"),
                startBalance = initialBalanceAccount2,
                balance = initialBalanceAccount2
            )
        )

        account1 = loadMoneyAccountByIdUseCase(account1Id)!!
        account2 = loadMoneyAccountByIdUseCase(account2Id)!!

        val incomeCategoryId = createCategoryUseCase(
            Category(
                title = Title("Зарплата"),
                categoryType = CategoryType.INCOME,
                iconResourceId = "ic_salary_icon",
                colorHex = 0x4CAF50.toInt()
            )
        )
        val expenseCategoryId = createCategoryUseCase(
            Category(
                title = Title("Продукты"),
                categoryType = CategoryType.EXPENSE,
                iconResourceId = "ic_food_icon",
                colorHex = 0xF44336.toInt()
            )
        )

        val loadedIncomeCat = categoryRepository.loadCategoryById(incomeCategoryId)
        assertNotNull("Категория дохода 'Зарплата' должна быть загружена", loadedIncomeCat)
        incomeCategory = BaseCategory(
            _title = Title(loadedIncomeCat!!.title),
            categoryType = loadedIncomeCat.categoryType,
            id = loadedIncomeCat.id,
            iconResourceId = loadedIncomeCat.iconResourceId,
            colorHex = loadedIncomeCat.colorHex
        )

        val loadedExpenseCat = categoryRepository.loadCategoryById(expenseCategoryId)
        assertNotNull("Категория расхода 'Продукты' должна быть загружена", loadedExpenseCat)
        expenseCategory = BaseCategory(
            _title = Title(loadedExpenseCat!!.title),
            categoryType = loadedExpenseCat.categoryType,
            id = loadedExpenseCat.id,
            iconResourceId = loadedExpenseCat.iconResourceId,
            colorHex = loadedExpenseCat.colorHex
        )
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Throws(InterruptedException::class, TimeoutException::class)
    fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS,
        condition: (T?) -> Boolean = { it != null }
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                if (condition(value)) {
                    data = value
                    latch.countDown()
                    this@getOrAwaitValue.removeObserver(this)
                }
            }
        }

        // Проверка начального значения
        if (condition(this.value)) {
            @Suppress("UNCHECKED_CAST")
            return this.value as T
        }

        this.observeForever(observer)
        try {
            if (!latch.await(time, timeUnit)) {
                val currentValue = this.value
                throw TimeoutException("LiveData value was never set or did not satisfy condition. Current value: $currentValue")
            }
        } finally {
            this.removeObserver(observer)
        }
        @Suppress("UNCHECKED_CAST")
        return data as T
    }


    @Test
    fun createIncomePayment_updatesAccountBalance() = runTest {
        val paymentAmount = 250.75
        val incomePayment = Payment(
            type = TransactionType.INCOME,
            balance = paymentAmount,
            moneyAccount = account1,
            category = incomeCategory,
            _note = Title("Премия"),
            date = LocalDate.now()
        )

        val paymentId = createPaymentUseCase(incomePayment)
        assertTrue("ID платежа должен быть больше 0", paymentId > 0L)

        val updatedAccount1 = loadMoneyAccountByIdUseCase(account1.id)
        assertNotNull("Обновленный 'Основной Кошелек' не должен быть null", updatedAccount1)
        assertEquals(
            "Баланс 'Основного Кошелька' должен увеличиться на сумму платежа",
            initialBalanceAccount1 + paymentAmount,
            updatedAccount1?.balance!!,
            0.001
        )

        val fetchedPayment = loadPaymentByIdUseCase(paymentId)
        assertNotNull("Загруженный платеж 'Премия' не должен быть null", fetchedPayment)
        assertEquals("Сумма загруженного платежа 'Премия' должна совпадать", paymentAmount, fetchedPayment?.balance!!, 0.001)
        assertEquals("ID счета в загруженном платеже 'Премия' должен совпадать", account1.id, fetchedPayment?.moneyAccount?.id)
        assertEquals("Заметка в загруженном платеже должна быть 'Премия'", "Премия", fetchedPayment?.note)

        val futureEntryCount = futureTransactionDao.loadCountFuturePaymentTransaction(paymentId)
        assertEquals("Для текущего платежа 'Премия' не должно быть записей о будущих платежах", 0, futureEntryCount)
    }

    @Test
    fun createFutureExpensePayment_andDoesNotChangeBalanceNow() = runTest {
        val paymentAmount = 75.50
        val futureDate = LocalDate.now().plusDays(7)
        val futureExpensePayment = Payment(
            type = TransactionType.EXPENSE,
            balance = paymentAmount,
            moneyAccount = account1,
            category = expenseCategory,
            _note = Title("Оплата интернета (будущий)"),
            date = futureDate
        )

        val paymentId = createPaymentUseCase(futureExpensePayment)
        assertTrue("ID будущего платежа 'Оплата интернета' должен быть больше 0", paymentId > 0L)

        val currentAccount1 = loadMoneyAccountByIdUseCase(account1.id)
        assertNotNull("'Основной Кошелек' не должен быть null", currentAccount1)
        assertEquals(
            "Баланс 'Основного Кошелька' не должен измениться для будущего платежа",
            initialBalanceAccount1,
            currentAccount1?.balance!!,
            0.001
        )

        val futureEntryCount = futureTransactionDao.loadCountFuturePaymentTransaction(paymentId)
        assertTrue("Для будущего платежа 'Оплата интернета' должна быть запись в FutureTransactionDao", futureEntryCount > 0)

        val fetchedPayment = loadPaymentByIdUseCase(paymentId)
        assertNotNull("Загруженный будущий платеж 'Оплата интернета' не должен быть null", fetchedPayment)
        assertEquals("Сумма загруженного платежа 'Оплата интернета' должна совпадать", paymentAmount, fetchedPayment?.balance!!, 0.001)
        assertEquals("Дата загруженного платежа 'Оплата интернета' должна быть будущей", futureDate, fetchedPayment?.date)
    }

    @Test
    fun createTransfer_updatesBothAccountBalances() = runTest {
        val transferAmount = 150.0
        val transferNote = "Перевод на сбережения"
        val transfer = Transfer(
            type = TransactionType.REMITTANCE,
            balance = transferAmount,
            moneyAccFrom = account1,
            moneyAccTo = account2,
            _note = Title(transferNote),
            date = LocalDate.now()
        )

        val transferId = createTransferUseCase(transfer)
        assertTrue("ID перевода 'Перевод на сбережения' должен быть больше 0", transferId > 0L)

        val updatedAccount1 = loadMoneyAccountByIdUseCase(account1.id)
        val updatedAccount2 = loadMoneyAccountByIdUseCase(account2.id)

        assertNotNull("Обновленный 'Основной Кошелек' не должен быть null", updatedAccount1)
        assertNotNull("Обновленный 'Сберегательный Счет' не должен быть null", updatedAccount2)

        assertEquals(
            "Баланс 'Основного Кошелька' должен уменьшиться на сумму перевода",
            initialBalanceAccount1 - transferAmount,
            updatedAccount1?.balance!!,
            0.001
        )
        assertEquals(
            "Баланс 'Сберегательного Счета' должен увеличиться на сумму перевода",
            initialBalanceAccount2 + transferAmount,
            updatedAccount2?.balance!!,
            0.001
        )

        val fetchedTransfer = loadTransferByIdUseCase(transferId)
        assertNotNull("Загруженный перевод 'Перевод на сбережения' не должен быть null", fetchedTransfer)
        assertEquals("Сумма загруженного перевода 'Перевод на сбережения' должна совпадать", transferAmount, fetchedTransfer?.balance!!, 0.001)
        assertEquals("ID счета-отправителя в загруженном переводе должен совпадать", account1.id, fetchedTransfer?.moneyAccFrom?.id)
        assertEquals("ID счета-получателя в загруженном переводе должен совпадать", account2.id, fetchedTransfer?.moneyAccTo?.id)
        assertEquals("Заметка в загруженном переводе должна быть 'Перевод на сбережения'", transferNote, fetchedTransfer?.note)

        val futureEntryCount = futureTransactionDao.loadCountFutureTransferTransaction(transferId)
        assertEquals("Для текущего перевода 'Перевод на сбережения' не должно быть записей о будущих переводах", 0, futureEntryCount)
    }

    @Test
    fun fetchTransactions_withoutDateRange() = runTest {
        val paymentNotePast = "ЗП часть 1"
        val transferNoteYesterday = "Мелкий перевод другу"
        val paymentNoteFuture = "Абонемент в зал (будущий)"

        val paymentDatePast = LocalDate.now().minusDays(2)
        val transferDateYesterday = LocalDate.now().minusDays(1)
        val paymentDateFuture = LocalDate.now().plusDays(3)

        val paymentIncomePastId = createPaymentUseCase(Payment(TransactionType.INCOME, 500.0, account1, incomeCategory, Title(paymentNotePast), paymentDatePast))
        val transferYesterdayId = createTransferUseCase(Transfer(TransactionType.REMITTANCE, 70.0, account1, account2, Title(transferNoteYesterday), transferDateYesterday))
        val paymentExpenseFutureId = createPaymentUseCase(Payment(TransactionType.EXPENSE, 120.0, account2, expenseCategory, Title(paymentNoteFuture), paymentDateFuture))

        // Проверка, что трансфер действительно создается и доступен
        val directLoadedTransfer = loadTransferByIdUseCase(transferYesterdayId)
        assertNotNull("Контрольная загрузка: Трансфер '$transferNoteYesterday' должен быть загружен напрямую", directLoadedTransfer)

        println("Ожидаемые ID: PaymentPast=$paymentIncomePastId, TransferYesterday=$transferYesterdayId, PaymentFuture=$paymentExpenseFutureId")

        val allTransactionsLiveData = fetchTransactionsUseCase(null, null)
        // Ожидаем, пока в LiveData не будет 3 элементов. Таймаут увеличен.
        val allTransactions = allTransactionsLiveData.getOrAwaitValue(time = 5, timeUnit = TimeUnit.SECONDS) { list ->
            (list as? List<*>)?.size == 3
        }

        println("Получено транзакций: ${allTransactions.size}")
        allTransactions.forEachIndexed { index, transaction ->
            println("Транзакция $index: id=${transaction.id}, type=${transaction.type.name}, note='${transaction.note}', date=${transaction.date}")
        }

        assertEquals(
            "Должны быть возвращены все 3 созданные транзакции. Найдено: ${allTransactions.size}",
            3,
            allTransactions.size
        )

        val pastPayment = allTransactions.find { it.id == paymentIncomePastId && it.type != TransactionType.REMITTANCE }
        assertNotNull("Прошлый доход '$paymentNotePast' (ID: $paymentIncomePastId) должен присутствовать в списке", pastPayment)
        assertTrue("Найденная транзакция (ID: $paymentIncomePastId) должна быть Payment. Фактический тип: ${pastPayment?.let { it::class.simpleName }}", pastPayment is Payment)
        assertEquals("Проверка заметки для прошлого дохода", paymentNotePast, pastPayment?.note)
        assertEquals("Проверка даты для прошлого дохода", paymentDatePast, (pastPayment as Payment).date)
        assertEquals("Проверка типа для прошлого дохода", TransactionType.INCOME, (pastPayment as Payment).type)

        val yesterdayTransfer = allTransactions.find { it.id == transferYesterdayId && it.type == TransactionType.REMITTANCE }
        assertNotNull("Вчерашний перевод '$transferNoteYesterday' (ID: $transferYesterdayId) должен присутствовать в списке", yesterdayTransfer)
        assertTrue("Найденная транзакция (ID: $transferYesterdayId) должна быть Transfer. Фактический тип: ${yesterdayTransfer?.let { it::class.simpleName }}", yesterdayTransfer is Transfer)
        assertEquals("Проверка заметки для вчерашнего перевода", transferNoteYesterday, yesterdayTransfer?.note)
        assertEquals("Проверка даты для вчерашнего перевода", transferDateYesterday, (yesterdayTransfer as Transfer).date)

        val futurePayment = allTransactions.find { it.id == paymentExpenseFutureId && it.type != TransactionType.REMITTANCE }
        assertNotNull("Будущий расход '$paymentNoteFuture' (ID: $paymentExpenseFutureId) должен присутствовать в списке", futurePayment)
        assertTrue("Найденная транзакция (ID: $paymentExpenseFutureId) должна быть Payment. Фактический тип: ${futurePayment?.let { it::class.simpleName }}", futurePayment is Payment)
        assertEquals("Проверка заметки для будущего расхода", paymentNoteFuture, futurePayment?.note)
        assertEquals("Проверка даты для будущего расхода", paymentDateFuture, (futurePayment as Payment).date)
        assertEquals("Проверка типа для будущего расхода", TransactionType.EXPENSE, (futurePayment as Payment).type)
    }

    @Test
    fun fetchTransactions_withDateRange() = runTest {
        val paymentNotePast = "ЗП часть 1 (вне диапазона)"
        val transferNoteInRange = "Мелкий перевод другу (в диапазоне)"
        val paymentNoteTodayInRange = "Обед сегодня (в диапазоне)"
        val paymentNoteFuture = "Абонемент в зал (будущий, вне диапазона)"

        val datePast = LocalDate.now().minusDays(3)
        val dateInRangeStart = LocalDate.now().minusDays(1)
        val dateInRangeEnd = LocalDate.now()
        val dateFuture = LocalDate.now().plusDays(3)

        createPaymentUseCase(Payment(TransactionType.INCOME, 500.0, account1, incomeCategory, Title(paymentNotePast), datePast))
        val transferInRangeId = createTransferUseCase(Transfer(TransactionType.REMITTANCE, 70.0, account1, account2, Title(transferNoteInRange), dateInRangeStart))
        val paymentTodayInRangeId = createPaymentUseCase(Payment(TransactionType.EXPENSE, 25.0, account1, expenseCategory, Title(paymentNoteTodayInRange), dateInRangeEnd))
        createPaymentUseCase(Payment(TransactionType.EXPENSE, 120.0, account2, expenseCategory, Title(paymentNoteFuture), dateFuture))

        val rangedTransactionsLiveData = fetchTransactionsUseCase(dateInRangeStart, dateInRangeEnd)
        // Ожидаем, пока в LiveData не будет 2 элементов. Таймаут увеличен.
        val rangedTransactions = rangedTransactionsLiveData.getOrAwaitValue(time = 5, timeUnit = TimeUnit.SECONDS) { list ->
            (list as? List<*>)?.size == 2
        }

        assertEquals(
            "В диапазоне (вчера-сегодня включительно) должно быть 2 транзакции. Найдено: ${rangedTransactions.size}",
            2,
            rangedTransactions.size
        )

        assertTrue(
            "Вчерашний перевод '$transferNoteInRange' должен быть в списке транзакций диапазона",
            rangedTransactions.any { it.id == transferInRangeId && it.note == transferNoteInRange && it is Transfer }
        )
        assertTrue(
            "Сегодняшний платеж '$paymentNoteTodayInRange' должен быть в списке транзакций диапазона",
            rangedTransactions.any { it.id == paymentTodayInRangeId && it.note == paymentNoteTodayInRange && it is Payment }
        )

        assertFalse(
            "Прошлый платеж '$paymentNotePast' не должен быть в списке транзакций диапазона",
            rangedTransactions.any { it.note == paymentNotePast }
        )
        assertFalse(
            "Будущий платеж '$paymentNoteFuture' не должен быть в списке транзакций диапазона",
            rangedTransactions.any { it.note == paymentNoteFuture }
        )
    }


    @Test
    fun updatePastIncomePaymentToFuture_revertsOriginalBalance() = runTest {
        val originalAmount = 150.0
        val pastDate = LocalDate.now().minusDays(2)
        val paymentId = createPaymentUseCase(Payment(TransactionType.INCOME, originalAmount, account1, incomeCategory, Title("Старый бонус (прошлый)"), pastDate))

        assertEquals(
            "Баланс 'Основного Кошелька' должен увеличиться после первоначального дохода 'Старый бонус'",
            initialBalanceAccount1 + originalAmount,
            loadMoneyAccountByIdUseCase(account1.id)?.balance!!,
            0.001
        )
        assertEquals(
            "Запись в FutureTransactionDao для прошлого платежа 'Старый бонус' должна отсутствовать",
            0,
            futureTransactionDao.loadCountFuturePaymentTransaction(paymentId)
        )

        val updatedAmount = 220.0
        val futureDate = LocalDate.now().plusDays(5)
        val updatedFutureIncomePayment = Payment(
            id = paymentId,
            type = TransactionType.INCOME,
            balance = updatedAmount,
            moneyAccount = account1,
            category = incomeCategory,
            _note = Title("Обновленный бонус (будущий)"),
            date = futureDate
        )

        updatePaymentUseCase(updatedFutureIncomePayment)

        assertEquals(
            "Баланс 'Основного Кошелька' должен вернуться к исходному после обновления платежа на будущий",
            initialBalanceAccount1,
            loadMoneyAccountByIdUseCase(account1.id)?.balance!!,
            0.001
        )
        assertTrue(
            "Запись в FutureTransactionDao для обновленного будущего платежа 'Обновленный бонус' должна существовать",
            futureTransactionDao.loadCountFuturePaymentTransaction(paymentId) > 0
        )

        val loadedPayment = loadPaymentByIdUseCase(paymentId)
        assertNotNull("Загруженный обновленный платеж 'Обновленный бонус' не должен быть null", loadedPayment)
        assertEquals("Сумма обновленного платежа должна быть " + updatedAmount, updatedAmount, loadedPayment?.balance!!, 0.001)
        assertEquals("Дата обновленного платежа должна стать будущей", futureDate, loadedPayment?.date)
        assertEquals("Заметка обновленного платежа должна быть 'Обновленный бонус (будущий)'", "Обновленный бонус (будущий)", loadedPayment?.note)
    }

    @Test
    fun deletePastExpensePayment_revertsAccountBalance() = runTest {
        val paymentAmount = 80.0
        val paymentId = createPaymentUseCase(Payment(TransactionType.EXPENSE, paymentAmount, account1, expenseCategory, Title("Случайный расход на удаление"), LocalDate.now()))

        assertEquals(
            "Баланс 'Основного Кошелька' должен уменьшиться после расхода 'Случайный расход'",
            initialBalanceAccount1 - paymentAmount,
            loadMoneyAccountByIdUseCase(account1.id)?.balance!!,
            0.001
        )

        val transactionToDelete = loadPaymentByIdUseCase(paymentId)!!
        deleteTransactionUseCase(transactionToDelete)

        assertEquals(
            "Баланс 'Основного Кошелька' должен вернуться к исходному после удаления расхода",
            initialBalanceAccount1,
            loadMoneyAccountByIdUseCase(account1.id)?.balance!!,
            0.001
        )
        assertNull("Удаленный платеж 'Случайный расход' не должен загружаться", loadPaymentByIdUseCase(paymentId))
    }

    @Test
    fun updatePastIncomeToPastExpense_updatesBalanceCorrectly_andChangesType() = runTest {
        val incomeAmount = 120.0
        val pastDate = LocalDate.now().minusDays(1)
        val paymentId = createPaymentUseCase(
            Payment(
                type = TransactionType.INCOME,
                balance = incomeAmount,
                moneyAccount = account1,
                category = incomeCategory,
                _note = Title("Первоначальный доход для изменения"),
                date = pastDate
            )
        )

        val accountAfterIncome = loadMoneyAccountByIdUseCase(account1.id)!!
        assertEquals(
            "Баланс после дохода должен быть initialBalance + incomeAmount",
            initialBalanceAccount1 + incomeAmount,
            accountAfterIncome.balance,
            0.001
        )

        val expenseAmount = 45.0
        val updatedExpensePayment = Payment(
            id = paymentId,
            type = TransactionType.EXPENSE,
            balance = expenseAmount,
            moneyAccount = account1,
            category = expenseCategory,
            _note = Title("Обновлено на расход с новой суммой"),
            date = pastDate
        )

        updatePaymentUseCase(updatedExpensePayment)

        val accountAfterUpdate = loadMoneyAccountByIdUseCase(account1.id)!!
        assertEquals(
            "Баланс после обновления на расход должен быть initialBalance - expenseAmount (старый доход отменен, новый расход применен)",
            initialBalanceAccount1 - expenseAmount,
            accountAfterUpdate.balance,
            0.001
        )

        val loadedPayment = loadPaymentByIdUseCase(paymentId)!!
        assertEquals("Тип платежа должен измениться на EXPENSE", TransactionType.EXPENSE, loadedPayment.type)
        assertEquals("Сумма платежа должна измениться на " + expenseAmount, expenseAmount, loadedPayment.balance, 0.001)
        assertEquals("Категория платежа должна измениться на 'Продукты'", expenseCategory.id, loadedPayment.category.id)
        assertEquals("Заметка платежа должна измениться", "Обновлено на расход с новой суммой", loadedPayment.note)
    }
}