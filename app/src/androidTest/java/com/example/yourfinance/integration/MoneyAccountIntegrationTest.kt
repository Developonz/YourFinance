package com.example.yourfinance.integration

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
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
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

    @Throws(InterruptedException::class, TimeoutException::class)
    fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        this.observeForever(observer)
        try {
            if (!latch.await(time, timeUnit)) {
                throw TimeoutException("LiveData value was never set.")
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
            startBalance = 1000.0,
            balance = 1000.0,
            dateCreation = LocalDate.now()
        )

        val generatedId = createMoneyAccountUseCase(newAccount)
        assertThat(generatedId).isGreaterThan(0L)

        val fetchedAccount = loadMoneyAccountByIdUseCase(generatedId)

        assertThat(fetchedAccount).isNotNull()
        assertThat(fetchedAccount?.id).isEqualTo(generatedId)
        assertThat(fetchedAccount?.title).isEqualTo("Integration Test Account")
        assertThat(fetchedAccount?.balance).isEqualTo(1000.0)
    }

    @Test
    fun updateAccount() = runTest {
        val initialAccount = MoneyAccount(_title = Title("Initial"), startBalance = 100.0)
        val id = createMoneyAccountUseCase(initialAccount)

        val updatedAccount = MoneyAccount(
            id = id,
            _title = Title("Updated Title"),
            startBalance = 150.0,
            balance = 120.0,
            dateCreation = initialAccount.dateCreation,
            default = false
        )

        updateMoneyAccountUseCase(updatedAccount)

        val fetchedAccount = loadMoneyAccountByIdUseCase(id)
        assertThat(fetchedAccount).isNotNull()
        assertThat(fetchedAccount?.title).isEqualTo("Updated Title")
        assertThat(fetchedAccount?.startBalance).isEqualTo(150.0)
        assertThat(fetchedAccount?.balance).isEqualTo(120.0)
    }

    @Test
    fun deleteAccount() = runTest {
        val account = MoneyAccount(_title = Title("To Delete"), startBalance = 50.0)
        val id = createMoneyAccountUseCase(account)
        val accountToDelete = account.copy(id = id)

        deleteMoneyAccountUseCase(accountToDelete)

        val fetchedAccount = loadMoneyAccountByIdUseCase(id)
        assertNull(fetchedAccount)
    }

    @Test
    fun fetchAllAccounts() = runTest {
        createMoneyAccountUseCase(MoneyAccount(_title = Title("Account B"), startBalance = 10.0))
        createMoneyAccountUseCase(MoneyAccount(_title = Title("Account A"), startBalance = 20.0))

        val accounts = fetchMoneyAccountsUseCase().getOrAwaitValue()

        assertThat(accounts).hasSize(2)
        assertThat(accounts[0].title).isEqualTo("Account A")
        assertThat(accounts[1].title).isEqualTo("Account B")
    }

    @Test
    fun setDefaultAccount() = runTest {
        val accountId1 = createMoneyAccountUseCase(MoneyAccount(_title = Title("Account 1"), startBalance = 10.0, default = false))
        val accountId2 = createMoneyAccountUseCase(MoneyAccount(_title = Title("Account 2"), startBalance = 10.0, default = false))

        setMoneyAccountAsDefaultUseCase(accountId2)

        val firstAccount = loadMoneyAccountByIdUseCase(accountId1)
        val secondAccount = loadMoneyAccountByIdUseCase(accountId2)

        assertThat(firstAccount?.default).isFalse()
        assertThat(secondAccount?.default).isTrue()

        setMoneyAccountAsDefaultUseCase(accountId1)
        val updatedFirstAccount = loadMoneyAccountByIdUseCase(accountId1)
        val updatedSecondAccount = loadMoneyAccountByIdUseCase(accountId2)

        assertThat(updatedFirstAccount?.default).isTrue()
        assertThat(updatedSecondAccount?.default).isFalse()
    }
}