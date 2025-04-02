package com.example.yourfinance.domain.repository

import androidx.lifecycle.LiveData
import com.example.yourfinance.domain.model.entity.category.FullCategory

interface CategoryRepository {
    fun getAllCategory(): LiveData<List<FullCategory>>

    suspend fun insertCategory(category: FullCategory) : Long
}