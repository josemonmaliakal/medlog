package com.queryb.medlog.ui.screens

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.queryb.medlog.data.LabResult
import com.queryb.medlog.ui.theme.*
import com.queryb.medlog.ui.viewmodel.LabViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import kotlinx.coroutines.launch

private val metrics = listOf(
    "Glucose", "Cholesterol", "HDL", "LDL", "Triglycerides", "Hemoglobin"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartScreen(
    viewModel: LabViewModel,
    onBack: () -> Unit
) {
    var selectedMetric by remember { mutableStateOf("Glucose") }
    var results by remember { mutableStateOf<List<LabResult>>(emptyList()) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        results = viewModel.getAllSortedByDate()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Health Trends", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back",
                            tint = Color.White)
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
                .padding(16.dp)
        ) {
            // Metric selector chips
            Text(
                "Select Metric",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextMuted
            )
            Spacer(Modifier.height(8.dp))

            // First row chips
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.take(3).forEach { metric ->
                    MetricChip(
                        label = metric,
                        selected = metric == selectedMetric,
                        onClick = { selectedMetric = metric }
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                metrics.drop(3).forEach { metric ->
                    MetricChip(
                        label = metric,
                        selected = metric == selectedMetric,
                        onClick = { selectedMetric = metric }
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            if (results.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No data yet. Add lab results first!",
                        color = TextMuted
                    )
                }
            } else {
                // Chart
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = CardWhite)
                ) {
                    AndroidView(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        factory = { ctx -> LineChart(ctx) },
                        update = { chart ->
                            val entries = results.mapIndexed { i, r ->
                                val v = when (selectedMetric) {
                                    "Glucose"       -> r.glucose
                                    "Cholesterol"   -> r.cholesterol
                                    "HDL"           -> r.hdl
                                    "LDL"           -> r.ldl
                                    "Triglycerides" -> r.triglycerides
                                    "Hemoglobin"    -> r.hemoglobin
                                    else -> 0f
                                }
                                Entry(i.toFloat(), v)
                            }

                            val dataSet = LineDataSet(entries, selectedMetric).apply {
                                color = AndroidColor.parseColor("#1565C0")
                                setCircleColor(AndroidColor.parseColor("#1565C0"))
                                lineWidth = 2.5f
                                circleRadius = 5f
                                valueTextSize = 10f
                                mode = LineDataSet.Mode.CUBIC_BEZIER
                                setDrawFilled(true)
                                fillColor = AndroidColor.parseColor("#BBDEFB")
                                fillAlpha = 80
                            }

                            chart.apply {
                                data = LineData(dataSet)
                                description.isEnabled = false
                                legend.textSize = 12f
                                xAxis.apply {
                                    valueFormatter = IndexAxisValueFormatter(results.map { it.date })
                                    position = XAxis.XAxisPosition.BOTTOM
                                    granularity = 1f
                                    labelRotationAngle = -30f
                                    textSize = 10f
                                }
                                axisRight.isEnabled = false
                                axisLeft.textSize = 11f
                                setExtraOffsets(8f, 8f, 8f, 16f)
                                animateX(800)
                                invalidate()
                            }
                        }
                    )
                }

                // Normal range note
                val range = when (selectedMetric) {
                    "Glucose"       -> "Normal fasting: 70–99 mg/dL"
                    "Cholesterol"   -> "Normal: < 200 mg/dL"
                    "HDL"           -> "Normal: > 40 mg/dL (men), > 50 (women)"
                    "LDL"           -> "Normal: < 100 mg/dL"
                    "Triglycerides" -> "Normal: < 150 mg/dL"
                    "Hemoglobin"    -> "Normal: 13.5–17.5 g/dL (men)"
                    else -> ""
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "ℹ️ $range",
                    fontSize = 12.sp,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, fontSize = 12.sp) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MedBlue,
            selectedLabelColor = Color.White
        )
    )
}
