package com.queryb.medlog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queryb.medlog.data.LabResult
import com.queryb.medlog.ui.components.MedLogLogo
import com.queryb.medlog.ui.theme.*
import com.queryb.medlog.ui.viewmodel.LabViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LabViewModel,
    username: String,
    onAddClick: () -> Unit,
    onChartClick: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    val results by viewModel.results.collectAsState()
    var resultToDelete by remember { mutableStateOf<LabResult?>(null) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    resultToDelete?.let { result ->
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            title = { Text("Delete Entry") },
            text  = { Text("Delete the entry dated ${result.date}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(result)
                    resultToDelete = null
                }) { Text("Delete", color = ErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) { Text("Cancel") }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon  = { Icon(Icons.Default.Logout, contentDescription = null, tint = ErrorRed) },
            title = { Text("Log Out") },
            text  = { Text("Are you sure you want to log out?") },
            confirmButton = {
                Button(
                    onClick = {
                        showLogoutDialog = false
                        onLogout()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                ) { Text("Log Out") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    // ── Scaffold ──────────────────────────────────────────────────────────────

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        MedLogLogo(size = 36.dp)
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "MedLog",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = Color.White
                            )
                            Text(
                                "Hello, ${username.replaceFirstChar { it.uppercase() }} 👋",
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                },
                actions = {
                    // Chart / History button
                    IconButton(onClick = onChartClick) {
                        Icon(
                            Icons.Default.ShowChart,
                            contentDescription = "View Trends",
                            tint = Color.White
                        )
                    }

                    // ⋮ Overflow menu
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = menuExpanded,
                            onDismissRequest = { menuExpanded = false }
                        ) {
                            // Header
                            Column(
                                modifier = Modifier.padding(
                                    horizontal = 16.dp, vertical = 8.dp
                                )
                            ) {
                                Text(
                                    username.replaceFirstChar { it.uppercase() },
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = TextDark
                                )
                                Text("MedLog account", fontSize = 11.sp, color = TextMuted)
                            }

                            HorizontalDivider()

                            // Profile
                            DropdownMenuItem(
                                text = { Text("My Profile") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null,
                                        tint = MedBlue)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onProfileClick()
                                }
                            )

                            // History (same as chart button)
                            DropdownMenuItem(
                                text = { Text("Health Trends") },
                                leadingIcon = {
                                    Icon(Icons.Default.ShowChart, contentDescription = null,
                                        tint = MedBlue)
                                },
                                onClick = {
                                    menuExpanded = false
                                    onChartClick()
                                }
                            )

                            HorizontalDivider()

                            // Logout
                            DropdownMenuItem(
                                text = { Text("Log Out", color = ErrorRed) },
                                leadingIcon = {
                                    Icon(Icons.Default.Logout, contentDescription = null,
                                        tint = ErrorRed)
                                },
                                onClick = {
                                    menuExpanded = false
                                    showLogoutDialog = true
                                }
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MedBlue,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Result") },
                containerColor = MedBlue,
                contentColor = Color.White
            )
        },
        containerColor = SurfaceWhite
    ) { padding ->
        if (results.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Science,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = MedBlueLight
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "No lab results yet",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextMuted
                    )
                    Text(
                        "Tap + to add your first result",
                        color = TextMuted,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        "${results.size} Result${if (results.size != 1) "s" else ""}",
                        color = TextMuted,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
                items(results, key = { it.id }) { result ->
                    LabResultCard(
                        result = result,
                        onDelete = { resultToDelete = result }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun LabResultCard(result: LabResult, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MedBlue.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = result.date,
                    color = MedBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                val chips = buildList {
                    if (result.glucose > 0)       add("Glucose: ${result.glucose}")
                    if (result.cholesterol > 0)   add("Chol: ${result.cholesterol}")
                    if (result.hdl > 0)           add("HDL: ${result.hdl}")
                    if (result.ldl > 0)           add("LDL: ${result.ldl}")
                    if (result.triglycerides > 0) add("Trig: ${result.triglycerides}")
                    if (result.hemoglobin > 0)    add("Hgb: ${result.hemoglobin}")
                }
                chips.forEach { Text(it, fontSize = 13.sp, color = TextDark) }
                if (result.pdfUri.isNotEmpty()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.PictureAsPdf, null,
                            tint = MedGreen, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("PDF attached", fontSize = 11.sp, color = MedGreen)
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, "Delete",
                    tint = ErrorRed.copy(alpha = 0.7f))
            }
        }
    }
}
