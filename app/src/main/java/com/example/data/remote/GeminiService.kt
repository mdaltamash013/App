package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.AppLanguage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

enum class AiErrorType {
    API_KEY_MISSING,
    INVALID_API_KEY,
    MODEL_UNAVAILABLE,
    QUOTA_EXCEEDED,
    HTTP_ERROR,
    NETWORK_TIMEOUT,
    NETWORK_UNAVAILABLE,
    EMPTY_RESPONSE,
    UNKNOWN
}

data class AiResponseResult(
    val success: Boolean,
    val text: String? = null,
    val modelUsed: String? = null,
    val errorType: AiErrorType? = null,
    val errorCode: Int? = null,
    val errorMessage: String? = null,
    val developerLog: String = "",
    val latencyMs: Long = 0L,
    val researchSources: List<ResearchReference> = emptyList(),
    val isResearchIncluded: Boolean = false
)

object GeminiService {
    private const val TAG = "GeminiService"
    const val PRIMARY_MODEL = "gemini-3.8-flash"
    private val CANDIDATE_MODELS = listOf("gemini-3.8-flash", "gemma-4-26b-a4b-it", "gemini-3.5-flash")
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    // Store the last request status for developer diagnostics panel
    var lastDiagnosticResult: AiResponseResult? = null
        private set

    fun resolveApiKey(): String {
        val buildKey = BuildConfig.GEMINI_API_KEY.trim()
        if (buildKey.isNotBlank() && buildKey != "MY_GEMINI_API_KEY") {
            return buildKey
        }
        val envKey = System.getenv("GEMINI_API_KEY")?.trim() ?: ""
        if (envKey.isNotBlank() && envKey != "MY_GEMINI_API_KEY") {
            return envKey
        }
        return ""
    }

    private fun sanitizeHistory(history: List<Pair<String, String>>): List<Pair<String, String>> {
        val sanitized = mutableListOf<Pair<String, String>>()
        for ((role, text) in history) {
            val trimmed = text.trim()
            if (trimmed.isBlank()) continue
            val normalizedRole = if (role == "user") "user" else "model"
            if (sanitized.isNotEmpty() && sanitized.last().first == normalizedRole) {
                val prev = sanitized.removeAt(sanitized.size - 1)
                sanitized.add(normalizedRole to "${prev.second}\n\n$trimmed")
            } else {
                sanitized.add(normalizedRole to trimmed)
            }
        }
        while (sanitized.isNotEmpty() && sanitized.first().first != "user") {
            sanitized.removeAt(0)
        }
        while (sanitized.isNotEmpty() && sanitized.last().first == "user") {
            sanitized.removeAt(sanitized.size - 1)
        }
        return sanitized
    }

    suspend fun generateHealthGuidance(
        userQuery: String,
        conversationHistory: List<Pair<String, String>>,
        language: AppLanguage,
        researchContext: String? = null,
        researchSources: List<ResearchReference> = emptyList()
    ): AiResponseResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val apiKey = resolveApiKey()

        if (apiKey.isBlank()) {
            val devLog = "DEVELOPER ERROR: GEMINI_API_KEY is missing or contains placeholder 'MY_GEMINI_API_KEY'. Please configure your key in AI Studio Secrets or .env file."
            Log.e(TAG, devLog)
            val result = AiResponseResult(
                success = false,
                errorType = AiErrorType.API_KEY_MISSING,
                errorMessage = "API key not configured",
                developerLog = devLog,
                latencyMs = System.currentTimeMillis() - startTime
            )
            lastDiagnosticResult = result
            return@withContext result
        }

        // Try primary model first, cascade to fallbacks if high demand (503), quota (429), or timeout occurs
        var lastErrorResult: AiResponseResult? = null

        for (model in CANDIDATE_MODELS) {
            Log.d(TAG, "Attempting AI request with model: $model")
            val attemptResult = executeGenerateRequest(
                model = model,
                apiKey = apiKey,
                userQuery = userQuery,
                conversationHistory = conversationHistory,
                language = language,
                researchContext = researchContext,
                researchSources = researchSources,
                startTime = startTime
            )

            if (attemptResult.success) {
                Log.i(TAG, "AI request successful with model: $model in ${attemptResult.latencyMs}ms")
                lastDiagnosticResult = attemptResult
                return@withContext attemptResult
            }

            // If it's a temporary error (503, 429, timeout), retry once after a short backoff before cascading
            val isTemporary = attemptResult.errorCode in listOf(503, 429, 500) || attemptResult.errorType == AiErrorType.NETWORK_TIMEOUT
            if (isTemporary && model == PRIMARY_MODEL) {
                Log.w(TAG, "Temporary failure on $model (${attemptResult.errorMessage}). Retrying once after 600ms...")
                delay(600)
                val retryResult = executeGenerateRequest(
                    model = model,
                    apiKey = apiKey,
                    userQuery = userQuery,
                    conversationHistory = conversationHistory,
                    language = language,
                    researchContext = researchContext,
                    researchSources = researchSources,
                    startTime = startTime
                )
                if (retryResult.success) {
                    Log.i(TAG, "Retry on $model succeeded!")
                    lastDiagnosticResult = retryResult
                    return@withContext retryResult
                }
            }

            lastErrorResult = attemptResult
            Log.w(TAG, "Model $model failed: ${attemptResult.errorMessage}. Trying next available model...")
        }

        val finalResult = lastErrorResult ?: AiResponseResult(
            success = false,
            errorType = AiErrorType.UNKNOWN,
            errorMessage = "All AI models were unavailable",
            developerLog = "All candidate models failed: ${CANDIDATE_MODELS.joinToString()}",
            latencyMs = System.currentTimeMillis() - startTime
        )
        lastDiagnosticResult = finalResult
        finalResult
    }

    private fun executeGenerateRequest(
        model: String,
        apiKey: String,
        userQuery: String,
        conversationHistory: List<Pair<String, String>>,
        language: AppLanguage,
        researchContext: String?,
        researchSources: List<ResearchReference>,
        startTime: Long
    ): AiResponseResult {
        return try {
            val systemInstruction = buildSystemPrompt(language, researchContext)
            val rootJson = JSONObject()

            // System instruction
            val sysContent = JSONObject()
            val sysParts = JSONArray().put(JSONObject().put("text", systemInstruction))
            sysContent.put("parts", sysParts)
            rootJson.put("systemInstruction", sysContent)

            // Multi-turn contents array with sanitized conversation history
            val contentsArray = JSONArray()
            val sanitizedTurns = sanitizeHistory(conversationHistory)
            for ((role, text) in sanitizedTurns.takeLast(10)) {
                val turnObj = JSONObject()
                turnObj.put("role", role)
                turnObj.put("parts", JSONArray().put(JSONObject().put("text", text)))
                contentsArray.put(turnObj)
            }

            // Current user query
            val currentTurn = JSONObject()
            currentTurn.put("role", "user")
            currentTurn.put("parts", JSONArray().put(JSONObject().put("text", userQuery)))
            contentsArray.put(currentTurn)

            rootJson.put("contents", contentsArray)

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            genConfig.put("maxOutputTokens", 600)
            rootJson.put("generationConfig", genConfig)

            val url = "$BASE_URL/$model:generateContent?key=$apiKey"
            val requestBody = rootJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val callStart = System.currentTimeMillis()
            val response = client.newCall(request).execute()
            val latencyMs = System.currentTimeMillis() - callStart
            val code = response.code

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val devLog = "HTTP $code from $model: $errBody"
                Log.w(TAG, devLog)

                val errType = when (code) {
                    401 -> AiErrorType.INVALID_API_KEY
                    403 -> AiErrorType.INVALID_API_KEY
                    404 -> AiErrorType.MODEL_UNAVAILABLE
                    429 -> AiErrorType.QUOTA_EXCEEDED
                    503 -> AiErrorType.MODEL_UNAVAILABLE
                    else -> AiErrorType.HTTP_ERROR
                }

                val userFriendlyMsg = when (code) {
                    401, 403 -> "API authentication failed. Please verify API key."
                    429 -> "Rate limit reached. Please wait a moment."
                    503 -> "Model is temporarily busy due to high demand."
                    else -> "Server error ($code)."
                }

                return AiResponseResult(
                    success = false,
                    modelUsed = model,
                    errorType = errType,
                    errorCode = code,
                    errorMessage = userFriendlyMsg,
                    developerLog = devLog,
                    latencyMs = latencyMs
                )
            }

            val respBody = response.body?.string() ?: ""
            if (respBody.isBlank()) {
                return AiResponseResult(
                    success = false,
                    modelUsed = model,
                    errorType = AiErrorType.EMPTY_RESPONSE,
                    errorMessage = "Empty response received from AI model",
                    developerLog = "Empty body with HTTP 200 from $model",
                    latencyMs = latencyMs
                )
            }

            val respJson = JSONObject(respBody)
            val candidates = respJson.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return AiResponseResult(
                    success = false,
                    modelUsed = model,
                    errorType = AiErrorType.EMPTY_RESPONSE,
                    errorMessage = "No text generated by AI model",
                    developerLog = "No candidates in response JSON: $respBody",
                    latencyMs = latencyMs
                )
            }

            val candidate = candidates.getJSONObject(0)
            val content = candidate.optJSONObject("content")
            val parts = content?.optJSONArray("parts")

            if (parts == null || parts.length() == 0) {
                return AiResponseResult(
                    success = false,
                    modelUsed = model,
                    errorType = AiErrorType.EMPTY_RESPONSE,
                    errorMessage = "AI response was blank",
                    developerLog = "Candidate had no content parts",
                    latencyMs = latencyMs
                )
            }

            val textBuilder = StringBuilder()
            for (i in 0 until parts.length()) {
                val part = parts.optJSONObject(i) ?: continue
                if (part.optBoolean("thought", false)) continue
                val text = part.optString("text", "")
                if (text.isNotBlank()) {
                    if (textBuilder.isNotEmpty()) textBuilder.append("\n")
                    textBuilder.append(text)
                }
            }

            val resultText = textBuilder.toString().trim()
            if (resultText.isBlank()) {
                return AiResponseResult(
                    success = false,
                    modelUsed = model,
                    errorType = AiErrorType.EMPTY_RESPONSE,
                    errorMessage = "AI generated empty content",
                    developerLog = "Parsed text parts were empty",
                    latencyMs = latencyMs
                )
            }

            AiResponseResult(
                success = true,
                text = resultText,
                modelUsed = model,
                developerLog = "HTTP 200 OK from $model in ${latencyMs}ms",
                latencyMs = latencyMs,
                researchSources = researchSources,
                isResearchIncluded = researchSources.isNotEmpty()
            )
        } catch (e: SocketTimeoutException) {
            val devLog = "Network timeout calling $model: ${e.message}"
            Log.e(TAG, devLog)
            AiResponseResult(
                success = false,
                modelUsed = model,
                errorType = AiErrorType.NETWORK_TIMEOUT,
                errorMessage = "Connection timed out. Please check your internet connection.",
                developerLog = devLog,
                latencyMs = System.currentTimeMillis() - startTime
            )
        } catch (e: IOException) {
            val devLog = "Network IO error calling $model: ${e.message}"
            Log.e(TAG, devLog)
            AiResponseResult(
                success = false,
                modelUsed = model,
                errorType = AiErrorType.NETWORK_UNAVAILABLE,
                errorMessage = "Network error. Unable to reach AI server.",
                developerLog = devLog,
                latencyMs = System.currentTimeMillis() - startTime
            )
        } catch (e: Exception) {
            val devLog = "Unexpected error calling $model: ${e.localizedMessage ?: e.javaClass.simpleName}"
            Log.e(TAG, devLog, e)
            AiResponseResult(
                success = false,
                modelUsed = model,
                errorType = AiErrorType.UNKNOWN,
                errorMessage = "Unexpected error occurred.",
                developerLog = devLog,
                latencyMs = System.currentTimeMillis() - startTime
            )
        }
    }

    private fun buildSystemPrompt(language: AppLanguage, researchContext: String?): String {
        val basePrompt = """
            You are LifeCare 24/7, a warm, caring, and trustworthy health companion. When introducing yourself or when asked who you are, introduce yourself using: "Hi, I'm your LifeCare 24/7 health assistant." You talk to the user naturally, comfortably, and warmly — like a helpful friend who genuinely understands and cares about them, while behaving responsibly as a healthcare assistant.

            CORE PERSONALITY & TONE:
            1. Friendly & Warm: Sound approachable, conversational, and caring. Never sound robotic, overly formal, clinical, or like a scripted IVR phone tree. Say things like: "Yeah, that sounds uncomfortable. Tell me a little more — have you been feeling tired since today, or has it been going on for a few days?"
            2. Empathy First: Always show genuine empathy when the user describes discomfort, pain, fatigue, stress, fear, or confusion.
            3. Natural Conversational Transitions: Use natural transitions such as "Okay, I understand", "Got it", "That sounds uncomfortable", "Let me ask you one more thing".
            4. Voice-Optimized Conciseness: Your response will be spoken aloud to the user over voice. Keep answers concise, natural, and comfortable to listen to (typically 2 to 4 sentences). Never output huge walls of text, dense bulleted lists, or raw markdown headers that sound awkward when read aloud.
            5. Simple Language: Avoid complicated medical jargon. Use clear everyday terms (e.g., "loose motions or upset stomach" instead of "acute gastroenteritis").

            LANGUAGE & HINGLISH SUPPORT:
            - Seamlessly support English, Hindi, and Hinglish. Match the user's language and speaking style naturally:
              - If the user speaks/types in English -> reply in warm conversational English.
              - If the user speaks/types in Hindi (Devanagari) -> reply in warm conversational Hindi.
              - If the user speaks/types in Hinglish (Roman Hindi/English mix) -> reply naturally in friendly Hinglish.

            HEALTHCARE SAFETY:
            - You are an empathetic AI companion, NOT a doctor. Do not pretend to be a doctor or claim certainty about a diagnosis.
            - Never tell the user that a serious condition is definitely present, and never advise stopping prescribed medication without appropriate medical guidance.
            - When symptoms indicate an emergency (severe crushing chest pain, difficulty breathing, sudden face drooping / slurred speech / paralysis, severe uncontrolled bleeding, loss of consciousness):
              Clearly and calmly recommend seeking immediate professional/emergency medical help or calling 108 right away, without causing panic.
            - For simple or general non-health questions (e.g., "What is Python?", "What is Java?", "Two plus two?", "How are you?"):
              Give a short, friendly, accurate answer in 1-2 sentences without forcing unsolicited medical advice.
        """.trimIndent()

        return if (!researchContext.isNullOrBlank()) {
            "$basePrompt\n\n$researchContext"
        } else {
            basePrompt
        }
    }

    suspend fun executeDiagnosticTestPrompt(): AiResponseResult = withContext(Dispatchers.IO) {
        val testPrompt = "Hello! Please confirm you are online and working properly in one sentence."
        generateHealthGuidance(
            userQuery = testPrompt,
            conversationHistory = emptyList(),
            language = AppLanguage.ENGLISH
        )
    }
}
