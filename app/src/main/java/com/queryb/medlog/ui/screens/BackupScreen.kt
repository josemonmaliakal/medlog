package com.queryb.medlog.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.common.api.ApiException
import com.queryb.medlog.backup.BackupResult
import com.queryb.medlog.backup.DriveBackupManager
import com.queryb.medlog.backup.RestoreResult
import com.queryb.medlog.ui.viewmodel.LabViewModel
import kotlinx.coroutines.launch

private val Teal        = Color(0xFF00897B)
private val TealLight   = Color(0xFFE8F5F3)
private val TealMid     = Color(0xFFB2DFDB)
private val NavyDark    = Color(0xFF0D1B2A)
private val GrayMuted   = Color(0xFF9CA3AF)
private val GrayText    = Color(0xFF6B7280)
private val ScreenBg    = Color(0xFFF8FFFE)
private val CardWhite   = Color(0xFFFFFFFF)
private val ErrorRed    = Color(0xFFD32F2F)
private val ErrorLight  = Color(0xFFFFEBEE)
private val SuccessGreen = Color(0xFF2E7D32)
private val SuccessLight = Color(0xFFE8F5E9)
private val GoogleBlue  = Color(0xFF4285F4)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    viewModel: LabViewModel,
    userId: String,
    onBack: () -> Unit
) {
    val context  = LocalContext.current
    val scope    = rememberCoroutineScope()
    val manager  = remember { DriveBackupManager(context) }
    val results  by viewModel.results.collectAsState()

    var isSignedIn       by remember { mutableStateOf(manager.isSignedIn()) }
    var signedInEmail    by remember { mutableStateOf(manager.getSignedInAccount()?.email ?: "") }
    var isLoading        by remember { mutableStateOf(false) }
    var statusMessage    by remember { mutableStateOf("") }
    var statusSuccess    by remember { mutableStateOf(true) }
    var lastBackupTime   by remember { mutableStateOf<String?>(null) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var showDeleteDialog  by remember { mutableStateOf(false) }

    // Load last backup time on open
    LaunchedEffect(isSignedIn) {
        if (isSignedIn) {
            lastBackupTime = manager.getLastBackupInfo(userId)
        }
    }

    // Google Sign-In launcher
    val signInLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            isSignedIn   = true
            signedInEmail = account.email ?: ""
            statusMessage = "Signed in as ${account.email}"
            statusSuccess = true
        } catch (e: ApiException) {
            statusMessage = "Sign in failed: ${e.message}"
            statusSuccess = false
        }
    }

    // Restore confirmation dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Restore Backup?", fontWeight = FontWeight.Bold, color = NavyDark)
            },
            text = {
                Text(
                    "This will add all records from your Google Drive backup. " +
                            "Existing records will not be deleted.",
                    color = GrayText, fontSize = 13.sp, lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showRestoreDialog = false
                        scope.launch {
                            isLoading = true
                            statusMessage = ""
                            when (val res = manager.restore(userId)) {
                                is RestoreResult.Success -> {
                                    res.records.forEach { viewModel.insert(it) }
                                    statusSuccess = true
                                    statusMessage = "Restored ${res.records.size} records successfully"
                                    lastBackupTime = manager.getLastBackupInfo(userId)
                                }
                                is RestoreResult.NoBackupFound -> {
                                    statusSuccess = false
                                    statusMessage = "No backup found on Google Drive"
                                }
                                is RestoreResult.Error -> {
                                    statusSuccess = false
                                    statusMessage = res.message
                                }
                            }
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Teal),
                    shape  = RoundedCornerShape(12.dp)
                ) { Text("Restore") }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = false }) {
                    Text("Cancel", color = GrayMuted)
                }
            }
        )
    }

    // Delete backup dialog
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            shape = RoundedCornerShape(20.dp),
            title = {
                Text("Delete Backup?", fontWeight = FontWeight.Bold, color = NavyDark)
            },
            text = {
                Text(
                    "This will permanently delete your backup from Google Drive. " +
                            "Your local data will not be affected.",
                    color = GrayText, fontSize = 13.sp, lineHeight = 19.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteDialog = false
                        scope.launch {
                            isLoading = true
                            val deleted = manager.deleteBackup(userId)
                            statusSuccess = deleted
                            statusMessage = if (deleted) "Backup deleted from Google Drive"
                            else "Failed to delete backup"
                            if (deleted) lastBackupTime = null
                            isLoading = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape  = RoundedCornerShape(12.dp)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel", color = GrayMuted)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Google Drive Backup",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp,
                            color      = Color.White)
                        Text("Keep your health data safe",
                            fontSize = 11.sp,
                            color    = Color.White.copy(alpha = 0.75f))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Teal,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = ScreenBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Status banner ─────────────────────────────────────────────────
            AnimatedVisibility(
                visible = statusMessage.isNotEmpty(),
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                StatusBanner(message = statusMessage, success = statusSuccess)
            }

            // ── Google account card ───────────────────────────────────────────
            BackupCard(title = "Google Account", icon = Icons.Outlined.AccountCircle) {
                if (isSignedIn) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GoogleBlue.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AccountCircle, null,
                                tint = GoogleBlue, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Signed in", fontSize = 11.sp,
                                color = GrayMuted, fontWeight = FontWeight.Medium)
                            Text(signedInEmail, fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold, color = NavyDark)
                        }
                        TextButton(
                            onClick = {
                                manager.signOut {
                                    isSignedIn    = false
                                    signedInEmail = ""
                                    lastBackupTime = null
                                    statusMessage  = "Signed out of Google"
                                    statusSuccess  = true
                                }
                            }
                        ) {
                            Text("Sign out", color = ErrorRed, fontSize = 13.sp)
                        }
                    }
                } else {
                    Column(
                        modifier            = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Outlined.CloudOff, null,
                            tint     = GrayMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Text(
                            "Not connected to Google",
                            fontWeight = FontWeight.SemiBold,
                            color      = NavyDark,
                            fontSize   = 14.sp
                        )
                        Text(
                            "Sign in to back up and restore\nyour health data",
                            fontSize  = 12.sp,
                            color     = GrayMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                        Spacer(Modifier.height(14.dp))
                        Button(
                            onClick = {
                                signInLauncher.launch(manager.getSignInIntent())
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape    = RoundedCornerShape(12.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = GoogleBlue
                            ),
                            elevation = ButtonDefaults.buttonElevation(0.dp)
                        ) {
                            Icon(Icons.Default.AccountCircle, null,
                                modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sign in with Google",
                                fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // ── Backup status card ────────────────────────────────────────────
            if (isSignedIn) {
                BackupCard(title = "Backup Status", icon = Icons.Outlined.CloudDone) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (lastBackupTime != null)
                                    "Last backup"
                                else
                                    "No backup yet",
                                fontSize = 11.sp,
                                color    = GrayMuted,
                                fontWeight = FontWeight.Medium
                            )
                            if (lastBackupTime != null) {
                                Text(
                                    lastBackupTime!!,
                                    fontSize   = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color      = NavyDark
                                )
                            }
                        }
                        Surface(
                            color = if (lastBackupTime != null) TealLight else Color(0xFFFFF3E0),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = if (lastBackupTime != null) "Backed up" else "Never",
                                fontSize   = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (lastBackupTime != null) Teal
                                else Color(0xFFF57C00),
                                modifier   = Modifier.padding(
                                    horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Storage, null,
                            tint = GrayMuted, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(5.dp))
                        Text(
                            "${results.size} record${if (results.size != 1) "s" else ""} locally",
                            fontSize = 12.sp, color = GrayMuted
                        )
                    }
                }

                // ── Backup actions card ───────────────────────────────────────
                BackupCard(title = "Actions", icon = Icons.Outlined.CloudSync) {
                    if (isLoading) {
                        Box(
                            modifier            = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment    = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(
                                    color    = Teal,
                                    modifier = Modifier.size(36.dp)
                                )
                                Spacer(Modifier.height(10.dp))
                                Text("Processing...", color = GrayMuted, fontSize = 13.sp)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            // Backup now
                            ActionButton(
                                icon    = Icons.Default.CloudUpload,
                                label   = "Back Up Now",
                                sub     = "Upload ${results.size} records to Google Drive",
                                color   = Teal,
                                bgColor = TealLight,
                                onClick = {
                                    scope.launch {
                                        isLoading     = true
                                        statusMessage = ""
                                        when (val res = manager.backup(results, userId)) {
                                            is BackupResult.Success -> {
                                                statusSuccess  = true
                                                statusMessage  = res.message
                                                lastBackupTime = manager.getLastBackupInfo(userId)
                                            }
                                            is BackupResult.Error -> {
                                                statusSuccess = false
                                                statusMessage = res.message
                                            }
                                        }
                                        isLoading = false
                                    }
                                }
                            )

                            // Restore
                            ActionButton(
                                icon    = Icons.Default.CloudDownload,
                                label   = "Restore from Drive",
                                sub     = if (lastBackupTime != null)
                                    "From $lastBackupTime"
                                else
                                    "No backup found yet",
                                color   = GoogleBlue,
                                bgColor = GoogleBlue.copy(alpha = 0.08f),
                                enabled = lastBackupTime != null,
                                onClick = { showRestoreDialog = true }
                            )

                            // Delete backup
                            if (lastBackupTime != null) {
                                ActionButton(
                                    icon    = Icons.Default.DeleteOutline,
                                    label   = "Delete Drive Backup",
                                    sub     = "Remove backup from Google Drive only",
                                    color   = ErrorRed,
                                    bgColor = ErrorLight,
                                    onClick = { showDeleteDialog = true }
                                )
                            }
                        }
                    }
                }

                // ── Privacy info ──────────────────────────────────────────────
                Card(
                    modifier  = Modifier.fillMaxWidth(),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = CardDefaults.cardColors(containerColor = TealLight),
                    elevation = CardDefaults.cardElevation(0.dp),
                    border    = androidx.compose.foundation.BorderStroke(1.dp, TealMid)
                ) {
                    Row(
                        modifier          = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(Icons.Default.Shield, null,
                            tint     = Teal,
                            modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text("Your data stays private",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 13.sp,
                                color      = NavyDark)
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "Backups are stored in your personal Google Drive app folder. " +
                                        "Only MedLog can access them — not even you can see them directly in Drive.",
                                fontSize   = 11.sp,
                                color      = GrayText,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Action button row inside card ─────────────────────────────────────────────
@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    sub: String,
    color: Color,
    bgColor: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.4f
    Card(
        onClick   = { if (enabled) onClick() },
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f * alpha)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null,
                    tint     = color.copy(alpha = alpha),
                    modifier = Modifier.size(22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 13.sp,
                    color      = if (enabled) NavyDark else GrayMuted)
                Text(sub,
                    fontSize = 11.sp,
                    color    = GrayMuted.copy(alpha = alpha))
            }
            if (enabled) {
                Icon(Icons.Default.ChevronRight, null,
                    tint     = color,
                    modifier = Modifier.size(18.dp))
            }
        }
    }
}

// ── Backup section card wrapper ───────────────────────────────────────────────
@Composable
private fun BackupCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp,
            Color(0xFFB2DFDB))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(TealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Teal, modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.width(10.dp))
                Text(title,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = NavyDark)
            }
            HorizontalDivider(
                modifier  = Modifier.padding(vertical = 14.dp),
                color     = TealLight,
                thickness = 1.dp
            )
            content()
        }
    }
}

// ── Status banner ─────────────────────────────────────────────────────────────
@Composable
private fun StatusBanner(message: String, success: Boolean) {
    val bg    = if (success) SuccessLight else ErrorLight
    val color = if (success) SuccessGreen else ErrorRed
    val icon  = if (success) Icons.Default.CheckCircleOutline
    else         Icons.Default.ErrorOutline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg)
            .border(1.dp,
                if (success) SuccessGreen.copy(alpha = 0.3f)
                else ErrorRed.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(10.dp))
        Text(message, fontSize = 13.sp,
            color = color, fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f))
    }
}