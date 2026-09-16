package com.example.cykluscalk.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cykluscalk.R
import com.example.cykluscalk.data.DayRecord
import com.example.cykluscalk.data.TagDefinition
import com.example.cykluscalk.logic.CycleCalculator
import com.example.cykluscalk.logic.DayStatus
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayRecordScreen(viewModel: DayRecordViewModel) {
    val selectedDateStr by viewModel.selectedDate.collectAsState()
    val record by viewModel.currentRecord.collectAsState()
    val dayOfCycle by viewModel.dayOfCycle.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val allRecords by viewModel.allRecords.collectAsState()
    val allTags by viewModel.allTags.collectAsState()
    val tempUnit by viewModel.temperatureUnit.collectAsState()
    val minTemp by viewModel.minTemp.collectAsState()
    val maxTemp by viewModel.maxTemp.collectAsState()
    val showAdvancedSex by viewModel.showAdvancedSex.collectAsState()
    
    val selectedDate = LocalDate.parse(selectedDateStr)
    
    var calendarMonth by remember { mutableStateOf(YearMonth.from(selectedDate)) }
    var noteText by remember(selectedDateStr) { mutableStateOf(record?.note ?: "") }
    var showBttGuide by remember { mutableStateOf(false) }
    var showTempDialog by remember { mutableStateOf(false) }
    var showSexDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }
    var showDayDetailsDialog by remember { mutableStateOf(false) }
    var detailsDate by remember { mutableStateOf<LocalDate?>(null) }
    var showQuickPicker by remember { mutableStateOf(false) }
    
    LaunchedEffect(record) {
        if (record?.note != noteText) {
            noteText = record?.note ?: ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CalendarHeader(
            currentMonth = calendarMonth, 
            onMonthChange = { calendarMonth = it },
            onQuickPickClick = { showQuickPicker = true }
        )
        CalendarGrid(
            yearMonth = calendarMonth, 
            selectedDate = selectedDate, 
            allRecords = allRecords, 
            allTags = allTags, 
            insights = insights,
            milestones = insights.milestones,
            showAdvancedSex = showAdvancedSex,
            onDateClick = { date ->
                viewModel.selectDate(date)
                val dayRecord = allRecords.find { it.date == date.toString() }
                val hasMilestone = insights.milestones.values.contains(date.toString())
                
                // Show dialog if there's a record, OR if it's a pregnancy day, OR if it has a milestone
                if (dayRecord != null || hasMilestone || insights.isPregnancyMode) {
                    detailsDate = date
                    showDayDetailsDialog = true
                }
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- INFO CARD ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = dayOfCycle?.let { stringResource(R.string.cycle_day, it) } ?: stringResource(R.string.no_cycle),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    val statusLabel = when(insights.dayStatus) {
                        DayStatus.PERIOD -> stringResource(R.string.menstruation)
                        DayStatus.FERTILE -> stringResource(R.string.status_fertile)
                        DayStatus.OVULATION -> stringResource(R.string.status_ovulation)
                        DayStatus.PREGNANCY -> stringResource(R.string.status_pregnancy)
                        else -> stringResource(R.string.status_normal)
                    }
                    Text(text = stringResource(R.string.status, statusLabel), style = MaterialTheme.typography.bodySmall)
                }
                if (insights.isPregnancyMode) {
                    insights.dueDate?.let {
                        Text(text = stringResource(R.string.due_date, it), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                } else {
                    insights.nextPeriodDate?.let {
                        Text(text = stringResource(R.string.next_period, it), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (insights.isPregnancyMode) {
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF176).copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.pregnancy_week_day, insights.pregnancyWeek, insights.pregnancyDay),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF57F17)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    insights.dueDate?.let { Text(text = stringResource(R.string.due_date, it), style = MaterialTheme.typography.bodySmall) }
                    insights.doctorDueDate?.let { Text(text = stringResource(R.string.due_date_doctor, it), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold) }
                    insights.conceptionDate?.let { Text(text = stringResource(R.string.conception_date, it), style = MaterialTheme.typography.bodySmall) }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- CATEGORIES ---
        
        if (!insights.isPregnancyMode) {
            ExpandableSection(title = stringResource(R.string.menstruation)) {
                FilterChip(
                    selected = record?.tags?.contains("Menstruace") == true,
                    onClick = { viewModel.toggleTag("Menstruace") },
                    label = { Text(stringResource(R.string.period_today), modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.errorContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                )
            }
        } else {
            ExpandableSection(title = stringResource(R.string.pregnancy_symptoms)) {
                Column {
                    OutlinedCard(
                        onClick = { showWeightDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stringResource(R.string.weight), modifier = Modifier.weight(1f))
                            Text(
                                text = record?.bodyWeight?.let { "$it ${stringResource(R.string.weight_unit)}" } ?: "--",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    CategoryTagSelector(
                        categoryTags = allTags.filter { it.category == "Pregnancy" },
                        selectedTags = record?.tags ?: emptyList(),
                        onTagToggle = { viewModel.toggleTag(it) }
                    )
                }
            }
        }

        ExpandableSection(title = stringResource(R.string.symptoms)) {
            Column {
                OutlinedCard(
                    onClick = { showTempDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.basal_temperature), modifier = Modifier.weight(1f))
                        Text(
                            text = record?.basalTemperature?.let { "$it ${if (tempUnit == "C") "°C" else "°F"}" } ?: "--",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                CategoryTagSelector(
                    categoryTags = allTags.filter { it.category == "Symptom" },
                    selectedTags = record?.tags ?: emptyList(),
                    onTagToggle = { viewModel.toggleTag(it) }
                )
            }
        }

        if (showAdvancedSex) {
            ExpandableSection(title = stringResource(R.string.sex_activity)) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { showSexDialog = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = stringResource(R.string.sex_activity))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if ((record?.sexSunrise ?: 0) > 0) Icon(painterResource(R.drawable.ic_sunrise), null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    if ((record?.sexDay ?: 0) > 0) Icon(painterResource(R.drawable.ic_sun_medium), null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    if ((record?.sexSunset ?: 0) > 0) Icon(painterResource(R.drawable.ic_sunset), null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                    if ((record?.sexMoon ?: 0) > 0) Icon(painterResource(R.drawable.ic_moon), null, modifier = Modifier.size(16.dp), tint = Color.Gray)
                                }
                            }
                            Text(
                                text = record?.sexFrequency?.toString() ?: "0",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    IconButton(
                        onClick = { viewModel.incrementSexUnassigned() },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add Sex", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                CategoryTagSelector(
                    categoryTags = allTags.filter { it.category == "Sex" },
                    selectedTags = record?.tags ?: emptyList(),
                    onTagToggle = { viewModel.toggleTag(it) }
                )
            }
        }

        ExpandableSection(title = stringResource(R.string.note)) {
            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it; viewModel.saveNote(it) },
                placeholder = { Text(stringResource(R.string.note_placeholder)) },
                modifier = Modifier.fillMaxWidth().height(100.dp),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }

    // --- DIALOGS ---

    if (showTempDialog) {
        var tempInput by remember { mutableStateOf(record?.basalTemperature?.toString() ?: "") }
        var showWarning by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showTempDialog = false },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.temp_data_entry), modifier = Modifier.weight(1f))
                    IconButton(onClick = { showBttGuide = true }) {
                        Icon(painterResource(R.drawable.ic_info), null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            },
            text = {
                Column {
                    OutlinedTextField(
                        value = tempInput,
                        onValueChange = { if (it.length <= 5) tempInput = it.replace(",", ".") },
                        label = { Text(stringResource(R.string.basal_temperature)) },
                        suffix = { Text(if (tempUnit == "C") "°C" else "°F") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    if (showWarning) {
                        val range = if (tempUnit == "C") "${minTemp}-${maxTemp}°C" else "96.6–99.5°F"
                        Text(
                            text = stringResource(R.string.temp_out_of_range_warning, tempInput, range),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newTemp = tempInput.toDoubleOrNull()
                    if (newTemp != null && newTemp != 0.0) {
                        val outOfRange = if (tempUnit == "C") (newTemp !in minTemp.toDouble()..maxTemp.toDouble()) else (newTemp !in 96.6..99.5)
                        if (outOfRange && !showWarning) {
                            showWarning = true
                        } else {
                            viewModel.updateTemperature(newTemp)
                            showTempDialog = false
                        }
                    } else if (tempInput.isEmpty() || newTemp == 0.0) {
                        viewModel.updateTemperature(newTemp)
                        showTempDialog = false
                    }
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showTempDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showSexDialog) {
        var sunrInput by remember { mutableStateOf(record?.sexSunrise?.toString() ?: "") }
        var dayInput by remember { mutableStateOf(record?.sexDay?.toString() ?: "") }
        var sunsInput by remember { mutableStateOf(record?.sexSunset?.toString() ?: "") }
        var moonInput by remember { mutableStateOf(record?.sexMoon?.toString() ?: "") }
        var unasInput by remember { mutableStateOf(record?.sexUnassigned?.toString() ?: "") }

        AlertDialog(
            onDismissRequest = { showSexDialog = false },
            title = { Text(stringResource(R.string.sex_data_entry)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    SexInputRow(R.drawable.ic_sunrise, sunrInput) { if (it.length <= 2) sunrInput = it.filter { c -> c.isDigit() } }
                    SexInputRow(R.drawable.ic_sun_medium, dayInput) { if (it.length <= 2) dayInput = it.filter { c -> c.isDigit() } }
                    SexInputRow(R.drawable.ic_sunset, sunsInput) { if (it.length <= 2) sunsInput = it.filter { c -> c.isDigit() } }
                    SexInputRow(R.drawable.ic_moon, moonInput) { if (it.length <= 2) moonInput = it.filter { c -> c.isDigit() } }
                    
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = unasInput,
                            onValueChange = { if (it.length <= 2) unasInput = it.filter { c -> c.isDigit() } },
                            label = { Text(stringResource(R.string.unassigned_sex)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateSexData(
                        sunrInput.toIntOrNull() ?: 0,
                        dayInput.toIntOrNull() ?: 0,
                        sunsInput.toIntOrNull() ?: 0,
                        moonInput.toIntOrNull() ?: 0,
                        unasInput.toIntOrNull() ?: 0
                    )
                    showSexDialog = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showSexDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showBttGuide) {
        AlertDialog(
            onDismissRequest = { showBttGuide = false },
            title = { Text(stringResource(R.string.btt_guide_title)) },
            text = { Column(modifier = Modifier.verticalScroll(rememberScrollState())) { Text(stringResource(R.string.btt_guide_content), style = MaterialTheme.typography.bodySmall) } },
            confirmButton = { TextButton(onClick = { showBttGuide = false }) { Text(stringResource(R.string.confirm)) } }
        )
    }

    if (showWeightDialog) {
        var weightInput by remember { mutableStateOf(record?.bodyWeight?.toString() ?: "") }
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text(stringResource(R.string.weight_entry)) },
            text = {
                OutlinedTextField(
                    value = weightInput,
                    onValueChange = { if (it.length <= 6) weightInput = it.replace(",", ".") },
                    label = { Text(stringResource(R.string.weight)) },
                    suffix = { Text(stringResource(R.string.weight_unit)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateWeight(weightInput.toDoubleOrNull())
                    showWeightDialog = false
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = { TextButton(onClick = { showWeightDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showDayDetailsDialog && detailsDate != null) {
        val recordForDetails = allRecords.find { it.date == detailsDate.toString() }
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val dayMilestones = insights.milestones.filter { it.value == detailsDate.toString() }
        
        AlertDialog(
            onDismissRequest = { showDayDetailsDialog = false },
            title = { Text(stringResource(R.string.day_details_title, detailsDate!!.format(formatter))) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // --- Pregnancy Fetal Development Info (Prescribed Milestones) ---
                    if (insights.isPregnancyMode) {
                        // Calculate pregnancy week for the SPECIFIC day clicked, not the global insights.pregnancyWeek
                        val lmp = insights.conceptionDate?.let { 
                            try { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy")).minusDays(14) } catch(e: Exception) { null }
                        }
                        
                        if (lmp != null) {
                            val daysSinceLmp = ChronoUnit.DAYS.between(lmp, detailsDate!!)
                            if (daysSinceLmp >= 0) {
                                val weekOfSelectedDay = (daysSinceLmp / 7).toInt() + 1
                                val fetalMonth = fetalDevelopmentData.flatMap { it.months }
                                    .find { weekOfSelectedDay in it.weeks }
                                    
                                if (fetalMonth != null) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF176).copy(alpha = 0.3f)),
                                        border = BorderStroke(1.dp, Color(0xFFFBC02D))
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = Color(0xFFF57F17)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = stringResource(R.string.fetal_development_title),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color(0xFFF57F17),
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = stringResource(R.string.month_label_count, fetalMonth.month) + " " + stringResource(R.string.week_range, fetalMonth.weeks.first, fetalMonth.weeks.last),
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            FetalInfoRow(stringResource(R.string.size_label), fetalMonth.size)
                                            FetalInfoRow(stringResource(R.string.organs_label), fetalMonth.developedOrgans)
                                            FetalInfoRow(stringResource(R.string.future_organs_label), fetalMonth.willForm)
                                            FetalInfoRow(stringResource(R.string.reactions_label), fetalMonth.reactions)
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }
                        }
                    }

                    // --- User Milestones ---
                    if (dayMilestones.isNotEmpty()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF176).copy(alpha = 0.5f)),
                            border = BorderStroke(1.dp, Color(0xFFFBC02D))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = stringResource(R.string.pregnancy_milestones),
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF57F17),
                                    style = MaterialTheme.typography.titleSmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                dayMilestones.forEach { (key, _) ->
                                    val labelRes = when(key) {
                                        "milestone_positive_test" -> R.string.milestone_positive_test
                                        "milestone_first_doctor" -> R.string.milestone_first_doctor
                                        "milestone_first_ultrasound" -> R.string.milestone_first_ultrasound
                                        "milestone_heartbeat" -> R.string.milestone_heartbeat
                                        "milestone_gender" -> R.string.milestone_gender
                                        "milestone_first_kick" -> R.string.milestone_first_kick
                                        "milestone_name_selected" -> R.string.milestone_name_selected
                                        "milestone_hospital_bag" -> R.string.milestone_hospital_bag
                                        "milestone_birth" -> R.string.milestone_birth
                                        else -> null
                                    }
                                    if (labelRes != null) {
                                        Text(text = "• " + stringResource(labelRes), style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (recordForDetails != null) {
                        val filteredTags = recordForDetails.tags.filter { tagName ->
                            val tagDef = allTags.find { it.name == tagName }
                            when (tagDef?.category) {
                                "Sex" -> showAdvancedSex
                                "Pregnancy" -> insights.isPregnancyMode
                                else -> true
                            }
                        }

                        if (filteredTags.isNotEmpty()) {
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                filteredTags.forEach { tag ->
                                    AssistChip(onClick = {}, label = { Text(tag) })
                                }
                            }
                        }
                        
                        if (recordForDetails.note.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = stringResource(R.string.note) + ":", fontWeight = FontWeight.Bold)
                            Text(text = recordForDetails.note)
                        }

                        if (recordForDetails.basalTemperature != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.basal_temperature) + ": ${recordForDetails.basalTemperature} ${if (tempUnit == "C") "°C" else "°F"}")
                        }

                        if (recordForDetails.bodyWeight != null && insights.isPregnancyMode) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.weight) + ": ${recordForDetails.bodyWeight} kg")
                        }

                        if (recordForDetails.sexFrequency > 0 && showAdvancedSex) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = stringResource(R.string.sex_activity) + ": ${recordForDetails.sexFrequency}x")
                        }
                    } else {
                        Text(text = stringResource(R.string.no_data_for_day))
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showDayDetailsDialog = false }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }

    if (showQuickPicker) {
        QuickMonthYearPicker(
            currentDate = calendarMonth,
            viewMode = "Month",
            onDateSelected = { 
                calendarMonth = it
                showQuickPicker = false
            },
            onDismiss = { showQuickPicker = false }
        )
    }
}

@Composable
fun SexInputRow(iconRes: Int, value: String, onValueChange: (String) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Icon(painterResource(iconRes), null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.width(80.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
        )
    }
}

@Composable
fun ExpandableSection(title: String, content: @Composable () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Icon(imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            AnimatedVisibility(visible = expanded) { Column(modifier = Modifier.padding(8.dp)) { content() } }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryTagSelector(categoryTags: List<TagDefinition>, selectedTags: List<String>, onTagToggle: (String) -> Unit) {
    if (categoryTags.isEmpty()) {
        Text(stringResource(R.string.no_tags), style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    } else {
        FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            categoryTags.forEach { tag ->
                FilterChip(selected = selectedTags.contains(tag.name), onClick = { onTagToggle(tag.name) }, label = { Text(tag.name) })
            }
        }
    }
}

@Composable
fun CalendarHeader(currentMonth: YearMonth, onMonthChange: (YearMonth) -> Unit, onQuickPickClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, null) }
        Text(
            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault()).replaceFirstChar { it.uppercase() }} ${currentMonth.year}", 
            fontWeight = FontWeight.Bold, 
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.clickable { onQuickPickClick() }
        )
        IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) { Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null) }
    }
}

@Composable
fun CalendarGrid(yearMonth: YearMonth, selectedDate: LocalDate, allRecords: List<DayRecord>, allTags: List<TagDefinition>, insights: CycleInsights, milestones: Map<String, String>, showAdvancedSex: Boolean, onDateClick: (LocalDate) -> Unit) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value
    val offset = firstDayOfMonth - 1
    val totalCells = offset + daysInMonth
    val rows = (totalCells + 6) / 7
    
    // Get LMP date for fetal milestones
    val lmp = insights.conceptionDate?.let { 
        try { LocalDate.parse(it, DateTimeFormatter.ofPattern("dd.MM.yyyy")).minusDays(14) } catch(e: Exception) { null }
    }
    val milestoneStartWeeks = fetalDevelopmentData.flatMap { it.months }.map { it.weeks.first }.toSet()

    Column {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach {
                Text(text = it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        for (r in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (c in 0 until 7) {
                    val dayIndex = r * 7 + c - offset
                    if (dayIndex in 0 until daysInMonth) {
                        val date = yearMonth.atDay(dayIndex + 1)
                        val status = CycleCalculator.getDayStatus(date, allRecords)
                        val record = allRecords.find { it.date == date.toString() }
                        
                        // Check for fetal milestones (start of each month's week range)
                        var isFetalMilestone = false
                        if (insights.isPregnancyMode && lmp != null) {
                            val daysSinceLmp = ChronoUnit.DAYS.between(lmp, date)
                            if (daysSinceLmp >= 0 && daysSinceLmp % 7 == 0L) {
                                val weekNumber = (daysSinceLmp / 7).toInt() + 1
                                if (milestoneStartWeeks.contains(weekNumber)) {
                                    isFetalMilestone = true
                                }
                            }
                        }

                        val hasMilestone = milestones.values.contains(date.toString()) || isFetalMilestone
                        CalendarDay(
                            date = date, 
                            isSelected = date == selectedDate, 
                            isToday = date == LocalDate.now(), 
                            status = status, 
                            record = record, 
                            tagDefinitions = allTags, 
                            hasMilestone = hasMilestone, 
                            isPregnancyMode = insights.isPregnancyMode,
                            showAdvancedSex = showAdvancedSex,
                            onClick = { onDateClick(date) }
                        )
                    } else { Box(modifier = Modifier.weight(1f).aspectRatio(1f)) }
                }
            }
        }
    }
}

@Composable
fun RowScope.CalendarDay(
    date: LocalDate, 
    isSelected: Boolean, 
    isToday: Boolean, 
    status: DayStatus, 
    record: DayRecord?, 
    tagDefinitions: List<TagDefinition>, 
    hasMilestone: Boolean, 
    isPregnancyMode: Boolean,
    showAdvancedSex: Boolean,
    onClick: () -> Unit
) {
    val statusColor = when (status) {
        DayStatus.PERIOD -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f)
        DayStatus.FERTILE -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        DayStatus.OVULATION -> MaterialTheme.colorScheme.primaryContainer
        DayStatus.PREGNANCY -> Color(0xFFFFF176).copy(alpha = 0.6f)
        else -> Color.Transparent
    }
    val tags = record?.tags ?: emptyList()
    val hasNote = record?.note?.isNotBlank() == true
    Box(
        modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)
            .then(if (isToday) Modifier.border(1.5.dp, MaterialTheme.colorScheme.outline, CircleShape) else Modifier)
            .background(statusColor, shape = CircleShape)
            .then(if (isSelected) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), shape = CircleShape) else Modifier)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (hasMilestone) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                modifier = Modifier.size(24.dp).align(Alignment.Center),
                tint = Color(0xFFFBC02D).copy(alpha = 0.4f)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = date.dayOfMonth.toString(), fontSize = 14.sp, fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.padding(top = 1.dp)) {
                if (tags.any { tag -> tagDefinitions.find { it.name == tag }?.category == "Symptom" }) Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                
                // Only show sex dot if advanced tracking is enabled
                if (showAdvancedSex && (tags.any { tag -> tagDefinitions.find { it.name == tag }?.category == "Sex" } || (record?.sexFrequency ?: 0) > 0)) {
                    Box(modifier = Modifier.size(4.dp).background(Color(0xFFE91E63), CircleShape))
                }
                
                if (hasNote) Box(modifier = Modifier.size(4.dp).background(MaterialTheme.colorScheme.tertiary, CircleShape))
                
                // Only show pregnancy dots if pregnancy mode is enabled
                if (isPregnancyMode) {
                    if (tags.any { tag -> tagDefinitions.find { it.name == tag }?.category == "Pregnancy" }) {
                        Box(modifier = Modifier.size(4.dp).background(Color(0xFF8BC34A), CircleShape))
                    }
                    if (record?.bodyWeight != null) {
                        Box(modifier = Modifier.size(4.dp).background(Color(0xFF4CAF50), CircleShape))
                    }
                }
                
                if (record?.basalTemperature != null) Box(modifier = Modifier.size(4.dp).background(Color(0xFFFF9800), CircleShape))
            }
        }
    }
}
