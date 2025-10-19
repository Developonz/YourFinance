package com.example.yourfinance.domain.repository

//imports12345678
import com.example.yourfinance.domain.model.common.Result

interface BackupRepository {
    suspend fun exportJson(): Result<String>
    suspend fun importJson(json: String): Result<Unit>
}