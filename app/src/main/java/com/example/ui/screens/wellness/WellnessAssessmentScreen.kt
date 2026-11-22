package com.example.ui.screens.wellness

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.wellness.DutyWorkloadData
import com.example.data.wellness.SelfReportedWellness
import com.example.data.wellness.SupportCategory
import com.example.data.wellness.WearableBiometricData
import com.example.data.wellness.WelfareAssessmentResult
import com.example.data.wellness.WelfareIndicatorLevel
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun WellnessAssessmentScreen(
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()

    // Work & Duty States
    var dutyHours by remember { mutableFloatStateOf(9f) }
    var nightDuties by remember { mutableIntStateOf(1) }
    var deploymentDays by remember { mutableIntStateOf(20) }
    var daysSinceLeave by remember { mutableIntStateOf(45) }
    var irregularShifts by remember { mutableStateOf(false) }
    var heavyTraining by remember { mutableStateOf(false) }

    // Wellness States
    var stressLevel by remember { mutableIntStateOf(4) }
    var mood by remember { mutableStateOf("Neutral") }
    var sleepHours by remember { mutableFloatStateOf(6.5f) }
    var sleepQuality by remember { mutableIntStateOf(3) }
    var fatigueLevel by remember { mutableIntStateOf(4) }
    var workloadPerception by remember { mutableIntStateOf(5) }
    var familySupportRating by remember { mutableIntStateOf(4) }
    var interestInSupport by remember { mutableStateOf(false) }
    var optionalNotes by remember { mutableStateOf("") }

    // Optional Wearable Consent
    var wearableConsent by remember { mutableStateOf(false) }
    var restingHeartRate by remember { mutableIntStateOf(72) }

    var assessmentResult by remember { mutableStateOf<WelfareAssessmentResult?>(null) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var hasSavedRecord by remember { mutableStateOf(false) }

    val moodOptions = listOf("Good", "Calm", "Neutral", "Tired", "Overwhelmed")

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("assessment_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI) "कल्याण एवं तनाव स्व-मूल्यांकन" else "Wellness & Stress Self-Assessment",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI) "कल्याण सहायता हेतु — यह कोई मेडिकल डायग्नोसिस नहीं है" else "For Welfare Support — Not a Medical Diagnosis",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Section 1: Workload & Duty Information
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("1", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "ड्यूटी एवं कार्यभार संदर्भ (Work & Duty)" else "Duty & Operational Workload Context",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Duty hours last 24h
                    Text(
                        text = if (language == AppLanguage.HINDI) "पिछले 24 घंटों में ड्यूटी अवधि: ${dutyHours.toInt()} घंटे" else "Duty Hours in Last 24 Hours: ${dutyHours.toInt()} hrs",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = dutyHours,
                        onValueChange = { dutyHours = it },
                        valueRange = 4f..20f,
                        steps = 15,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Night shifts in last 7 days
                    Text(
                        text = if (language == AppLanguage.HINDI) "पिछले 7 दिनों में नाइट ड्यूटी संख्या: $nightDuties रातें" else "Night Duties in Last 7 Days: $nightDuties nights",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = nightDuties.toFloat(),
                        onValueChange = { nightDuties = it.toInt() },
                        valueRange = 0f..7f,
                        steps = 6,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Continuous deployment days
                    Text(
                        text = if (language == AppLanguage.HINDI) "वर्तमान फील्ड/आउटपोस्ट तैनाती अवधि: $deploymentDays दिन" else "Current Field / Outpost Deployment: $deploymentDays days",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = deploymentDays.toFloat(),
                        onValueChange = { deploymentDays = it.toInt() },
                        valueRange = 0f..180f,
                        steps = 17,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Days since formal leave
                    Text(
                        text = if (language == AppLanguage.HINDI) "अंतिम औपचारिक छुट्टी के बाद के दिन: $daysSinceLeave दिन" else "Days Since Last Formal Leave: $daysSinceLeave days",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = daysSinceLeave.toFloat(),
                        onValueChange = { daysSinceLeave = it.toInt() },
                        valueRange = 0f..180f,
                        steps = 17,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Checkboxes
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = irregularShifts,
                            onCheckedChange = { irregularShifts = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "अनियमित या बारी-बारी से बदलने वाली शिफ्ट्स" else "Irregular or alternating shift rotations",
                            fontSize = 13.sp
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = heavyTraining,
                            onCheckedChange = { heavyTraining = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "समवर्ती गहन शारीरिक या सामरिक प्रशिक्षण" else "Concurrent heavy physical / tactical training",
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Section 2: Self-Reported Wellness & Perception
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("2", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "व्यक्तिगत कल्याण अनुभूति (Self-Reported Wellness)" else "Self-Reported Wellness & Experience",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Stress Slider
                    Text(
                        text = if (language == AppLanguage.HINDI) "वर्तमान तनाव स्तर: $stressLevel / 10" else "Current Stress Level: $stressLevel / 10",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = stressLevel.toFloat(),
                        onValueChange = { stressLevel = it.toInt() },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("stress_slider")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Mood selector
                    Text(
                        text = if (language == AppLanguage.HINDI) "सामान्य मनोदशा (Mood):" else "General Mood:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        moodOptions.forEach { option ->
                            FilterChip(
                                selected = mood == option,
                                onClick = { mood = option },
                                label = { Text(option, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sleep Hours
                    Text(
                        text = if (language == AppLanguage.HINDI) "औसत दैनिक नींद: ${sleepHours.toInt()} घंटे" else "Average Sleep: ${sleepHours.toInt()} hours",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = sleepHours,
                        onValueChange = { sleepHours = it },
                        valueRange = 2f..12f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Sleep Quality (1 to 5)
                    Text(
                        text = if (language == AppLanguage.HINDI) "नींद की गुणवत्ता: $sleepQuality / 5 (1=अशांत, 5=गहरी)" else "Sleep Quality: $sleepQuality / 5 (1=Restless, 5=Restful)",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = sleepQuality.toFloat(),
                        onValueChange = { sleepQuality = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Physical & Mental Fatigue (0 - 10)
                    Text(
                        text = if (language == AppLanguage.HINDI) "शारीरिक व मानसिक थकान स्तर: $fatigueLevel / 10" else "Fatigue & Tiredness Level: $fatigueLevel / 10",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = fatigueLevel.toFloat(),
                        onValueChange = { fatigueLevel = it.toInt() },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Workload perception
                    Text(
                        text = if (language == AppLanguage.HINDI) "कार्यभार का दबाव: $workloadPerception / 10" else "Perceived Workload Pressure: $workloadPerception / 10",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = workloadPerception.toFloat(),
                        onValueChange = { workloadPerception = it.toInt() },
                        valueRange = 0f..10f,
                        steps = 9,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Family support (1 - 5)
                    Text(
                        text = if (language == AppLanguage.HINDI) "पारिवारिक / सामाजिक संपर्क सहजता: $familySupportRating / 5" else "Family & Social Connection: $familySupportRating / 5",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Slider(
                        value = familySupportRating.toFloat(),
                        onValueChange = { familySupportRating = it.toInt() },
                        valueRange = 1f..5f,
                        steps = 3,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = interestInSupport,
                            onCheckedChange = { interestInSupport = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "कल्याण अधिकारी या काउंसलर से संपर्क में रुचि है"
                            else
                                "Interested in speaking with a welfare officer or counselor",
                            fontSize = 13.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = optionalNotes,
                        onValueChange = { optionalNotes = it },
                        label = { Text(if (language == AppLanguage.HINDI) "अतिरिक्त विचार या चिंता (वैकल्पिक)" else "Optional Notes or Comments") },
                        placeholder = { Text(if (language == AppLanguage.HINDI) "गोपनीय एवं सुरक्षित" else "Confidential voluntary notes") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Section 3: Optional Wearable Data (Explicit Consent)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Watch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.HINDI) "वैकल्पिक वियरेबल संकेत (Optional Wearable Data)" else "Optional Wearable Biometric Signals",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI) "पूर्णतः स्वैच्छिक एवं सहमति आधारित" else "Strictly voluntary & consent-based",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Checkbox(
                            checked = wearableConsent,
                            onCheckedChange = { wearableConsent = it }
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "मैं वियरेबल संकेत (हृदय गति, नींद) को संदर्भ के रूप में शामिल करने की सहमति देता हूँ।"
                            else
                                "I voluntarily consent to include wearable metrics as one contextual signal among multiple factors.",
                            fontSize = 12.sp
                        )
                    }

                    if (wearableConsent) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "विश्राम हृदय गति (Resting HR): $restingHeartRate bpm" else "Resting Heart Rate: $restingHeartRate bpm",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Slider(
                            value = restingHeartRate.toFloat(),
                            onValueChange = { restingHeartRate = it.toInt() },
                            valueRange = 50f..115f,
                            steps = 12,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        // Action Button: Calculate Indicators
        item {
            Button(
                onClick = {
                    val dutyData = DutyWorkloadData(
                        dutyHoursLast24h = dutyHours,
                        nightDutyCountLastWeek = nightDuties,
                        deploymentDays = deploymentDays,
                        daysSinceLastLeave = daysSinceLeave,
                        hasIrregularShifts = irregularShifts,
                        hasHeavyTrainingCommitment = heavyTraining,
                        restHoursLast24h = (24f - dutyHours).coerceAtLeast(3f)
                    )
                    val wellnessData = SelfReportedWellness(
                        stressLevel = stressLevel,
                        mood = mood,
                        sleepHours = sleepHours,
                        sleepQuality = sleepQuality,
                        fatigueLevel = fatigueLevel,
                        workloadPerception = workloadPerception,
                        emotionalWellbeing = (10 - stressLevel).coerceIn(0, 10),
                        familySupportRating = familySupportRating,
                        interestInSupport = interestInSupport,
                        optionalNotes = optionalNotes
                    )
                    val wearableData = WearableBiometricData(
                        isEnabled = wearableConsent,
                        restingHeartRateBpm = if (wearableConsent) restingHeartRate else null,
                        sleepDurationHours = if (wearableConsent) sleepHours else null
                    )

                    viewModel.submitWellnessAssessment(
                        duty = dutyData,
                        wellness = wellnessData,
                        wearable = wearableData
                    ) { result ->
                        assessmentResult = result
                        hasSavedRecord = true
                    }
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("calculate_welfare_indicator_btn")
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.HINDI) "कल्याण संकेतक का विश्लेषण करें" else "Evaluate Welfare Indicators",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        }

        // Assessment Results Card
        item {
            val result = assessmentResult
            if (result != null) {
                val (badgeColor, label, desc) = when (result.indicatorLevel) {
                    WelfareIndicatorLevel.LOWER -> Triple(
                        LowRiskGreen,
                        if (language == AppLanguage.HINDI) "🟢 कम कल्याण संकेतक (Lower Indicators)" else "🟢 Lower Welfare Indicators",
                        if (language == AppLanguage.HINDI) result.indicatorLevel.explanationHi else result.indicatorLevel.explanationEn
                    )
                    WelfareIndicatorLevel.MODERATE -> Triple(
                        ModerateYellow,
                        if (language == AppLanguage.HINDI) "🟡 मध्यम कल्याण संकेतक (Moderate Indicators)" else "🟡 Moderate Welfare Indicators",
                        if (language == AppLanguage.HINDI) result.indicatorLevel.explanationHi else result.indicatorLevel.explanationEn
                    )
                    WelfareIndicatorLevel.ELEVATED -> Triple(
                        EmergencyRed,
                        if (language == AppLanguage.HINDI) "🔴 उन्नत कल्याण संकेतक (Elevated Indicators)" else "🔴 Elevated Welfare Indicators",
                        if (language == AppLanguage.HINDI) result.indicatorLevel.explanationHi else result.indicatorLevel.explanationEn
                    )
                }

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(2.dp, badgeColor.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = badgeColor.copy(alpha = 0.15f),
                                modifier = Modifier.padding(end = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            if (hasSavedRecord) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Saved",
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "सहेजा गया" else "Saved",
                                    color = PrimaryTeal,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = desc,
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                        )

                        // Crucial principle disclaimer
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI)
                                        "यह एक कल्याण-समर्थन संकेतक है, कोई मेडिकल डायग्नोसिस या अनुशासनात्मक मूल्यांकन नहीं।"
                                    else
                                        "This is a welfare-support indicator, not a medical diagnosis or fitness-for-duty classification.",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp)
                                )
                            }
                        }

                        // Contributing signals
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "संकेतक में योगदान देने वाले प्रमुख कारक:" else "Key Contributing Signals Considered:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val displaySignals = if (language == AppLanguage.HINDI) result.contributingSignalsHi else result.contributingSignals
                        displaySignals.forEach { signal ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = badgeColor, fontWeight = FontWeight.Bold)
                                Text(signal, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        // Recommendations
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "सहायक सुझाव एवं मार्गदर्शन:" else "Supportive Welfare Recommendations:",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        val displayRecs = if (language == AppLanguage.HINDI) result.recommendationsHi else result.recommendationsEn
                        displayRecs.forEach { rec ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("✓ ", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                                Text(rec, style = MaterialTheme.typography.bodySmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = { showSupportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("results_i_need_support_btn")
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.HINDI) "सहायता चाहिए" else "I Need Support", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = onNavigateBack,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (language == AppLanguage.HINDI) "कल्याण हब पर जाएं" else "Back to Hub", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSupportDialog) {
        SupportRequestDialog(
            language = language,
            onDismiss = { showSupportDialog = false },
            onSubmit = { category, details, urgency, contactMethod, name, unit ->
                viewModel.submitSupportRequest(
                    category = category,
                    details = details,
                    urgency = urgency,
                    preferredContactMethod = contactMethod,
                    personnelName = name,
                    unitOrStation = unit
                )
                showSupportDialog = false
            }
        )
    }
}
