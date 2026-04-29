package com.queryb.medlog.ui.screens


import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queryb.medlog.auth.AuthManager
import com.queryb.medlog.ui.theme.*
import com.queryb.medlog.ui.components.MedLogLogo

@Composable
fun LoginScreen(
    authManager: AuthManager,
    onLoginSuccess: () -> Unit
) {
    var isRegisterMode by remember { mutableStateOf(authManager.isFirstLaunch) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(MedBlueDark, MedBlue, MedBlueLight)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo area
//            Icon(
//                imageVector = Icons.Default.MonitorHeart,
//                contentDescription = null,
//                tint = androidx.compose.ui.graphics.Color.White,
//                modifier = Modifier.size(72.dp)
//            )
            MedLogLogo(size = 80.dp)

            Spacer(Modifier.height(12.dp))

//            Text(
//                text = "MedLog",
//                color = androidx.compose.ui.graphics.Color.White,
//                fontSize = 36.sp,
//                fontWeight = FontWeight.Bold
//            )
            Text(
                text = "Your personal health tracker",
                color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp
            )

            Spacer(Modifier.height(40.dp))

            // Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (isRegisterMode) "Create Account" else "Welcome Back",
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextDark
                    )

                    Spacer(Modifier.height(24.dp))

                    // Username
                    OutlinedTextField(
                        value = username,
                        onValueChange = { username = it; errorMessage = "" },
                        label = { Text("Username") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = "" },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Confirm password (register only)
                    AnimatedVisibility(visible = isRegisterMode) {
                        Column {
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it; errorMessage = "" },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Default.LockOpen, null) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // Error
                    AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(Modifier.height(24.dp))

                    // Primary button
                    Button(
                        onClick = {
                            errorMessage = ""
                            if (isRegisterMode) {
                                when {
                                    username.isBlank() ->
                                        errorMessage = "Username cannot be empty"
                                    password.length < 4 ->
                                        errorMessage = "Password must be at least 4 characters"
                                    password != confirmPassword ->
                                        errorMessage = "Passwords do not match"
                                    else -> {
                                        when (authManager.registerWithResult(username, password)) {
                                            AuthManager.RegisterResult.SUCCESS ->
                                                onLoginSuccess()
                                            AuthManager.RegisterResult.USERNAME_TAKEN ->
                                                errorMessage = "Username \"$username\" is already taken"
                                            AuthManager.RegisterResult.USERNAME_BLANK ->
                                                errorMessage = "Username cannot be empty"
                                            AuthManager.RegisterResult.PASSWORD_TOO_SHORT ->
                                                errorMessage = "Password must be at least 4 characters"
                                        }
                                    }
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
                            .height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MedBlue)
                    ) {
                        Text(
                            text = if (isRegisterMode) "Create Account" else "Log In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Switch mode
                    TextButton(onClick = {
                        isRegisterMode = !isRegisterMode
                        errorMessage = ""
                    }) {
                        Text(
                            text = if (isRegisterMode)
                                "Already have an account? Log in"
                            else
                                "First time? Create an account",
                            color = MedBlue
                        )
                    }
                }
            }
        }
    }
}
