package com.example.budgetiq.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,
    
    @ColumnInfo(name = "username")
    val username: String = "",
    
    @ColumnInfo(name = "password")
    val password: String = "" // Note: In a production app, this should be properly hashed
) {
    constructor() : this(0, "", "")
} 