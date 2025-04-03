package com.example.yourfinance.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.example.yourfinance.data.mapper.toData
import com.example.yourfinance.data.mapper.toDomain
import com.example.yourfinance.data.source.FinanceDao
import com.example.yourfinance.domain.model.entity.category.FullCategory
import com.example.yourfinance.domain.repository.CategoryRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(private val dao: FinanceDao) : CategoryRepository{
    override fun getAllCategory(): LiveData<List<FullCategory>> {
        val mediator = MediatorLiveData<List<FullCategory>>()
        val categories = dao.getFullAllCategory()
        mediator.addSource(categories) {
            mediator.value = (categories.value ?: emptyList()).map {it.toDomain()}
        }
        return mediator
    }

    override suspend fun insertCategory(category: FullCategory) : Long {
        var id: Long = 0
        withContext(Dispatchers.IO) {
            id = dao.insertCategory(category.category.toData())
        }
        return id
    }
}