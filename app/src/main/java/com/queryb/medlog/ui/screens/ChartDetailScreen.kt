package com.queryb.medlog.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Bloodtype
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.queryb.medlog.data.LabResult
import com.queryb.medlog.ui.viewmodel.LabViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.queryb.medlog.ui.utils.glucoseColor
import com.queryb.medlog.ui.utils.cholesterolColor


private val Teal      = Color(0xFF00897B)
private val TealLight = Color(0xFFE8F5F3)
private val TealMid   = Color(0xFFB2DFDB)
private val NavyDark  = Color(0xFF0D1B2A)
private val GrayMuted = Color(0xFF9CA3AF)
private val ScreenBg  = Color(0xFFF8FFFE)
private val CardWhite = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDetailScreen(
    metric: String,
    viewModel: LabViewModel,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var results by remember { mutableStateOf<List<LabResult>>(emptyList()) }

    val isGlucose   = metric == "glucose"
    val title       = if (isGlucose) "Blood Sugar" else "Cholesterol"
    val unit        = "mg/dL"
    val icon: ImageVector = if (isGlucose) Icons.Outlined.Bloodtype else Icons.Outlined.Favorite

    // Normal range for reference line
    val normalMax   = if (isGlucose) 99f else 200f
    val normalMin   = if (isGlucose) 70f else 0f
    val rangeLabel  = if (isGlucose) "Normal: 70–99 mg/dL (fasting)"
    else "Normal: < 200 mg/dL"

    LaunchedEffect(Unit) {
        results = viewModel.getAllSortedByDate()
    }

    // Derived stats for the current metric
    val values = results.map { if (isGlucose) it.glucose else it.cholesterol }
        .filter { it > 0 }
    val latest  = values.lastOrNull()
    val average = values.average().takeIf { !it.isNaN() }
    val highest = values.maxOrNull()
    val lowest  = values.minOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(TealLight),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, null, tint = Teal,
                                modifier = Modifier.size(18.dp))
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Teal,
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ── Summary stat row ──────────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Latest",
                    value    = latest?.let { "%.1f".format(it) } ?: "--",
                    unit     = unit,
                    color    = if (isGlucose) glucoseColor(latest)
                    else cholesterolColor(latest)
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Average",
                    value    = average?.let { "%.1f".format(it) } ?: "--",
                    unit     = unit,
                    color    = Teal
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Highest",
                    value    = highest?.let { "%.1f".format(it) } ?: "--",
                    unit     = unit,
                    color    = Color(0xFFF57C00)
                )
                MiniStatCard(
                    modifier = Modifier.weight(1f),
                    label    = "Lowest",
                    value    = lowest?.let { "%.1f".format(it) } ?: "--",
                    unit     = unit,
                    color    = Color(0xFF1E88E5)
                )
            }

            // ── Chart card ────────────────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardWhite),
                elevation = CardDefaults.cardElevation(0.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, TealMid)
            ) {
                if (results.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No data yet.\nAdd readings to see your trend.",
                            color = GrayMuted,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                } else {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        factory = { ctx -> LineChart(ctx) },
                        update  = { chart ->
                            val filteredResults = results.filter {
                                (if (isGlucose) it.glucose else it.cholesterol) > 0
                            }
                            val entries = filteredResults.mapIndexed { i, r ->
                                Entry(i.toFloat(),
                                    if (isGlucose) r.glucose else r.cholesterol)
                            }
                            val labels = filteredResults.map { it.date }

                            val dataSet = LineDataSet(entries, title).apply {
                                color        = AndroidColor.parseColor("#00897B")
                                lineWidth    = 2.5f
                                setCircleColor(AndroidColor.parseColor("#00897B"))
                                circleRadius = 5f
                                circleHoleColor = AndroidColor.parseColor("#FFFFFF")
                                circleHoleRadius = 2.5f
                                valueTextSize  = 10f
                                valueTextColor = AndroidColor.parseColor("#0D1B2A")
                                mode           = LineDataSet.Mode.CUBIC_BEZIER
                                setDrawFilled(true)
                                fillColor  = AndroidColor.parseColor("#00897B")
                                fillAlpha  = 25
                            }

                            // Normal max reference line
                            val limitLine = LimitLine(normalMax, "Normal max").apply {
                                lineWidth   = 1.5f
                                lineColor   = AndroidColor.parseColor("#B2DFDB")
                                enableDashedLine(12f, 6f, 0f)
                                textColor   = AndroidColor.parseColor("#9CA3AF")
                                textSize    = 9f
                            }

                            chart.apply {
                                data = LineData(dataSet)
                                description.isEnabled = false
                                legend.isEnabled      = false
                                setTouchEnabled(true)
                                setPinchZoom(true)

                                xAxis.apply {
                                    valueFormatter    = IndexAxisValueFormatter(labels)
                                    position          = XAxis.XAxisPosition.BOTTOM
                                    granularity       = 1f
                                    labelRotationAngle = -30f
                                    textSize          = 9f
                                    textColor         = AndroidColor.parseColor("#9CA3AF")
                                    gridColor         = AndroidColor.parseColor("#F0F0F0")
                                    axisLineColor     = AndroidColor.parseColor("#B2DFDB")
                                }
                                axisLeft.apply {
                                    textSize  = 9f
                                    textColor = AndroidColor.parseColor("#9CA3AF")
                                    gridColor = AndroidColor.parseColor("#F5F5F5")
                                    axisLineColor = AndroidColor.parseColor("#B2DFDB")
                                    addLimitLine(limitLine)
                                }
                                axisRight.isEnabled = false
                                setExtraOffsets(8f, 12f, 8f, 8f)
                                animateX(600)
                                invalidate()
                            }
                        }
                    )
                }
            }

            // ── Normal range note ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(TealLight)
                    .border(1.dp, TealMid, RoundedCornerShape(10.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("ℹ️", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    rangeLabel,
                    fontSize = 12.sp,
                    color = NavyDark,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// ── Mini stat card ────────────────────────────────────────────────────────────
@Composable
private fun MiniStatCard(
    modifier: Modifier,
    label: String,
    value: String,
    unit: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, TealMid)
    ) {
        Column(
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 10.sp, color = GrayMuted,
                fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold, color = color)
            Text(unit, fontSize = 9.sp, color = GrayMuted)
        }
    }
}

