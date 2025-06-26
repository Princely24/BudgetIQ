package com.example.budgetiq.data.dao

import androidx.room.*
import com.example.budgetiq.data.model.Badge
import kotlinx.coroutines.flow.Flow

@Dao
interface BadgeDao {
    @Insert
    suspend fun insertBadge(badge: Badge): Long

    @Query("SELECT * FROM badges WHERE userId = :userId ORDER BY dateAwarded DESC")
    fun getBadgesForUser(userId: Long): Flow<List<Badge>>

    @Query("SELECT * FROM badges WHERE userId = :userId AND name = :name AND strftime('%Y-%m', dateAwarded / 1000, 'unixepoch') = :yearMonth LIMIT 1")
    suspend fun getBadgeForUserByMonth(userId: Long, name: String, yearMonth: String): Badge?

    @Delete
    suspend fun deleteBadge(badge: Badge)

    @Query("DELETE FROM badges WHERE userId = :userId")
    suspend fun deleteAllBadgesForUser(userId: Long)
} 