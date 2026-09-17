package com.example.cykluscalk.data

import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode

    private val _language = MutableStateFlow(prefs.getString("language", "cs") ?: "cs")
    val language: StateFlow<String> = _language

    private val _showAdvancedSex = MutableStateFlow(prefs.getBoolean("show_advanced_sex", false))
    val showAdvancedSex: StateFlow<Boolean> = _showAdvancedSex

    private val _minTemp = MutableStateFlow(prefs.getFloat("min_temp", 35.9f))
    val minTemp: StateFlow<Float> = _minTemp

    private val _maxTemp = MutableStateFlow(prefs.getFloat("max_temp", 37.5f))
    val maxTemp: StateFlow<Float> = _maxTemp

    private val _temperatureUnit = MutableStateFlow(prefs.getString("temp_unit", "C") ?: "C")
    val temperatureUnit: StateFlow<String> = _temperatureUnit

    private val _appTheme = MutableStateFlow(prefs.getString("app_theme", "BLUE") ?: "BLUE")
    val appTheme: StateFlow<String> = _appTheme

    private val _isPregnancyMode = MutableStateFlow(prefs.getBoolean("pregnancy_mode", false))
    val isPregnancyMode: StateFlow<Boolean> = _isPregnancyMode

    private val _conceptionDate = MutableStateFlow(prefs.getString("conception_date", null))
    val conceptionDate: StateFlow<String?> = _conceptionDate

    private val _doctorDueDate = MutableStateFlow(prefs.getString("doctor_due_date", null))
    val doctorDueDate: StateFlow<String?> = _doctorDueDate

    private val _pregnancyMilestones = MutableStateFlow(prefs.getString("pregnancy_milestones", "{}") ?: "{}")
    val pregnancyMilestones: StateFlow<String> = _pregnancyMilestones

    private val _userName = MutableStateFlow(prefs.getString("user_name", "") ?: "")
    val userName: StateFlow<String> = _userName

    private val _userBirth = MutableStateFlow(prefs.getString("user_birth", "") ?: "")
    val userBirth: StateFlow<String> = _userBirth

    private val _lastReportTitle = MutableStateFlow(prefs.getString("last_report_title", "") ?: "")
    val lastReportTitle: StateFlow<String> = _lastReportTitle

    fun setDarkMode(enabled: Boolean) {
        prefs.edit().putBoolean("dark_mode", enabled).apply()
        _isDarkMode.value = enabled
    }

    fun setLanguage(lang: String) {
        prefs.edit().putString("language", lang).apply()
        _language.value = lang
    }

    fun setShowAdvancedSex(enabled: Boolean) {
        prefs.edit().putBoolean("show_advanced_sex", enabled).apply()
        _showAdvancedSex.value = enabled
    }

    fun setTemperatureUnit(unit: String) {
        prefs.edit().putString("temp_unit", unit).apply()
        _temperatureUnit.value = unit
    }

    fun setAppTheme(theme: String) {
        prefs.edit().putString("app_theme", theme).apply()
        _appTheme.value = theme
    }

    fun setTempRange(min: Float, max: Float) {
        prefs.edit().putFloat("min_temp", min).putFloat("max_temp", max).apply()
        _minTemp.value = min
        _maxTemp.value = max
    }

    fun setPregnancyMode(enabled: Boolean, date: String? = null) {
        prefs.edit().putBoolean("pregnancy_mode", enabled).putString("conception_date", date).apply()
        _isPregnancyMode.value = enabled
        _conceptionDate.value = date
    }

    fun setDoctorDueDate(date: String?) {
        prefs.edit().putString("doctor_due_date", date).apply()
        _doctorDueDate.value = date
    }

    fun setConceptionDate(date: String?) {
        prefs.edit().putString("conception_date", date).apply()
        _conceptionDate.value = date
    }

    fun setPregnancyMilestones(milestonesJson: String) {
        prefs.edit().putString("pregnancy_milestones", milestonesJson).apply()
        _pregnancyMilestones.value = milestonesJson
    }

    fun setUserName(name: String) {
        prefs.edit().putString("user_name", name).apply()
        _userName.value = name
    }

    fun setUserBirth(birth: String) {
        prefs.edit().putString("user_birth", birth).apply()
        _userBirth.value = birth
    }

    fun setLastReportTitle(title: String) {
        prefs.edit().putString("last_report_title", title).apply()
        _lastReportTitle.value = title
    }

    fun clearCurrentPregnancy() {
        prefs.edit()
            .putBoolean("pregnancy_mode", false)
            .remove("conception_date")
            .remove("doctor_due_date")
            .putString("pregnancy_milestones", "{}")
            .apply()
        _isPregnancyMode.value = false
        _conceptionDate.value = null
        _doctorDueDate.value = null
        _pregnancyMilestones.value = "{}"
    }

    fun getAllSettings(): Map<String, *> = prefs.all

    fun restoreSettings(settings: Map<String, *>) {
        val editor = prefs.edit()
        settings.forEach { (key, value) ->
            when (value) {
                is Boolean -> editor.putBoolean(key, value)
                is Float -> editor.putFloat(key, value)
                is Int -> editor.putInt(key, value)
                is Long -> editor.putLong(key, value)
                is String -> editor.putString(key, value)
            }
        }
        editor.apply()
        
        // Refresh flows
        _isDarkMode.value = prefs.getBoolean("dark_mode", false)
        _language.value = prefs.getString("language", "cs") ?: "cs"
        _showAdvancedSex.value = prefs.getBoolean("show_advanced_sex", false)
        _minTemp.value = prefs.getFloat("min_temp", 35.9f)
        _maxTemp.value = prefs.getFloat("max_temp", 37.5f)
        _temperatureUnit.value = prefs.getString("temp_unit", "C") ?: "C"
        _isPregnancyMode.value = prefs.getBoolean("pregnancy_mode", false)
        _conceptionDate.value = prefs.getString("conception_date", null)
        _doctorDueDate.value = prefs.getString("doctor_due_date", null)
        _pregnancyMilestones.value = prefs.getString("pregnancy_milestones", "{}") ?: "{}"
        _userName.value = prefs.getString("user_name", "") ?: ""
        _userBirth.value = prefs.getString("user_birth", "") ?: ""
        _lastReportTitle.value = prefs.getString("last_report_title", "") ?: ""
    }
}
