package com.example.ui.screens.wellness

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.wellness.DemoPersonnelProfile
import com.example.data.wellness.RequestStatus
import com.example.data.wellness.SupportCategory
import com.example.data.wellness.SupportRequestEntity
import com.example.data.wellness.UserRole
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
fun WelfareOfficerDashboardScreen(
    viewModel: HealthViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val currentUserRole by viewModel.currentUserRole.collectAsState()
    val isDemoModeActive by viewModel.isDemoModeActive.collectAsState()
    val supportRequests by viewModel.supportRequests.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Aggregated Trends, 1: Support Requests, 2: SIH Demo Profiles, 3: Audit Logs
    val demoProfiles = remember { viewModel.getDemoProfiles() }

    var editingRequest by remember { mutableStateOf<SupportRequestEntity?>(null) }
    var officerNoteText by remember { mutableStateOf("") }
    var selectedNewStatus by remember { mutableStateOf(RequestStatus.SUPPORT_OFFERED) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("officer_dash_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI) "अधिकृत कल्याण डैशबोर्ड" else "Authorized Welfare Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI) "कल्याण निगरानी एवं मानव निर्णय सहायता" else "Welfare Oversight & Decision Support",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Role & Demo Toggle Bar
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "सक्रिय भूमिका:" else "Active Role:",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        // Role Selector Chips
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(UserRole.WELFARE_OFFICER, UserRole.COMMANDER).forEach { role ->
                                FilterChip(
                                    selected = currentUserRole == role,
                                    onClick = { viewModel.switchUserRole(role) },
                                    label = { Text(if (role == UserRole.WELFARE_OFFICER) "Welfare Officer" else "Commander", fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // SIH / Synthetic Demo Mode Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (language == AppLanguage.HINDI) "सिंथेटिक डेमो मोड (SIH Presentation)" else "Synthetic / Demo Mode (SIH Presentation)",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI) "काल्पनिक कार्मिकों के डेटा का सुरक्षित प्रदर्शन" else "Safe presentation with fictional synthetic personnel",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                            )
                        }
                        Switch(
                            checked = isDemoModeActive,
                            onCheckedChange = { viewModel.toggleDemoMode() },
                            modifier = Modifier.testTag("toggle_demo_mode_switch")
                        )
                    }
                }
            }
        }

        // Demo Mode Disclaimer Banner
        if (isDemoModeActive) {
            item {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = ModerateYellow.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, ModerateYellow.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = ModerateYellow, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "⚠️ सिंथेटिक/डेमो डेटा — यह वास्तविक कर्मियों का डेटा नहीं है (सुरक्षित प्रस्तुति)"
                            else
                                "⚠️ Synthetic / Demo Data — Not Real Personnel Data (Presentation Mode)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        )
                    }
                }
            }
        }

        // Navigation Tabs (Trends, Requests, Synthetic Profiles, Audit Logs)
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val tabs = listOf(
                    if (language == AppLanguage.HINDI) "यूनिट रुझान" else "Unit Trends",
                    if (language == AppLanguage.HINDI) "सहायता कतार (${supportRequests.size})" else "Support Queue (${supportRequests.size})",
                    if (language == AppLanguage.HINDI) "डेमो प्रोफाइल्स (A/B/C)" else "Demo Profiles (A/B/C)",
                    if (language == AppLanguage.HINDI) "ऑडिट लॉग्स" else "Audit Logs"
                )
                items(tabs.indices.toList()) { index ->
                    FilterChip(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        label = { Text(tabs[index], fontSize = 12.sp) }
                    )
                }
            }
        }

        // TAB 0: Aggregated Unit Welfare Trends
        if (selectedTab == 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Assessment, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "यूनिट कल्याण संकेतक वितरण (Aggregated)" else "Unit-Level Welfare Indicator Distribution",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Distribution Stat Bars
                        val totalPersonnel = if (isDemoModeActive) 140 else 24
                        val lowerPct = 0.64f
                        val moderatePct = 0.24f
                        val elevatedPct = 0.12f

                        // Lower
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (language == AppLanguage.HINDI) "कम संकेतक (Lower)" else "Lower Indicators", fontSize = 12.sp, color = LowRiskGreen, fontWeight = FontWeight.Bold)
                            Text("${(lowerPct * 100).toInt()}% (${(totalPersonnel * lowerPct).toInt()} personnel)", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { lowerPct },
                            color = LowRiskGreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Moderate
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (language == AppLanguage.HINDI) "मध्यम संकेतक (Moderate)" else "Moderate Indicators", fontSize = 12.sp, color = ModerateYellow, fontWeight = FontWeight.Bold)
                            Text("${(moderatePct * 100).toInt()}% (${(totalPersonnel * moderatePct).toInt()} personnel)", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { moderatePct },
                            color = ModerateYellow,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Elevated
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(if (language == AppLanguage.HINDI) "उन्नत संकेतक (Elevated)" else "Elevated Indicators", fontSize = 12.sp, color = EmergencyRed, fontWeight = FontWeight.Bold)
                            Text("${(elevatedPct * 100).toInt()}% (${(totalPersonnel * elevatedPct).toInt()} personnel)", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { elevatedPct },
                            color = EmergencyRed,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        // Common Contributing Unit-Level Signals
                        Text(
                            text = if (language == AppLanguage.HINDI) "शीर्ष संगठनात्मक कार्यभार कारक:" else "Top Organizational Workload Factors Identified:",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        listOf(
                            "38% personnel on continuous deployment exceeding 60 days.",
                            "29% personnel completed >= 3 night shifts in the past week.",
                            "22% personnel reported sleep duration under 5.5 hours.",
                            "18% personnel indicate over 90 days since last formal leave."
                        ).forEach { factor ->
                            Row(modifier = Modifier.padding(vertical = 2.dp)) {
                                Text("• ", color = PrimaryTeal, fontWeight = FontWeight.Bold)
                                Text(factor, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: Support Requests Queue
        if (selectedTab == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "सहायता अनुरोध कतार" else "Confidential Support Requests",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${supportRequests.size} requests",
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            if (supportRequests.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = PrimaryTeal, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "वर्तमान में कोई लंबित सहायता अनुरोध नहीं है।" else "No pending support requests in the queue.",
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                }
            } else {
                items(supportRequests) { request ->
                    val statusColor = when (request.status) {
                        RequestStatus.NEW.name -> EmergencyRed
                        RequestStatus.UNDER_REVIEW.name -> ModerateYellow
                        RequestStatus.SUPPORT_OFFERED.name -> PrimaryTeal
                        else -> LowRiskGreen
                    }
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(request.timestamp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = request.status,
                                        color = statusColor,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Text(dateFormatted, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = request.category.replace("_", " "),
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )

                            Text(
                                text = "${request.personnelName} • ${request.unitOrStation}",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.primary)
                            )

                            if (request.details.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${request.details}\"",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }

                            if (!request.officerReviewNotes.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Officer Note: ${request.officerReviewNotes} (by ${request.reviewedBy ?: "Welfare Cell"})",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Button: Update Request
                            OutlinedButton(
                                onClick = {
                                    editingRequest = request
                                    officerNoteText = request.officerReviewNotes ?: ""
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.HINDI) "समीक्षा करें / स्थिति बदलें" else "Review & Update Status", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: SIH Demo Profiles (Personnel A, B, C)
        if (selectedTab == 2) {
            item {
                Text(
                    text = if (language == AppLanguage.HINDI) "सिंथेटिक केस स्टडीज (Synthetic SIH Profiles)" else "Synthetic Demonstration Profiles (Personnel A, B, C)",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            items(demoProfiles) { profile ->
                val badgeColor = when (profile.assessmentResult.indicatorLevel) {
                    WelfareIndicatorLevel.ELEVATED -> EmergencyRed
                    WelfareIndicatorLevel.MODERATE -> ModerateYellow
                    WelfareIndicatorLevel.LOWER -> LowRiskGreen
                }

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.5.dp, badgeColor.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(profile.codeName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                Text("${profile.rankOrRole} • ${profile.unit}", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = profile.assessmentResult.indicatorLevel.name,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Workload signals
                        Text(
                            text = "Workload Context: ${profile.dutyData.dutyHoursLast24h.toInt()}h shift, ${profile.dutyData.nightDutyCountLastWeek} night duties, ${profile.dutyData.deploymentDays} days deployed, ${profile.dutyData.daysSinceLastLeave}d since leave.",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Self-Reported: Stress ${profile.wellnessData.stressLevel}/10, Mood: ${profile.wellnessData.mood}, Sleep: ${profile.wellnessData.sleepHours}h.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Key Contributing Signals:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                        profile.assessmentResult.contributingSignals.take(3).forEach { s ->
                            Row(modifier = Modifier.padding(vertical = 1.dp)) {
                                Text("• ", color = badgeColor, fontWeight = FontWeight.Bold)
                                Text(s, style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Human decision support action button
                        Button(
                            onClick = {
                                viewModel.submitSupportRequest(
                                    category = SupportCategory.WORKLOAD_REST_DISCUSSION,
                                    details = "Demonstration review initiated for ${profile.codeName} based on elevated deployment signals.",
                                    urgency = "Timely",
                                    preferredContactMethod = "In-Person Discussion",
                                    personnelName = profile.codeName,
                                    unitOrStation = profile.unit
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.People, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (language == AppLanguage.HINDI) "सहायक समीक्षा प्रारंभ करें" else "Initiate Supportive Review (Demo)", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // TAB 3: Audit Logs
        if (selectedTab == 3) {
            item {
                Text(
                    text = if (language == AppLanguage.HINDI) "कल्याण सुरक्षा एवं ऑडिट ट्रेल्स" else "Welfare Security & Access Audit Trails",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
            }

            if (auditLogs.isEmpty()) {
                item {
                    Text(
                        text = if (language == AppLanguage.HINDI) "अभी कोई ऑडिट इवेंट दर्ज नहीं है।" else "No audit events logged yet.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            } else {
                items(auditLogs) { log ->
                    val dateFormatted = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.getDefault()).format(Date(log.timestamp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = PrimaryTeal)
                                Text(dateFormatted, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("By Role: ${log.performedByRole} • Target: ${log.personnelOrUnitIdentifier}", fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }

    // Edit Request Modal
    editingRequest?.let { req ->
        androidx.compose.ui.window.Dialog(onDismissRequest = { editingRequest = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("Review Support Request #${req.id}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    Text("${req.personnelName} (${req.category})", style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Update Status:", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.height(6.dp))

                    RequestStatus.values().forEach { st ->
                        FilterChip(
                            selected = selectedNewStatus == st,
                            onClick = { selectedNewStatus = st },
                            label = { Text(st.labelEn, fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = officerNoteText,
                        onValueChange = { officerNoteText = it },
                        label = { Text("Officer Review Notes") },
                        placeholder = { Text("e.g. Discussed shift balance with outpost commander.") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { editingRequest = null }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                viewModel.updateSupportRequestStatus(
                                    request = req,
                                    newStatus = selectedNewStatus,
                                    notes = officerNoteText.trim(),
                                    reviewedBy = "Welfare Liaison Officer"
                                )
                                editingRequest = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                        ) {
                            Text("Save Review")
                        }
                    }
                }
            }
        }
    }
}
