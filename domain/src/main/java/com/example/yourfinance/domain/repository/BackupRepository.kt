package com.example.yourfinance.domain.repository

import com.example.yourfinance.domain.model.backup.ExportImportDomainData
import com.example.yourfinance.domain.model.common.Result

interface BackupRepository {
    suspend fun getAllDataForExport(): Result<ExportImportDomainData>
    suspend fun importAllData(data: ExportImportDomainData): Result<Unit>
}