package com.ragul.ccms.feature.records.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ragul.ccms.feature.records.data.HistoryEntity
import com.ragul.ccms.feature.records.data.LoanDepositEntity
import com.ragul.ccms.feature.records.data.MembershipEntity
import com.ragul.ccms.feature.records.data.MonthlyDueEntity
import com.ragul.ccms.feature.records.data.RecordCounts
import com.ragul.ccms.feature.records.data.RecordsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Exposes the user's records from the local database. Every flow is scoped to the signed-in
 * account, and Room re-emits whenever a row is inserted, updated or deleted.
 */
class RecordsViewModel(
    private val repository: RecordsRepository,
    private val ownerId: String,
) : ViewModel() {

    val counts: StateFlow<RecordCounts> = repository.observeCounts(ownerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), RecordCounts())

    val memberships: StateFlow<List<MembershipEntity>> = repository.observeMemberships(ownerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val loanDeposits: StateFlow<List<LoanDepositEntity>> = repository.observeLoanDeposits(ownerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val monthlyDues: StateFlow<List<MonthlyDueEntity>> = repository.observeMonthlyDues(ownerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val history: StateFlow<List<HistoryEntity>> = repository.observeHistory(ownerId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        repository.startSync(ownerId, viewModelScope)
    }

    fun saveMembership(
        existing: MembershipEntity?,
        name: String,
        gender: String,
        dob: String,
        parentName: String,
        grandfatherName: String,
        citizenshipNumber: String,
        address: String,
        phone: String,
        email: String,
        status: String,
        notes: String,
    ) {
        val now = System.currentTimeMillis()
        val entity = MembershipEntity(
            id = existing?.id ?: 0L,
            memberId = existing?.memberId ?: 0L,
            ownerId = ownerId,
            name = name.trim(),
            gender = gender.trim(),
            dob = dob.trim(),
            parentName = parentName.trim(),
            grandfatherName = grandfatherName.trim(),
            citizenshipNumber = citizenshipNumber.trim(),
            address = address.trim(),
            phone = phone.trim(),
            email = email.trim(),
            status = status,
            notes = notes.trim(),
            joinedOn = existing?.joinedOn ?: now,
            updatedAt = now,
        )
        write("Membership saved.") { repository.saveMembership(ownerId, entity) }
    }

    fun deleteMembership(membership: MembershipEntity) {
        write("Membership deleted.") { repository.deleteMembership(ownerId, membership) }
    }

    fun saveLoanDeposit(
        existing: LoanDepositEntity?,
        memberId: Long,
        isLoan: Boolean,
        personName: String,
        amount: Double,
        time: Double,
        rate: Double,
        notes: String,
    ) {
        val now = System.currentTimeMillis()
        val entity = LoanDepositEntity(
            id = existing?.id ?: 0L,
            memberId = memberId,
            ownerId = ownerId,
            kind = if (isLoan) LoanDepositEntity.KIND_LOAN else LoanDepositEntity.KIND_DEPOSIT,
            personName = personName.trim(),
            amount = amount,
            time = time,
            rate = rate,
            notes = notes.trim(),
            date = existing?.date ?: now,
            updatedAt = now,
        )
        write("${if (isLoan) "Loan" else "Deposit"} saved.") {
            repository.saveLoanDeposit(ownerId, entity)
        }
    }

    fun deleteLoanDeposit(loanDeposit: LoanDepositEntity) {
        write("Record deleted.") { repository.deleteLoanDeposit(ownerId, loanDeposit) }
    }

    fun saveMonthlyDue(
        existing: MonthlyDueEntity?,
        memberId: Long,
        title: String,
        period: String,
        amountDeposit: Double,
        grandTotal: Double,
        isPaid: Boolean,
        notes: String,
    ) {
        val now = System.currentTimeMillis()
        val entity = MonthlyDueEntity(
            id = existing?.id ?: 0L,
            memberId = memberId,
            ownerId = ownerId,
            title = title.trim(),
            period = period.trim(),
            amount = grandTotal,
            amountDeposit = amountDeposit,
            grandTotal = grandTotal,
            isPaid = isPaid,
            notes = notes.trim(),
            dueDate = existing?.dueDate ?: now,
            updatedAt = now,
        )
        write("Monthly due saved.") { repository.saveMonthlyDue(ownerId, entity) }
    }

    fun deleteMonthlyDue(due: MonthlyDueEntity) {
        write("Monthly due deleted.") { repository.deleteMonthlyDue(ownerId, due) }
    }

    fun clearHistory() {
        write("History cleared.") { repository.clearHistory(ownerId) }
    }

    fun showMessage(message: String) {
        _message.value = message
    }

    fun messageShown() {
        _message.value = null
    }

    private fun write(successMessage: String, action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }
                .onSuccess { _message.value = successMessage }
                .onFailure { error ->
                    _message.value = error.message ?: "Could not save the record."
                }
        }
    }

    companion object {
        fun factory(context: Context, ownerId: String): ViewModelProvider.Factory =
            viewModelFactory {
                initializer {
                    RecordsViewModel(
                        repository = RecordsRepository.from(context.applicationContext),
                        ownerId = ownerId,
                    )
                }
            }
    }
}
