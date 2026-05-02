package com.queryb.medlog.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.queryb.medlog.data.OnboardingPrefs
import com.queryb.medlog.ui.components.MedLogLogo
import kotlinx.coroutines.launch

// ── Colors ────────────────────────────────────────────────────────────────────
private val Teal      = Color(0xFF00897B)
private val TealLight = Color(0xFFE8F5F3)
private val TealMid   = Color(0xFFB2DFDB)
private val NavyDark  = Color(0xFF0D1B2A)
private val GrayMuted = Color(0xFF9CA3AF)
private val GrayText  = Color(0xFF6B7280)
private val ScreenBg  = Color(0xFFF8FFFE)

// ── Data ──────────────────────────────────────────────────────────────────────
data class TrackingItem(
    val id: String,
    val label: String,
    val icon: ImageVector
)

private val trackingItems = listOf(
    TrackingItem("blood_sugar",  "Blood Sugar",  Icons.Outlined.Bloodtype),
    TrackingItem("cholesterol",  "Cholesterol",  Icons.Outlined.Favorite),
)

// ── Main ──────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    username: String,
    onboardingPrefs: OnboardingPrefs,
    onFinished: () -> Unit
) {
    val scope      = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 3 })

    val selectedItems = remember { mutableStateListOf<String>() }
    var nickname      by remember { mutableStateOf("") }
    var glucoseIdx    by remember { mutableStateOf(0) }
    var weightIdx     by remember { mutableStateOf(0) }

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    fun goNext() = scope.launch {
        pagerState.animateScrollToPage(pagerState.currentPage + 1)
    }
    fun goBack() = scope.launch {
        pagerState.animateScrollToPage(pagerState.currentPage - 1)
    }
    fun finish() {
        onboardingPrefs.setTrackedItems(username, selectedItems.toSet())
        onboardingPrefs.setGlucoseUnit(username, if (glucoseIdx == 0) "mg/dL" else "mmol/L")
        onboardingPrefs.setWeightUnit(username, if (weightIdx == 0) "kg" else "lb")
        onboardingPrefs.setCompleteFor(username)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        HorizontalPager(
            state = pagerState,
            userScrollEnabled = false,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            when (page) {
                0 -> Slide1Welcome(
                    onContinue = { goNext() }
                )
                1 -> Slide2Track(
                    selectedItems = selectedItems,
                    onToggle      = { id ->
                        if (selectedItems.contains(id)) selectedItems.remove(id)
                        else selectedItems.add(id)
                    },
                    onContinue = { goNext() },
                    onBack     = { goBack() }
                )
                2 -> Slide3Preferences(
                    nickname     = nickname,
                    onNickname   = { nickname = it },
                    glucoseIdx   = glucoseIdx,
                    onGlucose    = { glucoseIdx = it },
                    weightIdx    = weightIdx,
                    onWeight     = { weightIdx = it },
                    onGetStarted = { finish() },
                    onBack       = { goBack() }
                )
            }
        }
    }
}

// ── Nav bar ───────────────────────────────────────────────────────────────────
// showBack = false on slide 1, true on slides 2 & 3
// onForward label changes to "finish" on slide 3

@Composable
private fun NavBar(
    currentPage: Int,
    totalPages: Int,
    showBack: Boolean,
    onBack: () -> Unit,
    onForward: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Left — back arrow or empty space to keep dots centered
        Box(modifier = Modifier.size(44.dp), contentAlignment = Alignment.Center) {
            if (showBack) {
                FilledIconButton(
                    onClick = onBack,
                    modifier = Modifier.size(44.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = TealLight
                    ),
                    shape = CircleShape
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Teal,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Center — progress dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(totalPages) { i ->
                val isActive = i == currentPage
                val width by animateDpAsState(
                    targetValue = if (isActive) 24.dp else 8.dp,
                    label = "dot_$i"
                )
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(width)
                        .clip(CircleShape)
                        .background(if (isActive) Teal else TealMid)
                )
            }
        }

        // Right — forward arrow (filled teal)
        FilledIconButton(
            onClick = onForward,
            modifier = Modifier.size(44.dp),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Teal
            ),
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Filled.ArrowForward,
                contentDescription = "Continue",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

// ── Slide 1 — Welcome ─────────────────────────────────────────────────────────
@Composable
private fun Slide1Welcome(onContinue: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 28.dp)
                .padding(top = 64.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(TealLight),
                contentAlignment = Alignment.Center
            ) {
                MedLogLogo(size = 60.dp)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Welcome to MedLog",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyDark,
                textAlign = TextAlign.Center,
                lineHeight = 34.sp
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = "Log blood sugar, cholesterol, blood pressure and more. See trends, share with your doctor.",
                fontSize = 15.sp,
                color = GrayText,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(28.dp))

            FeaturePoint(
                icon  = Icons.Filled.Insights,
                title = "Personalised Insights",
                desc  = "Understand your results with contextual reference ranges."
            )
            Spacer(Modifier.height(12.dp))
            FeaturePoint(
                icon  = Icons.Filled.ShowChart,
                title = "Smart Trend Graphs",
                desc  = "Visualise improvements over weeks, months, and years."
            )
            Spacer(Modifier.height(12.dp))
            FeaturePoint(
                icon  = Icons.Filled.Lock,
                title = "Stays Private on Your Device",
                desc  = "Your health data never leaves your phone without permission."
            )
        }

        // Nav bar — slide 1 has no back
        HorizontalDivider(color = TealMid.copy(alpha = 0.4f), thickness = 1.dp)
        NavBar(
            currentPage = 0,
            totalPages  = 3,
            showBack    = false,
            onBack      = {},
            onForward   = onContinue
        )
        Spacer(Modifier.navigationBarsPadding())
    }
}

@Composable
private fun FeaturePoint(icon: ImageVector, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White)
            .border(1.dp, TealMid, RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(TealLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null,
                tint = Teal, modifier = Modifier.size(24.dp))
        }
        Spacer(Modifier.width(14.dp))
        Column {
            Text(title, fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp, color = NavyDark)
            Text(desc, fontSize = 12.sp,
                color = GrayMuted, lineHeight = 17.sp)
        }
    }
}

// ── Slide 2 — Track ───────────────────────────────────────────────────────────
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
            .background(ScreenBg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "What would you like to track?",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyDark,
                textAlign = TextAlign.Center,
                lineHeight = 32.sp
            )

            Spacer(Modifier.height(10.dp))

            Text(
                "Pick everything you want to log. You can change this later.",
                fontSize = 14.sp,
                color = GrayText,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                trackingItems.forEach { item ->
                    TrackingCard(
                        item     = item,
                        selected = selectedItems.contains(item.id),
                        onClick  = { onToggle(item.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        HorizontalDivider(color = TealMid.copy(alpha = 0.4f), thickness = 1.dp)
        NavBar(
            currentPage = 1,
            totalPages  = 3,
            showBack    = true,
            onBack      = onBack,
            onForward   = onContinue
        )
        Spacer(Modifier.navigationBarsPadding())
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
        targetValue = if (selected) TealLight else Color.White,
        label = "card_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Teal else Color(0xFFE0E0E0),
        label = "card_border"
    )

    Card(
        onClick = onClick,
        modifier = modifier.aspectRatio(0.95f),
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
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (selected) Teal.copy(alpha = 0.15f)
                            else Color(0xFFF0F0F0)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        item.icon,
                        contentDescription = null,
                        tint = if (selected) Teal else GrayMuted,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    item.label,
                    fontSize = 13.sp,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Teal else NavyDark,
                    textAlign = TextAlign.Center
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                visible  = selected,
                enter    = scaleIn(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit     = scaleOut() + fadeOut(),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(Teal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

// ── Slide 3 — Preferences ─────────────────────────────────────────────────────
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
            .background(ScreenBg)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 64.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Set your preferences",
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = NavyDark,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(28.dp))

            PrefCard {
                Text(
                    "What should we call you?",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = NavyDark
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = nickname,
                    onValueChange = onNickname,
                    placeholder = { Text("Your name or nickname", color = GrayMuted) },
                    leadingIcon = { Icon(Icons.Outlined.Person, null, tint = Teal) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Teal,
                        unfocusedBorderColor = TealMid
                    )
                )
            }

            Spacer(Modifier.height(14.dp))

            PrefCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Bloodtype, null,
                        tint = Teal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Glucose Unit", fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, color = NavyDark)
                }
                Spacer(Modifier.height(14.dp))
                UnitToggle(
                    options  = listOf("mg/dL", "mmol/L"),
                    selected = glucoseIdx,
                    onSelect = onGlucose
                )
            }

            Spacer(Modifier.height(14.dp))

            PrefCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.FitnessCenter, null,
                        tint = Teal, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Weight Unit", fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp, color = NavyDark)
                }
                Spacer(Modifier.height(14.dp))
                UnitToggle(
                    options  = listOf("kg", "lb"),
                    selected = weightIdx,
                    onSelect = onWeight
                )
            }

            Spacer(Modifier.height(14.dp))

            // Privacy card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = TealLight),
                border = BorderStroke(1.dp, TealMid)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Teal.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Shield, null,
                            tint = Teal, modifier = Modifier.size(20.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Your data is private",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = NavyDark
                        )
                        Spacer(Modifier.height(3.dp))
                        Text(
                            "Stored locally on this device. Optional cloud sync available later.",
                            fontSize = 12.sp,
                            color = GrayText,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Nav bar — slide 3, forward triggers finish
        HorizontalDivider(color = TealMid.copy(alpha = 0.4f), thickness = 1.dp)
        NavBar(
            currentPage = 2,
            totalPages  = 3,
            showBack    = true,
            onBack      = onBack,
            onForward   = onGetStarted
        )
        Spacer(Modifier.navigationBarsPadding())
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────
@Composable
private fun PrefCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, TealMid),
        elevation = CardDefaults.cardElevation(0.dp),
        content = { Column(modifier = Modifier.padding(18.dp), content = content) }
    )
}

@Composable
private fun UnitToggle(
    options: List<String>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TealLight)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            val isSelected = index == selected
            val bgColor by animateColorAsState(
                targetValue = if (isSelected) Teal else Color.Transparent,
                label = "toggle_bg_$index"
            )
            val textColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else GrayText,
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