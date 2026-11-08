package com.ragul.ccms.feature.records.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MembershipEntity::class,
        LoanDepositEntity::class,
        MonthlyDueEntity::class,
        HistoryEntity::class,
    ],
    version = 1,
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
                ).build().also { instance = it }
            }
    }
}
