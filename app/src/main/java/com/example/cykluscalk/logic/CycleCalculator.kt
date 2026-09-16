package com.example.cykluscalk.logic

import com.example.cykluscalk.data.DayRecord
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class DayStatus {
    NONE, PERIOD, FERTILE, OVULATION, PREGNANCY
}

object CycleCalculator {
    const val TAG_PERIOD = "Menstruace"
    private const val DEFAULT_CYCLE_LENGTH = 28
    private const val LUTEAL_PHASE_LENGTH = 14
    private const val PREGNANCY_DURATION_DAYS = 280
    private const val CONCEPTION_TO_DUE_DATE_DAYS = 266

    fun getPeriodStartDates(records: List<DayRecord>): List<LocalDate> {
        val periodDates = records
            .filter { it.tags.contains(TAG_PERIOD) }
            .map { LocalDate.parse(it.date) }
            .sorted()

        if (periodDates.isEmpty()) return emptyList()

        val startDates = mutableListOf<LocalDate>()
        startDates.add(periodDates[0])

        for (i in 1 until periodDates.size) {
            val prev = periodDates[i - 1]
            val curr = periodDates[i]
            if (ChronoUnit.DAYS.between(prev, curr) > 4) {
                startDates.add(curr)
            }
        }
        return startDates
    }

    fun calculateAverageCycleLength(records: List<DayRecord>): Int {
        val startDates = getPeriodStartDates(records)
        if (startDates.size < 2) return DEFAULT_CYCLE_LENGTH

        val intervals = mutableListOf<Long>()
        for (i in 1 until startDates.size) {
            val days = ChronoUnit.DAYS.between(startDates[i - 1], startDates[i])
            // Ignorujeme mezery delší než 50 dní (pravděpodobně těhotenství nebo výpadek)
            if (days in 15..50) {
                intervals.add(days)
            }
        }
        
        if (intervals.isEmpty()) return DEFAULT_CYCLE_LENGTH
        return (intervals.sum() / intervals.size).toInt()
    }

    fun calculateAveragePeriodDuration(records: List<DayRecord>): Int {
        val periodDates = records
            .filter { it.tags.contains(TAG_PERIOD) }
            .map { LocalDate.parse(it.date) }
            .sorted()
        if (periodDates.isEmpty()) return 5
        val startDates = getPeriodStartDates(records)
        if (startDates.isEmpty()) return 5

        var totalDuration = 0
        startDates.forEach { start ->
            var duration = 1
            while (periodDates.contains(start.plusDays(duration.toLong()))) {
                duration++
                if (duration > 15) break
            }
            totalDuration += duration
        }
        return totalDuration / startDates.size
    }

    fun calculateCycleRegularity(records: List<DayRecord>): Int {
        val startDates = getPeriodStartDates(records)
        if (startDates.size < 3) return 0
        val lengths = mutableListOf<Long>()
        for (i in 1 until startDates.size) {
            val len = ChronoUnit.DAYS.between(startDates[i - 1], startDates[i])
            if (len in 15..50) lengths.add(len)
        }
        if (lengths.isEmpty()) return 0
        return (lengths.maxOrNull()!! - lengths.minOrNull()!!).toInt()
    }

    /**
     * Vypočítá toleranci zpoždění na základě historie.
     * Vrací rozdíl mezi maximální délkou cyklu a průměrem.
     */
    fun calculateDelayTolerance(records: List<DayRecord>): Int {
        val startDates = getPeriodStartDates(records)
        if (startDates.size < 3) return 2 // Výchozí tolerance 2 dny
        
        val lengths = mutableListOf<Long>()
        for (i in 1 until startDates.size) {
            val len = ChronoUnit.DAYS.between(startDates[i - 1], startDates[i])
            if (len in 15..50) lengths.add(len)
        }
        if (lengths.isEmpty()) return 2
        
        val avg = lengths.average()
        val max = lengths.maxOrNull()?.toDouble() ?: avg
        return (max - avg).toInt().coerceAtLeast(1)
    }

    /**
     * Vypočítá počet dní zpoždění oproti predikci, se započtením tolerance.
     */
    fun getPeriodDelay(currentDate: LocalDate, records: List<DayRecord>): Int {
        if (records.any { it.date == currentDate.toString() && it.tags.contains(TAG_PERIOD) }) return 0
        
        val nextPeriod = predictNextPeriod(records) ?: return 0
        if (!currentDate.isAfter(nextPeriod)) return 0
        
        val tolerance = calculateDelayTolerance(records)
        val diff = ChronoUnit.DAYS.between(nextPeriod, currentDate).toInt()
        
        return if (diff > tolerance) diff else 0
    }

    fun predictNextPeriod(records: List<DayRecord>): LocalDate? {
        val startDates = getPeriodStartDates(records)
        if (startDates.isEmpty()) return null
        return startDates.last().plusDays(calculateAverageCycleLength(records).toLong())
    }

    /**
     * Vypočítá všechna plodná okna v historii i budoucnosti.
     */
    fun getAllFertileWindows(records: List<DayRecord>): List<LocalDate> {
        val startDates = getPeriodStartDates(records)
        if (startDates.isEmpty()) return emptyList()
        
        val avgLength = calculateAverageCycleLength(records)
        val fertileDays = mutableListOf<LocalDate>()
        
        // Pro každý známý začátek cyklu najdeme další začátek a z něj vypočítáme plodné okno
        // (Ovulace je cca 14 dní PŘED dalším začátkem)
        
        // 1. Minulá okna (mezi zadanými menstruacemi)
        for (i in 1 until startDates.size) {
            val nextStart = startDates[i]
            val ovulation = nextStart.minusDays(LUTEAL_PHASE_LENGTH.toLong())
            for (d in 0..5) fertileDays.add(ovulation.minusDays(d.toLong()))
        }
        
        // 2. Budoucí okno (z predikovaného začátku)
        val predictedStart = predictNextPeriod(records)
        predictedStart?.let { nextStart ->
            val ovulation = nextStart.minusDays(LUTEAL_PHASE_LENGTH.toLong())
            for (d in 0..5) fertileDays.add(ovulation.minusDays(d.toLong()))
        }
        
        return fertileDays.distinct().sorted()
    }

    fun getDayOfCycle(date: LocalDate, records: List<DayRecord>): Int? {
        val startDates = getPeriodStartDates(records)
        val lastStartBeforeDate = startDates.lastOrNull { !it.isAfter(date) } ?: return null
        return (ChronoUnit.DAYS.between(lastStartBeforeDate, date) + 1).toInt()
    }
    
    fun getDayStatus(date: LocalDate, records: List<DayRecord>, isPregnancyMode: Boolean = false): DayStatus {
        if (isPregnancyMode) return DayStatus.PREGNANCY
        if (records.any { it.date == date.toString() && it.tags.contains(TAG_PERIOD) }) return DayStatus.PERIOD
        
        val allFertileDays = getAllFertileWindows(records)
        if (allFertileDays.contains(date)) {
            val nextStarts = getPeriodStartDates(records).filter { it.isAfter(date) }.plus(predictNextPeriod(records)).filterNotNull()
            val closestNextStart = nextStarts.minOrNull()
            if (closestNextStart != null && date == closestNextStart.minusDays(LUTEAL_PHASE_LENGTH.toLong())) {
                return DayStatus.OVULATION
            }
            return DayStatus.FERTILE
        }
        
        return DayStatus.NONE
    }

    /**
     * Vypočítá týden a den těhotenství (např. 8t + 2d).
     */
    fun getPregnancyWeekAndDay(currentDate: LocalDate, startDate: LocalDate): Pair<Int, Int> {
        val totalDays = ChronoUnit.DAYS.between(startDate, currentDate)
        if (totalDays < 0) return 0 to 0
        val weeks = (totalDays / 7).toInt()
        val days = (totalDays % 7).toInt()
        return weeks to days
    }

    /**
     * Vypočítá předpokládaný termín porodu (LMP + 280 dní).
     */
    fun calculateDueDate(startDate: LocalDate): LocalDate {
        return startDate.plusDays(PREGNANCY_DURATION_DAYS.toLong())
    }

    /**
     * Vypočítá předpokládaný termín porodu z data početí (Conception + 266 dní).
     */
    fun calculateDueDateFromConception(conceptionDate: LocalDate): LocalDate {
        return conceptionDate.plusDays(CONCEPTION_TO_DUE_DATE_DAYS.toLong())
    }
}
