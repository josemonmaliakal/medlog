package com.queryb.medlog.backup

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.InputStreamContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.queryb.medlog.data.LabResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
// Result types
sealed class BackupResult {
    data class Success(val message: String) : BackupResult()
    data class Error(val message: String)   : BackupResult()
}

sealed class RestoreResult {
    data class Success(val records: List<LabResult>) : RestoreResult()
    data class Error(val message: String)            : RestoreResult()
    object NoBackupFound                             : RestoreResult()
}

class DriveBackupManager(private val context: Context) {

    companion object {
        private const val BACKUP_FILE_NAME = "medlog_backup.json"
        private const val FOLDER_NAME      = "MedLog Backups"
        private const val APP_NAME         = "MedLog"
    }

    // ── Google Sign-In client ─────────────────────────────────────────────────

    private fun buildSignInClient(): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_APPDATA))
            .build()
        return GoogleSignIn.getClient(context, options)
    }

    fun getSignInIntent(): Intent = buildSignInClient().signInIntent

    fun getSignedInAccount(): GoogleSignInAccount? =
        GoogleSignIn.getLastSignedInAccount(context)

    fun isSignedIn(): Boolean {
        val account = getSignedInAccount() ?: return false
        return account.grantedScopes.any { it.scopeUri == DriveScopes.DRIVE_APPDATA }
    }

    fun signOut(onComplete: () -> Unit) {
        buildSignInClient().signOut().addOnCompleteListener { onComplete() }
    }

    // ── Build Drive service ───────────────────────────────────────────────────

    private fun buildDriveService(account: GoogleSignInAccount): Drive {
        val credential = GoogleAccountCredential.usingOAuth2(
            context, listOf(DriveScopes.DRIVE_APPDATA)
        ).apply { selectedAccount = account.account }

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        ).setApplicationName(APP_NAME).build()
    }

    // ── Backup ────────────────────────────────────────────────────────────────

    suspend fun backup(
        results: List<LabResult>,
        userId: String
    ): BackupResult = withContext(Dispatchers.IO) {
        try {
            val account = getSignedInAccount()
                ?: return@withContext BackupResult.Error("Not signed in to Google")

            val drive   = buildDriveService(account)
            val json    = serializeResults(results, userId)
            val bytes   = json.toByteArray(Charsets.UTF_8)
            val stream  = ByteArrayInputStream(bytes)

            // Check if backup file already exists
            val existingId = findBackupFileId(drive, userId)

            val metadata = File().apply {
                name    = "${userId}_$BACKUP_FILE_NAME"
                parents = listOf("appDataFolder")
            }

            if (existingId != null) {
                // Update existing file
                drive.files().update(existingId, null,
                    InputStreamContent("application/json", stream)
                ).execute()
            } else {
                // Create new file
                drive.files().create(metadata,
                    InputStreamContent("application/json", stream)
                ).setFields("id").execute()
            }

            val timestamp = SimpleDateFormat(
                "dd MMM yyyy, hh:mm a", Locale.getDefault()
            ).format(Date())

            BackupResult.Success("Backed up ${results.size} records on $timestamp")

        } catch (e: Exception) {
            BackupResult.Error("Backup failed: ${e.message ?: "Unknown error"}")
        }
    }

    // ── Restore ───────────────────────────────────────────────────────────────

    suspend fun restore(userId: String): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val account = getSignedInAccount()
                ?: return@withContext RestoreResult.Error("Not signed in to Google")

            val drive      = buildDriveService(account)
            val existingId = findBackupFileId(drive, userId)
                ?: return@withContext RestoreResult.NoBackupFound

            val output = ByteArrayOutputStream()
            drive.files().get(existingId).executeMediaAndDownloadTo(output)

            val json    = output.toString(Charsets.UTF_8.name())
            val records = deserializeResults(json, userId)

            RestoreResult.Success(records)

        } catch (e: Exception) {
            RestoreResult.Error("Restore failed: ${e.message ?: "Unknown error"}")
        }
    }

    // ── Get last backup info ──────────────────────────────────────────────────

    suspend fun getLastBackupInfo(userId: String): String? = withContext(Dispatchers.IO) {
        try {
            val account = getSignedInAccount() ?: return@withContext null
            val drive   = buildDriveService(account)
            val fileId  = findBackupFileId(drive, userId) ?: return@withContext null

            val file = drive.files().get(fileId)
                .setFields("modifiedTime")
                .execute()

            file.modifiedTime?.let {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                    .format(Date(it.value))
            }
        } catch (e: Exception) {
            null
        }
    }

    // ── Delete backup ─────────────────────────────────────────────────────────

    suspend fun deleteBackup(userId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = getSignedInAccount() ?: return@withContext false
            val drive   = buildDriveService(account)
            val fileId  = findBackupFileId(drive, userId) ?: return@withContext false
            drive.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            false
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun findBackupFileId(drive: Drive, userId: String): String? {
        val result = drive.files().list()
            .setSpaces("appDataFolder")
            .setQ("name = '${userId}_$BACKUP_FILE_NAME'")
            .setFields("files(id, name)")
            .execute()
        return result.files?.firstOrNull()?.id
    }

    private fun serializeResults(results: List<LabResult>, userId: String): String {
        val array = JSONArray()
        results.forEach { r ->
            array.put(JSONObject().apply {
                put("id",            r.id)
                put("userId",        r.userId)
                put("date",          r.date)
                put("time",          r.time)
                put("readingType",   r.readingType)
                put("pdfUri",        r.pdfUri)
                put("glucose",       r.glucose)
                put("cholesterol",   r.cholesterol)
                put("hdl",           r.hdl)
                put("ldl",           r.ldl)
                put("triglycerides", r.triglycerides)
                put("hemoglobin",    r.hemoglobin)
            })
        }
        return JSONObject().apply {
            put("userId",    userId)
            put("version",   1)
            put("exportedAt", System.currentTimeMillis())
            put("records",   array)
        }.toString()
    }

    private fun deserializeResults(json: String, userId: String): List<LabResult> {
        val root    = JSONObject(json)
        val array   = root.getJSONArray("records")
        val results = mutableListOf<LabResult>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            results.add(
                LabResult(
                    id            = 0,   // Room will auto-generate new IDs
                    userId        = userId,
                    date          = obj.optString("date"),
                    time          = obj.optString("time"),
                    readingType   = obj.optString("readingType"),
                    pdfUri        = obj.optString("pdfUri"),
                    glucose       = obj.optDouble("glucose", 0.0).toFloat(),
                    cholesterol   = obj.optDouble("cholesterol", 0.0).toFloat(),
                    hdl           = obj.optDouble("hdl", 0.0).toFloat(),
                    ldl           = obj.optDouble("ldl", 0.0).toFloat(),
                    triglycerides = obj.optDouble("triglycerides", 0.0).toFloat(),
                    hemoglobin    = obj.optDouble("hemoglobin", 0.0).toFloat()
                )
            )
        }
        return results
    }
}