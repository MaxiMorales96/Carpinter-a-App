package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.model.CarpentryBudget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Query("SELECT * FROM carpentry_budgets ORDER BY date DESC")
    fun getAllBudgets(): Flow<List<CarpentryBudget>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBudget(budget: CarpentryBudget): Long

    @Delete
    suspend fun deleteBudget(budget: CarpentryBudget)

    @Query("DELETE FROM carpentry_budgets WHERE id = :id")
    suspend fun deleteBudgetById(id: Int)
}
