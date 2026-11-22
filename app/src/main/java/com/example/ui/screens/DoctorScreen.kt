package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.DoctorInfo
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun DoctorScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val doctors by viewModel.doctors.collectAsState()
    val currentAssessment by viewModel.currentAssessment.collectAsState()

    var attachSummary by remember { mutableStateOf(currentAssessment != null) }
    var showBookingSuccessDialog by remember { mutableStateOf<DoctorInfo?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Telemedicine Banner
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "👨‍⚕️ प्रमाणित डॉक्टरों से टेली-परामर्श" else "👨‍⚕️ Telemedicine with Qualified Doctors",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) {
                            "ग्रामीण स्वास्थ्य केंद्रों और सरकारी अस्पतालों से जुड़े प्रमाणित चिकित्सक। सीधे आवाज़ या वीडियो कॉल पर परामर्श लें।"
                        } else {
                            "Certified medical doctors serving rural & block health centers. Connect via Voice Call, Video Call, or Chat."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            lineHeight = 16.sp
                        )
                    )

                    // Attach patient summary option
                    if (currentAssessment != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = attachSummary,
                                    onCheckedChange = { attachSummary = it }
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "रोगी लक्षण सारांश साथ भेजें" else "Attach Current Symptom Assessment",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${currentAssessment?.mainComplaint ?: ""} (${currentAssessment?.riskLevel?.name ?: ""})",
                                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Available Doctors
        item {
            Text(
                text = if (language == AppLanguage.HINDI) "उपलब्ध डॉक्टर (Available Doctors)" else "Available Doctors",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(doctors) { doctor ->
            DoctorCard(
                doctor = doctor,
                language = language,
                onConnect = {
                    showBookingSuccessDialog = doctor
                }
            )
        }
    }

    if (showBookingSuccessDialog != null) {
        val doc = showBookingSuccessDialog!!
        AlertDialog(
            onDismissRequest = { showBookingSuccessDialog = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = LowRiskGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(if (language == AppLanguage.HINDI) "परामर्श सत्र प्रारंभ" else "Consultation Connected")
            },
            text = {
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI) {
                            "${doc.name} के साथ आपका टेली-परामर्श सत्र तैयार है। यदि आपने रोगी सारांश जोड़ा है, तो डॉक्टर को आपके लक्षण प्राप्त हो चुके हैं।"
                        } else {
                            "Your consultation session with ${doc.name} is ready. Patient summary and symptom history have been shared."
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Specialty: ${doc.specialty}\nQualifications: ${doc.qualifications}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBookingSuccessDialog = null
                        // Trigger dialer
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:104"))
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (language == AppLanguage.HINDI) "कॉल शुरू करें" else "Start Call")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBookingSuccessDialog = null }) {
                    Text(if (language == AppLanguage.HINDI) "बंद करें" else "Close")
                }
            }
        )
    }
}

@Composable
fun DoctorCard(
    doctor: DoctorInfo,
    language: AppLanguage,
    onConnect: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("doctor_card_${doctor.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = doctor.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = doctor.specialty,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Text(
                        text = "${doctor.experienceYears} yrs exp • ${doctor.qualifications}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    )
                }

                if (doctor.isAvailableNow) {
                    Surface(
                        shape = CircleShape,
                        color = LowRiskGreen.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "● Online",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = LowRiskGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Languages spoken
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Languages: ",
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                doctor.languages.forEach { lang ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.padding(horizontal = 3.dp)
                    ) {
                        Text(
                            text = lang,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Connect button
            Button(
                onClick = onConnect,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("connect_doctor_${doctor.id}")
            ) {
                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "तुरंत परामर्श शुरू करें" else "Start Consultation (Voice / Video)",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
            }
        }
    }
}
