package com.queryb.medlog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Logout

private val Teal      = Color(0xFF00897B)
private val TealLight = Color(0xFFE8F5F3)
private val TealMid   = Color(0xFFB2DFDB)
private val NavyDark  = Color(0xFF0D1B2A)
private val GrayMuted = Color(0xFF9CA3AF)
private val CardWhite = Color(0xFFFFFFFF)
private val ErrorRed  = Color(0xFFD32F2F)
private val ErrorLight = Color(0xFFFFEBEE)

// ── Generic themed dialog ──────────────────────────────────────────────────────
@Composable
fun ThemedDialog(
    icon: ImageVector,
    iconTint: Color,
    iconBg: Color,
    title: String,
    message: String,
    confirmLabel: String,
    confirmColor: Color,
    dismissLabel: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape     = RoundedCornerShape(24.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier            = Modifier.padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon badge
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector        = icon,
                        contentDescription = null,
                        tint               = iconTint,
                        modifier           = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text(
                    text       = title,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color      = NavyDark,
                    textAlign  = TextAlign.Center
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text      = message,
                    fontSize  = 13.sp,
                    color     = GrayMuted,
                    textAlign = TextAlign.Center,
                    lineHeight = 19.sp
                )

                Spacer(Modifier.height(28.dp))

                // Confirm button
                Button(
                    onClick   = onConfirm,
                    modifier  = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = confirmColor),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text(
                        confirmLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                }

                Spacer(Modifier.height(10.dp))

                // Dismiss text button
                TextButton(
                    onClick  = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        dismissLabel,
                        color    = GrayMuted,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

// ── Delete confirmation dialog ─────────────────────────────────────────────────
@Composable
fun DeleteConfirmDialog(
    date: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ThemedDialog(
        icon          = androidx.compose.material.icons.Icons.Default.DeleteOutline,
        iconTint      = ErrorRed,
        iconBg        = ErrorLight,
        title         = "Delete Entry",
        message       = "Remove the record from $date?\nThis cannot be undone.",
        confirmLabel  = "Delete",
        confirmColor  = ErrorRed,
        onConfirm     = onConfirm,
        onDismiss     = onDismiss
    )
}

// ── Logout confirmation dialog ─────────────────────────────────────────────────
@Composable
fun LogoutConfirmDialog(
    username: String = "",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = if (username.isNotEmpty())
        "You're signed in as $username.\nYou'll need your password to sign back in."
    else
        "You'll need your password to sign back in."

    ThemedDialog(
        icon          = androidx.compose.material.icons.Icons.Default.Logout,
        iconTint      = Teal,
        iconBg        = TealLight,
        title         = "Log Out?",
        message       = message,
        confirmLabel  = "Log Out",
        confirmColor  = Teal,
        onConfirm     = onConfirm,
        onDismiss     = onDismiss
    )
}