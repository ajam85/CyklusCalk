package com.example.cykluscalk.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cykluscalk.R
import com.example.cykluscalk.data.TagDefinition
import java.time.LocalDate

@Composable
fun SettingsScreen(viewModel: DayRecordViewModel) {
    val allTags by viewModel.allTags.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val language by viewModel.language.collectAsState()
    val showAdvancedSex by viewModel.showAdvancedSex.collectAsState()
    val isPregnancyMode by viewModel.isPregnancyMode.collectAsState()
    val tempUnit by viewModel.temperatureUnit.collectAsState()
    val appTheme by viewModel.appTheme.collectAsState()
    val minTemp by viewModel.minTemp.collectAsState()
    val maxTemp by viewModel.maxTemp.collectAsState()
    
    var newTagName by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Symptom") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- APP SETTINGS ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.dark_mode), modifier = Modifier.weight(1f))
                        Switch(checked = isDarkMode, onCheckedChange = { viewModel.toggleDarkMode(it) })
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.language), modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.setLanguage(if (language == "cs") "en" else "cs") }) {
                            Text(text = if (language == "cs") stringResource(R.string.czech) else stringResource(R.string.english))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.theme_selection), modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.setAppTheme(if (appTheme == "BLUE") "PINK" else "BLUE") }) {
                            Text(text = if (appTheme == "BLUE") stringResource(R.string.theme_blue) else stringResource(R.string.theme_pink))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = stringResource(R.string.advanced_sex_tracking), modifier = Modifier.weight(1f))
                        Switch(checked = showAdvancedSex, onCheckedChange = { viewModel.setShowAdvancedSex(it) })
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(text = stringResource(R.string.temperature_unit), style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = tempUnit == "C", onClick = { viewModel.setTemperatureUnit("C") })
                        Text(stringResource(R.string.celsius), modifier = Modifier.clickable { viewModel.setTemperatureUnit("C") })
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(selected = tempUnit == "F", onClick = { viewModel.setTemperatureUnit("F") })
                        Text(stringResource(R.string.fahrenheit), modifier = Modifier.clickable { viewModel.setTemperatureUnit("F") })
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(text = "Validní rozmezí teploty (${if (tempUnit == "C") "°C" else "°F"})", style = MaterialTheme.typography.bodyMedium)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minTemp.toString(),
                            onValueChange = { 
                                val newVal = it.replace(",", ".").toFloatOrNull() ?: minTemp
                                viewModel.setTempRange(newVal, maxTemp)
                            },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = maxTemp.toString(),
                            onValueChange = { 
                                val newVal = it.replace(",", ".").toFloatOrNull() ?: maxTemp
                                viewModel.setTempRange(minTemp, newVal)
                            },
                            label = { Text("Max") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // --- APP INFO & VERSION ---
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.app_version),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.developer_info),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // --- ADD NEW TAG ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.add_new_tag), style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = newTagName,
                        onValueChange = { newTagName = it },
                        label = { Text(stringResource(R.string.tag_name)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Fixed Categories Layout to prevent deformation
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                            RadioButton(selected = selectedCategory == "Symptom", onClick = { selectedCategory = "Symptom" })
                            Text(stringResource(R.string.category_symptom), modifier = Modifier.clickable { selectedCategory = "Symptom" }, maxLines = 1)
                        }
                        if (showAdvancedSex) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                RadioButton(selected = selectedCategory == "Sex", onClick = { selectedCategory = "Sex" })
                                Text(stringResource(R.string.category_sex), modifier = Modifier.clickable { selectedCategory = "Sex" }, maxLines = 1)
                            }
                        }
                        if (isPregnancyMode) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                RadioButton(selected = selectedCategory == "Pregnancy", onClick = { selectedCategory = "Pregnancy" })
                                Text(stringResource(R.string.pregnancy_mode), modifier = Modifier.clickable { selectedCategory = "Pregnancy" }, maxLines = 1)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (newTagName.isNotBlank()) {
                                viewModel.addNewTagDefinition(newTagName.trim(), selectedCategory)
                                newTagName = ""
                            }
                        },
                        modifier = Modifier.align(Alignment.End),
                        enabled = newTagName.isNotBlank()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.save))
                    }
                }
            }
        }

        item {
            Text(text = stringResource(R.string.existing_tags), style = MaterialTheme.typography.titleLarge)
        }

        items(allTags.filter { tag ->
            when (tag.category) {
                "Sex" -> showAdvancedSex
                "Pregnancy" -> isPregnancyMode
                else -> true
            }
        }) { tag ->
            TagSettingItem(tag = tag, onDelete = {
                viewModel.deleteTagDefinition(tag) 
            })
        }
    }
}

@Composable
fun TagSettingItem(tag: TagDefinition, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val categoryLabel = when(tag.category) {
                "Sex" -> stringResource(R.string.category_sex)
                "Pregnancy" -> stringResource(R.string.pregnancy_mode)
                else -> stringResource(R.string.category_symptom)
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Text(text = tag.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = categoryLabel, style = MaterialTheme.typography.labelSmall)
            }
            IconButton(onClick = onDelete) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_trash),
                    contentDescription = stringResource(R.string.delete),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
