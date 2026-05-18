package com.queryb.medlog.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.queryb.medlog.data.LabDatabase
import com.queryb.medlog.data.LabResult
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.flatMapLatest
import androidx.compose.runtime.snapshotFlow
import com.queryb.medlog.data.OnboardingPrefs
class LabViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = LabDatabase.getDatabase(app).labResultDao()

    // userId is set once after login, before any screen observes results
    private var userId: String = ""

    fun setUserId(id: String) {
        userId = id
    }

    val results: StateFlow<List<LabResult>> = snapshotFlow { userId }
        .flatMapLatest { id ->
            if (id.isEmpty()) kotlinx.coroutines.flow.flowOf(emptyList())
            else dao.getAllResults(id)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insert(result: LabResult) = viewModelScope.launch {
        dao.insert(result.copy(userId = userId))   // ← stamp userId on every insert
    }

    fun delete(result: LabResult) = viewModelScope.launch { dao.delete(result) }

    fun getTrackedItems(onboardingPrefs: OnboardingPrefs, userId: String): Set<String> =
        onboardingPrefs.getTrackedItems(userId).ifEmpty {
            setOf("blood_sugar", "cholesterol") // default: show all if nothing selected
        }

    suspend fun getAllSortedByDate(): List<LabResult> =
        dao.getAllResultsSortedByDate(userId)
}
