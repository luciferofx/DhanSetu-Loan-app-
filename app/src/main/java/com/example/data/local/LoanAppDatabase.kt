package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UserProfileEntity::class,
        LoanApplicationEntity::class,
        EmiItemEntity::class,
        TransactionRecordEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class LoanAppDatabase : RoomDatabase() {
    abstract fun loanAppDao(): LoanAppDao

    companion object {
        @Volatile
        private var INSTANCE: LoanAppDatabase? = null

        fun getDatabase(context: Context): LoanAppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    LoanAppDatabase::class.java,
                    "instant_loan_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
