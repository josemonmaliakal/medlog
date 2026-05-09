package com.queryb.medlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [LabResult::class], version = 4, exportSchema = false)
abstract class LabDatabase : RoomDatabase() {

    abstract fun labResultDao(): LabResultDao

    companion object {
        @Volatile
        private var INSTANCE: LabDatabase? = null

        // 1 → 2: added userId
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE lab_results ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        // 2 → 3: added readingType
        private val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE lab_results ADD COLUMN readingType TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        // 3 → 4: adds time column (was in LabResult.kt but missing from DB)
        private val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE lab_results ADD COLUMN time TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getDatabase(context: Context): LabDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    LabDatabase::class.java,
                    "lab_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}