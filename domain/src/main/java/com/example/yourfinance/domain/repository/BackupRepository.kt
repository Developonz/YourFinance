package com.example.yourfinance.domain.repository


import com.example.yourfinance.domain.model.common.Result

interface BackupRepository {
    suspend fun exportJson(): Result<String>
    suspend fun importJson(json: String): Result<Unit>
}