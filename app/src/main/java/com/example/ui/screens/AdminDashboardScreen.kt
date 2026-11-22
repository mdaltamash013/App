package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val totalCount by viewModel.totalAssessmentCount.collectAsState()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Analytics,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (language == AppLanguage.HINDI) "ग्रामीण स्वास्थ्य एनालिटिक्स" else "Rural Health Intelligence Dashboard",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI) "सामुदायिक स्तर पर स्वास्थ्य रुझान एवं सुरक्षा ट्रैकिंग" else "Community triage insights & offline reliability",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        }

        // Metrics Stat Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = if (language == AppLanguage.HINDI) "सहेजी गई जांच" else "Saved Assessments",
                    value = "$totalCount",
                    sub = if (language == AppLanguage.HINDI) "स्थानीय डेटाबेस (Room)" else "Local Room DB",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = if (language == AppLanguage.HINDI) "ऑफ़लाइन उपलब्धता" else "Offline Safety Engine",
                    value = "100%",
                    sub = if (language == AppLanguage.HINDI) "शून्य डेटा निर्भरता" else "Deterministic Guard",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Triage Distribution Breakdown
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "जोखिम वर्गीकरण विश्लेषण" else "Triage Risk Breakdown",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    TriageMetricBar(
                        label = if (language == AppLanguage.HINDI) "कम जोखिम (घरेलू देखभाल / ORS / विश्राम)" else "Low Risk (Supportive Home Care / ORS)",
                        percentage = 64,
                        color = LowRiskGreen
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TriageMetricBar(
                        label = if (language == AppLanguage.HINDI) "मध्यम जोखिम (डॉक्टर परामर्श अनुशंसित)" else "Moderate Risk (Doctor Review Recommended)",
                        percentage = 26,
                        color = ModerateYellow
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    TriageMetricBar(
                        label = if (language == AppLanguage.HINDI) "आपातकालीन रेड-फ्लैग (तुरंत 108 / अस्पताल)" else "Emergency Red Flags (Immediate 108 / Hospital)",
                        percentage = 10,
                        color = EmergencyRed
                    )
                }
            }
        }

        // Top Conditions & Languages
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "प्रमुख ग्रामीण स्वास्थ्य लक्षण (Top Rural Syndromes)" else "Top Syndromes in Rural Blocks",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val syndromes = listOf(
                        "1. Acute Diarrhea & Dehydration (ORS guidance) — 38%",
                        "2. Seasonal Viral Fever & Bodyache — 27%",
                        "3. Upper Respiratory Cough & Cold — 21%",
                        "4. Maternal & Prenatal Anemia / Care — 14%"
                    )
                    syndromes.forEach { s ->
                        Text(
                            text = s,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 20.sp
                            )
                        )
                    }
                }
            }
        }

        // Quick Scenario Testing Console
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "परीक्षण कंसोल (Testing Scenarios)" else "Test Scenario Runner",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val testList = listOf(
                        Pair(1, "Scenario 1: Low Risk (Mild Fever)"),
                        Pair(2, "Scenario 2: Moderate Risk (Diarrhea/ORS)"),
                        Pair(3, "Scenario 3: Emergency Override (Chest Pain)"),
                        Pair(4, "Scenario 4: Offline Simulation"),
                        Pair(5, "Scenario 5: Hindi Voice Flow"),
                        Pair(6, "Scenario 6: Jan Aushadhi Medicine Search")
                    )

                    testList.forEach { (num, name) ->
                        Button(
                            onClick = { viewModel.triggerScenario(num) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .testTag("admin_trigger_scenario_$num")
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = name,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, sub: String, modifier: Modifier = Modifier) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black, color = PrimaryTeal))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = sub, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant))
        }
    }
}

@Composable
fun TriageMetricBar(label: String, percentage: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp))
            Text(text = "$percentage%", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = color))
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = color,
            trackColor = color.copy(alpha = 0.15f)
        )
    }
}
