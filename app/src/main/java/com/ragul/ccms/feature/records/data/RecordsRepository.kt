package com.ragul.ccms.feature.records.data

import android.content.Context
import androidx.room.withTransaction
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import java.text.NumberFormat
import java.util.UUID

/** Firestore is the shared record store; Room is its local replica for offline access. */
class RecordsRepository(
    private val database: CcmsDatabase,
    private val firestore: FirebaseFirestore? = null,
) {
    private val dao = database.recordsDao()
    private var firestoreSync: FirestoreRecordsSync? = null

    fun startSync(ownerId: String, scope: CoroutineScope) {
        val cloud = firestore ?: return
        if (firestoreSync != null) return
        FirestoreRecordsSync(cloud, dao, ownerId, scope).also {
            firestoreSync = it
            it.start()
        }
    }

    fun observeMemberships(ownerId: String): Flow<List<MembershipEntity>> =
        dao.observeMemberships(ownerId)

    fun observeLoanDeposits(ownerId: String): Flow<List<LoanDepositEntity>> =
        dao.observeLoanDeposits(ownerId)

    fun observeMonthlyDues(ownerId: String): Flow<List<MonthlyDueEntity>> =
        dao.observeMonthlyDues(ownerId)

    fun observeHistory(ownerId: String): Flow<List<HistoryEntity>> = dao.observeHistory(ownerId)

    fun observeCounts(ownerId: String): Flow<RecordCounts> = combine(
        dao.observeMembershipCount(ownerId),
        dao.observeLoanDepositCount(ownerId),
        dao.observeMonthlyDueCount(ownerId),
    ) { memberships, loanDeposits, monthlyDues ->
        RecordCounts(memberships, loanDeposits, monthlyDues)
    }

    suspend fun getMaxMemberId(ownerId: String): Long = dao.getMaxMemberId(ownerId) ?: 0L

    suspend fun saveMembership(ownerId: String, membership: MembershipEntity) {
        val now = System.currentTimeMillis()
        val isNew = membership.id == 0L
        val assignedMemberId = if (membership.memberId == 0L) {
            (dao.getMaxMemberId(ownerId) ?: 0L) + 1L
        } else {
            membership.memberId
        }
        val entity = membership.copy(
            ownerId = ownerId,
            memberId = assignedMemberId,
            updatedAt = now,
            syncId = membership.syncId.withId(),
        )
        val history = newHistory(
            ownerId, RecordSection.MEMBERSHIP, if (isNew) ACTION_ADDED else ACTION_UPDATED,
            "${entity.name} (#${entity.memberId})", now,
        )
        database.withTransaction {
            dao.upsertMembership(entity)
            dao.insertHistory(history)
        }
        firestoreSync?.save(entity)
        firestoreSync?.save(history)
    }

    suspend fun deleteMembership(ownerId: String, membership: MembershipEntity) {
        val history = newHistory(ownerId, RecordSection.MEMBERSHIP, ACTION_DELETED, "${membership.name} (#${membership.memberId})")
        database.withTransaction {
            dao.deleteMembership(membership.id, ownerId)
            dao.insertHistory(history)
        }
        firestoreSync?.delete(membership)
        firestoreSync?.save(history)
    }

    suspend fun saveLoanDeposit(ownerId: String, loanDeposit: LoanDepositEntity) {
        val now = System.currentTimeMillis()
        val isNew = loanDeposit.id == 0L
        val entity = loanDeposit.copy(
            ownerId = ownerId,
            updatedAt = now,
            syncId = loanDeposit.syncId.withId(),
        )
        val type = if (entity.isLoan) "Loan" else "Deposit"
        val history = newHistory(
            ownerId, RecordSection.LOAN_DEPOSIT, if (isNew) ACTION_ADDED else ACTION_UPDATED,
            "$type · ${entity.personName} · ${formatAmount(entity.amount)}", now,
        )
        database.withTransaction {
            dao.upsertLoanDeposit(entity)
            dao.insertHistory(history)
        }
        firestoreSync?.save(entity)
        firestoreSync?.save(history)
    }

    suspend fun deleteLoanDeposit(ownerId: String, loanDeposit: LoanDepositEntity) {
        val type = if (loanDeposit.isLoan) "Loan" else "Deposit"
        val history = newHistory(
            ownerId, RecordSection.LOAN_DEPOSIT, ACTION_DELETED, "$type · ${loanDeposit.personName} · ${formatAmount(loanDeposit.amount)}",
        )
        database.withTransaction {
            dao.deleteLoanDeposit(loanDeposit.id, ownerId)
            dao.insertHistory(history)
        }
        firestoreSync?.delete(loanDeposit)
        firestoreSync?.save(history)
    }

    suspend fun saveMonthlyDue(ownerId: String, due: MonthlyDueEntity) {
        val now = System.currentTimeMillis()
        val isNew = due.id == 0L
        val entity = due.copy(ownerId = ownerId, updatedAt = now, syncId = due.syncId.withId())
        val statusText = if (entity.isPaid) "Paid" else "Pending"
        val history = newHistory(
            ownerId, RecordSection.MONTHLY_DUE, if (isNew) ACTION_ADDED else ACTION_UPDATED,
            "${entity.title} · ${entity.period} · $statusText · ${formatAmount(entity.amount)}", now,
        )
        database.withTransaction {
            dao.upsertMonthlyDue(entity)
            dao.insertHistory(history)
        }
        firestoreSync?.save(entity)
        firestoreSync?.save(history)
    }

    suspend fun deleteMonthlyDue(ownerId: String, due: MonthlyDueEntity) {
        val statusText = if (due.isPaid) "Paid" else "Pending"
        val history = newHistory(
            ownerId, RecordSection.MONTHLY_DUE, ACTION_DELETED, "${due.title} · ${due.period} · $statusText · ${formatAmount(due.amount)}",
        )
        database.withTransaction {
            dao.deleteMonthlyDue(due.id, ownerId)
            dao.insertHistory(history)
        }
        firestoreSync?.delete(due)
        firestoreSync?.save(history)
    }

    suspend fun clearHistory(ownerId: String) {
        val history = dao.getAllHistory(ownerId)
        dao.clearHistory(ownerId)
        firestoreSync?.deleteHistory(history)
    }

    private fun formatAmount(value: Double): String =
        NumberFormat.getCurrencyInstance().format(value)

    private fun String.withId(): String = ifBlank { UUID.randomUUID().toString() }

    private fun newHistory(
        ownerId: String,
        section: RecordSection,
        action: String,
        summary: String,
        timestamp: Long = System.currentTimeMillis(),
    ) = HistoryEntity(
        ownerId = ownerId,
        section = section.label,
        action = action,
        summary = summary,
        timestamp = timestamp,
        syncId = UUID.randomUUID().toString(),
    )

    companion object {
        const val ACTION_ADDED = "Added"
        const val ACTION_UPDATED = "Updated"
        const val ACTION_DELETED = "Deleted"

        fun from(context: Context): RecordsRepository {
            val appContext = context.applicationContext
            val cloud = runCatching {
                if (FirebaseApp.getApps(appContext).isEmpty()) null else FirebaseFirestore.getInstance()
            }.getOrNull()
            return RecordsRepository(CcmsDatabase.get(appContext), cloud)
        }
    }
}
