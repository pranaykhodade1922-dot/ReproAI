package com.pranay.reproai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.pranay.reproai.data.local.dao.DebugEventDao
import com.pranay.reproai.data.local.dao.DebugSessionDao
import com.pranay.reproai.data.local.entity.DebugEventEntity
import com.pranay.reproai.data.local.entity.DebugSessionEntity

@Database(
    entities = [DebugSessionEntity::class, DebugEventEntity::class, com.pranay.reproai.data.local.entity.IncidentExecutionEntity::class,
        com.pranay.reproai.data.local.entity.ReportMetadataEntity::class, com.pranay.reproai.data.local.entity.ExecutionHistoryEntity::class,
        com.pranay.reproai.data.local.entity.IncidentAnalysisEntity::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ReproDatabase : RoomDatabase() {

    abstract fun debugSessionDao(): DebugSessionDao
    abstract fun debugEventDao(): DebugEventDao
    abstract fun incidentExecutionDao(): com.pranay.reproai.data.local.dao.IncidentExecutionDao
    abstract fun reportDao(): com.pranay.reproai.data.local.dao.ReportDao

    companion object {
        @Volatile
        private var INSTANCE: ReproDatabase? = null

        fun getInstance(context: Context): ReproDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ReproDatabase::class.java,
                    "repro_ai.db"
                ).addMigrations(object : androidx.room.migration.Migration(1, 2) {
                    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS incident_executions (sessionId TEXT NOT NULL PRIMARY KEY, analysisJson TEXT NOT NULL, scenarioJson TEXT NOT NULL, resultJson TEXT NOT NULL, purpose TEXT NOT NULL)")
                    }
                }, object : androidx.room.migration.Migration(2, 3) {
                    override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                        db.execSQL("CREATE TABLE IF NOT EXISTS incident_reports (sessionId TEXT NOT NULL PRIMARY KEY, reportId TEXT NOT NULL, createdAt INTEGER NOT NULL, status TEXT NOT NULL, likelyOwnerCategory TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS execution_history (executionId TEXT NOT NULL PRIMARY KEY, sessionId TEXT NOT NULL, purpose TEXT NOT NULL, resultJson TEXT NOT NULL, scenarioId TEXT NOT NULL, finishedAt TEXT NOT NULL)")
                        db.execSQL("CREATE TABLE IF NOT EXISTS incident_analyses (sessionId TEXT NOT NULL PRIMARY KEY, analysisJson TEXT NOT NULL)")
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
