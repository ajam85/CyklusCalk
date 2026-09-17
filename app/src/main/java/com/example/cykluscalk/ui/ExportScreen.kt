package com.example.cykluscalk.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.cykluscalk.R
import java.time.LocalDate

@Composable
fun ExportScreen(viewModel: DayRecordViewModel) {
    val tempUnit by viewModel.temperatureUnit.collectAsState()
    val isPregnancyMode by viewModel.isPregnancyMode.collectAsState()
    val showAdvancedSex by viewModel.showAdvancedSex.collectAsState()
    val savedUserName by viewModel.userName.collectAsState()
    val savedUserBirth by viewModel.userBirth.collectAsState()
    val savedReportTitle by viewModel.lastReportTitle.collectAsState()
    
    val context = LocalContext.current

    var headerTitle by remember(savedReportTitle) { 
        mutableStateOf(if (savedReportTitle.isBlank()) context.getString(R.string.pdf_report_title) else savedReportTitle) 
    }
    var userName by remember(savedUserName) { mutableStateOf(savedUserName) }
    var userBirth by remember(savedUserBirth) { mutableStateOf(savedUserBirth) }
    
    var expMenses by remember { mutableStateOf(true) }
    var expSymp by remember { mutableStateOf(true) }
    var expTemp by remember { mutableStateOf(true) }
    var expPreg by remember { mutableStateOf(true) }
    var expSex by remember { mutableStateOf(true) }
    var expNotes by remember { mutableStateOf(true) }
    
    // 0 = All Time, 1 = Single Month, 2 = Range
    var dateRangeMode by remember { mutableIntStateOf(0) }
    
    var expYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var expMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }

    var startYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var startMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var endYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var endMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        uri?.let {
            val json = viewModel.exportBackupJson()
            context.contentResolver.openOutputStream(it)?.use { out ->
                out.write(json.toByteArray())
            }
            Toast.makeText(context, context.getString(R.string.backup_success), Toast.LENGTH_SHORT).show()
        }
    }

    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            context.contentResolver.openInputStream(it)?.use { input ->
                val json = input.bufferedReader().readText()
                if (viewModel.importBackupJson(json)) {
                    Toast.makeText(context, context.getString(R.string.import_success), Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, context.getString(R.string.import_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val pdfLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let {
            // Save user info and title for future use before exporting
            viewModel.setUserName(userName)
            viewModel.setUserBirth(userBirth)
            viewModel.setLastReportTitle(headerTitle)
            
            context.contentResolver.openOutputStream(it)?.use { out ->
                viewModel.exportPdfReport(
                    context = context,
                    outputStream = out,
                    title = headerTitle,
                    userName = userName,
                    userBirth = userBirth,
                    generatedAtLabel = context.getString(R.string.pdf_generated_at),
                    tempUnit = if (tempUnit == "C") "°C" else "°F",
                    includeMenstruation = expMenses,
                    includeSymptoms = expSymp,
                    includeTemperature = expTemp,
                    includePregnancy = expPreg,
                    includeSex = expSex,
                    includeNotes = expNotes,
                    filterMonth = if (dateRangeMode == 1) expMonth else null,
                    filterYear = if (dateRangeMode == 1) expYear else null,
                    startMonth = if (dateRangeMode == 2) startMonth else null,
                    startYear = if (dateRangeMode == 2) startYear else null,
                    endMonth = if (dateRangeMode == 2) endMonth else null,
                    endYear = if (dateRangeMode == 2) endYear else null,
                    footerText = context.getString(R.string.pdf_footer_note)
                )
            }
            Toast.makeText(context, context.getString(R.string.backup_success), Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = stringResource(R.string.export_data), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.pdf_header_title), style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = headerTitle,
                    onValueChange = { headerTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = userName,
                        onValueChange = { userName = it },
                        label = { Text(stringResource(R.string.pdf_user_name)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = userBirth,
                        onValueChange = { userBirth = it },
                        label = { Text(stringResource(R.string.pdf_user_birth)) },
                        placeholder = { Text("DD.MM.RRRR") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                HorizontalDivider()

                Text(text = stringResource(R.string.select_export_sections), style = MaterialTheme.typography.titleMedium)
                ExportCheckbox(stringResource(R.string.export_menstruation), expMenses) { expMenses = it }
                ExportCheckbox(stringResource(R.string.export_symptoms), expSymp) { expSymp = it }
                ExportCheckbox(stringResource(R.string.export_temperature), expTemp) { expTemp = it }
                if (isPregnancyMode) {
                    ExportCheckbox(stringResource(R.string.export_pregnancy), expPreg) { expPreg = it }
                }
                if (showAdvancedSex) {
                    ExportCheckbox(stringResource(R.string.export_sex), expSex) { expSex = it }
                }
                ExportCheckbox(stringResource(R.string.export_notes), expNotes) { expNotes = it }

                HorizontalDivider()

                Text("Rozsah dat", style = MaterialTheme.typography.titleMedium)
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = dateRangeMode == 0, onClick = { dateRangeMode = 0 })
                    Text(text = stringResource(R.string.export_all_time), modifier = Modifier.clickable { dateRangeMode = 0 })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = dateRangeMode == 1, onClick = { dateRangeMode = 1 })
                    Text(text = "Konkrétní měsíc", modifier = Modifier.clickable { dateRangeMode = 1 })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = dateRangeMode == 2, onClick = { dateRangeMode = 2 })
                    Text(text = "Časové rozmezí", modifier = Modifier.clickable { dateRangeMode = 2 })
                }
                
                if (dateRangeMode == 1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = expMonth.toString(),
                            onValueChange = { expMonth = it.filter { c -> c.isDigit() }.toIntOrNull() ?: expMonth },
                            label = { Text(stringResource(R.string.month_label)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = expYear.toString(),
                            onValueChange = { expYear = it.filter { c -> c.isDigit() }.toIntOrNull() ?: expYear },
                            label = { Text(stringResource(R.string.year_label)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                } else if (dateRangeMode == 2) {
                    Text("Od:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startMonth.toString(),
                            onValueChange = { startMonth = it.filter { c -> c.isDigit() }.toIntOrNull() ?: startMonth },
                            label = { Text(stringResource(R.string.month_label)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = startYear.toString(),
                            onValueChange = { startYear = it.filter { c -> c.isDigit() }.toIntOrNull() ?: startYear },
                            label = { Text(stringResource(R.string.year_label)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Text("Do:", style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = endMonth.toString(),
                            onValueChange = { endMonth = it.filter { c -> c.isDigit() }.toIntOrNull() ?: endMonth },
                            label = { Text(stringResource(R.string.month_label)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = endYear.toString(),
                            onValueChange = { endYear = it.filter { c -> c.isDigit() }.toIntOrNull() ?: endYear },
                            label = { Text(stringResource(R.string.year_label)) },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Button(
                    onClick = { pdfLauncher.launch("cyklus_report_${LocalDate.now()}.pdf") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.PictureAsPdf, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.export_pdf))
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = stringResource(R.string.backup_restore), style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { exportLauncher.launch("cyklus_backup_${LocalDate.now()}.json") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.SaveAlt, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.export_json))
                    }
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.FileUpload, null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.import_json))
                    }
                }
            }
        }
    }
}

@Composable
fun ExportCheckbox(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { onCheckedChange(!checked) }) {
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}
