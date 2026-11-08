package com.ragul.ccms.feature.records.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * All record tables are scoped by [ownerId] so two Firebase accounts that use the same
 * device never see each other's data.
 */
@Entity(tableName = "memberships", indices = [Index("ownerId")])
data class MembershipEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: String,
    val name: String,
    val phone: String,
    val status: String,
    val notes: String,
    val joinedOn: Long,
    val updatedAt: Long,
)

@Entity(tableName = "loan_deposits", indices = [Index("ownerId")])
data class LoanDepositEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: String,
    val kind: String,
    val personName: String,
    val amount: Double,
    val notes: String,
    val date: Long,
    val updatedAt: Long,
) {
    val isLoan: Boolean get() = kind == KIND_LOAN

    companion object {
        const val KIND_LOAN = "LOAN"
        const val KIND_DEPOSIT = "DEPOSIT"
    }
}

@Entity(tableName = "monthly_dues", indices = [Index("ownerId")])
data class MonthlyDueEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: String,
    val title: String,
    val period: String,
    val amount: Double,
    val isPaid: Boolean,
    val notes: String,
    val dueDate: Long,
    val updatedAt: Long,
)

@Entity(tableName = "history", indices = [Index("ownerId")])
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: String,
    val section: String,
    val action: String,
    val summary: String,
    val timestamp: Long,
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
