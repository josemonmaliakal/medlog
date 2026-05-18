package com.queryb.medlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Info
import com.queryb.medlog.auth.AuthManager
import androidx.compose.ui.platform.LocalContext
import com.queryb.medlog.ui.components.LogoutConfirmDialog
import androidx.fragment.app.FragmentActivity
import com.queryb.medlog.auth.BiometricHelper
import com.queryb.medlog.auth.BiometricResult
import com.queryb.medlog.backup.DriveBackupManager
import com.queryb.medlog.data.OnboardingPrefs



// ── Colors ─────────────────────────────────────────────────────────────────────
private val Teal       = Color(0xFF00897B)
private val TealLight  = Color(0xFFE8F5F3)
private val TealMid    = Color(0xFFB2DFDB)
private val NavyDark   = Color(0xFF0D1B2A)
private val GrayMuted  = Color(0xFF9CA3AF)
private val GrayText   = Color(0xFF6B7280)
private val ScreenBg   = Color(0xFFF8FFFE)
private val CardWhite  = Color(0xFFFFFFFF)
private val ErrorRed   = Color(0xFFD32F2F)
private val ErrorLight = Color(0xFFFFEBEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authManager: AuthManager,
    onboardingPrefs: OnboardingPrefs,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onBackupClick: () -> Unit

) {
    // ── State ─────────────────────────────────────────────────────────────────
    var displayName     by remember { mutableStateOf(authManager.getDisplayName()) }
    var email           by remember { mutableStateOf(authManager.getEmail()) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmNew      by remember { mutableStateOf("") }

    var showCurrentPw by remember { mutableStateOf(false) }
    var showNewPw     by remember { mutableStateOf(false) }
    var showConfirmPw by remember { mutableStateOf(false) }

    var profileMsg      by remember { mutableStateOf("") }
    var profileSuccess  by remember { mutableStateOf(false) }
    var passwordMsg     by remember { mutableStateOf("") }
    var passwordSuccess by remember { mutableStateOf(false) }

    var showLogoutDialog by remember { mutableStateOf(false) }
    val context  = LocalContext.current
    val activity = context as? FragmentActivity
    var biometricEnabled by remember {
        mutableStateOf(authManager.isBiometricEnabledForCurrent())
    }
    val biometricAvailable = remember { BiometricHelper.isAvailable(context) }
    val driveManager = remember { DriveBackupManager(context) }
    var isBackedUp   by remember { mutableStateOf(driveManager.isSignedIn()) }
    var trackedItems by remember {
        mutableStateOf(
            onboardingPrefs.getTrackedItems(authManager.getUsername())
                .ifEmpty { setOf("blood_sugar", "cholesterol") }
        )
    }

    // ── Logout dialog ─────────────────────────────────────────────────────────
    if (showLogoutDialog) {
        LogoutConfirmDialog(
            username  = authManager.getUsername(),
            onConfirm = { showLogoutDialog = false; onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "My Profile",
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Teal,
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
                .padding(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Avatar header ─────────────────────────────────────────────────
            Card(
                modifier  = Modifier.fillMaxWidth(),
                shape     = RoundedCornerShape(20.dp),
                colors    = CardDefaults.cardColors(containerColor = Teal),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Avatar circle
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f))
                            .border(2.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (authManager.getDisplayName().ifEmpty {
                                authManager.getUsername()
                            }).take(1).uppercase(),
                            color      = Color.White,
                            fontSize   = 26.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = authManager.getDisplayName()
                                .ifEmpty { authManager.getUsername() }
                                .replaceFirstChar { it.uppercase() },
                            color      = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 17.sp
                        )
                        Spacer(Modifier.height(3.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.AccountCircle, null,
                                tint     = Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = "@${authManager.getUsername()}",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 13.sp
                            )
                        }
                        if (authManager.getEmail().isNotEmpty()) {
                            Spacer(Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Outlined.Email, null,
                                    tint     = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text     = authManager.getEmail(),
                                    color    = Color.White.copy(alpha = 0.75f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── Personal information card ─────────────────────────────────────
            ProfileCard(
                title = "Personal Information",
                icon  = Icons.Outlined.Person
            ) {
                ProfileField(
                    label        = "Display Name",
                    value        = displayName,
                    onValueChange = {
                        displayName = it
                        profileMsg  = ""
                    },
                    placeholder  = "Your name or nickname",
                    icon         = Icons.Outlined.Badge,
                    keyboardType = KeyboardType.Text
                )

                Spacer(Modifier.height(12.dp))

                ProfileField(
                    label        = "Email (optional)",
                    value        = email,
                    onValueChange = {
                        email      = it
                        profileMsg = ""
                    },
                    placeholder  = "your@email.com",
                    icon         = Icons.Outlined.Email,
                    keyboardType = KeyboardType.Email
                )

                // Feedback banner
                if (profileMsg.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    FeedbackBanner(message = profileMsg, success = profileSuccess)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val ok = authManager.updateDisplayName(displayName)
                        authManager.updateEmail(email)
                        profileSuccess = ok
                        profileMsg     = if (ok) "Profile updated successfully"
                        else "Display name cannot be empty"
                    },
                    modifier  = Modifier.fillMaxWidth().height(48.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = Teal),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Save Changes", fontWeight = FontWeight.SemiBold)
                }
            }

            // --- Selection card -----
            ProfileCard(title = "Tracking Preferences", icon = Icons.Outlined.Tune) {
                Text(
                    "Select what you want to track and see on your dashboard.",
                    fontSize = 12.sp,
                    color = GrayMuted,
                    lineHeight = 17.sp
                )
                Spacer(Modifier.height(14.dp))

                val options = listOf(
                    "blood_sugar"  to "Blood Sugar",
                    "cholesterol"  to "Cholesterol"
                )

                options.forEach { (id, label) ->
                    val isSelected = trackedItems.contains(id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) TealLight else Color(0xFFF5F5F5))
                            .border(
                                1.dp,
                                if (isSelected) Teal else Color(0xFFE0E0E0),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                trackedItems = if (isSelected && trackedItems.size > 1) {
                                    trackedItems - id      // prevent deselecting all
                                } else {
                                    trackedItems + id
                                }
                                onboardingPrefs.setTrackedItems(authManager.getUsername(), trackedItems)
                            }
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (id == "blood_sugar") Icons.Outlined.Bloodtype
                            else Icons.Outlined.Favorite,
                            contentDescription = null,
                            tint = if (isSelected) Teal else GrayMuted,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            label,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                            fontSize   = 14.sp,
                            color      = if (isSelected) Teal else NavyDark,
                            modifier   = Modifier.weight(1f)
                        )
                        if (isSelected) {
                            Icon(Icons.Default.CheckCircle, null,
                                tint = Teal, modifier = Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                // Hint when only one selected
                if (trackedItems.size == 1) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFF3E0))
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Outlined.Info, null,
                            tint = Color(0xFFF57C00), modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "At least one option must remain selected",
                            fontSize = 11.sp, color = Color(0xFFF57C00)
                        )
                    }
                }
            }

            // ── Backup card ───────────────────────────────────────────────────────────────
            ProfileCard(title = "Data Backup", icon = Icons.Outlined.CloudSync) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Google Drive Backup",
                            fontWeight = FontWeight.SemiBold,
                            fontSize   = 14.sp,
                            color      = NavyDark
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            if (isBackedUp) "Connected — your data is backed up"
                            else "Not connected — tap to set up backup",
                            fontSize   = 12.sp,
                            color      = if (isBackedUp) Teal else GrayMuted,
                            lineHeight = 17.sp
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = GrayMuted
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick   = onBackupClick,          // ← wired via parameter below
                    modifier  = Modifier.fillMaxWidth().height(46.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = Teal),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(
                        if (isBackedUp) Icons.Default.CloudSync else Icons.Default.CloudUpload,
                        null, modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isBackedUp) "Manage Backup" else "Set Up Backup",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // ── Change password card ──────────────────────────────────────────
            ProfileCard(
                title = "Change Password",
                icon  = Icons.Outlined.Lock
            ) {
                PasswordField(
                    label    = "Current Password",
                    value    = currentPassword,
                    visible  = showCurrentPw,
                    onValueChange = {
                        currentPassword = it
                        passwordMsg     = ""
                    },
                    onToggle = { showCurrentPw = !showCurrentPw }
                )

                Spacer(Modifier.height(12.dp))

                PasswordField(
                    label    = "New Password",
                    value    = newPassword,
                    visible  = showNewPw,
                    onValueChange = {
                        newPassword = it
                        passwordMsg = ""
                    },
                    onToggle = { showNewPw = !showNewPw }
                )

                Spacer(Modifier.height(12.dp))

                PasswordField(
                    label    = "Confirm New Password",
                    value    = confirmNew,
                    visible  = showConfirmPw,
                    onValueChange = {
                        confirmNew  = it
                        passwordMsg = ""
                    },
                    onToggle = { showConfirmPw = !showConfirmPw }
                )

                if (passwordMsg.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    FeedbackBanner(message = passwordMsg, success = passwordSuccess)
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        passwordMsg = when {
                            currentPassword.isBlank() ->
                                "Enter your current password"
                            newPassword.length < 4    ->
                                "New password must be at least 4 characters"
                            newPassword != confirmNew ->
                                "New passwords do not match"
                            else -> {
                                val ok = authManager.updatePassword(currentPassword, newPassword)
                                passwordSuccess = ok
                                if (ok) {
                                    currentPassword = ""
                                    newPassword     = ""
                                    confirmNew      = ""
                                    "Password changed successfully"
                                } else {
                                    "Current password is incorrect"
                                }
                            }
                        }
                        if (!passwordSuccess && passwordMsg != "Password changed successfully") {
                            passwordSuccess = false
                        }
                    },
                    modifier  = Modifier.fillMaxWidth().height(48.dp),
                    shape     = RoundedCornerShape(12.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = Teal),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.LockReset, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Update Password", fontWeight = FontWeight.SemiBold)
                }
            }
            // ── Biometric card — only shown if hardware available ─────────────────────────
            if (biometricAvailable) {
                ProfileCard(
                    title = "Quick Access",
                    icon  = Icons.Outlined.Fingerprint
                ) {
                    Row(
                        modifier          = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Biometric / PIN Login",
                                fontWeight = FontWeight.SemiBold,
                                fontSize   = 14.sp,
                                color      = NavyDark
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "Use fingerprint, face or device PIN\nto log in without a password",
                                fontSize   = 12.sp,
                                color      = GrayMuted,
                                lineHeight = 17.sp
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Switch(
                            checked         = biometricEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && activity != null) {
                                    // Verify identity before enabling
                                    BiometricHelper.authenticate(
                                        activity = activity,
                                        title    = "Confirm identity",
                                        subtitle = "Verify to enable biometric login"
                                    ) { result ->
                                        when (result) {
                                            is BiometricResult.Success -> {
                                                biometricEnabled = true
                                                authManager.setBiometricEnabledForCurrent(true)
                                            }
                                            else -> { /* do nothing — toggle stays off */ }
                                        }
                                    }
                                } else {
                                    biometricEnabled = false
                                    authManager.setBiometricEnabledForCurrent(false)
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor  = Color.White,
                                checkedTrackColor  = Teal,
                                uncheckedThumbColor = GrayMuted,
                                uncheckedTrackColor = Color(0xFFE0E0E0)
                            )
                        )
                    }

                    // Info note
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(TealLight)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Info, null,
                            tint     = Teal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text      = if (biometricEnabled)
                                "Biometric login is active for this account"
                            else
                                "Enable to skip password on next login",
                            fontSize  = 11.sp,
                            color     = Teal,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            // ── Account info card ─────────────────────────────────────────────
            ProfileCard(
                title = "Account",
                icon  = Icons.Outlined.ManageAccounts
            ) {
                Row(
                    modifier          = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(TealLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Outlined.AccountCircle, null,
                            tint = Teal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Username",
                            fontSize   = 11.sp,
                            color      = GrayMuted,
                            fontWeight = FontWeight.Medium)
                        Text(
                            "@${authManager.getUsername()}",
                            fontSize   = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = NavyDark
                        )
                    }
                    Surface(
                        color = TealLight,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Fixed",
                            fontSize   = 10.sp,
                            color      = Teal,
                            fontWeight = FontWeight.SemiBold,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Log out button
                OutlinedButton(
                    onClick  = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape    = RoundedCornerShape(12.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border   = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed.copy(alpha = 0.5f))
                ) {
                    Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// ── Profile card wrapper ───────────────────────────────────────────────────────
@Composable
private fun ProfileCard(
    title: String,
    icon: ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(20.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = androidx.compose.foundation.BorderStroke(1.dp, TealMid)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Card title row
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
                Text(
                    text       = title,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = NavyDark
                )
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

// ── Outlined profile input field ───────────────────────────────────────────────
@Composable
private fun ProfileField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 13.sp) },
        placeholder   = { Text(placeholder, color = GrayMuted, fontSize = 13.sp) },
        leadingIcon   = { Icon(icon, null, tint = Teal, modifier = Modifier.size(18.dp)) },
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Teal,
            unfocusedBorderColor = TealMid,
            focusedLabelColor    = Teal,
            cursorColor          = Teal
        )
    )
}

// ── Password field ─────────────────────────────────────────────────────────────
@Composable
private fun PasswordField(
    label: String,
    value: String,
    visible: Boolean,
    onValueChange: (String) -> Unit,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        label         = { Text(label, fontSize = 13.sp) },
        leadingIcon   = {
            Icon(Icons.Outlined.Lock, null, tint = Teal, modifier = Modifier.size(18.dp))
        },
        trailingIcon  = {
            IconButton(onClick = onToggle) {
                Icon(
                    imageVector = if (visible) Icons.Default.VisibilityOff
                    else Icons.Default.Visibility,
                    contentDescription = null,
                    tint     = GrayMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None
        else PasswordVisualTransformation(),
        singleLine    = true,
        modifier      = Modifier.fillMaxWidth(),
        shape         = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = Teal,
            unfocusedBorderColor = TealMid,
            focusedLabelColor    = Teal,
            cursorColor          = Teal
        )
    )
}

// ── Feedback banner — success (teal) or error (red) ───────────────────────────
@Composable
private fun FeedbackBanner(message: String, success: Boolean) {
    val bgColor   = if (success) TealLight      else ErrorLight
    val textColor = if (success) Teal           else ErrorRed
    val icon      = if (success) Icons.Default.CheckCircleOutline
    else         Icons.Default.ErrorOutline

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, if (success) TealMid else ErrorRed.copy(alpha = 0.3f),
                RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = textColor, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(message, fontSize = 12.sp, color = textColor, fontWeight = FontWeight.Medium)
    }
}