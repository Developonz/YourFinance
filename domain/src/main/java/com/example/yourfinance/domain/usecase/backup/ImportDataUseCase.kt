package com.example.yourfinance.domain.usecase.backup


import com.example.yourfinance.domain.repository.BackupRepository
import com.example.yourfinance.domain.model.common.Result
import javax.inject.Inject

class ImportDataUseCase @Inject constructor(
    private val backupRepository: BackupRepository
) {
    suspend operator fun invoke(json: String): Result<Unit> {
        return backupRepository.importJson(json)
    }
}