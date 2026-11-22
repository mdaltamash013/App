package com.example.data.location

import com.example.data.model.AppLanguage

enum class LocationPermissionStatus {
    NOT_REQUESTED,
    GRANTED,
    DENIED,
    PERMANENTLY_DENIED
}

enum class LocationErrorType {
    PERMISSION_REQUIRED,
    SERVICES_DISABLED,
    TIMEOUT,
    UNAVAILABLE,
    UNSUPPORTED
}

sealed class LocationState {
    object Idle : LocationState()

    object Detecting : LocationState()

    data class Success(
        val latitude: Double,
        val longitude: Double,
        val readableAddress: String,
        val isReverseGeocoded: Boolean,
        val city: String? = null,
        val state: String? = null,
        val country: String? = null,
        val accuracy: Float? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : LocationState() {
        fun getFormattedLocation(language: AppLanguage): String {
            return if (isReverseGeocoded) {
                readableAddress
            } else {
                if (language == AppLanguage.HINDI) {
                    "अक्षांश: ${"%.3f".format(latitude)}, देशांतर: ${"%.3f".format(longitude)} (ऑफलाइन)"
                } else {
                    "Lat: ${"%.3f".format(latitude)}°, Lng: ${"%.3f".format(longitude)}° (Offline)"
                }
            }
        }
    }

    data class Error(
        val errorType: LocationErrorType,
        val message: String,
        val canRetry: Boolean = true,
        val lastCoordinates: Pair<Double, Double>? = null
    ) : LocationState()
}
