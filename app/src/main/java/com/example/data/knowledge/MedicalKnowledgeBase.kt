package com.example.data.knowledge

import com.example.data.model.AppLanguage
import com.example.data.model.TriageLevel

data class ClinicalGuidance(
    val category: String,
    val titleEn: String,
    val titleHi: String,
    val triggerKeywords: List<String>,
    val riskLevel: TriageLevel,
    val followUpQuestionsEn: List<String>,
    val followUpQuestionsHi: List<String>,
    val supportiveCareEn: List<String>,
    val supportiveCareHi: List<String>,
    val redFlagsEn: List<String>,
    val redFlagsHi: List<String>
)

object MedicalKnowledgeBase {

    val guidances = listOf(
        ClinicalGuidance(
            category = "Fever & Viral Illness",
            titleEn = "Fever / Pyrexia Supportive Care",
            titleHi = "बुखार की सुरक्षित देखभाल",
            triggerKeywords = listOf("fever", "bukhar", "बुखार", "temperature", "chills", "shivering", "कपकपी"),
            riskLevel = TriageLevel.LOW,
            followUpQuestionsEn = listOf(
                "How many days have you had the fever?",
                "Are you experiencing shivering, severe body aches, rash, or vomiting?",
                "Do you have severe headache or stiffness in your neck?"
            ),
            followUpQuestionsHi = listOf(
                "आपको बुखार कितने दिनों से है?",
                "क्या आपको कपकपी, शरीर में तेज दर्द, उल्टी या शरीर पर दाने हैं?",
                "क्या सिर में बहुत तेज दर्द या गर्दन में अकड़न है?"
            ),
            supportiveCareEn = listOf(
                "Rest in a cool, ventilated room.",
                "Drink plenty of fluids: clean boiled water, coconut water, thin dal water, or lemon water.",
                "Apply lukewarm water sponge/cloth on forehead to reduce body heat. Do NOT use ice water.",
                "Wear light, comfortable cotton clothing.",
                "Consult a PHC/CHC doctor for a simple blood smear or rapid test (for malaria/dengue/typhoid) if fever lasts more than 48 hours."
            ),
            supportiveCareHi = listOf(
                "हवादार और शांत कमरे में आराम करें।",
                "खूब तरल पदार्थ पिएं: उबला हुआ पानी, नारियल पानी, पतली दाल का पानी या नींबू पानी।",
                "माथे पर सामान्य या हल्के गुनगुने पानी की पट्टी रखें। बर्फ के पानी का इस्तेमाल न करें।",
                "हल्के सूती कपड़े पहनें।",
                "यदि बुखार 2 दिन से अधिक रहे, तो तुरंत नजदीकी स्वास्थ्य केंद्र (PHC) जाकर खून की जांच कराएं।"
            ),
            redFlagsEn = listOf(
                "High fever exceeding 103°F (39.4°C) not coming down",
                "Severe neck stiffness, extreme confusion, or drowsiness",
                "Red/purple spots or bleeding from gums/nose"
            ),
            redFlagsHi = listOf(
                "103°F से अधिक तेज बुखार जो कम न हो रहा हो",
                "गर्दन में अत्यधिक अकड़न या मरीज का बेहोश/सुस्त होना",
                "शरीर पर लाल-बैंगनी चकत्ते या मसूड़ों/नाक से खून आना"
            )
        ),

        ClinicalGuidance(
            category = "Respiratory & Cold",
            titleEn = "Cough, Cold & Mild Respiratory Guidance",
            titleHi = "खांसी और जुकाम की देखभाल",
            triggerKeywords = listOf("cough", "cold", "khansi", "खांसी", "जुकाम", "runny nose", "sore throat", "gala kharab", "गला दर्द"),
            riskLevel = TriageLevel.LOW,
            followUpQuestionsEn = listOf(
                "Is the cough dry or producing yellow/green phlegm?",
                "How long has this cough been present?",
                "Are you having any difficulty catching your breath or chest tightness?"
            ),
            followUpQuestionsHi = listOf(
                "क्या खांसी सूखी है या बलगम आ रहा है?",
                "यह खांसी कितने दिनों से है?",
                "क्या सांस फूल रही है या सीने में जकड़न महसूस हो रही है?"
            ),
            supportiveCareEn = listOf(
                "Drink warm water throughout the day.",
                "Gargle with warm salt water 2-3 times daily to soothe throat irritation.",
                "Steam inhalation with plain warm water for 5-10 minutes can ease nasal congestion.",
                "Honey with warm water or ginger tulsi tea can provide natural throat comfort.",
                "Avoid cold drinks, smoking, and smoky chulhas (firewood smoke)."
            ),
            supportiveCareHi = listOf(
                "दिनभर हल्का गुनगुना पानी पिएं।",
                "हल्के गर्म नमक के पानी से दिन में 2-3 बार गरारे करें।",
                "नाक बंद होने पर सादे गर्म पानी की भाप लें।",
                "तुलसी और अदरक की चाय या शहद गले को राहत दे सकता है।",
                "ठंडे पेय पदार्थ, धूम्रपान और चूल्हे के धुएं से बचें।"
            ),
            redFlagsEn = listOf(
                "Difficulty breathing or rapid shallow breathing",
                "Cough lasting more than 2 weeks (requires TB sputum test at PHC)",
                "Coughing up blood or rust-colored sputum"
            ),
            redFlagsHi = listOf(
                "सांस लेने में कठिनाई या सांस का तेज चलना",
                "2 सप्ताह से अधिक समय से लगातार खांसी (टीबी जांच हेतु PHC जाएं)",
                "खांसी में खून आना"
            )
        ),

        ClinicalGuidance(
            category = "Gastrointestinal & Dehydration",
            titleEn = "Diarrhea & Dehydration Management (ORS Focus)",
            titleHi = "दस्त और निर्जलीकरण (डिहाइड्रेशन) से बचाव",
            triggerKeywords = listOf("diarrhea", "dast", "दस्त", "loose motion", "pet kharab", "vomiting", "ulti", "उल्टी"),
            riskLevel = TriageLevel.MODERATE,
            followUpQuestionsEn = listOf(
                "How many loose stools have occurred in the last 24 hours?",
                "Is there any blood or mucus in the stool?",
                "Is the person passing urine normally, or is the mouth very dry?"
            ),
            followUpQuestionsHi = listOf(
                "पिछले 24 घंटों में कितनी बार पतले दस्त हुए हैं?",
                "क्या दस्त में खून या आंव आ रहा है?",
                "क्या पेशाब सामान्य रूप से आ रहा है या मुंह बहुत सूख रहा है?"
            ),
            supportiveCareEn = listOf(
                "CRITICAL: Start Oral Rehydration Salts (ORS) solution immediately after every loose stool.",
                "ORS Preparation: Mix 1 whole packet of standard ORS in 1 liter of clean drinking water. Drink in small frequent sips.",
                "Home alternative: 1 liter boiled water + 6 teaspoons sugar + half teaspoon salt.",
                "Eat soft, bland foods: khichdi, mashed banana, rice water (maand), and curd.",
                "Wash hands thoroughly with soap before eating and after using the toilet."
            ),
            supportiveCareHi = listOf(
                "अत्यंत आवश्यक: हर दस्त के बाद तुरंत ओआरएस (ORS) का घोल पीना शुरू करें।",
                "ओआरएस बनाने की विधि: 1 पैकेट ओआरएस 1 लीटर साफ पीने के पानी में घोलें और घूंट-घूंट करके पिएं।",
                "घरेलू विकल्प: 1 लीटर पानी में 6 चम्मच चीनी और आधा चम्मच नमक मिलाएं।",
                "हल्का भोजन लें: मूंग दाल की खिचड़ी, केला, चावल का मांड और ताजा दही।",
                "शौच के बाद और खाना खाने से पहले साबुन से अच्छी तरह हाथ धोएं।"
            ),
            redFlagsEn = listOf(
                "Inability to keep liquids down or constant vomiting",
                "Sunken eyes, extreme thirst, no urine for over 6-8 hours",
                "Blood in the stool or high fever with diarrhea"
            ),
            redFlagsHi = listOf(
                "बिल्कुल भी पानी न पच पाना या लगातार उल्टी होना",
                "आंखें अंदर धंसना, अत्यधिक प्यास, 6-8 घंटे से पेशाब न होना",
                "दस्त में खून आना या बहुत तेज बुखार"
            )
        ),

        ClinicalGuidance(
            category = "Abdominal & Gastric",
            titleEn = "Mild Abdominal Discomfort & Acidity",
            titleHi = "पेट दर्द और गैस/एसिडिटी का मार्गदर्शन",
            triggerKeywords = listOf("stomach", "stomach pain", "stomach hurt", "stomach ache", "belly", "tummy", "pet dard", "pet me dard", "पेट दर्द", "gas", "acidity", "indigestion", "अपच", "bloating"),
            riskLevel = TriageLevel.LOW,
            followUpQuestionsEn = listOf(
                "Where exactly is the pain (upper belly, lower belly, right side)?",
                "Is it a burning sensation or cramping?",
                "Did it start suddenly, or is it related to recent meals?"
            ),
            followUpQuestionsHi = listOf(
                "दर्द पेट के किस हिस्से में है (ऊपर, नीचे या दाईं तरफ)?",
                "क्या पेट में जलन हो रही है या मरोड़ उठ रही है?",
                "क्या यह अचानक शुरू हुआ या भारी भोजन के बाद हुआ?"
            ),
            supportiveCareEn = listOf(
                "Eat small, frequent, light meals instead of heavy, spicy, or fried foods.",
                "Sip warm water or mint/ajwain (carom seed) infused water.",
                "Avoid lying flat immediately after eating.",
                "Avoid alcohol, tobacco, and excessively sour foods.",
                "Visit a health centre if pain persists more than 24 hours."
            ),
            supportiveCareHi = listOf(
                "एक साथ ज्यादा खाने के बजाय हल्का और सुपाच्य भोजन लें। तला-भुना न खाएं।",
                "हल्का गुनगुना पानी या अजवाइन का उबला पानी पिएं।",
                "खाना खाते ही सीधे बिस्तर पर न लेटें।",
                "बीड़ी, तंबाकू और अत्यधिक तीखे भोजन से परहेज करें।",
                "यदि दर्द 24 घंटे से अधिक रहे तो स्वास्थ्य केंद्र जाएं।"
            ),
            redFlagsEn = listOf(
                "Severe sudden pain on the lower right side of the belly (possible appendicitis)",
                "Rigid, hard belly that cannot be touched",
                "Vomiting green bile or blood"
            ),
            redFlagsHi = listOf(
                "पेट के निचले दाएं हिस्से में अचानक तेज असहनीय दर्द (अपेंडिक्स का खतरा)",
                "पेट का पत्थर जैसा कड़ा होना और छूने पर तेज दर्द",
                "हरे रंग की या खून की उल्टी होना"
            )
        ),

        ClinicalGuidance(
            category = "Skin & Minor Infections",
            titleEn = "Skin Rash, Scabies & Itching Support",
            titleHi = "त्वचा के दाने, खुजली और संक्रमण",
            triggerKeywords = listOf("itching", "khujli", "खुजली", "rash", "skin infection", "फोड़े", "boil", "daane", "दाने"),
            riskLevel = TriageLevel.LOW,
            followUpQuestionsEn = listOf(
                "Is the itching worse at night (suggestive of scabies)?",
                "Are other family members also experiencing itching?",
                "Is there any swelling, oozing pus, or warmth around the skin?"
            ),
            followUpQuestionsHi = listOf(
                "क्या खुजली रात के समय अधिक बढ़ जाती है?",
                "क्या परिवार के अन्य सदस्यों को भी खुजली की समस्या है?",
                "क्या दानों में मवाद (पस) आ रहा है या त्वचा गर्म और सूजी हुई है?"
            ),
            supportiveCareEn = listOf(
                "Bathe daily with clean water and mild soap.",
                "Wash and dry all clothes and bedsheets under direct sunlight.",
                "Keep fingernails short and clean to prevent secondary bacterial infection from scratching.",
                "Apply calamine lotion or cool clean compresses to soothe irritation.",
                "Do NOT apply unverified chemicals or dangerous battery water/kerosene to skin."
            ),
            supportiveCareHi = listOf(
                "साफ पानी और हल्के साबुन से रोजाना स्नान करें।",
                "कपड़े और चादरें धोकर तेज धूप में सुखाएं।",
                "नाखून छोटे और साफ रखें ताकि खुजलाने से घाव न बने।",
                "राहत के लिए कैलामाइन लोशन या ठंडे पानी की पट्टी लगाएं।",
                "त्वचा पर मिट्टी का तेल या हानिकारक रसायन कभी न लगाएं।"
            ),
            redFlagsEn = listOf(
                "Rapidly spreading red, hot swelling with fever (cellulitis)",
                "Blisters covering large areas of the body"
            ),
            redFlagsHi = listOf(
                "तेजी से फैलने वाली लालिमा, सूजन और साथ में बुखार",
                "शरीर के बड़े हिस्से पर बड़े छाले बनना"
            )
        ),

        ClinicalGuidance(
            category = "Headache & Migraine",
            titleEn = "Headache & Head Pain Supportive Care",
            titleHi = "सिरदर्द और माइग्रेन की सुरक्षित देखभाल",
            triggerKeywords = listOf("headache", "head pain", "sir dard", "sar dard", "सिरदर्द", "सिर दर्द", "माथा दर्द", "migraine"),
            riskLevel = TriageLevel.LOW,
            followUpQuestionsEn = listOf(
                "Is the pain constant, or does it come and go?",
                "Did it start today, or have you had it for a few days?",
                "Do you also have nausea, sensitivity to bright light, or blurred vision?"
            ),
            followUpQuestionsHi = listOf(
                "क्या सिरदर्द लगातार बना हुआ है या बीच-बीच में कम हो रहा है?",
                "क्या यह आज सुबह से शुरू हुआ है या कुछ दिनों से है?",
                "क्या उल्टी जैसा लगना या तेज रोशनी से परेशानी हो रही है?"
            ),
            supportiveCareEn = listOf(
                "Rest in a quiet, dimly lit, well-ventilated room.",
                "Drink a tall glass of water; mild dehydration is a frequent headache trigger.",
                "Apply a cold or warm clean compress across forehead or temples.",
                "Avoid screens, loud noises, and excessive caffeine.",
                "If headache is accompanied by sudden severe weakness, confusion, or stiff neck, seek medical attention immediately."
            ),
            supportiveCareHi = listOf(
                "शांत और हल्की रोशनी वाले हवादार कमरे में आराम करें।",
                "एक-दो गिलास साफ पानी पिएं, कई बार पानी की कमी से सिरदर्द होता है।",
                "माथे पर हल्की ठंडी या गुनगुनी पट्टी रखें।",
                "मोबाइल स्क्रीन और तेज आवाजों से कुछ देर दूर रहें।",
                "यदि सिरदर्द के साथ गर्दन में अकड़न या चक्कर आए तो तुरंत डॉक्टर से मिलें।"
            ),
            redFlagsEn = listOf(
                "Sudden worst headache of your life (thunderclap headache)",
                "Headache accompanied by stiff neck, high fever, or confusion",
                "Weakness or numbness on one side of the face or body"
            ),
            redFlagsHi = listOf(
                "अचानक बहुत तेज असहनीय सिरदर्द",
                "सिरदर्द के साथ तेज बुखार, गर्दन अकड़ना या बेहोशी",
                "चेहरे या शरीर के एक तरफ कमजोरी या सुन्नपन"
            )
        ),

        ClinicalGuidance(
            category = "Fatigue & Weakness",
            titleEn = "Tiredness, Exhaustion & General Weakness",
            titleHi = "थकान और सामान्य कमजोरी का मार्गदर्शन",
            triggerKeywords = listOf("tired", "fatigue", "exhausted", "weakness", "thakan", "thaka", "kamzori", "कमजोरी", "थकान", "सुस्ती", "low energy"),
            riskLevel = TriageLevel.LOW,
            followUpQuestionsEn = listOf(
                "Have you been feeling tired since today, or has it been going on for a few days?",
                "Have you been sleeping well at night and eating proper meals?",
                "Are you also experiencing fever, dizziness, or shortness of breath?"
            ),
            followUpQuestionsHi = listOf(
                "क्या आपको थकान आज से ही महसूस हो रही है, या पिछले कुछ दिनों से बनी हुई है?",
                "क्या रात में नींद ठीक से आ रही है और खान-पान सामान्य है?",
                "क्या साथ में बुखार, चक्कर आना या सांस फूलने जैसी समस्या भी है?"
            ),
            supportiveCareEn = listOf(
                "Ensure at least 7 to 8 hours of restful sleep in a comfortable space.",
                "Drink plenty of fluids: clean water, coconut water, or fresh fruit juice.",
                "Eat balanced, nourishing meals rich in iron and protein: dal, green leafy vegetables, jaggery (gur), and fruits.",
                "Take short restful breaks instead of overexerting yourself.",
                "Consult a PHC doctor to check for anemia or vitamin deficiencies if fatigue persists for more than a week."
            ),
            supportiveCareHi = listOf(
                "कम से कम 7-8 घंटे की पर्याप्त और सुकून भरी नींद लें।",
                "पर्याप्त तरल पदार्थ पिएं: पानी, नारियल पानी या नींबू पानी।",
                "पौष्टिक भोजन करें: हरी पत्तेदार सब्जियां, दालें, गुड़ और मौसमी फल।",
                "काम के बीच-बीच में थोड़ा आराम करें और खुद को ज्यादा न थकाएं।",
                "यदि थकान एक सप्ताह से अधिक बनी रहे, तो खून की कमी (एनीमिया) जांच के लिए डॉक्टर से संपर्क करें।"
            ),
            redFlagsEn = listOf(
                "Extreme weakness with chest tightness, shortness of breath, or fainting",
                "Pale tongue, nails, or rapid heart rate with fatigue (severe anemia)"
            ),
            redFlagsHi = listOf(
                "थकान के साथ सीने में भारीपन, सांस फूलना या बेहोश होना",
                "नाखून और जीभ का बहुत सफेद होना या दिल की धड़कन तेज होना"
            )
        )
    )

    fun findGuidance(query: String): ClinicalGuidance? {
        val lower = query.lowercase()
        return guidances.firstOrNull { guidance ->
            guidance.triggerKeywords.any { kw -> lower.contains(kw.lowercase()) }
        }
    }
}
