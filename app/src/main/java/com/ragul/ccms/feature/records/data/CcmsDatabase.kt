package com.ragul.ccms.feature.records.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MembershipEntity::class,
        LoanDepositEntity::class,
        MonthlyDueEntity::class,
        FinancialTransactionEntity::class,
        HistoryEntity::class,
    ],
    version = 7,
    exportSchema = false,
)
abstract class CcmsDatabase : RoomDatabase() {

    abstract fun recordsDao(): RecordsDao

    companion object {
        @Volatile
        private var instance: CcmsDatabase? = null

        fun get(context: Context): CcmsDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    CcmsDatabase::class.java,
                    "ccms.db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7)
                    .fallbackToDestructiveMigration(true)
                    .fallbackToDestructiveMigrationOnDowngrade(false)
                    .build()
                    .also { instance = it }
            }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE memberships ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE loan_deposits ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE monthly_dues ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE history ADD COLUMN syncId TEXT NOT NULL DEFAULT ''")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE memberships ADD COLUMN memberId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE memberships ADD COLUMN gender TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE memberships ADD COLUMN dob TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE memberships ADD COLUMN parentName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE memberships ADD COLUMN grandfatherName TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE memberships ADD COLUMN citizenshipNumber TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE memberships ADD COLUMN address TEXT NOT NULL DEFAULT ''")
                database.execSQL("ALTER TABLE memberships ADD COLUMN email TEXT NOT NULL DEFAULT ''")

                database.execSQL("UPDATE memberships SET memberId = id WHERE memberId = 0")

                database.execSQL("ALTER TABLE loan_deposits ADD COLUMN memberId INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE monthly_dues ADD COLUMN memberId INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE loan_deposits ADD COLUMN time REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE loan_deposits ADD COLUMN rate REAL NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE monthly_dues ADD COLUMN amountDeposit REAL NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE monthly_dues ADD COLUMN grandTotal REAL NOT NULL DEFAULT 0")
                // Existing dues only stored one amount, so it becomes the grand total.
                database.execSQL("UPDATE monthly_dues SET grandTotal = amount WHERE grandTotal = 0")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE monthly_dues ADD COLUMN monthlyPaymentAmount REAL NOT NULL DEFAULT 0")
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS financial_transactions (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "ownerId TEXT NOT NULL, memberId INTEGER NOT NULL, " +
                        "loanSyncId TEXT NOT NULL DEFAULT '', dueSyncId TEXT NOT NULL DEFAULT '', " +
                        "parentSyncId TEXT NOT NULL DEFAULT '', kind TEXT NOT NULL, amount REAL NOT NULL, " +
                        "date INTEGER NOT NULL, status TEXT NOT NULL, notes TEXT NOT NULL, " +
                        "createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL, syncId TEXT NOT NULL DEFAULT ''" +
                        ")",
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_financial_transactions_ownerId ON financial_transactions(ownerId)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_financial_transactions_memberId ON financial_transactions(memberId)")
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_financial_transactions_syncId ON financial_transactions(syncId)")
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE loan_deposits ADD COLUMN loanStatus TEXT NOT NULL DEFAULT 'ACTIVE'")
            }
        }
    }
}
