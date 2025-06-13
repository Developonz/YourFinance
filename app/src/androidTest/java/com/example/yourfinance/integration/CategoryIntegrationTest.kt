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

    private lateinit var db: FinanceDataBase
    private lateinit var categoryDao: CategoryDao
    private lateinit var categoryRepo: CategoryRepository

    private lateinit var createCategoryUseCase: CreateAnyCategoryUseCase
    private lateinit var fetchCategoriesUseCase: FetchCategoriesUseCase
    private lateinit var loadCategoryUseCase: LoadCategoryByIdUseCase
    private lateinit var loadSubcategoryUseCase: LoadSubcategoryByIdUseCase
    private lateinit var updateCategoryUseCase: UpdateAnyCategoryUseCase
    private lateinit var deleteCategoryUseCase: DeleteAnyCategoryUseCase

    @Before
    fun setup() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            FinanceDataBase::class.java
        ).allowMainThreadQueries().build()

        categoryDao = db.getCategoryDao()
        categoryRepo = CategoryRepositoryImpl(categoryDao)

        createCategoryUseCase = CreateAnyCategoryUseCase(categoryRepo)
        fetchCategoriesUseCase = FetchCategoriesUseCase(categoryRepo)
        loadCategoryUseCase = LoadCategoryByIdUseCase(categoryRepo)
        loadSubcategoryUseCase = LoadSubcategoryByIdUseCase(categoryRepo)
        updateCategoryUseCase = UpdateAnyCategoryUseCase(categoryRepo)
        deleteCategoryUseCase = DeleteAnyCategoryUseCase(categoryRepo)
    }

    @After
    fun tearDown() { db.close() }

    // helper для LiveData
    @Throws(InterruptedException::class, TimeoutException::class)
    private fun <T> LiveData<T>.getOrAwait(
        time: Long = 2, unit: TimeUnit = TimeUnit.SECONDS,
        condition: (T?) -> Boolean = { it != null }
    ): T {
        var data: T? = null
        val latch = CountDownLatch(1)
        val obs = object : Observer<T> {
            override fun onChanged(v: T) {
                if (condition(v)) {
                    data = v; latch.countDown(); this@getOrAwait.removeObserver(this)
                }
            }
        }
        if (condition(this.value)) return this.value as T
        this.observeForever(obs)
        if (!latch.await(time, unit))
            throw TimeoutException("No data or condition failed: ${this.value}")
        return data as T
    }

    // --- Create Operations ---

    @Test
    fun createCategory_savesCorrectly() = runTest {
        val cat = Category(
            title = Title("Еда"),
            categoryType = CategoryType.EXPENSE,
            iconResourceId = "ic_food",
            colorHex = 0xFFCC00
        )

        val id = createCategoryUseCase(cat)
        val saved = loadCategoryUseCase(id)!!

        assertTrue(id > 0L)
        assertEquals("Еда", saved.title)
        assertEquals(CategoryType.EXPENSE, saved.categoryType)
        assertEquals("ic_food", saved.iconResourceId)
        assertEquals(0xFFCC00, saved.colorHex)
    }

    @Test
    fun createSubcategory_inheritsParentProperties() = runTest {
        val parentColor = 0x123456
        val parentIcon = "ic_transport"

        val parent = Category(
            title = Title("Транспорт"),
            categoryType = CategoryType.EXPENSE,
            iconResourceId = parentIcon,
            colorHex = parentColor
        )
        val parentId = createCategoryUseCase(parent)

        val sub = Subcategory(
            title = Title("Такси"),
            categoryType = CategoryType.EXPENSE,
            parentId = parentId,
            iconResourceId = null, // наследуется
            colorHex = null        // наследуется
        )
        val subId = createCategoryUseCase(sub)
        val saved = loadSubcategoryUseCase(subId)!!

        assertEquals("Такси", saved.title)
        assertEquals(parentIcon, saved.iconResourceId)
        assertEquals(parentColor, saved.colorHex)
        assertEquals(parentId, saved.parentId)
    }

    // --- Read Operations ---

    @Test
    fun fetchCategories_includesSubcategoriesWithInheritance() = runTest {
        val incomeTitle = "Зарплата"
        val bonusTitle = "Бонус"
        val expenseTitle = "Развлечения"

        // создаём родительскую категорию доходов
        val income = Category(
            title = Title(incomeTitle),
            categoryType = CategoryType.INCOME,
            iconResourceId = "ic_salary",
            colorHex = 0x00FF00
        )
        val incomeId = createCategoryUseCase(income)

        // создаём подкатегорию бонуса
        val bonus = Subcategory(
            title = Title(bonusTitle),
            categoryType = CategoryType.INCOME,
            parentId = incomeId
        )
        val bonusId = createCategoryUseCase(bonus)

        // создаём категорию расходов без подкатегорий
        val expense = Category(
            title = Title(expenseTitle),
            iconResourceId = "ic_entertainment",
            colorHex = 0xFF00FF,
            categoryType = CategoryType.EXPENSE
        )
        val expenseId = createCategoryUseCase(expense)

        val categories = fetchCategoriesUseCase().getOrAwait { it?.size == 2 }

        assertEquals("Должно быть 2 родительские категории", 2, categories.size)

        val fetchedIncome = categories.find { it.id == incomeId }!!
        assertEquals(incomeTitle, fetchedIncome.title)
        assertEquals(1, fetchedIncome.children.size)

        val fetchedBonus = fetchedIncome.children[0]
        assertEquals(bonusTitle, fetchedBonus.title)
        assertEquals("ic_salary", fetchedBonus.iconResourceId) // наследуется
        assertEquals(0x00FF00, fetchedBonus.colorHex)         // наследуется
        assertEquals(incomeId, fetchedBonus.parentId)

        val fetchedExpense = categories.find { it.id == expenseId }!!
        assertEquals(expenseTitle, fetchedExpense.title)
        assertTrue(fetchedExpense.children.isEmpty())
    }

    // --- Update Operations ---

    @Test
    fun updateCategory_changesAllProperties() = runTest {
        val oldTitle = "Старый Дом"
        val newTitle = "Новый Дом"

        val initial = Category(
            title = Title(oldTitle),
            categoryType = CategoryType.EXPENSE,
            iconResourceId = "ic_old_home",
            colorHex = 0xAAAAAA
        )
        val id = createCategoryUseCase(initial)

        val updated = Category(
            id = id,
            title = Title(newTitle),
            categoryType = CategoryType.INCOME, // меняем тип
            iconResourceId = "ic_new_home",
            colorHex = 0xBBBBBB
        )

        updateCategoryUseCase(updated)
        val saved = loadCategoryUseCase(id)!!

        assertEquals(newTitle, saved.title)
        assertEquals(CategoryType.INCOME, saved.categoryType)
        assertEquals("ic_new_home", saved.iconResourceId)
        assertEquals(0xBBBBBB, saved.colorHex)
    }

    // --- Delete Operations ---

    @Test
    fun deleteParentCategory_cascadesDeleteToChildren() = runTest {
        val parentTitle = "Родительская категория"
        val childTitle = "Дочерняя подкатегория"

        val parent = Category(
            title = Title(parentTitle),
            categoryType = CategoryType.EXPENSE,
            iconResourceId = null,
            colorHex = null
        )
        val parentId = createCategoryUseCase(parent)

        val child = Subcategory(
            title = Title(childTitle),
            categoryType = CategoryType.EXPENSE,
            parentId = parentId
        )
        val childId = createCategoryUseCase(child)

        // проверяем что обе категории созданы
        assertNotNull(loadCategoryUseCase(parentId))
        assertNotNull(loadSubcategoryUseCase(childId))

        // удаляем родительскую
        deleteCategoryUseCase(parentId)

        // проверяем каскадное удаление
        assertNull("Родительская категория должна быть удалена", loadCategoryUseCase(parentId))
        assertNull("Дочерняя категория должна быть удалена каскадно", loadSubcategoryUseCase(childId))
    }
}