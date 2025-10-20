package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.MoneyAccountEntity
import com.example.yourfinance.data.model.PaymentEntity
import com.example.yourfinance.data.model.pojo.FullPayment
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.TransactionType
import com.example.yourfinance.domain.model.entity.MoneyAccount
import com.example.yourfinance.domain.model.entity.Payment
import com.example.yourfinance.domain.model.entity.category.BaseCategory
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test
import java.math.BigDecimal
import java.time.LocalDate

class PaymentMappersTest {

    private val testDate: LocalDate = LocalDate.of(2023, 5, 10)

    @Test
    fun `toDomain should map FullPayment to Payment correctly`() {
        val paymentEntity = PaymentEntity(
            id = 1L,
            type = TransactionType.EXPENSE,
            balance = BigDecimal("50.00"),
            moneyAccID = 10L,
            categoryID = 20L,
            note = "Lunch",
            is_done = true,
            date = testDate
        )
        val moneyAccountEntity = MoneyAccountEntity(
            id = 10L,
            title = "Wallet",
            startBalance = BigDecimal("100.00"),
            balance = BigDecimal("50.00"),
            excluded = false,
            default = true,
            used = true,
            dateCreation = testDate.minusDays(5),
            iconResourceId = "ic_wallet",
            colorHex = 0x123456
        )
        val categoryEntity = CategoryEntity(
            id = 20L,
            title = "Food",
            categoryType = CategoryType.EXPENSE,
            parentId = null,
            iconResourceId = "ic_food",
            colorHex = 0x654321
        )
        val fullPayment = FullPayment(
            payment = paymentEntity,
            moneyAcc = moneyAccountEntity,
            category = categoryEntity
        )

        val domainPayment = fullPayment.toDomain()

        assertEquals(paymentEntity.id, domainPayment.id)
        assertEquals(paymentEntity.type, domainPayment.type)
        assertEquals(paymentEntity.balance, domainPayment.balance)
        assertEquals(paymentEntity.note, domainPayment.note)
        assertEquals(paymentEntity.date, domainPayment.date)

        assertEquals(moneyAccountEntity.id, domainPayment.moneyAccount.id)
        assertEquals(moneyAccountEntity.title, domainPayment.moneyAccount.title)

        assertEquals(categoryEntity.id, domainPayment.category.id)
        assertEquals(categoryEntity.title, domainPayment.category.title)
        assertTrue(domainPayment.category is BaseCategory)
    }

    @Test
    fun `toData should map Payment to PaymentEntity correctly`() {
        val moneyAccount = MoneyAccount(
            _title = Title("Card"),
            startBalance = BigDecimal("1000.00"),
            id = 11L,
            iconResourceId = null,
            colorHex = null
        )
        val category = BaseCategory(
            _title = Title("Shopping"),
            categoryType = CategoryType.EXPENSE,
            id = 22L,
            iconResourceId = null,
            colorHex = null
        )
        val domainPayment = Payment(
            id = 2L,
            type = TransactionType.EXPENSE,
            balance = BigDecimal("75.50"),
            moneyAccount = moneyAccount,
            category = category,
            _note = Title("New shoes"),
            date = testDate
        )

        val paymentEntity = domainPayment.toData()

        assertEquals(domainPayment.id, paymentEntity.id)
        assertEquals(domainPayment.type, paymentEntity.type)
        assertEquals(domainPayment.balance, paymentEntity.balance)
        assertEquals(domainPayment.moneyAccount.id, paymentEntity.moneyAccID)
        assertEquals(domainPayment.category.id, paymentEntity.categoryID)
        assertEquals(domainPayment.note, paymentEntity.note)
        assertEquals(domainPayment.date, paymentEntity.date)
    }

}