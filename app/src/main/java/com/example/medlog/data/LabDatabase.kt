package com.queryb.medlog.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LabResult::class], version = 2, exportSchema = false)
abstract class LabDatabase : RoomDatabase() {

    abstract fun labResultDao(): LabResultDao

    companion object {
        @Volatile
        private var INSTANCE: LabDatabase? = null
        // Migration: adds userId column (empty string default for existing rows)
        private val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(database: androidx.sqlite.db.SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE lab_results ADD COLUMN userId TEXT NOT NULL DEFAULT ''"
                )
            }
        }

        fun getDatabase(context: Context): LabDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    LabDatabase::class.java,
                    "lab_database"
                ).addMigrations(MIGRATION_1_2).build().also { INSTANCE = it }
            }
        }
    }
}
