package com.queryb.medlog.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queryb.medlog.auth.AuthManager
import com.queryb.medlog.ui.components.MedLogLogo

private val Teal       = Color(0xFF00897B)
private val TealLight  = Color(0xFFE8F5F3)
private val TealMid    = Color(0xFFB2DFDB)
private val NavyDark   = Color(0xFF0D1B2A)
private val GrayMuted  = Color(0xFF9CA3AF)
private val GrayLine   = Color(0xFFE0E0E0)
private val CardWhite  = Color(0xFFFFFFFF)
private val ErrorRed   = Color(0xFFD32F2F)
private val SuccessGreen = Color(0xFF2E7D32)
private val SuccessLight = Color(0xFFE8F5E9)

// Steps in the reset flow
private enum class ResetStep { USERNAME, NEW_PASSWORD, DONE }

@Composable
fun ForgotPasswordScreen(
    authManager: AuthManager,
    onBack: () -> Unit,
    onSuccess: () -> Unit
) {
    var step            by remember { mutableStateOf(ResetStep.USERNAME) }
    var username        by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showNewPw       by remember { mutableStateOf(false) }
    var showConfirmPw   by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // ── TOP — same white header style as login ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.38f)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(TealLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (step) {
                            ResetStep.DONE -> Icons.Default.CheckCircle
                            else           -> Icons.Outlined.LockReset
                        },
                        contentDescription = null,
                        tint     = Teal,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = when (step) {
                        ResetStep.USERNAME     -> "FORGOT PASSWORD"
                        ResetStep.NEW_PASSWORD -> "SET NEW PASSWORD"
                        ResetStep.DONE         -> "ALL DONE!"
                    },
                    fontSize   = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 4.sp,
                    color = Teal
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = when (step) {
                        ResetStep.USERNAME     -> "Enter your username to continue"
                        ResetStep.NEW_PASSWORD -> "Choose a new password for @$username"
                        ResetStep.DONE         -> "Your password has been updated"
                    },
                    fontSize = 12.sp,
                    color    = GrayMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        // Divider
        HorizontalDivider(color = Color(0xFFF3F4F6), thickness = 1.dp)

        // ── BOTTOM — white form ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.62f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── Step 1: Username ──────────────────────────────────────────────
            AnimatedVisibility(
                visible = step == ResetStep.USERNAME,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "Find your account.",
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = NavyDark
                    )

                    Spacer(Modifier.height(28.dp))

                    ResetUnderlineField(
                        value         = username,
                        onValueChange = { username = it; errorMessage = "" },
                        label         = "USERNAME",
                        imeAction     = ImeAction.Done
                    )

                    AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                        Text(
                            text      = errorMessage,
                            color     = ErrorRed,
                            fontSize  = 13.sp,
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            errorMessage = ""
                            when {
                                username.isBlank() ->
                                    errorMessage = "Please enter your username"
                                !authManager.usernameExists(username) ->
                                    errorMessage = "No account found for \"${username.trim()}\""
                                else -> step = ResetStep.NEW_PASSWORD
                            }
                        },
                        modifier  = Modifier.fillMaxWidth().height(54.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Teal),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Continue", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null)
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick  = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ArrowBack, null,
                            tint     = GrayMuted,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Back to login", color = GrayMuted, fontSize = 14.sp)
                    }
                }
            }

            // ── Step 2: New password ──────────────────────────────────────────
            AnimatedVisibility(
                visible = step == ResetStep.NEW_PASSWORD,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "New password.",
                        fontSize   = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color      = NavyDark
                    )

                    Spacer(Modifier.height(28.dp))

                    ResetUnderlineField(
                        value         = newPassword,
                        onValueChange = { newPassword = it; errorMessage = "" },
                        label         = "NEW PASSWORD",
                        imeAction     = ImeAction.Next,
                        isPassword    = true,
                        visible       = showNewPw,
                        onToggle      = { showNewPw = !showNewPw }
                    )

                    Spacer(Modifier.height(24.dp))

                    ResetUnderlineField(
                        value         = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = "" },
                        label         = "CONFIRM PASSWORD",
                        imeAction     = ImeAction.Done,
                        isPassword    = true,
                        visible       = showConfirmPw,
                        onToggle      = { showConfirmPw = !showConfirmPw }
                    )

                    AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                        Text(
                            text      = errorMessage,
                            color     = ErrorRed,
                            fontSize  = 13.sp,
                            modifier  = Modifier
                                .fillMaxWidth()
                                .padding(top = 10.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            errorMessage = ""
                            when {
                                newPassword.length < 4    ->
                                    errorMessage = "Password must be at least 4 characters"
                                newPassword != confirmPassword ->
                                    errorMessage = "Passwords do not match"
                                else -> {
                                    val ok = authManager.resetPassword(username, newPassword)
                                    if (ok) step = ResetStep.DONE
                                    else errorMessage = "Something went wrong. Try again."
                                }
                            }
                        },
                        modifier  = Modifier.fillMaxWidth().height(54.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Teal),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Reset Password", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }

                    Spacer(Modifier.height(16.dp))

                    TextButton(
                        onClick  = { step = ResetStep.USERNAME; errorMessage = "" },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ArrowBack, null,
                            tint     = GrayMuted,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Back", color = GrayMuted, fontSize = 14.sp)
                    }
                }
            }

            // ── Step 3: Done ──────────────────────────────────────────────────
            AnimatedVisibility(
                visible = step == ResetStep.DONE,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Column(
                    modifier            = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(Modifier.height(8.dp))

                    // Success banner
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(SuccessLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier            = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                null,
                                tint     = SuccessGreen,
                                modifier = Modifier.size(40.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                "Password updated!",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 16.sp,
                                color      = SuccessGreen
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "You can now log in with your new password.",
                                fontSize  = 13.sp,
                                color     = SuccessGreen.copy(alpha = 0.75f),
                                textAlign = TextAlign.Center,
                                lineHeight = 19.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick   = onSuccess,
                        modifier  = Modifier.fillMaxWidth().height(54.dp),
                        shape     = RoundedCornerShape(14.dp),
                        colors    = ButtonDefaults.buttonColors(containerColor = Teal),
                        elevation = ButtonDefaults.buttonElevation(0.dp)
                    ) {
                        Text("Back to Login", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

// ── Underline field — same style as LoginScreen ────────────────────────────────
@Composable
private fun ResetUnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    visible: Boolean = false,
    onToggle: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text          = label,
            fontSize      = 11.sp,
            color         = GrayMuted,
            fontWeight    = FontWeight.Medium,
            letterSpacing = 0.8.sp
        )
        Spacer(Modifier.height(8.dp))
        BasicTextField(
            value               = value,
            onValueChange       = onValueChange,
            singleLine          = true,
            textStyle           = TextStyle(
                fontSize   = 16.sp,
                color      = NavyDark,
                fontWeight = FontWeight.Normal
            ),
            visualTransformation = if (isPassword && !visible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions      = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else KeyboardType.Text,
                imeAction    = imeAction
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text     = "Enter ${label.lowercase().replaceFirstChar { it.uppercase() }}",
                                fontSize = 16.sp,
                                color    = Color(0xFFD1D5DB)
                            )
                        }
                        innerTextField()
                    }
                    if (isPassword && onToggle != null) {
                        IconButton(
                            onClick  = onToggle,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = if (visible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null,
                                tint     = GrayMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        HorizontalDivider(
            color     = if (value.isNotEmpty()) Teal else GrayLine,
            thickness = if (value.isNotEmpty()) 2.dp else 1.dp
        )
    }
}