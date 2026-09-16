package com.example.cykluscalk.ui

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import kotlin.math.roundToInt
import com.example.cykluscalk.R
import com.example.cykluscalk.data.TagDefinition
import java.time.Month
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*
import kotlin.math.ceil

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StatisticsScreen(viewModel: DayRecordViewModel) {
    val stats by viewModel.statistics.collectAsState()
    val statsMonth by viewModel.statsMonth.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val isPregModeActive by viewModel.isPregnancyMode.collectAsState()
    val showAdvancedSex by viewModel.showAdvancedSex.collectAsState()
    
    var viewMode by remember { mutableStateOf("Month") }
    var selectedChartTags by remember { mutableStateOf(setOf<String>()) }
    var showChartInfo by remember { mutableStateOf(false) }
    var showQuickPicker by remember { mutableStateOf(false) }

    var basalExpanded by remember { mutableStateOf(!isPregModeActive) }
    var symptomsExpanded by remember { mutableStateOf(false) }
    var sexExpanded by remember { mutableStateOf(false) }
    var pregnancyExpanded by remember { mutableStateOf(isPregModeActive) }

    val romanMonths = listOf("I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X", "XI", "XII")
    
    LaunchedEffect(allTags) {
        if (selectedChartTags.isEmpty() && allTags.isNotEmpty()) {
            selectedChartTags = allTags.take(3).map { it.name }.toSet()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. AVERAGES & INFO ICON ---
        item {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Průměrné hodnoty",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showChartInfo = true }) {
                        Icon(painterResource(R.drawable.ic_info), null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = stringResource(R.string.avg_cycle_length, stats.avgCycleLength),
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = stringResource(R.string.avg_period_duration, stats.avgPeriodDuration),
                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }

                if (showChartInfo) {
                    AlertDialog(
                        onDismissRequest = { showChartInfo = false },
                        title = { Text(stringResource(R.string.chart_info_title)) },
                        text = {
                            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                                Text(stringResource(R.string.chart_info_content), style = MaterialTheme.typography.bodySmall)
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showChartInfo = false }) { Text(stringResource(R.string.confirm)) }
                        }
                    )
                }
            }
        }

        // --- 2. VIEW MODE SELECTOR (MONTH/YEAR) ---
        item {
            TabRow(selectedTabIndex = if (viewMode == "Month") 0 else 1, containerColor = Color.Transparent) {
                Tab(selected = viewMode == "Month", onClick = { viewMode = "Month" }) {
                    Text(text = "Měsíc", modifier = Modifier.padding(8.dp))
                }
                Tab(selected = viewMode == "Year", onClick = { viewMode = "Year" }) {
                    Text(text = "Rok", modifier = Modifier.padding(8.dp))
                }
            }
        }

        // --- 3. SPECIFIC MONTH/YEAR SELECTOR (CENTERED) ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { 
                    if (viewMode == "Month") viewModel.setStatsMonth(statsMonth.minusMonths(1))
                    else viewModel.setStatsMonth(statsMonth.minusYears(1))
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                }
                
                Text(
                    text = if (viewMode == "Month") 
                        "${statsMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }} ${statsMonth.year}" 
                        else statsMonth.year.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .clickable { showQuickPicker = true }
                )
                
                IconButton(onClick = { 
                    if (viewMode == "Month") viewModel.setStatsMonth(statsMonth.plusMonths(1))
                    else viewModel.setStatsMonth(statsMonth.plusYears(1))
                }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }
        }

        // --- 4. PREGNANCY SECTION (TOP IF ACTIVE) ---
        if (isPregModeActive || stats.pregnancyTagUsage.isNotEmpty() || stats.dailyWeight.isNotEmpty()) {
            item {
                ExpandableChartCard(
                    title = stringResource(R.string.pregnancy_mode),
                    expanded = pregnancyExpanded,
                    onToggle = { pregnancyExpanded = !pregnancyExpanded }
                ) {
                    // Weight Chart
                    val weightData = if (viewMode == "Month") {
                        stats.dailyWeight.map { it.first.toFloat() to it.second.toFloat() }
                    } else {
                        stats.monthlyWeight.map { it.first.toFloat() to it.second.toFloat() }
                    }

                    if (weightData.isNotEmpty() && !weightData.all { it.second == 0f }) {
                        Text(text = stringResource(R.string.weight), style = MaterialTheme.typography.labelSmall)
                        val validWeights = weightData.filter { it.second > 0 }
                        val minW = (validWeights.minOfOrNull { it.second } ?: 0f) - 2f
                        val maxW = (validWeights.maxOfOrNull { it.second } ?: 100f) + 2f
                        BarChart(
                            data = weightData,
                            minVal = minW.coerceAtLeast(0f),
                            maxVal = maxW,
                            color = Color(0xFF4CAF50),
                            labelSuffix = " kg",
                            xLabels = if (viewMode == "Year") romanMonths else null
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Pregnancy Tag Chart (Timeline in Month view)
                    val pregnancyTags = allTags.filter { it.category == "Pregnancy" }.map { it.name }
                    val currentPregUsage = if (viewMode == "Month") stats.pregnancyTagUsage else stats.yearTagUsage.filterKeys { it in pregnancyTags }
                    val selectedPregTags = selectedChartTags.filter { it in pregnancyTags }
                    
                    if (currentPregUsage.isNotEmpty() || selectedPregTags.isNotEmpty()) {
                        Text(text = stringResource(R.string.pregnancy_symptoms), style = MaterialTheme.typography.labelSmall)
                        
                        if (viewMode == "Month") {
                            TimelineChart(
                                days = statsMonth.lengthOfMonth(),
                                selectedTags = selectedPregTags,
                                dailyTags = stats.dailyTags,
                                color = Color(0xFF8BC34A)
                            )
                        } else {
                            val chartData = currentPregUsage.filterKeys { it in selectedPregTags }
                            val maxTagVal = (chartData.values.maxOrNull() ?: 5).toFloat().coerceAtLeast(1f)
                            BarChart(
                                data = chartData.toList().mapIndexed { index, pair -> index.toFloat() to pair.second.toFloat() },
                                minVal = 0f,
                                maxVal = ceil(maxTagVal),
                                color = Color(0xFF8BC34A),
                                isInteger = true,
                                xLabels = chartData.keys.toList()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    DisplayTagSelectors(
                        allTags = allTags.filter { it.category == "Pregnancy" },
                        currentUsage = currentPregUsage,
                        selectedTags = selectedChartTags,
                        onToggle = { tag ->
                            selectedChartTags = if (selectedChartTags.contains(tag)) selectedChartTags - tag else selectedChartTags + tag
                        }
                    )
                }
            }
        }

        // --- 5. BASAL TEMPERATURE SECTION ---
        item {
            ExpandableChartCard(
                title = stringResource(R.string.basal_temperature),
                expanded = basalExpanded,
                onToggle = { basalExpanded = !basalExpanded }
            ) {
                val tempData = if (viewMode == "Month") {
                    stats.dailyTemps.map { it.first.toFloat() to it.second.toFloat() }
                } else {
                    stats.monthlyTemps.map { it.first.toFloat() to it.second.toFloat() }
                }
                
                val finalTempData = if (viewMode == "Year" && tempData.isEmpty()) {
                    (1..12).map { it.toFloat() to 0f }
                } else tempData

                BarChart(
                    data = finalTempData,
                    minVal = 35f,
                    maxVal = 38f,
                    labelSuffix = "°",
                    xLabels = if (viewMode == "Year") romanMonths else null
                )
            }
        }

        // --- 6. SYMPTOMS SECTION ---
        item {
            ExpandableChartCard(
                title = stringResource(R.string.symptoms),
                expanded = symptomsExpanded,
                onToggle = { symptomsExpanded = !symptomsExpanded }
            ) {
                val symptomTags = allTags.filter { it.category == "Symptom" }.map { it.name }
                val currentUsage = if (viewMode == "Month") stats.tagUsage.filterKeys { it in symptomTags } else stats.yearTagUsage.filterKeys { it in symptomTags }
                val selectedSymptomTags = selectedChartTags.filter { it in symptomTags }
                
                Text(text = stringResource(R.string.tag_frequency), style = MaterialTheme.typography.labelSmall)
                
                if (viewMode == "Month") {
                    TimelineChart(
                        days = statsMonth.lengthOfMonth(),
                        selectedTags = selectedSymptomTags,
                        dailyTags = stats.dailyTags,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    val chartData = currentUsage.filterKeys { it in selectedSymptomTags }
                    val maxTagVal = (chartData.values.maxOrNull() ?: 5).toFloat().coerceAtLeast(1f)
                    BarChart(
                        data = chartData.toList().mapIndexed { index, pair -> index.toFloat() to pair.second.toFloat() },
                        minVal = 0f,
                        maxVal = ceil(maxTagVal),
                        color = MaterialTheme.colorScheme.primary,
                        isInteger = true,
                        xLabels = chartData.keys.toList()
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                DisplayTagSelectors(
                    allTags = allTags.filter { it.category == "Symptom" },
                    currentUsage = currentUsage,
                    selectedTags = selectedChartTags,
                    onToggle = { tag ->
                        selectedChartTags = if (selectedChartTags.contains(tag)) selectedChartTags - tag else selectedChartTags + tag
                    }
                )
                
                StatRow(label = stringResource(R.string.total_notes), value = stats.totalNotes.toString())
            }
        }

        // --- 7. SEXUAL ACTIVITY SECTION ---
        if (showAdvancedSex) {
            item {
                ExpandableChartCard(
                    title = stringResource(R.string.sex_activity),
                    expanded = sexExpanded,
                    onToggle = { sexExpanded = !sexExpanded }
                ) {
                    val sexTags = allTags.filter { it.category == "Sex" }.map { it.name }
                    val currentUsage = if (viewMode == "Month") stats.tagUsage.filterKeys { it in sexTags } else stats.yearTagUsage.filterKeys { it in sexTags }
                    val totalSexCount = if (viewMode == "Month") stats.totalSexCount else stats.yearTotalSexCount
                    val selectedSexTags = selectedChartTags.filter { it in sexTags }
                    
                    if (viewMode == "Month") {
                        // Timeline Heatmap for Sexual Activity
                        // We also want to include the base "Celkem" as a row if we follow the pattern
                        val labelsForTimeline = mutableListOf("Celkem") + selectedSexTags
                        
                        // We need a custom mapping for "Celkem" in dailyTags
                        val sexTimelineData = stats.dailyTags.mapValues { (_, tags) ->
                            // If record.sexFrequency > 0, we consider it a "Celkem" event
                            // (This is a bit simplified, but reflects the day activity)
                            tags
                        }
                        
                        // Actually, let's just use the selected tags for the timeline to keep it consistent
                        TimelineChart(
                            days = statsMonth.lengthOfMonth(),
                            selectedTags = selectedSexTags,
                            dailyTags = stats.dailyTags,
                            color = Color(0xFFE91E63)
                        )
                    } else {
                        // Yearly Bar Chart
                        BarChart(
                            data = stats.monthlySex.map { it.first.toFloat() to it.second.toFloat() },
                            minVal = 0f,
                            maxVal = ceil((stats.monthlySex.maxOfOrNull { it.second } ?: 5).toFloat()).coerceAtLeast(1f),
                            color = Color(0xFFE91E63),
                            isInteger = true,
                            xLabels = romanMonths
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    StatRow(label = stringResource(R.string.avg_frequency), value = stats.avgSexFreq.toInt().toString())

                    if (viewMode == "Year") {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Statistika roku ${statsMonth.year}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Celkem styků:", style = MaterialTheme.typography.bodyMedium)
                                    Text(stats.yearTotalSexCount.toString(), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Průměr na den:", style = MaterialTheme.typography.bodyMedium)
                                    Text("%.3f".format(stats.yearTotalSexCount.toFloat() / 365f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                                val count = stats.yearTotalSexCount
                                if (count > 0) {
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Interval:", style = MaterialTheme.typography.bodyMedium)
                                        Text("každý %.1f. den".format(365f / count.toFloat()), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Průměr týden / měsíc:", style = MaterialTheme.typography.bodyMedium)
                                        Text("%.1f / %.1f".format(count.toFloat() / (365f / 7f), count.toFloat() / 12f), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    
                    DisplayTagSelectors(
                        allTags = allTags.filter { it.category == "Sex" },
                        currentUsage = currentUsage,
                        selectedTags = selectedChartTags,
                        onToggle = { tag ->
                            selectedChartTags = if (selectedChartTags.contains(tag)) selectedChartTags - tag else selectedChartTags + tag
                        }
                    )

                    stats.sexTimeStats.forEach { (time, count) ->
                        val label = when(time) {
                            "Morning" -> stringResource(R.string.morning)
                            "Day" -> stringResource(R.string.day)
                            "Evening" -> stringResource(R.string.evening)
                            "Night" -> stringResource(R.string.night)
                            else -> time
                        }
                        StatRow(label = label, value = count.toString())
                    }
                }
            }
        }

        // --- 8. YEARLY COMPARISON ---
        if (showAdvancedSex && viewMode == "Year") {
            val filteredComparison = stats.yearlyComparison.filter { it.second > 0 }
            if (filteredComparison.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Srovnání průměrů mezi roky",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Visual bar chart for the comparison
                            BarChart(
                                data = filteredComparison.map { it.first.toFloat() to it.second.toFloat() },
                                minVal = 0f,
                                maxVal = ceil((filteredComparison.maxOfOrNull { it.second } ?: 10).toFloat()).coerceAtLeast(1f),
                                color = MaterialTheme.colorScheme.tertiary,
                                isInteger = true,
                                xLabels = filteredComparison.map { it.first.toString() }
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Table with detailed data
                            filteredComparison.forEach { (year, count) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = year.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "$count styků",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        val perDay = count.toFloat() / 365f
                                        val everyXDay = if (count > 0) 365f / count.toFloat() else 0f
                                        val perWeek = count.toFloat() / (365f / 7f)
                                        val perMonth = count.toFloat() / 12f

                                        Text(
                                            text = "ø %.3f / den (každý %.1f. den)".format(perDay, everyXDay),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "ø %.1f / týden | ø %.1f / měsíc".format(perWeek, perMonth),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                }
                                if (year != filteredComparison.last().first) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 4.dp),
                                        thickness = 0.5.dp,
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    if (showQuickPicker) {
        QuickMonthYearPicker(
            currentDate = statsMonth,
            viewMode = viewMode,
            onDateSelected = { 
                viewModel.setStatsMonth(it)
                showQuickPicker = false
            },
            onDismiss = { showQuickPicker = false }
        )
    }
}

@Composable
fun QuickMonthYearPicker(
    currentDate: YearMonth,
    viewMode: String,
    onDateSelected: (YearMonth) -> Unit,
    onDismiss: () -> Unit
) {
    var tempYear by remember { mutableIntStateOf(currentDate.year) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { tempYear-- }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null)
                }
                Text(text = tempYear.toString(), style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = { tempYear++ }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null)
                }
            }
        },
        text = {
            if (viewMode == "Month") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    val months = (1..12).chunked(3)
                    months.forEach { row ->
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            row.forEach { m ->
                                val monthName = Month.of(m).getDisplayName(TextStyle.SHORT, Locale.getDefault())
                                TextButton(
                                    onClick = { onDateSelected(YearMonth.of(tempYear, m)) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(
                                        text = monthName,
                                        color = if (m == currentDate.monthValue && tempYear == currentDate.year) 
                                            MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                Text(text = "Vyberte rok pomocí šipek výše a potvrďte.", style = MaterialTheme.typography.bodyMedium)
            }
        },
        confirmButton = {
            TextButton(onClick = { 
                if (viewMode == "Year") onDateSelected(YearMonth.of(tempYear, 1))
                else onDismiss()
            }) {
                Text(if (viewMode == "Year") "Vybrat rok" else "Zrušit")
            }
        }
    )
}

@Composable
fun ExpandableChartCard(title: String, expanded: Boolean, onToggle: () -> Unit, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column {
                    Spacer(modifier = Modifier.height(16.dp))
                    content()
                }
            }
        }
    }
}

@Composable
fun DisplayTagSelectors(
    allTags: List<TagDefinition>,
    currentUsage: Map<String, Int>,
    selectedTags: Set<String>,
    onToggle: (String) -> Unit
) {
    if (allTags.isEmpty()) return
    Column {
        allTags.forEach { tagDef ->
            val count = currentUsage[tagDef.name] ?: 0
            TagStatRow(
                tag = tagDef.name,
                count = count,
                isSelected = selectedTags.contains(tagDef.name),
                onToggle = { onToggle(tagDef.name) }
            )
        }
    }
}

@Composable
fun StatRow(label: String, value: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun TagStatRow(tag: String, count: Int, isSelected: Boolean, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { onToggle() }, shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text = tag, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Text(text = count.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun BarChart(data: List<Pair<Float, Float>>, minVal: Float, maxVal: Float, color: Color = Color.Blue, labelSuffix: String = "", xLabels: List<String>? = null, isInteger: Boolean = false) {
    val axisColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    var selectedBarIndex by remember { mutableStateOf(-1) }
    var touchPoint by remember { mutableStateOf(Offset.Zero) }

    Box(modifier = Modifier.fillMaxWidth().height(200.dp)) {
        Canvas(modifier = Modifier.fillMaxSize().padding(bottom = 24.dp, start = 80.dp, end = 8.dp)
            .pointerInput(data) {
                detectTapGestures { offset ->
                    val canvasWidth = size.width.toFloat()
                    val barWidthWithSpacing = if (data.isNotEmpty()) canvasWidth / data.size else 1f
                    val index = (offset.x / barWidthWithSpacing).toInt()
                    if (index in data.indices) {
                        selectedBarIndex = if (selectedBarIndex == index) -1 else index
                        touchPoint = offset
                    } else {
                        selectedBarIndex = -1
                    }
                }
            }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val steps = if (isInteger && (maxVal - minVal) < 5) (maxVal - minVal).toInt().coerceAtLeast(1) else 4
            for (i in 0..steps) {
                val y = canvasHeight - (canvasHeight / steps * i)
                val value = minVal + (maxVal - minVal) / steps * i
                drawLine(color = axisColor.copy(alpha = 0.2f), start = Offset(0f, y), end = Offset(canvasWidth, y), strokeWidth = 1f)
                val labelText = if (isInteger) "${value.toInt()}$labelSuffix" else "${"%.1f".format(value)}$labelSuffix"
                drawContext.canvas.nativeCanvas.drawText(
                    labelText,
                    -25f,
                    y + 10f,
                    Paint().apply {
                        this.color = textColor.hashCode()
                        this.textSize = 28f
                        this.textAlign = Paint.Align.RIGHT
                    }
                )
            }
            val barWidth = if (data.isNotEmpty()) (canvasWidth / data.size) * 0.6f else 0f
            val spacing = if (data.isNotEmpty()) (canvasWidth / data.size) * 0.4f else 0f
            data.forEachIndexed { index, point ->
                val barHeight = (((point.second - minVal).coerceAtLeast(0f)) / (maxVal - minVal)) * canvasHeight
                val x = (barWidth + spacing) * index + spacing / 2
                drawRect(
                    color = if (selectedBarIndex == index) color.copy(alpha = 0.7f) else color,
                    topLeft = Offset(x, canvasHeight - barHeight),
                    size = Size(barWidth, barHeight)
                )
                val label = xLabels?.getOrNull(index) ?: point.first.toInt().toString()
                drawContext.canvas.nativeCanvas.drawText(label, x + barWidth / 2, canvasHeight + 40f, Paint().apply { this.color = textColor.hashCode(); this.textSize = 24f; this.textAlign = Paint.Align.CENTER })
            }
        }

        if (selectedBarIndex != -1) {
            val value = data[selectedBarIndex].second
            val labelText = if (isInteger) "${value.toInt()}$labelSuffix" else "${"%.1f".format(value)}$labelSuffix"
            
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(touchPoint.x.roundToInt() + 60, touchPoint.y.roundToInt() - 40),
                onDismissRequest = { selectedBarIndex = -1 }
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = labelText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineChart(
    days: Int,
    selectedTags: List<String>,
    dailyTags: Map<Int, List<String>>,
    color: Color
) {
    val axisColor = MaterialTheme.colorScheme.outline
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant

    if (selectedTags.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
            Text("Vyberte štítky pro zobrazení časové osy", style = MaterialTheme.typography.bodySmall)
        }
        return
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height((selectedTags.size * 40 + 60).dp)
            .padding(bottom = 24.dp, start = 80.dp, end = 8.dp)
    ) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val rowHeight = canvasHeight / selectedTags.size
        val dayWidth = canvasWidth / days

        selectedTags.forEachIndexed { i, tag ->
            val y = rowHeight * i + rowHeight / 2
            
            drawContext.canvas.nativeCanvas.drawText(
                if (tag.length > 10) tag.take(8) + ".." else tag,
                -15f,
                y + 10f,
                Paint().apply {
                    this.color = textColor.hashCode()
                    this.textSize = 24f
                    this.textAlign = Paint.Align.RIGHT
                }
            )

            drawLine(
                color = axisColor.copy(alpha = 0.1f),
                start = Offset(0f, y),
                end = Offset(canvasWidth, y)
            )

            for (day in 1..days) {
                if (dailyTags[day]?.contains(tag) == true) {
                    val x = (day - 1) * dayWidth + dayWidth / 2
                    drawCircle(
                        color = color,
                        radius = 10f,
                        center = Offset(x, y)
                    )
                }
            }
        }

        for (day in listOf(1, 5, 10, 15, 20, 25, 30)) {
            if (day <= days) {
                val x = (day - 1) * dayWidth + dayWidth / 2
                drawContext.canvas.nativeCanvas.drawText(
                    day.toString(),
                    x,
                    canvasHeight + 35f,
                    Paint().apply {
                        this.color = textColor.hashCode()
                        this.textSize = 22f
                        this.textAlign = Paint.Align.CENTER
                    }
                )
            }
        }
    }
}
