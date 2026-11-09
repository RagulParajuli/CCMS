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

    fun observeFinancialTransactions(ownerId: String): Flow<List<FinancialTransactionEntity>> =
        dao.observeFinancialTransactions(ownerId)

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
            if (entity.isLoan) {
                if (!isNew && dao.getAllFinancialTransactions(ownerId).any { it.loanSyncId == entity.syncId }) {
                    val original = dao.observeLoanDeposits(ownerId).first().firstOrNull { it.id == entity.id }
                    require(original == null || (original.amount == entity.amount && original.time == entity.time && original.rate == entity.rate)) {
                        "Loan terms cannot change after financial transactions are recorded."
                    }
                }
            }
            dao.upsertLoanDeposit(entity)
            dao.insertHistory(history)
        }
        firestoreSync?.save(entity)
        firestoreSync?.save(history)
    }

    suspend fun saveFinancialTransaction(ownerId: String, transaction: FinancialTransactionEntity) {
        require(transaction.amount > 0.0) { "Transaction amount must be greater than zero." }
        val now = System.currentTimeMillis()
        val syncId = transaction.syncId.ifBlank { UUID.randomUUID().toString() }
        val previous = dao.getFinancialTransaction(ownerId, syncId)
        val entity = transaction.copy(
            ownerId = ownerId,
            syncId = syncId,
            createdAt = previous?.createdAt ?: now,
            updatedAt = now,
        )
        val allTransactions = dao.getAllFinancialTransactions(ownerId).filterNot { it.syncId == syncId }
        validateFinancialTransaction(ownerId, entity, allTransactions)
        val history = if (previous == null) newHistory(
            ownerId,
            RecordSection.LOAN_DEPOSIT,
            ACTION_ADDED,
            "${entity.kind} · member #${entity.memberId} · ${formatAmount(entity.amount)} · ${entity.status}",
            now,
        ) else null
        var updatedLoan: LoanDepositEntity? = null
        database.withTransaction {
            dao.upsertFinancialTransaction(entity)
            if (history != null) dao.insertHistory(history)
            if (entity.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT ||
                entity.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION
            ) {
                val loan = dao.getLoanBySyncId(ownerId, entity.loanSyncId)
                if (loan != null && loan.syncId == entity.loanSyncId) {
                    val paid = dao.getAllFinancialTransactions(ownerId).filter {
                        it.memberId == entity.memberId && it.loanSyncId == loan.syncId &&
                            it.status == FinancialTransactionEntity.STATUS_PAID &&
                            (it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT ||
                                it.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION)
                    }.sumOf { it.amount }
                    val status = if (loan.total - paid <= 0.000001) {
                        LoanDepositEntity.LOAN_STATUS_PAID_OFF
                    } else {
                        LoanDepositEntity.LOAN_STATUS_ACTIVE
                    }
                    updatedLoan = loan.copy(loanStatus = status, updatedAt = now)
                    dao.upsertLoanDeposit(updatedLoan!!)
                }
            }
        }
        firestoreSync?.save(entity)
        updatedLoan?.let { firestoreSync?.save(it) }
        if (history != null) firestoreSync?.save(history)
    }

    private suspend fun validateFinancialTransaction(
        ownerId: String,
        transaction: FinancialTransactionEntity,
        existing: List<FinancialTransactionEntity>,
    ) {
        if (transaction.status != FinancialTransactionEntity.STATUS_PAID) return
        when (transaction.kind) {
            FinancialTransactionEntity.KIND_LOAN_REPAYMENT,
            FinancialTransactionEntity.KIND_EXTRA_ALLOCATION -> {
                val loan = dao.getLoanBySyncId(ownerId, transaction.loanSyncId)
                    ?: throw IllegalArgumentException("A loan is required for this repayment.")
                require(transaction.loanSyncId == loan.syncId) { "Transaction is linked to a different loan record." }
                val balance = (loan.total - existing.filter {
                    it.memberId == transaction.memberId && it.loanSyncId == loan.syncId &&
                        it.status == FinancialTransactionEntity.STATUS_PAID &&
                        (it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT ||
                            it.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION)
                }.sumOf { it.amount }).coerceAtLeast(0.0)
                require(transaction.amount <= balance + 0.000001) { "Payment exceeds the outstanding loan balance." }
                if (transaction.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT && transaction.dueSyncId.isNotBlank()) {
                    val due = dao.observeMonthlyDues(ownerId).first().firstOrNull { it.syncId == transaction.dueSyncId }
                        ?: throw IllegalArgumentException("Monthly due was not found.")
                    require(due.memberId == transaction.memberId) { "Monthly due belongs to a different member." }
                    val paidForDue = existing.filter {
                        it.dueSyncId == due.syncId && it.kind == FinancialTransactionEntity.KIND_LOAN_REPAYMENT &&
                            it.status == FinancialTransactionEntity.STATUS_PAID
                    }.sumOf { it.amount }
                    val dueBalance = (due.monthlyPaymentAmount - paidForDue).coerceAtLeast(0.0)
                    require(transaction.amount <= dueBalance + 0.000001) {
                        "Payment exceeds the unpaid monthly amount. Record the extra as a separate deposit."
                    }
                }
                if (transaction.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION) {
                    require(transaction.parentSyncId.isNotBlank()) { "Select an extra deposit to allocate." }
                    val deposit = existing.firstOrNull {
                        it.syncId == transaction.parentSyncId && it.kind == FinancialTransactionEntity.KIND_EXTRA_DEPOSIT &&
                            it.status == FinancialTransactionEntity.STATUS_PAID && it.memberId == transaction.memberId &&
                            it.loanSyncId == transaction.loanSyncId
                    } ?: throw IllegalArgumentException("A confirmed extra deposit is required for allocation.")
                    val available = deposit.amount - existing.filter {
                        it.parentSyncId == deposit.syncId && it.status == FinancialTransactionEntity.STATUS_PAID &&
                            (it.kind == FinancialTransactionEntity.KIND_EXTRA_WITHDRAWAL || it.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION)
                    }.sumOf { it.amount }
                    require(transaction.amount <= available + 0.000001) { "Allocation exceeds the available extra deposit." }
                }
            }
            FinancialTransactionEntity.KIND_EXTRA_WITHDRAWAL -> {
                val deposit = existing.firstOrNull {
                    it.syncId == transaction.parentSyncId && it.kind == FinancialTransactionEntity.KIND_EXTRA_DEPOSIT &&
                        it.status == FinancialTransactionEntity.STATUS_PAID && it.memberId == transaction.memberId &&
                        it.loanSyncId == transaction.loanSyncId
                } ?: throw IllegalArgumentException("A confirmed extra deposit is required for withdrawal.")
                val available = deposit.amount - existing.filter {
                    it.parentSyncId == deposit.syncId && it.status == FinancialTransactionEntity.STATUS_PAID &&
                        (it.kind == FinancialTransactionEntity.KIND_EXTRA_WITHDRAWAL || it.kind == FinancialTransactionEntity.KIND_EXTRA_ALLOCATION)
                }.sumOf { it.amount }
                require(transaction.amount <= available + 0.000001) { "Withdrawal exceeds the available extra deposit." }
            }
            FinancialTransactionEntity.KIND_EXTRA_DEPOSIT -> Unit
            else -> throw IllegalArgumentException("Unknown financial transaction type.")
        }
    }

    suspend fun deleteLoanDeposit(ownerId: String, loanDeposit: LoanDepositEntity) {
        val type = if (loanDeposit.isLoan) "Loan" else "Deposit"
        val history = newHistory(
            ownerId, RecordSection.LOAN_DEPOSIT, ACTION_DELETED, "$type · ${loanDeposit.personName} · ${formatAmount(loanDeposit.amount)}",
        )
        val associatedTransactions = if (loanDeposit.isLoan && loanDeposit.syncId.isNotBlank()) {
            dao.getAllFinancialTransactions(ownerId).filter { it.loanSyncId == loanDeposit.syncId }
        } else {
            emptyList()
        }
        database.withTransaction {
            if (loanDeposit.isLoan && loanDeposit.syncId.isNotBlank()) {
                dao.deleteFinancialTransactionsByLoanSyncId(ownerId, loanDeposit.syncId)
            }
            dao.deleteLoanDeposit(loanDeposit.id, ownerId)
            dao.insertHistory(history)
        }
        associatedTransactions.forEach { transaction ->
            firestoreSync?.delete(transaction)
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
            val duplicatePeriod = dao.observeMonthlyDues(ownerId).first().firstOrNull {
                it.memberId == entity.memberId && it.period == entity.period && it.id != entity.id
            }
            require(duplicatePeriod == null) { "A monthly due already exists for this member and period." }
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
