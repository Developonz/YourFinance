package com.example.yourfinance.presentation.ui.fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.example.yourfinance.domain.model.Transaction
import com.example.yourfinance.domain.model.entity.Budget
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.usecase.budget.FetchBudgetsUseCase
import com.example.yourfinance.domain.usecase.moneyaccount.FetchMoneyAccountsUseCase
import com.example.yourfinance.domain.usecase.transaction.FetchTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GeneralViewModel @Inject constructor(
    fetchTransactionsUseCase: FetchTransactionsUseCase,
    fetchMoneyAccountsUseCase: FetchMoneyAccountsUseCase,
    fetchBudgetsUseCase: FetchBudgetsUseCase,

) : ViewModel() {

    val transactionsList: LiveData<List<Transaction>> = fetchTransactionsUseCase()
    val accountsList: LiveData<List<MoneyAccount>> = fetchMoneyAccountsUseCase()
    val budgetsList: LiveData<List<Budget>> = fetchBudgetsUseCase()


}


// TODO: сделать отслеживание фильтров. Пример:
/*
class ItemViewModel(private val itemDao: ItemDao) : ViewModel() {

    // MutableLiveData для хранения текущего типа фильтра
    private val _filterType = MutableLiveData<String>()
    val filterType: LiveData<String> get() = _filterType // Наружу expose LiveData

    // Итоговая LiveData, на которую будет подписан UI.
    // Используем switchMap: когда _filterType меняется,
    // switchMap вызывает функцию-маппер, которая возвращает
    // новую LiveData из DAO с новыми параметрами.
    val filteredItems: LiveData<List<Item>> = Transformations.switchMap(_filterType) { type ->
        if (type.isNullOrEmpty()) {
            // Если фильтр пуст или null, возвращаем все элементы
            itemDao.getAllItems()
        } else {
            // Если фильтр задан, вызываем DAO-метод с фильтром
            itemDao.getItemsByType(type)
        }
    }

    // Метод для обновления фильтра из UI
    fun setFilterType(type: String?) {
        // Устанавливаем новое значение фильтра.
        // Это действие вызывает срабатывание switchMap.
        _filterType.value = type
    }

    // Метод для вставки данных (для примера, чтобы можно было менять данные)
    fun insertItem(item: Item) = viewModelScope.launch {
        itemDao.insertItem(item)
    }

    // Инициализация начального фильтра (например, показать все сразу)
    init {
        _filterType.value = null // Или начальное значение фильтра
    }
}

// Класс для ViewModel Factory (стандартный подход для ViewModel с зависимостями)
class ItemViewModelFactory(private val itemDao: ItemDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItemViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItemViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
 */