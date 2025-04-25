package com.example.yourfinance.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.example.yourfinance.domain.model.CategoryType

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["parentId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class SubcategoryEntity(
    override var title: String,
    override val categoryType: CategoryType,
    @PrimaryKey(autoGenerate = true)
    override val id: Long = 0,
    val parentId: Long
) : CategoryEntity(title, categoryType, id)