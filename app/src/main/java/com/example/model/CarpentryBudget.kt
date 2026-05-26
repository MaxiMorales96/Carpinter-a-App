package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "carpentry_budgets")
data class CarpentryBudget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val clientName: String,
    val projectName: String,
    val description: String, // Detailed notes/work specification ("detalles")
    val materialsCost: Double,
    val laborCost: Double,
    val otherCosts: Double,
    val discount: Double = 0.0,
    val date: Long = System.currentTimeMillis()
)
