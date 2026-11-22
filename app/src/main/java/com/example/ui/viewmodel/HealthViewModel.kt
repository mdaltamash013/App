package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthState
import com.example.data.auth.UserProfile
import com.example.data.knowledge.SafetyRuleEngine
import com.example.data.local.AssessmentRecordEntity
import com.example.data.local.EmergencyContactEntity
import com.example.data.location.LocationErrorType
import com.example.data.location.LocationService
import com.example.data.location.LocationState
import com.example.data.wellness.DemoPersonnelProfile
import com.example.data.wellness.DutyWorkloadData
import com.example.data.wellness.RequestStatus
import com.example.data.wellness.SelfReportedWellness
import com.example.data.wellness.SupportCategory
import com.example.data.wellness.SupportRequestEntity
import com.example.data.wellness.UserRole
import com.example.data.wellness.WearableBiometricData
import com.example.data.wellness.WelfareAssessmentResult
import com.example.data.wellness.WelfareAuditLogEntity
import com.example.data.wellness.WelfareResource
import com.example.data.wellness.WellnessConsentEntity
import com.example.data.wellness.WellnessRecordEntity
import com.example.data.wellness.WellnessRepository
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.DoctorInfo
import com.example.data.model.FacilityType
import com.example.data.model.HealthcareFacility
import com.example.data.model.MedicineItem
import com.example.data.model.MessageSender
import com.example.data.model.NetworkStatus
import com.example.data.model.SymptomAssessment
import com.example.data.model.TriageLevel
import com.example.data.model.VoiceState
import com.example.data.remote.AiResponseResult
import com.example.data.remote.ConnectivityHealthReport
import com.example.data.remote.ConnectivityService
import com.example.data.remote.GeminiService
import com.example.data.repository.HealthRepository
import com.example.voice.VoiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HealthViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HealthRepository(application)
    private val authRepository = AuthRepository(application)
    private val wellnessRepository = WellnessRepository(application)
    private val locationService = LocationService(application)

    // Current Device Location State
    private val _locationState = MutableStateFlow<LocationState>(LocationState.Idle)
    val locationState: StateFlow<LocationState> = _locationState.asStateFlow()

    private val _detectedLocationText = MutableStateFlow<String>("Detecting location...")
    val detectedLocationText: StateFlow<String> = _detectedLocationText.asStateFlow()

    // Wellness & Welfare Streams
    val wellnessRecords: StateFlow<List<WellnessRecordEntity>> = wellnessRepository.wellnessRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val latestWellnessRecord: StateFlow<WellnessRecordEntity?> = wellnessRepository.latestWellnessRecord
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val supportRequests: StateFlow<List<SupportRequestEntity>> = wellnessRepository.supportRequests
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val consentRecord: StateFlow<WellnessConsentEntity?> = wellnessRepository.consentRecord
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val auditLogs: StateFlow<List<WelfareAuditLogEntity>> = wellnessRepository.auditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentUserRole = MutableStateFlow(UserRole.PERSONNEL)
    val currentUserRole: StateFlow<UserRole> = _currentUserRole.asStateFlow()

    private val _isDemoModeActive = MutableStateFlow(false)
    val isDemoModeActive: StateFlow<Boolean> = _isDemoModeActive.asStateFlow()

    fun switchUserRole(role: UserRole) {
        _currentUserRole.value = role
    }

    fun toggleDemoMode() {
        _isDemoModeActive.value = !_isDemoModeActive.value
    }

    // Authentication State
    val authState: StateFlow<AuthState> = authRepository.authState

    fun isInitialWelcomeRequired(): Boolean = authRepository.isInitialWelcomeRequired()

    fun skipAuth(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.skipAuth()
            onComplete()
        }
    }

    fun loginWithGoogle(
        preferredName: String = "Google User",
        preferredEmail: String = "user@lifecare247.ai",
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = authRepository.loginWithGoogle(preferredName, preferredEmail)
            onComplete(result.isSuccess)
        }
    }

    fun sendPhoneOtp(phone: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendPhoneOtp(phone)
            if (result.isSuccess) {
                onResult(true, result.getOrNull() ?: "OTP Sent")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Failed to send OTP")
            }
        }
    }

    fun verifyPhoneOtp(phone: String, otp: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.verifyPhoneOtp(phone, otp)
            if (result.isSuccess) {
                onResult(true, "Phone verified successfully")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Verification failed")
            }
        }
    }

    fun loginWithEmail(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.loginWithEmail(email, password)
            if (result.isSuccess) {
                onResult(true, "Signed in successfully")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun registerWithEmail(name: String, email: String, password: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.registerWithEmail(name, email, password)
            if (result.isSuccess) {
                onResult(true, "Registration successful")
            } else {
                onResult(false, result.exceptionOrNull()?.message ?: "Registration failed")
            }
        }
    }

    fun logout(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            authRepository.logout()
            onComplete()
        }
    }

    suspend fun skipAuthSuspend() = authRepository.skipAuth()
    suspend fun loginWithGoogleSuspend(preferredName: String = "Google User", preferredEmail: String = "user@lifecare247.ai") =
        authRepository.loginWithGoogle(preferredName, preferredEmail)
    suspend fun sendPhoneOtpSuspend(phone: String) = authRepository.sendPhoneOtp(phone)
    suspend fun verifyPhoneOtpSuspend(phone: String, otp: String) = authRepository.verifyPhoneOtp(phone, otp)
    suspend fun loginWithEmailSuspend(email: String, password: String) = authRepository.loginWithEmail(email, password)
    suspend fun registerWithEmailSuspend(name: String, email: String, password: String) = authRepository.registerWithEmail(name, email, password)
    suspend fun logoutSuspend() = authRepository.logout()

    fun clearAuthMessages() {
        authRepository.clearMessages()
    }

    // Language state (English default as required)
    private val _language = MutableStateFlow(AppLanguage.ENGLISH)
    val language: StateFlow<AppLanguage> = _language.asStateFlow()

    // Connectivity state
    private val _networkStatus = MutableStateFlow(NetworkStatus.ONLINE)
    val networkStatus: StateFlow<NetworkStatus> = _networkStatus.asStateFlow()

    private val _connectivityReport = MutableStateFlow(
        ConnectivityHealthReport(
            isDeviceConnected = false,
            isBackendReachable = false,
            isAiServiceReachable = false,
            hasValidApiKey = false,
            activeModel = GeminiService.PRIMARY_MODEL,
            lastLatencyMs = 0L
        )
    )
    val connectivityReport: StateFlow<ConnectivityHealthReport> = _connectivityReport.asStateFlow()

    private val _lastAiDiagnostic = MutableStateFlow<AiResponseResult?>(null)
    val lastAiDiagnostic: StateFlow<AiResponseResult?> = _lastAiDiagnostic.asStateFlow()

    // Typing fallback vs Voice first
    private val _isTypingMode = MutableStateFlow(false)
    val isTypingMode: StateFlow<Boolean> = _isTypingMode.asStateFlow()

    // Navigation events triggered by voice commands
    private val _navEvent = MutableSharedFlow<String>()
    val navEvent: SharedFlow<String> = _navEvent.asSharedFlow()

    // Active assessment & Emergency
    private val _currentAssessment = MutableStateFlow<SymptomAssessment?>(null)
    val currentAssessment: StateFlow<SymptomAssessment?> = _currentAssessment.asStateFlow()

    private val _isEmergencyActive = MutableStateFlow(false)
    val isEmergencyActive: StateFlow<Boolean> = _isEmergencyActive.asStateFlow()

    // Conversation history
    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    // Facilities & Medicines
    private val _selectedFacilityType = MutableStateFlow<FacilityType?>(null)
    val selectedFacilityType: StateFlow<FacilityType?> = _selectedFacilityType.asStateFlow()

    private val _facilities = MutableStateFlow<List<HealthcareFacility>>(emptyList())
    val facilities: StateFlow<List<HealthcareFacility>> = _facilities.asStateFlow()

    private val _medicineQuery = MutableStateFlow("")
    val medicineQuery: StateFlow<String> = _medicineQuery.asStateFlow()

    private val _medicines = MutableStateFlow<List<MedicineItem>>(emptyList())
    val medicines: StateFlow<List<MedicineItem>> = _medicines.asStateFlow()

    // Doctors
    private val _doctors = MutableStateFlow<List<DoctorInfo>>(emptyList())
    val doctors: StateFlow<List<DoctorInfo>> = _doctors.asStateFlow()

    private val _selectedDoctor = MutableStateFlow<DoctorInfo?>(null)
    val selectedDoctor: StateFlow<DoctorInfo?> = _selectedDoctor.asStateFlow()

    // Room DB streams
    val savedAssessments: StateFlow<List<AssessmentRecordEntity>> = repository.savedAssessments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val emergencyContacts: StateFlow<List<EmergencyContactEntity>> = repository.emergencyContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalAssessmentCount: StateFlow<Int> = repository.assessmentCount
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // Voice Manager integration
    val voiceManager: VoiceManager = VoiceManager(
        context = application,
        onSpeechRecognized = { recognizedText ->
            processSymptomInput(recognizedText)
        },
        onVoiceNavCommand = { command ->
            handleVoiceNavigation(command)
        }
    )

    val voiceState: StateFlow<VoiceState> = voiceManager.voiceState
    val audioRms: StateFlow<Float> = voiceManager.audioRms
    val isHandsFree: StateFlow<Boolean> = voiceManager.isHandsFree
    val isVoiceSessionActive: StateFlow<Boolean> = voiceManager.isVoiceSessionActive
    val isMuted: StateFlow<Boolean> = voiceManager.isMuted

    init {
        monitorNetwork(application)
        loadInitialData()
        seedDefaultEmergencyContacts()
        if (locationService.hasLocationPermission()) {
            requestCurrentLocation()
        }
    }

    private fun loadInitialData() {
        _facilities.value = repository.getNearbyFacilities()
        _medicines.value = repository.searchMedicines("")
        _doctors.value = repository.getDoctors()
    }

    private fun seedDefaultEmergencyContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.saveEmergencyContact("Family Emergency", "Primary Kin", "108", isPrimary = true)
            repository.saveEmergencyContact("Village ASHA Worker (Kamla Devi)", "Health Worker", "+91 94120 12345", isPrimary = false)
            repository.saveEmergencyContact("Local Ambulance Driver (Ramesh)", "Emergency Driver", "+91 98370 56789", isPrimary = false)
        }
    }

    private fun monitorNetwork(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        refreshConnectivityStatus(context)

        if (cm != null) {
            val req = NetworkRequest.Builder().build()
            cm.registerNetworkCallback(req, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    refreshConnectivityStatus(context)
                }

                override fun onLost(network: Network) {
                    _networkStatus.value = NetworkStatus.OFFLINE
                    _connectivityReport.value = _connectivityReport.value.copy(
                        isDeviceConnected = false,
                        isBackendReachable = false,
                        isAiServiceReachable = false,
                        failureReason = "Device network disconnected"
                    )
                }
            })
        }
    }

    fun refreshConnectivityStatus(context: Context = getApplication()) {
        viewModelScope.launch {
            val report = ConnectivityService.verifyRealConnectivity(context)
            _connectivityReport.value = report
            _networkStatus.value = when {
                !report.isDeviceConnected -> NetworkStatus.OFFLINE
                !report.isBackendReachable -> NetworkStatus.WEAK
                !report.isAiServiceReachable -> NetworkStatus.WEAK
                else -> NetworkStatus.ONLINE
            }
            Log.d("HealthViewModel", "Connectivity verification: $report, status: ${_networkStatus.value}")
            if (report.isDeviceConnected) {
                retryPendingReverseGeocoding()
            }
        }
    }

    // Geolocation Methods
    fun requestCurrentLocation() {
        viewModelScope.launch {
            _locationState.value = LocationState.Detecting
            val result = locationService.detectCurrentLocation()
            _locationState.value = result
            when (result) {
                is LocationState.Success -> {
                    _detectedLocationText.value = result.readableAddress
                }
                is LocationState.Error -> {
                    _detectedLocationText.value = if (result.errorType == LocationErrorType.PERMISSION_REQUIRED) {
                        "Location permission required"
                    } else {
                        "Unable to detect location"
                    }
                }
                else -> {}
            }
        }
    }

    fun retryLocation() {
        requestCurrentLocation()
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        if (isGranted) {
            requestCurrentLocation()
        } else {
            _locationState.value = LocationState.Error(
                errorType = LocationErrorType.PERMISSION_REQUIRED,
                message = "Location permission is required to detect nearby medical facilities and emergency aid."
            )
            _detectedLocationText.value = "Location permission required"
        }
    }

    fun updateManualLocation(text: String) {
        val trimmed = text.trim()
        if (trimmed.isNotBlank()) {
            _detectedLocationText.value = trimmed
            val current = _locationState.value
            if (current is LocationState.Success) {
                _locationState.value = current.copy(readableAddress = trimmed)
            } else {
                _locationState.value = LocationState.Success(
                    latitude = 0.0,
                    longitude = 0.0,
                    readableAddress = trimmed,
                    isReverseGeocoded = true
                )
            }
        }
    }

    private fun retryPendingReverseGeocoding() {
        val current = _locationState.value
        if (current is LocationState.Success && !current.isReverseGeocoded && (current.latitude != 0.0 || current.longitude != 0.0)) {
            viewModelScope.launch {
                val resolved = locationService.reverseGeocode(current.latitude, current.longitude)
                if (resolved.isReverseGeocoded) {
                    _locationState.value = current.copy(
                        readableAddress = resolved.readableAddress,
                        isReverseGeocoded = true,
                        city = resolved.city,
                        state = resolved.state,
                        country = resolved.country
                    )
                    _detectedLocationText.value = resolved.readableAddress
                }
            }
        }
    }

    fun testOnlineAiConnection(onComplete: (AiResponseResult) -> Unit = {}) {
        viewModelScope.launch {
            val result = GeminiService.executeDiagnosticTestPrompt()
            _lastAiDiagnostic.value = result
            if (result.success) {
                _networkStatus.value = NetworkStatus.ONLINE
            }
            onComplete(result)
        }
    }

    fun setLanguage(lang: AppLanguage) {
        _language.value = lang
        voiceManager.setLanguage(lang)
    }

    fun toggleLanguage() {
        val next = if (_language.value == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
        setLanguage(next)
    }

    fun setTypingMode(enabled: Boolean) {
        _isTypingMode.value = enabled
        if (enabled) {
            voiceManager.stopListening()
        }
    }

    fun startListening() {
        voiceManager.startVoiceSession(_language.value)
    }

    fun startVoiceSession() {
        voiceManager.startVoiceSession(_language.value)
    }

    fun stopVoiceSession() {
        voiceManager.stopVoiceSession()
    }

    fun toggleVoiceSession() {
        voiceManager.toggleVoiceSession()
    }

    fun stopListening() {
        voiceManager.stopListening()
    }

    fun toggleHandsFree() {
        voiceManager.toggleHandsFree()
    }

    fun stopHandsFree() {
        voiceManager.stopHandsFree()
    }

    fun toggleMute() {
        voiceManager.toggleMute()
    }

    val voiceFeedbackMessage: StateFlow<String?> = voiceManager.voiceFeedbackMessage

    fun handleMicTap() {
        voiceManager.handleMicTap()
    }

    fun repeatLastVoice() {
        voiceManager.repeatLast()
    }

    fun processSymptomInput(input: String, forceOffline: Boolean = false) {
        val trimmedInput = input.trim()
        if (trimmedInput.isBlank()) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = trimmedInput)
        _chatMessages.value = _chatMessages.value + userMsg

        viewModelScope.launch {
            // Format previous conversation history for Gemini multi-turn API
            val previousMessages = _chatMessages.value.dropLast(1)
            val historyPairs = mutableListOf<Pair<String, String>>()
            for (msg in previousMessages) {
                val role = if (msg.sender == MessageSender.USER) "user" else "model"
                if (historyPairs.isNotEmpty() && historyPairs.last().first == role) {
                    val prev = historyPairs.removeAt(historyPairs.size - 1)
                    historyPairs.add(role to "${prev.second}\n\n${msg.text}")
                } else {
                    historyPairs.add(role to msg.text)
                }
            }
            while (historyPairs.isNotEmpty() && historyPairs.first().first != "user") {
                historyPairs.removeAt(0)
            }
            while (historyPairs.isNotEmpty() && historyPairs.last().first == "user") {
                historyPairs.removeAt(historyPairs.size - 1)
            }

            // Check deterministic safety red flags first
            val safetyResult = SafetyRuleEngine.evaluate(trimmedInput, _language.value)

            if (safetyResult.isEmergency) {
                _isEmergencyActive.value = true
                val emergencyAssessment = SymptomAssessment(
                    mainComplaint = trimmedInput,
                    symptoms = listOf(trimmedInput),
                    riskLevel = TriageLevel.EMERGENCY,
                    guidanceTitle = safetyResult.displayWarning,
                    guidancePoints = safetyResult.immediateSteps,
                    isEmergency = true,
                    emergencyReason = safetyResult.redFlagReason,
                    rawResponse = safetyResult.spokenWarning
                )
                _currentAssessment.value = emergencyAssessment

                val aiMsg = ChatMessage(
                    sender = MessageSender.AI,
                    text = "${safetyResult.displayWarning}\n\n${safetyResult.spokenWarning}",
                    isEmergency = true
                )
                _chatMessages.value = _chatMessages.value + aiMsg

                // Immediate Spoken Voice Override
                voiceManager.speak(safetyResult.spokenWarning, _language.value)

                // Emit navigation to emergency screen
                _navEvent.emit("emergency")
            } else {
                _isEmergencyActive.value = false
                val isOnline = !forceOffline && _networkStatus.value != NetworkStatus.OFFLINE
                val assessment = repository.evaluateSymptomInput(
                    input = trimmedInput,
                    language = _language.value,
                    isOnline = isOnline,
                    conversationHistory = historyPairs
                )
                _currentAssessment.value = assessment

                // Build response body: prioritize fresh raw AI response
                val responseBody = if (assessment.rawResponse.isNotBlank()) {
                    assessment.rawResponse
                } else {
                    buildString {
                        append(assessment.guidanceTitle)
                        if (assessment.guidancePoints.isNotEmpty()) {
                            append("\n\n")
                            assessment.guidancePoints.forEachIndexed { idx, pt ->
                                append("${idx + 1}. $pt\n")
                            }
                        }
                        if (assessment.followUpQuestions.isNotEmpty()) {
                            append("\n")
                            append(if (_language.value == AppLanguage.HINDI) "फॉलो-अप प्रश्न:" else "Follow-up Questions:")
                            append("\n")
                            assessment.followUpQuestions.forEach { q ->
                                append("• $q\n")
                            }
                        }
                    }
                }

                val aiMsg = ChatMessage(
                    sender = MessageSender.AI,
                    text = responseBody,
                    isOnlineAi = assessment.isOnlineAiResponse,
                    modelName = assessment.aiModelUsed,
                    isFallback = assessment.isFallbackUsed,
                    fallbackReason = assessment.fallbackReason,
                    researchSources = assessment.researchCitations
                )
                _chatMessages.value = _chatMessages.value + aiMsg

                // Automatic Spoken Response: Speak complete response without any truncation
                val spokenText = if (assessment.rawResponse.isNotBlank()) {
                    cleanMarkdownForSpeech(assessment.rawResponse)
                } else {
                    buildSpokenResponse(assessment, _language.value)
                }
                voiceManager.speak(spokenText, _language.value)

                // Only navigate away if NOT in a continuous voice session
                if (!voiceManager.isVoiceSessionActive.value) {
                    _navEvent.emit("assessment")
                }
            }

            try {
                _currentAssessment.value?.let { current ->
                    repository.saveAssessment(current, _language.value)
                }
            } catch (e: Exception) {
                Log.e("HealthViewModel", "Error saving assessment to database", e)
            }
        }
    }

    fun cleanMarkdownForSpeech(text: String): String {
        return text
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "$1")
            .replace(Regex("\\*(.*?)\\*"), "$1")
            .replace(Regex("`{1,3}(.*?)(?:`{1,3}|$)", RegexOption.DOT_MATCHES_ALL), "$1")
            .replace(Regex("^#{1,6}\\s*", RegexOption.MULTILINE), "")
            .replace(Regex("^[-*•]\\s+", RegexOption.MULTILINE), "")
            .replace(Regex("\n{2,}"), ". ")
            .replace(Regex("\n"), " ")
            .trim()
    }

    private fun buildSpokenResponse(assessment: SymptomAssessment, lang: AppLanguage): String {
        return buildString {
            if (lang == AppLanguage.HINDI) {
                val intro = when (assessment.riskLevel) {
                    TriageLevel.LOW -> "मैं समझ सकता हूँ, यह वाकई तकलीफदेह है। कृपया थोड़ा आराम करें।"
                    TriageLevel.MODERATE -> "मैं समझ रहा हूँ। सुरक्षित रहने के लिए एक बार नजदीकी डॉक्टर से जांच जरूर कराएं।"
                    TriageLevel.EMERGENCY -> "कृपया तुरंत 108 पर कॉल करें या नजदीकी अस्पताल जाएं।"
                }
                append(intro).append(" ")
                if (assessment.guidancePoints.isNotEmpty()) {
                    assessment.guidancePoints.take(2).forEach { pt ->
                        append(pt).append(". ")
                    }
                }
                if (assessment.followUpQuestions.isNotEmpty()) {
                    append(assessment.followUpQuestions.first()).append(" ")
                }
            } else {
                val intro = when (assessment.riskLevel) {
                    TriageLevel.LOW -> "I hear you, and that sounds really uncomfortable. Please make sure to get plenty of rest."
                    TriageLevel.MODERATE -> "I understand how tiring that can be. Having a doctor take a look would be best."
                    TriageLevel.EMERGENCY -> "Please seek emergency medical care or call 108 right away."
                }
                append(intro).append(" ")
                if (assessment.guidancePoints.isNotEmpty()) {
                    assessment.guidancePoints.take(2).forEach { pt ->
                        append(pt).append(". ")
                    }
                }
                if (assessment.followUpQuestions.isNotEmpty()) {
                    append(assessment.followUpQuestions.first()).append(" ")
                }
            }
        }.trim()
    }

    private fun handleVoiceNavigation(command: String): Boolean {
        val lower = command.lowercase()
        return when {
            lower.contains("emergency") || lower.contains("help") || lower.contains("आपातकाल") || lower.contains("मदद") -> {
                viewModelScope.launch {
                    _isEmergencyActive.value = true
                    _navEvent.emit("emergency")
                }
                true
            }
            lower.contains("hospital") || lower.contains("clinic") || lower.contains("facility") || lower.contains("अस्पताल") -> {
                viewModelScope.launch { _navEvent.emit("facilities") }
                true
            }
            lower.contains("doctor") || lower.contains("consult") || lower.contains("डॉक्टर") -> {
                viewModelScope.launch { _navEvent.emit("doctor") }
                true
            }
            lower.contains("medicine") || lower.contains("pharmacy") || lower.contains("दवा") -> {
                viewModelScope.launch { _navEvent.emit("medicine") }
                true
            }
            lower.contains("record") || lower.contains("summary") || lower.contains("रिकॉर्ड") -> {
                viewModelScope.launch { _navEvent.emit("records") }
                true
            }
            lower.contains("wellness") || lower.contains("stress") || lower.contains("burnout") || lower.contains("welfare") || lower.contains("तनाव") || lower.contains("कल्याण") -> {
                viewModelScope.launch { _navEvent.emit("wellness") }
                val msg = if (_language.value == AppLanguage.HINDI) "कल्याण एवं तनाव सहायता केंद्र खोला जा रहा है।" else "Opening My Wellness and Welfare Support."
                voiceManager.speak(msg, _language.value)
                true
            }
            lower.contains("hindi") || lower.contains("हिन्दी") -> {
                setLanguage(AppLanguage.HINDI)
                voiceManager.speak("भाषा हिन्दी में सेट कर दी गई है।", AppLanguage.HINDI)
                true
            }
            lower.contains("english") || lower.contains("अंग्रेजी") -> {
                setLanguage(AppLanguage.ENGLISH)
                voiceManager.speak("Language switched to English.", AppLanguage.ENGLISH)
                true
            }
            else -> false
        }
    }

    fun filterFacilities(type: FacilityType?) {
        _selectedFacilityType.value = type
        _facilities.value = repository.getNearbyFacilities(type)
    }

    fun searchMedicines(query: String) {
        _medicineQuery.value = query
        _medicines.value = repository.searchMedicines(query)
    }

    fun selectDoctor(doctor: DoctorInfo) {
        _selectedDoctor.value = doctor
    }

    fun saveCurrentAssessmentToRecord() {
        val assessment = _currentAssessment.value ?: return
        viewModelScope.launch {
            repository.saveAssessment(assessment, _language.value)
        }
    }

    fun addEmergencyContact(name: String, relation: String, phone: String) {
        viewModelScope.launch {
            repository.saveEmergencyContact(name, relation, phone)
        }
    }

    fun deleteEmergencyContact(contact: EmergencyContactEntity) {
        viewModelScope.launch {
            repository.deleteEmergencyContact(contact)
        }
    }

    // Welfare & Burnout Early-Warning Methods
    fun submitWellnessAssessment(
        duty: DutyWorkloadData,
        wellness: SelfReportedWellness,
        wearable: WearableBiometricData = WearableBiometricData(),
        onResult: (WelfareAssessmentResult) -> Unit = {}
    ) {
        viewModelScope.launch {
            val result = wellnessRepository.saveWellnessAssessment(duty, wellness, wearable, _language.value)
            onResult(result)
        }
    }

    fun submitSupportRequest(
        category: SupportCategory,
        details: String,
        urgency: String = "Timely",
        preferredContactMethod: String = "In-Person Discussion",
        personnelName: String = "Confidential Personnel",
        unitOrStation: String = "General Duty Unit",
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val id = wellnessRepository.submitSupportRequest(
                category = category,
                details = details,
                urgency = urgency,
                preferredContactMethod = preferredContactMethod,
                personnelName = personnelName,
                unitOrStation = unitOrStation
            )
            onComplete(id > 0)
        }
    }

    fun updateSupportRequestStatus(
        request: SupportRequestEntity,
        newStatus: RequestStatus,
        notes: String?,
        reviewedBy: String
    ) {
        viewModelScope.launch {
            wellnessRepository.updateSupportRequestStatus(request, newStatus, notes, reviewedBy)
        }
    }

    fun updateConsent(
        voluntaryAssessment: Boolean,
        wearable: Boolean,
        unitAnalytics: Boolean
    ) {
        viewModelScope.launch {
            wellnessRepository.updateConsent(voluntaryAssessment, wearable, unitAnalytics)
        }
    }

    fun clearMyWellnessHistory(onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            wellnessRepository.clearMyWellnessHistory()
            onComplete()
        }
    }

    fun getWelfareResources(): List<WelfareResource> {
        return wellnessRepository.getWelfareResources()
    }

    fun getDemoProfiles(): List<DemoPersonnelProfile> {
        return wellnessRepository.getDemoProfiles()
    }

    fun triggerScenario(scenarioNumber: Int) {
        when (scenarioNumber) {
            1 -> { // Scenario 1: Low Risk (Mild Fever / Cold)
                val text = if (_language.value == AppLanguage.HINDI) "मुझे कल से हल्का बुखार और सिरदर्द है" else "I have mild fever and headache since yesterday"
                processSymptomInput(text)
            }
            2 -> { // Scenario 2: Moderate Risk (Diarrhea / Dehydration)
                val text = if (_language.value == AppLanguage.HINDI) "मुझे 2 दिन से लगातार दस्त और बहुत कमजोरी है" else "I have had loose motions for 2 days and feel very weak"
                processSymptomInput(text)
            }
            3 -> { // Scenario 3: Emergency Red Flag Override
                val text = if (_language.value == AppLanguage.HINDI) "मेरे सीने में बहुत तेज दर्द हो रहा है और सांस नहीं आ रही" else "I have severe crushing chest pain and cannot breathe properly"
                processSymptomInput(text)
            }
            4 -> { // Scenario 4: Offline Mode Simulation
                val text = if (_language.value == AppLanguage.HINDI) "पेट में गैस और जलन हो रही है" else "I have stomach acidity and burning pain"
                processSymptomInput(text, forceOffline = true)
            }
            5 -> { // Scenario 5: Hindi Voice
                setLanguage(AppLanguage.HINDI)
                val text = "मेरे बच्चे को खांसी और जुकाम है"
                processSymptomInput(text)
            }
            6 -> { // Scenario 6: Medicine & Verified Pharmacy Availability
                searchMedicines("ORS")
                viewModelScope.launch { _navEvent.emit("medicine") }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.release()
    }
}
