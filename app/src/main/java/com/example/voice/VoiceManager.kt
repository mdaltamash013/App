package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AppLanguage
import com.example.data.model.VoiceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class VoiceManager(
    private val context: Context,
    private val onSpeechRecognized: (String) -> Unit = {},
    private val onVoiceNavCommand: (String) -> Boolean = { false }
) {
    private val TAG = "VoiceManager"

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    private val _voiceState = MutableStateFlow(VoiceState.IDLE)
    val voiceState: StateFlow<VoiceState> = _voiceState.asStateFlow()

    private val _audioRms = MutableStateFlow(0f)
    val audioRms: StateFlow<Float> = _audioRms.asStateFlow()

    private val _isMuted = MutableStateFlow(false)
    val isMuted: StateFlow<Boolean> = _isMuted.asStateFlow()

    private val _isVoiceSessionActive = MutableStateFlow(false)
    val isVoiceSessionActive: StateFlow<Boolean> = _isVoiceSessionActive.asStateFlow()

    // Backward compatibility with isHandsFree
    val isHandsFree: StateFlow<Boolean> = _isVoiceSessionActive.asStateFlow()

    private val _lastSpokenText = MutableStateFlow("")
    val lastSpokenText: StateFlow<String> = _lastSpokenText.asStateFlow()

    private val _voiceFeedbackMessage = MutableStateFlow<String?>(null)
    val voiceFeedbackMessage: StateFlow<String?> = _voiceFeedbackMessage.asStateFlow()

    private val mainHandler = Handler(Looper.getMainLooper())
    private var currentLanguage: AppLanguage = AppLanguage.ENGLISH
    private var currentFinalUtteranceId: String? = null

    init {
        initTts()
        initSpeechRecognizer()
    }

    private fun initTts() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsReady = true
                applyTtsLanguage(currentLanguage)
                textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _voiceState.value = VoiceState.SPEAKING
                    }

                    override fun onDone(utteranceId: String?) {
                        handleUtteranceCompleted(utteranceId)
                    }

                    override fun onError(utteranceId: String?) {
                        handleUtteranceCompleted(utteranceId)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?, errorCode: Int) {
                        handleUtteranceCompleted(utteranceId)
                    }
                })
            } else {
                Log.e(TAG, "TTS Initialization failed")
            }
        }
    }

    private fun handleUtteranceCompleted(utteranceId: String?) {
        // Only trigger completion actions on the final utterance chunk
        val isFinal = utteranceId == null ||
                utteranceId == currentFinalUtteranceId ||
                utteranceId.startsWith("swasthya_final_")

        if (isFinal) {
            mainHandler.post {
                _voiceState.value = VoiceState.IDLE
                if (_isVoiceSessionActive.value) {
                    // Continuous Voice Mode: Automatically resume listening for the next question
                    mainHandler.postDelayed({
                        if (_isVoiceSessionActive.value && _voiceState.value == VoiceState.IDLE) {
                            startListening(currentLanguage)
                        }
                    }, 500)
                }
            }
        }
    }

    private fun applyTtsLanguage(language: AppLanguage) {
        if (!isTtsReady || textToSpeech == null) return
        val locale = if (language == AppLanguage.HINDI) Locale("hi", "IN") else Locale.US
        val res = textToSpeech?.setLanguage(locale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language $locale not supported, falling back to US English")
            textToSpeech?.language = Locale.US
        }
    }

    private fun initSpeechRecognizer() {
        mainHandler.post {
            try {
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechRecognizer?.destroy()
                    speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                        setRecognitionListener(createRecognitionListener())
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed creating SpeechRecognizer", e)
            }
        }
    }

    private fun createRecognitionListener(): RecognitionListener {
        return object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                _voiceState.value = VoiceState.LISTENING
                _voiceFeedbackMessage.value = null
            }

            override fun onBeginningOfSpeech() {
                _voiceState.value = VoiceState.LISTENING
                _voiceFeedbackMessage.value = null
            }

            override fun onRmsChanged(rmsdB: Float) {
                // Normalize roughly 0 to 1
                val normalized = ((rmsdB + 2f) / 10f).coerceIn(0f, 1f)
                _audioRms.value = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                _voiceState.value = VoiceState.PROCESSING
                _audioRms.value = 0f
            }

            override fun onError(error: Int) {
                _audioRms.value = 0f
                Log.w(TAG, "Speech recognition error code: $error")
                val isHindi = currentLanguage == AppLanguage.HINDI
                val friendlyMsg = when (error) {
                    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
                        if (isHindi) "वॉइस बातचीत के लिए माइक्रोफ़ोन अनुमति आवश्यक है। कृपया इसे डिवाइस सेटिंग्स में अनुमति दें।"
                        else "Microphone permission is needed for voice conversations. Please allow it in your device settings."
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
                        if (isHindi) "माफ़ कीजिए, सुन नहीं पाया। क्या आप दोबारा कह सकते हैं?"
                        else "Sorry, I didn't catch that. Could you say that again?"
                    SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                        if (isHindi) "नेटवर्क समस्या है। आप लिखकर भी बता सकते हैं।"
                        else "Network connection issue. You can also type or try again."
                    else ->
                        if (isHindi) "माफ़ कीजिए, आवाज़ समझ नहीं आई। कृपया दोबारा बोलें।"
                        else "Sorry, I didn't catch that. Could you say that again?"
                }
                _voiceFeedbackMessage.value = friendlyMsg

                if (_isVoiceSessionActive.value) {
                    _voiceState.value = VoiceState.IDLE
                    // In continuous voice mode, wait briefly and resume listening
                    mainHandler.postDelayed({
                        if (_isVoiceSessionActive.value && _voiceState.value == VoiceState.IDLE) {
                            startListening(currentLanguage)
                        }
                    }, 1200)
                } else {
                    _voiceState.value = VoiceState.ERROR
                }
            }

            override fun onResults(results: Bundle?) {
                _audioRms.value = 0f
                _voiceFeedbackMessage.value = null
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val recognizedText = matches?.firstOrNull()?.trim() ?: ""
                if (recognizedText.isNotBlank()) {
                    handleRecognizedText(recognizedText)
                } else {
                    _voiceState.value = VoiceState.IDLE
                    if (_isVoiceSessionActive.value) {
                        mainHandler.postDelayed({
                            if (_isVoiceSessionActive.value && _voiceState.value == VoiceState.IDLE) {
                                startListening(currentLanguage)
                            }
                        }, 800)
                    }
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        }
    }

    fun setLanguage(language: AppLanguage) {
        currentLanguage = language
        applyTtsLanguage(language)
    }

    fun startListening(language: AppLanguage = currentLanguage) {
        currentLanguage = language
        stopSpeaking()

        mainHandler.post {
            try {
                if (speechRecognizer == null) {
                    initSpeechRecognizer()
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    val langTag = if (language == AppLanguage.HINDI) "hi-IN" else "en-IN"
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, langTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, langTag)
                }

                _voiceState.value = VoiceState.LISTENING
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed starting speech listening", e)
                _voiceState.value = VoiceState.ERROR
            }
        }
    }

    fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping listening", e)
            }
            if (_voiceState.value == VoiceState.LISTENING) {
                _voiceState.value = VoiceState.IDLE
            }
            _audioRms.value = 0f
        }
    }

    fun speak(text: String, language: AppLanguage = currentLanguage) {
        val trimmedText = text.trim()
        if (trimmedText.isBlank()) {
            _voiceState.value = VoiceState.IDLE
            if (_isVoiceSessionActive.value) {
                mainHandler.postDelayed({
                    if (_isVoiceSessionActive.value && _voiceState.value == VoiceState.IDLE) {
                        startListening(currentLanguage)
                    }
                }, 500)
            }
            return
        }

        _lastSpokenText.value = trimmedText
        if (_isMuted.value) {
            _voiceState.value = VoiceState.IDLE
            if (_isVoiceSessionActive.value) {
                mainHandler.postDelayed({
                    if (_isVoiceSessionActive.value && _voiceState.value == VoiceState.IDLE) {
                        startListening(currentLanguage)
                    }
                }, 500)
            }
            return
        }

        if (!isTtsReady || textToSpeech == null) {
            _voiceState.value = VoiceState.IDLE
            if (_isVoiceSessionActive.value) {
                mainHandler.postDelayed({
                    if (_isVoiceSessionActive.value && _voiceState.value == VoiceState.IDLE) {
                        startListening(currentLanguage)
                    }
                }, 1000)
            }
            return
        }

        // Script-aware TTS language adaptation
        val hasDevanagari = trimmedText.any { it.code in 0x0900..0x097F }
        val targetLocale = if (hasDevanagari) Locale("hi", "IN") else Locale("en", "IN")
        val res = textToSpeech?.setLanguage(targetLocale)
        if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
            textToSpeech?.language = if (hasDevanagari) Locale("hi", "IN") else Locale.US
        }
        _voiceState.value = VoiceState.SPEAKING

        // Chunk if text is excessively long to prevent Android TTS buffer overflow
        val chunks = chunkTextForTts(trimmedText, 2500)
        val finalId = "swasthya_final_${System.currentTimeMillis()}"
        currentFinalUtteranceId = finalId

        for (i in chunks.indices) {
            val chunk = chunks[i]
            val utteranceId = if (i == chunks.lastIndex) finalId else "swasthya_chunk_${System.currentTimeMillis()}_$i"
            val params = Bundle()
            params.putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            val queueMode = if (i == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            textToSpeech?.speak(chunk, queueMode, params, utteranceId)
        }
    }

    fun chunkTextForTts(text: String, maxChunkSize: Int): List<String> {
        if (text.length <= maxChunkSize) return listOf(text)
        val chunks = mutableListOf<String>()
        var remaining = text
        while (remaining.length > maxChunkSize) {
            var splitIdx = remaining.lastIndexOf(". ", maxChunkSize)
            if (splitIdx == -1) splitIdx = remaining.lastIndexOf("। ", maxChunkSize)
            if (splitIdx == -1) splitIdx = remaining.lastIndexOf("\n", maxChunkSize)
            if (splitIdx == -1) splitIdx = remaining.lastIndexOf(" ", maxChunkSize)
            if (splitIdx == -1) splitIdx = maxChunkSize
            else splitIdx += 1

            chunks.add(remaining.substring(0, splitIdx).trim())
            remaining = remaining.substring(splitIdx).trim()
        }
        if (remaining.isNotBlank()) {
            chunks.add(remaining)
        }
        return chunks
    }

    fun repeatLast() {
        if (_lastSpokenText.value.isNotBlank()) {
            speak(_lastSpokenText.value, currentLanguage)
        }
    }

    fun stopSpeaking() {
        try {
            textToSpeech?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS", e)
        }
        if (_voiceState.value == VoiceState.SPEAKING) {
            _voiceState.value = VoiceState.IDLE
        }
    }

    fun toggleMute() {
        _isMuted.value = !_isMuted.value
        if (_isMuted.value) {
            stopSpeaking()
        }
    }

    fun startVoiceSession(language: AppLanguage = currentLanguage) {
        _isVoiceSessionActive.value = true
        startListening(language)
    }

    fun stopVoiceSession() {
        _isVoiceSessionActive.value = false
        stopListening()
        stopSpeaking()
    }

    fun toggleVoiceSession() {
        if (_isVoiceSessionActive.value) {
            stopVoiceSession()
        } else {
            startVoiceSession(currentLanguage)
        }
    }

    fun handleMicTap() {
        if (_voiceState.value == VoiceState.SPEAKING) {
            // User interrupted the assistant while speaking!
            stopSpeaking()
            if (_isVoiceSessionActive.value) {
                mainHandler.postDelayed({
                    if (_isVoiceSessionActive.value && _voiceState.value == VoiceState.IDLE) {
                        startListening(currentLanguage)
                    }
                }, 300)
            }
        } else if (_isVoiceSessionActive.value || _voiceState.value == VoiceState.LISTENING) {
            stopVoiceSession()
        } else {
            startVoiceSession(currentLanguage)
        }
    }

    fun toggleHandsFree() {
        toggleVoiceSession()
    }

    fun stopHandsFree() {
        stopVoiceSession()
    }

    private fun handleRecognizedText(text: String) {
        // First check for voice navigation commands
        val handled = checkVoiceCommands(text)
        if (!handled) {
            _voiceState.value = VoiceState.PROCESSING
            onSpeechRecognized(text)
        }
    }

    private fun checkVoiceCommands(text: String): Boolean {
        val lower = text.trim().lowercase()

        // Stop / Repeat commands
        if (lower in listOf("stop", "ruko", "ruk jao", "रुकें", "रुक जाओ", "बंद करो", "exit", "quit")) {
            stopVoiceSession()
            return true
        }

        if (lower in listOf("repeat", "say again", "phir se bolo", "दोहराएं", "फिर से बोलो")) {
            repeatLast()
            return true
        }

        // Delegate navigation commands (emergency, hospital, pharmacy, doctor, hindi, english, etc.)
        return onVoiceNavCommand(text)
    }

    fun release() {
        try {
            speechRecognizer?.destroy()
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing VoiceManager", e)
        }
    }
}
