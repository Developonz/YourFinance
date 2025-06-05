package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.MoneyAccount
import junit.framework.TestCase.assertEquals
import org.junit.Test
import java.time.LocalDate

class MoneyAccountMappersTest {

    private val testDate: LocalDate = LocalDate.of(2023, 1, 15)

    @Test
    fun `toDomain should map MoneyAccountEntity to MoneyAccount correctly`() {
        val entity = MoneyAccountEntity(
            id = 1L,
            title = "Cash",
            startBalance = 100.0,
            balance = 150.0,
            excluded = false,
            default = true,
            used = true,
            dateCreation = testDate,
            iconResourceId = "ic_cash",
            colorHex = 0xAABBCC
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.startBalance, domain.startBalance)
        assertEquals(entity.balance, domain.balance)
        assertEquals(entity.excluded, domain.excluded)
        assertEquals(entity.default, domain.default)
        assertEquals(entity.used, domain.used)
        assertEquals(entity.dateCreation, domain.dateCreation)
        assertEquals(entity.iconResourceId, domain.iconResourceId)
        assertEquals(entity.colorHex, domain.colorHex)
    }

    @Test
    fun `toData should map MoneyAccount to MoneyAccountEntity correctly`() {
        val domain = MoneyAccount(
            id = 2L,
            _title = Title("Bank Account"),
            startBalance = 500.0,
            balance = 450.0,
            excluded = true,
            default = false,
            used = false,
            dateCreation = testDate,
            iconResourceId = "ic_bank",
            colorHex = 0xCCBBAA
        )

        val entity = domain.toData()

        assertEquals(domain.id, entity.id)
        assertEquals(domain.title, entity.title)
        assertEquals(domain.startBalance, entity.startBalance)
        assertEquals(domain.balance, entity.balance)
        assertEquals(domain.excluded, entity.excluded)
        assertEquals(domain.default, entity.default)
        assertEquals(domain.used, entity.used)
        assertEquals(domain.dateCreation, entity.dateCreation)
        assertEquals(domain.iconResourceId, entity.iconResourceId)
        assertEquals(domain.colorHex, entity.colorHex)
    }
}