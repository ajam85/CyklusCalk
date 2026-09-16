package com.example.cykluscalk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.cykluscalk.ui.DayRecordScreen
import com.example.cykluscalk.ui.DayRecordViewModel
import com.example.cykluscalk.ui.SettingsScreen
import com.example.cykluscalk.ui.PregnancyScreen
import com.example.cykluscalk.ui.StatisticsScreen
import com.example.cykluscalk.ui.ExportScreen
import com.example.cykluscalk.ui.SplashScreen
import com.example.cykluscalk.ui.theme.CyklusCalkTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.*

enum class Screen {
    Main, Settings, Statistics, Export, Pregnancy
}

@OptIn(ExperimentalMaterial3Api::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val viewModel: DayRecordViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply initial language
        updateLocale(viewModel.language.value)
        
        enableEdgeToEdge()
        setContent {
            var showSplash by remember { mutableStateOf(true) }
            val isDarkModePref by viewModel.isDarkMode.collectAsState()
            val language by viewModel.language.collectAsState()
            val appTheme by viewModel.appTheme.collectAsState()
            
            // Recreate activity on language change to ensure resources are reloaded
            LaunchedEffect(language) {
                val currentLocale = resources.configuration.locales[0]
                if (currentLocale.language != language) {
                    updateLocale(language)
                    recreate()
                }
            }

            CyklusCalkTheme(darkTheme = isDarkModePref, appTheme = appTheme) {
                if (showSplash) {
                    SplashScreen(onTimeout = { showSplash = false })
                } else {
                    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                    val scope = rememberCoroutineScope()
                    var currentScreen by remember { mutableStateOf(Screen.Main) }

                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet {
                                Spacer(modifier = Modifier.height(16.dp))
                                NavigationDrawerItem(
                                    icon = { Icon(painterResource(R.drawable.ic_calendar), null) },
                                    label = { Text(stringResource(R.string.calendar)) },
                                    selected = currentScreen == Screen.Main,
                                    onClick = {
                                        currentScreen = Screen.Main
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(painterResource(R.drawable.ic_chart), null) },
                                    label = { Text(stringResource(R.string.statistics)) },
                                    selected = currentScreen == Screen.Statistics,
                                    onClick = {
                                        currentScreen = Screen.Statistics
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(painterResource(R.drawable.ic_export), null) },
                                    label = { Text(stringResource(R.string.export_data)) },
                                    selected = currentScreen == Screen.Export,
                                    onClick = {
                                        currentScreen = Screen.Export
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(painterResource(R.drawable.ic_baby), null) },
                                    label = { Text(stringResource(R.string.pregnancy)) },
                                    selected = currentScreen == Screen.Pregnancy,
                                    onClick = {
                                        currentScreen = Screen.Pregnancy
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    icon = { Icon(painterResource(R.drawable.ic_settings), null) },
                                    label = { Text(stringResource(R.string.settings)) },
                                    selected = currentScreen == Screen.Settings,
                                    onClick = {
                                        currentScreen = Screen.Settings
                                        scope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                            }
                        }
                    ) {
                        Scaffold(
                            topBar = {
                                TopAppBar(
                                    title = { Text(stringResource(R.string.app_name)) },
                                    navigationIcon = {
                                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                                        }
                                    }
                                )
                            }
                        ) { innerPadding ->
                            Box(modifier = Modifier.padding(innerPadding)) {
                                when (currentScreen) {
                                    Screen.Main -> DayRecordScreen(viewModel = viewModel)
                                    Screen.Settings -> SettingsScreen(viewModel = viewModel)
                                    Screen.Statistics -> StatisticsScreen(viewModel = viewModel)
                                    Screen.Export -> ExportScreen(viewModel = viewModel)
                                    Screen.Pregnancy -> PregnancyScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun updateLocale(lang: String) {
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
    }
}
