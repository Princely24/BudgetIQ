package com.example.budgetiq.data.repository

import com.example.budgetiq.data.dao.BadgeDao
import com.example.budgetiq.data.model.Badge
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeRepository @Inject constructor(
    private val badgeDao: BadgeDao
) {
    fun getBadgesForUser(userId: Long): Flow<List<Badge>> = badgeDao.getBadgesForUser(userId)

    suspend fun awardBadge(badge: Badge): Long = badgeDao.insertBadge(badge)

    suspend fun hasBadgeForUserByMonth(userId: Long, name: String, yearMonth: String): Boolean =
        badgeDao.getBadgeForUserByMonth(userId, name, yearMonth) != null

    suspend fun deleteBadge(badge: Badge) = badgeDao.deleteBadge(badge)

    suspend fun deleteAllBadgesForUser(userId: Long) = badgeDao.deleteAllBadgesForUser(userId)
} 