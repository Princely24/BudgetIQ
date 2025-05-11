package com.example.budgetiq.data.model

import androidx.room.ColumnInfo

data class CategoryTotal(
    @ColumnInfo(name = "categoryId")
    val categoryId: Long,
    
    @ColumnInfo(name = "total")
    val total: Double
) {
    constructor() : this(0, 0.0)
} 