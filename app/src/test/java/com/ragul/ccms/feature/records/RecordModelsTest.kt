package com.ragul.ccms.feature.records

import com.ragul.ccms.feature.records.data.LoanDepositEntity
import com.ragul.ccms.feature.records.data.RecordCounts
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RecordModelsTest {

    @Test
    fun `record counts start at zero`() {
        val counts = RecordCounts()

        assertEquals(0, counts.memberships)
        assertEquals(0, counts.loanDeposits)
        assertEquals(0, counts.monthlyDues)
        assertEquals(0, counts.total)
    }

    @Test
    fun `record counts total is the sum of all sections`() {
        val counts = RecordCounts(memberships = 4, loanDeposits = 2, monthlyDues = 7)

        assertEquals(13, counts.total)
    }

    @Test
    fun `loan kind reports a loan`() {
        val record = loan(kind = LoanDepositEntity.KIND_LOAN)

        assertTrue(record.isLoan)
    }

    @Test
    fun `deposit kind does not report a loan`() {
        val record = loan(kind = LoanDepositEntity.KIND_DEPOSIT)

        assertFalse(record.isLoan)
    }

    @Test
    fun `loan record derives interest and total from amount time and rate`() {
        val record = loan(kind = LoanDepositEntity.KIND_LOAN)
            .copy(amount = 1_000.0, time = 2.0, rate = 10.0)

        assertEquals(200.0, record.interest, 0.001)
        assertEquals(1_200.0, record.total, 0.001)
    }

    private fun loan(kind: String) = LoanDepositEntity(
        ownerId = "test-owner",
        kind = kind,
        personName = "Test",
        amount = 100.0,
        notes = "",
        date = 0L,
        updatedAt = 0L,
    )
}
