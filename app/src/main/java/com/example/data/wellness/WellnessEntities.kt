package com.example.data.wellness

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "wellness_records")
data class WellnessRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personnelId: String = "self",
    val stressLevel: Int,
    val mood: String,
    val sleepHours: Float,
    val sleepQuality: Int,
    val fatigueLevel: Int,
    val workloadPerception: Int,
    val dutyHoursLast24h: Float,
    val nightDutyCountLastWeek: Int,
    val deploymentDays: Int,
    val daysSinceLastLeave: Int,
    val familySupportRating: Int,
    val interestInSupport: Boolean,
    val indicatorLevel: String,
    val compositeScore: Float,
    val contributingSignalsJson: String,
    val recommendationsJson: String,
    val optionalNotes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "welfare_support_requests")
data class SupportRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personnelId: String = "self",
    val personnelName: String = "Confidential Personnel",
    val unitOrStation: String = "General Duty Unit",
    val category: String,
    val preferredContactMethod: String = "In-Person Discussion",
    val urgency: String = "Timely",
    val details: String = "",
    val status: String = "NEW",
    val officerReviewNotes: String? = null,
    val reviewedBy: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "wellness_consent_records")
data class WellnessConsentEntity(
    @PrimaryKey val personnelId: String = "self",
    val voluntaryAssessmentConsent: Boolean = true,
    val wearableConsent: Boolean = false,
    val anonymizedUnitAnalyticsConsent: Boolean = true,
    val consentedAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "welfare_audit_logs")
data class WelfareAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val action: String,
    val performedByRole: String,
    val personnelOrUnitIdentifier: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)
