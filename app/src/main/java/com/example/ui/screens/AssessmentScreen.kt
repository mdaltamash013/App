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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.data.model.TriageLevel
import com.example.data.model.VoiceState
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.LowRiskGreenContainer
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.ModerateYellowContainer
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun AssessmentScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val currentAssessment by viewModel.currentAssessment.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val isVoiceSessionActive by viewModel.isVoiceSessionActive.collectAsState()

    var hasSaved by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Triage Level Banner
        item {
            val assessment = currentAssessment
            if (assessment != null) {
                val (bgColor, textColor, label) = when (assessment.riskLevel) {
                    TriageLevel.LOW -> Triple(
                        LowRiskGreenContainer,
                        LowRiskGreen,
                        if (language == AppLanguage.HINDI) "🟢 कम जोखिम — सामान्य सहायक मार्गदर्शन" else "🟢 Low Risk — Safe Supportive Guidance"
                    )
                    TriageLevel.MODERATE -> Triple(
                        ModerateYellowContainer,
                        ModerateYellow,
                        if (language == AppLanguage.HINDI) "🟡 मध्यम जोखिम — डॉक्टर परामर्श अनुशंसित" else "🟡 Moderate Risk — Doctor Consultation Recommended"
                    )
                    else -> Triple(
                        EmergencyRedContainer,
                        EmergencyRed,
                        if (language == AppLanguage.HINDI) "🔴 आपातकालीन स्थिति — तुरंत अस्पताल जाएं" else "🔴 Possible Emergency — Immediate Care Required"
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = bgColor,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(textColor)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
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

        // Conversational Dialogue Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
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
                            text = if (language == AppLanguage.HINDI) "सहानुभूतिपूर्ण स्वास्थ्य वार्तालाप" else "Voice Health Dialogue",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        OutlinedButton(
                            onClick = { viewModel.repeatLastVoice() },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("repeat_response_button")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "Repeat", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.HINDI) "सुनाएं" else "Listen", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    for (message in chatMessages) {
                        ChatBubble(message = message, language = language)
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }

        // Supportive Guidance & Care Steps
        item {
            val assessment = currentAssessment
            if (assessment != null && assessment.guidancePoints.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = PrimaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "सुरक्षित घरेलू देखभाल निर्देश" else "Safe Supportive Care Instructions",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        for ((idx, point) in assessment.guidancePoints.withIndex()) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(22.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "${idx + 1}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 20.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Relevant Follow-up Questions Card
        item {
            val assessment = currentAssessment
            if (assessment != null && assessment.followUpQuestions.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "प्रासंगिक फॉलो-अप प्रश्न (बोलकर उत्तर दें)" else "Relevant Follow-up Questions (Answer by Voice)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        for (q in assessment.followUpQuestions) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            ) {
                                Text(
                                    text = "• $q",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }

        // Red Flags / Warning Signs
        item {
            val assessment = currentAssessment
            if (assessment != null && assessment.warningSigns.isNotEmpty()) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = EmergencyRedContainer.copy(alpha = 0.3f)),
                    border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = EmergencyRed,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "⚠️ खतरे के संकेत (दिखें तो तुरंत डॉक्टर के पास जाएं)" else "⚠️ Warning Signs (Seek Doctor Immediately)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmergencyRed
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        for (w in assessment.warningSigns) {
                            Text(
                                text = "• $w",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = EmergencyRed,
                                    lineHeight = 18.sp
                                ),
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Toolbar: Continue Voice, Summary, Save, Doctor
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Continuous Voice Session Status Banner
                if (isVoiceSessionActive) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = EmergencyRed.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(EmergencyRed)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val statusDesc = when (voiceState) {
                                VoiceState.SPEAKING -> if (language == AppLanguage.HINDI) "🔊 उत्तर सुनाया जा रहा है..." else "🔊 Speaking answer..."
                                VoiceState.PROCESSING -> if (language == AppLanguage.HINDI) "🧠 उत्तर तैयार किया जा रहा है..." else "🧠 Thinking & answering..."
                                VoiceState.LISTENING -> if (language == AppLanguage.HINDI) "🎙️ सुन रहे हैं... बोलिए" else "🎙️ Listening... Speak now"
                                else -> if (language == AppLanguage.HINDI) "🔴 लाइव बातचीत: अगला प्रश्न पूछें" else "🔴 Live Session: Listening for next question..."
                            }
                            Text(
                                text = statusDesc,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = EmergencyRed
                                )
                            )
                        }
                    }
                }

                // Primary Continue Voice Input
                Button(
                    onClick = {
                        if (isVoiceSessionActive || voiceState == VoiceState.LISTENING || voiceState == VoiceState.SPEAKING) {
                            viewModel.stopVoiceSession()
                        } else {
                            viewModel.startVoiceSession()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isVoiceSessionActive) EmergencyRed else PrimaryTeal
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("continue_voice_button")
                ) {
                    Icon(
                        imageVector = if (isVoiceSessionActive) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isVoiceSessionActive) {
                            if (language == AppLanguage.HINDI) "⏹️ वॉयस बातचीत बंद करें" else "⏹️ Stop Continuous Voice Mode"
                        } else {
                            if (language == AppLanguage.HINDI) "🎤 आवाज़ से बातचीत जारी रखें" else "🎤 Start Continuous Voice Mode"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                // Quick typing follow-up field
                var followUpInputText by remember { mutableStateOf("") }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = followUpInputText,
                        onValueChange = { followUpInputText = it },
                        placeholder = {
                            Text(
                                text = if (language == AppLanguage.HINDI) "नया प्रश्न या उत्तर टाइप करें..." else "Ask another question...",
                                fontSize = 13.sp
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("assessment_followup_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (followUpInputText.isNotBlank()) {
                                viewModel.processSymptomInput(followUpInputText)
                                followUpInputText = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier.testTag("assessment_followup_send")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // View Patient Summary
                    OutlinedButton(
                        onClick = { onNavigate("summary") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("view_summary_button")
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == AppLanguage.HINDI) "रोगी सारांश" else "Summary", maxLines = 1)
                    }

                    // Save to Health Records
                    OutlinedButton(
                        onClick = {
                            viewModel.saveCurrentAssessmentToRecord()
                            hasSaved = true
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_record_button")
                    ) {
                        Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (hasSaved) {
                                if (language == AppLanguage.HINDI) "सहेज लिया ✓" else "Saved ✓"
                            } else {
                                if (language == AppLanguage.HINDI) "सहेजें" else "Save"
                            },
                            maxLines = 1
                        )
                    }
                }

                // Talk to Doctor link
                Button(
                    onClick = { onNavigate("doctor") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("consult_doctor_button")
                ) {
                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) "👨‍⚕️ डॉक्टर से परामर्श करें (टेलीमेडिसिन)" else "👨‍⚕️ Consult Qualified Doctor (Telemedicine)",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage, language: AppLanguage) {
    val isUser = message.sender == MessageSender.USER
    val isEmergency = message.isEmergency

    val bgColor = when {
        isEmergency -> EmergencyRedContainer
        isUser -> PrimaryTeal
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val textColor = when {
        isEmergency -> EmergencyRed
        isUser -> Color.White
        else -> MaterialTheme.colorScheme.onSurface
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = bgColor,
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isUser) {
                            if (language == AppLanguage.HINDI) "आप (You)" else "You"
                        } else {
                            "LifeCare 24/7"
                        },
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor.copy(alpha = 0.7f),
                            fontSize = 10.sp
                        )
                    )

                    if (!isUser) {
                        if (message.isOnlineAi) {
                            Text(
                                text = "🟢 Online (${message.modelName ?: "AI"})",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = LowRiskGreen,
                                    fontSize = 9.sp
                                )
                            )
                        } else if (message.isFallback) {
                            Text(
                                text = "⚠️ Offline Fallback",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ModerateYellow,
                                    fontSize = 9.sp
                                )
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = textColor,
                        lineHeight = 19.sp
                    )
                )
                if (!isUser && message.researchSources.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📚 Grounded in: ${message.researchSources.first()}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 9.sp,
                            color = textColor.copy(alpha = 0.75f)
                        )
                    )
                }
            }
        }
    }
}
