package com.ragul.ccms.feature.records.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordsDao {

    @Query("SELECT * FROM memberships WHERE ownerId = :ownerId ORDER BY name COLLATE NOCASE ASC")
    fun observeMemberships(ownerId: String): Flow<List<MembershipEntity>>

    @Query("SELECT COUNT(*) FROM memberships WHERE ownerId = :ownerId")
    fun observeMembershipCount(ownerId: String): Flow<Int>

    @Query("SELECT MAX(memberId) FROM memberships WHERE ownerId = :ownerId")
    suspend fun getMaxMemberId(ownerId: String): Long?

    @Upsert
    suspend fun upsertMembership(membership: MembershipEntity)

    @Query("DELETE FROM memberships WHERE ownerId = :ownerId AND syncId = :syncId")
    suspend fun deleteMembershipBySyncId(ownerId: String, syncId: String)

    @Query("DELETE FROM memberships WHERE id = :id AND ownerId = :ownerId")
    suspend fun deleteMembership(id: Long, ownerId: String)

    @Query("SELECT * FROM loan_deposits WHERE ownerId = :ownerId ORDER BY date DESC, id DESC")
    fun observeLoanDeposits(ownerId: String): Flow<List<LoanDepositEntity>>

    @Query("SELECT COUNT(*) FROM loan_deposits WHERE ownerId = :ownerId")
    fun observeLoanDepositCount(ownerId: String): Flow<Int>

    @Upsert
    suspend fun upsertLoanDeposit(loanDeposit: LoanDepositEntity)

    @Query("DELETE FROM loan_deposits WHERE ownerId = :ownerId AND syncId = :syncId")
    suspend fun deleteLoanDepositBySyncId(ownerId: String, syncId: String)

    @Query("DELETE FROM loan_deposits WHERE id = :id AND ownerId = :ownerId")
    suspend fun deleteLoanDeposit(id: Long, ownerId: String)

    @Query("SELECT * FROM loan_deposits WHERE ownerId = :ownerId AND memberId = :memberId AND kind = 'LOAN' ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getLoanForMember(ownerId: String, memberId: Long): LoanDepositEntity?

    @Query("SELECT * FROM loan_deposits WHERE ownerId = :ownerId AND syncId = :syncId LIMIT 1")
    suspend fun getLoanBySyncId(ownerId: String, syncId: String): LoanDepositEntity?

    @Query("SELECT * FROM financial_transactions WHERE ownerId = :ownerId ORDER BY date DESC, id DESC")
    fun observeFinancialTransactions(ownerId: String): Flow<List<FinancialTransactionEntity>>

    @Query("SELECT * FROM financial_transactions WHERE ownerId = :ownerId AND syncId = :syncId LIMIT 1")
    suspend fun getFinancialTransaction(ownerId: String, syncId: String): FinancialTransactionEntity?

    @Query("SELECT * FROM financial_transactions WHERE ownerId = :ownerId")
    suspend fun getAllFinancialTransactions(ownerId: String): List<FinancialTransactionEntity>

    @Upsert
    suspend fun upsertFinancialTransaction(transaction: FinancialTransactionEntity)

    @Query("DELETE FROM financial_transactions WHERE ownerId = :ownerId AND syncId = :syncId")
    suspend fun deleteFinancialTransaction(ownerId: String, syncId: String)

    @Query("DELETE FROM financial_transactions WHERE ownerId = :ownerId AND loanSyncId = :loanSyncId")
    suspend fun deleteFinancialTransactionsByLoanSyncId(ownerId: String, loanSyncId: String)

    @Query("SELECT * FROM monthly_dues WHERE ownerId = :ownerId ORDER BY updatedAt DESC")
    fun observeMonthlyDues(ownerId: String): Flow<List<MonthlyDueEntity>>

    @Query("SELECT COUNT(*) FROM monthly_dues WHERE ownerId = :ownerId")
    fun observeMonthlyDueCount(ownerId: String): Flow<Int>

    @Upsert
    suspend fun upsertMonthlyDue(due: MonthlyDueEntity)

    @Query("DELETE FROM monthly_dues WHERE ownerId = :ownerId AND syncId = :syncId")
    suspend fun deleteMonthlyDueBySyncId(ownerId: String, syncId: String)

    @Query("DELETE FROM monthly_dues WHERE id = :id AND ownerId = :ownerId")
    suspend fun deleteMonthlyDue(id: Long, ownerId: String)

    @Query("SELECT * FROM history WHERE ownerId = :ownerId ORDER BY timestamp DESC, id DESC LIMIT 200")
    fun observeHistory(ownerId: String): Flow<List<HistoryEntity>>

    @Upsert
    suspend fun insertHistory(entry: HistoryEntity)

    @Query("SELECT * FROM history WHERE ownerId = :ownerId ORDER BY timestamp DESC, id DESC")
    suspend fun getAllHistory(ownerId: String): List<HistoryEntity>

    @Query("DELETE FROM history WHERE ownerId = :ownerId AND syncId = :syncId")
    suspend fun deleteHistoryBySyncId(ownerId: String, syncId: String)

    @Query("DELETE FROM history WHERE ownerId = :ownerId")
    suspend fun clearHistory(ownerId: String)
}
