package com.example.yourfinance.integration

//qweqwq1
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.yourfinance.data.repository.MoneyAccountRepositoryImpl
import com.example.yourfinance.data.source.FinanceDataBase
import com.example.yourfinance.data.source.MoneyAccountDao
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.repository.MoneyAccountRepository
import com.example.yourfinance.domain.usecase.moneyaccount.CreateMoneyAccountUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.DeleteMoneyAccountUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.FetchMoneyAccountsUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.LoadMoneyAccountByIdUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.SetMoneyAccountAsDefaultUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.UpdateMoneyAccountUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.math.BigDecimal // Убедись, что этот импорт есть
import java.time.LocalDate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class MoneyAccountIntegrationTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FinanceDataBase
    private lateinit var moneyAccountDao: MoneyAccountDao
    private lateinit var moneyAccountRepository: MoneyAccountRepository

    private lateinit var createMoneyAccountUseCase: CreateMoneyAccountUseCase
    private lateinit var fetchMoneyAccountsUseCase: FetchMoneyAccountsUseCase
    private lateinit var loadMoneyAccountByIdUseCase: LoadMoneyAccountByIdUseCase
    private lateinit var updateMoneyAccountUseCase: UpdateMoneyAccountUseCase
    private lateinit var deleteMoneyAccountUseCase: DeleteMoneyAccountUseCase
    private lateinit var setMoneyAccountAsDefaultUseCase: SetMoneyAccountAsDefaultUseCase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FinanceDataBase::class.java
        )
            .allowMainThreadQueries()
            .build()

        moneyAccountDao = database.getMoneyAccountDao()
        moneyAccountRepository = MoneyAccountRepositoryImpl(moneyAccountDao)

        createMoneyAccountUseCase = CreateMoneyAccountUseCase(moneyAccountRepository)
        fetchMoneyAccountsUseCase = FetchMoneyAccountsUseCase(moneyAccountRepository)
        loadMoneyAccountByIdUseCase = LoadMoneyAccountByIdUseCase(moneyAccountRepository)
        updateMoneyAccountUseCase = UpdateMoneyAccountUseCase(moneyAccountRepository)
        deleteMoneyAccountUseCase = DeleteMoneyAccountUseCase(moneyAccountRepository)
        setMoneyAccountAsDefaultUseCase = SetMoneyAccountAsDefaultUseCase(moneyAccountRepository)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    // helper для LiveData
    @Throws(InterruptedException::class, TimeoutException::class)
    private fun <T> LiveData<T>.getOrAwait(
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
                    this@getOrAwait.removeObserver(this)
                }
            }
        }
        if (condition(this.value)) return this.value as T
        this.observeForever(observer)
        try {
            if (!latch.await(time, timeUnit)) {
                throw TimeoutException("No data or condition failed: ${this.value}")
            }
        } finally {
            this.removeObserver(observer)
        }
        @Suppress("UNCHECKED_CAST")
        return data as T
    }

    @Test
    fun createAccountAndFetchById() = runTest {
        val newAccount = MoneyAccount(
            _title = Title("Integration Test Account"),
            startBalance = BigDecimal("1000.0"),
            balance = BigDecimal("1000.0"),
            dateCreation = LocalDate.now()
        )

        val generatedId = createMoneyAccountUseCase(newAccount)
        assertTrue(generatedId > 0L)

        val fetchedAccount = loadMoneyAccountByIdUseCase(generatedId)

        assertNotNull(fetchedAccount)
        assertEquals(generatedId, fetchedAccount?.id)
        assertEquals("Integration Test Account", fetchedAccount?.title)
        
        // ИСПРАВЛЕНО: Сравниваем BigDecimal с BigDecimal через compareTo
        assertEquals(0, BigDecimal("1000.0").compareTo(fetchedAccount!!.balance))
    }

    @Test
    fun updateAccount() = runTest {
        val initialAccount = MoneyAccount(_title = Title("Initial"), startBalance = BigDecimal("100.0"))
        val id = createMoneyAccountUseCase(initialAccount)

        val updatedAccount = MoneyAccount(
            id = id,
            _title = Title("Updated Title"),
            startBalance = BigDecimal("150.0"),
            balance = BigDecimal("120.0"),
            dateCreation = initialAccount.dateCreation,
            default = false
        )

        updateMoneyAccountUseCase(updatedAccount)

        val fetchedAccount = loadMoneyAccountByIdUseCase(id)
        assertNotNull(fetchedAccount)
        assertEquals("Updated Title", fetchedAccount?.title)
        
        // ИСПРАВЛЕНО: Сравниваем BigDecimal с BigDecimal через compareTo
        assertEquals(0, BigDecimal("150.0").compareTo(fetchedAccount!!.startBalance))
        assertEquals(0, BigDecimal("120.0").compareTo(fetchedAccount.balance))
    }

    @Test
    fun deleteAccount() = runTest {
        val account = MoneyAccount(_title = Title("To Delete"), startBalance = BigDecimal("50.0"))
        val id = createMoneyAccountUseCase(account)
        // ИСПРАВЛЕНО: Правильно создаем объект для удаления
        val accountToDelete = loadMoneyAccountByIdUseCase(id)
        assertNotNull(accountToDelete) // Убедимся, что он существует
        
        deleteMoneyAccountUseCase(accountToDelete!!)

        val fetchedAccount = loadMoneyAccountByIdUseCase(id)
        assertNull(fetchedAccount)
    }

    @Test
    fun fetchAllAccounts() = runTest {
        createMoneyAccountUseCase(MoneyAccount(_title = Title("Account B"), startBalance = BigDecimal("10.0")))
        createMoneyAccountUseCase(MoneyAccount(_title = Title("Account A"), startBalance = BigDecimal("20.0")))

        val accounts = fetchMoneyAccountsUseCase().getOrAwait(time = 5) { it?.size == 2 }

        assertEquals(2, accounts.size)

        // ИСПРАВЛЕНО: Сравниваем BigDecimal с BigDecimal напрямую
        assertTrue(
            accounts.any { it.title == "Account B" && it.startBalance == BigDecimal("10.0") }
        )
        assertTrue(
            accounts.any { it.title == "Account A"  && it.startBalance == BigDecimal("20.0")}
        )
    }

    @Test
    fun setDefaultAccount() = runTest {
        val accountId1 = createMoneyAccountUseCase(MoneyAccount(_title = Title("Account 1"), startBalance = BigDecimal("10.0"), default = false))
        val accountId2 = createMoneyAccountUseCase(MoneyAccount(_title = Title("Account 2"), startBalance = BigDecimal("10.0"), default = false))

        setMoneyAccountAsDefaultUseCase(accountId2)

        val firstAccount = loadMoneyAccountByIdUseCase(accountId1)
        val secondAccount = loadMoneyAccountByIdUseCase(accountId2)

        assertNotNull(firstAccount)
        assertNotNull(secondAccount)

        assertFalse(firstAccount!!.default )
        assertTrue(secondAccount!!.default)

        setMoneyAccountAsDefaultUseCase(accountId1)
        val updatedFirstAccount = loadMoneyAccountByIdUseCase(accountId1)
        val updatedSecondAccount = loadMoneyAccountByIdUseCase(accountId2)

        assertNotNull(updatedFirstAccount)
        assertNotNull(updatedSecondAccount)

        assertTrue(updatedFirstAccount!!.default)
        assertFalse(updatedSecondAccount!!.default)
    }
}