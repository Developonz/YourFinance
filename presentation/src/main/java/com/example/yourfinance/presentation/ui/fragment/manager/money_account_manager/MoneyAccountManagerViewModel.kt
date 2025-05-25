package com.example.yourfinance.presentation.ui.fragment.manager.money_account_manager

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.usecase.moneyaccount.CreateMoneyAccountUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.DeleteMoneyAccountUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.FetchMoneyAccountsUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.LoadMoneyAccountByIdUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.SetMoneyAccountAsDefaultUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.UpdateMoneyAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MoneyAccountManagerViewModel @Inject constructor(
    fetchMoneyAccountsUseCase: FetchMoneyAccountsUseCase,
    private val deleteMoneyAccountUseCase: DeleteMoneyAccountUseCase,
    private val createMoneyAccountUseCase: CreateMoneyAccountUseCase,
    private val updateMoneyAccountUseCase: UpdateMoneyAccountUseCase,
    private val loadMoneyAccountByIdUseCase: LoadMoneyAccountByIdUseCase,
    private val setMoneyAccountAsDefaultUseCase: SetMoneyAccountAsDefaultUseCase
    ) : ViewModel() {

    val accountsList: LiveData<List<MoneyAccount>> = fetchMoneyAccountsUseCase()

    fun deleteAccount(account: MoneyAccount) {
        viewModelScope.launch {
            deleteMoneyAccountUseCase(account)
        }
    }

    fun createAccount(account: MoneyAccount) {
        viewModelScope.launch {
            createMoneyAccountUseCase(account)
        }
    }

    fun updateAccount(account: MoneyAccount) {
        viewModelScope.launch {
            updateMoneyAccountUseCase(account)
        }
    }

    fun setDefaultAccount(account: MoneyAccount) {
        viewModelScope.launch {
            setMoneyAccountAsDefaultUseCase(account.id)
        }
    }

    suspend fun loadAccountById(id: Long): MoneyAccount? = loadMoneyAccountByIdUseCase(id)

}