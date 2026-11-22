package com.example.ui.screens.wellness

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import com.example.data.wellness.RequestStatus
import com.example.data.wellness.SupportCategory
import com.example.data.wellness.WelfareIndicatorLevel
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MyWellnessScreen(
    viewModel: HealthViewModel,
    onNavigateToAssessment: () -> Unit,
    onNavigateToOfficerDashboard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val latestRecord by viewModel.latestWellnessRecord.collectAsState()
    val allRecords by viewModel.wellnessRecords.collectAsState()
    val consentRecord by viewModel.consentRecord.collectAsState()
    val supportRequests by viewModel.supportRequests.collectAsState()

    var showSupportDialog by remember { mutableStateOf(false) }
    var showWithdrawConfirm by remember { mutableStateOf(false) }

    val welfareResources = remember { viewModel.getWelfareResources() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Module Header Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = PrimaryTeal.copy(alpha = 0.15f),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.SelfImprovement,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "मेरा कल्याण (My Wellness)" else "My Wellness & Welfare Support",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (language == AppLanguage.HINDI) "तनाव एवं बर्नआउट प्रारंभिक चेतावनी" else "Stress & Burnout Early-Warning Support",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary)
                                )
                            }
                        }

                        // Link to Officer Dashboard
                        IconButton(
                            onClick = onNavigateToOfficerDashboard,
                            modifier = Modifier.testTag("officer_dash_icon_btn")
                        ) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "Officer Dashboard",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "कल्याण सहायता, निगरानी नहीं। यह प्रणाली सीएपीएफ, पुलिस एवं सशस्त्र बलों के कर्मियों में अत्यधिक कार्यभार व तनाव के संकेतों की समय पर पहचान कर सहायक मार्गदर्शन प्रदान करती है।"
                        else
                            "Welfare, not surveillance. Designed to identify early patterns associated with workload fatigue and stress among uniformed personnel, offering supportive guidance rather than clinical diagnosis.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // PRIMARY INTERACTION: "I NEED SUPPORT" BUTTON
        item {
            Button(
                onClick = { showSupportDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("i_need_support_primary_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI) "मुझे सहायता चाहिए (I Need Support)" else "I Need Support (Confidential)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI) "काउंसलिंग, कल्याण अधिकारी या कार्यभार चर्चा" else "Counseling, welfare officer, or workload review",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // Current Welfare Status Card
        item {
            val record = latestRecord
            val (badgeColor, statusTitle, statusDesc) = if (record != null) {
                when (record.indicatorLevel) {
                    WelfareIndicatorLevel.ELEVATED.name -> Triple(
                        EmergencyRed,
                        if (language == AppLanguage.HINDI) "🔴 उन्नत कल्याण संकेतक (Elevated Indicators)" else "🔴 Elevated Welfare Indicators",
                        if (language == AppLanguage.HINDI)
                            "हाल के संकेतों में कार्यभार व तनाव का स्तर अधिक देखा गया है। कल्याण अधिकारी से चर्चा पर विचार करें।"
                        else
                            "Your recent responses show some elevated wellness indicators. Consider speaking with a welfare professional or your supervisory chain if you would like support."
                    )
                    WelfareIndicatorLevel.MODERATE.name -> Triple(
                        ModerateYellow,
                        if (language == AppLanguage.HINDI) "🟡 मध्यम कल्याण संकेतक (Moderate Indicators)" else "🟡 Moderate Welfare Indicators",
                        if (language == AppLanguage.HINDI)
                            "कुछ संकेत बताते हैं कि विश्राम या कार्यभार समीक्षा उचित हो सकती है।"
                        else
                            "Some patterns suggest that additional rest, recreation, or a follow-up assessment may be beneficial."
                    )
                    else -> Triple(
                        LowRiskGreen,
                        if (language == AppLanguage.HINDI) "🟢 कम कल्याण संकेतक (Lower Indicators)" else "🟢 Lower Welfare Indicators",
                        if (language == AppLanguage.HINDI)
                            "उपलब्ध जानकारी में कोई गंभीर तनाव पैटर्न नहीं देखा गया है। स्वस्थ दिनचर्या बनाए रखें।"
                        else
                            "No significant elevated patterns detected from available information. Continue maintaining your balanced routine."
                    )
                }
            } else {
                Triple(
                    PrimaryTeal,
                    if (language == AppLanguage.HINDI) "नया कल्याण चेक-इन उपलब्ध है" else "Ready for Wellness Check-In",
                    if (language == AppLanguage.HINDI)
                        "2 मिनट का संक्षिप्त मूल्यांकन पूरा करें ताकि आप अपने कार्यभार व तनाव के रुझान देख सकें।"
                    else
                        "Complete a quick 2-minute check-in to observe your duty workload balance and stress trends over time."
                )
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.6f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = badgeColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = statusTitle,
                                color = badgeColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        if (record != null) {
                            val dateStr = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(record.timestamp))
                            Text(
                                text = dateStr,
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = statusDesc,
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onNavigateToAssessment,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("start_wellness_assessment_btn")
                        ) {
                            Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (record != null) {
                                    if (language == AppLanguage.HINDI) "नया चेक-इन करें" else "New Check-In"
                                } else {
                                    if (language == AppLanguage.HINDI) "मूल्यांकन प्रारंभ करें" else "Start Assessment"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = onNavigateToOfficerDashboard,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_officer_dash_btn")
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "कल्याण डैशबोर्ड" else "Officer Dashboard",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // Personal Trends Summary (Section 13, 21)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TrendingUp, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "व्यक्तिगत कल्याण रुझान (Recent Trends)" else "Personal Wellness & Duty Trends",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val rec = latestRecord
                    val stressVal = rec?.stressLevel ?: 4
                    val sleepVal = rec?.sleepHours ?: 6.5f
                    val dutyVal = rec?.dutyHoursLast24h ?: 9f
                    val nightsVal = rec?.nightDutyCountLastWeek ?: 1

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TrendMetricItem("Stress Level", "$stressVal / 10", if (stressVal > 6) "↑ Elevated" else "Steady", if (stressVal > 6) EmergencyRed else LowRiskGreen)
                        TrendMetricItem("Sleep", "${sleepVal.toInt()} hrs", if (sleepVal < 6) "↓ Deficit" else "Adequate", if (sleepVal < 6) ModerateYellow else LowRiskGreen)
                        TrendMetricItem("Duty Shift", "${dutyVal.toInt()} hrs", if (dutyVal > 11) "↑ Long" else "Normal", if (dutyVal > 11) EmergencyRed else LowRiskGreen)
                        TrendMetricItem("Night Duties", "$nightsVal nights", if (nightsVal >= 3) "↑ Frequent" else "Low", if (nightsVal >= 3) ModerateYellow else LowRiskGreen)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "समय के साथ रुझान देखने से यह समझने में मदद मिलती है कि कब लंबी ड्यूटी या नींद की कमी जमा हो रही है।"
                        else
                            "Monitoring trends over time helps you recognize when extended shift patterns or rest deficits are accumulating.",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    )
                }
            }
        }

        // Support Requests Submitted by User
        if (supportRequests.isNotEmpty()) {
            item {
                Text(
                    text = if (language == AppLanguage.HINDI) "मेरे सहायता अनुरोध (My Support Requests)" else "My Submitted Support Requests",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(supportRequests.filter { it.personnelId == "self" }) { req ->
                val statusColor = when (req.status) {
                    RequestStatus.NEW.name -> EmergencyRed
                    RequestStatus.UNDER_REVIEW.name -> ModerateYellow
                    RequestStatus.SUPPORT_OFFERED.name -> PrimaryTeal
                    else -> LowRiskGreen
                }
                val dateFormatted = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(req.timestamp))

                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(req.category.replace("_", " "), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = req.status,
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Submitted $dateFormatted • Preference: ${req.preferredContactMethod}",
                            style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                        if (!req.officerReviewNotes.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Welfare Update: ${req.officerReviewNotes}",
                                style = MaterialTheme.typography.bodySmall.copy(color = PrimaryTeal, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }

        // Welfare Resources & Helplines (Cached Offline)
        item {
            Text(
                text = if (language == AppLanguage.HINDI) "अधिकृत कल्याण एवं परामर्श हेल्पलाइन (24x7)" else "Authorized Welfare Helplines & Desks",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
        }

        items(welfareResources) { resource ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.HINDI) resource.titleHi else resource.titleEn,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) resource.descriptionHi else resource.descriptionEn,
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📞 ${resource.contactNumber} • ${resource.availableHours}",
                            style = MaterialTheme.typography.labelSmall.copy(color = PrimaryTeal, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    IconButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${resource.contactNumber}"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(PrimaryTeal.copy(alpha = 0.12f))
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = "Call", tint = PrimaryTeal)
                    }
                }
            }
        }

        // Consent & Privacy Management (Section 4, 17, 18)
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "गोपनीयता, सहमति एवं स्वैच्छिक डेटा नियंत्रण" else "Privacy, Consent & Data Ownership",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "आपकी कल्याण जानकारी स्वैच्छिक है। यह कभी अनुशासनात्मक या रोजगार निर्णयों के लिए उपयोग नहीं की जाती।"
                        else
                            "All welfare self-assessments are strictly voluntary. Data is encrypted, pseudonymized, and never used for disciplinary actions.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Withdraw / Delete Data Action
                    OutlinedButton(
                        onClick = { showWithdrawConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = EmergencyRed),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("withdraw_wellness_history_btn")
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "मेरा मूल्यांकन डेटा हटाएं (Withdraw Data)" else "Withdraw & Delete My Wellness History",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }

    // Support Request Modal
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

    // Withdraw Confirm Dialog
    if (showWithdrawConfirm) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showWithdrawConfirm = false }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "मूल्यांकन डेटा हटाएं?" else "Delete Wellness Records?",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "क्या आप अपनी सभी पिछली स्वैच्छिक कल्याण प्रतिक्रियाएं हटाना चाहते हैं? यह क्रिया स्थायी है।"
                        else
                            "Are you sure you want to permanently delete all voluntary wellness records from this device?",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showWithdrawConfirm = false }) {
                            Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.clearMyWellnessHistory {
                                    showWithdrawConfirm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                        ) {
                            Text(if (language == AppLanguage.HINDI) "हटाएं" else "Delete")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendMetricItem(
    title: String,
    value: String,
    trend: String,
    trendColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
        Spacer(modifier = Modifier.height(2.dp))
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = trendColor.copy(alpha = 0.15f)
        ) {
            Text(trend, color = trendColor, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
        }
    }
}
