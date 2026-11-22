package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.wellness.SupportRequestEntity
import com.example.data.wellness.WelfareAuditLogEntity
import com.example.data.wellness.WellnessConsentEntity
import com.example.data.wellness.WellnessDao
import com.example.data.wellness.WellnessRecordEntity

@Database(
    entities = [
        AssessmentRecordEntity::class,
        EmergencyContactEntity::class,
        WellnessRecordEntity::class,
        SupportRequestEntity::class,
        WellnessConsentEntity::class,
        WelfareAuditLogEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun assessmentDao(): AssessmentDao
    abstract fun emergencyContactDao(): EmergencyContactDao
    abstract fun wellnessDao(): WellnessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "swasthyasathi_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
