//package com.example.yourfinance.integration
// 1
//import androidx.arch.core.executor.testing.InstantTaskExecutorRule
//import androidx.lifecycle.LiveData
//import androidx.lifecycle.Observer
//import androidx.room.Room
//import androidx.test.core.app.ApplicationProvider
//import androidx.test.ext.junit.runners.AndroidJUnit4
//import androidx.test.filters.LargeTest
//import com.example.yourfinance.data.repository.CategoryRepositoryImpl
//import com.example.yourfinance.data.repository.MoneyAccountRepositoryImpl
//import com.example.yourfinance.data.repository.TransactionRepositoryImpl
//import com.example.yourfinance.data.source.*
//import com.example.yourfinance.domain.model.CategoryType
//import com.example.yourfinance.domain.model.Title
//import com.example.yourfinance.domain.model.TransactionType
//import com.example.yourfinance.domain.model.entity.MoneyAccount
//import com.example.yourfinance.domain.model.entity.Payment
//import com.example.yourfinance.domain.model.entity.Transfer
//import com.example.yourfinance.domain.model.entity.category.BaseCategory
//import com.example.yourfinance.domain.model.entity.category.Category
//import com.example.yourfinance.domain.repository.CategoryRepository
//import com.example.yourfinance.domain.repository.MoneyAccountRepository
//import com.example.yourfinance.domain.repository.TransactionRepository
//import com.example.yourfinance.domain.usecase.categories.general.CreateAnyCategoryUseCase
//import com.example.yourfinance.domain.usecase.moneyaccount.CreateMoneyAccountUseCase
//import com.example.yourfinance.domain.usecase.moneyaccount.LoadMoneyAccountByIdUseCase
//import com.example.yourfinance.domain.usecase.transaction.*
//import kotlinx.coroutines.ExperimentalCoroutinesApi
//import kotlinx.coroutines.test.runTest
//import org.junit.After
//import org.junit.Before
//import org.junit.Rule
//import org.junit.Test
//import org.junit.runner.RunWith
//import java.time.LocalDate
//import java.util.concurrent.CountDownLatch
//import java.util.concurrent.TimeUnit
//import java.util.concurrent.TimeoutException
//import org.junit.Assert.*
//
//@ExperimentalCoroutinesApi
//@RunWith(AndroidJUnit4::class)
//@LargeTest
//class TransactionIntegrationTest {
//
//    @get:Rule(order = 0)
//    var instantTaskExecutorRule = InstantTaskExecutorRule()
//
//    private lateinit var db: FinanceDataBase
//    private lateinit var accountDao: MoneyAccountDao
//    private lateinit var categoryDao: CategoryDao
//    private lateinit var transactionDao: TransactionDao
//
//    private lateinit var accountRepo: MoneyAccountRepository
//    private lateinit var categoryRepo: CategoryRepository
//    private lateinit var transactionRepo: TransactionRepository
//
//    private lateinit var createAccountUseCase: CreateMoneyAccountUseCase
//    private lateinit var createCategoryUseCase: CreateAnyCategoryUseCase
//    private lateinit var loadAccountUseCase: LoadMoneyAccountByIdUseCase
//
//    private lateinit var createPaymentUseCase: CreatePaymentUseCase
//    private lateinit var createTransferUseCase: CreateTransferUseCase
//    private lateinit var fetchTransactionsUseCase: FetchTransactionsUseCase
//    private lateinit var loadPaymentUseCase: LoadPaymentByIdUseCase
//    private lateinit var loadTransferUseCase: LoadTransferByIdUseCase
//    private lateinit var updatePaymentUseCase: UpdatePaymentUseCase
//    private lateinit var updateTransferUseCase: UpdateTransferUseCase
//    private lateinit var deleteTransactionUseCase: DeleteTransactionUseCase
//
//    private lateinit var acc1: MoneyAccount
//    private lateinit var acc2: MoneyAccount
//    private lateinit var catIncome: BaseCategory
//    private lateinit var catExpense: BaseCategory
//
//    private val startBal1 = 1000.0
//    private val startBal2 = 500.0
//
//    @Before
//    fun setup() = runTest {
//        db = Room.inMemoryDatabaseBuilder(
//            ApplicationProvider.getApplicationContext(),
//            FinanceDataBase::class.java
//        ).allowMainThreadQueries().build()
//
//        accountDao = db.getMoneyAccountDao()
//        categoryDao = db.getCategoryDao()
//        transactionDao = db.getTransactionDao()
//
//        accountRepo = MoneyAccountRepositoryImpl(accountDao)
//        categoryRepo = CategoryRepositoryImpl(categoryDao)
//        transactionRepo = TransactionRepositoryImpl(transactionDao)
//
//        createAccountUseCase = CreateMoneyAccountUseCase(accountRepo)
//        createCategoryUseCase = CreateAnyCategoryUseCase(categoryRepo)
//        loadAccountUseCase = LoadMoneyAccountByIdUseCase(accountRepo)
//
//        createPaymentUseCase = CreatePaymentUseCase(transactionRepo)
//        createTransferUseCase = CreateTransferUseCase(transactionRepo)
//        fetchTransactionsUseCase = FetchTransactionsUseCase(transactionRepo)
//        loadPaymentUseCase = LoadPaymentByIdUseCase(transactionRepo)
//        loadTransferUseCase = LoadTransferByIdUseCase(transactionRepo)
//        updatePaymentUseCase = UpdatePaymentUseCase(transactionRepo)
//        updateTransferUseCase = UpdateTransferUseCase(transactionRepo)
//        deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepo)
//
//        // создаём два счёта
//        val id1 = createAccountUseCase(MoneyAccount(Title("A1"), startBal1, startBal1))
//        val id2 = createAccountUseCase(MoneyAccount(Title("A2"), startBal2, startBal2))
//        acc1 = loadAccountUseCase(id1)!!
//        acc2 = loadAccountUseCase(id2)!!
//
//        // создаём категории
//        val incId = createCategoryUseCase(Category(title = Title("Inc"), categoryType = CategoryType.INCOME, iconResourceId = "ic_food", colorHex = 0x000))
//        val expId = createCategoryUseCase(Category(title = Title("Exp"), categoryType = CategoryType.INCOME, iconResourceId = "ic_food", colorHex = 0x222))
//        catIncome = BaseCategory(_title = Title("Inc"), categoryType = CategoryType.INCOME, id = incId, iconResourceId = "ic_food", colorHex = 0x000)
//        catExpense = BaseCategory(_title = Title("Exp"), categoryType = CategoryType.EXPENSE, id = expId, iconResourceId = "ic_food", colorHex = 0x222)
//    }
//
//    @After
//    fun tearDown() { db.close() }
//
//    // helper для LiveData
//    @Throws(InterruptedException::class, TimeoutException::class)
//    private fun <T> LiveData<T>.getOrAwait(
//        time: Long = 2, unit: TimeUnit = TimeUnit.SECONDS,
//        condition: (T?) -> Boolean = { it != null }
//    ): T {
//        var data: T? = null
//        val latch = CountDownLatch(1)
//        val obs = object : Observer<T> {
//            override fun onChanged(v: T) {
//                if (condition(v)) {
//                    data = v; latch.countDown(); this@getOrAwait.removeObserver(this)
//                }
//            }
//        }
//        if (condition(this.value)) return this.value as T
//        this.observeForever(obs)
//        if (!latch.await(time, unit))
//            throw TimeoutException("No data or condition failed: ${this.value}")
//        return data as T
//    }
//
//    // --- Create Operations ---
//
//    @Test
//    fun createIncomePayment_now_changesBalance_andIsDoneTrue() = runTest {
//        val p = Payment(TransactionType.INCOME, 200.0, acc1, catIncome, Title("I"), LocalDate.now())
//        val pid = createPaymentUseCase(p)
//        val updated = loadAccountUseCase(acc1.id)!!
//        assertEquals(startBal1 + 200.0, updated.balance, 1e-3)
//        assertTrue(loadPaymentUseCase(pid)!!.is_done)
//    }
//
//    @Test
//    fun createExpensePayment_now_changesBalance_andIsDoneTrue() = runTest {
//        val p = Payment(TransactionType.EXPENSE, 50.0, acc1, catExpense, Title("E"), LocalDate.now())
//        val pid = createPaymentUseCase(p)
//        val updated = loadAccountUseCase(acc1.id)!!
//        assertEquals(startBal1 - 50.0, updated.balance, 1e-3)
//        assertTrue(loadPaymentUseCase(pid)!!.is_done)
//    }
//
//    @Test
//    fun createIncomePayment_future_noBalanceChange_andIsDoneFalse() = runTest {
//        val date = LocalDate.now().plusDays(5)
//        val p = Payment(TransactionType.INCOME, 300.0, acc1, catIncome, Title("F"), date)
//        val pid = createPaymentUseCase(p)
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertFalse(loadPaymentUseCase(pid)!!.is_done)
//    }
//
//    @Test
//    fun createTransfer_now_changesBothBalances_andIsDoneTrue() = runTest {
//        val t = Transfer(80.0, acc1, acc2, Title("T"), LocalDate.now())
//        val tid = createTransferUseCase(t)
//        assertEquals(startBal1 - 80.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2 + 80.0, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//        assertTrue(loadTransferUseCase(tid)!!.is_done)
//    }
//
//    @Test
//    fun createTransfer_future_noBalanceChange_andIsDoneFalse() = runTest {
//        val date = LocalDate.now().plusDays(3)
//        val t = Transfer( 60.0, acc1, acc2, Title("TF"), date)
//        val tid = createTransferUseCase(t)
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//        assertFalse(loadTransferUseCase(tid)!!.is_done)
//    }
//
//    // --- Read Operations ---
//
//    @Test
//    fun fetchTransactions_withoutDateRange_returnsAllTransactions() = runTest {
//        val paymentNotePast = "ЗП часть 1"
//        val transferNoteYesterday = "Перевод другу"
//        val paymentNoteFuture = "Абонемент в зал (будущий)"
//
//        val paymentPastId = createPaymentUseCase(Payment(TransactionType.INCOME, 500.0, acc1, catIncome, Title(paymentNotePast), LocalDate.now().minusDays(2)))
//        val transferYesterdayId = createTransferUseCase(Transfer(70.0, acc1, acc2, Title(transferNoteYesterday), LocalDate.now().minusDays(1)))
//        val paymentFutureId = createPaymentUseCase(Payment(TransactionType.EXPENSE, 120.0, acc2, catExpense, Title(paymentNoteFuture), LocalDate.now().plusDays(3)))
//
//        val allTransactions = fetchTransactionsUseCase(null, null).getOrAwait(time = 5) { it?.size == 3 }
//
//        assertEquals("Должны быть возвращены все 3 созданные транзакции", 3, allTransactions.size)
//
//        assertTrue(
//            "Прошлый доход '$paymentNotePast' должен присутствовать в списке",
//            allTransactions.any { it.id == paymentPastId && it is Payment && it.note == paymentNotePast }
//        )
//        assertTrue(
//            "Вчерашний перевод '$transferNoteYesterday' должен присутствовать в списке",
//            allTransactions.any { it.id == transferYesterdayId && it is Transfer && it.note == transferNoteYesterday }
//        )
//        assertTrue(
//            "Будущий расход '$paymentNoteFuture' должен присутствовать в списке",
//            allTransactions.any { it.id == paymentFutureId && it is Payment && it.note == paymentNoteFuture }
//        )
//    }
//
//    @Test
//    fun fetchTransactions_withDateRange_returnsOnlyTransactionsWithinRange() = runTest {
//        val datePast = LocalDate.now().minusDays(3)
//        val dateInRangeStart = LocalDate.now().minusDays(1)
//        val dateInRangeEnd = LocalDate.now()
//        val dateFuture = LocalDate.now().plusDays(3)
//
//        val noteOutOfRangePast = "ЗП часть 1 (вне диапазона)"
//        val noteInRangeTransfer = "Перевод другу (в диапазоне)"
//        val noteInRangePayment = "Обед сегодня (в диапазоне)"
//        val noteOutOfRangeFuture = "Абонемент в зал (будущий, вне диапазона)"
//
//        // Транзакции вне диапазона
//        createPaymentUseCase(Payment(TransactionType.INCOME, 500.0, acc1, catIncome, Title(noteOutOfRangePast), datePast))
//        createPaymentUseCase(Payment(TransactionType.EXPENSE, 120.0, acc2, catExpense, Title(noteOutOfRangeFuture), dateFuture))
//
//        // Транзакции в диапазоне
//        val transferInRangeId = createTransferUseCase(Transfer( 70.0, acc1, acc2, Title(noteInRangeTransfer), dateInRangeStart))
//        val paymentInRangeId = createPaymentUseCase(Payment(TransactionType.EXPENSE, 25.0, acc1, catExpense, Title(noteInRangePayment), dateInRangeEnd))
//
//        val rangedTransactions = fetchTransactionsUseCase(dateInRangeStart, dateInRangeEnd).getOrAwait(time = 5) { it?.size == 2 }
//
//        assertEquals("В диапазоне должны быть ровно 2 транзакции", 2, rangedTransactions.size)
//
//        assertTrue(
//            "Перевод '$noteInRangeTransfer' должен быть в списке",
//            rangedTransactions.any { it.id == transferInRangeId }
//        )
//        assertTrue(
//            "Платеж '$noteInRangePayment' должен быть в списке",
//            rangedTransactions.any { it.id == paymentInRangeId }
//        )
//    }
//
//    // --- Update Operations ---
//
//    @Test
//    fun updatePastPayment_toFuture_revertsBalance_andIsDoneFalse() = runTest {
//        val pid = createPaymentUseCase(Payment(TransactionType.INCOME, 100.0, acc1, catIncome, Title("UP"), LocalDate.now().minusDays(2)))
//        assertTrue(loadPaymentUseCase(pid)!!.is_done)
//        assertEquals(startBal1 + 100.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//
//        updatePaymentUseCase(Payment(id = pid, type = TransactionType.INCOME, balance = 100.0,
//            moneyAccount = acc1, category = catIncome, _note = Title("UP"), date = LocalDate.now().plusDays(2)))
//
//        assertFalse(loadPaymentUseCase(pid)!!.is_done)
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//    }
//
//    @Test
//    fun updateFuturePayment_toPast_appliesBalance_andIsDoneTrue() = runTest {
//        val dateF = LocalDate.now().plusDays(4)
//        val pid = createPaymentUseCase(Payment(TransactionType.EXPENSE, 40.0, acc1, catExpense, Title("UF"), dateF))
//        assertFalse(loadPaymentUseCase(pid)!!.is_done)
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//
//        updatePaymentUseCase(Payment(id = pid, type = TransactionType.EXPENSE, balance = 40.0,
//            moneyAccount = acc1, category = catExpense, _note = Title("UF"), date = LocalDate.now().minusDays(1)))
//
//        assertTrue(loadPaymentUseCase(pid)!!.is_done)
//        assertEquals(startBal1 - 40.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//    }
//
//    @Test fun updatePayment_changeTypeAndAmount_recalculatesBalance() = runTest {
//        val pid = createPaymentUseCase(Payment(TransactionType.INCOME, 80.0, acc1, catIncome, Title("CT"), LocalDate.now()))
//        assertEquals(startBal1 + 80.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//
//        updatePaymentUseCase(Payment(id = pid, type = TransactionType.EXPENSE, balance = 30.0,
//            moneyAccount = acc1, category = catExpense, _note = Title("CT"), date = LocalDate.now()))
//        assertEquals(startBal1 - 30.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//    }
//
//    @Test
//    fun updatePayment_changeAccount_revertsOldAndAppliesToNewAccount() = runTest {
//        val pid = createPaymentUseCase(Payment(TransactionType.EXPENSE, 40.0, acc1, catExpense, Title("Move"), LocalDate.now()))
//        assertEquals(startBal1 - 40.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//
//        // Обновляем тот же платёж, но переводим его на acc2
//        updatePaymentUseCase(Payment(TransactionType.EXPENSE, 40.0, acc2, catExpense, Title("Move"), LocalDate.now(), id = pid))
//
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2 - 40.0, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//
//        assertTrue(loadPaymentUseCase(pid)!!.is_done)
//    }
//
//
//    @Test fun updateTransfer_changeAmountAndAccounts_appliesCorrectly() = runTest {
//        val tid = createTransferUseCase(Transfer( 50.0, acc1, acc2, Title("UT"), LocalDate.now()))
//
//        assertEquals(startBal1 - 50.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2 + 50.0, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//
//        updateTransferUseCase(Transfer(id = tid,  balance = 20.0,
//            moneyAccFrom = acc2, moneyAccTo = acc1, _note = Title("UT"), date = LocalDate.now()))
//
//        assertEquals(startBal1 + 20.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2 - 20.0, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//    }
//
//    @Test fun updateTransfer_pastToFuture_revertsBalance_andIsDoneFalse() = runTest {
//        val tid = createTransferUseCase(Transfer( 30.0, acc1, acc2, Title("TF"), LocalDate.now()))
//        assertTrue(loadTransferUseCase(tid)!!.is_done)
//        updateTransferUseCase(Transfer(id = tid,  balance = 30.0,
//            moneyAccFrom = acc1, moneyAccTo = acc2, _note = Title("TF"),
//            date = LocalDate.now().plusDays(2)))
//        assertFalse(loadTransferUseCase(tid)!!.is_done)
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//    }
//
//    @Test fun updateFutureTransfer_toPast_appliesBalance_andIsDoneTrue() = runTest {
//        val dateF = LocalDate.now().plusDays(3)
//        val tid = createTransferUseCase(Transfer( 25.0, acc1, acc2, Title("FP"), dateF))
//        assertFalse(loadTransferUseCase(tid)!!.is_done)
//
//        updateTransferUseCase(Transfer(id = tid, balance = 25.0,
//            moneyAccFrom = acc1, moneyAccTo = acc2, _note = Title("FP"), date = LocalDate.now()))
//        assertTrue(loadTransferUseCase(tid)!!.is_done)
//        assertEquals(startBal1 - 25.0, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2 + 25.0, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//    }
//
//    // --- Delete Operations ---
//
//    @Test fun deletePastPayment_revertsBalance_andIsDoneFalseAfterLoad() = runTest {
//        val pid = createPaymentUseCase(Payment(TransactionType.EXPENSE, 15.0, acc1, catExpense, Title("DP"), LocalDate.now()))
//        assertTrue(loadPaymentUseCase(pid)!!.is_done)
//        deleteTransactionUseCase(loadPaymentUseCase(pid)!!)
//        assertNull(loadPaymentUseCase(pid))
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//    }
//
//    @Test fun deletePastTransfer_revertsBothBalances_andIsDoneFalseAfterLoad() = runTest {
//        val tid = createTransferUseCase(Transfer( 45.0, acc1, acc2, Title("DT"), LocalDate.now()))
//        assertTrue(loadTransferUseCase(tid)!!.is_done)
//        deleteTransactionUseCase(loadTransferUseCase(tid)!!)
//        assertNull(loadTransferUseCase(tid))
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//    }
//
//    @Test
//    fun deleteFuturePayment_noBalanceChange_andIsDeleted() = runTest {
//        val futureDate = LocalDate.now().plusDays(10)
//        val pid = createPaymentUseCase(Payment(TransactionType.EXPENSE, 100.0, acc1, catExpense, Title("Future Delete"), futureDate))
//
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        val paymentToDelete = loadPaymentUseCase(pid)!!
//        assertFalse(paymentToDelete.is_done)
//
//        deleteTransactionUseCase(paymentToDelete)
//
//        assertNull(loadPaymentUseCase(pid))
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//    }
//
//    @Test
//    fun deleteFutureTransfer_noBalanceChange_andIsDeleted() = runTest {
//        val futureDate = LocalDate.now().plusDays(5)
//        val tid = createTransferUseCase(Transfer( 50.0, acc1, acc2, Title("Future Transfer Delete"), futureDate))
//
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//        val transferToDelete = loadTransferUseCase(tid)!!
//        assertFalse(transferToDelete.is_done)
//
//        deleteTransactionUseCase(transferToDelete)
//
//        assertNull(loadTransferUseCase(tid))
//        assertEquals(startBal1, loadAccountUseCase(acc1.id)!!.balance, 1e-3)
//        assertEquals(startBal2, loadAccountUseCase(acc2.id)!!.balance, 1e-3)
//    }
//}
