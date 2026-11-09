package com.ragul.ccms.feature.records.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class NepaliDateTest {

    @Test
    fun `month names follow the Bikram Sambat order`() {
        assertEquals("Baisakh", NepaliDate.monthName(1))
        assertEquals("Jestha", NepaliDate.monthName(2))
        assertEquals("Ashadh", NepaliDate.monthName(3))
        assertEquals("Chaitra", NepaliDate.monthName(12))
    }

    @Test
    fun `month lengths vary within the same year`() {
        assertEquals(32, NepaliDate.daysInMonth(2083, 3))
        assertEquals(30, NepaliDate.daysInMonth(2083, 7))
        assertEquals(29, NepaliDate.daysInMonth(2083, 8))
    }

    @Test
    fun `the table handles 365 and 366 day BS years`() {
        assertEquals(366, NepaliDate.daysInYear(2081))
        assertEquals(365, NepaliDate.daysInYear(2082))
        assertEquals(365, NepaliDate.daysInYear(2083))
    }

    @Test
    fun `validation rejects impossible BS dates`() {
        assertTrue(NepaliDate.isValid(2083, 3, 32))
        assertFalse(NepaliDate.isValid(2083, 3, 33))
        assertFalse(NepaliDate.isValid(2083, 7, 31))
        assertFalse(NepaliDate.isValid(2083, 8, 30))
        assertFalse(NepaliDate.isValid(2083, 0, 1))
        assertFalse(NepaliDate.isValid(2083, 13, 1))
        assertFalse(NepaliDate.isValid(2083, 1, 0))
        assertFalse(NepaliDate.isValid(1999, 1, 1))
        assertFalse(NepaliDate.isValid(2091, 1, 1))
    }

    @Test
    fun `iso values round trip through storage`() {
        val date = NepaliDate(2083, 7, 23)

        assertEquals("2083-07-23", date.toIso())
        assertEquals(date, NepaliDate.fromIso(date.toIso()))
        assertNull(NepaliDate.fromIso("2083-02-32"))
        assertNull(NepaliDate.fromIso("not-a-date"))
    }

    @Test
    fun `dates format with the Nepali month and BS year`() {
        assertEquals("23 Kartik 2083 BS", NepaliDate(2083, 7, 23).format())
    }

    @Test
    fun `AD dates convert to the expected BS date`() {
        assertEquals(NepaliDate(2000, 1, 1), NepaliDate.fromAd(1943, 4, 14))
        assertEquals(NepaliDate(2081, 1, 1), NepaliDate.fromAd(2024, 4, 13))
        assertEquals(NepaliDate(2083, 1, 1), NepaliDate.fromAd(2026, 4, 14))
        assertEquals(NepaliDate(2083, 7, 23), NepaliDate.fromAd(2026, 11, 9))
        assertNull(NepaliDate.fromAd(1943, 4, 13))
    }

    @Test
    fun `dates compare chronologically`() {
        assertTrue(NepaliDate(2083, 7, 23) > NepaliDate(2083, 7, 22))
        assertTrue(NepaliDate(2083, 8, 1) > NepaliDate(2083, 7, 30))
        assertTrue(NepaliDate(2084, 1, 1) > NepaliDate(2083, 12, 30))
    }
}
