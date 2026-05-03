package com.queryb.medlog.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queryb.medlog.data.LabResult
import com.queryb.medlog.ui.viewmodel.LabViewModel
import java.time.LocalDate
import com.queryb.medlog.ui.utils.glucoseColor
import com.queryb.medlog.ui.utils.cholesterolColor

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

// ── Filter options ─────────────────────────────────────────────────────────────
private enum class HistoryFilter(val label: String) {
    ALL("All"),
    SUGAR("Blood Sugar"),
    CHOLESTEROL("Cholesterol")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: LabViewModel,
    onBack: () -> Unit
) {
    val results by viewModel.results.collectAsState()

    var activeFilter    by remember { mutableStateOf(HistoryFilter.ALL) }
    var resultToDelete  by remember { mutableStateOf<LabResult?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Apply filter
    val filteredResults = remember(results, activeFilter) {
        when (activeFilter) {
            HistoryFilter.ALL          -> results
            HistoryFilter.SUGAR        -> results.filter { it.glucose > 0 }
            HistoryFilter.CHOLESTEROL  -> results.filter { it.cholesterol > 0 }
        }
    }

    // ── Delete dialog ─────────────────────────────────────────────────────────
    if (showDeleteDialog) {
        resultToDelete?.let { result ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false; resultToDelete = null },
                icon  = { Icon(Icons.Default.DeleteOutline, null, tint = ErrorRed) },
                title = { Text("Delete Entry") },
                text  = { Text("Delete the entry dated ${result.date}? This cannot be undone.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.delete(result)
                            showDeleteDialog = false
                            resultToDelete   = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) { Text("Delete") }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showDeleteDialog = false
                        resultToDelete   = null
                    }) { Text("Cancel") }
                }
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "History",
                            fontWeight = FontWeight.Bold,
                            fontSize   = 18.sp,
                            color      = Color.White
                        )
                        Text(
                            "${filteredResults.size} record${if (filteredResults.size != 1) "s" else ""}",
                            fontSize = 11.sp,
                            color    = Color.White.copy(alpha = 0.75f)
                        )
                    }
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
        ) {
            // ── Filter chips ──────────────────────────────────────────────────
            LazyRow(
                modifier            = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(HistoryFilter.entries) { filter ->
                    val active = filter == activeFilter
                    Surface(
                        onClick = { activeFilter = filter },
                        shape  = RoundedCornerShape(20.dp),
                        color  = if (active) Teal else Color.White,
                        border = BorderStroke(
                            1.dp,
                            if (active) Teal else TealMid
                        )
                    ) {
                        Text(
                            text     = filter.label,
                            fontSize = 12.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            color    = if (active) Color.White else GrayText,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            HorizontalDivider(color = TealLight, thickness = 1.dp)

            // ── List ──────────────────────────────────────────────────────────
            if (filteredResults.isEmpty()) {
                Box(
                    modifier         = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Outlined.SearchOff, null,
                            modifier = Modifier.size(64.dp),
                            tint     = TealMid
                        )
                        Text(
                            text       = if (activeFilter == HistoryFilter.ALL)
                                "No records yet"
                            else
                                "No ${activeFilter.label} records",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = GrayMuted
                        )
                        Text(
                            text     = "Tap Log Reading on the dashboard\nto add your first entry",
                            fontSize = 13.sp,
                            color    = GrayMuted,
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier        = Modifier.fillMaxSize(),
                    contentPadding  = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Group by month
                    val grouped = filteredResults.groupBy { result ->
                        try {
                            val d = LocalDate.parse(result.date)
                            "${d.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${d.year}"
                        } catch (e: Exception) {
                            "Other"
                        }
                    }

                    grouped.forEach { (monthLabel, monthResults) ->
                        // Month header
                        item(key = "header_$monthLabel") {
                            Row(
                                modifier          = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text       = monthLabel,
                                    fontSize   = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color      = Teal
                                )
                                Spacer(Modifier.width(8.dp))
                                HorizontalDivider(
                                    modifier  = Modifier.weight(1f),
                                    color     = TealMid,
                                    thickness = 1.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text     = "${monthResults.size}",
                                    fontSize = 11.sp,
                                    color    = GrayMuted
                                )
                            }
                        }

                        // Cards for this month
                        items(monthResults, key = { it.id }) { result ->
                            FullHistoryCard(
                                result   = result,
                                onDelete = {
                                    resultToDelete   = result
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(32.dp)) }
                }
            }
        }
    }
}

// ── Full expandable history card ───────────────────────────────────────────────
@Composable
private fun FullHistoryCard(
    result: LabResult,
    onDelete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val extraValues = buildList {
        if (result.hdl > 0)           add(Triple("HDL",           "${result.hdl} mg/dL",           Icons.Outlined.BarChart))
        if (result.ldl > 0)           add(Triple("LDL",           "${result.ldl} mg/dL",           Icons.Outlined.BarChart))
        if (result.triglycerides > 0) add(Triple("Triglycerides", "${result.triglycerides} mg/dL", Icons.Outlined.Science))
        if (result.hemoglobin > 0)    add(Triple("Hemoglobin",    "${result.hemoglobin} g/dL",     Icons.Outlined.Bloodtype))
    }
    val hasExtras = extraValues.isNotEmpty()

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border    = BorderStroke(
            width = if (expanded) 1.5.dp else 1.dp,
            color = if (expanded) Teal else TealMid
        )
    ) {
        Column {
            // ── Main row ──────────────────────────────────────────────────────
            Row(
                modifier          = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left: date + time + reading type
                Column(
                    modifier            = Modifier.width(80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(color = TealLight, shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text       = formatShortDate(result.date),
                            color      = Teal,
                            fontWeight = FontWeight.Bold,
                            fontSize   = 10.sp,
                            modifier   = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    if (result.time.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccessTime, null,
                                tint = GrayMuted, modifier = Modifier.size(10.dp))
                            Spacer(Modifier.width(2.dp))
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
                                text       = result.readingType.uppercase(),
                                fontSize   = 8.sp,
                                fontWeight = FontWeight.Bold,
                                color      = if (isFasting) Color(0xFFF57C00) else Teal,
                                modifier   = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.width(10.dp))

                // Center: primary readings
                Column(modifier = Modifier.weight(1f)) {
                    if (result.glucose > 0) {
                        ReadingRow(
                            icon  = Icons.Outlined.Bloodtype,
                            label = "Sugar",
                            value = "${"%.1f".format(result.glucose)} mg/dL",
                            color = glucoseColor(result.glucose)
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    if (result.cholesterol > 0) {
                        ReadingRow(
                            icon  = Icons.Outlined.Favorite,
                            label = "Chol",
                            value = "${"%.1f".format(result.cholesterol)} mg/dL",
                            color = cholesterolColor(result.cholesterol)
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    if (!hasExtras && result.pdfUri.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, null,
                                tint = Teal, modifier = Modifier.size(11.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("PDF attached", fontSize = 10.sp, color = Teal)
                        }
                    }
                    if (!expanded && hasExtras) {
                        Text(
                            "+${extraValues.size} more",
                            fontSize = 10.sp,
                            color    = GrayMuted
                        )
                    }
                }

                // Right: status dot + delete + expand
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val dotColor = when {
                        result.glucose > 0     -> glucoseColor(result.glucose)
                        result.cholesterol > 0 -> cholesterolColor(result.cholesterol)
                        else                   -> GrayMuted
                    }
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(9.dp)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                    Spacer(Modifier.height(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.DeleteOutline, null,
                            tint     = ErrorRed.copy(alpha = 0.45f),
                            modifier = Modifier.size(15.dp))
                    }
                    if (hasExtras) {
                        IconButton(
                            onClick  = { expanded = !expanded },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = if (expanded) Icons.Default.ExpandLess
                                else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint     = if (expanded) Teal else GrayMuted,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            // ── Expanded extras ───────────────────────────────────────────────
            AnimatedVisibility(
                visible = expanded && hasExtras,
                enter   = fadeIn(),
                exit    = fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(TealLight.copy(alpha = 0.4f))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    extraValues.forEach { (label, value, icon) ->
                        Row(
                            modifier          = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icon, null,
                                tint     = Teal,
                                modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(label, fontSize = 12.sp,
                                color = GrayText, modifier = Modifier.weight(1f))
                            Text(value, fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold, color = NavyDark)
                        }
                    }
                    if (result.pdfUri.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PictureAsPdf, null,
                                tint = Teal, modifier = Modifier.size(13.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("PDF report attached", fontSize = 12.sp, color = Teal)
                        }
                    }
                }
            }
        }
    }
}

// ── Single reading row inside the card ────────────────────────────────────────
@Composable
private fun ReadingRow(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
        Spacer(Modifier.width(5.dp))
        Text("$label: ", fontSize = 12.sp, color = GrayText)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}

// ── Short date helper ─────────────────────────────────────────────────────────
private fun formatShortDate(date: String): String {
    return try {
        val ld    = LocalDate.parse(date)
        val month = ld.month.name.take(3).lowercase()
            .replaceFirstChar { it.uppercase() }
        "$month ${ld.dayOfMonth}"
    } catch (e: Exception) {
        date
    }
}


