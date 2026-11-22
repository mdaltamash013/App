package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.TriageLevel
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.LowRiskGreenContainer
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.ModerateYellowContainer
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun SummaryScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val currentAssessment by viewModel.currentAssessment.collectAsState()
    var hasSaved by remember { mutableStateOf(false) }

    val assessment = currentAssessment

    fun buildShareableClinicalNote(): String {
        return buildString {
            append("=== LIFECARE 24/7 — CLINICAL PATIENT SUMMARY ===\n")
            append("Date: ${java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}\n")
            append("Chief Complaint: ${assessment?.mainComplaint ?: "General Health Consultation"}\n")
            append("Triage Risk Classification: ${assessment?.riskLevel?.name ?: "EVALUATED"}\n\n")

            append("SYMPTOMS REPORTED:\n")
            assessment?.symptoms?.forEach { s -> append("• $s\n") }

            append("\nSUPPORTIVE GUIDANCE PROVIDED:\n")
            assessment?.guidancePoints?.forEachIndexed { idx, p -> append("${idx + 1}. $p\n") }

            val signs = assessment?.warningSigns
            if (!signs.isNullOrEmpty()) {
                append("\nRED FLAG WARNING SIGNS:\n")
                signs.forEach { w -> append("! $w\n") }
            }

            append("\nDISCLAIMER: Prepared via LifeCare 24/7 offline-first clinical triage engine for rural healthcare support. Not an independent medical prescription.")
        }
    }

    fun shareSummary() {
        val note = buildShareableClinicalNote()
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, note)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Clinical Patient Summary")
        context.startActivity(shareIntent)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Back & Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onNavigate("assessment") }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI) "रोगी स्वास्थ्य सारांश" else "Clinical Patient Summary",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI) "डॉक्टर या आशा कार्यकर्ता से साझा करने योग्य" else "Suitable for sharing with doctor or ASHA worker",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Triage Badge
        item {
            if (assessment != null) {
                val (bgColor, textColor, label) = when (assessment.riskLevel) {
                    TriageLevel.LOW -> Triple(LowRiskGreenContainer, LowRiskGreen, "Low Risk — Mild / Home Care")
                    TriageLevel.MODERATE -> Triple(ModerateYellowContainer, ModerateYellow, "Moderate Risk — Medical Consultation Needed")
                    else -> Triple(EmergencyRedContainer, EmergencyRed, "Emergency Alert — Immediate Care")
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = bgColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(textColor)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        )
                    }
                }
            }
        }

        // Clinical Document Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "CLINICAL ASSESSMENT RECORD",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    SummarySection(
                        title = if (language == AppLanguage.HINDI) "मुख्य समस्या (Chief Complaint):" else "Chief Complaint:",
                        content = assessment?.mainComplaint ?: "General Health Concern"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SummarySection(
                        title = if (language == AppLanguage.HINDI) "लक्षण विवरण (Symptoms):" else "Symptoms Reported:",
                        content = assessment?.symptoms?.joinToString(", ") ?: "N/A"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI) "सहायक देखभाल निर्देश (Supportive Care):" else "Supportive Care Recommendations:",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    assessment?.guidancePoints?.let { points ->
                        for ((idx, pt) in points.withIndex()) {
                            Text(
                                text = "${idx + 1}. $pt",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 18.sp
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }

                    val redFlags = assessment?.warningSigns
                    if (!redFlags.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "चेतावनी संकेत (Red Flags):" else "Warning Signs (Red Flags):",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmergencyRed
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        for (w in redFlags) {
                            Text(
                                text = "• $w",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EmergencyRed,
                                    lineHeight = 16.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Action Buttons: Share with Doctor, Speak Aloud, Save, Doctor Consult
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Share via WhatsApp / SMS / Doctor
                Button(
                    onClick = { shareSummary() },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("share_summary_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "📤 डॉक्टर या आशा को सारांश भेजें" else "📤 Share Summary with Doctor / ASHA",
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Read Aloud via Voice
                    OutlinedButton(
                        onClick = {
                            val textToSpeak = "${assessment?.guidanceTitle ?: ""} ${assessment?.guidancePoints?.firstOrNull() ?: ""}"
                            viewModel.voiceManager.speak(textToSpeak, language)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("speak_summary_button")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == AppLanguage.HINDI) "बोलकर सुनाएं" else "Read Aloud")
                    }

                    // Save to Room DB
                    OutlinedButton(
                        onClick = {
                            viewModel.saveCurrentAssessmentToRecord()
                            hasSaved = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_summary_record_button")
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (hasSaved) "Saved ✓" else "Save")
                    }
                }

                // Consult Doctor
                Button(
                    onClick = { onNavigate("doctor") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (language == AppLanguage.HINDI) "👨‍⚕️ डॉक्टर से परामर्श करें" else "👨‍⚕️ Consult Doctor with this Summary")
                }
            }
        }
    }
}

@Composable
fun SummarySection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
        )
    }
}
