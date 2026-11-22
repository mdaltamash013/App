package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun EmergencyScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val currentAssessment by viewModel.currentAssessment.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()
    val facilities by viewModel.facilities.collectAsState()
    val detectedLocationText by viewModel.detectedLocationText.collectAsState()

    val nearestEmergencyFacility = facilities.firstOrNull { it.isEmergencyCapable } ?: facilities.firstOrNull()

    fun dialPhone(phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun shareLocationSOS() {
        try {
            val locationSnippet = if (detectedLocationText.isNotBlank() && !detectedLocationText.contains("required") && !detectedLocationText.contains("Unable")) {
                "\nस्थान / Location: $detectedLocationText"
            } else ""
            val emergencyText = if (language == AppLanguage.HINDI) {
                "आपातकालीन चिकित्सा अलर्ट: मुझे तत्काल चिकित्सा सहायता की आवश्यकता है। लक्षण: ${currentAssessment?.mainComplaint ?: "गंभीर स्थिति"}।$locationSnippet कृपया तुरंत सहायता करें।"
            } else {
                "MEDICAL EMERGENCY ALERT: I need urgent medical assistance. Symptoms: ${currentAssessment?.mainComplaint ?: "Severe emergency"}.$locationSnippet"
            }
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, emergencyText)
                type = "text/plain"
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Emergency SOS")
            context.startActivity(shareIntent)
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // High Contrast Emergency Header
        item {
            Surface(
                color = EmergencyRed,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Emergency Alert",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "🚨 संभावित चिकित्सा आपातकाल" else "🚨 POSSIBLE MEDICAL EMERGENCY",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) {
                            "आपके लक्षण तत्काल इन-पर्सन चिकित्सा देखभाल की मांग करते हैं। अनावश्यक पूछताछ रोक दी गई है।"
                        } else {
                            "Your symptoms may require urgent in-person medical care. Unnecessary questioning has been stopped."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color.White.copy(alpha = 0.95f),
                            textAlign = TextAlign.Center,
                            lineHeight = 20.sp
                        )
                    )
                }
            }
        }

        // Primary Ambulance Dialers (108, 102, 112)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, EmergencyRed.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "📞 आपातकालीन हेल्पलाइन (तुरंत कॉल करें)" else "📞 Emergency Helplines (Call Immediately)",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = EmergencyRed
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // 108 Ambulance
                    EmergencyDialButton(
                        number = "108",
                        label = if (language == AppLanguage.HINDI) "108 — राष्ट्रीय एम्बुलेंस सेवा (24x7 निःशुल्क)" else "108 — National Ambulance (Free 24x7)",
                        sub = if (language == AppLanguage.HINDI) "गंभीर आपातकाल, आघात व दुर्घटना" else "Severe emergency, trauma & critical care",
                        isPrimary = true,
                        onDial = { dialPhone("108") },
                        testTag = "dial_108"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 102 Maternal/Child Ambulance
                    EmergencyDialButton(
                        number = "102",
                        label = if (language == AppLanguage.HINDI) "102 — जननी-शिशु एम्बुलेंस सेवा" else "102 — Maternal & Infant Ambulance",
                        sub = if (language == AppLanguage.HINDI) "प्रसव पीड़ा, नवजात शिशु व गर्भवती सहायता" else "Labor pain, newborn & pregnancy emergency",
                        isPrimary = false,
                        onDial = { dialPhone("102") },
                        testTag = "dial_102"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 112 All-in-One Emergency
                    EmergencyDialButton(
                        number = "112",
                        label = if (language == AppLanguage.HINDI) "112 — एकीकृत आपातकालीन हेल्पलाइन" else "112 — All-in-One National Emergency",
                        sub = if (language == AppLanguage.HINDI) "पुलिस, अग्निशमन व आपातकालीन सहायता" else "Integrated Police, Fire & Health SOS",
                        isPrimary = false,
                        onDial = { dialPhone("112") },
                        testTag = "dial_112"
                    )
                }
            }
        }

        // Nearest Emergency Facility
        item {
            if (nearestEmergencyFacility != null) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "🏥 नजदीकी आपातकालीन केंद्र" else "🏥 Nearest Emergency Facility",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${nearestEmergencyFacility.distanceKm} km",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = nearestEmergencyFacility.name,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = nearestEmergencyFacility.address,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { dialPhone(nearestEmergencyFacility.phone) },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("call_nearest_hospital")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.HINDI) "कॉल करें" else "Call Facility")
                            }

                            OutlinedButton(
                                onClick = {
                                    openDirections(
                                        nearestEmergencyFacility.latitude,
                                        nearestEmergencyFacility.longitude,
                                        nearestEmergencyFacility.name
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("directions_nearest_hospital")
                            ) {
                                Icon(Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.HINDI) "दिशा-निर्देश" else "Directions")
                            }
                        }
                    }
                }
            }
        }

        // Quick Location Share & Contacts
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "📍 स्थान साझा व आपातकालीन संपर्क" else "📍 Share Location & Notify Contacts",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { shareLocationSOS() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("share_location_sos_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "आपातकालीन SOS संदेश भेजें" else "Share Emergency SOS Alert",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Saved Emergency Contacts list
                    if (emergencyContacts.isNotEmpty()) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "सहेजे गए संपर्क:" else "Saved Contacts:",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        for (contact in emergencyContacts) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = contact.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${contact.relation} • ${contact.phone}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                                Button(
                                    onClick = { dialPhone(contact.phone) },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                ) {
                                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Call", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmergencyDialButton(
    number: String,
    label: String,
    sub: String,
    isPrimary: Boolean,
    onDial: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = if (isPrimary) EmergencyRedContainer else MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isPrimary) EmergencyRed else MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
            Button(
                onClick = onDial,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPrimary) EmergencyRed else PrimaryTeal
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Call $number", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}
