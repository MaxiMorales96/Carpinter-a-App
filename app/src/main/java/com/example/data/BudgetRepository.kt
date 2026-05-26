package com.example.data

import com.example.model.CarpentryBudget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {
    val allBudgets: Flow<List<CarpentryBudget>> = budgetDao.getAllBudgets()

    suspend fun insertBudget(budget: CarpentryBudget): Long {
        return budgetDao.insertBudget(budget)
    }

    suspend fun deleteBudget(budget: CarpentryBudget) {
        budgetDao.deleteBudget(budget)
    }

    suspend fun deleteBudgetById(id: Int) {
        budgetDao.deleteBudgetById(id)
    }
}
