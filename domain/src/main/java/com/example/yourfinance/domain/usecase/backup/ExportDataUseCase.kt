package com.example.yourfinance.domain.usecase.backup

import com.example.yourfinance.domain.model.backup.ExportImportDomainData
import com.example.yourfinance.domain.repository.BackupRepository
import com.example.yourfinance.domain.model.common.Result
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(): Result<ExportImportDomainData> {
        return backupRepository.getAllDataForExport()
    }
}