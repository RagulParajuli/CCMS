package com.ragul.ccms.feature.records.domain

import java.util.Calendar
import java.util.Locale

/**
 * A date in the Bikram Sambat (BS) calendar used by the membership form.
 *
 * BS month lengths follow no arithmetic leap-year rule, so the supported years use the
 * published month-length table (2000-2090 BS, i.e. 1943-2034 AD). Years such as 2081 BS have
 * 366 days, and individual months range from 29 to 32 days; both are handled by the table.
 */
data class NepaliDate(
    val year: Int,
    val month: Int,
    val day: Int,
) : Comparable<NepaliDate> {

    /** Stable, sortable `yyyy-MM-dd` value stored in the membership record. */
    fun toIso(): String = String.format(Locale.US, "%04d-%02d-%02d", year, month, day)

    /** Human-readable form, for example `23 Kartik 2083 BS`. */
    fun format(): String = "$day ${monthName(month)} $year BS"

    override fun compareTo(other: NepaliDate): Int {
        if (year != other.year) return year.compareTo(other.year)
        if (month != other.month) return month.compareTo(other.month)
        return day.compareTo(other.day)
    }

    companion object {
        const val MIN_YEAR = 2000
        const val MAX_YEAR = 2090

        val MONTH_NAMES = listOf(
            "Baisakh",
            "Jestha",
            "Ashadh",
            "Shrawan",
            "Bhadra",
            "Ashwin",
            "Kartik",
            "Mangsir",
            "Poush",
            "Magh",
            "Falgun",
            "Chaitra",
        )

        /** Month lengths for [MIN_YEAR]..[MAX_YEAR]; index 0 is Baisakh. */
        private val MONTH_LENGTHS: Array<IntArray> = arrayOf(
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2000
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2001
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2002
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2003
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2004
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2005
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2006
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2007
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31), // 2008
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2009
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2010
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2011
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30), // 2012
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2013
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2014
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2015
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30), // 2016
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2017
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2018
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2019
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30), // 2020
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2021
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30), // 2022
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2023
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30), // 2024
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2025
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2026
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2027
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2028
            intArrayOf(31, 31, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30), // 2029
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2030
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2031
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2032
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2033
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2034
            intArrayOf(30, 32, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31), // 2035
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2036
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2037
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2038
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30), // 2039
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2040
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2041
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2042
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30), // 2043
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2044
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2045
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2046
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30), // 2047
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2048
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30), // 2049
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2050
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30), // 2051
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2052
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30), // 2053
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2054
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2055
            intArrayOf(31, 31, 32, 31, 32, 30, 30, 29, 30, 29, 30, 30), // 2056
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2057
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2058
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2059
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2060
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2061
            intArrayOf(30, 32, 31, 32, 31, 31, 29, 30, 29, 30, 29, 31), // 2062
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2063
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2064
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2065
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 29, 31), // 2066
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2067
            intArrayOf(31, 31, 32, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2068
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2069
            intArrayOf(31, 31, 31, 32, 31, 31, 29, 30, 30, 29, 30, 30), // 2070
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2071
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 29, 30, 29, 30, 30), // 2072
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 31), // 2073
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30), // 2074
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2075
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30), // 2076
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2077
            intArrayOf(31, 31, 31, 32, 31, 31, 30, 29, 30, 29, 30, 30), // 2078
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2079
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 29, 30, 30), // 2080
            intArrayOf(31, 32, 31, 32, 31, 30, 30, 30, 29, 30, 29, 31), // 2081
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2082
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 29, 30, 29, 30, 30), // 2083
            intArrayOf(31, 31, 32, 31, 31, 30, 30, 30, 29, 30, 30, 30), // 2084
            intArrayOf(31, 32, 31, 32, 30, 31, 30, 30, 29, 30, 30, 30), // 2085
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30), // 2086
            intArrayOf(31, 31, 32, 31, 31, 31, 30, 30, 29, 30, 30, 30), // 2087
            intArrayOf(30, 31, 32, 32, 30, 31, 30, 30, 29, 30, 30, 30), // 2088
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30), // 2089
            intArrayOf(30, 32, 31, 32, 31, 30, 30, 30, 29, 30, 30, 30), // 2090
        )

        private val AD_EPOCH_DAYS: Long = daysFromCivil(1943, 4, 14) // 2000-01-01 BS

        fun monthName(month: Int): String = MONTH_NAMES.getOrElse(month - 1) { "" }

        /** Days in [month] (1 = Baisakh) for [year], or 0 when the date is outside the table. */
        fun daysInMonth(year: Int, month: Int): Int {
            if (year !in MIN_YEAR..MAX_YEAR || month !in 1..12) return 0
            return MONTH_LENGTHS[year - MIN_YEAR][month - 1]
        }

        /** Total days in a BS year; 366 for BS leap years such as 2081. */
        fun daysInYear(year: Int): Int {
            if (year !in MIN_YEAR..MAX_YEAR) return 0
            return MONTH_LENGTHS[year - MIN_YEAR].sum()
        }

        fun isValid(year: Int, month: Int, day: Int): Boolean =
            day in 1..daysInMonth(year, month)

        /** Parses the `yyyy-MM-dd` value stored in a membership record. */
        fun fromIso(value: String): NepaliDate? {
            val parts = value.trim().split('-')
            if (parts.size != 3) return null
            val year = parts[0].toIntOrNull() ?: return null
            val month = parts[1].toIntOrNull() ?: return null
            val day = parts[2].toIntOrNull() ?: return null
            return if (isValid(year, month, day)) NepaliDate(year, month, day) else null
        }

        /** Today's BS date, or null when the device clock is outside the published table. */
        fun today(now: Calendar = Calendar.getInstance()): NepaliDate? = fromAd(
            year = now.get(Calendar.YEAR),
            month = now.get(Calendar.MONTH) + 1,
            day = now.get(Calendar.DAY_OF_MONTH),
        )

        /** Converts a Gregorian (AD) date to BS, or null when it is outside the table. */
        fun fromAd(year: Int, month: Int, day: Int): NepaliDate? {
            if (month !in 1..12 || day !in 1..31) return null

            var remaining = daysFromCivil(year, month, day) - AD_EPOCH_DAYS
            if (remaining < 0L) return null

            var bsYear = MIN_YEAR
            while (bsYear <= MAX_YEAR && remaining >= daysInYear(bsYear)) {
                remaining -= daysInYear(bsYear)
                bsYear++
            }
            if (bsYear > MAX_YEAR) return null

            var bsMonth = 1
            while (bsMonth < 12 && remaining >= daysInMonth(bsYear, bsMonth)) {
                remaining -= daysInMonth(bsYear, bsMonth)
                bsMonth++
            }
            return NepaliDate(bsYear, bsMonth, remaining.toInt() + 1)
        }

        /** Days since 1970-01-01 for a proleptic Gregorian date (time-zone independent). */
        private fun daysFromCivil(year: Int, month: Int, day: Int): Long {
            val shiftedYear = year.toLong() - (if (month <= 2) 1 else 0)
            val era = (if (shiftedYear >= 0) shiftedYear else shiftedYear - 399) / 400
            val yearOfEra = shiftedYear - era * 400
            val dayOfYear = (153 * (month + (if (month > 2) -3 else 9)) + 2) / 5 + day - 1
            val dayOfEra = yearOfEra * 365 + yearOfEra / 4 - yearOfEra / 100 + dayOfYear
            return era * 146097 + dayOfEra - 719468
        }
    }
}
