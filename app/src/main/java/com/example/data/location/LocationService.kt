package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.remote.ConnectivityService
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume

class LocationService(private val context: Context) {

    companion object {
        private const val TAG = "LocationService"
        private const val TIMEOUT_MS = 12_000L
        private const val NOMINATIM_URL = "https://nominatim.openstreetmap.org/reverse?format=json&zoom=10&addressdetails=1"

        private val httpClient = OkHttpClient.Builder()
            .connectTimeout(6, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    private val fusedClient by lazy {
        try {
            LocationServices.getFusedLocationProviderClient(context)
        } catch (e: Exception) {
            null
        }
    }

    private val locationManager by lazy {
        context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
    }

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isLocationServicesEnabled(): Boolean {
        val lm = locationManager ?: return false
        val gpsEnabled = try { lm.isProviderEnabled(LocationManager.GPS_PROVIDER) } catch (e: Exception) { false }
        val netEnabled = try { lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { false }
        return gpsEnabled || netEnabled
    }

    suspend fun detectCurrentLocation(): LocationState = withContext(Dispatchers.IO) {
        Log.i(TAG, "LOCATION_REQUEST_STARTED")

        // 1. Permission Check
        if (!hasLocationPermission()) {
            Log.i(TAG, "LOCATION_PERMISSION_STATUS: DENIED")
            Log.w(TAG, "LOCATION_ERROR: PERMISSION_REQUIRED - Location permission is required to detect nearby medical facilities")
            return@withContext LocationState.Error(
                errorType = LocationErrorType.PERMISSION_REQUIRED,
                message = "Location permission is required to find nearby healthcare centers and emergency aid."
            )
        }
        Log.i(TAG, "LOCATION_PERMISSION_STATUS: GRANTED")

        // 2. Location Services Check
        if (!isLocationServicesEnabled()) {
            Log.e(TAG, "LOCATION_ERROR: SERVICES_DISABLED - Device GPS/Location provider is turned off")
            return@withContext LocationState.Error(
                errorType = LocationErrorType.SERVICES_DISABLED,
                message = "Device location services are turned off. Please enable GPS in device settings."
            )
        }

        // 3. Acquire Coordinates
        val location = withTimeoutOrNull(TIMEOUT_MS) {
            obtainDeviceLocation()
        }

        if (location == null) {
            Log.e(TAG, "LOCATION_ERROR: TIMEOUT - Failed to obtain device coordinates within timeout limit")
            return@withContext LocationState.Error(
                errorType = LocationErrorType.TIMEOUT,
                message = "Location detection timed out. Please ensure GPS is active and retry."
            )
        }

        // Safe developer log: Do not expose precise coordinates in production logs
        val providerName = location.provider ?: "system"
        val accuracy = if (location.hasAccuracy()) "${location.accuracy.toInt()}m" else "approx"
        Log.i(TAG, "LOCATION_COORDINATES_RECEIVED (provider=$providerName, accuracy=$accuracy)")

        val lat = location.latitude
        val lng = location.longitude

        // 4. Reverse Geocoding
        val addressResult = reverseGeocode(lat, lng)

        return@withContext LocationState.Success(
            latitude = lat,
            longitude = lng,
            readableAddress = addressResult.readableAddress,
            isReverseGeocoded = addressResult.isReverseGeocoded,
            city = addressResult.city,
            state = addressResult.state,
            country = addressResult.country,
            accuracy = if (location.hasAccuracy()) location.accuracy else null,
            timestamp = System.currentTimeMillis()
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun obtainDeviceLocation(): Location? {
        val lm = locationManager

        // Quick check: If device/system already has a valid recent location fix (under 2 minutes), use it immediately
        try {
            val gpsLast = try { lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
            if (gpsLast != null && (System.currentTimeMillis() - gpsLast.time) < 120_000L) {
                return gpsLast
            }
            val netLast = try { lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
            if (netLast != null && (System.currentTimeMillis() - netLast.time) < 120_000L) {
                return netLast
            }
        } catch (e: Exception) {
            // continue
        }

        // Attempt 1: FusedLocationProviderClient (High Accuracy) with 3.5s bounded timeout
        try {
            val fused = fusedClient
            if (fused != null) {
                val fusedLocation = withTimeoutOrNull(3500L) {
                    val cts = CancellationTokenSource()
                    val current = fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
                    if (current != null) return@withTimeoutOrNull current

                    val last = fused.lastLocation.await()
                    if (last != null && (System.currentTimeMillis() - last.time) < 300_000L) {
                        return@withTimeoutOrNull last
                    }
                    null
                }
                if (fusedLocation != null) return fusedLocation
            }
        } catch (e: Exception) {
            Log.w(TAG, "FusedLocationProvider failed: ${e.message}, falling back to LocationManager")
        }

        // Attempt 2: Standard LocationManager (GPS then Network)
        if (lm != null) {
            try {
                val gpsLast = try { lm.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (e: Exception) { null }
                if (gpsLast != null) return gpsLast

                val netLast = try { lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (e: Exception) { null }
                if (netLast != null) return netLast

                // Fresh single update request
                return requestSingleLocationUpdate(lm)
            } catch (e: Exception) {
                Log.e(TAG, "LocationManager fallback failed", e)
            }
        }
        return null
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocationUpdate(lm: LocationManager): Location? = suspendCancellableCoroutine { cont ->
        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        if (provider == null) {
            cont.resume(null)
            return@suspendCancellableCoroutine
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(loc: Location) {
                try {
                    lm.removeUpdates(this)
                } catch (e: Exception) {
                    // ignore
                }
                if (cont.isActive) cont.resume(loc)
            }
            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {
                try { lm.removeUpdates(this) } catch (e: Exception) {}
                if (cont.isActive) cont.resume(null)
            }
        }

        try {
            lm.requestSingleUpdate(provider, listener, null)
            cont.invokeOnCancellation {
                try { lm.removeUpdates(listener) } catch (e: Exception) {}
            }
        } catch (e: Exception) {
            if (cont.isActive) cont.resume(null)
        }
    }

    data class AddressResult(
        val readableAddress: String,
        val isReverseGeocoded: Boolean,
        val city: String? = null,
        val state: String? = null,
        val country: String? = null
    )

    suspend fun reverseGeocode(lat: Double, lng: Double): AddressResult = withContext(Dispatchers.IO) {
        val isOnline = ConnectivityService.isDeviceNetworkAvailable(context)
        if (!isOnline) {
            Log.w(TAG, "LOCATION_ERROR: Network offline for reverse-geocoding, retaining coordinates locally")
            val coordText = "Lat: ${"%.3f".format(lat)}°, Lng: ${"%.3f".format(lng)}°"
            return@withContext AddressResult(
                readableAddress = coordText,
                isReverseGeocoded = false
            )
        }

        Log.i(TAG, "LOCATION_REVERSE_GEOCODING_STARTED")

        // 1. Try Android Platform Geocoder
        if (Geocoder.isPresent()) {
            try {
                val address = resolveGeocoderAddress(lat, lng)
                if (address != null) {
                    val city = address.locality ?: address.subAdminArea ?: address.subLocality
                    val state = address.adminArea
                    val country = address.countryName

                    val readable = formatAddress(city, state, country)
                    if (readable.isNotBlank()) {
                        Log.i(TAG, "LOCATION_REVERSE_GEOCODING_SUCCESS (resolved: $readable)")
                        return@withContext AddressResult(
                            readableAddress = readable,
                            isReverseGeocoded = true,
                            city = city,
                            state = state,
                            country = country
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Platform Geocoder lookup failed: ${e.message}, attempting network geocoding fallback")
            }
        }

        // 2. Fallback: Network HTTP Reverse Geocoding
        try {
            val fallbackAddress = resolveHttpReverseGeocoding(lat, lng)
            if (fallbackAddress != null) {
                Log.i(TAG, "LOCATION_REVERSE_GEOCODING_SUCCESS (HTTP fallback: ${fallbackAddress.readableAddress})")
                return@withContext fallbackAddress
            }
        } catch (e: Exception) {
            Log.e(TAG, "LOCATION_ERROR: Reverse-geocoding network fallback failed: ${e.message}")
        }

        // 3. Fallback to coordinate representation
        Log.w(TAG, "LOCATION_ERROR: All reverse geocoding attempts exhausted, using local coordinates")
        val coordText = "Lat: ${"%.3f".format(lat)}°, Lng: ${"%.3f".format(lng)}°"
        return@withContext AddressResult(
            readableAddress = coordText,
            isReverseGeocoded = false
        )
    }

    @Suppress("DEPRECATION")
    private suspend fun resolveGeocoderAddress(lat: Double, lng: Double): Address? {
        val geocoder = Geocoder(context, Locale.getDefault())
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            suspendCancellableCoroutine { cont ->
                geocoder.getFromLocation(lat, lng, 1, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        cont.resume(addresses.firstOrNull())
                    }
                    override fun onError(errorMessage: String?) {
                        Log.w(TAG, "Geocoder error callback: $errorMessage")
                        cont.resume(null)
                    }
                })
            }
        } else {
            val list = geocoder.getFromLocation(lat, lng, 1)
            list?.firstOrNull()
        }
    }

    private fun resolveHttpReverseGeocoding(lat: Double, lng: Double): AddressResult? {
        val url = "$NOMINATIM_URL&lat=$lat&lon=$lng"
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "LifeCare-24-7-HealthcareApp/1.0 (lifecare247@example.com)")
            .build()

        val response = httpClient.newCall(request).execute()
        if (!response.isSuccessful) {
            response.close()
            return null
        }

        val body = response.body?.string() ?: return null
        val json = JSONObject(body)
        val addressObj = json.optJSONObject("address") ?: return null

        val city = addressObj.optString("city", "").ifBlank {
            addressObj.optString("town", "").ifBlank {
                addressObj.optString("village", "").ifBlank {
                    addressObj.optString("county", "")
                }
            }
        }.ifBlank { null }

        val state = addressObj.optString("state", "").ifBlank { null }
        val country = addressObj.optString("country", "").ifBlank { null }

        val readable = formatAddress(city, state, country)
        if (readable.isBlank()) return null

        return AddressResult(
            readableAddress = readable,
            isReverseGeocoded = true,
            city = city,
            state = state,
            country = country
        )
    }

    private fun formatAddress(city: String?, state: String?, country: String?): String {
        val parts = mutableListOf<String>()
        if (!city.isNullOrBlank()) parts.add(city.trim())
        if (!state.isNullOrBlank()) parts.add(state.trim())
        if (parts.isEmpty() && !country.isNullOrBlank()) parts.add(country.trim())
        return parts.joinToString(", ")
    }
}
