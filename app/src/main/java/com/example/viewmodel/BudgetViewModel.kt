package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.BudgetRepository
import com.example.model.CarpentryBudget
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: BudgetRepository
    val budgetsState: StateFlow<List<CarpentryBudget>>

    // Business Profile State
    private val prefs = application.getSharedPreferences("carpentry_profile_prefs", Context.MODE_PRIVATE)

    private val _businessName = MutableStateFlow(prefs.getString("business_name", "Fábrica de Muebles a Medida") ?: "Fábrica de Muebles a Medida")
    val businessName: StateFlow<String> = _businessName.asStateFlow()

    private val _carpenterName = MutableStateFlow(prefs.getString("carpenter_name", "") ?: "")
    val carpenterName: StateFlow<String> = _carpenterName.asStateFlow()

    private val _businessPhone = MutableStateFlow(prefs.getString("business_phone", "") ?: "")
    val businessPhone: StateFlow<String> = _businessPhone.asStateFlow()

    private val _logoPath = MutableStateFlow(prefs.getString("logo_path", null))
    val logoPath: StateFlow<String?> = _logoPath.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = BudgetRepository(database.budgetDao())
        budgetsState = repository.allBudgets.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }

    // Save profile configurations
    fun updateProfile(name: String, carpenter: String, phone: String) {
        _businessName.value = name
        _carpenterName.value = carpenter
        _businessPhone.value = phone

        prefs.edit().apply {
            putString("business_name", name)
            putString("carpenter_name", carpenter)
            putString("business_phone", phone)
            apply()
        }
    }

    // Save picked logo to internal storage for permanent app access
    fun saveLogoUri(uri: Uri) {
        viewModelScope.launch {
            val context = getApplication<Application>().applicationContext
            val savedPath = saveImageToInternalStorage(context, uri)
            if (savedPath != null) {
                _logoPath.value = savedPath
                prefs.edit().putString("logo_path", savedPath).apply()
            }
        }
    }

    // Clear logo configuration
    fun removeLogo() {
        _logoPath.value = null
        prefs.edit().remove("logo_path").apply()
        // Delete physical file under logos folder if exists
        try {
            val context = getApplication<Application>().applicationContext
            val directory = File(context.filesDir, "logos")
            val file = File(directory, "business_logo.png")
            if (file.exists()) {
                file.delete()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Create a new budget
    fun createBudget(
        clientName: String,
        projectName: String,
        description: String,
        materials: Double,
        labor: Double,
        other: Double,
        discount: Double,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val budget = CarpentryBudget(
                clientName = clientName.trim(),
                projectName = projectName.trim(),
                description = description.trim(),
                materialsCost = materials,
                laborCost = labor,
                otherCosts = other,
                discount = discount,
                date = System.currentTimeMillis()
            )
            repository.insertBudget(budget)
            onComplete()
        }
    }

    // Delete a budget
    fun deleteBudget(budget: CarpentryBudget) {
        viewModelScope.launch {
            repository.deleteBudget(budget)
        }
    }

    // Dynamic copy of Stream input helper
    private fun saveImageToInternalStorage(context: Context, uri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            val directory = File(context.filesDir, "logos")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "business_logo.png")
            file.outputStream().use { outputStream ->
                inputStream.use { inputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
