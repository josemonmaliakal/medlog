package com.queryb.medlog.data

import android.content.Context
import androidx.core.content.edit

class OnboardingPrefs(context: Context) {

    private val prefs = context.getSharedPreferences("medlog_onboarding", Context.MODE_PRIVATE)

    // ── Per-user key helpers ──────────────────────────────────────────────────

    private fun completeKey(username: String)     = "complete_$username"
    private fun trackedKey(username: String)      = "tracked_$username"
    private fun glucoseUnitKey(username: String)  = "glucose_unit_$username"
    private fun weightUnitKey(username: String)   = "weight_unit_$username"

    // ── Onboarding completion ─────────────────────────────────────────────────

    fun isCompleteFor(username: String): Boolean =
        prefs.getBoolean(completeKey(username), false)

    fun setCompleteFor(username: String) =
        prefs.edit { putBoolean(completeKey(username), true) }

    // ── Slide 2 — tracked items ───────────────────────────────────────────────

    fun getTrackedItems(username: String): Set<String> =
        prefs.getStringSet(trackedKey(username), emptySet()) ?: emptySet()

    fun setTrackedItems(username: String, items: Set<String>) =
        prefs.edit { putStringSet(trackedKey(username), items) }

    // ── Slide 3 — preferences ─────────────────────────────────────────────────

    fun getGlucoseUnit(username: String): String =
        prefs.getString(glucoseUnitKey(username), "mg/dL") ?: "mg/dL"

    fun setGlucoseUnit(username: String, unit: String) =
        prefs.edit { putString(glucoseUnitKey(username), unit) }

    fun getWeightUnit(username: String): String =
        prefs.getString(weightUnitKey(username), "kg") ?: "kg"

    fun setWeightUnit(username: String, unit: String) =
        prefs.edit { putString(weightUnitKey(username), unit) }
}