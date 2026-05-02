package com.queryb.medlog.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Badge
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queryb.medlog.auth.AuthManager
import com.queryb.medlog.ui.theme.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    authManager: AuthManager,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    // ── State ─────────────────────────────────────────────────────────────────
    var displayName     by remember { mutableStateOf(authManager.getDisplayName()) }
    var email           by remember { mutableStateOf(authManager.getEmail()) }
    var currentPassword by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmNew      by remember { mutableStateOf("") }

    var showCurrentPw  by remember { mutableStateOf(false) }
    var showNewPw      by remember { mutableStateOf(false) }
    var showConfirmPw  by remember { mutableStateOf(false) }

    var profileMsg     by remember { mutableStateOf("") }
    var profileSuccess by remember { mutableStateOf(false) }
    var passwordMsg    by remember { mutableStateOf("") }
    var passwordSuccess by remember { mutableStateOf(false) }

    var showLogoutDialog by remember { mutableStateOf(false) }

    // ── Logout Dialog ─────────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon  = { Icon(Icons.Default.Logout, null, tint = ErrorRed) },
            title = { Text("Log Out") },
            text  = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors  = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Profile", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MedBlue,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = SurfaceWhite
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // ── Avatar ────────────────────────────────────────────────────────
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(CircleShape)
                            .background(MedBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (authManager.getDisplayName().ifEmpty {
                                authManager.getUsername()
                            }).take(1).uppercase(),
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        authManager.getUsername(),
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = TextDark
                    )
                    Text(
                        "MedLog account",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // ── Profile Info Card ─────────────────────────────────────────────
            ProfileCard(title = "Personal Information", icon = Icons.Default.Person) {

                OutlinedTextField(
                    value = displayName,
                    onValueChange = { displayName = it; profileMsg = "" },
                    label = { Text("Display Name") },
                    leadingIcon = { Icon(Icons.Outlined.Badge, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(Modifier.height(10.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it; profileMsg = "" },
                    label = { Text("Email (optional)") },
                    leadingIcon = { Icon(Icons.Default.Email, null) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Feedback
                AnimatedVisibility(profileMsg.isNotEmpty()) {
                    Text(
                        profileMsg,
                        color = if (profileSuccess) MedGreen else ErrorRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        val ok = authManager.updateDisplayName(displayName)
                        authManager.updateEmail(email)
                        profileSuccess = ok
                        profileMsg = if (ok) "✅ Profile updated!" else "Display name cannot be empty"
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBlue)
                ) {
                    Icon(Icons.Default.Save, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Save Changes")
                }
            }

            // ── Change Password Card ──────────────────────────────────────────
            ProfileCard(title = "Change Password", icon = Icons.Default.Lock) {

                PasswordField(
                    value = currentPassword,
                    onValueChange = { currentPassword = it; passwordMsg = "" },
                    label = "Current Password",
                    visible = showCurrentPw,
                    onToggle = { showCurrentPw = !showCurrentPw }
                )

                Spacer(Modifier.height(10.dp))

                PasswordField(
                    value = newPassword,
                    onValueChange = { newPassword = it; passwordMsg = "" },
                    label = "New Password",
                    visible = showNewPw,
                    onToggle = { showNewPw = !showNewPw }
                )

                Spacer(Modifier.height(10.dp))

                PasswordField(
                    value = confirmNew,
                    onValueChange = { confirmNew = it; passwordMsg = "" },
                    label = "Confirm New Password",
                    visible = showConfirmPw,
                    onToggle = { showConfirmPw = !showConfirmPw }
                )

                // Feedback
                AnimatedVisibility(passwordMsg.isNotEmpty()) {
                    Text(
                        passwordMsg,
                        color = if (passwordSuccess) MedGreen else ErrorRed,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        passwordMsg = when {
                            currentPassword.isBlank() ->
                                "Enter your current password"
                            newPassword.length < 4 ->
                                "New password must be at least 4 characters"
                            newPassword != confirmNew ->
                                "New passwords do not match"
                            else -> {
                                val ok = authManager.updatePassword(currentPassword, newPassword)
                                passwordSuccess = ok
                                if (ok) {
                                    currentPassword = ""; newPassword = ""; confirmNew = ""
                                    "✅ Password changed successfully!"
                                } else {
                                    "❌ Current password is incorrect"
                                }
                            }
                        }
                        if (passwordMsg.startsWith("Enter") ||
                            passwordMsg.startsWith("New password") ||
                            passwordMsg.startsWith("New passwords")) {
                            passwordSuccess = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MedBlue)
                ) {
                    Icon(Icons.Default.LockReset, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Update Password")
                }
            }

            // ── Account Card ──────────────────────────────────────────────────
            ProfileCard(title = "Account", icon = Icons.Default.ManageAccounts) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Username", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(authManager.getUsername(), color = TextMuted, fontSize = 13.sp)
                    }
                    Surface(
                        color = MedBlue.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "Cannot be changed",
                            fontSize = 10.sp,
                            color = MedBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                OutlinedButton(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ErrorRed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, ErrorRed)
                ) {
                    Icon(Icons.Default.Logout, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

// ── Helper Composables ────────────────────────────────────────────────────────

@Composable
private fun ProfileCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MedBlue,
                    modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, color = TextDark)
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
            content()
        }
    }
}

@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    visible: Boolean,
    onToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, null) },
        trailingIcon = {
            IconButton(onClick = onToggle) {
                Icon(
                    if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null
                )
            }
        },
        visualTransformation = if (visible) VisualTransformation.None
        else PasswordVisualTransformation(),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        shape = RoundedCornerShape(12.dp)
    )
}
