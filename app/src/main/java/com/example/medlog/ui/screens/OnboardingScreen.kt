package com.queryb.medlog.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queryb.medlog.data.OnboardingPrefs
import com.queryb.medlog.ui.components.MedLogLogo
import com.queryb.medlog.ui.theme.*
import kotlinx.coroutines.launch

// ─── Data models ─────────────────────────────────────────────────────────────

data class TrackingItem(
    val id: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private val trackingItems = listOf(
    TrackingItem("blood_sugar",   "Blood Sugar",   Icons.Outlined.Bloodtype,     Color(0xFFE53935)),
    TrackingItem("cholesterol",   "Cholesterol",   Icons.Outlined.Favorite,      Color(0xFFD81B60)),
    TrackingItem("blood_pressure","Blood Pressure",Icons.Outlined.MonitorHeart,  Color(0xFF8E24AA)),
    TrackingItem("weight",        "Weight",        Icons.Outlined.FitnessCenter,  Color(0xFF1E88E5)),
    TrackingItem("meals",         "Meals",         Icons.Outlined.Restaurant,     Color(0xFF43A047)),
    TrackingItem("medication",    "Medication",    Icons.Outlined.Medication,     Color(0xFF00897B)),
    TrackingItem("exercise",      "Exercise",      Icons.Outlined.DirectionsRun,  Color(0xFFF4511E)),
)

// ─── Main Composable ─────────────────────────────────────────────────────────

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    username: String,
    onboardingPrefs: OnboardingPrefs,
    onFinished: () -> Unit
) {
    val context = LocalContext.current
    val scope   = rememberCoroutineScope()

    val pagerState = rememberPagerState(pageCount = { 3 })

    // Slide 2 state
    val selectedItems = remember { mutableStateListOf<String>() }

    // Slide 3 state
    var nickname      by remember { mutableStateOf("") }
    var glucoseIdx    by remember { mutableStateOf(0) }   // 0 = mg/dL, 1 = mmol/L
    var weightIdx     by remember { mutableStateOf(0) }   // 0 = kg,    1 = lb

    fun goNext() {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage + 1)
        }
    }
    fun goBack() {
        scope.launch {
            pagerState.animateScrollToPage(pagerState.currentPage - 1)
        }
    }
    fun finish() {
        onboardingPrefs.setTrackedItems(username, selectedItems.toSet())
        onboardingPrefs.setGlucoseUnit(username, if (glucoseIdx == 0) "mg/dL" else "mmol/L")
        onboardingPrefs.setWeightUnit(username, if (weightIdx == 0) "kg" else "lb")
        onboardingPrefs.setCompleteFor(username)
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,           // only navigate via buttons
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> Slide1Welcome(onContinue = { goNext() })
                1 -> Slide2Track(
                    selectedItems = selectedItems,
                    onToggle = { id ->
                        if (selectedItems.contains(id)) selectedItems.remove(id)
                        else selectedItems.add(id)
                    },
                    onContinue = { goNext() },
                    onBack     = { goBack() }
                )
                2 -> Slide3Preferences(
                    nickname    = nickname,
                    onNickname  = { nickname = it },
                    glucoseIdx  = glucoseIdx,
                    onGlucose   = { glucoseIdx = it },
                    weightIdx   = weightIdx,
                    onWeight    = { weightIdx = it },
                    onGetStarted = { finish() },
                    onBack       = { goBack() }
                )
            }
        }

        // Dot indicators — always on top
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) { i ->
                val isActive = i == pagerState.currentPage
                val width by animateDpAsState(if (isActive) 24.dp else 8.dp, label = "dot")
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(
                            if (isActive) MedBlue
                            else MedBlue.copy(alpha = 0.25f)
                        )
                )
            }
        }
    }
}

// ─── Slide 1 — Welcome ───────────────────────────────────────────────────────

@Composable
private fun Slide1Welcome(onContinue: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0.0f to Color(0xFFEEF4FF),
                        0.5f to Color(0xFFF8FAFF),
                        1.0f to Color(0xFFFFFFFF)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 80.dp, bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Logo
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(MedBlueLight, MedBlueDark)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                MedLogLogo(size = 60.dp)
            }

            Spacer(Modifier.height(32.dp))

            // Main heading
            Text(
                text = "Welcome to MedLog",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = TextDark,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(Modifier.height(14.dp))

            // Sub heading
            Text(
                text = "Log blood sugar, cholesterol, blood pressure and more. See trends, share with your doctor.",
                fontSize = 15.sp,
                color = TextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(36.dp))

            // Feature points
            FeaturePoint(
                icon  = Icons.Filled.Insights,
                color = Color(0xFF5E92F3),
                title = "Personalised Insights",
                desc  = "Understand your results with contextual reference ranges."
            )
            Spacer(Modifier.height(16.dp))
            FeaturePoint(
                icon  = Icons.Filled.ShowChart,
                color = Color(0xFF43A047),
                title = "Smart Trend Graphs",
                desc  = "Visualise improvements over weeks, months, and years."
            )
            Spacer(Modifier.height(16.dp))
            FeaturePoint(
                icon  = Icons.Filled.Lock,
                color = Color(0xFF8E24AA),
                title = "Stays Private on Your Device",
                desc  = "Your health data never leaves your phone without permission."
            )

            Spacer(Modifier.weight(1f))
            Spacer(Modifier.height(32.dp))

            // Continue button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MedBlue),
                elevation = ButtonDefaults.buttonElevation(6.dp)
            ) {
                Text("Continue", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.ArrowForward, contentDescription = null)
            }
        }
    }
}

@Composable
private fun FeaturePoint(
    icon: ImageVector,
    color: Color,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardWhite)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
            Text(desc, fontSize = 12.sp, color = TextMuted, lineHeight = 17.sp)
        }
    }
}

// ─── Slide 2 — What to Track ─────────────────────────────────────────────────

@Composable
private fun Slide2Track(
    selectedItems: List<String>,
    onToggle: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 80.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "What would you like to track?",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark,
            textAlign = TextAlign.Center,
            lineHeight = 32.sp
        )

        Spacer(Modifier.height(10.dp))

        Text(
            "Pick everything you want to log. You can change this later.",
            fontSize = 14.sp,
            color = TextMuted,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // Grid of cards — 2 columns
        val chunked = trackingItems.chunked(2)
        chunked.forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                row.forEach { item ->
                    TrackingCard(
                        item       = item,
                        selected   = selectedItems.contains(item.id),
                        onClick    = { onToggle(item.id) },
                        modifier   = Modifier.weight(1f)
                    )
                }
                // If odd number fill last cell
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        // Continue button
        Button(
            onClick = onContinue,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedItems.isNotEmpty()) MedBlue
                else Color(0xFFBDBDBD)
            ),
            elevation = ButtonDefaults.buttonElevation(if (selectedItems.isNotEmpty()) 6.dp else 0.dp)
        ) {
            Text("Continue", fontSize = 17.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(8.dp))
            Icon(Icons.Default.ArrowForward, contentDescription = null)
        }

        Spacer(Modifier.height(14.dp))

        // Back as text
        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null,
                tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Back", color = TextMuted, fontSize = 14.sp)
        }
    }
}

@Composable
private fun TrackingCard(
    item: TrackingItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) item.color.copy(alpha = 0.10f) else CardWhite,
        label = "card_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) item.color else Color(0xFFE0E0E0),
        label = "card_border"
    )
    val scale by animateFloatAsState(
        targetValue = if (selected) 0.97f else 1f,
        label = "card_scale"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .aspectRatio(0.95f),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(if (selected) 0.dp else 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(14.dp)) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) item.color.copy(alpha = 0.18f)
                            else Color(0xFFF0F0F0)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = if (selected) item.color else TextMuted,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    item.label,
                    fontSize = 12.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) item.color else TextDark,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
            }

            // Checkmark badge (top-right)
            androidx.compose.animation.AnimatedVisibility(
                visible = selected,
                enter = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit  = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(item.color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ─── Slide 3 — Preferences ───────────────────────────────────────────────────

@Composable
private fun Slide3Preferences(
    nickname: String,
    onNickname: (String) -> Unit,
    glucoseIdx: Int,
    onGlucose: (Int) -> Unit,
    weightIdx: Int,
    onWeight: (Int) -> Unit,
    onGetStarted: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SurfaceWhite)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 80.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Set your preferences",
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TextDark,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(28.dp))

        // ── Name field ─────────────────────────────────────────────────────
        PrefCard {
            Text(
                "What should we call you?",
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = TextDark
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = nickname,
                onValueChange = onNickname,
                placeholder = { Text("Your name or nickname", color = TextMuted) },
                leadingIcon = {
                    Icon(Icons.Outlined.Person, null, tint = MedBlue)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MedBlue,
                    unfocusedBorderColor = Color(0xFFDDE3F0)
                )
            )
        }

        Spacer(Modifier.height(14.dp))

        // ── Glucose unit ───────────────────────────────────────────────────
        PrefCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.Bloodtype, null,
                    tint = Color(0xFFE53935), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Glucose Unit",
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
            }
            Spacer(Modifier.height(14.dp))
            UnitToggle(
                options  = listOf("mg/dL", "mmol/L"),
                selected = glucoseIdx,
                color    = Color(0xFFE53935),
                onSelect = onGlucose
            )
        }

        Spacer(Modifier.height(14.dp))

        // ── Weight unit ────────────────────────────────────────────────────
        PrefCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Outlined.FitnessCenter, null,
                    tint = Color(0xFF1E88E5), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("Weight Unit",
                    fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = TextDark)
            }
            Spacer(Modifier.height(14.dp))
            UnitToggle(
                options  = listOf("kg", "lb"),
                selected = weightIdx,
                color    = Color(0xFF1E88E5),
                onSelect = onWeight
            )
        }

        Spacer(Modifier.height(14.dp))

        // ── Privacy info card ──────────────────────────────────────────────
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF0F4FF)
            ),
            border = BorderStroke(1.dp, MedBlue.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MedBlue.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = MedBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(
                        "Your data is private",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MedBlueDark
                    )
                    Spacer(Modifier.height(3.dp))
                    Text(
                        "Stored locally on this device. Optional cloud sync available later.",
                        fontSize = 12.sp,
                        color = Color(0xFF4A5568),
                        lineHeight = 17.sp
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(24.dp))

        // Get Started button
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MedGreen),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            Icon(Icons.Default.RocketLaunch, contentDescription = null)
            Spacer(Modifier.width(10.dp))
            Text("Get Started!", fontSize = 17.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(14.dp))

        TextButton(onClick = onBack) {
            Icon(Icons.Default.ArrowBack, contentDescription = null,
                tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(4.dp))
            Text("Back", color = TextMuted, fontSize = 14.sp)
        }
    }
}

// ─── Reusable pref card container ────────────────────────────────────────────

@Composable
private fun PrefCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

// ─── Unit toggle (sliding pill selector) ─────────────────────────────────────

@Composable
private fun UnitToggle(
    options: List<String>,
    selected: Int,
    color: Color,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF0F0F0))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) color else Color.Transparent,
                label = "toggle_bg_$index"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else TextMuted,
                label = "toggle_text_$index"
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(bgColor)
                    .clickable { onSelect(index) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 14.sp,
                    color = textColor
                )
            }
        }
    }
}

