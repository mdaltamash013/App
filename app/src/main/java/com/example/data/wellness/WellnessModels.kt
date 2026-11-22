package com.example.data.wellness

import com.example.data.model.AppLanguage

enum class WelfareIndicatorLevel(val labelEn: String, val labelHi: String) {
    LOWER("Lower Indicators", "कम संकेतक"),
    MODERATE("Moderate Indicators", "मध्यम संकेतक"),
    ELEVATED("Elevated Indicators", "उन्नत कल्याण संकेतक");

    val explanationEn: String
        get() = when (this) {
            LOWER -> "No significant elevated pattern detected from available welfare information. Continue maintaining healthy rest and routine."
            MODERATE -> "Some patterns suggest that additional attention, rest, or a follow-up assessment may be appropriate."
            ELEVATED -> "Multiple indicators suggest that timely welfare follow-up, rest consideration, or speaking with a welfare officer may be beneficial."
        }

    val explanationHi: String
        get() = when (this) {
            LOWER -> "उपलब्ध जानकारी में कोई गंभीर तनाव पैटर्न नहीं देखा गया है। स्वस्थ दिनचर्या और विश्राम बनाए रखें।"
            MODERATE -> "कुछ संकेत सुझाव देते हैं कि अतिरिक्त विश्राम, कार्यभार समीक्षा या अनुवर्ती मूल्यांकन उचित हो सकता है।"
            ELEVATED -> "कई संकेत सुझाव देते हैं कि समय पर कल्याण सहायता, विश्राम या कल्याण अधिकारी से चर्चा लाभकारी हो सकती है।"
        }
}

enum class SupportCategory(val titleEn: String, val titleHi: String) {
    COUNSELING("Confidential Counseling", "गोपनीय परामर्श"),
    WELFARE_OFFICER("Welfare Officer Discussion", "कल्याण अधिकारी से चर्चा"),
    MENTAL_HEALTH_PROFESSIONAL("Specialist Consultation", "विशेषज्ञ परामर्श"),
    WORKLOAD_REST_DISCUSSION("Workload & Rest Review", "कार्यभार एवं विश्राम समीक्षा"),
    FAMILY_RESOURCES("Family & Social Support", "पारिवारिक एवं सामाजिक सहायता"),
    OTHER("Other Assistance", "अन्य सहायता")
}

enum class RequestStatus(val labelEn: String, val labelHi: String) {
    NEW("New", "नया"),
    UNDER_REVIEW("Under Review", "समीक्षाधीन"),
    SUPPORT_OFFERED("Support Offered", "सहायता प्रस्तावित"),
    FOLLOW_UP_REQUIRED("Follow-up Required", "अनुवर्ती आवश्यक"),
    RESOLVED("Resolved", "समाधानित"),
    CLOSED("Closed", "पूर्ण")
}

enum class UserRole(val labelEn: String, val labelHi: String) {
    PERSONNEL("Personnel (Self)", "कार्मिक (स्वयं)"),
    WELFARE_OFFICER("Authorized Welfare Officer", "अधिकृत कल्याण अधिकारी"),
    COMMANDER("Authorized Commander", "अधिकृत कमांडर"),
    ADMINISTRATOR("System Administrator", "सिस्टम व्यवस्थापक")
}

data class DutyWorkloadData(
    val dutyHoursLast24h: Float = 8f,
    val nightDutyCountLastWeek: Int = 1,
    val deploymentDays: Int = 14,
    val daysSinceLastLeave: Int = 30,
    val hasIrregularShifts: Boolean = false,
    val hasHeavyTrainingCommitment: Boolean = false,
    val restHoursLast24h: Float = 7f
)

data class SelfReportedWellness(
    val stressLevel: Int = 3, // 0 to 10
    val mood: String = "Neutral", // "Calm", "Good", "Neutral", "Tired", "Overwhelmed"
    val sleepHours: Float = 7f,
    val sleepQuality: Int = 3, // 1 (Poor) to 5 (Restful)
    val fatigueLevel: Int = 3, // 0 to 10
    val workloadPerception: Int = 4, // 0 to 10
    val emotionalWellbeing: Int = 7, // 0 to 10
    val familySupportRating: Int = 4, // 1 to 5
    val interestInSupport: Boolean = false,
    val optionalNotes: String = ""
)

data class WearableBiometricData(
    val isEnabled: Boolean = false,
    val avgHeartRateBpm: Int? = null,
    val restingHeartRateBpm: Int? = null,
    val sleepDurationHours: Float? = null,
    val deepSleepPercentage: Float? = null,
    val dailySteps: Int? = null,
    val syncTimestamp: Long? = null
)

data class WelfareAssessmentResult(
    val indicatorLevel: WelfareIndicatorLevel,
    val compositeScore: Float, // 0 to 100
    val contributingSignals: List<String>,
    val contributingSignalsHi: List<String>,
    val confidence: String, // "High", "Moderate", "Limited Information"
    val dataLimitations: List<String>,
    val recommendationsEn: List<String>,
    val recommendationsHi: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

data class WelfareResource(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val category: String,
    val contactNumber: String,
    val descriptionEn: String,
    val descriptionHi: String,
    val availableHours: String = "24x7",
    val isConfidential: Boolean = true
)

data class DemoPersonnelProfile(
    val id: String,
    val codeName: String,
    val rankOrRole: String,
    val unit: String,
    val dutyData: DutyWorkloadData,
    val wellnessData: SelfReportedWellness,
    val assessmentResult: WelfareAssessmentResult,
    val activeSupportRequest: String? = null
)
