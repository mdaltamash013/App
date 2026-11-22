package com.example.data.knowledge

import com.example.data.model.AppLanguage
import com.example.data.model.TriageLevel

data class SafetyCheckResult(
    val isEmergency: Boolean,
    val triageLevel: TriageLevel,
    val redFlagReason: String,
    val spokenWarning: String,
    val displayWarning: String,
    val immediateSteps: List<String>
)

object SafetyRuleEngine {

    // Deterministic emergency keyword matchers (English and Hindi)
    private val emergencyPatterns = listOf(
        // Chest & Cardiac
        Regex("(?i)(?:\\b(chest pain|heart attack|crushing pain|tightness in chest|left arm pain)\\b|(सीने में (?:तेज )?दर्द|छाती में दर्द|दिल का दौरा))"),
        // Respiratory Emergency
        Regex("(?i)(?:\\b(can'?t breathe|cannot breathe|severe shortness of breath|gasping|choking|blue lips)\\b|(सांस नहीं आ रही|सांस लेने में बहुत तकलीफ|सांस लेने में तकलीफ|दम घुट रहा|नीले होंठ))"),
        // Neurological & Consciousness
        Regex("(?i)(?:\\b(unconscious|passed out|fainted|blacked out|loss of consciousness|stroke|slurred speech|face drooping|sudden paralysis)\\b|(बेहोश|बेहोशी|मूर्छित|लकवा|मुंह टेढ़ा|बोलने में असमर्थ))"),
        // Seizures
        Regex("(?i)(?:\\b(seizure|convulsion|fits|epilepsy attack)\\b|(दौरा|मिरगी का दौरा|झटके))"),
        // Severe Bleeding & Trauma
        Regex("(?i)(?:\\b(heavy bleeding|severe bleeding|vomiting blood|coughing blood|blood in vomit|deep wound|head trauma|skull fracture)\\b|(खून बह रहा|खून की उल्टी|खून की खांसी|सिर में गंभीर चोट))"),
        // Anaphylaxis & Allergic
        Regex("(?i)(?:\\b(anaphylaxis|throat swelling|swollen tongue|can'?t swallow|severe allergic reaction)\\b|(गला सूज गया|जीभ सूज गई|सांस रुक रही))"),
        // Poisoning & Envenomation
        Regex("(?i)(?:\\b(snake bite|snakebite|poison|poisoning|consumed pesticide|chemical ingestion)\\b|(सांप ने काटा|जहर|कीटनाशक पी लिया))"),
        // High risk pediatric
        Regex("(?i)(?:\\b(baby not crying|infant unresponsive|sunken eyes child|stiff neck high fever)\\b|(बच्चा रो नहीं रहा|बच्चा बेसुध|गर्दन अकड़ना))")
    )

    private val moderateRiskPatterns = listOf(
        Regex("(?i)(?:\\b(fever for 3 days|high fever|persistent vomiting|severe stomach pain|blood in stool|pregnant fever|dehydration)\\b|(तेज बुखार|3 दिन से बुखार|लगातार उल्टी|पेट में तेज दर्द|दस्त में खून|गर्भावस्था में बुखार))")
    )

    fun evaluate(input: String, language: AppLanguage): SafetyCheckResult {
        val trimmed = input.trim()

        // 1. Check CRITICAL EMERGENCY RED FLAGS
        for (pattern in emergencyPatterns) {
            val match = pattern.find(trimmed)
            if (match != null) {
                val matchedText = match.value
                return if (language == AppLanguage.HINDI) {
                    SafetyCheckResult(
                        isEmergency = true,
                        triageLevel = TriageLevel.EMERGENCY,
                        redFlagReason = "आपातकालीन चेतावनी लक्षण: $matchedText",
                        spokenWarning = "सावधान! आपके लक्षण तत्काल चिकित्सा सहायता के संकेत दे रहे हैं। कृपया तुरंत नजदीकी अस्पताल या डॉक्टर के पास जाएं।",
                        displayWarning = "🚨 संभावित चिकित्सा आपातकाल (Emergency Alert)",
                        immediateSteps = listOf(
                            "तुरंत राष्ट्रीय एम्बुलेंस 108 या आपातकालीन नंबर 112 पर कॉल करें।",
                            "नजदीकी आपातकालीन अस्पताल या प्राथमिक स्वास्थ्य केंद्र (PHC/CHC) जाएं।",
                            "मरीज को शांत रखें और आराम की मुद्रा में लिटाएं।",
                            "घर के सदस्य या आपातकालीन संपर्क को सूचित करें।"
                        )
                    )
                } else {
                    SafetyCheckResult(
                        isEmergency = true,
                        triageLevel = TriageLevel.EMERGENCY,
                        redFlagReason = "Emergency warning sign detected: $matchedText",
                        spokenWarning = "Alert. Your symptoms may require urgent medical attention. Please seek immediate in-person medical care.",
                        displayWarning = "🚨 Possible Medical Emergency (Emergency Alert)",
                        immediateSteps = listOf(
                            "Call National Ambulance Service 108 or Emergency 112 immediately.",
                            "Go to the nearest 24x7 Emergency Hospital, CHC, or District Hospital.",
                            "Keep the patient calm and in a comfortable resting position.",
                            "Notify your emergency contact or local health worker immediately."
                        )
                    )
                }
            }
        }

        // 2. Check MODERATE RISK
        for (pattern in moderateRiskPatterns) {
            if (pattern.containsMatchIn(trimmed)) {
                return if (language == AppLanguage.HINDI) {
                    SafetyCheckResult(
                        isEmergency = false,
                        triageLevel = TriageLevel.MODERATE,
                        redFlagReason = "मध्यम जोखिम लक्षण पाए गए।",
                        spokenWarning = "आपके लक्षणों के लिए किसी योग्य डॉक्टर या स्वास्थ्य केंद्र से परामर्श लेना उचित होगा।",
                        displayWarning = "🟡 मध्यम जोखिम — डॉक्टर परामर्श की सलाह",
                        immediateSteps = listOf(
                            "नजदीकी प्राथमिक स्वास्थ्य केंद्र (PHC) या डॉक्टर से परामर्श लें।",
                            "तरल पदार्थ और ओआरएस लेते रहें।",
                            "यदि लक्षण बिगड़ते हैं तो तुरंत आपातकालीन सहायता लें।"
                        )
                    )
                } else {
                    SafetyCheckResult(
                        isEmergency = false,
                        triageLevel = TriageLevel.MODERATE,
                        redFlagReason = "Moderate clinical concern detected.",
                        spokenWarning = "It is recommended to consult a qualified healthcare professional or visit a local clinic.",
                        displayWarning = "🟡 Moderate Risk — Doctor Consultation Recommended",
                        immediateSteps = listOf(
                            "Consult a doctor at your nearest PHC, CHC, or clinic.",
                            "Maintain adequate hydration and rest.",
                            "Seek immediate care if symptoms worsen or red flags appear."
                        )
                    )
                }
            }
        }

        // 3. LOW RISK
        return if (language == AppLanguage.HINDI) {
            SafetyCheckResult(
                isEmergency = false,
                triageLevel = TriageLevel.LOW,
                redFlagReason = "सामान्य लक्षण (कम जोखिम)",
                spokenWarning = "आपके लक्षण सामान्य स्तर के प्रतीत होते हैं। सुरक्षित घरेलू देखभाल और निगरानी का पालन करें।",
                displayWarning = "🟢 कम जोखिम — सामान्य सहायक मार्गदर्शन",
                immediateSteps = listOf(
                    "पर्याप्त आराम करें और स्वच्छ पानी व तरल पदार्थ पिएं।",
                    "स्वच्छता का ध्यान रखें।",
                    "यदि लक्षण 2-3 दिन में ठीक न हों तो डॉक्टर को दिखाएं।"
                )
            )
        } else {
            SafetyCheckResult(
                isEmergency = false,
                triageLevel = TriageLevel.LOW,
                redFlagReason = "Mild or supportive care symptoms.",
                spokenWarning = "Your symptoms appear to be mild. Follow safe home supportive care and monitor closely.",
                displayWarning = "🟢 Low Risk — Safe Supportive Guidance",
                immediateSteps = listOf(
                    "Rest well and drink plenty of clean boiled or filtered water.",
                    "Eat light, easily digestible nutritious food.",
                    "Consult a doctor if symptoms persist past 2 to 3 days or worsen."
                )
            )
        }
    }
}
