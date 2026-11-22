package com.example.data.auth

enum class AuthProvider {
    GOOGLE,
    PHONE,
    EMAIL,
    GUEST
}

data class UserProfile(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val authProvider: AuthProvider,
    val createdAt: Long = System.currentTimeMillis()
)

data class AuthState(
    val isAuthenticated: Boolean = false,
    val isSkipped: Boolean = false,
    val user: UserProfile? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)
