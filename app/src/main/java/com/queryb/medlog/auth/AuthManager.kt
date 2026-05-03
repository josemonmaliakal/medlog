package com.queryb.medlog.auth

import android.content.Context
import androidx.core.content.edit

class AuthManager(context: Context) {

    private val prefs = context.getSharedPreferences("medlog_auth", Context.MODE_PRIVATE)

    // ── Session ───────────────────────────────────────────────────────────────

    var currentUser: String
        get() = prefs.getString(KEY_CURRENT_USER, "") ?: ""
        private set(v) = prefs.edit { putString(KEY_CURRENT_USER, v) }

    val isLoggedIn: Boolean get() = currentUser.isNotEmpty()

    val isFirstLaunch: Boolean
        get() = getAllUsers().isEmpty()

    // ── User list — stored as comma-separated string, NOT StringSet ───────────
    // Android SharedPreferences.getStringSet() has a known caching bug where
    // updates to the set are silently dropped. Plain strings never have this issue.

    private fun getAllUsers(): Set<String> {
        // Migration: old versions stored this as a StringSet.
        // If that old key exists, migrate it to CSV string and delete the old key.
        val oldSet = try {
            prefs.getStringSet(KEY_ALL_USERS, null)
        } catch (e: ClassCastException) {
            null
        }

        if (oldSet != null) {
            // Migrate old StringSet → new CSV string format
            val migrated = oldSet.filter { it.isNotBlank() }.toSet()
            prefs.edit {
                remove(KEY_ALL_USERS)                           // remove the StringSet
                putString(KEY_ALL_USERS, migrated.joinToString(","))  // save as CSV
            }
            return migrated
        }

        // Normal path — read as CSV string
        val raw = try {
            prefs.getString(KEY_ALL_USERS, "") ?: ""
        } catch (e: ClassCastException) {
            // Fallback: wipe the corrupted key entirely
            prefs.edit { remove(KEY_ALL_USERS) }
            ""
        }

        return if (raw.isBlank()) emptySet()
        else raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    private fun saveAllUsers(users: Set<String>) {
        prefs.edit { putString(KEY_ALL_USERS, users.joinToString(",")) }
    }

    // ── Per-user key helpers ──────────────────────────────────────────────────

    private fun pwKey(username: String)          = "pw_$username"
    private fun displayNameKey(username: String) = "dn_$username"
    private fun emailKey(username: String)       = "em_$username"

    // ── Auth ──────────────────────────────────────────────────────────────────

    enum class RegisterResult {
        SUCCESS,
        USERNAME_TAKEN,
        USERNAME_BLANK,
        PASSWORD_TOO_SHORT
    }

    fun registerWithResult(username: String, password: String): RegisterResult {
        val name = username.trim().lowercase()
        return when {
            name.isBlank()             -> RegisterResult.USERNAME_BLANK
            password.length < 4        -> RegisterResult.PASSWORD_TOO_SHORT
            getAllUsers().contains(name) -> RegisterResult.USERNAME_TAKEN
            else -> {
                val users = getAllUsers().toMutableSet()
                users.add(name)
                saveAllUsers(users)
                prefs.edit {
                    putString(pwKey(name), password.hashCode().toString())
                    putString(displayNameKey(name), username.trim())
                }
                currentUser = name
                RegisterResult.SUCCESS
            }
        }
    }

    // Keep old register() so nothing else breaks
    fun register(username: String, password: String): Boolean =
        registerWithResult(username, password) == RegisterResult.SUCCESS

    fun login(username: String, password: String): Boolean {
        val name = username.trim().lowercase()
        val storedHash = prefs.getString(pwKey(name), null) ?: return false
        val ok = storedHash == password.hashCode().toString()
        if (ok) currentUser = name
        return ok
    }

    fun logout() = prefs.edit { putString(KEY_CURRENT_USER, "") }

    // ── Profile reads ─────────────────────────────────────────────────────────

    fun getUsername(): String = currentUser

    fun getDisplayName(): String =
        prefs.getString(displayNameKey(currentUser), "") ?: ""

    fun getEmail(): String =
        prefs.getString(emailKey(currentUser), "") ?: ""

    // ── Profile writes ────────────────────────────────────────────────────────

    fun updateDisplayName(name: String): Boolean {
        if (name.isBlank()) return false
        prefs.edit { putString(displayNameKey(currentUser), name.trim()) }
        return true
    }

    fun updateEmail(email: String): Boolean {
        prefs.edit { putString(emailKey(currentUser), email.trim()) }
        return true
    }

    fun updatePassword(currentPassword: String, newPassword: String): Boolean {
        val storedHash = prefs.getString(pwKey(currentUser), null)
        if (storedHash != currentPassword.hashCode().toString()) return false
        if (newPassword.length < 4) return false
        prefs.edit { putString(pwKey(currentUser), newPassword.hashCode().toString()) }
        return true
    }

    companion object {
        private const val KEY_CURRENT_USER = "current_user"
        private const val KEY_ALL_USERS    = "all_users"   // now a CSV string, not StringSet
    }
    /**
     * Resets password without requiring the old one.
     * Returns false if username doesn't exist or new password is too short.
     */
    fun resetPassword(username: String, newPassword: String): Boolean {
        val name = username.trim().lowercase()
        val users = getAllUsers()
        if (!users.contains(name)) return false
        if (newPassword.length < 4) return false
        prefs.edit { putString(pwKey(name), newPassword.hashCode().toString()) }
        return true
    }

    fun usernameExists(username: String): Boolean =
        getAllUsers().contains(username.trim().lowercase())
}