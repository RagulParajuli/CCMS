package com.ragul.ccms.feature.records.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.ragul.ccms.feature.records.domain.LoanCalculator

/**
 * All record tables are scoped by [ownerId] so two Firebase accounts that use the same
 * device never see each other's data.
 */
@Entity(tableName = "memberships", indices = [Index("ownerId")])
data class MembershipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "0") val memberId: Long = 0,
    val ownerId: String,
    val name: String,
    @ColumnInfo(defaultValue = "''") val gender: String = "",
    @ColumnInfo(defaultValue = "''") val dob: String = "",
    @ColumnInfo(defaultValue = "''") val parentName: String = "",
    @ColumnInfo(defaultValue = "''") val grandfatherName: String = "",
    @ColumnInfo(defaultValue = "''") val citizenshipNumber: String = "",
    @ColumnInfo(defaultValue = "''") val address: String = "",
    val phone: String,
    @ColumnInfo(defaultValue = "''") val email: String = "",
    val status: String,
    val notes: String,
    val joinedOn: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "''") val syncId: String = "",
)

@Entity(tableName = "loan_deposits", indices = [Index("ownerId")])
data class LoanDepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "0") val memberId: Long = 0,
    val ownerId: String,
    val kind: String,
    val personName: String,
    /** Principal loan amount. */
    val amount: Double,
    /** Loan duration in years. */
    @ColumnInfo(defaultValue = "0") val time: Double = 0.0,
    /** Annual interest rate in percent. */
    @ColumnInfo(defaultValue = "0") val rate: Double = 0.0,
    @ColumnInfo(defaultValue = "'ACTIVE'") val loanStatus: String = LOAN_STATUS_ACTIVE,
    val notes: String,
    val date: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "''") val syncId: String = "",
) {
    val isLoan: Boolean get() = kind == KIND_LOAN

    /** Derived simple interest; never stored, so amount/time/rate stay the source of truth. */
    val interest: Double get() = LoanCalculator.interest(amount, time, rate)

    /** Derived total (principal + interest). */
    val total: Double get() = LoanCalculator.total(amount, time, rate)

    companion object {
        const val KIND_LOAN = "LOAN"
        const val KIND_DEPOSIT = "DEPOSIT"
        const val LOAN_STATUS_ACTIVE = "ACTIVE"
        const val LOAN_STATUS_PAID_OFF = "PAID_OFF"
    }
}

@Entity(tableName = "monthly_dues", indices = [Index("ownerId")])
data class MonthlyDueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(defaultValue = "0") val memberId: Long = 0,
    val ownerId: String,
    val title: String,
    /** Bikram Sambat period label, for example `Kartik 2083 BS`. */
    val period: String,
    /** Legacy due amount, kept in sync with [grandTotal] for older clients and history. */
    val amount: Double,
    @ColumnInfo(defaultValue = "0") val amountDeposit: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val grandTotal: Double = 0.0,
    @ColumnInfo(defaultValue = "0") val monthlyPaymentAmount: Double = 0.0,
    val isPaid: Boolean,
    val notes: String,
    val dueDate: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "''") val syncId: String = "",
) {
    /** Derived balance; never stored so the two amounts stay the source of truth. */
    val remaining: Double get() = (grandTotal - amountDeposit).coerceAtLeast(0.0)
}

@Entity(
    tableName = "financial_transactions",
    indices = [Index("ownerId"), Index("memberId"), Index("syncId", unique = true)],
)
data class FinancialTransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: String,
    val memberId: Long,
    /** Stable Firestore IDs link transactions to a loan, monthly due or extra deposit. */
    @ColumnInfo(defaultValue = "''") val loanSyncId: String = "",
    @ColumnInfo(defaultValue = "''") val dueSyncId: String = "",
    @ColumnInfo(defaultValue = "''") val parentSyncId: String = "",
    val kind: String,
    val amount: Double,
    val date: Long,
    val status: String,
    val notes: String = "",
    val createdAt: Long,
    val updatedAt: Long,
    @ColumnInfo(defaultValue = "''") val syncId: String = "",
) {
    companion object {
        const val KIND_LOAN_REPAYMENT = "LOAN_REPAYMENT"
        const val KIND_EXTRA_DEPOSIT = "EXTRA_DEPOSIT"
        const val KIND_EXTRA_WITHDRAWAL = "EXTRA_WITHDRAWAL"
        const val KIND_EXTRA_ALLOCATION = "EXTRA_ALLOCATION"
        const val STATUS_PENDING = "PENDING"
        const val STATUS_PAID = "PAID"
        const val STATUS_CANCELLED = "CANCELLED"
    }
}

@Entity(tableName = "history", indices = [Index("ownerId")])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: String,
    val section: String,
    val action: String,
    val summary: String,
    val timestamp: Long,
    @ColumnInfo(defaultValue = "''") val syncId: String = "",
)

/** Record totals used by the dashboard cards and the pie chart. */
data class RecordCounts(
    val memberships: Int = 0,
    val loanDeposits: Int = 0,
    val monthlyDues: Int = 0,
) {
    val total: Int get() = memberships + loanDeposits + monthlyDues
}

/** Sections that write to the activity history. */
enum class RecordSection(val label: String) {
    MEMBERSHIP("Membership"),
    LOAN_DEPOSIT("Loan or Deposit"),
    MONTHLY_DUE("Monthly Due"),
}
