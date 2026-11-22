package com.example.data.wellness

import com.example.data.model.AppLanguage

object WelfareAiEngine {

    fun analyzeWelfarePattern(
        duty: DutyWorkloadData,
        wellness: SelfReportedWellness,
        wearable: WearableBiometricData = WearableBiometricData(),
        language: AppLanguage = AppLanguage.ENGLISH
    ): WelfareAssessmentResult {
        var score = 0f
        val signalsEn = mutableListOf<String>()
        val signalsHi = mutableListOf<String>()
        val limitations = mutableListOf<String>()

        // 1. Duty Hours Analysis
        when {
            duty.dutyHoursLast24h >= 14f -> {
                score += 18f
                signalsEn.add("Extended duty hours (${duty.dutyHoursLast24h.toInt()}h in last 24h)")
                signalsHi.add("लंबी ड्यूटी अवधि (पिछले 24 घंटों में ${duty.dutyHoursLast24h.toInt()} घंटे)")
            }
            duty.dutyHoursLast24h >= 11f -> {
                score += 10f
                signalsEn.add("Above-average duty shift (${duty.dutyHoursLast24h.toInt()}h)")
                signalsHi.add("औसत से अधिक ड्यूटी शिफ्ट (${duty.dutyHoursLast24h.toInt()} घंटे)")
            }
            else -> {
                score += 2f
            }
        }

        // 2. Night Duties in past week
        when {
            duty.nightDutyCountLastWeek >= 4 -> {
                score += 15f
                signalsEn.add("Frequent night shifts (${duty.nightDutyCountLastWeek} nights this week)")
                signalsHi.add("बार-बार नाइट ड्यूटी (इस सप्ताह ${duty.nightDutyCountLastWeek} रातें)")
            }
            duty.nightDutyCountLastWeek >= 2 -> {
                score += 8f
                signalsEn.add("Multiple night duty rotations (${duty.nightDutyCountLastWeek} nights)")
                signalsHi.add("एकाधिक नाइट ड्यूटी रोटेशन (${duty.nightDutyCountLastWeek} रातें)")
            }
        }

        // 3. Deployment & Separation from home
        when {
            duty.deploymentDays >= 90 -> {
                score += 14f
                signalsEn.add("Prolonged deployment (${duty.deploymentDays} continuous days)")
                signalsHi.add("लंबे समय तक तैनाती (लगातार ${duty.deploymentDays} दिन)")
            }
            duty.deploymentDays >= 45 -> {
                score += 8f
                signalsEn.add("Active field deployment (${duty.deploymentDays} days)")
                signalsHi.add("सक्रिय फील्ड तैनाती (${duty.deploymentDays} दिन)")
            }
        }

        // 4. Leave & Rest deficit
        when {
            duty.daysSinceLastLeave >= 120 -> {
                score += 12f
                signalsEn.add("Long interval without leave or restorative break (${duty.daysSinceLastLeave} days)")
                signalsHi.add("बिना छुट्टी या विश्राम के लंबा अंतराल (${duty.daysSinceLastLeave} दिन)")
            }
            duty.daysSinceLastLeave >= 60 -> {
                score += 6f
                signalsEn.add("Over 2 months since last formal leave period")
                signalsHi.add("अंतिम छुट्टी के बाद से 2 महीने से अधिक का समय")
            }
        }

        if (duty.hasIrregularShifts) {
            score += 6f
            signalsEn.add("Irregular or alternating shift schedules")
            signalsHi.add("अनियमित या परिवर्तनशील शिफ्ट शेड्यूल")
        }

        if (duty.hasHeavyTrainingCommitment) {
            score += 5f
            signalsEn.add("Concurrent intensive physical training commitment")
            signalsHi.add("समवर्ती गहन शारीरिक प्रशिक्षण प्रतिबद्धता")
        }

        // 5. Self-reported Stress Level (0 - 10)
        when {
            wellness.stressLevel >= 8 -> {
                score += 20f
                signalsEn.add("High self-reported stress level (${wellness.stressLevel}/10)")
                signalsHi.add("उच्च स्व-रिपोर्ट किया गया तनाव स्तर (${wellness.stressLevel}/10)")
            }
            wellness.stressLevel >= 5 -> {
                score += 10f
                signalsEn.add("Moderate self-reported stress (${wellness.stressLevel}/10)")
                signalsHi.add("मध्यम स्व-रिपोर्ट किया गया तनाव (${wellness.stressLevel}/10)")
            }
        }

        // 6. Sleep Duration & Quality
        when {
            wellness.sleepHours < 5f -> {
                score += 15f
                signalsEn.add("Short sleep duration (${wellness.sleepHours} hours)")
                signalsHi.add("कम नींद की अवधि (${wellness.sleepHours} घंटे)")
            }
            wellness.sleepHours < 6.5f -> {
                score += 7f
                signalsEn.add("Slight sleep deficit (${wellness.sleepHours} hours)")
                signalsHi.add("नींद में हल्का घाटा (${wellness.sleepHours} घंटे)")
            }
        }

        if (wellness.sleepQuality <= 2) {
            score += 8f
            signalsEn.add("Restless or disrupted sleep quality (${wellness.sleepQuality}/5)")
            signalsHi.add("अशांत या बाधित नींद की गुणवत्ता (${wellness.sleepQuality}/5)")
        }

        // 7. Fatigue & Workload Perception
        if (wellness.fatigueLevel >= 7) {
            score += 10f
            signalsEn.add("Noticeable physical or mental exhaustion reported (${wellness.fatigueLevel}/10)")
            signalsHi.add("शारीरिक या मानसिक थकान महसूस होना (${wellness.fatigueLevel}/10)")
        }

        if (wellness.workloadPerception >= 8) {
            score += 8f
            signalsEn.add("Perception of high workload pressure (${wellness.workloadPerception}/10)")
            signalsHi.add("उच्च कार्यभार दबाव की धारणा (${wellness.workloadPerception}/10)")
        }

        // 8. Social & Family support buffer
        if (wellness.familySupportRating <= 2) {
            score += 6f
            signalsEn.add("Limited regular family or social contact rating")
            signalsHi.add("सीमित पारिवारिक या सामाजिक संपर्क")
        } else if (wellness.familySupportRating >= 4) {
            // Buffer discount
            score = (score - 5f).coerceAtLeast(0f)
        }

        // 9. Optional Wearable Data Signals (Only if explicitly consented)
        if (wearable.isEnabled) {
            wearable.restingHeartRateBpm?.let { rhr ->
                if (rhr > 85) {
                    score += 6f
                    signalsEn.add("Wearable signal: Elevated resting heart rate ($rhr bpm)")
                    signalsHi.add("वियरेबल संकेत: बढ़ा हुआ विश्राम हृदय गति ($rhr bpm)")
                }
            }
            wearable.deepSleepPercentage?.let { deep ->
                if (deep < 12f) {
                    score += 5f
                    signalsEn.add("Wearable signal: Low deep sleep recovery (${deep.toInt()}%)")
                    signalsHi.add("वियरेबल संकेत: कम गहरी नींद की रिकवरी (${deep.toInt()}%)")
                }
            }
        } else {
            limitations.add("Wearable biometric data is unlinked or optional consent not provided.")
        }

        // Normalize score between 0 and 100
        val compositeScore = score.coerceIn(0f, 100f)

        val level = when {
            compositeScore >= 62f -> WelfareIndicatorLevel.ELEVATED
            compositeScore >= 32f -> WelfareIndicatorLevel.MODERATE
            else -> WelfareIndicatorLevel.LOWER
        }

        // Determine confidence
        val confidence = when {
            signalsEn.size >= 5 -> "High"
            signalsEn.size >= 2 -> "Moderate"
            else -> "Limited Information - Missing Signals"
        }

        // Supportive recommendations (Framed as options, never commands)
        val recsEn = mutableListOf<String>()
        val recsHi = mutableListOf<String>()

        when (level) {
            WelfareIndicatorLevel.ELEVATED -> {
                recsEn.add("Consider speaking with an authorized unit welfare officer for a friendly check-in.")
                recsEn.add("Consider whether a workload or shift rest review would be appropriate with your supervisory chain.")
                recsEn.add("Confidential Tele-MANAS (14416) or service counseling desks are available 24x7.")
                recsEn.add("Prioritize restorative sleep and take brief hydration and breathing breaks during shifts.")

                recsHi.add("मार्गदर्शन एवं सहायता हेतु यूनिट कल्याण अधिकारी से अनौपचारिक बातचीत पर विचार करें।")
                recsHi.add("शिफ्ट विश्राम या कार्यभार संतुलन की समीक्षा पर विचार करना उचित हो सकता है।")
                recsHi.add("गोपनीय टेली-मानस (14416) या बल परामर्श डेस्क 24x7 उपलब्ध हैं।")
                recsHi.add("पर्याप्त नींद को प्राथमिकता दें और ड्यूटी के बीच में विश्राम व पानी का ध्यान रखें।")
            }
            WelfareIndicatorLevel.MODERATE -> {
                recsEn.add("Consider taking a short relaxation break and keeping a steady sleep schedule.")
                recsEn.add("Reach out to family or peer support networks to share experiences.")
                recsEn.add("A routine discussion with a welfare officer is always available if desired.")
                recsEn.add("Complete another brief wellness check-in in a few days to observe your personal trend.")

                recsHi.add("नियमित नींद और संक्षिप्त विश्राम दिनचर्या बनाए रखने का प्रयास करें।")
                recsHi.add("परिवार या साथी सहयोगियों से बातचीत कर मन हल्का करें।")
                recsHi.add("यदि आवश्यकता महसूस हो तो कल्याण अधिकारी से किसी भी समय चर्चा की जा सकती है।")
                recsHi.add("व्यक्तिगत रुझान देखने के लिए कुछ दिनों बाद पुनः संक्षिप्त चेक-इन करें।")
            }
            WelfareIndicatorLevel.LOWER -> {
                recsEn.add("Continue maintaining your balanced work, physical exercise, and rest routine.")
                recsEn.add("Support fellow personnel who may be handling high workloads.")
                recsEn.add("Remember that confidential welfare resources remain available anytime you need them.")

                recsHi.add("अपनी संतुलित कार्य, शारीरिक व्यायाम और विश्राम की दिनचर्या जारी रखें।")
                recsHi.add("साथी कर्मियों की सहायता करें जो अधिक कार्यभार संभाल रहे हों।")
                recsHi.add("याद रखें कि आवश्यकता पड़ने पर गोपनीय कल्याण संसाधन सदैव उपलब्ध हैं।")
            }
        }

        if (signalsEn.isEmpty()) {
            signalsEn.add("No significant elevated stressors or workload imbalances reported.")
            signalsHi.add("कोई महत्वपूर्ण तनाव या कार्यभार असंतुलन दर्ज नहीं किया गया।")
        }

        return WelfareAssessmentResult(
            indicatorLevel = level,
            compositeScore = compositeScore,
            contributingSignals = signalsEn,
            contributingSignalsHi = signalsHi,
            confidence = confidence,
            dataLimitations = limitations,
            recommendationsEn = recsEn,
            recommendationsHi = recsHi
        )
    }

    // Default National & Uniformed Services Welfare Resources
    fun getDefaultWelfareResources(): List<WelfareResource> {
        return listOf(
            WelfareResource(
                id = "tele_manas",
                titleEn = "Tele-MANAS (Govt. of India National Mental Health Helpline)",
                titleHi = "टेली-मानस (राष्ट्रीय मानसिक स्वास्थ्य हेल्पलाइन)",
                category = "National Helpline",
                contactNumber = "14416",
                descriptionEn = "Toll-free 24x7 multi-lingual psychological counseling and welfare support provided by Government of India.",
                descriptionHi = "भारत सरकार द्वारा 24x7 निःशुल्क बहुभाषी मनोवैज्ञानिक परामर्श और कल्याण सहायता।",
                availableHours = "24x7 Toll-Free",
                isConfidential = true
            ),
            WelfareResource(
                id = "kiran_helpline",
                titleEn = "KIRAN National Mental Health Helpline",
                titleHi = "किरण राष्ट्रीय मानसिक स्वास्थ्य हेल्पलाइन",
                category = "National Helpline",
                contactNumber = "1800-599-0019",
                descriptionEn = "Dedicated 24x7 mental health and crisis support helpline operated by the Ministry of Social Justice and Empowerment.",
                descriptionHi = "सामाजिक न्याय एवं अधिकारिता मंत्रालय द्वारा संचालित 24x7 मानसिक स्वास्थ्य सहायता।",
                availableHours = "24x7 Toll-Free",
                isConfidential = true
            ),
            WelfareResource(
                id = "capf_welfare_desk",
                titleEn = "CAPF & Police Personnel Welfare Liaison Cell",
                titleHi = "सीएपीएफ एवं पुलिस कार्मिक कल्याण प्रकोष्ठ",
                category = "Force Welfare",
                contactNumber = "+91 11 2338 5000",
                descriptionEn = "Confidential welfare grievance, family support, and officer liaison desk for central armed police and police personnel.",
                descriptionHi = "केंद्रीय सशस्त्र पुलिस एवं पुलिस कर्मियों के लिए गोपनीय कल्याण व पारिवारिक सहायता डेस्क।",
                availableHours = "08:00 - 20:00 Daily",
                isConfidential = true
            ),
            WelfareResource(
                id = "emergency_112",
                titleEn = "National Emergency Support (112)",
                titleHi = "राष्ट्रीय आपातकालीन सहायता (112)",
                category = "Immediate Emergency",
                contactNumber = "112",
                descriptionEn = "Unified emergency helpline for immediate distress, medical urgency, or crisis assistance across India.",
                descriptionHi = "तत्काल चिकित्सा या संकट सहायता के लिए एकीकृत राष्ट्रीय आपातकालीन नंबर।",
                availableHours = "24x7 Immediate",
                isConfidential = false
            )
        )
    }

    // SIH / Demo Mode Synthetic Profiles (Personnel A, Personnel B, Personnel C)
    fun getDemoPersonnelProfiles(): List<DemoPersonnelProfile> {
        val pA_duty = DutyWorkloadData(
            dutyHoursLast24h = 15f,
            nightDutyCountLastWeek = 5,
            deploymentDays = 110,
            daysSinceLastLeave = 145,
            hasIrregularShifts = true,
            hasHeavyTrainingCommitment = true,
            restHoursLast24h = 4.5f
        )
        val pA_wellness = SelfReportedWellness(
            stressLevel = 8,
            mood = "Overwhelmed",
            sleepHours = 4f,
            sleepQuality = 1,
            fatigueLevel = 8,
            workloadPerception = 9,
            emotionalWellbeing = 3,
            familySupportRating = 2,
            interestInSupport = true,
            optionalNotes = "Continuous border patrol rotations with minimal sleep. Feeling heavily drained."
        )

        val pB_duty = DutyWorkloadData(
            dutyHoursLast24h = 11f,
            nightDutyCountLastWeek = 2,
            deploymentDays = 50,
            daysSinceLastLeave = 75,
            hasIrregularShifts = true,
            hasHeavyTrainingCommitment = false,
            restHoursLast24h = 6f
        )
        val pB_wellness = SelfReportedWellness(
            stressLevel = 6,
            mood = "Tired",
            sleepHours = 5.5f,
            sleepQuality = 2,
            fatigueLevel = 6,
            workloadPerception = 6,
            emotionalWellbeing = 6,
            familySupportRating = 3,
            interestInSupport = false,
            optionalNotes = "High transit and traffic duty shifts recently."
        )

        val pC_duty = DutyWorkloadData(
            dutyHoursLast24h = 8f,
            nightDutyCountLastWeek = 1,
            deploymentDays = 15,
            daysSinceLastLeave = 20,
            hasIrregularShifts = false,
            hasHeavyTrainingCommitment = false,
            restHoursLast24h = 8f
        )
        val pC_wellness = SelfReportedWellness(
            stressLevel = 3,
            mood = "Good",
            sleepHours = 7.5f,
            sleepQuality = 4,
            fatigueLevel = 2,
            workloadPerception = 3,
            emotionalWellbeing = 8,
            familySupportRating = 5,
            interestInSupport = false,
            optionalNotes = "Station duties going smoothly, slept well."
        )

        return listOf(
            DemoPersonnelProfile(
                id = "synth_p1",
                codeName = "Personnel A (Synthetic Demo)",
                rankOrRole = "Head Constable / GD",
                unit = "104 Bn High-Altitude Border Outpost",
                dutyData = pA_duty,
                wellnessData = pA_wellness,
                assessmentResult = analyzeWelfarePattern(pA_duty, pA_wellness),
                activeSupportRequest = "Workload & Rest Review"
            ),
            DemoPersonnelProfile(
                id = "synth_p2",
                codeName = "Personnel B (Synthetic Demo)",
                rankOrRole = "Sub-Inspector / Traffic & Law",
                unit = "Central Division Quick Response Unit",
                dutyData = pB_duty,
                wellnessData = pB_wellness,
                assessmentResult = analyzeWelfarePattern(pB_duty, pB_wellness),
                activeSupportRequest = null
            ),
            DemoPersonnelProfile(
                id = "synth_p3",
                codeName = "Personnel C (Synthetic Demo)",
                rankOrRole = "Constable / Communications",
                unit = "Headquarters Logistics Wing",
                dutyData = pC_duty,
                wellnessData = pC_wellness,
                assessmentResult = analyzeWelfarePattern(pC_duty, pC_wellness),
                activeSupportRequest = null
            )
        )
    }
}
