package com.ragul.ccms.feature.records.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class LoanCalculatorTest {

    @Test
    fun `interest is principal times time times annual rate`() {
        assertEquals(2_500.0, LoanCalculator.interest(10_000.0, 2.5, 10.0), 0.0001)
    }

    @Test
    fun `total is principal plus interest`() {
        assertEquals(12_500.0, LoanCalculator.total(10_000.0, 2.5, 10.0), 0.0001)
    }

    @Test
    fun `zero rate leaves the total equal to the principal`() {
        assertEquals(0.0, LoanCalculator.interest(10_000.0, 2.0, 0.0), 0.0001)
        assertEquals(10_000.0, LoanCalculator.total(10_000.0, 2.0, 0.0), 0.0001)
    }

    @Test
    fun `invalid values do not produce negative or NaN money`() {
        assertEquals(0.0, LoanCalculator.interest(-100.0, 2.0, 10.0), 0.0001)
        assertEquals(0.0, LoanCalculator.interest(100.0, -2.0, 10.0), 0.0001)
        assertEquals(0.0, LoanCalculator.interest(100.0, 2.0, -10.0), 0.0001)
        assertEquals(0.0, LoanCalculator.total(-100.0, 2.0, 10.0), 0.0001)
    }
}
