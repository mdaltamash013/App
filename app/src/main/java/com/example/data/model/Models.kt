package com.example.data.model

enum class TriageLevel {
    LOW,
    MODERATE,
    EMERGENCY
}

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    SPEAKING,
    ERROR
}

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी")
}

data class SymptomAssessment(
    val id: Long = 0,
    val mainComplaint: String = "",
    val symptoms: List<String> = emptyList(),
    val duration: String = "",
    val severity: String = "",
    val ageGroup: String = "",
    val riskLevel: TriageLevel = TriageLevel.LOW,
    val guidanceTitle: String = "",
    val guidancePoints: List<String> = emptyList(),
    val followUpQuestions: List<String> = emptyList(),
    val warningSigns: List<String> = emptyList(),
    val isEmergency: Boolean = false,
    val emergencyReason: String = "",
    val rawResponse: String = "",
    val isOnlineAiResponse: Boolean = false,
    val aiModelUsed: String? = null,
    val isFallbackUsed: Boolean = false,
    val fallbackReason: String? = null,
    val researchCitations: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val isEmergency: Boolean = false,
    val isOnlineAi: Boolean = false,
    val modelName: String? = null,
    val isFallback: Boolean = false,
    val fallbackReason: String? = null,
    val researchSources: List<String> = emptyList(),
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER,
    AI,
    SYSTEM
}

data class HealthcareFacility(
    val id: String,
    val name: String,
    val type: FacilityType,
    val distanceKm: Double,
    val address: String,
    val phone: String,
    val isOpen24x7: Boolean,
    val services: List<String>,
    val latitude: Double,
    val longitude: Double,
    val isEmergencyCapable: Boolean
)

enum class FacilityType(val label: String, val hindiLabel: String) {
    PHC("Primary Health Centre (PHC)", "प्राथमिक स्वास्थ्य केंद्र (PHC)"),
    CHC("Community Health Centre (CHC)", "सामुदायिक स्वास्थ्य केंद्र (CHC)"),
    DISTRICT_HOSPITAL("District Hospital", "जिला अस्पताल"),
    AYUSHMAN_MANDIR("Ayushman Arogya Mandir", "आयुष्मान आरोग्य मंदिर"),
    JAN_AUSHADHI("Jan Aushadhi Kendra (Pharmacy)", "जन औषधि केंद्र (फार्मेसी)"),
    EMERGENCY_TRAUMA("24x7 Trauma & Emergency", "24x7 आपातकालीन आघात केंद्र")
}

data class MedicineItem(
    val id: String,
    val name: String,
    val genericName: String,
    val category: String,
    val requiresPrescription: Boolean,
    val purpose: String,
    val precautions: List<String>,
    val sideEffects: List<String>,
    val verifiedAvailable: Boolean,
    val verifiedPharmacy: String = "Jan Aushadhi Kendra - Block Center",
    val estimatedDelivery: String = "Available for in-person pickup / Local courier where verified"
)

data class DoctorInfo(
    val id: String,
    val name: String,
    val specialty: String,
    val experienceYears: Int,
    val qualifications: String,
    val languages: List<String>,
    val isAvailableNow: Boolean,
    val consultationModes: List<String> = listOf("Voice", "Video", "Chat")
)

enum class NetworkStatus {
    ONLINE,
    WEAK,
    OFFLINE
}
