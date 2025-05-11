package com.example.budgetiq.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate
import java.time.LocalTime

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("categoryId")
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "userId")
    val userId: Long = 0,
    
    @ColumnInfo(name = "categoryId")
    val categoryId: Long = 0,
    
    @ColumnInfo(name = "amount")
    val amount: Double = 0.0,
    
    @ColumnInfo(name = "description")
    val description: String = "",
    
    @ColumnInfo(name = "date")
    val date: LocalDate = LocalDate.now(),
    
    @ColumnInfo(name = "startTime")
    val startTime: LocalTime = LocalTime.now(),
    
    @ColumnInfo(name = "endTime")
    val endTime: LocalTime = LocalTime.now(),
    
    @ColumnInfo(name = "photoPath")
    val photoPath: String? = null // Path to stored photo if any
) {
    constructor() : this(
        0, 0, 0, 0.0, "",
        LocalDate.now(), LocalTime.now(), LocalTime.now(), null
    )
} 