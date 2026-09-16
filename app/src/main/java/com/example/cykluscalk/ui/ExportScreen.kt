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
    val context = LocalContext.current

    var headerTitle by remember { mutableStateOf(context.getString(R.string.pdf_report_title)) }
    var expMenses by remember { mutableStateOf(true) }
    var expSymp by remember { mutableStateOf(true) }
    var expTemp by remember { mutableStateOf(true) }
    var expPreg by remember { mutableStateOf(true) }
    var expSex by remember { mutableStateOf(true) }
    var expNotes by remember { mutableStateOf(true) }
    
    var expYear by remember { mutableIntStateOf(LocalDate.now().year) }
    var expMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var expAllTime by remember { mutableStateOf(false) }

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
            context.contentResolver.openOutputStream(it)?.use { out ->
                viewModel.exportPdfReport(
                    outputStream = out,
                    title = headerTitle,
                    generatedAtLabel = context.getString(R.string.pdf_generated_at),
                    tempUnit = if (tempUnit == "C") "°C" else "°F",
                    includeMenstruation = expMenses,
                    includeSymptoms = expSymp,
                    includeTemperature = expTemp,
                    includePregnancy = expPreg,
                    includeSex = expSex,
                    includeNotes = expNotes,
                    filterMonth = if (expAllTime) null else expMonth,
                    filterYear = if (expAllTime) null else expYear,
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
                ExportCheckbox(stringResource(R.string.export_all_time), expAllTime) { expAllTime = it }
                
                if (!expAllTime) {
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
