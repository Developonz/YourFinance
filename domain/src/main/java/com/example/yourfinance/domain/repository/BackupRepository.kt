package com.example.yourfinance.domain.repository

//imports12
import com.example.yourfinance.domain.model.common.Result

interface BackupRepository {
    suspend fun exportJson(): Result<String>
    suspend fun importJson(json: String): Result<Unit>
}