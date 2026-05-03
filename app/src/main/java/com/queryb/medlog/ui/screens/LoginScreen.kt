package com.queryb.medlog.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
private val NavyDark   = Color(0xFF0D1B2A)
private val GrayMuted  = Color(0xFF9CA3AF)
private val GrayText   = Color(0xFF6B7280)
private val GrayLine   = Color(0xFFE0E0E0)
private val GrayDivide = Color(0xFFF3F4F6)

@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit
) {
    var isRegisterMode  by remember { mutableStateOf(authManager.isFirstLaunch) }
    var username        by remember { mutableStateOf("") }
    var password        by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmVisible  by remember { mutableStateOf(false) }
    var errorMessage    by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {

        // ── TOP — white header with logo ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.40f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(CircleShape)
                        .background(TealLight),
                    contentAlignment = Alignment.Center
                ) {
                    MedLogLogo(size = 56.dp)
                }

                Spacer(Modifier.height(14.dp))

                Text(
                    text = "MEDLOG",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 6.sp,
                    color = Teal
                )

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Your personal health tracker",
                    fontSize = 12.sp,
                    color = GrayMuted
                )
            }
        }

        // Subtle divider between header and form
        HorizontalDivider(color = GrayDivide, thickness = 1.dp)

        // ── BOTTOM — form area ────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.60f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
                .padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Heading
            Text(
                text = if (isRegisterMode) "Create Account." else "Let's Get In.",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = NavyDark,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            // ── Username ──────────────────────────────────────────────────────
            UnderlineField(
                value = username,
                onValueChange = { username = it; errorMessage = "" },
                label = "Username",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            Spacer(Modifier.height(24.dp))

            // ── Password ──────────────────────────────────────────────────────
            UnderlineField(
                value = password,
                onValueChange = { password = it; errorMessage = "" },
                label = "Password",
                keyboardType = KeyboardType.Password,
                imeAction = if (isRegisterMode) ImeAction.Next else ImeAction.Done,
                isPassword = true,
                passwordVisible = passwordVisible,
                onTogglePassword = { passwordVisible = !passwordVisible }
            )

            // ── Confirm password (register mode only) ─────────────────────────
            AnimatedVisibility(visible = isRegisterMode) {
                Column {
                    Spacer(Modifier.height(24.dp))
                    UnderlineField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it; errorMessage = "" },
                        label = "Confirm Password",
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done,
                        isPassword = true,
                        passwordVisible = confirmVisible,
                        onTogglePassword = { confirmVisible = !confirmVisible }
                    )
                }
            }

            // ── Forgot password (login mode only) ─────────────────────────────
            AnimatedVisibility(
                visible = !isRegisterMode,
                enter = fadeIn(),
                exit  = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    TextButton(
                        onClick =  onForgotPassword ,
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Text(
                            text = "Forgot Password?",
                            color = Teal,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // ── Error message ─────────────────────────────────────────────────
            AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                )
            }

            Spacer(Modifier.height(36.dp))

            // ── Primary CTA button ────────────────────────────────────────────
            Button(
                onClick = {
                    errorMessage = ""
                    if (isRegisterMode) {
                        if (password != confirmPassword) {
                            errorMessage = "Passwords do not match"
                            return@Button
                        }
                        when (authManager.registerWithResult(username, password)) {
                            AuthManager.RegisterResult.SUCCESS        -> onLoginSuccess()
                            AuthManager.RegisterResult.USERNAME_TAKEN ->
                                errorMessage = "Username \"$username\" is already taken"
                            AuthManager.RegisterResult.USERNAME_BLANK ->
                                errorMessage = "Username cannot be empty"
                            AuthManager.RegisterResult.PASSWORD_TOO_SHORT ->
                                errorMessage = "Password must be at least 4 characters"
                        }
                    } else {
                        if (authManager.login(username, password)) {
                            onLoginSuccess()
                        } else {
                            errorMessage = "Incorrect username or password"
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Teal),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = if (isRegisterMode) "Sign Up" else "Login",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── Switch register / login mode ──────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegisterMode) "Already have an account?  "
                    else "Don't have an account?  ",
                    fontSize = 14.sp,
                    color = GrayText
                )
                TextButton(
                    onClick = {
                        isRegisterMode = !isRegisterMode
                        errorMessage = ""
                        username = ""
                        password = ""
                        confirmPassword = ""
                    },
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text(
                        text = if (isRegisterMode) "LOG IN" else "SIGN UP",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Teal
                    )
                }
            }
        }
    }
}

// ── Underline input field ─────────────────────────────────────────────────────

@Composable
private fun UnderlineField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            color = GrayMuted,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.8.sp
        )

        Spacer(Modifier.height(8.dp))

        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                fontSize = 16.sp,
                color = NavyDark,
                fontWeight = FontWeight.Normal
            ),
            visualTransformation = if (isPassword && !passwordVisible)
                PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = imeAction
            ),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Enter $label",
                                fontSize = 16.sp,
                                color = Color(0xFFD1D5DB)
                            )
                        }
                        innerTextField()
                    }
                    if (isPassword && onTogglePassword != null) {
                        IconButton(
                            onClick = onTogglePassword,
                            modifier = Modifier.size(22.dp)
                        ) {
                            Icon(
                                imageVector = if (passwordVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = null,
                                tint = GrayMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        )

        // Underline — teal when filled, gray when empty
        HorizontalDivider(
            color = if (value.isNotEmpty()) Teal else GrayLine,
            thickness = if (value.isNotEmpty()) 2.dp else 1.dp
        )
    }
}