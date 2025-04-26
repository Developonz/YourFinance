package com.example.yourfinance.domain.model.entity.category

import com.example.yourfinance.domain.model.CategoryType
import com.example.yourfinance.domain.model.Title


//data class Category(
//    private var _title: Title,
//    val categoryType: CategoryType,
//    val id: Long = 0,
//) {
//    var title: String
//        get() = _title.value
//        set(value) { _title = Title(value) }
//}

data class Category private constructor(
    private val baseProperties: BaseCategory,
    val children: MutableList<Subcategory> = mutableListOf()
) : ICategoryData by baseProperties {


    constructor(
        title: Title,
        categoryType: CategoryType,
        id: Long = 0,
        children: MutableList<Subcategory> = mutableListOf()
    ) : this(BaseCategory(title, categoryType, id), children)

}



/*


interface ICategoryData {
    val id: Long
    var title: String
    val categoryType: CategoryType
    // Если были бы общие методы, они были бы тут:
    // fun printBaseInfo()
}

// 2. Класс ТОЛЬКО для базовых свойств - ОБЫЧНЫЙ КЛАСС, РЕАЛИЗУЕТ ICategoryData
class BaseCategoryProperties(
    private var _title: String,
    override val categoryType: CategoryType,
    override val id: Long = 0,
) : ICategoryData { // <-- Реализуем интерфейс ICategoryData

    override var title: String
        get() = _title
        set(value) {
            _title = getUpperFirstChar(value)
        }
    init {
        title = title
    }

    override fun toString(): String {
        return "BaseCategoryProperties(_title='$_title', categoryType=$categoryType, id=$id)"
    }

    // Если в ICategoryData был fun printBaseInfo(), его реализация была бы здесь
    // override fun printBaseInfo() { println("ID: $id, Title: $title, Type: $categoryType") }
}

// 3. Класс Category - data class, РЕАЛИЗУЕТ ICategoryData (через делегирование), содержит children
data class Category(
    private val baseProperties: BaseCategoryProperties, // Свойство data class
    val children: MutableList<Subcategory> = mutableListOf() // Свойство data class
) : ICategoryData by baseProperties { // <-- Делегируем РЕАЛИЗАЦИЮ ИНТЕРФЕЙСА

    // Вторичный конструктор для удобства
    constructor(
        title: String,
        categoryType: CategoryType,
        id: Long = 0,
        children: MutableList<Subcategory> = mutableListOf()
    ) : this(BaseCategoryProperties(title, categoryType, id), children)

    // Здесь автоматически предоставляются члены ICategoryData (id, title, categoryType)
    // путем перенаправления вызовов на baseProperties.
    // Если у вас были бы другие члены (не из ICategoryData), они были бы здесь.

    fun addSubcategory(subcategory: Subcategory) {
         if (subcategory.parentId != this.id) {
             println("Warning: Adding subcategory '${subcategory.title}' with incorrect parentId (${subcategory.parentId}) to category '${this.title}' (ID: ${this.id}).")
         }
         children.add(subcategory)
     }
}

// 4. Класс Subcategory - data class, РЕАЛИЗУЕТ ICategoryData (через делегирование), содержит parentId
data class Subcategory(
    private val baseProperties: BaseCategoryProperties, // Свойство data class
    val parentId: Long
) : ICategoryData by baseProperties { // <-- Делегируем РЕАЛИЗАЦИЮ ИНТЕРФЕЙСА

    // Вторичный конструктор для удобства
    constructor(
        title: String,
        categoryType: CategoryType,
        id: Long = 0,
        parentId: Long
    ) : this(BaseCategoryProperties(title, categoryType, id), parentId)

    // Здесь автоматически предоставляются члены ICategoryData (id, title, categoryType)
    // путем перенаправления вызовов на baseProperties.
}

 */