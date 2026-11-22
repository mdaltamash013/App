package com.example.data.auth

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.UUID

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _authState = MutableStateFlow(loadInitialState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private fun loadInitialState(): AuthState {
        val isAuthenticated = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
        val isSkipped = prefs.getBoolean(KEY_IS_SKIPPED, false)

        val user = if (isAuthenticated) {
            val id = prefs.getString(KEY_USER_ID, UUID.randomUUID().toString()) ?: UUID.randomUUID().toString()
            val name = prefs.getString(KEY_USER_NAME, "User") ?: "User"
            val email = prefs.getString(KEY_USER_EMAIL, null)
            val phone = prefs.getString(KEY_USER_PHONE, null)
            val providerStr = prefs.getString(KEY_AUTH_PROVIDER, AuthProvider.GOOGLE.name) ?: AuthProvider.GOOGLE.name
            val provider = try {
                AuthProvider.valueOf(providerStr)
            } catch (e: Exception) {
                AuthProvider.GOOGLE
            }
            UserProfile(
                id = id,
                name = name,
                email = email,
                phone = phone,
                authProvider = provider
            )
        } else null

        return AuthState(
            isAuthenticated = isAuthenticated,
            isSkipped = isSkipped,
            user = user
        )
    }

    fun isInitialWelcomeRequired(): Boolean {
        val isAuthenticated = prefs.getBoolean(KEY_IS_AUTHENTICATED, false)
        val isSkipped = prefs.getBoolean(KEY_IS_SKIPPED, false)
        return !isAuthenticated && !isSkipped
    }

    suspend fun skipAuth() = withContext(Dispatchers.IO) {
        prefs.edit()
            .putBoolean(KEY_IS_SKIPPED, true)
            .apply()
        _authState.value = _authState.value.copy(
            isSkipped = true,
            errorMessage = null,
            successMessage = "Guest access granted"
        )
    }

    suspend fun loginWithGoogle(
        preferredName: String = "Google User",
        preferredEmail: String = "user@lifecare247.ai"
    ): Result<UserProfile> = withContext(Dispatchers.IO) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        val profile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = preferredName,
            email = preferredEmail,
            authProvider = AuthProvider.GOOGLE
        )
        persistUser(profile)
        _authState.value = AuthState(
            isAuthenticated = true,
            isSkipped = false,
            user = profile,
            isLoading = false,
            successMessage = "Signed in successfully with Google"
        )
        Result.success(profile)
    }

    suspend fun sendPhoneOtp(phone: String): Result<String> = withContext(Dispatchers.IO) {
        val cleanPhone = phone.trim().replace("\\s+".toRegex(), "")
        if (cleanPhone.length < 10) {
            return@withContext Result.failure(IllegalArgumentException("Please enter a valid 10-digit mobile number"))
        }
        Result.success("OTP sent successfully to +91 $cleanPhone")
    }

    suspend fun verifyPhoneOtp(phone: String, otp: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        val cleanPhone = phone.trim().replace("\\s+".toRegex(), "")
        val cleanOtp = otp.trim()

        if (cleanOtp.length != 6 || !cleanOtp.all { it.isDigit() }) {
            _authState.value = _authState.value.copy(
                isLoading = false,
                errorMessage = "Please enter a valid 6-digit verification code"
            )
            return@withContext Result.failure(IllegalArgumentException("Invalid 6-digit OTP"))
        }

        val profile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = "Patient +91-${cleanPhone.takeLast(4)}",
            phone = cleanPhone,
            authProvider = AuthProvider.PHONE
        )
        persistUser(profile)
        _authState.value = AuthState(
            isAuthenticated = true,
            isSkipped = false,
            user = profile,
            isLoading = false,
            successMessage = "Phone verified successfully"
        )
        Result.success(profile)
    }

    suspend fun loginWithEmail(email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        val cleanEmail = email.trim()

        if (!isValidEmail(cleanEmail)) {
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Please enter a valid email address")
            return@withContext Result.failure(IllegalArgumentException("Invalid email"))
        }
        if (password.length < 6) {
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Password must be at least 6 characters")
            return@withContext Result.failure(IllegalArgumentException("Password too short"))
        }

        val profile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = cleanEmail.substringBefore("@").replaceFirstChar { it.uppercase() },
            email = cleanEmail,
            authProvider = AuthProvider.EMAIL
        )
        persistUser(profile)
        _authState.value = AuthState(
            isAuthenticated = true,
            isSkipped = false,
            user = profile,
            isLoading = false,
            successMessage = "Welcome back!"
        )
        Result.success(profile)
    }

    suspend fun registerWithEmail(name: String, email: String, password: String): Result<UserProfile> = withContext(Dispatchers.IO) {
        _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)
        val cleanName = name.trim()
        val cleanEmail = email.trim()

        if (cleanName.length < 2) {
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Please enter your full name")
            return@withContext Result.failure(IllegalArgumentException("Name too short"))
        }
        if (!isValidEmail(cleanEmail)) {
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Please enter a valid email address")
            return@withContext Result.failure(IllegalArgumentException("Invalid email"))
        }
        if (password.length < 6) {
            _authState.value = _authState.value.copy(isLoading = false, errorMessage = "Password must be at least 6 characters")
            return@withContext Result.failure(IllegalArgumentException("Password too short"))
        }

        val profile = UserProfile(
            id = UUID.randomUUID().toString(),
            name = cleanName,
            email = cleanEmail,
            authProvider = AuthProvider.EMAIL
        )
        persistUser(profile)
        _authState.value = AuthState(
            isAuthenticated = true,
            isSkipped = false,
            user = profile,
            isLoading = false,
            successMessage = "Account created successfully"
        )
        Result.success(profile)
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
        _authState.value = AuthState(
            isAuthenticated = false,
            isSkipped = false,
            user = null
        )
    }

    fun clearMessages() {
        _authState.value = _authState.value.copy(errorMessage = null, successMessage = null)
    }

    private fun persistUser(user: UserProfile) {
        prefs.edit()
            .putBoolean(KEY_IS_AUTHENTICATED, true)
            .putBoolean(KEY_IS_SKIPPED, false)
            .putString(KEY_USER_ID, user.id)
            .putString(KEY_USER_NAME, user.name)
            .putString(KEY_USER_EMAIL, user.email)
            .putString(KEY_USER_PHONE, user.phone)
            .putString(KEY_AUTH_PROVIDER, user.authProvider.name)
            .apply()
    }

    private fun isValidEmail(email: String): Boolean {
        if (email.isBlank()) return false
        val regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return regex.matches(email)
    }

    companion object {
        private const val PREFS_NAME = "swasthyasathi_auth_prefs"
        private const val KEY_IS_AUTHENTICATED = "is_authenticated"
        private const val KEY_IS_SKIPPED = "is_skipped"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_PHONE = "user_phone"
        private const val KEY_AUTH_PROVIDER = "auth_provider"
    }
}
