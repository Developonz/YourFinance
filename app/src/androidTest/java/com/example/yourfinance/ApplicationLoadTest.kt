package com.example.yourfinance

import android.util.Log
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
import com.example.yourfinance.data.source.CategoryDao
import com.example.yourfinance.data.source.FinanceDataBase
import com.example.yourfinance.data.source.MoneyAccountDao
import com.example.yourfinance.data.source.TransactionDao
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.Transfer
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.ICategoryData
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.CategoryRepository
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import com.example.yourfinance.domain.repository.TransactionRepository
import com.example.yourfinance.domain.usecase.categories.general.CreateAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.category.FetchCategoriesUseCase
import com.example.yourfinance.domain.usecase.categories.category.LoadCategoryByIdUseCase
import com.example.yourfinance.domain.usecase.categories.subcategory.LoadSubcategoryByIdUseCase
import com.example.yourfinance.domain.usecase.categories.general.UpdateAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.general.DeleteAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.CreateMoneyAccountUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.FetchMoneyAccountsUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.LoadMoneyAccountByIdUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.SetMoneyAccountAsDefaultUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.UpdateMoneyAccountUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.DeleteMoneyAccountUseCase
import com.example.yourfinance.domain.usecase.transaction.CreatePaymentUseCase
import com.example.yourfinance.domain.usecase.transaction.CreateTransferUseCase
import com.example.yourfinance.domain.usecase.transaction.FetchTransactionsUseCase
import com.example.yourfinance.domain.usecase.transaction.LoadPaymentByIdUseCase
import com.example.yourfinance.domain.usecase.transaction.LoadTransferByIdUseCase
import com.example.yourfinance.domain.usecase.transaction.UpdatePaymentUseCase
import com.example.yourfinance.domain.usecase.transaction.UpdateTransferUseCase
import com.example.yourfinance.domain.usecase.transaction.DeleteTransactionUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.random.Random
import kotlin.system.measureTimeMillis
import com.example.yourfinance.data.mapper.toData
import java.math.BigDecimal
import kotlin.time.Duration.Companion.minutes

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@LargeTest
class ApplicationLoadTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FinanceDataBase
    private lateinit var moneyAccountDao: MoneyAccountDao
    private lateinit var categoryDao: CategoryDao
    private lateinit var transactionDao: TransactionDao

    private lateinit var moneyAccountRepository: MoneyAccountRepository
    private lateinit var categoryRepository: CategoryRepository
    private lateinit var transactionRepository: TransactionRepository

    private lateinit var createMoneyAccountUseCase: CreateMoneyAccountUseCase
    private lateinit var fetchMoneyAccountsUseCase: FetchMoneyAccountsUseCase
    private lateinit var loadMoneyAccountByIdUseCase: LoadMoneyAccountByIdUseCase
    private lateinit var updateMoneyAccountUseCase: UpdateMoneyAccountUseCase
    private lateinit var deleteMoneyAccountUseCase: DeleteMoneyAccountUseCase
    private lateinit var setMoneyAccountAsDefaultUseCase: SetMoneyAccountAsDefaultUseCase

    private lateinit var createCategoryUseCase: CreateAnyCategoryUseCase
    private lateinit var fetchCategoriesUseCase: FetchCategoriesUseCase
    private lateinit var loadCategoryByIdUseCase: LoadCategoryByIdUseCase
    private lateinit var loadSubcategoryByIdUseCase: LoadSubcategoryByIdUseCase
    private lateinit var updateCategoryUseCase: UpdateAnyCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteAnyCategoryUseCase

    private lateinit var createPaymentUseCase: CreatePaymentUseCase
    private lateinit var createTransferUseCase: CreateTransferUseCase
    private lateinit var fetchTransactionsUseCase: FetchTransactionsUseCase
    private lateinit var loadPaymentByIdUseCase: LoadPaymentByIdUseCase
    private lateinit var loadTransferByIdUseCase: LoadTransferByIdUseCase
    private lateinit var updatePaymentUseCase: UpdatePaymentUseCase
    private lateinit var updateTransferUseCase: UpdateTransferUseCase
    private lateinit var deleteTransactionUseCase: DeleteTransactionUseCase

    private val createdAccounts = mutableListOf<MoneyAccount>()
    private val createdCategories = mutableListOf<ICategoryData>()
    private val createdParentCategories = mutableListOf<Category>()
    private val createdPayments = mutableListOf<Payment>()
    private val createdTransfers = mutableListOf<Transfer>()

    companion object {
        const val NUM_ACCOUNTS = 8
        const val NUM_PARENT_CATEGORIES = 20
        const val NUM_SUBCATEGORIES_PER_PARENT = 10
        const val NUM_PAYMENTS = 13350
        const val NUM_TRANSFERS = 10040
        const val NUM_OPERATIONS_UNDER_LOAD = 50
        const val TAG = "LoadTest"
    }

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FinanceDataBase::class.java
        )
            .allowMainThreadQueries()
            .build()

        moneyAccountDao = database.getMoneyAccountDao()
        categoryDao = database.getCategoryDao()
        transactionDao = database.getTransactionDao()

        moneyAccountRepository = MoneyAccountRepositoryImpl(moneyAccountDao)
        categoryRepository = CategoryRepositoryImpl(categoryDao)
        transactionRepository = TransactionRepositoryImpl(transactionDao)

        createMoneyAccountUseCase = CreateMoneyAccountUseCase(moneyAccountRepository)
        fetchMoneyAccountsUseCase = FetchMoneyAccountsUseCase(moneyAccountRepository)
        loadMoneyAccountByIdUseCase = LoadMoneyAccountByIdUseCase(moneyAccountRepository)
        updateMoneyAccountUseCase = UpdateMoneyAccountUseCase(moneyAccountRepository)
        deleteMoneyAccountUseCase = DeleteMoneyAccountUseCase(moneyAccountRepository)
        setMoneyAccountAsDefaultUseCase = SetMoneyAccountAsDefaultUseCase(moneyAccountRepository)

        createCategoryUseCase = CreateAnyCategoryUseCase(categoryRepository)
        fetchCategoriesUseCase = FetchCategoriesUseCase(categoryRepository)
        loadCategoryByIdUseCase = LoadCategoryByIdUseCase(categoryRepository)
        loadSubcategoryByIdUseCase = LoadSubcategoryByIdUseCase(categoryRepository)
        updateCategoryUseCase = UpdateAnyCategoryUseCase(categoryRepository)
        deleteCategoryUseCase = DeleteAnyCategoryUseCase(categoryRepository)

        createPaymentUseCase = CreatePaymentUseCase(transactionRepository)
        createTransferUseCase = CreateTransferUseCase(transactionRepository)
        fetchTransactionsUseCase = FetchTransactionsUseCase(transactionRepository)
        loadPaymentByIdUseCase = LoadPaymentByIdUseCase(transactionRepository)
        loadTransferByIdUseCase = LoadTransferByIdUseCase(transactionRepository)
        updatePaymentUseCase = UpdatePaymentUseCase(transactionRepository)
        updateTransferUseCase = UpdateTransferUseCase(transactionRepository)
        deleteTransactionUseCase = DeleteTransactionUseCase(transactionRepository)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Throws(InterruptedException::class, TimeoutException::class)
    private fun <T> LiveData<T>.getOrAwaitValue(
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
        if (condition(this.value)) {
            this.removeObserver(observer)
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
    fun runFullLoadTest() = runTest(timeout = 15.minutes) {
        Log.d(TAG, "Starting full load test...")

        val dataGenTime = measureTimeMillis {
            generateAccounts()
            generateCategories()
            generateTransactions()
        }
        Log.d(TAG, "Data generation took $dataGenTime ms")
        Log.d(TAG, "Generated: ${createdAccounts.size} accounts, ${createdCategories.size} categories (${createdParentCategories.size} parents), ${createdPayments.size} payments, ${createdTransfers.size} transfers.")

        assertTrue("Should generate accounts", createdAccounts.isNotEmpty())
        assertTrue("Should generate categories", createdCategories.isNotEmpty())
        assertTrue("Should generate transactions", createdPayments.isNotEmpty() || createdTransfers.isNotEmpty())

        performOperationsUnderLoad()

        Log.d(TAG, "Load test finished.")
    }

    private suspend fun generateAccounts() {
        Log.d(TAG, "Generating $NUM_ACCOUNTS accounts...")
        for (i in 1..NUM_ACCOUNTS) {
            val balance = Random.nextDouble(100.0, 10000.0)
            val account = MoneyAccount(
                _title = Title("Account $i"),
                startBalance = BigDecimal(balance),
                balance = BigDecimal(balance),
                dateCreation = LocalDate.now().minusDays(Random.nextLong(0, 365))
            )
            val id = createMoneyAccountUseCase(account)
            val loadedAccount = loadMoneyAccountByIdUseCase(id)
            assertNotNull(loadedAccount)
            createdAccounts.add(loadedAccount!!)
        }
    }

    private suspend fun generateCategories() {
        Log.d(TAG, "Generating categories...")
        for (i in 1..NUM_PARENT_CATEGORIES) {
            val type = if (Random.nextBoolean()) CategoryType.INCOME else CategoryType.EXPENSE
            val parentCategory = Category(
                title = Title("Parent Category $i"),
                categoryType = type,
                iconResourceId = "ic_cat_$i",
                colorHex = Random.nextInt()
            )
            // Assuming CreateAnyCategoryUseCase is modified to return ID,
            // or we fetch it. For simplicity, using DAO directly to get ID for now.
            val parentId = categoryDao.insertCategory(parentCategory.toDataEntity()) // Direct DAO for ID
            val loadedParent = loadCategoryByIdUseCase(parentId)
            assertNotNull(loadedParent)
            createdCategories.add(loadedParent!!)
            createdParentCategories.add(loadedParent)


            for (j in 1..NUM_SUBCATEGORIES_PER_PARENT) {
                val subcategory = Subcategory(
                    title = Title("Subcategory $i-$j"),
                    categoryType = type,
                    parentId = parentId,
                    iconResourceId = loadedParent.iconResourceId,
                    colorHex = loadedParent.colorHex
                )
                val subId = categoryDao.insertCategory(subcategory.toDataEntity()) // Direct DAO for ID
                val loadedSub = loadSubcategoryByIdUseCase(subId)
                assertNotNull(loadedSub)
                createdCategories.add(loadedSub!!)
            }
        }
    }
    private fun Category.toDataEntity() = com.example.yourfinance.data.model.CategoryEntity(this.title, this.categoryType, this.id, null, this.iconResourceId, this.colorHex)
    private fun Subcategory.toDataEntity() = com.example.yourfinance.data.model.CategoryEntity(this.title, this.categoryType, this.id, this.parentId, this.iconResourceId, this.colorHex)


    private suspend fun generateTransactions() {
        Log.d(TAG, "Generating transactions...")
        val startDate = LocalDate.now().minusYears(1)
        val endDate = LocalDate.now().plusMonths(6)

        for (i in 1..NUM_PAYMENTS) {
            val account = createdAccounts.random()
            val category = createdCategories.random()
            val type = if (category.categoryType == CategoryType.INCOME) TransactionType.INCOME else TransactionType.EXPENSE
            val amount = Random.nextDouble(5.0, 500.0)
            val date = LocalDate.ofEpochDay(Random.nextLong(startDate.toEpochDay(), endDate.toEpochDay()))

            val payment = Payment(
                type = type,
                balance = BigDecimal(amount),
                moneyAccount = account,
                category = category,
                _note = Title("Payment $i on $date"),
                date = date
            )
            // Assuming CreatePaymentUseCase is modified to return ID
            val paymentId = transactionDao.insertPaymentTransaction(payment.toData()) // Direct DAO for ID
            val loadedPayment = loadPaymentByIdUseCase(paymentId)
            assertNotNull(loadedPayment)
            createdPayments.add(loadedPayment!!)
        }

        if (createdAccounts.size < 2) {
            Log.w(TAG, "Not enough accounts to create transfers. Skipping.")
            return
        }

        for (i in 1..NUM_TRANSFERS) {
            val accFrom = createdAccounts.random()
            var accTo = createdAccounts.random()
            while (accTo.id == accFrom.id) {
                accTo = createdAccounts.random()
            }
            val amount = Random.nextDouble(10.0, 1000.0)
            val date = LocalDate.ofEpochDay(Random.nextLong(startDate.toEpochDay(), endDate.toEpochDay()))

            val transfer = Transfer(
                balance = BigDecimal(amount),
                moneyAccFrom = accFrom,
                moneyAccTo = accTo,
                _note = Title("Transfer $i on $date"),
                date = date
            )
            // Assuming CreateTransferUseCase is modified to return ID
            val transferId = transactionDao.insertTransferTransaction(transfer.toData()) // Direct DAO for ID
            val loadedTransfer = loadTransferByIdUseCase(transferId)
            assertNotNull(loadedTransfer)
            createdTransfers.add(loadedTransfer!!)
        }
    }

    private suspend fun performOperationsUnderLoad() {
        Log.d(TAG, "Performing operations under load...")

        val fetchAllTime = measureTimeMillis {
            val allTransactions = fetchTransactionsUseCase(null, null).getOrAwaitValue(time = 10) { (it?.size ?: 0) >= (createdPayments.size + createdTransfers.size) }
            Log.d(TAG, "Fetched ${allTransactions.size} total transactions.")
            assertTrue(allTransactions.size >= createdPayments.size + createdTransfers.size)
        }
        Log.d(TAG, "Fetch all transactions took $fetchAllTime ms.")

        val fetchRangedTime = measureTimeMillis {
            val rangeStart = LocalDate.now().minusMonths(3)
            val rangeEnd = LocalDate.now().plusMonths(1)
            val rangedTransactions = fetchTransactionsUseCase(rangeStart, rangeEnd).getOrAwaitValue(time = 5)
            Log.d(TAG, "Fetched ${rangedTransactions.size} ranged transactions.")
        }
        Log.d(TAG, "Fetch ranged transactions took $fetchRangedTime ms.")

        if (createdPayments.isNotEmpty()) {
            var totalUpdateTime = 0L
            repeat(NUM_OPERATIONS_UNDER_LOAD.coerceAtMost(createdPayments.size)) {
                val paymentToUpdate = createdPayments.random()
                val newAmount = Random.nextDouble(1.0, 600.0)
                val updatedPayment = paymentToUpdate.copy(balance = BigDecimal(newAmount), _note = Title("Updated ${paymentToUpdate.note}"))
                val time = measureTimeMillis { updatePaymentUseCase(updatedPayment) }
                totalUpdateTime += time
                val reloaded = loadPaymentByIdUseCase(paymentToUpdate.id)
                assertNotNull(reloaded)
                assertEquals(newAmount, reloaded!!.balance.toDouble(), 0.01)
            }
            Log.d(TAG, "Updating $NUM_OPERATIONS_UNDER_LOAD payments took avg ${totalUpdateTime / NUM_OPERATIONS_UNDER_LOAD.coerceAtMost(createdPayments.size)} ms.")
        }

        if (createdTransfers.isNotEmpty()) {
            var totalUpdateTime = 0L
            repeat(NUM_OPERATIONS_UNDER_LOAD.coerceAtMost(createdTransfers.size)) {
                val transferToUpdate = createdTransfers.random()
                val newAmount = Random.nextDouble(1.0, 1200.0)
                val updatedTransfer = transferToUpdate.copy(balance = BigDecimal(newAmount), _note = Title("Updated ${transferToUpdate.note}"))
                val time = measureTimeMillis { updateTransferUseCase(updatedTransfer) }
                totalUpdateTime += time
                val reloaded = loadTransferByIdUseCase(transferToUpdate.id)
                assertNotNull(reloaded)
                assertEquals(newAmount, reloaded!!.balance.toDouble(), 0.01)
            }
            Log.d(TAG, "Updating $NUM_OPERATIONS_UNDER_LOAD transfers took avg ${totalUpdateTime / NUM_OPERATIONS_UNDER_LOAD.coerceAtMost(createdTransfers.size)} ms.")
        }

        var totalDeleteTime = 0L
        val transactionsToDelete = (createdPayments.take(NUM_OPERATIONS_UNDER_LOAD / 2) + createdTransfers.take(NUM_OPERATIONS_UNDER_LOAD / 2)).shuffled()
        if (transactionsToDelete.isNotEmpty()) {
            transactionsToDelete.forEach { transaction ->
                val time = measureTimeMillis { deleteTransactionUseCase(transaction) }
                totalDeleteTime += time
                if (transaction is Payment) assertNull(loadPaymentByIdUseCase(transaction.id))
                if (transaction is Transfer) assertNull(loadTransferByIdUseCase(transaction.id))
            }
            Log.d(TAG, "Deleting ${transactionsToDelete.size} transactions took avg ${totalDeleteTime / transactionsToDelete.size} ms.")
        }

        if (createdAccounts.isNotEmpty()) {
            var totalSetDefaultTime = 0L
            repeat(NUM_OPERATIONS_UNDER_LOAD.coerceAtMost(createdAccounts.size)) {
                val accountToSetDefault = createdAccounts.random()
                val time = measureTimeMillis { setMoneyAccountAsDefaultUseCase(accountToSetDefault.id) }
                totalSetDefaultTime += time
                val reloaded = loadMoneyAccountByIdUseCase(accountToSetDefault.id)
                assertNotNull(reloaded)
                assertTrue(reloaded!!.default)
            }
            Log.d(TAG, "Setting default account $NUM_OPERATIONS_UNDER_LOAD times took avg ${totalSetDefaultTime / NUM_OPERATIONS_UNDER_LOAD.coerceAtMost(createdAccounts.size)} ms.")
        }
    }
}