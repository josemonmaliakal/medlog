package com.queryb.medlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.queryb.medlog.data.LabResult
import com.queryb.medlog.ui.components.MedLogLogo
import com.queryb.medlog.ui.viewmodel.LabViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import java.util.Calendar
import androidx.compose.foundation.BorderStroke
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.statusBars


// ── Colors ─────────────────────────────────────────────────────────────────────
private val Teal      = Color(0xFF00897B)
private val TealLight = Color(0xFFE8F5F3)
private val TealMid   = Color(0xFFB2DFDB)
private val NavyDark  = Color(0xFF0D1B2A)
private val GrayMuted = Color(0xFF9CA3AF)
private val GrayText  = Color(0xFF6B7280)
private val ScreenBg  = Color(0xFFF8FFFE)
private val CardWhite = Color(0xFFFFFFFF)
private val ErrorRed  = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: LabViewModel,
    username: String,
    onAddClick: () -> Unit,
    onDetailClick: (String) -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit
) {
    val results by viewModel.results.collectAsState()
    val scope   = rememberCoroutineScope()

    // Right-side drawer state
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    var resultToDelete   by remember { mutableStateOf<LabResult?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showQuickAdd     by remember { mutableStateOf(false) }

    // ── Derived stats ─────────────────────────────────────────────────────────
    val latestGlucose     = results.firstOrNull { it.glucose > 0 }?.glucose
    val latestCholesterol = results.firstOrNull { it.cholesterol > 0 }?.cholesterol
    val avgGlucose        = results.filter { it.glucose > 0 }
        .map { it.glucose }.average().takeIf { !it.isNaN() }
    val avgCholesterol    = results.filter { it.cholesterol > 0 }
        .map { it.cholesterol }.average().takeIf { !it.isNaN() }

    // ── Delete dialog ─────────────────────────────────────────────────────────
    resultToDelete?.let { result ->
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            title = { Text("Delete Entry") },
            text  = { Text("Delete the entry dated ${result.date}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.delete(result); resultToDelete = null }) {
                    Text("Delete", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) { Text("Cancel") }
            }
        )
    }

    // ── Logout dialog ─────────────────────────────────────────────────────────
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

    // ── Quick add dialog ──────────────────────────────────────────────────────
    if (showQuickAdd) {
        QuickAddDialog(
            onDismiss = { showQuickAdd = false },
            onSave    = { result -> viewModel.insert(result); showQuickAdd = false }
        )
    }

    // ── RTL wrapper anchors the drawer to the RIGHT edge ─────────────────────
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState     = drawerState,
            gesturesEnabled = true,
            drawerContent   = {
                // Flip content back to LTR inside the panel
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    SidePanelContent(
                        username    = username,
                        onProfile   = {
                            scope.launch { drawerState.close() }
                            onProfileClick()
                        },
                        onDashboard = { scope.launch { drawerState.close() } },

                        onLogout    = {
                            scope.launch { drawerState.close() }
                            showLogoutDialog = true
                        }
                    )
                }
            }
        ) {
            // Flip main content back to LTR
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(TealLight),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        MedLogLogo(size = 24.dp)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            "MedLog",
                                            fontWeight = FontWeight.Bold,
                                            fontSize   = 18.sp,
                                            color      = Color.White
                                        )
                                        Text(
                                            "Hello, ${username.replaceFirstChar { it.uppercase() }} 👋",
                                            fontSize = 11.sp,
                                            color    = Color.White.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            },
                            actions = {
                                // Avatar circle — replaces MoreVert dropdown
                                Box(
                                    modifier = Modifier
                                        .padding(end = 12.dp)
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.22f))
                                        .clickable { scope.launch { drawerState.open() } },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text       = username.take(1).uppercase(),
                                        color      = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize   = 16.sp
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor    = Teal,
                                titleContentColor = Color.White
                            )
                        )
                    },
                    floatingActionButton = {
                        ExtendedFloatingActionButton(
                            onClick        = { showQuickAdd = true },
                            icon           = { Icon(Icons.Default.Add, null) },
                            text           = { Text("Log Reading") },
                            containerColor = Teal,
                            contentColor   = Color.White
                        )
                    },
                    containerColor = ScreenBg
                ) { padding ->
                    LazyColumn(
                        modifier            = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {

                        // ── Latest readings ───────────────────────────────────
                        item {
                            Text("Latest Readings",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp,
                                color      = GrayText)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    modifier   = Modifier.weight(1f),
                                    icon       = Icons.Outlined.Bloodtype,
                                    label      = "Blood Sugar",
                                    value      = latestGlucose?.let { "%.1f".format(it) } ?: "--",
                                    unit       = "mg/dL",
                                    trendColor = glucoseColor(latestGlucose),
                                    onClick    = { onDetailClick("glucose") }
                                )
                                StatCard(
                                    modifier   = Modifier.weight(1f),
                                    icon       = Icons.Outlined.Favorite,
                                    label      = "Cholesterol",
                                    value      = latestCholesterol?.let { "%.1f".format(it) } ?: "--",
                                    unit       = "mg/dL",
                                    trendColor = cholesterolColor(latestCholesterol),
                                    onClick    = { onDetailClick("cholesterol") }
                                )
                            }
                        }

                        // ── Averages ──────────────────────────────────────────
                        item {
                            Text("Averages",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp,
                                color      = GrayText)
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier              = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StatCard(
                                    modifier   = Modifier.weight(1f),
                                    icon       = Icons.Outlined.QueryStats,
                                    label      = "Avg Sugar",
                                    value      = avgGlucose?.let { "%.1f".format(it) } ?: "--",
                                    unit       = "mg/dL",
                                    trendColor = Teal
                                )
                                StatCard(
                                    modifier   = Modifier.weight(1f),
                                    icon       = Icons.Outlined.BarChart,
                                    label      = "Avg Cholesterol",
                                    value      = avgCholesterol?.let { "%.1f".format(it) } ?: "--",
                                    unit       = "mg/dL",
                                    trendColor = Teal
                                )
                            }
                        }

                        // ── History ───────────────────────────────────────────
                        item {
                            Text("History",
                                fontWeight = FontWeight.Bold,
                                fontSize   = 14.sp,
                                color      = GrayText)
                        }

                        // ── Empty state ───────────────────────────────────────
                        if (results.isEmpty()) {
                            item {
                                Box(
                                    modifier            = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 40.dp),
                                    contentAlignment    = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Outlined.Science, null,
                                            modifier = Modifier.size(64.dp),
                                            tint     = TealMid)
                                        Spacer(Modifier.height(12.dp))
                                        Text("No results yet",
                                            color      = GrayMuted,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize   = 15.sp)
                                        Text("Tap Log Reading to get started",
                                            color    = GrayMuted,
                                            fontSize = 13.sp)
                                    }
                                }
                            }
                        }

                        // ── Result rows ───────────────────────────────────────
                        items(results, key = { it.id }) { result ->
                            HistoryCard(
                                result   = result,
                                onDelete = { resultToDelete = result }
                            )
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

// ── Side panel ─────────────────────────────────────────────────────────────────
@Composable
private fun SidePanelContent(
    username: String,
    onProfile: () -> Unit,
    onDashboard: () -> Unit,
    onLogout: () -> Unit          // ← onTrends parameter removed
) {
    ModalDrawerSheet(
        drawerShape          = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        drawerContainerColor = CardWhite,
        windowInsets         = WindowInsets(0),   // ← removes the white status bar gap
        modifier             = Modifier.width(260.dp)
    ) {
        // ── Teal header — extends behind status bar ───────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Teal)
                .windowInsetsPadding(WindowInsets.statusBars)  // ← pushes content below status bar but keeps teal bg
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f))
                        .clickable { onProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = username.take(1).uppercase(),
                        color      = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize   = 20.sp
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        text       = username.replaceFirstChar { it.uppercase() },
                        color      = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 15.sp
                    )
                    Text(
                        text     = "MedLog account",
                        color    = Color.White.copy(alpha = 0.75f),
                        fontSize = 11.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ── Menu items ────────────────────────────────────────────────────────
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            PanelMenuItem(
                icon      = Icons.Default.Person,
                label     = "My Profile",
                highlight = true,
                onClick   = onProfile
            )
            PanelMenuItem(
                icon    = Icons.Default.Home,
                label   = "Dashboard",
                onClick = onDashboard
            )
            // Trends item removed
        }

        Spacer(Modifier.weight(1f))

        // ── Logout pinned to bottom ───────────────────────────────────────────
        HorizontalDivider(
            color     = TealMid,
            thickness = 1.dp,
            modifier  = Modifier.padding(horizontal = 12.dp)
        )
        Spacer(Modifier.height(8.dp))
        Box(modifier = Modifier.padding(horizontal = 12.dp)) {
            PanelMenuItem(
                icon    = Icons.Default.Logout,
                label   = "Log Out",
                isRed   = true,
                onClick = onLogout
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}
// ── Panel menu item ────────────────────────────────────────────────────────────
@Composable
private fun PanelMenuItem(
    icon: ImageVector,
    label: String,
    highlight: Boolean = false,
    isRed: Boolean = false,
    onClick: () -> Unit
) {
    val bgColor   = if (highlight) TealLight else Color.Transparent
    val iconColor = when { isRed -> ErrorRed; highlight -> Teal; else -> GrayText }
    val textColor = when { isRed -> ErrorRed; highlight -> Teal; else -> NavyDark }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null,
            tint     = iconColor,
            modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text       = label,
            fontSize   = 14.sp,
            fontWeight = if (highlight || isRed) FontWeight.SemiBold else FontWeight.Normal,
            color      = textColor
        )
    }
}

// ── Stat card ──────────────────────────────────────────────────────────────────
@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    trendColor: Color,
    onClick: (() -> Unit)? = null
) {
    Card(
        onClick   = { onClick?.invoke() },
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(
            width = if (onClick != null) 1.5.dp else 1.dp,
            color = TealMid
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
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
                Spacer(Modifier.width(8.dp))
                Text(label, fontSize = 11.sp, color = GrayMuted,
                    fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(10.dp))
            Text(value,
                fontSize   = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color      = trendColor)
            Text(unit, fontSize = 11.sp, color = GrayMuted)
        }
    }
}

// ── History card ───────────────────────────────────────────────────────────────
@Composable
private fun HistoryCard(result: LabResult, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    val extraValues = buildList {
        if (result.hdl > 0)           add("HDL: ${result.hdl} mg/dL")
        if (result.ldl > 0)           add("LDL: ${result.ldl} mg/dL")
        if (result.triglycerides > 0) add("Trig: ${result.triglycerides} mg/dL")
        if (result.hemoglobin > 0)    add("Hgb: ${result.hemoglobin} g/dL")
    }
    val hasExtras = extraValues.isNotEmpty()

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(
            width = if (expanded) 1.5.dp else 1.dp,
            color = if (expanded) Teal else TealMid
        )
    ) {
        Column {
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // ── Date / time / badge column ────────────────────────────────
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier            = Modifier.width(82.dp)
                ) {
                    Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            result.date,
                            color      = Teal,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 10.sp,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (result.time.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(Icons.Outlined.AccessTime, null,
                                tint     = GrayMuted,
                                modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(3.dp))
                            Text(result.time, fontSize = 10.sp, color = GrayMuted)
                        }
                    }
                    if (result.readingType.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        val isFasting = result.readingType == "Fasting"
                        Surface(
                            color = if (isFasting) Color(0xFFFFF3E0) else TealLight,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                result.readingType.uppercase(),
                                fontSize   = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (isFasting) Color(0xFFF57C00) else Teal,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                // ── Values column ─────────────────────────────────────────────
                Column(modifier = Modifier.weight(1f)) {
                    if (result.glucose > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Bloodtype, null,
                                tint     = glucoseColor(result.glucose),
                                modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Sugar: ", fontSize = 12.sp, color = GrayText)
                            Text("${result.glucose} mg/dL",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = glucoseColor(result.glucose))
                        }
                        Spacer(Modifier.height(3.dp))
                    }
                    if (result.cholesterol > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Favorite, null,
                                tint     = cholesterolColor(result.cholesterol),
                                modifier = Modifier.size(12.dp))
                            Spacer(Modifier.width(5.dp))
                            Text("Chol: ", fontSize = 12.sp, color = GrayText)
                            Text("${result.cholesterol} mg/dL",
                                fontSize   = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color      = cholesterolColor(result.cholesterol))
                        }
                        Spacer(Modifier.height(3.dp))
                    }

                    if (!hasExtras && result.pdfUri.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, null,
                                tint     = Teal,
                                modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF attached", fontSize = 10.sp, color = Teal)
                        }
                    }

                    if (expanded && hasExtras) {
                        Spacer(Modifier.height(6.dp))
                        HorizontalDivider(color = TealMid.copy(alpha = 0.5f))
                        Spacer(Modifier.height(6.dp))
                        extraValues.forEach { v ->
                            Text(v, fontSize = 11.sp, color = GrayText, lineHeight = 17.sp)
                        }
                        if (result.pdfUri.isNotEmpty()) {
                            Spacer(Modifier.height(3.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PictureAsPdf, null,
                                    tint     = Teal,
                                    modifier = Modifier.size(10.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("PDF attached", fontSize = 10.sp, color = Teal)
                            }
                        }
                    }

                    if (!expanded && hasExtras) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "+${extraValues.size} more value${if (extraValues.size > 1) "s" else ""}",
                            fontSize = 11.sp,
                            color    = GrayMuted
                        )
                    }
                }

                // ── Delete ────────────────────────────────────────────────────
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.DeleteOutline, null,
                        tint     = ErrorRed.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp))
                }
            }

            // ── Expand footer (only when extras exist) ────────────────────────
            if (hasExtras) {
                HorizontalDivider(color = TealLight)
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .clickable { expanded = !expanded }
                        .background(ScreenBg)
                        .padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = if (expanded) "Show less" else "Show all values",
                        fontSize   = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = Teal
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector        = if (expanded) Icons.Default.KeyboardArrowUp
                        else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint               = Teal,
                        modifier           = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ── Quick Add Dialog ───────────────────────────────────────────────────────────
@Composable
private fun QuickAddDialog(
    onDismiss: () -> Unit,
    onSave: (LabResult) -> Unit
) {
    val context = LocalContext.current
    val cal     = Calendar.getInstance()

    var selectedTab by remember { mutableStateOf(0) }
    var sugarValue  by remember { mutableStateOf("") }
    var readingType by remember { mutableStateOf("Fasting") }
    var sugarDate   by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }
    var sugarTime   by remember {
        mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
    }
    var cholValue   by remember { mutableStateOf("") }
    var cholDate    by remember {
        mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")))
    }
    var cholTime    by remember {
        mutableStateOf(LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm")))
    }
    var errorMsg    by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape     = RoundedCornerShape(24.dp),
            colors    = CardDefaults.cardColors(containerColor = CardWhite),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Text("Log Reading",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize   = 20.sp,
                    color      = NavyDark)

                Spacer(Modifier.height(16.dp))

                // Toggle tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(TealLight)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf("Blood Sugar", "Cholesterol").forEachIndexed { index, label ->
                        val active = index == selectedTab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) Teal else Color.Transparent)
                                .clickable { selectedTab = index; errorMsg = "" }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(label,
                                fontSize   = 13.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color      = if (active) Color.White else GrayText)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Blood Sugar tab
                if (selectedTab == 0) {
                    DialogField("Blood Sugar (mg/dL)", sugarValue,
                        { sugarValue = it; errorMsg = "" },
                        KeyboardType.Decimal, "e.g. 95")

                    Spacer(Modifier.height(14.dp))

                    Text("Reading Type", fontSize = 12.sp,
                        color = GrayMuted, fontWeight = FontWeight.Medium)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Fasting", "After Food").forEach { type ->
                            FilterChip(
                                selected = readingType == type,
                                onClick  = { readingType = type },
                                label    = { Text(type, fontSize = 12.sp) },
                                colors   = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Teal,
                                    selectedLabelColor     = Color.White
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(14.dp))
                    DateTimeRow(sugarDate, sugarTime, context, cal,
                        { sugarDate = it }, { sugarTime = it })
                }

                // Cholesterol tab
                if (selectedTab == 1) {
                    DialogField("Cholesterol (mg/dL)", cholValue,
                        { cholValue = it; errorMsg = "" },
                        KeyboardType.Decimal, "e.g. 180")

                    Spacer(Modifier.height(14.dp))
                    DateTimeRow(cholDate, cholTime, context, cal,
                        { cholDate = it }, { cholTime = it })
                }

                if (errorMsg.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMsg, color = ErrorRed, fontSize = 12.sp)
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (selectedTab == 0) {
                            val v = sugarValue.toFloatOrNull()
                            if (v == null || v <= 0f) {
                                errorMsg = "Enter a valid blood sugar value"
                                return@Button
                            }
                            onSave(LabResult(
                                date        = sugarDate,
                                time        = sugarTime,
                                readingType = readingType,
                                glucose     = v
                            ))
                        } else {
                            val v = cholValue.toFloatOrNull()
                            if (v == null || v <= 0f) {
                                errorMsg = "Enter a valid cholesterol value"
                                return@Button
                            }
                            onSave(LabResult(
                                date        = cholDate,
                                time        = cholTime,
                                cholesterol = v
                            ))
                        }
                    },
                    modifier  = Modifier.fillMaxWidth().height(50.dp),
                    shape     = RoundedCornerShape(14.dp),
                    colors    = ButtonDefaults.buttonColors(containerColor = Teal),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Text("Save Reading", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Spacer(Modifier.height(8.dp))

                TextButton(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                    Text("Cancel", color = GrayMuted)
                }
            }
        }
    }
}

// ── Date + time row ────────────────────────────────────────────────────────────
@Composable
private fun DateTimeRow(
    date: String,
    time: String,
    context: android.content.Context,
    cal: Calendar,
    onDate: (String) -> Unit,
    onTime: (String) -> Unit
) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        OutlinedButton(
            onClick = {
                DatePickerDialog(context, { _, y, m, d ->
                    onDate("$y-${(m+1).toString().padStart(2,'0')}-${d.toString().padStart(2,'0')}")
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.weight(1f),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Teal),
            border   = BorderStroke(1.dp, TealMid)
        ) {
            Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(date, fontSize = 11.sp)
        }

        OutlinedButton(
            onClick = {
                TimePickerDialog(context, { _, h, m ->
                    onTime("${h.toString().padStart(2,'0')}:${m.toString().padStart(2,'0')}")
                }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
            },
            modifier = Modifier.weight(1f),
            shape    = RoundedCornerShape(10.dp),
            colors   = ButtonDefaults.outlinedButtonColors(contentColor = Teal),
            border   = BorderStroke(1.dp, TealMid)
        ) {
            Icon(Icons.Outlined.AccessTime, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(time, fontSize = 11.sp)
        }
    }
}

// ── Dialog field ───────────────────────────────────────────────────────────────
@Composable
private fun DialogField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType,
    placeholder: String
) {
    Column {
        Text(label, fontSize = 12.sp, color = GrayMuted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value           = value,
            onValueChange   = onValueChange,
            placeholder     = { Text(placeholder, color = GrayMuted, fontSize = 14.sp) },
            singleLine      = true,
            modifier        = Modifier.fillMaxWidth(),
            shape           = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors          = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = Teal,
                unfocusedBorderColor = TealMid
            )
        )
    }
}

// ── Color helpers ──────────────────────────────────────────────────────────────
private fun glucoseColor(value: Float?): Color {
    value ?: return Color(0xFF9CA3AF)
    return when {
        value < 70   -> Color(0xFF1E88E5)
        value <= 99  -> Color(0xFF00897B)
        value <= 125 -> Color(0xFFF57C00)
        else         -> Color(0xFFD32F2F)
    }
}

private fun cholesterolColor(value: Float?): Color {
    value ?: return Color(0xFF9CA3AF)
    return when {
        value < 200  -> Color(0xFF00897B)
        value <= 239 -> Color(0xFFF57C00)
        else         -> Color(0xFFD32F2F)
    }
}