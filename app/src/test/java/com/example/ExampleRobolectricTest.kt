package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.knowledge.DoctorDatabase
import com.example.data.knowledge.FacilityDatabase
import com.example.data.knowledge.MedicineDatabase
import com.example.data.knowledge.SafetyRuleEngine
import com.example.data.model.AppLanguage
import com.example.data.model.FacilityType
import com.example.data.model.TriageLevel
import com.example.data.repository.HealthRepository
import com.example.data.wellness.DutyWorkloadData
import com.example.data.wellness.RequestStatus
import com.example.data.wellness.SelfReportedWellness
import com.example.data.wellness.SupportCategory
import com.example.data.wellness.UserRole
import com.example.data.wellness.WelfareAiEngine
import com.example.data.wellness.WelfareIndicatorLevel
import com.example.data.wellness.WellnessRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("LifeCare 24/7", appName)
    }

    @Test
    fun `safety rule engine flags emergency for chest pain`() {
        val assessment = SafetyRuleEngine.evaluate(
            input = "I have sudden severe chest pain and left arm radiating pressure",
            language = AppLanguage.ENGLISH
        )
        assertEquals(TriageLevel.EMERGENCY, assessment.triageLevel)
        assertTrue(assessment.isEmergency)
        assertTrue(assessment.immediateSteps.isNotEmpty())
    }

    @Test
    fun `safety rule engine flags emergency for hindi chest pain`() {
        val assessment = SafetyRuleEngine.evaluate(
            input = "सीने में तेज दर्द और सांस लेने में बहुत तकलीफ हो रही है",
            language = AppLanguage.HINDI
        )
        assertEquals(TriageLevel.EMERGENCY, assessment.triageLevel)
        assertTrue(assessment.isEmergency)
    }

    @Test
    fun `safety rule engine evaluates moderate risk`() {
        val assessment = SafetyRuleEngine.evaluate(
            input = "I have high fever for 3 days and vomiting",
            language = AppLanguage.ENGLISH
        )
        assertEquals(TriageLevel.MODERATE, assessment.triageLevel)
    }

    @Test
    fun `offline medicine database has items with verified availability`() {
        val medicines = MedicineDatabase.medicines
        assertTrue(medicines.isNotEmpty())
        val orspack = medicines.find { it.name.contains("ORS", ignoreCase = true) }
        assertNotNull(orspack)
        assertTrue(orspack!!.verifiedAvailable)
    }

    @Test
    fun `offline facility database has emergency capable PHC and CHC centers`() {
        val facilities = FacilityDatabase.facilities
        assertTrue(facilities.isNotEmpty())
        val emergencyCenters = facilities.filter { it.isEmergencyCapable }
        assertTrue(emergencyCenters.isNotEmpty())
    }

    @Test
    fun `offline doctor database provides telemedicine specialists`() {
        val doctors = DoctorDatabase.doctors
        assertTrue(doctors.isNotEmpty())
        assertTrue(doctors.any { it.isAvailableNow })
    }

    @Test
    fun `test fresh distinct Gemini responses for distinct questions`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = HealthRepository(context)

        val resPython = repo.evaluateSymptomInput("What is Python?", AppLanguage.ENGLISH, isOnline = true)
        val resJava = repo.evaluateSymptomInput("What is Java?", AppLanguage.ENGLISH, isOnline = true)
        val resMath = repo.evaluateSymptomInput("2+2?", AppLanguage.ENGLISH, isOnline = true)

        assertNotNull(resPython.rawResponse)
        assertNotNull(resJava.rawResponse)
        assertNotNull(resMath.rawResponse)

        assertTrue(resPython.rawResponse.isNotBlank())
        assertTrue(resJava.rawResponse.isNotBlank())
        assertTrue(resMath.rawResponse.isNotBlank())

        // If online Gemini response is received (when API key is configured and not offline fallback)
        val isOfflineFallback = resPython.isFallbackUsed || !resPython.isOnlineAiResponse ||
                resPython.rawResponse.contains("unavailable", ignoreCase = true) ||
                resPython.rawResponse.contains("trouble connecting", ignoreCase = true) ||
                resPython.guidanceTitle.contains("Offline", ignoreCase = true)
        if (!isOfflineFallback) {
            // Ensure responses are completely distinct and not identical repetitions
            assertNotEquals(resPython.rawResponse, resJava.rawResponse)
            assertNotEquals(resPython.rawResponse, resMath.rawResponse)
            assertNotEquals(resJava.rawResponse, resMath.rawResponse)

            // Ensure Python response discusses programming or python
            assertTrue(resPython.rawResponse.contains("python", ignoreCase = true) || resPython.rawResponse.contains("programming", ignoreCase = true))

            // Ensure 2+2 response evaluates to 4
            assertTrue(resMath.rawResponse.contains("4") || resMath.rawResponse.contains("four", ignoreCase = true))
        }

        // Ensure not repeating the stale hardcoded fever/hydration fallback
        assertTrue(!resPython.rawResponse.contains("Rest well and stay hydrated", ignoreCase = true))
    }

    @Test
    fun `test voice manager text chunking splits long response without truncation`() {
        val longParagraph = "Python is a versatile high-level programming language used extensively in data science, artificial intelligence, web development, and automation. ".repeat(30)
        assertTrue(longParagraph.length > 3000)

        val context = ApplicationProvider.getApplicationContext<Context>()
        val voiceManager = com.example.voice.VoiceManager(context)
        val chunks = voiceManager.chunkTextForTts(longParagraph, maxChunkSize = 2500)

        // Multiple chunks created
        assertTrue(chunks.size > 1)
        // No chunk exceeds 2500 chars
        assertTrue(chunks.all { it.length <= 2500 })
        // Total combined text contains all original words without truncation
        val combined = chunks.joinToString(" ")
        assertTrue(combined.contains("versatile high-level programming language"))
        assertTrue(combined.contains("data science"))
    }

    @Test
    fun `test markdown cleaner removes formatting while retaining complete words`() {
        val raw = "**Java** is an *object-oriented* programming language.\n\n- It is used for Android apps.\n- It runs on the JVM."
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = com.example.ui.viewmodel.HealthViewModel(app)
        val cleaned = vm.cleanMarkdownForSpeech(raw)

        assertEquals("Java is an object-oriented programming language.. It is used for Android apps. It runs on the JVM.", cleaned)
        assertTrue(cleaned.contains("Java"))
        assertTrue(cleaned.contains("object-oriented"))
        assertTrue(cleaned.contains("Android apps"))
        assertTrue(!cleaned.contains("**"))
        assertTrue(!cleaned.contains("*"))
    }

    @Test
    fun `test voice session active state initial and toggle`() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = com.example.ui.viewmodel.HealthViewModel(app)
        assertEquals(false, vm.isVoiceSessionActive.value)

        vm.startVoiceSession()
        assertEquals(true, vm.isVoiceSessionActive.value)

        vm.stopVoiceSession()
        assertEquals(false, vm.isVoiceSessionActive.value)
    }

    @Test
    fun `test welcome screen initial required for new user and skip flow`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = com.example.ui.viewmodel.HealthViewModel(app)

        // Clear any previous state
        vm.logoutSuspend()
        assertTrue(vm.isInitialWelcomeRequired())
        assertEquals(false, vm.authState.value.isAuthenticated)
        assertEquals(false, vm.authState.value.isSkipped)

        // Skip auth flow
        vm.skipAuthSuspend()

        assertTrue(vm.authState.value.isSkipped)
        assertEquals(false, vm.authState.value.isAuthenticated)
        // Once skipped, initial welcome screen is no longer required on subsequent app opens
        assertEquals(false, vm.isInitialWelcomeRequired())
    }

    @Test
    fun `test google login flow sets authenticated state`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = com.example.ui.viewmodel.HealthViewModel(app)

        vm.logoutSuspend()
        val result = vm.loginWithGoogleSuspend(
            preferredName = "Aarav Sharma",
            preferredEmail = "aarav@gmail.com"
        )

        assertTrue(result.isSuccess)
        assertTrue(vm.authState.value.isAuthenticated)
        assertEquals("Aarav Sharma", vm.authState.value.user?.name)
        assertEquals("aarav@gmail.com", vm.authState.value.user?.email)
        assertEquals(com.example.data.auth.AuthProvider.GOOGLE, vm.authState.value.user?.authProvider)
        assertEquals(false, vm.isInitialWelcomeRequired())
    }

    @Test
    fun `test phone auth validation and otp verification`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = com.example.ui.viewmodel.HealthViewModel(app)

        // Test invalid phone number
        val resInvalidPhone = vm.sendPhoneOtpSuspend("123")
        assertTrue(resInvalidPhone.isFailure)

        // Test valid 10-digit phone
        val resValidPhone = vm.sendPhoneOtpSuspend("9876543210")
        assertTrue(resValidPhone.isSuccess)

        // Test invalid OTP code
        val resInvalidOtp = vm.verifyPhoneOtpSuspend("9876543210", "12")
        assertTrue(resInvalidOtp.isFailure)

        // Test valid 6-digit OTP code
        val resValidOtp = vm.verifyPhoneOtpSuspend("9876543210", "654321")
        assertTrue(resValidOtp.isSuccess)
        assertTrue(vm.authState.value.isAuthenticated)
        assertEquals(com.example.data.auth.AuthProvider.PHONE, vm.authState.value.user?.authProvider)
        assertEquals(false, vm.isInitialWelcomeRequired())
    }

    @Test
    fun `test email registration and login validation`() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val vm = com.example.ui.viewmodel.HealthViewModel(app)

        // Registration with invalid email
        val resInvalidEmail = vm.registerWithEmailSuspend("Pooja", "not-an-email", "123456")
        assertTrue(resInvalidEmail.isFailure)

        // Registration with short password
        val resShortPass = vm.registerWithEmailSuspend("Pooja", "pooja@example.com", "123")
        assertTrue(resShortPass.isFailure)

        // Valid registration
        val resValidReg = vm.registerWithEmailSuspend("Pooja Verma", "pooja@example.com", "password123")
        assertTrue(resValidReg.isSuccess)
        assertTrue(vm.authState.value.isAuthenticated)
        assertEquals("Pooja Verma", vm.authState.value.user?.name)
        assertEquals("pooja@example.com", vm.authState.value.user?.email)

        // Logout resets state
        vm.logoutSuspend()
        assertEquals(false, vm.authState.value.isAuthenticated)
        assertTrue(vm.isInitialWelcomeRequired())
    }

    @Test
    fun `test voice assistant empathetic response on tiredness`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = HealthRepository(context)

        val res = repo.evaluateSymptomInput("I'm feeling really tired today", AppLanguage.ENGLISH, isOnline = false)
        assertNotNull(res.rawResponse)
        // Should show warmth/empathy and ask natural follow-up question
        val text = res.rawResponse.lowercase()
        assertTrue(text.contains("uncomfortable") || text.contains("hear you") || text.contains("tired"))
        assertTrue(text.contains("today") || text.contains("few days") || text.contains("rest"))
        // Should NOT sound robotic like an IVR
        assertFalse(text.contains("please provide additional information"))
    }

    @Test
    fun `test voice assistant Hinglish response on headache`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = HealthRepository(context)

        val res = repo.evaluateSymptomInput("Yaar mujhe subah se headache ho raha hai", AppLanguage.ENGLISH, isOnline = false)
        assertNotNull(res.rawResponse)
        val text = res.rawResponse.lowercase()
        // Should understand Hinglish and reply in friendly Hinglish
        assertTrue(text.contains("samajh gaya") || text.contains("headache") || text.contains("pain"))
        assertTrue(text.contains("constant") || text.contains("kam") || text.contains("aaram"))
    }

    @Test
    fun `test voice assistant maintains conversational context across turns`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = HealthRepository(context)

        // Turn 1: User says stomach hurts
        val history = listOf("user" to "My stomach hurts", "model" to "Okay. When did the pain start?")
        // Turn 2: User provides follow-up answer: "Last night"
        val res = repo.evaluateSymptomInput("Last night", AppLanguage.ENGLISH, isOnline = false, conversationHistory = history)
        assertNotNull(res.rawResponse)
        val text = res.rawResponse.lowercase()
        // Should understand "last night" in context rather than failing as unknown
        assertTrue(text.contains("got it") || text.contains("explaining") || text.contains("rest") || text.contains("stomach"))
        assertFalse(text.contains("what are you talking about"))
    }

    @Test
    fun `test WelfareAiEngine detects elevated welfare risk for prolonged deployment and night duties`() {
        val duty = DutyWorkloadData(
            dutyHoursLast24h = 16f,
            nightDutyCountLastWeek = 5,
            deploymentDays = 120,
            daysSinceLastLeave = 150,
            hasIrregularShifts = true,
            hasHeavyTrainingCommitment = true
        )
        val wellness = SelfReportedWellness(
            stressLevel = 8,
            mood = "Overwhelmed",
            sleepHours = 4.5f,
            sleepQuality = 1,
            fatigueLevel = 8,
            workloadPerception = 9
        )
        val result = WelfareAiEngine.analyzeWelfarePattern(duty, wellness)

        assertEquals(WelfareIndicatorLevel.ELEVATED, result.indicatorLevel)
        assertTrue(result.compositeScore >= 62f)
        assertTrue(result.contributingSignals.isNotEmpty())
        // Welfare recommendations must be framed as supportive options, not clinical diagnoses or commands
        assertTrue(result.recommendationsEn.any { it.contains("Consider", ignoreCase = true) })
        assertFalse(result.recommendationsEn.any { it.contains("You have burnout", ignoreCase = true) })
    }

    @Test
    fun `test WelfareAiEngine outputs lower indicators for balanced routine`() {
        val duty = DutyWorkloadData(
            dutyHoursLast24h = 8f,
            nightDutyCountLastWeek = 0,
            deploymentDays = 10,
            daysSinceLastLeave = 15,
            hasIrregularShifts = false,
            hasHeavyTrainingCommitment = false
        )
        val wellness = SelfReportedWellness(
            stressLevel = 2,
            mood = "Calm",
            sleepHours = 7.5f,
            sleepQuality = 4,
            fatigueLevel = 2,
            workloadPerception = 3
        )
        val result = WelfareAiEngine.analyzeWelfarePattern(duty, wellness)

        assertEquals(WelfareIndicatorLevel.LOWER, result.indicatorLevel)
        assertTrue(result.compositeScore < 32f)
        // Even for lower risk, supportive resources remain available
        assertTrue(result.recommendationsEn.isNotEmpty())
    }

    @Test
    fun `test WelfareAiEngine records data limitations when optional wearable is unlinked`() {
        val duty = DutyWorkloadData()
        val wellness = SelfReportedWellness()
        val result = WelfareAiEngine.analyzeWelfarePattern(duty, wellness)

        assertTrue(result.dataLimitations.any { it.contains("Wearable biometric", ignoreCase = true) })
        assertNotNull(result.confidence)
    }

    @Test
    fun `test WellnessRepository assessment persistence and audit logging`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = WellnessRepository(context)

        val duty = DutyWorkloadData(dutyHoursLast24h = 12f)
        val wellness = SelfReportedWellness(stressLevel = 6)

        val result = repo.saveWellnessAssessment(duty, wellness)
        assertNotNull(result)

        val records = repo.wellnessRecords.first()
        assertTrue(records.isNotEmpty())
        assertEquals(wellness.stressLevel, records.first().stressLevel)

        val logs = repo.auditLogs.first()
        assertTrue(logs.any { it.action == "ASSESSMENT_COMPLETED" })
    }

    @Test
    fun `test WellnessRepository support request lifecycle and officer review`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = WellnessRepository(context)

        val reqId = repo.submitSupportRequest(
            category = SupportCategory.COUNSELING,
            details = "Requesting confidential talk regarding sleep and stress",
            urgency = "Timely",
            personnelName = "HC Sharma",
            unitOrStation = "104 Bn"
        )
        assertTrue(reqId > 0)

        val list = repo.supportRequests.first()
        val myReq = list.firstOrNull { it.id == reqId }
        assertNotNull(myReq)
        assertEquals("NEW", myReq?.status)

        // Authorized Officer review
        repo.updateSupportRequestStatus(
            request = myReq!!,
            newStatus = RequestStatus.SUPPORT_OFFERED,
            officerNotes = "Contacted counselor; session scheduled.",
            reviewedBy = "Welfare Officer Capt. Rawat"
        )

        val updatedList = repo.supportRequests.first()
        val updatedReq = updatedList.firstOrNull { it.id == reqId }
        assertEquals(RequestStatus.SUPPORT_OFFERED.name, updatedReq?.status)
        assertEquals("Welfare Officer Capt. Rawat", updatedReq?.reviewedBy)
    }

    @Test
    fun `test voluntary consent management and history clearing`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = WellnessRepository(context)

        // Update voluntary consent
        repo.updateConsent(voluntaryAssessment = true, wearable = false, unitAnalytics = true)
        val consent = repo.consentRecord.first()
        assertNotNull(consent)
        assertTrue(consent?.voluntaryAssessmentConsent == true)
        assertFalse(consent?.wearableConsent == true)

        // Right to withdraw / delete personal assessment history
        repo.clearMyWellnessHistory()
        val recordsAfterClear = repo.wellnessRecords.first()
        assertTrue(recordsAfterClear.isEmpty())

        val logs = repo.auditLogs.first()
        assertTrue(logs.any { it.action == "WELLNESS_HISTORY_CLEARED" })
    }

    @Test
    fun `test real connectivity verification report structure`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val report = com.example.data.remote.ConnectivityService.verifyRealConnectivity(context)
        assertNotNull(report)
        assertNotNull(report.activeModel)
        assertTrue(report.activeModel.isNotEmpty())
    }

    @Test
    fun `test health research service detects research queries and retrieves sources`() = runBlocking {
        assertTrue(com.example.data.remote.HealthResearchService.requiresOnlineResearch("What are the WHO guidelines for dehydration?"))
        assertTrue(com.example.data.remote.HealthResearchService.requiresOnlineResearch("Latest fever management recommendations"))
        assertFalse(com.example.data.remote.HealthResearchService.requiresOnlineResearch("Hello how are you"))

        val researchResult = com.example.data.remote.HealthResearchService.retrieveHealthResearch("WHO dehydration ORS")
        assertTrue(researchResult.isResearchAttempted)
        assertTrue(researchResult.isResearchSuccessful)
        assertTrue(researchResult.sources.isNotEmpty())
        assertTrue(researchResult.aggregatedContext?.contains("WHO") == true)
    }

    @Test
    fun `test transparent fallback notice when offline`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = HealthRepository(context)

        // Force offline request
        val result = repo.evaluateSymptomInput("I have mild fever and headache", AppLanguage.ENGLISH, isOnline = false)
        assertNotNull(result)
        assertFalse(result.isOnlineAiResponse)
        assertTrue(result.rawResponse.isNotBlank())
    }
}
