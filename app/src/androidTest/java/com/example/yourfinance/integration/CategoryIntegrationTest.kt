package com.example.yourfinance.integration

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.example.yourfinance.data.repository.CategoryRepositoryImpl
import com.example.yourfinance.data.source.CategoryDao
import com.example.yourfinance.data.source.FinanceDataBase
import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title
import com.example.yourfinance.domain.model.entity.category.Category
import com.example.yourfinance.domain.model.entity.category.Subcategory
import com.example.yourfinance.domain.repository.CategoryRepository
import com.example.yourfinance.domain.usecase.categories.general.CreateAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.category.FetchCategoriesUseCase
import com.example.yourfinance.domain.usecase.categories.category.LoadCategoryByIdUseCase
import com.example.yourfinance.domain.usecase.categories.general.UpdateAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.general.DeleteAnyCategoryUseCase
import com.example.yourfinance.domain.usecase.categories.subcategory.LoadSubcategoryByIdUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.junit.Assert.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class CategoryIntegrationTest {

    @get:Rule(order = 0)
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: FinanceDataBase
    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryRepository: CategoryRepository

    private lateinit var createCategoryUseCase: CreateAnyCategoryUseCase
    private lateinit var fetchCategoriesUseCase: FetchCategoriesUseCase
    private lateinit var loadCategoryByIdUseCase: LoadCategoryByIdUseCase
    private lateinit var loadSubcategoryByIdUseCase: LoadSubcategoryByIdUseCase
    private lateinit var updateCategoryUseCase: UpdateAnyCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteAnyCategoryUseCase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FinanceDataBase::class.java
        )
            .allowMainThreadQueries()
            .build()

        categoryDao = database.getCategoryDao()
        categoryRepository = CategoryRepositoryImpl(categoryDao)

        createCategoryUseCase = CreateAnyCategoryUseCase(categoryRepository)
        fetchCategoriesUseCase = FetchCategoriesUseCase(categoryRepository)
        loadCategoryByIdUseCase = LoadCategoryByIdUseCase(categoryRepository)
        loadSubcategoryByIdUseCase = LoadSubcategoryByIdUseCase(categoryRepository)
        updateCategoryUseCase = UpdateAnyCategoryUseCase(categoryRepository)
        deleteCategoryUseCase = DeleteAnyCategoryUseCase(categoryRepository)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        database.close()
    }

    @Throws(InterruptedException::class, TimeoutException::class)
    private fun <T> LiveData<T>.getOrAwaitValue(
        time: Long = 2,
        timeUnit: TimeUnit = TimeUnit.SECONDS
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val observer = object : Observer<T> {
            override fun onChanged(value: T) {
                data = value
                latch.countDown()
                this@getOrAwaitValue.removeObserver(this)
            }
        }
        this.observeForever(observer)
        try {
            if (!latch.await(time, timeUnit)) {
                throw TimeoutException("LiveData value was never set.")
            }
        } finally {
            this.removeObserver(observer)
        }
        @Suppress("UNCHECKED_CAST")
        return data as T
    }

    @Test
    fun createCategory() = runTest {
        val foodCategory = Category (
            title = Title("Еда"),
            categoryType = CategoryType.EXPENSE,
            iconResourceId = "ic_food_icon",
            colorHex = 0xFFCC00
        )

        val generatedId = createCategoryUseCase(foodCategory)
        assertTrue("Generated ID should be positive",generatedId > 0L)

        val fetchedCategory: Category? = loadCategoryByIdUseCase(generatedId)

        assertNotNull("Fetched category should not be null", fetchedCategory)
        assertEquals("ID should match", generatedId, fetchedCategory?.id)
        assertEquals("Title should match", "Еда", fetchedCategory?.title)
        assertEquals("CategoryType should match", CategoryType.EXPENSE, fetchedCategory?.categoryType)
        assertEquals("IconResourceId should match", "ic_food_icon", fetchedCategory?.iconResourceId)
        assertEquals("ColorHex should match", 0xFFCC00.toInt(), fetchedCategory?.colorHex)
    }

    @Test
    fun createSubcategory_inheritsParentPropertiesOnCreation() = runTest {
        val parentColor = 0x123456
        val parentIcon = "ic_parent_transport"
        val transportCategory = Category(
            title = Title("Транспорт"),
            categoryType = CategoryType.EXPENSE,
            iconResourceId = parentIcon,
            colorHex = parentColor
        )
        val parentId = createCategoryUseCase(transportCategory)
        assertTrue(parentId > 0L)

        val taxiSubcategory = Subcategory(
            title = Title("Такси"),
            categoryType = CategoryType.EXPENSE,
            parentId = parentId,
            iconResourceId = null, // Explicitly null to check inheritance
            colorHex = null       // Explicitly null to check inheritance
        )
        val subcategoryId = createCategoryUseCase(taxiSubcategory)
        assertTrue(subcategoryId > 0L)

        val fetchedSubcategory: Subcategory? = loadSubcategoryByIdUseCase(subcategoryId)

        assertNotNull("Fetched subcategory should not be null", fetchedSubcategory)
        assertEquals("Icon should be inherited from parent", parentIcon, fetchedSubcategory?.iconResourceId)
        assertEquals("Color should be inherited from parent", parentColor, fetchedSubcategory?.colorHex)
        assertEquals("ParentId should be correct", parentId, fetchedSubcategory?.parentId)
        assertEquals("Title should be correct", "Такси", fetchedSubcategory?.title)
    }


    @Test
    fun fetchAllCategories_includesSubcategoriesWithInheritedProperties() = runTest {
        val incomeCategory = Category(
            title = Title("Зарплата"),
            categoryType = CategoryType.INCOME,
            iconResourceId = "ic_salary",
            colorHex = 0x00FF00
        )
        val incomeCategoryId = createCategoryUseCase(incomeCategory)
        assertTrue(incomeCategoryId > 0L)

        val bonusSubcategory = Subcategory(
            title = Title("Бонус"),
            categoryType = CategoryType.INCOME,
            parentId = incomeCategoryId
        )
        val bonusSubcategoryId = createCategoryUseCase(bonusSubcategory)
        assertTrue(bonusSubcategoryId > 0L)

        val expenseCategory = Category(
            title = Title("Развлечения"),
            iconResourceId = "ic_entertainment",
            colorHex = 0xFF00FF,
            categoryType = CategoryType.EXPENSE
        )
        val expenseCategoryId = createCategoryUseCase(expenseCategory)
        assertTrue(expenseCategoryId > 0L)

        val categoriesWithSubcategories = fetchCategoriesUseCase().getOrAwaitValue()

        assertEquals("Should be 2 parent categories",2, categoriesWithSubcategories.size)

        val fetchedIncomeCategory = categoriesWithSubcategories.find { it.id == incomeCategoryId }
        assertNotNull("Income category should be found", fetchedIncomeCategory)
        assertEquals("Income category title mismatch", "Зарплата", fetchedIncomeCategory?.title)
        assertNotNull("Income category children should not be null", fetchedIncomeCategory?.children)
        assertEquals("Income category should have 1 child",1, fetchedIncomeCategory?.children?.size)

        val fetchedBonusSubcategory = fetchedIncomeCategory?.children?.get(0)
        assertNotNull("Bonus subcategory should exist", fetchedBonusSubcategory)
        assertEquals("Bonus subcategory title mismatch", "Бонус", fetchedBonusSubcategory?.title)
        assertEquals("Bonus subcategory icon should be inherited", "ic_salary", fetchedBonusSubcategory?.iconResourceId)
        assertEquals("Bonus subcategory color should be inherited", 0x00FF00.toInt(), fetchedBonusSubcategory?.colorHex)
        assertEquals("Bonus subcategory parent ID should match", incomeCategoryId, fetchedBonusSubcategory?.parentId)


        val fetchedExpenseCategory = categoriesWithSubcategories.find { it.id == expenseCategoryId }
        assertNotNull("Expense category should be found", fetchedExpenseCategory)
        assertEquals("Expense category title mismatch", "Развлечения", fetchedExpenseCategory?.title)
        assertNotNull("Expense category children should not be null", fetchedExpenseCategory?.children)
        assertTrue("Expense category should have no children", fetchedExpenseCategory?.children?.isEmpty() == true)
    }

    @Test
    fun updateCategory() = runTest {
        val initialHomeCategory = Category(
            title = Title("Старый Дом"),
            categoryType = CategoryType.EXPENSE,
            iconResourceId = "ic_old_home",
            colorHex = 0xAAAAAA.toInt()
        )
        val categoryId = createCategoryUseCase(initialHomeCategory)
        assertTrue(categoryId > 0L)

        val updatedHomeCategory = Category(
            id = categoryId,
            title = Title("Новый Дом"),
            categoryType = CategoryType.INCOME, // Changed type
            iconResourceId = "ic_new_home",
            colorHex = 0xBBBBBB.toInt()
        )

        updateCategoryUseCase(updatedHomeCategory)

        val fetchedCategory: Category? = loadCategoryByIdUseCase(categoryId)
        assertNotNull("Fetched category after update should not be null", fetchedCategory)
        assertEquals("Title should be updated", "Новый Дом", fetchedCategory?.title)
        assertEquals("CategoryType should be updated", CategoryType.INCOME, fetchedCategory?.categoryType)
        assertEquals("IconResourceId should be updated", "ic_new_home", fetchedCategory?.iconResourceId)
        assertEquals("ColorHex should be updated", 0xBBBBBB.toInt(), fetchedCategory?.colorHex)
    }


    @Test
    fun deleteParentCategory_cascadesDeletionToSubcategories() = runTest {
        val parentCategoryForDeletion = Category(
            title = Title("Удалить Родителя"),
            categoryType = CategoryType.EXPENSE,
            iconResourceId = null,
            colorHex = null
        )
        val parentId = createCategoryUseCase(parentCategoryForDeletion)
        assertTrue(parentId > 0L)

        val childCategoryForDeletion = Subcategory(
            title = Title("Удалить Ребенка"),
            categoryType = CategoryType.EXPENSE,
            parentId = parentId
        )
        val childId = createCategoryUseCase(childCategoryForDeletion)
        assertTrue(childId > 0L)


        assertNotNull("Parent category should exist before deletion", loadCategoryByIdUseCase(parentId))
        assertNotNull("Child subcategory should exist before deletion", loadSubcategoryByIdUseCase(childId))

        deleteCategoryUseCase(parentId)

        assertNull("Parent category should be null after deletion", loadCategoryByIdUseCase(parentId))
        assertNull("Child subcategory should be null after parent deletion (cascade)", loadSubcategoryByIdUseCase(childId))
    }
}