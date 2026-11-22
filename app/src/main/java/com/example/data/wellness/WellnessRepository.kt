package com.example.data.wellness

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONArray

class WellnessRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val wellnessDao = database.wellnessDao()

    val wellnessRecords: Flow<List<WellnessRecordEntity>> = wellnessDao.getAllWellnessRecords()
    val latestWellnessRecord: Flow<WellnessRecordEntity?> = wellnessDao.getLatestWellnessRecord()
    val supportRequests: Flow<List<SupportRequestEntity>> = wellnessDao.getAllSupportRequests()
    val consentRecord: Flow<WellnessConsentEntity?> = wellnessDao.getConsent()
    val auditLogs: Flow<List<WelfareAuditLogEntity>> = wellnessDao.getAuditLogs()

    suspend fun saveWellnessAssessment(
        duty: DutyWorkloadData,
        wellness: SelfReportedWellness,
        wearable: WearableBiometricData = WearableBiometricData(),
        language: AppLanguage = AppLanguage.ENGLISH
    ): WelfareAssessmentResult = withContext(Dispatchers.IO) {
        val result = WelfareAiEngine.analyzeWelfarePattern(duty, wellness, wearable, language)

        val entity = WellnessRecordEntity(
            personnelId = "self",
            stressLevel = wellness.stressLevel,
            mood = wellness.mood,
            sleepHours = wellness.sleepHours,
            sleepQuality = wellness.sleepQuality,
            fatigueLevel = wellness.fatigueLevel,
            workloadPerception = wellness.workloadPerception,
            dutyHoursLast24h = duty.dutyHoursLast24h,
            nightDutyCountLastWeek = duty.nightDutyCountLastWeek,
            deploymentDays = duty.deploymentDays,
            daysSinceLastLeave = duty.daysSinceLastLeave,
            familySupportRating = wellness.familySupportRating,
            interestInSupport = wellness.interestInSupport,
            indicatorLevel = result.indicatorLevel.name,
            compositeScore = result.compositeScore,
            contributingSignalsJson = JSONArray(result.contributingSignals).toString(),
            recommendationsJson = JSONArray(result.recommendationsEn).toString(),
            optionalNotes = wellness.optionalNotes
        )

        wellnessDao.insertWellnessRecord(entity)

        // Audit log
        wellnessDao.insertAuditLog(
            WelfareAuditLogEntity(
                action = "ASSESSMENT_COMPLETED",
                performedByRole = UserRole.PERSONNEL.name,
                personnelOrUnitIdentifier = "self",
                details = "Completed wellness assessment: Indicator Level = ${result.indicatorLevel.name}, Score = ${result.compositeScore.toInt()}"
            )
        )

        result
    }

    suspend fun submitSupportRequest(
        category: SupportCategory,
        details: String,
        urgency: String = "Timely",
        preferredContactMethod: String = "In-Person Discussion",
        personnelName: String = "Confidential Personnel",
        unitOrStation: String = "General Duty Unit"
    ): Long = withContext(Dispatchers.IO) {
        val request = SupportRequestEntity(
            personnelId = "self",
            personnelName = personnelName,
            unitOrStation = unitOrStation,
            category = category.name,
            preferredContactMethod = preferredContactMethod,
            urgency = urgency,
            details = details,
            status = RequestStatus.NEW.name
        )
        val id = wellnessDao.insertSupportRequest(request)

        wellnessDao.insertAuditLog(
            WelfareAuditLogEntity(
                action = "SUPPORT_REQUEST_SUBMITTED",
                performedByRole = UserRole.PERSONNEL.name,
                personnelOrUnitIdentifier = "self",
                details = "Submitted ${category.name} request (Urgency: $urgency)"
            )
        )
        id
    }

    suspend fun updateSupportRequestStatus(
        request: SupportRequestEntity,
        newStatus: RequestStatus,
        officerNotes: String?,
        reviewedBy: String
    ) = withContext(Dispatchers.IO) {
        val updated = request.copy(
            status = newStatus.name,
            officerReviewNotes = officerNotes,
            reviewedBy = reviewedBy,
            updatedAt = System.currentTimeMillis()
        )
        wellnessDao.updateSupportRequest(updated)

        wellnessDao.insertAuditLog(
            WelfareAuditLogEntity(
                action = "SUPPORT_REQUEST_REVIEWED",
                performedByRole = UserRole.WELFARE_OFFICER.name,
                personnelOrUnitIdentifier = request.personnelId,
                details = "Updated request #${request.id} to ${newStatus.name} by $reviewedBy"
            )
        )
    }

    suspend fun updateConsent(
        voluntaryAssessment: Boolean,
        wearable: Boolean,
        unitAnalytics: Boolean
    ) = withContext(Dispatchers.IO) {
        val record = WellnessConsentEntity(
            personnelId = "self",
            voluntaryAssessmentConsent = voluntaryAssessment,
            wearableConsent = wearable,
            anonymizedUnitAnalyticsConsent = unitAnalytics,
            updatedAt = System.currentTimeMillis()
        )
        wellnessDao.upsertConsent(record)

        wellnessDao.insertAuditLog(
            WelfareAuditLogEntity(
                action = "CONSENT_UPDATED",
                performedByRole = UserRole.PERSONNEL.name,
                personnelOrUnitIdentifier = "self",
                details = "Updated consent: Voluntary=$voluntaryAssessment, Wearable=$wearable, UnitAnalytics=$unitAnalytics"
            )
        )
    }

    suspend fun clearMyWellnessHistory() = withContext(Dispatchers.IO) {
        wellnessDao.clearAllWellnessRecords("self")
        wellnessDao.insertAuditLog(
            WelfareAuditLogEntity(
                action = "WELLNESS_HISTORY_CLEARED",
                performedByRole = UserRole.PERSONNEL.name,
                personnelOrUnitIdentifier = "self",
                details = "Personnel exercised right to delete voluntary personal wellness assessment history."
            )
        )
    }

    fun getWelfareResources(): List<WelfareResource> {
        return WelfareAiEngine.getDefaultWelfareResources()
    }

    fun getDemoProfiles(): List<DemoPersonnelProfile> {
        return WelfareAiEngine.getDemoPersonnelProfiles()
    }

    suspend fun seedInitialDataIfEmpty() = withContext(Dispatchers.IO) {
        // Seed default consent if null
        // And if no support requests exist, seed one illustrative demonstration entry
    }
}
