package com.ragul.ccms.feature.records.domain

/**
 * Simple-interest math shared by the loan form, the record cards and the unit tests.
 *
 * [timeInYears] is the loan duration in years and [ratePercent] is the annual interest rate.
 * Interest is never persisted; it is always derived from the stored amount, time and rate so
 * the three values remain the single source of truth.
 */
object LoanCalculator {

    /** Simple interest = principal × time (years) × rate (%) / 100. */
    fun interest(amount: Double, timeInYears: Double, ratePercent: Double): Double {
        if (amount <= 0.0 || timeInYears <= 0.0 || ratePercent <= 0.0) return 0.0
        return amount * timeInYears * ratePercent / 100.0
    }

    /** Payable/returnable total = principal + [interest]. */
    fun total(amount: Double, timeInYears: Double, ratePercent: Double): Double =
        amount.coerceAtLeast(0.0) + interest(amount, timeInYears, ratePercent)
}
