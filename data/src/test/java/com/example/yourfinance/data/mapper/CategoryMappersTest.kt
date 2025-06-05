package com.example.yourfinance.data.mapper

import com.example.yourfinance.data.model.CategoryEntity
import com.example.yourfinance.data.model.pojo.CategoryWithSubcategories
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.Subcategory
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test


class CategoryMappersTest {

    @Test
    fun `toDomain should map CategoryEntity to BaseCategory correctly`() {
        val entity = CategoryEntity(
            id = 1L,
            title = "Groceries",
            categoryType = CategoryType.EXPENSE,
            parentId = null,
            iconResourceId = "ic_groceries",
            colorHex = 0xFF00FF
        )

        val domain = entity.toDomain()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.categoryType, domain.categoryType)
        assertEquals(entity.iconResourceId, domain.iconResourceId)
        assertEquals(entity.colorHex, domain.colorHex)
    }

    @Test
    fun `toDomainSubcategory should map CategoryEntity to Subcategory correctly`() {
        val entity = CategoryEntity(
            id = 2L,
            title = "Fruits",
            categoryType = CategoryType.EXPENSE,
            parentId = 1L,
            iconResourceId = "ic_fruits",
            colorHex = 0x00FF00
        )

        val domain = entity.toDomainSubcategory()

        assertEquals(entity.id, domain.id)
        assertEquals(entity.title, domain.title)
        assertEquals(entity.categoryType, domain.categoryType)
        assertEquals(entity.parentId, domain.parentId)
        assertEquals(entity.iconResourceId, domain.iconResourceId)
        assertEquals(entity.colorHex, domain.colorHex)
    }

    @Test
    fun `toDomainCategory should map CategoryWithSubcategories to Category correctly`() {
        val parentEntity = CategoryEntity(
            id = 1L,
            title = "Food",
            categoryType = CategoryType.EXPENSE,
            parentId = null,
            iconResourceId = "ic_food",
            colorHex = 0xFF0000
        )
        val subEntity1 = CategoryEntity(
            id = 2L,
            title = "Fruits",
            categoryType = CategoryType.EXPENSE,
            parentId = 1L,
            iconResourceId = "ic_fruits",
            colorHex = 0x00FF00
        )
        val subEntity2 = CategoryEntity(
            id = 3L,
            title = "Vegetables",
            categoryType = CategoryType.EXPENSE,
            parentId = 1L,
            iconResourceId = "ic_vegetables",
            colorHex = 0x0000FF
        )
        val categoryWithSubcategories = CategoryWithSubcategories(
            category = parentEntity,
            subcategories = listOf(subEntity1, subEntity2)
        )

        val domainCategory = categoryWithSubcategories.toDomainCategory()

        assertEquals(parentEntity.id, domainCategory.id)
        assertEquals(parentEntity.title, domainCategory.title)
        assertEquals(parentEntity.categoryType, domainCategory.categoryType)
        assertEquals(parentEntity.iconResourceId, domainCategory.iconResourceId)
        assertEquals(parentEntity.colorHex, domainCategory.colorHex)
        assertEquals(2, domainCategory.children.size)

        val domainSub1 = domainCategory.children.find { it.id == subEntity1.id }
        assertNotNull(domainSub1)
        assertEquals(subEntity1.title, domainSub1!!.title)
        assertEquals(subEntity1.parentId, domainSub1.parentId)

        val domainSub2 = domainCategory.children.find { it.id == subEntity2.id }
        assertNotNull(domainSub2)
        assertEquals(subEntity2.title, domainSub2!!.title)
        assertEquals(subEntity2.parentId, domainSub2.parentId)
    }

    @Test
    fun `toDomainCategory should handle empty subcategories`() {
        val parentEntity = CategoryEntity(
            id = 1L,
            title = "Food",
            categoryType = CategoryType.EXPENSE,
            parentId = null,
            iconResourceId = "ic_food",
            colorHex = 0xFF0000
        )
        val categoryWithSubcategories = CategoryWithSubcategories(
            category = parentEntity,
            subcategories = emptyList()
        )

        val domainCategory = categoryWithSubcategories.toDomainCategory()

        assertEquals(parentEntity.id, domainCategory.id)
        assertEquals(parentEntity.title, domainCategory.title)
        assertTrue(domainCategory.children.isEmpty())
    }

    @Test
    fun `toData should map Category to CategoryEntity correctly`() {
        val domainCategory = Category(
            id = 1L,
            title = Title("Salary"),
            categoryType = CategoryType.INCOME,
            iconResourceId = "ic_salary",
            colorHex = 0x00FFFF
        )

        val entity = domainCategory.toData()

        assertEquals(domainCategory.id, entity.id)
        assertEquals(domainCategory.title, entity.title)
        assertEquals(domainCategory.categoryType, entity.categoryType)
        assertNull(entity.parentId)
        assertEquals(domainCategory.iconResourceId, entity.iconResourceId)
        assertEquals(domainCategory.colorHex, entity.colorHex)
    }

    @Test
    fun `toData should map Subcategory to CategoryEntity correctly`() {
        val domainSubcategory = Subcategory(
            id = 2L,
            title = Title("Bonus"),
            categoryType = CategoryType.INCOME,
            parentId = 1L,
            iconResourceId = "ic_bonus", // Маппер Subcategory.toData() устанавливает iconResourceId = null
            colorHex = 0xFFFF00
        )

        val entity = domainSubcategory.toData()

        assertEquals(domainSubcategory.id, entity.id)
        assertEquals(domainSubcategory.title, entity.title)
        assertEquals(domainSubcategory.categoryType, entity.categoryType)
        assertEquals(domainSubcategory.parentId, entity.parentId)
        assertNull(entity.iconResourceId) // Проверка на null согласно мапперу
        assertEquals(domainSubcategory.colorHex, entity.colorHex)
    }
}