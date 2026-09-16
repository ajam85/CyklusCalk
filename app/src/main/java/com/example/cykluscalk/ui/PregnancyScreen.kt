package com.example.cykluscalk.ui

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.cykluscalk.R
import com.example.cykluscalk.data.PregnancyRecord
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun PregnancyScreen(viewModel: DayRecordViewModel) {
    val isPregnancyMode by viewModel.isPregnancyMode.collectAsState()
    val insights by viewModel.insights.collectAsState()
    val history by viewModel.pregnancyHistory.collectAsState()
    val context = LocalContext.current
    var showEndPregnancyDialog by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = stringResource(R.string.pregnancy_mode),
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (history.isNotEmpty()) {
                            IconButton(onClick = { showHistory = !showHistory }) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = "History",
                                    tint = if (showHistory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.pregnancy_mode), modifier = Modifier.weight(1f))
                        Switch(checked = isPregnancyMode, onCheckedChange = { enabled ->
                            if (enabled) {
                                val now = LocalDate.now()
                                DatePickerDialog(context, { _, y, m, d ->
                                    viewModel.setPregnancyMode(true, LocalDate.of(y, m + 1, d))
                                }, now.year, now.monthValue - 1, now.dayOfMonth).show()
                            } else {
                                showEndPregnancyDialog = true
                            }
                        })
                    }

                    if (showEndPregnancyDialog) {
                        AlertDialog(
                            onDismissRequest = { showEndPregnancyDialog = false },
                            title = { Text(stringResource(R.string.end_pregnancy)) },
                            text = { Text(stringResource(R.string.end_pregnancy_dialog)) },
                            confirmButton = {
                                Button(onClick = {
                                    viewModel.setPregnancyMode(false)
                                    showEndPregnancyDialog = false
                                }) { Text(stringResource(R.string.confirm)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { showEndPregnancyDialog = false }) { 
                                    Text(stringResource(R.string.cancel)) 
                                }
                            }
                        )
                    }

                    if (isPregnancyMode) {
                        Spacer(modifier = Modifier.height(24.dp))
                        HorizontalDivider(thickness = 0.5.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Termíny a výpočty",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        // --- Datum početí ---
                        TextButton(onClick = {
                            val now = LocalDate.now()
                            DatePickerDialog(context, { _, y, m, d ->
                                viewModel.setConceptionDate(LocalDate.of(y, m + 1, d))
                            }, now.year, now.monthValue - 1, now.dayOfMonth).show()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (insights.conceptionDate != null) 
                                stringResource(R.string.conception_date, insights.conceptionDate!!)
                                else "Nastavit předpokládané datum početí")
                        }

                        // --- Výpočet porodu z početí ---
                        if (insights.conceptionDate != null && insights.dueDate != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = stringResource(R.string.due_date, insights.dueDate!!),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "(vypočítáno jako datum početí + 266 dní)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // --- Termín od lékaře ---
                        TextButton(onClick = {
                            val now = LocalDate.now()
                            DatePickerDialog(context, { _, y, m, d ->
                                viewModel.setDoctorDueDate(LocalDate.of(y, m + 1, d))
                            }, now.year, now.monthValue - 1, now.dayOfMonth).show()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = if (insights.doctorDueDate != null) 
                                stringResource(R.string.due_date_doctor, insights.doctorDueDate!!)
                                else stringResource(R.string.set_doctor_due_date))
                        }
                    }
                }
            }
        }

        if (showHistory && !isPregnancyMode) {
            item {
                Text(
                    text = stringResource(R.string.pregnancy_history),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            items(history) { record ->
                HistoryItem(record = record, onDelete = { viewModel.deletePregnancyFromHistory(record) })
            }
        }

        if (isPregnancyMode) {
            item {
                Text(
                    text = stringResource(R.string.pregnancy_milestones),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
            }

            val milestoneList = listOf(
                "milestone_positive_test" to R.string.milestone_positive_test,
                "milestone_first_doctor" to R.string.milestone_first_doctor,
                "milestone_first_ultrasound" to R.string.milestone_first_ultrasound,
                "milestone_heartbeat" to R.string.milestone_heartbeat,
                "milestone_gender" to R.string.milestone_gender,
                "milestone_first_kick" to R.string.milestone_first_kick,
                "milestone_name_selected" to R.string.milestone_name_selected,
                "milestone_hospital_bag" to R.string.milestone_hospital_bag,
                "milestone_birth" to R.string.milestone_birth
            )

            milestoneList.forEach { (key, labelRes) ->
                item {
                    val dateStr = insights.milestones[key]
                    MilestoneItem(
                        label = stringResource(labelRes),
                        date = dateStr,
                        onSetDate = {
                            val now = LocalDate.now()
                            DatePickerDialog(context, { _, y, m, d ->
                                viewModel.updateMilestone(key, LocalDate.of(y, m + 1, d))
                            }, now.year, now.monthValue - 1, now.dayOfMonth).show()
                        },
                        onClear = { viewModel.updateMilestone(key, null) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.fetal_development_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                )
                Text(
                    text = stringResource(R.string.fetal_development_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            fetalDevelopmentData.forEach { trimester ->
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = trimester.name + " " + stringResource(R.string.week_range, trimester.range.first, trimester.range.last),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
                }
                trimester.months.forEach { month ->
                    item {
                        FetalMonthCard(month, insights.pregnancyWeek)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = stringResource(R.string.fetal_development_source),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }
        }
    }
}

@Composable
fun MilestoneItem(label: String, date: String?, onSetDate: () -> Unit, onClear: () -> Unit) {
    OutlinedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        onClick = onSetDate,
        border = BorderStroke(1.dp, if (date != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
                if (date != null) {
                    Text(
                        text = date,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (date != null) {
                IconButton(onClick = onClear) {
                    Icon(Icons.Default.Delete, contentDescription = "Clear", tint = MaterialTheme.colorScheme.error)
                }
            } else {
                Icon(Icons.Default.Add, contentDescription = "Set Date", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun HistoryItem(record: PregnancyRecord, onDelete: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val displayDate = try {
        val start = record.conceptionDate?.let { LocalDate.parse(it).format(formatter) } ?: "?"
        val end = record.endDate?.let { LocalDate.parse(it).format(formatter) } ?: "?"
        "$start – $end"
    } catch (e: Exception) {
        "Neznámé datum"
    }

    OutlinedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = displayDate, fontWeight = FontWeight.Bold)
                if (!record.doctorDueDate.isNullOrBlank()) {
                    Text(
                        text = "Termín: ${LocalDate.parse(record.doctorDueDate).format(formatter)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun FetalMonthCard(month: FetalDevelopmentMonth, currentWeek: Int) {
    val isCurrent = currentWeek in month.weeks
    // Žluté zvýraznění pro aktuální měsíc
    val backgroundColor = if (isCurrent) Color(0xFFFFF176).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface
    val borderColor = if (isCurrent) Color(0xFFFBC02D) else MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(if (isCurrent) 2.dp else 1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isCurrent) 4.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.month_label_count, month.month),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isCurrent) Color(0xFFF57F17) else MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.week_range, month.weeks.first, month.weeks.last),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            FetalInfoRow(stringResource(R.string.size_label), month.size)
            FetalInfoRow(stringResource(R.string.organs_label), month.developedOrgans)
            FetalInfoRow(stringResource(R.string.future_organs_label), month.willForm)
            FetalInfoRow(stringResource(R.string.reactions_label), month.reactions)
        }
    }
}

@Composable
fun FetalInfoRow(label: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
