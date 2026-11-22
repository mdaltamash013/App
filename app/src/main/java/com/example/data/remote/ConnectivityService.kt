package com.example.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

data class ConnectivityHealthReport(
    val isDeviceConnected: Boolean,
    val isBackendReachable: Boolean,
    val isAiServiceReachable: Boolean,
    val hasValidApiKey: Boolean,
    val activeModel: String,
    val lastLatencyMs: Long,
    val lastCheckTimestamp: Long = System.currentTimeMillis(),
    val failureReason: String? = null
)

object ConnectivityService {
    private const val TAG = "ConnectivityService"
    private const val HEALTH_PROBE_URL = "https://generativelanguage.googleapis.com"
    private const val FALLBACK_PROBE_URL = "https://www.google.com/generate_204"

    private val probeClient = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    fun isDeviceNetworkAvailable(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    suspend fun verifyRealConnectivity(context: Context): ConnectivityHealthReport = withContext(Dispatchers.IO) {
        val deviceConnected = isDeviceNetworkAvailable(context)
        if (!deviceConnected) {
            Log.d(TAG, "Device network is disconnected")
            return@withContext ConnectivityHealthReport(
                isDeviceConnected = false,
                isBackendReachable = false,
                isAiServiceReachable = false,
                hasValidApiKey = false,
                activeModel = GeminiService.PRIMARY_MODEL,
                lastLatencyMs = 0L,
                failureReason = "Device network is offline (no Wi-Fi or Cellular connection)"
            )
        }

        val apiKey = GeminiService.resolveApiKey()
        val hasKey = apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY"

        var backendReachable = false
        var latencyMs = 0L
        var failureReason: String? = null

        val startTime = System.currentTimeMillis()
        try {
            // Probe backend reachability
            val request = Request.Builder()
                .url(HEALTH_PROBE_URL)
                .head()
                .build()
            val response = probeClient.newCall(request).execute()
            latencyMs = System.currentTimeMillis() - startTime
            // 200, 301, 302, 404 all indicate server was reached
            backendReachable = response.code in 200..499
            response.close()
        } catch (e: Exception) {
            Log.w(TAG, "Health probe to primary backend failed: ${e.message}, trying fallback")
            try {
                val fallbackReq = Request.Builder().url(FALLBACK_PROBE_URL).head().build()
                val fallbackResp = probeClient.newCall(fallbackReq).execute()
                latencyMs = System.currentTimeMillis() - startTime
                backendReachable = fallbackResp.isSuccessful || fallbackResp.code == 204
                fallbackResp.close()
            } catch (fallbackEx: Exception) {
                latencyMs = System.currentTimeMillis() - startTime
                backendReachable = false
                failureReason = "Backend server unreachable: ${fallbackEx.localizedMessage ?: "Connection timed out"}"
                Log.e(TAG, "Both primary and fallback reachability checks failed", fallbackEx)
            }
        }

        if (!hasKey) {
            failureReason = "GEMINI_API_KEY is missing or configured as placeholder in .env"
        }

        val aiServiceReachable = backendReachable && hasKey

        ConnectivityHealthReport(
            isDeviceConnected = true,
            isBackendReachable = backendReachable,
            isAiServiceReachable = aiServiceReachable,
            hasValidApiKey = hasKey,
            activeModel = GeminiService.PRIMARY_MODEL,
            lastLatencyMs = latencyMs,
            failureReason = failureReason
        )
    }
}
