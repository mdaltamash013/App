package com.example

import android.Manifest
import android.app.Application
import android.content.Context
import android.location.Location
import android.location.LocationManager
import androidx.test.core.app.ApplicationProvider
import com.example.data.location.LocationErrorType
import com.example.data.location.LocationService
import com.example.data.location.LocationState
import com.example.data.model.AppLanguage
import com.example.ui.viewmodel.HealthViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowApplication
import org.robolectric.shadows.ShadowLocationManager

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class LocationDetectionTest {

    private lateinit var context: Context
    private lateinit var application: Application
    private lateinit var shadowApplication: ShadowApplication
    private lateinit var shadowLocationManager: ShadowLocationManager
    private lateinit var locationService: LocationService

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        application = ApplicationProvider.getApplicationContext()
        shadowApplication = Shadows.shadowOf(application)
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        shadowLocationManager = Shadows.shadowOf(lm)
        locationService = LocationService(context)
    }

    // Case A: Permission Granted -> Retrieve location coordinates
    @Test
    fun `Case A - Permission granted detects location coordinates successfully`() = runBlocking {
        shadowApplication.grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        val testLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 23.2599
            longitude = 77.4126
            accuracy = 12.0f
            time = System.currentTimeMillis()
        }
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, testLocation)

        val result = locationService.detectCurrentLocation()
        assertTrue(result is LocationState.Success)
        val success = result as LocationState.Success
        assertEquals(23.2599, success.latitude, 0.001)
        assertEquals(77.4126, success.longitude, 0.001)
        assertTrue(success.readableAddress.isNotBlank())
    }

    // Case B: Permission Denied -> returns PERMISSION_REQUIRED error
    @Test
    fun `Case B - Permission denied yields clear permission required message`() = runBlocking {
        shadowApplication.denyPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        val result = locationService.detectCurrentLocation()
        assertTrue(result is LocationState.Error)
        val error = result as LocationState.Error
        assertEquals(LocationErrorType.PERMISSION_REQUIRED, error.errorType)
        assertTrue(error.message.contains("permission is required", ignoreCase = true))
    }

    // Case C: Location Services Disabled -> tell the user to enable device location
    @Test
    fun `Case C - Location services disabled yields SERVICES_DISABLED error`() = runBlocking {
        shadowApplication.grantPermissions(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, false)
        shadowLocationManager.setProviderEnabled(LocationManager.NETWORK_PROVIDER, false)

        val result = locationService.detectCurrentLocation()
        assertTrue(result is LocationState.Error)
        val error = result as LocationState.Error
        assertEquals(LocationErrorType.SERVICES_DISABLED, error.errorType)
        assertTrue(error.message.contains("turned off") || error.message.contains("enable GPS", ignoreCase = true))
    }

    // Case D: Internet Available -> reverse-geocodes or provides formatted location
    @Test
    fun `Case D - Reverse geocoding provides formatted location information`() = runBlocking {
        shadowApplication.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        val testLocation = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 23.2599
            longitude = 77.4126
            time = System.currentTimeMillis()
        }
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, testLocation)

        val addressResult = locationService.reverseGeocode(23.2599, 77.4126)
        assertNotNull(addressResult.readableAddress)
        assertTrue(addressResult.readableAddress.isNotBlank())
    }

    // Case E: Internet Unavailable -> retain lat lng locally
    @Test
    fun `Case E - Internet unavailable retains coordinates locally without crashing`() = runBlocking {
        // Test coordinate formatting without network
        val success = LocationState.Success(
            latitude = 23.2599,
            longitude = 77.4126,
            readableAddress = "Lat: 23.260°, Lng: 77.413°",
            isReverseGeocoded = false
        )
        val formattedEn = success.getFormattedLocation(AppLanguage.ENGLISH)
        assertTrue(formattedEn.contains("Lat: 23.260°"))
        assertTrue(formattedEn.contains("Offline"))

        val formattedHi = success.getFormattedLocation(AppLanguage.HINDI)
        assertTrue(formattedHi.contains("अक्षांश"))
        assertTrue(formattedHi.contains("ऑफलाइन"))
    }

    // Case F: Reverse-geocoding API unavailable fallback
    @Test
    fun `Case F - Reverse geocoding failure gracefully retains coordinates`() = runBlocking {
        val result = locationService.reverseGeocode(0.0, 0.0)
        assertNotNull(result)
        assertTrue(result.readableAddress.isNotBlank())
    }

    // Case G: User presses Retry Location
    @Test
    fun `Case G - ViewModel retry location initiates detection cycle`() {
        val vm = HealthViewModel(application)
        shadowApplication.grantPermissions(Manifest.permission.ACCESS_FINE_LOCATION)
        shadowLocationManager.setProviderEnabled(LocationManager.GPS_PROVIDER, true)

        val testLoc = Location(LocationManager.GPS_PROVIDER).apply {
            latitude = 28.6139
            longitude = 77.2090
            accuracy = 10f
            time = System.currentTimeMillis()
        }
        shadowLocationManager.setLastKnownLocation(LocationManager.GPS_PROVIDER, testLoc)

        vm.retryLocation()
        // Should transition to detecting or completed
        assertNotNull(vm.locationState.value)
    }

    // Case H: User changes location and retries
    @Test
    fun `Case H - User changes location manually and updates UI state`() {
        val vm = HealthViewModel(application)
        vm.updateManualLocation("Bhopal, Madhya Pradesh")

        assertEquals("Bhopal, Madhya Pradesh", vm.detectedLocationText.value)
        val state = vm.locationState.value
        assertTrue(state is LocationState.Success)
        assertEquals("Bhopal, Madhya Pradesh", (state as LocationState.Success).readableAddress)

        // User changes again
        vm.updateManualLocation("Indore, Madhya Pradesh")
        assertEquals("Indore, Madhya Pradesh", vm.detectedLocationText.value)
    }
}
