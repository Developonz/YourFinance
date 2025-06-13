package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.TransferEntity
import com.example.yourfinance.data.model.pojo.FullTransfer
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Transfer
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDate

class TransferMappersTest {

    private val testDate: LocalDate = LocalDate.of(2023, 8, 20)

    @Test
    fun `toDomain should map FullTransfer to Transfer correctly`() {
        val transferEntity = TransferEntity(
            id = 1L,
            type = TransactionType.REMITTANCE,
            balance = 100.0,
            moneyAccFromID = 10L,
            moneyAccToID = 11L,
            note = "Monthly savings",
            is_done = true,
            date = testDate
        )
        val moneyAccFromEntity = MoneyAccountEntity(
            id = 10L,
            title = "Checking",
            startBalance = 500.0,
            balance = 400.0,
            excluded = false,
            default = true,
            used = true,
            dateCreation = testDate.minusMonths(1),
            iconResourceId = "ic_checking",
            colorHex = 0x112233
        )
        val moneyAccToEntity = MoneyAccountEntity(
            id = 11L,
            title = "Savings",
            startBalance = 1000.0,
            balance = 1100.0,
            excluded = false,
            default = false,
            used = true,
            dateCreation = testDate.minusMonths(2),
            iconResourceId = "ic_savings",
            colorHex = 0x332211
        )
        val fullTransfer = FullTransfer(
            transfer = transferEntity,
            moneyAccFrom = moneyAccFromEntity,
            moneyAccTo = moneyAccToEntity
        )

        val domainTransfer = fullTransfer.toDomain()

        assertEquals(transferEntity.id, domainTransfer.id)
        assertEquals(transferEntity.type, domainTransfer.type)
        assertEquals(transferEntity.balance, domainTransfer.balance)
        assertEquals(transferEntity.note, domainTransfer.note)
        assertEquals(transferEntity.date, domainTransfer.date)

        assertEquals(moneyAccFromEntity.id, domainTransfer.moneyAccFrom.id)
        assertEquals(moneyAccFromEntity.title, domainTransfer.moneyAccFrom.title)

        assertEquals(moneyAccToEntity.id, domainTransfer.moneyAccTo.id)
        assertEquals(moneyAccToEntity.title, domainTransfer.moneyAccTo.title)
    }

    @Test
    fun `toData should map Transfer to TransferEntity correctly`() {
        val moneyAccFrom = MoneyAccount(
            _title = Title("Cash From"),
            startBalance = 200.0,
            id = 20L
        )
        val moneyAccTo = MoneyAccount(
            _title = Title("Cash To"),
            startBalance = 50.0,
            id = 21L
        )
        val domainTransfer = Transfer(
            id = 2L,
            balance = 50.0,
            moneyAccFrom = moneyAccFrom,
            moneyAccTo = moneyAccTo,
            _note = Title("Lend money"),
            date = testDate
        )

        val transferEntity = domainTransfer.toData()

        assertEquals(domainTransfer.id, transferEntity.id)
        assertEquals(domainTransfer.type, transferEntity.type)
        assertEquals(domainTransfer.balance, transferEntity.balance)
        assertEquals(domainTransfer.moneyAccFrom.id, transferEntity.moneyAccFromID)
        assertEquals(domainTransfer.moneyAccTo.id, transferEntity.moneyAccToID)
        assertEquals(domainTransfer.note, transferEntity.note)
        assertEquals(domainTransfer.date, transferEntity.date)
    }

}