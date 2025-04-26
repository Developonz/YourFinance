package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.yourfinance.domain.model.CategoryType

// TODO: сделать индекс для parentId
@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class CategoryEntity(
    var title: String,
    val categoryType: CategoryType,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val parentId: Long? = null
)

