package com.example.cykluscalk.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cykluscalk.R
import com.example.cykluscalk.data.*
import com.example.cykluscalk.logic.CycleCalculator
import com.example.cykluscalk.logic.DayStatus
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.OutputStream
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.*
import javax.inject.Inject
import kotlin.math.ceil

data class CycleInsights(
    val avgCycleLength: Int = 28,
    val avgPeriodDuration: Int = 5,
    val cycleRegularity: Int = 0,
    val nextPeriodDate: String? = null,
    val dayStatus: DayStatus = DayStatus.NONE,
    val isPregnancyMode: Boolean = false,
    val pregnancyWeek: Int = 0,
    val pregnancyDay: Int = 0,
    val dueDate: String? = null,
    val doctorDueDate: String? = null,
    val conceptionDate: String? = null,
    val milestones: Map<String, String> = emptyMap(),
    val periodDelayDays: Int = 0
)

data class StatsData(
    val tagUsage: Map<String, Int> = emptyMap(),
    val totalSexCount: Int = 0,
    val avgSexFreq: Double = 0.0,
    val sexTimeStats: Map<String, Int> = emptyMap(),
    val totalNotes: Int = 0,
    val dailyTemps: List<Pair<Int, Double>> = emptyList(),
    val dailySex: List<Pair<Int, Int>> = emptyList(),
    val monthlyTemps: List<Pair<Int, Double>> = emptyList(),
    val monthlySex: List<Pair<Int, Int>> = emptyList(),
    val yearTagUsage: Map<String, Int> = emptyMap(),
    val yearTotalSexCount: Int = 0,
    val avgCycleLength: Int = 28,
    val avgPeriodDuration: Int = 5,
    val yearlyComparison: List<Pair<Int, Int>> = emptyList(),
    val dailyWeight: List<Pair<Int, Double>> = emptyList(),
    val monthlyWeight: List<Pair<Int, Double>> = emptyList(),
    val pregnancyTagUsage: Map<String, Int> = emptyMap(),
    val dailyTags: Map<Int, List<String>> = emptyMap() // Day of month -> List of tags
)

data class BackupData(
    val records: List<DayRecord>,
    val tags: List<TagDefinition>,
    val settings: Map<String, Any?>
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class DayRecordViewModel @Inject constructor(
    private val repository: DayRecordRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val dbFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val uiFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    
    private val _selectedDate = MutableStateFlow(LocalDate.now().format(dbFormatter))
    val selectedDate: StateFlow<String> = _selectedDate

    private val _statsMonth = MutableStateFlow(YearMonth.now())
    val statsMonth: StateFlow<YearMonth> = _statsMonth

    val allRecords = repository.getAllRecords().stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val allTags = repository.getAllTags().stateIn(
        viewModelScope, SharingStarted.Eagerly, emptyList()
    )

    val pregnancyHistory = repository.getAllPregnancies().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    val currentRecord: StateFlow<DayRecord?> = _selectedDate
        .flatMapLatest { date ->
            repository.getRecordByDate(date)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val dayOfCycle: StateFlow<Int?> = combine(_selectedDate, allRecords) { dateStr, records ->
        CycleCalculator.getDayOfCycle(LocalDate.parse(dateStr), records)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val insights: StateFlow<CycleInsights> = combine(
        _selectedDate, 
        allRecords, 
        settingsRepository.isPregnancyMode,
        settingsRepository.conceptionDate,
        settingsRepository.doctorDueDate,
        settingsRepository.pregnancyMilestones
    ) { args ->
        val dateStr = args[0] as String
        val records = args[1] as List<DayRecord>
        val isPreg = args[2] as Boolean
        val concepDateStr = args[3] as String?
        val docDueDateStr = args[4] as String?
        val milestonesJson = args[5] as String
        
        val selectedLocalDate = LocalDate.parse(dateStr)
        val nextDate = CycleCalculator.predictNextPeriod(records)
        
        var pregWeek = 0
        var pregDay = 0
        var due: String? = null
        
        if (isPreg && concepDateStr != null) {
            val conceptionDate = LocalDate.parse(concepDateStr)
            val lmp = conceptionDate.minusDays(14)
            val (weeks, days) = CycleCalculator.getPregnancyWeekAndDay(selectedLocalDate, lmp)
            pregWeek = weeks
            pregDay = days
            due = CycleCalculator.calculateDueDateFromConception(conceptionDate).format(uiFormatter)
        }

        val milestones = try {
            Gson().fromJson(milestonesJson, Map::class.java) as? Map<String, String> ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }

        CycleInsights(
            avgCycleLength = CycleCalculator.calculateAverageCycleLength(records),
            avgPeriodDuration = CycleCalculator.calculateAveragePeriodDuration(records),
            cycleRegularity = CycleCalculator.calculateCycleRegularity(records),
            nextPeriodDate = nextDate?.format(uiFormatter),
            dayStatus = CycleCalculator.getDayStatus(selectedLocalDate, records, isPreg),
            isPregnancyMode = isPreg,
            pregnancyWeek = pregWeek,
            pregnancyDay = pregDay,
            dueDate = due,
            doctorDueDate = docDueDateStr?.let { LocalDate.parse(it).format(uiFormatter) },
            conceptionDate = concepDateStr?.let { LocalDate.parse(it).format(uiFormatter) },
            milestones = milestones,
            periodDelayDays = CycleCalculator.getPeriodDelay(LocalDate.now(), records)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CycleInsights())

    val statistics: StateFlow<StatsData> = combine(
        allRecords, 
        _statsMonth, 
        allTags, 
        settingsRepository.isPregnancyMode
    ) { records, month, tags, isPreg ->
        val monthRecords = records.filter { 
            val date = LocalDate.parse(it.date)
            date.year == month.year && date.month == month.month
        }

        val yearRecords = records.filter {
            val date = LocalDate.parse(it.date)
            date.year == month.year
        }

        val pregnancyTagNames = tags.filter { it.category == "Pregnancy" }.map { it.name }.toSet()

        val tagUsage = mutableMapOf<String, Int>()
        val pregnancyTagUsage = mutableMapOf<String, Int>()
        var totalSexFreq = 0
        val sexTimes = mutableMapOf<String, Int>()
        var notesCount = 0
        val dailyTemps = mutableListOf<Pair<Int, Double>>()
        val dailySex = mutableListOf<Pair<Int, Int>>()
        val dailyWeight = mutableListOf<Pair<Int, Double>>()
        val dailyTags = mutableMapOf<Int, List<String>>()
        
        monthRecords.forEach { record ->
            val dayOfMonth = LocalDate.parse(record.date).dayOfMonth
            dailyTags[dayOfMonth] = record.tags
            
            record.tags.forEach { tag ->
                if (tag in pregnancyTagNames) {
                    pregnancyTagUsage[tag] = (pregnancyTagUsage[tag] ?: 0) + 1
                } else {
                    tagUsage[tag] = (tagUsage[tag] ?: 0) + 1
                }
            }
            totalSexFreq += record.sexFrequency
            dailySex.add(dayOfMonth to record.sexFrequency)

            if (record.sexSunrise > 0) sexTimes["Morning"] = (sexTimes["Morning"] ?: 0) + record.sexSunrise
            if (record.sexDay > 0) sexTimes["Day"] = (sexTimes["Day"] ?: 0) + record.sexDay
            if (record.sexSunset > 0) sexTimes["Evening"] = (sexTimes["Evening"] ?: 0) + record.sexSunset
            if (record.sexMoon > 0) sexTimes["Night"] = (sexTimes["Night"] ?: 0) + record.sexMoon
            
            if (record.note.isNotBlank()) notesCount++
            
            record.basalTemperature?.let {
                if (it > 0.0) dailyTemps.add(dayOfMonth to it)
            }
            record.bodyWeight?.let {
                if (it > 0.0) dailyWeight.add(dayOfMonth to it)
            }
        }

        val yearTagUsage = mutableMapOf<String, Int>()
        var yearTotalSexCount = 0
        yearRecords.forEach { record ->
            yearTotalSexCount += record.sexFrequency
            record.tags.forEach { tag ->
                if (isPreg || tag !in pregnancyTagNames) {
                    yearTagUsage[tag] = (yearTagUsage[tag] ?: 0) + 1
                }
            }
        }

        val monthlyTemps = (1..12).map { m ->
            val monthTemps = yearRecords.filter { LocalDate.parse(it.date).monthValue == m }
                .mapNotNull { it.basalTemperature }
                .filter { it > 0.0 }
            m to if (monthTemps.isNotEmpty()) monthTemps.average() else 0.0
        }

        val monthlyWeight = (1..12).map { m ->
            val weights = yearRecords.filter { LocalDate.parse(it.date).monthValue == m }
                .mapNotNull { it.bodyWeight }
                .filter { it > 0.0 }
            m to if (weights.isNotEmpty()) weights.average() else 0.0
        }

        val monthlySex = (1..12).map { m ->
            val monthTotalSex = yearRecords.filter { LocalDate.parse(it.date).monthValue == m }
                .sumOf { it.sexFrequency }
            m to monthTotalSex
        }

        val yearlyComparison = records.groupBy { LocalDate.parse(it.date).year }
            .map { (year, recs) -> year to recs.sumOf { it.sexFrequency } }
            .sortedBy { it.first }
        
        StatsData(
            tagUsage = tagUsage.filterKeys { it != CycleCalculator.TAG_PERIOD }.toList().sortedByDescending { it.second }.toMap(),
            totalSexCount = totalSexFreq,
            avgSexFreq = if (monthRecords.isNotEmpty()) ceil(totalSexFreq.toDouble() / monthRecords.size) else 0.0,
            sexTimeStats = sexTimes,
            totalNotes = notesCount,
            dailyTemps = dailyTemps.sortedBy { it.first },
            dailySex = dailySex.sortedBy { it.first },
            monthlyTemps = monthlyTemps,
            monthlySex = monthlySex,
            yearTagUsage = yearTagUsage.filterKeys { it != CycleCalculator.TAG_PERIOD }.toList().sortedByDescending { it.second }.toMap(),
            yearTotalSexCount = yearTotalSexCount,
            avgCycleLength = CycleCalculator.calculateAverageCycleLength(records),
            avgPeriodDuration = CycleCalculator.calculateAveragePeriodDuration(records),
            yearlyComparison = yearlyComparison,
            dailyWeight = dailyWeight.sortedBy { it.first },
            monthlyWeight = monthlyWeight,
            pregnancyTagUsage = pregnancyTagUsage.toList().sortedByDescending { it.second }.toMap(),
            dailyTags = dailyTags
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StatsData())

    // Settings
    val isDarkMode = settingsRepository.isDarkMode
    val language = settingsRepository.language
    val showAdvancedSex = settingsRepository.showAdvancedSex
    val isPregnancyMode = settingsRepository.isPregnancyMode
    val doctorDueDate = settingsRepository.doctorDueDate
    val temperatureUnit = settingsRepository.temperatureUnit
    val appTheme = settingsRepository.appTheme
    val minTemp = settingsRepository.minTemp
    val maxTemp = settingsRepository.maxTemp
    val userName = settingsRepository.userName
    val userBirth = settingsRepository.userBirth
    val lastReportTitle = settingsRepository.lastReportTitle

    fun toggleDarkMode(enabled: Boolean) = settingsRepository.setDarkMode(enabled)
    fun setLanguage(lang: String) = settingsRepository.setLanguage(lang)
    fun setShowAdvancedSex(enabled: Boolean) = settingsRepository.setShowAdvancedSex(enabled)
    fun setTemperatureUnit(unit: String) = settingsRepository.setTemperatureUnit(unit)
    fun setAppTheme(theme: String) = settingsRepository.setAppTheme(theme)
    fun setTempRange(min: Float, max: Float) = settingsRepository.setTempRange(min, max)
    fun setUserName(name: String) = settingsRepository.setUserName(name)
    fun setUserBirth(birth: String) = settingsRepository.setUserBirth(birth)
    fun setLastReportTitle(title: String) = settingsRepository.setLastReportTitle(title)
    
    fun setPregnancyMode(enabled: Boolean, date: LocalDate? = null) {
        if (!enabled && isPregnancyMode.value) {
            viewModelScope.launch {
                val current = insights.value
                val record = PregnancyRecord(
                    conceptionDate = current.conceptionDate?.let { 
                        try { LocalDate.parse(it, uiFormatter).toString() } catch(e: Exception) { null }
                    },
                    doctorDueDate = current.doctorDueDate?.let { 
                        try { LocalDate.parse(it, uiFormatter).toString() } catch(e: Exception) { null }
                    },
                    endDate = LocalDate.now().toString(),
                    milestonesJson = settingsRepository.pregnancyMilestones.value
                )
                repository.addPregnancy(record)
                settingsRepository.clearCurrentPregnancy()
            }
        } else {
            settingsRepository.setPregnancyMode(enabled, date?.toString())
        }
    }

    fun deletePregnancyFromHistory(record: PregnancyRecord) {
        viewModelScope.launch {
            repository.deletePregnancy(record)
        }
    }
    
    fun setDoctorDueDate(date: LocalDate?) {
        settingsRepository.setDoctorDueDate(date?.toString())
    }
    
    fun setConceptionDate(date: LocalDate?) {
        settingsRepository.setConceptionDate(date?.toString())
    }

    fun updateMilestone(key: String, date: LocalDate?) {
        val currentMilestones = insights.value.milestones.toMutableMap()
        if (date == null) {
            currentMilestones.remove(key)
        } else {
            currentMilestones[key] = date.toString()
        }
        settingsRepository.setPregnancyMilestones(Gson().toJson(currentMilestones))
    }

    fun setStatsMonth(month: YearMonth) {
        _statsMonth.value = month
    }

    fun exportBackupJson(): String {
        val data = BackupData(
            records = allRecords.value,
            tags = allTags.value,
            settings = settingsRepository.getAllSettings()
        )
        return Gson().toJson(data)
    }

    fun importBackupJson(json: String): Boolean {
        return try {
            val data = Gson().fromJson(json, BackupData::class.java)
            viewModelScope.launch {
                repository.deleteAllRecords()
                data.records.forEach { repository.saveRecord(it) }
                repository.deleteAllTags()
                data.tags.forEach { repository.addTag(it) }
                settingsRepository.restoreSettings(data.settings)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    fun exportPdfReport(
        context: Context,
        outputStream: OutputStream,
        title: String,
        userName: String,
        userBirth: String,
        generatedAtLabel: String,
        tempUnit: String,
        includeMenstruation: Boolean,
        includeSymptoms: Boolean,
        includeTemperature: Boolean,
        includePregnancy: Boolean,
        includeSex: Boolean,
        includeNotes: Boolean,
        filterMonth: Int?,
        filterYear: Int?,
        startMonth: Int? = null,
        startYear: Int? = null,
        endMonth: Int? = null,
        endYear: Int? = null,
        footerText: String
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply { strokeWidth = 1f }
        val titlePaint = Paint().apply { textSize = 18f; isFakeBoldText = true; color = Color.BLACK; textAlign = Paint.Align.CENTER }
        val headerLabelPaint = Paint().apply { textSize = 10f; isFakeBoldText = true; color = Color.DKGRAY }
        val headerValuePaint = Paint().apply { textSize = 10f; color = Color.BLACK }
        val tableHeaderPaint = Paint().apply { textSize = 9f; isFakeBoldText = true; color = Color.WHITE }
        val tableRowPaint = Paint().apply { textSize = 8f; color = Color.BLACK }
        val datePaint = Paint().apply { textSize = 9f; isFakeBoldText = true; color = Color.BLACK }
        val footerPaint = Paint().apply { textSize = 8f; alpha = 120 }

        // --- Draw Logo & Title (Centered on separate lines) ---
        var currentHeaderY = 50f
        try {
            val logoDrawable = context.packageManager.getApplicationIcon(context.packageName)
            val logoSize = 48
            val centerX = pageWidth / 2
            logoDrawable.setBounds(centerX - (logoSize / 2), currentHeaderY.toInt(), centerX + (logoSize / 2), (currentHeaderY + logoSize).toInt())
            logoDrawable.draw(canvas)
            currentHeaderY += (logoSize + 20f)
        } catch (e: Exception) {
            currentHeaderY += 10f
        }
        
        canvas.drawText(title, (pageWidth / 2).toFloat(), currentHeaderY + 15f, titlePaint)
        currentHeaderY += 45f
        
        // Patient Info Box (Neutral Grey)
        canvas.drawRect(40f, currentHeaderY - 15f, 555f, currentHeaderY + 35f, Paint().apply { color = Color.parseColor("#EEEEEE") })
        
        canvas.drawText("JMÉNO:", 50f, currentHeaderY, headerLabelPaint)
        canvas.drawText(userName.ifBlank { "---" }, 100f, currentHeaderY, headerValuePaint)
        canvas.drawText("DATUM NAROZENÍ:", 320f, currentHeaderY, headerLabelPaint)
        canvas.drawText(userBirth.ifBlank { "---" }, 450f, currentHeaderY, headerValuePaint)
        
        currentHeaderY += 20f
        val periodText = if (startMonth != null && startYear != null && endMonth != null && endYear != null) {
            "$startMonth/$startYear – $endMonth/$endYear"
        } else if (filterMonth != null && filterYear != null) {
            "$filterMonth/$filterYear"
        } else if (filterYear != null) {
            "$filterYear"
        } else {
            "Celá historie"
        }
        canvas.drawText("OBDOBÍ:", 50f, currentHeaderY, headerLabelPaint)
        canvas.drawText(periodText, 100f, currentHeaderY, headerValuePaint)
        
        currentHeaderY += 45f
        var y = currentHeaderY

        val allRecs = allRecords.value
        val filteredRecords = allRecs.filter { rec ->
            val date = LocalDate.parse(rec.date)
            if (startMonth != null && startYear != null && endMonth != null && endYear != null) {
                val currentYM = YearMonth.of(date.year, date.monthValue)
                val startYM = YearMonth.of(startYear, startMonth)
                val endYM = YearMonth.of(endYear, endMonth)
                !currentYM.isBefore(startYM) && !currentYM.isAfter(endYM)
            } else {
                val monthMatch = filterMonth == null || date.monthValue == filterMonth
                val yearMatch = filterYear == null || date.year == filterYear
                monthMatch && yearMatch
            }
        }.sortedBy { it.date }

        // --- DRAW CHARTS ---
        if (includeTemperature && filteredRecords.any { (it.basalTemperature ?: 0.0) > 0.0 }) {
            val tempData = filteredRecords.map { LocalDate.parse(it.date).dayOfMonth to (it.basalTemperature ?: 0.0) }
            y = drawPdfChart(canvas, "Graf bazální teploty", tempData, 35.0, 38.0, y)
            y += 40f
        }

        if (includePregnancy && filteredRecords.any { (it.bodyWeight ?: 0.0) > 0.0 }) {
            val weightData = filteredRecords.map { LocalDate.parse(it.date).dayOfMonth to (it.bodyWeight ?: 0.0) }
            val weights = weightData.map { it.second }
            val minW = (weights.filter { it > 0 }.minOrNull() ?: 50.0) - 2.0
            val maxW = (weights.maxOrNull() ?: 100.0) + 2.0
            y = drawPdfChart(canvas, "Graf váhy (kg)", weightData, minW, maxW, y)
            y += 40f
        }

        // --- TABLE HEADER ---
        if (y > 700f) {
            pdfDocument.finishPage(page); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; y = 50f
        }
        
        val colWidths = floatArrayOf(90f, 50f, 50f, 130f, 235f) // Datum, Temp, Váha, Štítky, Poznámka
        val startX = 40f
        
        canvas.drawRect(startX, y - 12f, 555f, y + 8f, Paint().apply { color = Color.BLACK })
        var tableX = startX + 5f
        val headers = arrayOf("Datum", "Tepl.", "Váha", "Štítky", "Poznámka")
        headers.forEachIndexed { i, h ->
            canvas.drawText(h, tableX, y, tableHeaderPaint)
            tableX += colWidths[i]
        }
        y += 20f

        // --- DATA ROWS ---
        val tagsDef = allTags.value
        var lastMonthYear: String? = null

        filteredRecords.forEachIndexed { index, record ->
            val dateObj = LocalDate.parse(record.date)
            val currentMonthYear = dateObj.month.getDisplayName(TextStyle.FULL, Locale("cs")).replaceFirstChar { it.uppercase() } + " " + dateObj.year

            // Draw Month Separator
            if (currentMonthYear != lastMonthYear) {
                if (y > 750f) {
                    pdfDocument.finishPage(page); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; y = 50f
                }
                canvas.drawRect(startX, y - 12f, 555f, y + 8f, Paint().apply { color = Color.parseColor("#EEEEEE") })
                canvas.drawText(currentMonthYear, pageWidth / 2f, y, Paint().apply { 
                    textSize = 10f; isFakeBoldText = true; color = Color.BLACK; textAlign = Paint.Align.CENTER 
                })
                y += 20f
                lastMonthYear = currentMonthYear
            }

            val sympTags = tagsDef.filter { it.category == "Symptom" }.map { it.name }
            val pregTags = tagsDef.filter { it.category == "Pregnancy" }.map { it.name }

            val activeTagsList = mutableListOf<String>()
            if (includeMenstruation && record.tags.contains(CycleCalculator.TAG_PERIOD)) {
                activeTagsList.add(context.getString(R.string.menstruation))
            }
            if (includeSymptoms) {
                activeTagsList.addAll(record.tags.filter { it in sympTags && it != CycleCalculator.TAG_PERIOD })
            }
            if (includePregnancy) {
                activeTagsList.addAll(record.tags.filter { it in pregTags })
            }
            
            val tagsText = activeTagsList.distinct().joinToString(", ")
            val noteText = if (includeNotes) record.note else ""
            
            // Calculate multi-line height
            val tagsLines = calculateTextLines(tagsText, colWidths[3] - 10f, tableRowPaint)
            val noteLines = calculateTextLines(noteText, colWidths[4] - 10f, tableRowPaint)
            val rowHeight = (maxOf(tagsLines.size, noteLines.size, 1) * 12f).coerceAtLeast(18f)

            if (y + rowHeight > 800f) {
                pdfDocument.finishPage(page); page = pdfDocument.startPage(pageInfo); canvas = page.canvas; y = 50f
                canvas.drawRect(startX, y - 12f, 555f, y + 8f, Paint().apply { color = Color.BLACK })
                var tempX = startX + 5f
                headers.forEachIndexed { i, h -> canvas.drawText(h, tempX, y, tableHeaderPaint); tempX += colWidths[i] }
                y += 20f
            }

            if (index % 2 == 1) {
                canvas.drawRect(startX, y - 10f, 555f, y + rowHeight - 10f, Paint().apply { color = Color.parseColor("#F2F2F2") })
            }
            
            // Draw row cells
            var rowX = startX + 5f
            // Date
            canvas.drawText(dateObj.format(DateTimeFormatter.ofPattern("dd.MM (EEE)")), rowX, y, datePaint)
            rowX += colWidths[0]
            // Temp
            canvas.drawText(if (includeTemperature && (record.basalTemperature ?: 0.0) > 0.0) "${record.basalTemperature}" else "-", rowX, y, tableRowPaint)
            rowX += colWidths[1]
            // Weight
            canvas.drawText(if (includePregnancy && (record.bodyWeight ?: 0.0) > 0.0) "${record.bodyWeight}" else "-", rowX, y, tableRowPaint)
            rowX += colWidths[2]
            // Tags (wrapped)
            drawMultilineText(canvas, tagsLines, rowX, y, tableRowPaint)
            rowX += colWidths[3]
            // Note (wrapped)
            drawMultilineText(canvas, noteLines, rowX, y, tableRowPaint)
            
            y += rowHeight
        }

        canvas.drawText(footerText + " | " + generatedAtLabel + ": " + LocalDate.now().format(uiFormatter), 40f, pageHeight - 30f, footerPaint)
        
        pdfDocument.finishPage(page)
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()
    }

    private fun drawPdfChart(canvas: Canvas, title: String, data: List<Pair<Int, Double>>, minV: Double, maxV: Double, startY: Float): Float {
        var currentY = startY
        if (currentY + 150f > 800f) return currentY

        val paint = Paint().apply { textSize = 10f; isFakeBoldText = true }
        canvas.drawText(title, 40f, currentY, paint)
        currentY += 10f
        
        val chartHeight = 100f
        val chartWidth = 500f
        val left = 60f
        val bottom = currentY + chartHeight
        
        val axisPaint = Paint().apply { strokeWidth = 1f; color = Color.BLACK }
        canvas.drawLine(left, currentY, left, bottom, axisPaint)
        canvas.drawLine(left, bottom, left + chartWidth, bottom, axisPaint)
        
        val axisLabelPaint = Paint().apply { textSize = 7f; color = Color.DKGRAY; textAlign = Paint.Align.CENTER }
        val maxDay = data.maxOfOrNull { it.first } ?: 31
        val stepX = chartWidth / maxDay.coerceAtLeast(1)

        for (d in listOf(1, 5, 10, 15, 20, 25, 30, maxDay).distinct()) {
            if (d <= maxDay) {
                val labelX = left + ((d - 1) * stepX)
                canvas.drawText(d.toString(), labelX, bottom + 12f, axisLabelPaint)
            }
        }

        val filteredPoints = data.filter { it.second > 0 }
        if (filteredPoints.isNotEmpty()) {
            val pointPaint = Paint().apply { color = Color.BLACK; strokeWidth = 2f; style = Paint.Style.STROKE }
            val valueTextPaint = Paint().apply { textSize = 7f; color = Color.BLACK; textAlign = Paint.Align.CENTER }
            
            var lastX = -1f
            var lastY = -1f
            
            data.sortedBy { it.first }.forEach { (day, value) ->
                val x = left + ((day - 1) * stepX)
                if (value > 0) {
                    val normalizedY = ((value - minV) / (maxV - minV)).coerceIn(0.0, 1.0)
                    val y = bottom - (normalizedY.toFloat() * chartHeight)
                    
                    canvas.drawCircle(x, y, 2f, pointPaint)
                    canvas.drawText("%.1f".format(value), x, y - 5f, valueTextPaint)

                    if (lastX >= 0) canvas.drawLine(lastX, lastY, x, y, pointPaint)
                    lastX = x
                    lastY = y
                } else {
                    lastX = -1f
                }
            }
        }
        return bottom + 30f
    }

    init {
        viewModelScope.launch {
            allTags.take(1).collect { tags ->
                if (tags.isEmpty()) {
                    val defaultSymptoms = listOf("Bolest", "Únava", "Stres", "PMS", "Sport")
                    val defaultSex = listOf("Pohlavní styk", "Chráněný", "Nechráněný", "Snažení")
                    val defaultPregnancy = listOf("Pohyby plodu", "Nevolnost", "Otoky", "Tvrdnutí břicha")
                    defaultSymptoms.forEach { repository.addTag(TagDefinition(it, "Symptom")) }
                    defaultSex.forEach { repository.addTag(TagDefinition(it, "Sex")) }
                    defaultPregnancy.forEach { repository.addTag(TagDefinition(it, "Pregnancy")) }
                }
            }
        }
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date.format(dbFormatter)
    }

    fun saveNote(note: String) {
        viewModelScope.launch {
            val record = currentRecord.value?.copy(note = note) 
                ?: DayRecord(date = _selectedDate.value, note = note)
            repository.saveRecord(record)
        }
    }

    fun updateSexData(sunrise: Int, day: Int, sunset: Int, moon: Int, unassigned: Int = 0) {
        viewModelScope.launch {
            val total = sunrise + day + sunset + moon + unassigned
            val record = currentRecord.value?.copy(
                sexSunrise = sunrise,
                sexDay = day,
                sexSunset = sunset,
                sexMoon = moon,
                sexUnassigned = unassigned,
                sexFrequency = total
            ) ?: DayRecord(
                date = _selectedDate.value,
                sexSunrise = sunrise,
                sexDay = day,
                sexSunset = sunset,
                sexMoon = moon,
                sexUnassigned = unassigned,
                sexFrequency = total
            )
            repository.saveRecord(record)
        }
    }

    fun incrementSexUnassigned() {
        viewModelScope.launch {
            val record = currentRecord.value ?: DayRecord(date = _selectedDate.value)
            val newUnassigned = record.sexUnassigned + 1
            val newTotal = record.sexSunrise + record.sexDay + record.sexSunset + record.sexMoon + newUnassigned
            repository.saveRecord(record.copy(sexUnassigned = newUnassigned, sexFrequency = newTotal))
        }
    }

    fun updateTemperature(temp: Double?) {
        viewModelScope.launch {
            val record = currentRecord.value?.copy(basalTemperature = temp)
                ?: DayRecord(date = _selectedDate.value, basalTemperature = temp)
            repository.saveRecord(record)
        }
    }

    fun updateWeight(weight: Double?) {
        viewModelScope.launch {
            val record = currentRecord.value?.copy(bodyWeight = weight)
                ?: DayRecord(date = _selectedDate.value, bodyWeight = weight)
            repository.saveRecord(record)
        }
    }

    fun toggleTag(tagName: String) {
        viewModelScope.launch {
            val currentTags = currentRecord.value?.tags ?: emptyList()
            val isAdding = !currentTags.contains(tagName)
            val newTags = if (isAdding) currentTags + tagName else currentTags - tagName
            val record = currentRecord.value?.copy(tags = newTags)
                ?: DayRecord(date = _selectedDate.value, tags = newTags)
            repository.saveRecord(record)

            if (tagName == CycleCalculator.TAG_PERIOD && isAdding) {
                val newDate = LocalDate.parse(_selectedDate.value)
                fillMenstruationGaps(newDate)
                val hasPrev = allRecords.value.any { 
                    val d = LocalDate.parse(it.date)
                    it.tags.contains(CycleCalculator.TAG_PERIOD) && 
                    d.isBefore(newDate) && ChronoUnit.DAYS.between(d, newDate) <= 5 
                }
                if (!hasPrev) {
                    val avgDuration = insights.value.avgPeriodDuration
                    fillRange(newDate, newDate.plusDays(avgDuration.toLong() - 1))
                }
            }
        }
    }

    fun addNewTagDefinition(name: String, category: String) {
        viewModelScope.launch { repository.addTag(TagDefinition(name = name, category = category)) }
    }

    fun deleteTagDefinition(tag: TagDefinition) {
        viewModelScope.launch { repository.deleteTag(tag) }
    }

    private suspend fun fillMenstruationGaps(newDate: LocalDate) {
        val periodDates = allRecords.value
            .filter { it.tags.contains(CycleCalculator.TAG_PERIOD) }
            .map { LocalDate.parse(it.date) }
            .plus(newDate).distinct().sorted()

        val prev = periodDates.lastOrNull { it.isBefore(newDate) }
        val next = periodDates.firstOrNull { it.isAfter(newDate) }

        if (prev != null && ChronoUnit.DAYS.between(prev, newDate) <= 10) fillRange(prev, newDate)
        if (next != null && ChronoUnit.DAYS.between(newDate, next) <= 10) fillRange(newDate, next)
    }

    private suspend fun fillRange(start: LocalDate, end: LocalDate) {
        var current = start.plusDays(1)
        while (current.isBefore(end) || current == end) {
            val dateStr = current.format(dbFormatter)
            val existing = allRecords.value.find { it.date == dateStr }
            if (existing == null || !existing.tags.contains(CycleCalculator.TAG_PERIOD)) {
                val updatedTags = (existing?.tags ?: emptyList()) + CycleCalculator.TAG_PERIOD
                repository.saveRecord(DayRecord(date = dateStr, note = existing?.note ?: "", tags = updatedTags, sexFrequency = existing?.sexFrequency ?: 0, sexTimes = existing?.sexTimes ?: emptyList()))
            }
            current = current.plusDays(1)
        }
    }

    private fun calculateTextLines(text: String, maxWidth: Float, paint: Paint): List<String> {
        if (text.isEmpty()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) lines.add(currentLine.toString())
        return lines
    }

    private fun drawMultilineText(canvas: Canvas, lines: List<String>, x: Float, y: Float, paint: Paint) {
        var currentY = y
        lines.forEach { line ->
            canvas.drawText(line, x, currentY, paint)
            currentY += 12f
        }
    }
}
