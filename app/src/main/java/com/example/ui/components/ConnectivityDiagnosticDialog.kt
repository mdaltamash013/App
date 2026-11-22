package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.AiResponseResult
import com.example.data.remote.GeminiService
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.OnPrimary
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun ConnectivityDiagnosticDialog(
    viewModel: HealthViewModel,
    onDismiss: () -> Unit
) {
    val report by viewModel.connectivityReport.collectAsState()
    val lastAiDiag by viewModel.lastAiDiagnostic.collectAsState()
    var isTestingAi by remember { mutableStateOf(false) }
    var testResultText by remember { mutableStateOf<String?>(null) }
    var testResultSuccess by remember { mutableStateOf<Boolean?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(PrimaryTeal.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Connectivity & AI Diagnostics",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Real-time network, backend reachability & model status",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section 1: Device Network
                DiagnosticItemCard(
                    icon = Icons.Default.Wifi,
                    title = "Device Network",
                    statusText = if (report.isDeviceConnected) "Connected (Active Interface)" else "Disconnected / No Network",
                    isGood = report.isDeviceConnected
                )

                // Section 2: Backend Reachability
                DiagnosticItemCard(
                    icon = Icons.Default.Cloud,
                    title = "Backend Reachability (Google Generative AI)",
                    statusText = if (report.isBackendReachable) "Reachable (${report.lastLatencyMs} ms probe)" else "Unreachable / High latency",
                    isGood = report.isBackendReachable
                )

                // Section 3: AI Service & Provider
                val apiKeyMasked = remember {
                    val key = GeminiService.resolveApiKey()
                    if (key.length > 8) "${key.take(4)}...${key.takeLast(4)}" else if (key.isNotBlank()) "Configured" else "Missing"
                }
                DiagnosticItemCard(
                    icon = Icons.Default.Psychology,
                    title = "AI Provider & Model",
                    statusText = "Provider: Google Gemini\nModels: ${GeminiService.PRIMARY_MODEL} (fallback: gemma-4-26b-a4b-it)\nAPI Key: $apiKeyMasked",
                    isGood = report.hasValidApiKey
                )

                // Section 4: Research Service
                DiagnosticItemCard(
                    icon = Icons.Default.Search,
                    title = "Medical Research Grounding",
                    statusText = "WHO / MoHFW / ICMR verified guidelines retrieval active",
                    isGood = true
                )

                // Failure reason if any
                if (!report.failureReason.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Notice / Reason:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            )
                            Text(
                                text = report.failureReason ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onErrorContainer)
                            )
                        }
                    }
                }

                // Last AI Request Log
                val lastDiag = lastAiDiag ?: GeminiService.lastDiagnosticResult
                if (lastDiag != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Last Online AI Request:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Status: ${if (lastDiag.success) "🟢 SUCCESS" else "🔴 FAILED"}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                            )
                            if (lastDiag.modelUsed != null) {
                                Text(
                                    text = "Model used: ${lastDiag.modelUsed}",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                                )
                            }
                            Text(
                                text = "Latency: ${lastDiag.latencyMs} ms",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp)
                            )
                            if (lastDiag.developerLog.isNotBlank()) {
                                Text(
                                    text = "Log: ${lastDiag.developerLog}",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = if (lastDiag.success) LowRiskGreen else EmergencyRed
                                    )
                                )
                            }
                        }
                    }
                }

                // Real Live Probe Result Box
                AnimatedVisibility(visible = testResultText != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (testResultSuccess == true) LowRiskGreen.copy(alpha = 0.15f) else EmergencyRed.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (testResultSuccess == true) "🟢 Live AI Request Succeeded:" else "🔴 Live AI Request Failed:",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (testResultSuccess == true) LowRiskGreen else EmergencyRed
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = testResultText ?: "",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp)
                            )
                        }
                    }
                }

                // Interactive Test Button
                Button(
                    onClick = {
                        isTestingAi = true
                        testResultText = null
                        testResultSuccess = null
                        viewModel.testOnlineAiConnection { result ->
                            isTestingAi = false
                            testResultSuccess = result.success
                            testResultText = if (result.success) {
                                "${result.text}\n\n[Model: ${result.modelUsed} | Latency: ${result.latencyMs}ms]"
                            } else {
                                "${result.errorMessage}\n\n[Log: ${result.developerLog}]"
                            }
                        }
                    },
                    enabled = !isTestingAi,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_online_ai_button")
                ) {
                    if (isTestingAi) {
                        CircularProgressIndicator(
                            color = OnPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Testing Live AI Backend...")
                    } else {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Live Online AI Request")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_diagnostics_button")
            ) {
                Text("Close")
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = { viewModel.refreshConnectivityStatus() },
                modifier = Modifier.testTag("reprobe_connectivity_button")
            ) {
                Text("Re-Probe Network")
            }
        }
    )
}

@Composable
private fun DiagnosticItemCard(
    icon: ImageVector,
    title: String,
    statusText: String,
    isGood: Boolean
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (isGood) LowRiskGreen.copy(alpha = 0.2f) else EmergencyRed.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isGood) Icons.Default.CheckCircle else Icons.Default.Error,
                    contentDescription = null,
                    tint = if (isGood) LowRiskGreen else EmergencyRed,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}
