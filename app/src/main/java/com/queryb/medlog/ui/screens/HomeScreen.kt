package com.queryb.medlog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.queryb.medlog.ui.utils.glucoseColor
import com.queryb.medlog.ui.utils.cholesterolColor
import com.queryb.medlog.ui.components.DeleteConfirmDialog
import com.queryb.medlog.ui.components.LogoutConfirmDialog

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
    onHistoryClick: () -> Unit,           // ← NEW
    onLogout: () -> Unit
) {
    val results by viewModel.results.collectAsState()
    val scope   = rememberCoroutineScope()
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

    // Only show the 3 most recent entries in the dashboard card
    val recentResults = results.take(6)

    // ── Dialogs ───────────────────────────────────────────────────────────────
    resultToDelete?.let { result ->
        DeleteConfirmDialog(
            date      = result.date,
            onConfirm = { viewModel.delete(result); resultToDelete = null },
            onDismiss = { resultToDelete = null }
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmDialog(
            username  = username,
            onConfirm = { showLogoutDialog = false; onLogout() },
            onDismiss = { showLogoutDialog = false }
        )
    }

    if (showQuickAdd) {
        QuickAddDialog(
            onDismiss = { showQuickAdd = false },
            onSave    = { result -> viewModel.insert(result); showQuickAdd = false }
        )
    }

    // ── Right-side drawer ─────────────────────────────────────────────────────
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalNavigationDrawer(
            drawerState     = drawerState,
            gesturesEnabled = true,
            drawerContent   = {
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
                            SectionLabel("Latest Readings")
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
                            SectionLabel("Averages")
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

                        // ── Recent history card ───────────────────────────────
                        item {
                            SectionLabel("Recent History")
                            Spacer(Modifier.height(8.dp))
                            RecentHistoryCard(
                                results         = recentResults,
                                totalCount      = results.size,
                                onSeeAll        = onHistoryClick,
                                onDeleteResult  = { resultToDelete = it }
                            )
                        }

                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }
    }
}

// ── Compact recent history card shown on dashboard ─────────────────────────────
@Composable
private fun RecentHistoryCard(
    results: List<LabResult>,
    totalCount: Int,
    onSeeAll: () -> Unit,
    onDeleteResult: (LabResult) -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(1.dp, TealMid)
    ) {
        Column {
            if (results.isEmpty()) {
                Box(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment    = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Science, null,
                            modifier = Modifier.size(40.dp), tint = TealMid)
                        Spacer(Modifier.height(8.dp))
                        Text("No readings yet", color = GrayMuted,
                            fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Tap Log Reading to get started",
                            color = GrayMuted, fontSize = 11.sp)
                    }
                }
            } else {
                results.forEachIndexed { index, result ->
                    CompactHistoryRow(result = result, onDelete = { onDeleteResult(result) })
                    if (index < results.lastIndex) {
                        HorizontalDivider(
                            color     = TealLight,
                            thickness = 1.dp
                        )
                    }
                }
            }

            // ── See all footer ────────────────────────────────────────────────
            if (totalCount > 0) {
                HorizontalDivider(color = TealLight, thickness = 1.dp)
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .clickable { onSeeAll() }
                        .background(ScreenBg)
                        .padding(vertical = 11.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = if (totalCount > 3)
                            "See all $totalCount entries"
                        else
                            "View full history",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Teal
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.ChevronRight, null,
                        tint     = Teal,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

// ── Single slim row inside the dashboard history card ─────────────────────────
@Composable
private fun CompactHistoryRow(result: LabResult, onDelete: () -> Unit) {
    Row(
        modifier          = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date badge
        Surface(color = TealLight, shape = RoundedCornerShape(6.dp)) {
            Text(
                // Show "Apr 28" style short date
                text = formatShortDate(result.date),
                color      = Teal,
                fontWeight = FontWeight.Bold,
                fontSize   = 9.sp,
                modifier   = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
            )
        }

        Spacer(Modifier.width(10.dp))

        // Summary + meta
        Column(modifier = Modifier.weight(1f)) {
            val parts = buildList {
                if (result.glucose > 0)     add("Sugar: ${"%.0f".format(result.glucose)}")
                if (result.cholesterol > 0) add("Chol: ${"%.0f".format(result.cholesterol)}")
            }
            Text(
                text       = parts.joinToString(" · ").ifEmpty { "No values" },
                fontSize   = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color      = NavyDark
            )
            val meta = buildList {
                if (result.time.isNotEmpty())        add(result.time)
                if (result.readingType.isNotEmpty()) add(result.readingType)
            }
            if (meta.isNotEmpty()) {
                Text(
                    text     = meta.joinToString(" · "),
                    fontSize = 10.sp,
                    color    = GrayMuted
                )
            }
        }

        // Status dot
        val dotColor = when {
            result.glucose > 0      -> glucoseColor(result.glucose)
            result.cholesterol > 0  -> cholesterolColor(result.cholesterol)
            else                    -> GrayMuted
        }
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Spacer(Modifier.width(10.dp))

        // Delete
        IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(
                Icons.Default.DeleteOutline, null,
                tint     = ErrorRed.copy(alpha = 0.45f),
                modifier = Modifier.size(15.dp)
            )
        }
    }
}

// ── Helpers ────────────────────────────────────────────────────────────────────
private fun formatShortDate(date: String): String {
    return try {
        val ld   = LocalDate.parse(date)
        val month = ld.month.name.take(3).lowercase()
            .replaceFirstChar { it.uppercase() }
        "$month ${ld.dayOfMonth}"
    } catch (e: Exception) {
        date
    }
}

// ── Side panel ─────────────────────────────────────────────────────────────────
@Composable
private fun SidePanelContent(
    username: String,
    onProfile: () -> Unit,
    onDashboard: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape          = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        drawerContainerColor = CardWhite,
        windowInsets         = WindowInsets(0),
        modifier             = Modifier.width(260.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Teal)
                .windowInsetsPadding(WindowInsets.statusBars)
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
        }

        Spacer(Modifier.weight(1f))

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
        Icon(icon, null, tint = iconColor, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(14.dp))
        Text(
            text       = label,
            fontSize   = 14.sp,
            fontWeight = if (highlight || isRed) FontWeight.SemiBold else FontWeight.Normal,
            color      = textColor
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = GrayText)
}

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
        border    = BorderStroke(if (onClick != null) 1.5.dp else 1.dp, TealMid)
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
            Text(value, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = trendColor)
            Text(unit, fontSize = 11.sp, color = GrayMuted)
        }
    }
}

// ── Quick Add Dialog ───────────────────────────────────────────────────────────
@Composable
private fun QuickAddDialog(onDismiss: () -> Unit, onSave: (LabResult) -> Unit) {
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
                Text("Log Reading", fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp, color = NavyDark)

                Spacer(Modifier.height(16.dp))

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
                            Text(label, fontSize = 13.sp,
                                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                color = if (active) Color.White else GrayText)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (selectedTab == 0) {
                    DialogField("Blood Sugar (mg/dL)", sugarValue,
                        { sugarValue = it; errorMsg = "" }, KeyboardType.Decimal, "e.g. 95")
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

                if (selectedTab == 1) {
                    DialogField("Cholesterol (mg/dL)", cholValue,
                        { cholValue = it; errorMsg = "" }, KeyboardType.Decimal, "e.g. 180")
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
                                errorMsg = "Enter a valid blood sugar value"; return@Button
                            }
                            onSave(LabResult(date = sugarDate, time = sugarTime,
                                readingType = readingType, glucose = v))
                        } else {
                            val v = cholValue.toFloatOrNull()
                            if (v == null || v <= 0f) {
                                errorMsg = "Enter a valid cholesterol value"; return@Button
                            }
                            onSave(LabResult(date = cholDate, time = cholTime, cholesterol = v))
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

@Composable
private fun DateTimeRow(
    date: String, time: String,
    context: android.content.Context, cal: Calendar,
    onDate: (String) -> Unit, onTime: (String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
            onClick = {
                DatePickerDialog(context, { _, y, m, d ->
                    onDate("$y-${(m+1).toString().padStart(2,'0')}-${d.toString().padStart(2,'0')}")
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            },
            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal),
            border = BorderStroke(1.dp, TealMid)
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
            modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Teal),
            border = BorderStroke(1.dp, TealMid)
        ) {
            Icon(Icons.Outlined.AccessTime, null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text(time, fontSize = 11.sp)
        }
    }
}

@Composable
private fun DialogField(
    label: String, value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType, placeholder: String
) {
    Column {
        Text(label, fontSize = 12.sp, color = GrayMuted, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value, onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = GrayMuted, fontSize = 14.sp) },
            singleLine = true, modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Teal, unfocusedBorderColor = TealMid)
        )
    }
}

