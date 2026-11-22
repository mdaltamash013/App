package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.EditLocation
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.FacilityType
import com.example.data.model.HealthcareFacility
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun FacilitiesScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val facilities by viewModel.facilities.collectAsState()
    val selectedType by viewModel.selectedFacilityType.collectAsState()
    val locationState by viewModel.locationState.collectAsState()
    val detectedLocationText by viewModel.detectedLocationText.collectAsState()

    var showManualLocationDialog by remember { mutableStateOf(false) }

    fun dialPhone(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openDirections(lat: Double, lng: Double, label: String) {
        try {
            val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=${Uri.encode(label)}")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
            context.startActivity(mapIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        // Location Header with Manual Fallback
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Current Location",
                        tint = PrimaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "वर्तमान स्थान (Location)" else "Current Location",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        val locState = locationState
                        val displayLoc = when (locState) {
                            is com.example.data.location.LocationState.Success -> locState.getFormattedLocation(language)
                            is com.example.data.location.LocationState.Detecting -> if (language == AppLanguage.HINDI) "स्थान खोजा जा रहा है..." else "Detecting location..."
                            else -> detectedLocationText
                        }
                        Text(
                            text = displayLoc,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(
                        onClick = { viewModel.retryLocation() },
                        modifier = Modifier.testTag("facilities_retry_location_button")
                    ) {
                        Text(if (language == AppLanguage.HINDI) "रीफ्रेश" else "Refresh", fontSize = 12.sp)
                    }
                    TextButton(
                        onClick = { showManualLocationDialog = true },
                        modifier = Modifier.testTag("manual_location_button")
                    ) {
                        Icon(Icons.Default.EditLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (language == AppLanguage.HINDI) "बदलें" else "Change", fontSize = 12.sp)
                    }
                }
            }
        }

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { viewModel.filterFacilities(null) },
                    label = { Text(if (language == AppLanguage.HINDI) "सभी केंद्र" else "All Facilities") }
                )
            }
            items(FacilityType.values()) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { viewModel.filterFacilities(type) },
                    label = { Text(if (language == AppLanguage.HINDI) type.hindiLabel else type.label, fontSize = 12.sp) }
                )
            }
        }

        // Facility List
        LazyColumn(
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(facilities) { facility ->
                FacilityCard(
                    facility = facility,
                    language = language,
                    onCall = { dialPhone(facility.phone) },
                    onDirections = { openDirections(facility.latitude, facility.longitude, facility.name) }
                )
            }
        }
    }

    // Manual Location Dialog
    if (showManualLocationDialog) {
        var tempLoc by remember { mutableStateOf(detectedLocationText) }
        AlertDialog(
            onDismissRequest = { showManualLocationDialog = false },
            title = { Text(if (language == AppLanguage.HINDI) "स्थान मैन्युअल दर्ज करें" else "Enter Location Manually") },
            text = {
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI) {
                            "यदि जीपीएस उपलब्ध नहीं है, तो अपने गांव, ब्लॉक या तहसील का नाम लिखें।"
                        } else {
                            "If GPS is unavailable, enter your village, block, or district name."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tempLoc,
                        onValueChange = { tempLoc = it },
                        label = { Text(if (language == AppLanguage.HINDI) "गांव / ब्लॉक का नाम" else "Village / District Name") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateManualLocation(tempLoc)
                    showManualLocationDialog = false
                }) {
                    Text(if (language == AppLanguage.HINDI) "सहेजें" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualLocationDialog = false }) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun FacilityCard(
    facility: HealthcareFacility,
    language: AppLanguage,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("facility_card_${facility.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = facility.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI) facility.type.hindiLabel else facility.type.label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "${facility.distanceKm} km",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = facility.address,
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Timing badge
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = if (facility.isOpen24x7) LowRiskGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = if (facility.isOpen24x7) {
                            if (language == AppLanguage.HINDI) "● 24x7 खुला है" else "● Open 24x7"
                        } else {
                            if (language == AppLanguage.HINDI) "● ओपीडी: प्रातः 9 से सायं 4" else "● OPD: 9 AM - 4 PM"
                        },
                        color = if (facility.isOpen24x7) LowRiskGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Services list chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(facility.services) { s ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = s,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (language == AppLanguage.HINDI) "कॉल करें" else "Call", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onDirections,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (language == AppLanguage.HINDI) "रास्ता देखें" else "Directions", fontSize = 13.sp)
                }
            }
        }
    }
}
