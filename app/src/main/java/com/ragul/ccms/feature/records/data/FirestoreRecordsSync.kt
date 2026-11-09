package com.ragul.ccms.feature.records.data

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID

/** Mirrors one account's Room records to its Firestore subcollections. */
internal class FirestoreRecordsSync(
    private val firestore: FirebaseFirestore,
    private val dao: RecordsDao,
    private val ownerId: String,
    private val scope: CoroutineScope,
) {
    private val registrations = mutableListOf<ListenerRegistration>()
    private val serverInitialized = mutableSetOf<String>()
    private var closed = false

    fun start() {
        scope.launch {
            assignMissingIds()
            listen("memberships", ::syncMemberships)
            listen("loanDeposits", ::syncLoanDeposits)
            listen("monthlyDues", ::syncMonthlyDues)
            listen("history", ::syncHistory)
        }
        scope.coroutineContext[Job]?.invokeOnCompletion { close() }
    }

    fun save(membership: MembershipEntity) {
        document("memberships", membership.syncId).set(membership.toCloud(), SetOptions.merge())
            .logFailure("membership")
    }

    fun delete(membership: MembershipEntity) {
        delete("memberships", membership.syncId)
    }

    fun save(record: LoanDepositEntity) {
        document("loanDeposits", record.syncId).set(record.toCloud(), SetOptions.merge())
            .logFailure("loan or deposit")
    }

    fun delete(record: LoanDepositEntity) {
        delete("loanDeposits", record.syncId)
    }

    fun save(due: MonthlyDueEntity) {
        document("monthlyDues", due.syncId).set(due.toCloud(), SetOptions.merge())
            .logFailure("monthly due")
    }

    fun delete(due: MonthlyDueEntity) {
        delete("monthlyDues", due.syncId)
    }

    fun save(entry: HistoryEntity) {
        document("history", entry.syncId).set(entry.toCloud(), SetOptions.merge())
            .logFailure("history")
    }

    fun delete(entry: HistoryEntity) {
        delete("history", entry.syncId)
    }

    fun deleteHistory(entries: List<HistoryEntity>) {
        entries.forEach(::delete)
    }

    fun close() {
        if (closed) return
        closed = true
        registrations.forEach(ListenerRegistration::remove)
        registrations.clear()
    }

    private fun listen(collection: String, reconcile: suspend (DocumentSnapshot) -> Unit) {
        if (closed) return
        val registration = collection(collection).addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                Log.w(TAG, "Firestore listener failed for $collection", error)
                return@addSnapshotListener
            }
            if (snapshot == null) return@addSnapshotListener

            scope.launch {
                snapshot.documentChanges.forEach { change ->
                    val document = change.document
                    if (change.type.name == "REMOVED") {
                        removeRemote(collection, document.id)
                    } else {
                        reconcile(document)
                    }
                }

                // Wait for a server snapshot before treating absent documents as new local data.
                // This avoids uploading an incomplete Firestore cache while offline.
                if (!snapshot.metadata.isFromCache && serverInitialized.add(collection)) {
                    reconcileAll(snapshot.documents)
                    uploadLocalMissing(collection, snapshot.documents.mapTo(mutableSetOf()) { it.id })
                }
            }
        }
        registrations += registration
    }

    private suspend fun assignMissingIds() {
        dao.observeMemberships(ownerId).first().filter { it.syncId.isBlank() }.forEach {
            dao.upsertMembership(it.copy(syncId = UUID.randomUUID().toString()))
        }
        dao.observeLoanDeposits(ownerId).first().filter { it.syncId.isBlank() }.forEach {
            dao.upsertLoanDeposit(it.copy(syncId = UUID.randomUUID().toString()))
        }
        dao.observeMonthlyDues(ownerId).first().filter { it.syncId.isBlank() }.forEach {
            dao.upsertMonthlyDue(it.copy(syncId = UUID.randomUUID().toString()))
        }
        dao.getAllHistory(ownerId).filter { it.syncId.isBlank() }.forEach {
            dao.insertHistory(it.copy(syncId = UUID.randomUUID().toString()))
        }
    }

    private suspend fun reconcileAll(documents: List<DocumentSnapshot>) {
        documents.forEach { document ->
            when (document.reference.parent.id) {
                "memberships" -> reconcileMembership(document)
                "loanDeposits" -> reconcileLoanDeposit(document)
                "monthlyDues" -> reconcileMonthlyDue(document)
                "history" -> reconcileHistory(document)
            }
        }
    }

    private suspend fun syncMemberships(document: DocumentSnapshot) = reconcileMembership(document)

    private suspend fun reconcileMembership(document: DocumentSnapshot) {
        val remote = document.toMembership() ?: return
        val local = dao.observeMemberships(ownerId).first().firstOrNull { it.syncId == document.id }
        when {
            local == null -> dao.upsertMembership(remote)
            remote.updatedAt >= local.updatedAt -> {
                val merged = remote.copy(id = local.id)
                if (merged != local) dao.upsertMembership(merged)
            }
            local.updatedAt > remote.updatedAt -> save(local)
        }
    }

    private suspend fun syncLoanDeposits(document: DocumentSnapshot) = reconcileLoanDeposit(document)

    private suspend fun reconcileLoanDeposit(document: DocumentSnapshot) {
        val remote = document.toLoanDeposit() ?: return
        val local = dao.observeLoanDeposits(ownerId).first().firstOrNull { it.syncId == document.id }
        when {
            local == null -> dao.upsertLoanDeposit(remote)
            remote.updatedAt >= local.updatedAt -> {
                val merged = remote.copy(id = local.id)
                if (merged != local) dao.upsertLoanDeposit(merged)
            }
            local.updatedAt > remote.updatedAt -> save(local)
        }
    }

    private suspend fun syncMonthlyDues(document: DocumentSnapshot) = reconcileMonthlyDue(document)

    private suspend fun reconcileMonthlyDue(document: DocumentSnapshot) {
        val remote = document.toMonthlyDue() ?: return
        val local = dao.observeMonthlyDues(ownerId).first().firstOrNull { it.syncId == document.id }
        when {
            local == null -> dao.upsertMonthlyDue(remote)
            remote.updatedAt >= local.updatedAt -> {
                val merged = remote.copy(id = local.id)
                if (merged != local) dao.upsertMonthlyDue(merged)
            }
            local.updatedAt > remote.updatedAt -> save(local)
        }
    }

    private suspend fun syncHistory(document: DocumentSnapshot) = reconcileHistory(document)

    private suspend fun reconcileHistory(document: DocumentSnapshot) {
        val remote = document.toHistory() ?: return
        val local = dao.getAllHistory(ownerId).firstOrNull { it.syncId == document.id }
        when {
            local == null -> dao.insertHistory(remote)
            remote.timestamp >= local.timestamp -> {
                val merged = remote.copy(id = local.id)
                if (merged != local) dao.insertHistory(merged)
            }
            local.timestamp > remote.timestamp -> save(local)
        }
    }

    private suspend fun uploadLocalMissing(collection: String, remoteIds: Set<String>) {
        when (collection) {
            "memberships" -> dao.observeMemberships(ownerId).first()
                .filter { it.syncId !in remoteIds }.forEach(::save)
            "loanDeposits" -> dao.observeLoanDeposits(ownerId).first()
                .filter { it.syncId !in remoteIds }.forEach(::save)
            "monthlyDues" -> dao.observeMonthlyDues(ownerId).first()
                .filter { it.syncId !in remoteIds }.forEach(::save)
            "history" -> dao.getAllHistory(ownerId)
                .filter { it.syncId !in remoteIds }.forEach(::save)
        }
    }

    private suspend fun removeRemote(collection: String, syncId: String) {
        when (collection) {
            "memberships" -> dao.deleteMembershipBySyncId(ownerId, syncId)
            "loanDeposits" -> dao.deleteLoanDepositBySyncId(ownerId, syncId)
            "monthlyDues" -> dao.deleteMonthlyDueBySyncId(ownerId, syncId)
            "history" -> dao.deleteHistoryBySyncId(ownerId, syncId)
        }
    }

    private fun delete(collection: String, syncId: String) {
        if (syncId.isBlank()) return
        document(collection, syncId).delete().logFailure("$collection delete")
    }

    private fun collection(name: String) = firestore
        .collection("users")
        .document(ownerId)
        .collection(name)

    private fun document(collection: String, syncId: String) = collection(collection).document(syncId)

    private fun com.google.android.gms.tasks.Task<*>.logFailure(operation: String) {
        addOnFailureListener { error -> Log.w(TAG, "Firestore $operation failed; local data is retained", error) }
    }

    private fun MembershipEntity.toCloud() = mapOf(
        "ownerId" to ownerId,
        "syncId" to syncId,
        "memberId" to memberId,
        "name" to name,
        "gender" to gender,
        "dob" to dob,
        "parentName" to parentName,
        "grandfatherName" to grandfatherName,
        "citizenshipNumber" to citizenshipNumber,
        "address" to address,
        "phone" to phone,
        "email" to email,
        "status" to status,
        "notes" to notes,
        "joinedOn" to joinedOn,
        "updatedAt" to updatedAt,
    )

    private fun LoanDepositEntity.toCloud() = mapOf(
        "ownerId" to ownerId,
        "syncId" to syncId,
        "memberId" to memberId,
        "kind" to kind,
        "personName" to personName,
        "amount" to amount,
        "time" to time,
        "rate" to rate,
        "notes" to notes,
        "date" to date,
        "updatedAt" to updatedAt,
    )

    private fun MonthlyDueEntity.toCloud() = mapOf(
        "ownerId" to ownerId,
        "syncId" to syncId,
        "memberId" to memberId,
        "title" to title,
        "period" to period,
        "amount" to amount,
        "amountDeposit" to amountDeposit,
        "grandTotal" to grandTotal,
        "isPaid" to isPaid,
        "notes" to notes,
        "dueDate" to dueDate,
        "updatedAt" to updatedAt,
    )

    private fun HistoryEntity.toCloud() = mapOf(
        "ownerId" to ownerId,
        "syncId" to syncId,
        "section" to section,
        "action" to action,
        "summary" to summary,
        "timestamp" to timestamp,
    )

    private fun DocumentSnapshot.toMembership() = MembershipEntity(
        id = 0,
        memberId = getLong("memberId") ?: 0L,
        ownerId = ownerId,
        name = getString("name") ?: "",
        gender = getString("gender") ?: "",
        dob = getString("dob") ?: "",
        parentName = getString("parentName") ?: "",
        grandfatherName = getString("grandfatherName") ?: "",
        citizenshipNumber = getString("citizenshipNumber") ?: "",
        address = getString("address") ?: "",
        phone = getString("phone") ?: "",
        email = getString("email") ?: "",
        status = getString("status") ?: "Active",
        notes = getString("notes") ?: "",
        joinedOn = getLong("joinedOn") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L,
        syncId = id,
    )

    private fun DocumentSnapshot.toLoanDeposit() = LoanDepositEntity(
        id = 0,
        memberId = getLong("memberId") ?: 0L,
        ownerId = ownerId,
        kind = getString("kind") ?: LoanDepositEntity.KIND_LOAN,
        personName = getString("personName") ?: "",
        amount = getDouble("amount") ?: getLong("amount")?.toDouble() ?: 0.0,
        time = getDouble("time") ?: getLong("time")?.toDouble() ?: 0.0,
        rate = getDouble("rate") ?: getLong("rate")?.toDouble() ?: 0.0,
        notes = getString("notes") ?: "",
        date = getLong("date") ?: 0L,
        updatedAt = getLong("updatedAt") ?: 0L,
        syncId = id,
    )

    private fun DocumentSnapshot.toMonthlyDue(): MonthlyDueEntity {
        val amount = getDouble("amount") ?: getLong("amount")?.toDouble() ?: 0.0
        return MonthlyDueEntity(
            id = 0,
            memberId = getLong("memberId") ?: 0L,
            ownerId = ownerId,
            title = getString("title") ?: "",
            period = getString("period") ?: "",
            amount = amount,
            amountDeposit = getDouble("amountDeposit") ?: getLong("amountDeposit")?.toDouble() ?: 0.0,
            grandTotal = getDouble("grandTotal") ?: getLong("grandTotal")?.toDouble() ?: amount,
            isPaid = getBoolean("isPaid") ?: false,
            notes = getString("notes") ?: "",
            dueDate = getLong("dueDate") ?: 0L,
            updatedAt = getLong("updatedAt") ?: 0L,
            syncId = id,
        )
    }

    private fun DocumentSnapshot.toHistory() = HistoryEntity(
        id = 0,
        ownerId = ownerId,
        section = getString("section") ?: "",
        action = getString("action") ?: "",
        summary = getString("summary") ?: "",
        timestamp = getLong("timestamp") ?: 0L,
        syncId = id,
    )

    companion object {
        private const val TAG = "FirestoreRecordsSync"
    }
}
