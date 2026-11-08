package com.ragul.ccms.feature.records.data

import android.content.Context
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Single source of truth for Membership, Loan or Deposit, Monthly Due and the activity
 * History. Every screen observes these flows, so counts update as soon as a row changes.
 */
class RecordsRepository(private val database: CcmsDatabase) {

    private val dao = database.recordsDao()

    fun observeMemberships(ownerId: String): Flow<List<MembershipEntity>> =
        dao.observeMemberships(ownerId)

    fun observeLoanDeposits(ownerId: String): Flow<List<LoanDepositEntity>> =
        dao.observeLoanDeposits(ownerId)

    fun observeMonthlyDues(ownerId: String): Flow<List<MonthlyDueEntity>> =
        dao.observeMonthlyDues(ownerId)

    fun observeHistory(ownerId: String): Flow<List<HistoryEntity>> =
        dao.observeHistory(ownerId)

    fun observeCounts(ownerId: String): Flow<RecordCounts> = combine(
        dao.observeMembershipCount(ownerId),
        dao.observeLoanDepositCount(ownerId),
        dao.observeMonthlyDueCount(ownerId),
    ) { memberships, loanDeposits, monthlyDues ->
        RecordCounts(
            memberships = memberships,
            loanDeposits = loanDeposits,
            monthlyDues = monthlyDues,
        )
    }

    suspend fun saveMembership(ownerId: String, membership: MembershipEntity) {
        val now = System.currentTimeMillis()
        val isNew = membership.id == 0L
        val entity = membership.copy(ownerId = ownerId, updatedAt = now)
        database.withTransaction {
            dao.upsertMembership(entity)
            dao.insertHistory(
                HistoryEntity(
                    ownerId = ownerId,
                    section = RecordSection.MEMBERSHIP.label,
                    action = if (isNew) ACTION_ADDED else ACTION_UPDATED,
                    summary = entity.name,
                    timestamp = now,
                ),
            )
        }
    }

    suspend fun deleteMembership(ownerId: String, membership: MembershipEntity) {
        database.withTransaction {
            dao.deleteMembership(membership.id, ownerId)
            dao.insertHistory(
                HistoryEntity(
                    ownerId = ownerId,
                    section = RecordSection.MEMBERSHIP.label,
                    action = ACTION_DELETED,
                    summary = membership.name,
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun saveLoanDeposit(ownerId: String, loanDeposit: LoanDepositEntity) {
        val now = System.currentTimeMillis()
        val isNew = loanDeposit.id == 0L
        val entity = loanDeposit.copy(ownerId = ownerId, updatedAt = now)
        val kindLabel = if (entity.isLoan) "Loan" else "Deposit"
        database.withTransaction {
            dao.upsertLoanDeposit(entity)
            dao.insertHistory(
                HistoryEntity(
                    ownerId = ownerId,
                    section = RecordSection.LOAN_DEPOSIT.label,
                    action = if (isNew) ACTION_ADDED else ACTION_UPDATED,
                    summary = "$kindLabel · ${entity.personName}",
                    timestamp = now,
                ),
            )
        }
    }

    suspend fun deleteLoanDeposit(ownerId: String, loanDeposit: LoanDepositEntity) {
        database.withTransaction {
            dao.deleteLoanDeposit(loanDeposit.id, ownerId)
            dao.insertHistory(
                HistoryEntity(
                    ownerId = ownerId,
                    section = RecordSection.LOAN_DEPOSIT.label,
                    action = ACTION_DELETED,
                    summary = "${if (loanDeposit.isLoan) "Loan" else "Deposit"} · ${loanDeposit.personName}",
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun saveMonthlyDue(ownerId: String, due: MonthlyDueEntity) {
        val now = System.currentTimeMillis()
        val isNew = due.id == 0L
        val entity = due.copy(ownerId = ownerId, updatedAt = now)
        database.withTransaction {
            dao.upsertMonthlyDue(entity)
            dao.insertHistory(
                HistoryEntity(
                    ownerId = ownerId,
                    section = RecordSection.MONTHLY_DUE.label,
                    action = if (isNew) ACTION_ADDED else ACTION_UPDATED,
                    summary = "${entity.title} · ${entity.period}",
                    timestamp = now,
                ),
            )
        }
    }

    suspend fun deleteMonthlyDue(ownerId: String, due: MonthlyDueEntity) {
        database.withTransaction {
            dao.deleteMonthlyDue(due.id, ownerId)
            dao.insertHistory(
                HistoryEntity(
                    ownerId = ownerId,
                    section = RecordSection.MONTHLY_DUE.label,
                    action = ACTION_DELETED,
                    summary = "${due.title} · ${due.period}",
                    timestamp = System.currentTimeMillis(),
                ),
            )
        }
    }

    suspend fun clearHistory(ownerId: String) {
        dao.clearHistory(ownerId)
    }

    companion object {
        const val ACTION_ADDED = "Added"
        const val ACTION_UPDATED = "Updated"
        const val ACTION_DELETED = "Deleted"

        fun from(context: Context): RecordsRepository =
            RecordsRepository(CcmsDatabase.get(context))
    }
}
