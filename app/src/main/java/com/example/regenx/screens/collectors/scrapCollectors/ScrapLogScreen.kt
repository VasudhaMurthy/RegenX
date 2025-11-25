package com.example.regenx.screens.collectors.scrapCollectors

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

enum class ScrapLogView { DAILY, WEEKLY, MONTHLY }

data class ScrapBarEntry(
    val label: String,
    val value: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScrapLogScreen(navController: NavController) {
    var selectedView by remember { mutableStateOf(ScrapLogView.DAILY) }

    // TODO: plug your real data here
    val data = when (selectedView) {
        ScrapLogView.DAILY -> listOf(
            ScrapBarEntry("Mon", 12f),
            ScrapBarEntry("Tue", 8f),
            ScrapBarEntry("Wed", 15f),
            ScrapBarEntry("Thu", 9f),
            ScrapBarEntry("Fri", 18f),
            ScrapBarEntry("Sat", 6f),
            ScrapBarEntry("Sun", 10f),
        )
        ScrapLogView.WEEKLY -> listOf(
            ScrapBarEntry("W1", 40f),
            ScrapBarEntry("W2", 52f),
            ScrapBarEntry("W3", 30f),
            ScrapBarEntry("W4", 65f),
        )
        ScrapLogView.MONTHLY -> listOf(
            ScrapBarEntry("Jan", 120f),
            ScrapBarEntry("Feb", 95f),
            ScrapBarEntry("Mar", 140f),
            ScrapBarEntry("Apr", 110f),
            ScrapBarEntry("May", 160f),
            ScrapBarEntry("Jun", 135f),
        )
    }

    val totalKg = data.sumOf { it.value.toDouble() }.toFloat()
    val entriesCount = data.size
    val avgPerEntry = if (entriesCount > 0) totalKg / entriesCount else 0f
    val maxEntry = data.maxByOrNull { it.value }
    val minEntry = data.minByOrNull { it.value }
    val lastEntry = data.lastOrNull()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Collected Scrap Log",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Buyer overview & trends",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Summary title + action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Summary",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
//                AssistChip(
//                    onClick = { /* hook export later */ },
//                    label = {
//                        Text(
//                            text = "Download report",
//                            fontSize = 12.sp
//                        )
//                    },
//                    shape = CircleShape
//                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Summary cards – row 1
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Total Weight",
                    value = "${"%.1f".format(totalKg)} kg",
                    subtitle = when (selectedView) {
                        ScrapLogView.DAILY -> "This week"
                        ScrapLogView.WEEKLY -> "This month"
                        ScrapLogView.MONTHLY -> "Half-year"
                    },
                    modifier = Modifier.weightCompat()
                )
                SummaryCard(
                    title = "Entries",
                    value = entriesCount.toString(),
                    subtitle = "Pickups logged",
                    modifier = Modifier.weightCompat()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Summary cards – row 2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SummaryCard(
                    title = "Avg / Entry",
                    value = "${"%.1f".format(avgPerEntry)} kg",
                    subtitle = "Typical load size",
                    modifier = Modifier.weightCompat()
                )
                SummaryCard(
                    title = "Last Recorded",
                    value = lastEntry?.let { "${it.value.toInt()} kg" } ?: "-",
                    subtitle = lastEntry?.label ?: "No data",
                    modifier = Modifier.weightCompat()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // View By chips
            Text(
                text = "View by",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ViewChip(
                    label = "Daily",
                    selected = selectedView == ScrapLogView.DAILY,
                    onClick = { selectedView = ScrapLogView.DAILY }
                )
                ViewChip(
                    label = "Weekly",
                    selected = selectedView == ScrapLogView.WEEKLY,
                    onClick = { selectedView = ScrapLogView.WEEKLY }
                )
                ViewChip(
                    label = "Monthly",
                    selected = selectedView == ScrapLogView.MONTHLY,
                    onClick = { selectedView = ScrapLogView.MONTHLY }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Period details card (replaces graph)
            Text(
                text = "Period details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = when (selectedView) {
                            ScrapLogView.DAILY -> "Daily collection for current week."
                            ScrapLogView.WEEKLY -> "Weekly collection for current month."
                            ScrapLogView.MONTHLY -> "Month-wise collection for current period."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "Highest collection: ${
                            maxEntry?.let { "${it.label} – ${it.value} kg" } ?: "No data"
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Lowest collection: ${
                            minEntry?.let { "${it.label} – ${it.value} kg" } ?: "No data"
                        }",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "Use this to spot peak and low days/weeks and adjust pickup frequency and pricing accordingly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Simple material mix card (static example values)
            Text(
                text = "Material mix (example)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    MaterialRow(label = "Metal", percent = 55, accent = MaterialTheme.colorScheme.primary)
                    MaterialRow(label = "Plastic", percent = 25, accent = Color(0xFF00BFA5))
                    MaterialRow(label = "Paper", percent = 15, accent = Color(0xFFFFB300))
                    MaterialRow(label = "Other", percent = 5, accent = Color(0xFF8D6E63))
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Buyer notes / tips
            Text(
                text = "Buyer notes",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Track if today’s collected weight is on target, see which days underperform, and decide when to schedule extra pickups or renegotiate rates with suppliers.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Tip: Switch between Daily, Weekly, and Monthly above to understand seasonality and plan capacity.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// simple no-op extension so we don't call real weight()
private fun Modifier.weightCompat(): Modifier = this

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 14.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun ViewChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        tonalElevation = if (selected) 4.dp else 0.dp,
        color = if (selected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
        else
            MaterialTheme.colorScheme.surfaceVariant
    ) {
        TextButton(
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MaterialRow(
    label: String,
    percent: Int,
    accent: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$percent%",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(50)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = percent / 100f)
                    .height(6.dp)
                    .background(
                        color = accent,
                        shape = RoundedCornerShape(50)
                    )
            )
        }
    }
}







//package com.example.regenx.screens.collectors.scrapCollectors
//
//import androidx.compose.foundation.Canvas
//import androidx.compose.foundation.background
//import androidx.compose.foundation.layout.*
//import androidx.compose.foundation.rememberScrollState
//import androidx.compose.foundation.verticalScroll
//import androidx.compose.material.icons.Icons
//import androidx.compose.material.icons.filled.ArrowBack
//import androidx.compose.material3.*
//import androidx.compose.runtime.*
//import androidx.compose.ui.Alignment
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.geometry.Offset
//import androidx.compose.ui.geometry.Size
//import androidx.compose.ui.graphics.Color
//import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.unit.dp
//import androidx.compose.ui.unit.sp
//import androidx.navigation.NavController
//import kotlin.math.max
//
//enum class ScrapRange { DAILY, WEEKLY, MONTHLY }
//
//data class ScrapLogEntry(
//    val label: String,     // e.g. "Mon", "Week 1", "Jan"
//    val weightKg: Float
//)
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//fun ScrapLogScreen(navController: NavController) {
//    var selectedRange by remember { mutableStateOf(ScrapRange.WEEKLY) }
//
//    val data = remember(selectedRange) {
//        when (selectedRange) {
//            ScrapRange.DAILY -> listOf(
//                ScrapLogEntry("Mon", 12f),
//                ScrapLogEntry("Tue", 8f),
//                ScrapLogEntry("Wed", 15f),
//                ScrapLogEntry("Thu", 10f),
//                ScrapLogEntry("Fri", 18f),
//                ScrapLogEntry("Sat", 6f),
//                ScrapLogEntry("Sun", 9f),
//            )
//
//            ScrapRange.WEEKLY -> listOf(
//                ScrapLogEntry("W1", 60f),
//                ScrapLogEntry("W2", 72f),
//                ScrapLogEntry("W3", 54f),
//                ScrapLogEntry("W4", 90f),
//            )
//
//            ScrapRange.MONTHLY -> listOf(
//                ScrapLogEntry("Jan", 220f),
//                ScrapLogEntry("Feb", 180f),
//                ScrapLogEntry("Mar", 260f),
//                ScrapLogEntry("Apr", 210f),
//            )
//        }
//    }
//
//    val totalKg = data.sumOf { it.weightKg.toDouble() }.toFloat()
//    val maxKg = max(data.maxOfOrNull { it.weightKg } ?: 0f, 1f)
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                title = { Text("Collected Scrap Log") },
//                navigationIcon = {
//                    IconButton(onClick = { navController.popBackStack() }) {
//                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
//                    }
//                }
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//                .verticalScroll(rememberScrollState())
//                .padding(16.dp)
//        ) {
//
//            // Summary
//            Text("Summary", fontWeight = FontWeight.Bold, fontSize = 18.sp)
//            Spacer(Modifier.height(8.dp))
//
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(12.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                SummaryChip("Total", "${"%.1f".format(totalKg)} kg")
//                SummaryChip("Entries", data.size.toString())
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            // Range Selector
//            Text("View by", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
//            Spacer(Modifier.height(8.dp))
//            Row(
//                horizontalArrangement = Arrangement.spacedBy(8.dp),
//                modifier = Modifier.fillMaxWidth()
//            ) {
//                RangeChip("Daily", selectedRange == ScrapRange.DAILY) { selectedRange = ScrapRange.DAILY }
//                RangeChip("Weekly", selectedRange == ScrapRange.WEEKLY) { selectedRange = ScrapRange.WEEKLY }
//                RangeChip("Monthly", selectedRange == ScrapRange.MONTHLY) { selectedRange = ScrapRange.MONTHLY }
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            Text("Scrap Collected (kg)", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
//            Spacer(Modifier.height(12.dp))
//
//            Box(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(260.dp)
//                    .background(MaterialTheme.colorScheme.surfaceVariant, shape = MaterialTheme.shapes.medium)
//                    .padding(12.dp),
//                contentAlignment = Alignment.Center
//            ) {
//                BarChart(data = data, maxValue = maxKg)
//            }
//
//            Spacer(Modifier.height(24.dp))
//
//            Text(
//                text = "Tip: Tap different ranges to compare daily, weekly, or monthly collected scrap.",
//                style = MaterialTheme.typography.bodySmall
//            )
//        }
//    }
//}
//
//@Composable
//fun SummaryChip(title: String, value: String) {
//    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 2.dp) {
//        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
//            Text(title, style = MaterialTheme.typography.labelMedium)
//            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
//        }
//    }
//}
//
//@Composable
//fun RangeChip(text: String, selected: Boolean, onClick: () -> Unit) {
//    FilterChip(
//        selected = selected,
//        onClick = onClick,
//        label = { Text(text) }
//    )
//}
//
//@Composable
//fun BarChart(
//    data: List<ScrapLogEntry>,
//    maxValue: Float,
//    barSpacing: Float = 16f
//) {
//    if (data.isEmpty()) return
//
//    Canvas(modifier = Modifier.fillMaxSize()) {
//        val barWidth = (size.width - (barSpacing * (data.size + 1))) / data.size
//
//        data.forEachIndexed { index, entry ->
//            val left = barSpacing + index * (barWidth + barSpacing)
//            val top = size.height * (1f - (entry.weightKg / maxValue))
//            val rectSize = Size(barWidth, size.height - top)
//
//            // ✅ FIXED — drawRect now includes color
//            drawRect(
//                color = Color(0xFF4CAF50),
//                topLeft = Offset(left, top),
//                size = rectSize
//            )
//        }
//    }
//}