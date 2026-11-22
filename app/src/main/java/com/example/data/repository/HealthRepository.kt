package com.example.data.repository

import android.content.Context
import com.example.data.knowledge.ClinicalGuidance
import com.example.data.knowledge.DoctorDatabase
import com.example.data.knowledge.FacilityDatabase
import com.example.data.knowledge.MedicalKnowledgeBase
import com.example.data.knowledge.MedicineDatabase
import com.example.data.knowledge.SafetyRuleEngine
import com.example.data.local.AppDatabase
import com.example.data.local.AssessmentRecordEntity
import com.example.data.local.EmergencyContactEntity
import com.example.data.model.AppLanguage
import com.example.data.model.DoctorInfo
import com.example.data.model.FacilityType
import com.example.data.model.HealthcareFacility
import com.example.data.model.MedicineItem
import com.example.data.model.SymptomAssessment
import com.example.data.model.TriageLevel
import com.example.data.remote.GeminiService
import com.example.data.remote.HealthResearchService
import com.example.data.remote.ResearchResult
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray

class HealthRepository(context: Context) {

    private val database = AppDatabase.getDatabase(context)
    private val assessmentDao = database.assessmentDao()
    private val emergencyContactDao = database.emergencyContactDao()

    val savedAssessments: Flow<List<AssessmentRecordEntity>> = assessmentDao.getAllAssessments()
    val emergencyContacts: Flow<List<EmergencyContactEntity>> = emergencyContactDao.getAllContacts()
    val assessmentCount: Flow<Int> = assessmentDao.getAssessmentCount()

    suspend fun saveAssessment(assessment: SymptomAssessment, language: AppLanguage): Long {
        val entity = AssessmentRecordEntity(
            mainComplaint = assessment.mainComplaint,
            symptomsJson = JSONArray(assessment.symptoms).toString(),
            riskLevel = assessment.riskLevel.name,
            guidanceTitle = assessment.guidanceTitle,
            guidanceJson = JSONArray(assessment.guidancePoints).toString(),
            isEmergency = assessment.isEmergency,
            language = language.code,
            timestamp = System.currentTimeMillis()
        )
        return assessmentDao.insertAssessment(entity)
    }

    suspend fun deleteAssessment(id: Long) {
        assessmentDao.deleteAssessment(id)
    }

    suspend fun saveEmergencyContact(name: String, relation: String, phone: String, isPrimary: Boolean = false): Long {
        val contact = EmergencyContactEntity(
            name = name,
            relation = relation,
            phone = phone,
            isPrimary = isPrimary
        )
        return emergencyContactDao.insertContact(contact)
    }

    suspend fun deleteEmergencyContact(contact: EmergencyContactEntity) {
        emergencyContactDao.deleteContact(contact)
    }

    suspend fun evaluateSymptomInput(
        input: String,
        language: AppLanguage,
        isOnline: Boolean,
        conversationHistory: List<Pair<String, String>> = emptyList()
    ): SymptomAssessment {
        // 1. Mandatory Deterministic Safety Rule Check
        val safetyResult = SafetyRuleEngine.evaluate(input, language)
        if (safetyResult.isEmergency) {
            return SymptomAssessment(
                mainComplaint = input,
                symptoms = listOf(input),
                riskLevel = TriageLevel.EMERGENCY,
                guidanceTitle = safetyResult.displayWarning,
                guidancePoints = safetyResult.immediateSteps,
                followUpQuestions = emptyList(), // Stop questioning during emergency
                warningSigns = safetyResult.immediateSteps,
                isEmergency = true,
                emergencyReason = safetyResult.redFlagReason,
                rawResponse = safetyResult.spokenWarning
            )
        }

        // 2. Clinical Knowledge Base matching
        val clinicalGuidance = MedicalKnowledgeBase.findGuidance(input)

        // Contextual conversation tracking: check if previous turns contained symptoms
        val previousUserTurn = conversationHistory.reversed().firstOrNull { it.first == "user" }?.second
        val contextualGuidance = if (clinicalGuidance == null && previousUserTurn != null) {
            MedicalKnowledgeBase.findGuidance(previousUserTurn)
        } else null

        // 3. If online, trigger fresh API request to Gemini with online research grounding and model cascade
        val isHindi = language == AppLanguage.HINDI
        val isHinglish = isHinglishText(input) || (previousUserTurn != null && isHinglishText(previousUserTurn))
        val activeGuidance = clinicalGuidance ?: contextualGuidance

        if (isOnline) {
            val researchResult = if (HealthResearchService.requiresOnlineResearch(input)) {
                HealthResearchService.retrieveHealthResearch(input)
            } else {
                null
            }

            val researchContext = researchResult?.aggregatedContext
            val researchSources = researchResult?.sources ?: emptyList()

            val aiResult = GeminiService.generateHealthGuidance(
                userQuery = input,
                conversationHistory = conversationHistory,
                language = language,
                researchContext = researchContext,
                researchSources = researchSources
            )

            if (aiResult.success && !aiResult.text.isNullOrBlank()) {
                val trimmedResponse = aiResult.text.trim()

                if (activeGuidance != null) {
                    val pointsFromGemini = trimmedResponse.lines()
                        .map { it.trim().removePrefix("-").removePrefix("*").removePrefix("•").trim() }
                        .filter { it.isNotBlank() && it.length > 3 }
                    val points = if (pointsFromGemini.isNotEmpty()) {
                        pointsFromGemini.take(5)
                    } else {
                        if (isHindi) activeGuidance.supportiveCareHi else activeGuidance.supportiveCareEn
                    }

                    return SymptomAssessment(
                        mainComplaint = input,
                        symptoms = listOf(input),
                        riskLevel = activeGuidance.riskLevel,
                        guidanceTitle = if (isHindi) activeGuidance.titleHi else activeGuidance.titleEn,
                        guidancePoints = points,
                        followUpQuestions = if (isHindi) activeGuidance.followUpQuestionsHi else activeGuidance.followUpQuestionsEn,
                        warningSigns = if (isHindi) activeGuidance.redFlagsHi else activeGuidance.redFlagsEn,
                        isEmergency = false,
                        rawResponse = trimmedResponse,
                        isOnlineAiResponse = true,
                        aiModelUsed = aiResult.modelUsed,
                        researchCitations = researchSources.map { "${it.authority}: ${it.title}" }
                    )
                } else {
                    val paragraphs = trimmedResponse.split("\n\n")
                        .map { it.trim() }
                        .filter { it.isNotBlank() }
                    val points = if (paragraphs.isNotEmpty()) paragraphs else listOf(trimmedResponse)

                    return SymptomAssessment(
                        mainComplaint = input,
                        symptoms = listOf(input),
                        riskLevel = safetyResult.triageLevel,
                        guidanceTitle = if (isHindi) "AI उत्तर एवं मार्गदर्शन" else "AI Response",
                        guidancePoints = points,
                        followUpQuestions = emptyList(),
                        warningSigns = emptyList(),
                        isEmergency = false,
                        rawResponse = trimmedResponse,
                        isOnlineAiResponse = true,
                        aiModelUsed = aiResult.modelUsed,
                        researchCitations = researchSources.map { "${it.authority}: ${it.title}" }
                    )
                }
            } else {
                // Online request failed - transparent fallback to offline knowledge base
                val failureReason = aiResult.errorMessage ?: "Online service temporarily unavailable"
                val failureNotice = if (isHindi) {
                    "⚠️ ऑनलाइन AI सेवा फिलहाल अनुपलब्ध है ($failureReason)। मैं आपको उपलब्ध सत्यापित ऑफ़लाइन मार्गदर्शन प्रदान कर रहा हूँ:"
                } else {
                    "⚠️ Online AI service is currently unavailable ($failureReason). I'm giving you the available offline guidance:"
                }

                if (activeGuidance != null) {
                    val title = if (isHindi) activeGuidance.titleHi else activeGuidance.titleEn
                    val points = if (isHindi) activeGuidance.supportiveCareHi else activeGuidance.supportiveCareEn
                    val followUps = if (isHindi) activeGuidance.followUpQuestionsHi else activeGuidance.followUpQuestionsEn
                    val warnings = if (isHindi) activeGuidance.redFlagsHi else activeGuidance.redFlagsEn

                    val friendlySpoken = buildConversationalSpokenResponse(
                        input = input,
                        guidance = activeGuidance,
                        isHindi = isHindi,
                        isHinglish = isHinglish,
                        isContextualFollowup = (clinicalGuidance == null && contextualGuidance != null)
                    )

                    return SymptomAssessment(
                        mainComplaint = input,
                        symptoms = listOf(input),
                        riskLevel = activeGuidance.riskLevel,
                        guidanceTitle = "$title (Offline Fallback)",
                        guidancePoints = listOf(failureNotice) + points,
                        followUpQuestions = followUps,
                        warningSigns = warnings,
                        isEmergency = false,
                        rawResponse = "$failureNotice\n\n$friendlySpoken",
                        isOnlineAiResponse = false,
                        isFallbackUsed = true,
                        fallbackReason = failureReason
                    )
                } else {
                    val offlineMsg = if (isHinglish) {
                        "⚠️ Online service connect nahi ho pa rahi ($failureReason). Kripya thoda time baad try karein ya likhkar batayein."
                    } else if (isHindi) {
                        "⚠️ ऑनलाइन AI सेवा से संपर्क नहीं हो पाया ($failureReason)। कृपया थोड़ी देर बाद पुनः प्रयास करें या डॉक्टर से संपर्क करें।"
                    } else {
                        "⚠️ Online AI service is currently unavailable ($failureReason). Please try again shortly or consult a healthcare professional."
                    }
                    return SymptomAssessment(
                        mainComplaint = input,
                        symptoms = listOf(input),
                        riskLevel = TriageLevel.LOW,
                        guidanceTitle = if (isHindi) "ऑफ़लाइन मोड" else "Offline Mode",
                        guidancePoints = listOf(offlineMsg),
                        followUpQuestions = emptyList(),
                        warningSigns = emptyList(),
                        isEmergency = false,
                        rawResponse = offlineMsg,
                        isOnlineAiResponse = false,
                        isFallbackUsed = true,
                        fallbackReason = failureReason
                    )
                }
            }
        }

        // 4. Pure Offline Mode (device offline or force offline)
        if (activeGuidance != null) {
            val title = if (isHindi) activeGuidance.titleHi else activeGuidance.titleEn
            val points = if (isHindi) activeGuidance.supportiveCareHi else activeGuidance.supportiveCareEn
            val followUps = if (isHindi) activeGuidance.followUpQuestionsHi else activeGuidance.followUpQuestionsEn
            val warnings = if (isHindi) activeGuidance.redFlagsHi else activeGuidance.redFlagsEn

            val friendlySpoken = buildConversationalSpokenResponse(
                input = input,
                guidance = activeGuidance,
                isHindi = isHindi,
                isHinglish = isHinglish,
                isContextualFollowup = (clinicalGuidance == null && contextualGuidance != null)
            )

            return SymptomAssessment(
                mainComplaint = input,
                symptoms = listOf(input),
                riskLevel = activeGuidance.riskLevel,
                guidanceTitle = title,
                guidancePoints = points,
                followUpQuestions = followUps,
                warningSigns = warnings,
                isEmergency = false,
                rawResponse = friendlySpoken,
                isOnlineAiResponse = false
            )
        } else {
            val offlineMsg = when {
                isHinglish -> "Sorry, abhi device offline hai. Thoda time baad try kijiye ya likhkar batayein."
                isHindi -> "माफ़ कीजिए, डिवाइस अभी ऑफ़लाइन है। कृपया दोबारा प्रयास करें।"
                else -> "Device is currently offline. Please connect to the internet or try again."
            }
            return SymptomAssessment(
                mainComplaint = input,
                symptoms = listOf(input),
                riskLevel = TriageLevel.LOW,
                guidanceTitle = if (isHindi) "ऑफ़लाइन मोड" else "Offline Mode",
                guidancePoints = listOf(offlineMsg),
                followUpQuestions = emptyList(),
                warningSigns = emptyList(),
                isEmergency = false,
                rawResponse = offlineMsg,
                isOnlineAiResponse = false
            )
        }
    }

    private fun isHinglishText(text: String): Boolean {
        val hinglishTokens = setOf(
            "yaar", "bhai", "mujhe", "mera", "meri", "mere", "hai", "hain", "ho", "raha", "rahi", "rahe",
            "dard", "subah", "shaam", "raat", "kal", "aaj", "kya", "karu", "kaise", "thoda", "bohot",
            "bahut", "pet", "sar", "sir", "gala", "bukhar", "khansi", "dast", "ulti", "chakkar", "thakan",
            "kamzori", "lag", "nahi", "bataye", "samajh", "batao", "kripya", "theek", "dawa", "dawai"
        )
        val words = text.lowercase().split(Regex("[\\s,?.!]+")).filter { it.isNotBlank() }
        return words.any { it in hinglishTokens }
    }

    private fun buildConversationalSpokenResponse(
        input: String,
        guidance: ClinicalGuidance,
        isHindi: Boolean,
        isHinglish: Boolean,
        isContextualFollowup: Boolean
    ): String {
        val category = guidance.category.lowercase()

        if (isContextualFollowup) {
            val mention = when {
                category.contains("abdominal") -> if (isHindi) "पेट में दर्द" else "stomach pain"
                category.contains("headache") -> if (isHindi) "सिरदर्द" else "headache"
                category.contains("fatigue") -> if (isHindi) "थकान" else "tiredness"
                category.contains("fever") -> if (isHindi) "बुखार" else "fever"
                category.contains("respiratory") -> if (isHindi) "खांसी" else "cough"
                else -> if (isHindi) "तकलीफ" else "discomfort"
            }
            return when {
                isHinglish -> "Got it, samajh gaya. $mention hai — pain constant hai ya beech-beech mein ho raha hai? Thoda gunguna paani pijiye aur shaanti se aaram kijiye."
                isHindi -> "ठीक है, मैं समझ गया। $mention है — क्या दर्द लगातार बना हुआ है या बीच-बीच में कम हो रहा है? थोड़ा आराम करें और हल्का गुनगुना पानी पिएं।"
                else -> "Got it, thanks for explaining that. Since the $mention started, is the discomfort constant or coming in waves? Make sure to drink some warm water and rest."
            }
        }

        if (isHinglish) {
            return when {
                category.contains("headache") ->
                    "Okay, samajh gaya. Headache subah se hai — pain constant hai ya beech-beech mein kam ho raha hai? Thoda paani pijiye aur shaant kamre mein aaram kijiye."
                category.contains("fatigue") ->
                    "Yeah, that sounds uncomfortable. Thakan aaj se hi hai ya pichhle kuch dino se ho rahi hai? Thoda aaram kijiye aur paani pijiye."
                category.contains("abdominal") ->
                    "Okay, I understand. Pet me dard kab se shuru hua? Thoda gunguna paani pijiye aur halka khana khaiye."
                category.contains("fever") ->
                    "Okay, I understand. Bukhar kitne din se hai? Khub paani ya ORS pijiye aur aaram kijiye."
                category.contains("respiratory") ->
                    "Got it. Khansi ya gala kharab kab se hai? Gunguna paani pijiye aur namak ke paani ke garare kijiye."
                category.contains("gastrointestinal") ->
                    "Okay, samajh gaya. Sabse pehle ORS ka ghol ghoont-ghoont karke pijiye taaki kamzori na aaye."
                else ->
                    "Okay, samajh gaya. Yeh waqai uncomfortable ho sakta hai. Thoda aaram kijiye aur zaroorat ho to doctor ko dikhaiye."
            }
        }

        if (isHindi) {
            return when {
                category.contains("headache") ->
                    "ठीक है, मैं समझ गया। क्या सिरदर्द लगातार बना हुआ है या बीच-बीच में कम हो रहा है? शांत कमरे में आराम करें और पानी पिएं।"
                category.contains("fatigue") ->
                    "मैं समझ सकता हूँ, यह वाकई तकलीफदेह है। क्या आपको थकान आज से ही महसूस हो रही है, या पिछले कुछ दिनों से बनी हुई है? थोड़ा आराम करें और पानी पिएं।"
                category.contains("abdominal") ->
                    "ठीक है, मैं समझ सकता हूँ। पेट में दर्द कब से शुरू हुआ? हल्का गुनगुना पानी पिएं और सुपाच्य भोजन लें।"
                category.contains("fever") ->
                    "मैं समझ सकता हूँ, यह वाकई तकलीफदेह है। आपको बुखार कितने दिनों से है? हवादार कमरे में आराम करें और खूब पानी पिएं।"
                category.contains("respiratory") ->
                    "मैं समझ गया। क्या खांसी सूखी है या बलगम आ रहा है? दिनभर हल्का गुनगुना पानी पिएं और गरारे करें।"
                category.contains("gastrointestinal") ->
                    "मैं समझ सकता हूँ, इससे कमजोरी आ सकती है। तुरंत ओआरएस (ORS) का घोल घूंट-घूंट करके पीना शुरू करें।"
                else ->
                    "मैं समझ सकता हूँ, यह वाकई तकलीफदेह है। कृपया थोड़ा आराम करें और सुरक्षित रहने के लिए नजदीकी डॉक्टर से जांच कराएं।"
            }
        }

        // English default
        return when {
            category.contains("headache") ->
                "Okay, I understand. That sounds uncomfortable. Tell me a little more — is the headache constant or coming and going? Make sure to drink some water and rest in a quiet room."
            category.contains("fatigue") ->
                "Yeah, that sounds uncomfortable. Tell me a little more — have you been feeling tired since today, or has it been going on for a few days? Make sure to stay well hydrated and take a restful break."
            category.contains("abdominal") ->
                "Okay, I understand. When did the stomach discomfort begin? Resting and sipping warm water can help soothe your stomach."
            category.contains("fever") ->
                "I hear you, having a fever can be really exhausting. How many days have you had the fever? Drink plenty of fluids like clean water or coconut water and rest."
            category.contains("respiratory") ->
                "Got it. That sounds uncomfortable. Is the cough dry or are you having phlegm? Warm water and honey or salt water gargles can soothe your throat."
            category.contains("gastrointestinal") ->
                "I understand how exhausting that can be. The most important step right now is to sip Oral Rehydration Salts (ORS) frequently to stay hydrated."
            else ->
                "I hear you, and that sounds really uncomfortable. Please make sure to get plenty of rest and consult a doctor if symptoms persist."
        }
    }

    fun getNearbyFacilities(type: FacilityType? = null): List<HealthcareFacility> {
        return FacilityDatabase.getSortedFacilities(type)
    }

    fun searchMedicines(query: String): List<MedicineItem> {
        return MedicineDatabase.search(query)
    }

    fun getDoctors(): List<DoctorInfo> {
        return DoctorDatabase.doctors
    }
}
