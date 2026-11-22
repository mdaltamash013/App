package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stop
import com.example.ui.components.LocationStatusCard
import com.example.ui.screens.wellness.SupportRequestDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.MessageSender
import com.example.data.model.VoiceState
import com.example.ui.components.VoiceWaveform
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.EmergencyRedContainer
import com.example.ui.theme.OnPrimary
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.SecondarySage
import com.example.ui.theme.TertiaryWarm
import com.example.ui.theme.WarmBorderStroke
import com.example.ui.theme.WarmBorderSubtle
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun HomeScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val audioRms by viewModel.audioRms.collectAsState()
    val isHandsFree by viewModel.isHandsFree.collectAsState()
    val isVoiceSessionActive by viewModel.isVoiceSessionActive.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val isTypingMode by viewModel.isTypingMode.collectAsState()
    val voiceFeedbackMessage by viewModel.voiceFeedbackMessage.collectAsState()
    val locationState by viewModel.locationState.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarse = permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onLocationPermissionResult(fine || coarse)
    }

    var typingInputText by remember { mutableStateOf("") }
    var showHomeSupportDialog by remember { mutableStateOf(false) }
    val latestWellnessRecord by viewModel.latestWellnessRecord.collectAsState()

    if (showHomeSupportDialog) {
        SupportRequestDialog(
            language = language,
            onDismiss = { showHomeSupportDialog = false },
            onSubmit = { cat, details, urgency, method, name, unit ->
                viewModel.submitSupportRequest(cat, details, urgency, method, name, unit)
                showHomeSupportDialog = false
            }
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (voiceState == VoiceState.LISTENING) 1.25f else 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Empathetic Personal Greeting
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (language == AppLanguage.HINDI) "नमस्ते, आपका स्वागत है" else "Welcome, friend",
                            style = MaterialTheme.typography.titleLarge.copy(
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "हम आपके परिवार के स्वास्थ्य के लिए 24/7 उपस्थित हैं" else "Here for you and your family around the clock",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }

                    // Gentle Trust Stamp
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "🌿", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "24/7 Care",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            )
                        }
                    }
                }
            }
        }

        // Hero Rural Health Banner Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_rural_health),
                        contentDescription = "Rural healthcare guidance",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp),
                        contentScale = ContentScale.Crop
                    )
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "आवाज़ से बताएं अपनी तकलीफ" else "Speak what is wrong.",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) {
                                "डॉक्टर से मिलने से पहले लक्षणों की सही समझ और सुरक्षित घरेलू देखभाल।"
                            } else {
                                "AI-assisted health guidance and navigation for rural & underserved families."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }

        // Small Location Status Component
        item {
            LocationStatusCard(
                locationState = locationState,
                language = language,
                onRetryLocation = { viewModel.retryLocation() },
                onRequestPermission = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            android.Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        // Central Voice Section (PRIMARY INTERACTION)
        item {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, WarmBorderStroke),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "आज हम आपकी क्या मदद कर सकते हैं?" else "How can I help you today?",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center
                        )
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI) "अपने लक्षण, दर्द या स्वास्थ्य संबंधी चिंता बोलकर बताएं।" else "Tell me about your symptoms, pain, or health concern.",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    )

                    // Active Continuous Voice Mode Badge
                    if (isVoiceSessionActive) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = EmergencyRed.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.3f)),
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(EmergencyRed)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "🔴 रियल-टाइम वॉयस सत्र चालू है (Continuous Voice)" else "🔴 LIVE Voice Session (Continuous)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = EmergencyRed
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // DOMINANT MICROPHONE BUTTON
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(130.dp)
                    ) {
                        // Pulsing outer ripple when listening
                        if (voiceState == VoiceState.LISTENING || isVoiceSessionActive) {
                            Box(
                                modifier = Modifier
                                    .size(130.dp)
                                    .scale(pulseScale)
                                    .clip(CircleShape)
                                    .background(if (isVoiceSessionActive) EmergencyRed.copy(alpha = 0.15f) else PrimaryTeal.copy(alpha = 0.2f))
                            )
                        }

                        val micBgColor = when {
                            isVoiceSessionActive && voiceState == VoiceState.LISTENING -> EmergencyRed
                            voiceState == VoiceState.IDLE -> PrimaryTeal
                            voiceState == VoiceState.LISTENING -> SecondarySage
                            voiceState == VoiceState.PROCESSING -> MaterialTheme.colorScheme.tertiary
                            voiceState == VoiceState.SPEAKING -> TertiaryWarm
                            voiceState == VoiceState.ERROR -> EmergencyRed
                            else -> PrimaryTeal
                        }

                        Surface(
                            shape = CircleShape,
                            color = micBgColor,
                            shadowElevation = 6.dp,
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .clickable {
                                    viewModel.handleMicTap()
                                }
                                .testTag("primary_mic_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = when {
                                        voiceState == VoiceState.SPEAKING -> Icons.Default.Stop
                                        isVoiceSessionActive || voiceState == VoiceState.LISTENING -> Icons.Default.Stop
                                        else -> Icons.Default.Mic
                                    },
                                    contentDescription = when {
                                        voiceState == VoiceState.SPEAKING -> "Tap to interrupt speech"
                                        isVoiceSessionActive -> "Stop Voice Mode"
                                        else -> "Tap to talk"
                                    },
                                    tint = OnPrimary,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Voice State Indicator Text with Friendly Personality & Feedback
                    val statusText = when {
                        voiceState == VoiceState.SPEAKING -> if (language == AppLanguage.HINDI) "🔊 LifeCare 24/7 बोल रहा है... (रोकने के लिए टैप करें)" else "🔊 LifeCare 24/7 is speaking... (Tap to interrupt)"
                        voiceState == VoiceState.PROCESSING -> if (language == AppLanguage.HINDI) "🧠 सोच रहे हैं..." else "🧠 Thinking..."
                        voiceState == VoiceState.LISTENING -> if (language == AppLanguage.HINDI) "🎙️ सुन रहे हैं... (Listening...)" else "🎙️ Listening... Speak naturally"
                        voiceFeedbackMessage != null -> voiceFeedbackMessage ?: ""
                        isVoiceSessionActive -> if (language == AppLanguage.HINDI) "🎙️ अगला प्रश्न पूछने के लिए बोलें..." else "🎙️ Listening for next question..."
                        voiceState == VoiceState.ERROR -> if (language == AppLanguage.HINDI) "⚠️ आवाज़ समझ नहीं आई, फिर से बोलें" else "⚠️ Couldn't hear speech, tap to speak"
                        else -> if (language == AppLanguage.HINDI) "🎤 बात करने के लिए छुएं (Tap to talk)" else "🎤 Tap to talk"
                    }

                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (voiceState == VoiceState.ERROR) EmergencyRed else MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Audio Waveform Visualizer
                    VoiceWaveform(
                        voiceState = voiceState,
                        audioRms = audioRms,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    // Friendly Conversational Sample Chips (English, Hindi, Hinglish)
                    if (voiceState == VoiceState.IDLE) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "💡 उदाहरण के लिए बोलें या टैप करें:" else "💡 Try speaking or tap to ask:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val samplePrompts = if (language == AppLanguage.HINDI) {
                                listOf(
                                    "मुझे बहुत थकान लग रही है",
                                    "यार सुबह से सिरदर्द हो रहा है",
                                    "पेट में दर्द और गैस है"
                                )
                            } else {
                                listOf(
                                    "I'm feeling really tired today",
                                    "Yaar mujhe subah se headache ho raha hai",
                                    "What is Python?"
                                )
                            }

                            samplePrompts.forEach { prompt ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable {
                                            viewModel.startVoiceSession()
                                            viewModel.processSymptomInput(prompt)
                                        }
                                ) {
                                    Text(
                                        text = prompt,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        ),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Error retry buttons
                    if (voiceState == VoiceState.ERROR) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.startVoiceSession() },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                modifier = Modifier.testTag("try_again_button")
                            ) {
                                Icon(Icons.Default.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.HINDI) "फिर से बोलें" else "Try Again")
                            }

                            OutlinedButton(
                                onClick = { viewModel.setTypingMode(true) },
                                modifier = Modifier.testTag("type_instead_error_button")
                            ) {
                                Icon(Icons.Default.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (language == AppLanguage.HINDI) "लिखकर बताएं" else "Type Instead")
                            }
                        }
                    }

                    // Voice Controls: Hands-Free toggle & Repeat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Continuous Voice Mode Toggle Button
                        Button(
                            onClick = { viewModel.toggleVoiceSession() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isVoiceSessionActive) EmergencyRed else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (isVoiceSessionActive) Color.White else MaterialTheme.colorScheme.onSecondaryContainer
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("hands_free_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (isVoiceSessionActive) Icons.Default.Stop else Icons.Default.Headset,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isVoiceSessionActive) {
                                    if (language == AppLanguage.HINDI) "⏹️ वॉयस मोड बंद करें" else "⏹️ Stop Voice Mode"
                                } else {
                                    if (language == AppLanguage.HINDI) "🔊 हैंड्स-फ्री मोड" else "🔊 Continuous Mode"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Repeat spoken message
                        OutlinedButton(
                            onClick = { viewModel.repeatLastVoice() },
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.testTag("repeat_voice_button")
                        ) {
                            Icon(Icons.Default.Replay, contentDescription = "Repeat", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.HINDI) "दोहराएं" else "Repeat", fontSize = 12.sp)
                        }
                    }

                    // Live Conversation Inside the Voice Interface
                    if (chatMessages.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = "💬", fontSize = 16.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.HINDI) "वॉयस बातचीत (Voice Conversation)" else "Voice Conversation",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                    if (isVoiceSessionActive) {
                                        TextButton(
                                            onClick = { viewModel.stopVoiceSession() },
                                            modifier = Modifier.testTag("end_voice_mode_button")
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.HINDI) "⏹️ बंद करें" else "⏹️ End Voice Mode",
                                                color = EmergencyRed,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Show conversation items with complete messages
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    chatMessages.takeLast(6).forEach { msg ->
                                        val isUser = msg.sender == MessageSender.USER
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(
                                                    topStart = 14.dp,
                                                    topEnd = 14.dp,
                                                    bottomStart = if (isUser) 14.dp else 2.dp,
                                                    bottomEnd = if (isUser) 2.dp else 14.dp
                                                ),
                                                color = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                                modifier = Modifier.widthIn(max = 300.dp)
                                            ) {
                                                Column(modifier = Modifier.padding(10.dp)) {
                                                    Row(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        horizontalArrangement = Arrangement.SpaceBetween,
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Text(
                                                            text = if (isUser) "You" else "LifeCare 24/7",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontWeight = FontWeight.Bold,
                                                                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
                                                            )
                                                        )
                                                        if (!isUser) {
                                                            if (msg.isOnlineAi) {
                                                                Text(
                                                                    text = "🟢 Online (${msg.modelName ?: "AI"})",
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = MaterialTheme.colorScheme.primary
                                                                    )
                                                                )
                                                            } else if (msg.isFallback) {
                                                                Text(
                                                                    text = "⚠️ Offline Fallback",
                                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                                        fontSize = 9.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = MaterialTheme.colorScheme.error
                                                                    )
                                                                )
                                                            }
                                                        }
                                                    }
                                                    Spacer(modifier = Modifier.height(2.dp))
                                                    Text(
                                                        text = msg.text,
                                                        style = MaterialTheme.typography.bodyMedium
                                                    )
                                                    if (!isUser && msg.researchSources.isNotEmpty()) {
                                                        Spacer(modifier = Modifier.height(3.dp))
                                                        Text(
                                                            text = "📚 ${msg.researchSources.first()}",
                                                            style = MaterialTheme.typography.labelSmall.copy(
                                                                fontSize = 9.sp,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                // Optional action to view full clinical details if the user chooses to
                                Spacer(modifier = Modifier.height(10.dp))
                                OutlinedButton(
                                    onClick = { onNavigate("assessment") },
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("view_assessment_from_voice_button")
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "📋 विस्तृत रिपोर्ट व दवाइयां देखें" else "📋 View Detailed Assessment Report",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Typing Fallback Toggle & Input (Secondary)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!isTypingMode) {
                    TextButton(
                        onClick = { viewModel.setTypingMode(true) },
                        modifier = Modifier.testTag("type_instead_toggle")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Keyboard,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) "⌨️ लिखकर बताएं (Type instead)" else "⌨️ Type instead",
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "लक्षण टाइप करें (Optional Typing)" else "Type your symptoms (Optional)",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                TextButton(onClick = { viewModel.setTypingMode(false) }) {
                                    Text(if (language == AppLanguage.HINDI) "🎤 आवाज़ का उपयोग करें" else "🎤 Use voice instead")
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = typingInputText,
                                    onValueChange = { typingInputText = it },
                                    placeholder = {
                                        Text(if (language == AppLanguage.HINDI) "जैसे: 2 दिन से बुखार और बदन दर्द..." else "e.g., Fever for 2 days and body pain...")
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("typing_input_field"),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Button(
                                    onClick = {
                                        if (typingInputText.isNotBlank()) {
                                            viewModel.processSymptomInput(typingInputText)
                                            typingInputText = ""
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                    modifier = Modifier.testTag("typing_send_button")
                                ) {
                                    Icon(Icons.Default.Send, contentDescription = "Send", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quick Navigation Grid
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "त्वरित सेवाएं (Quick Services)" else "Quick Services",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Emergency,
                        title = if (language == AppLanguage.HINDI) "आपातकाल" else "Emergency",
                        subtitle = "108 / 112 SOS",
                        bgColor = EmergencyRedContainer,
                        textColor = EmergencyRed,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("emergency") }
                    )

                    QuickActionCard(
                        icon = Icons.Default.LocalHospital,
                        title = if (language == AppLanguage.HINDI) "अस्पताल" else "Nearby Care",
                        subtitle = "PHC / CHC",
                        bgColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        textColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("facilities") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.Person,
                        title = if (language == AppLanguage.HINDI) "डॉक्टर से बात" else "Talk to Doctor",
                        subtitle = if (language == AppLanguage.HINDI) "टेलीमेडिसिन" else "Telemedicine",
                        bgColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
                        textColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("doctor") }
                    )

                    QuickActionCard(
                        icon = Icons.Default.MedicalServices,
                        title = if (language == AppLanguage.HINDI) "दवाइयां" else "Medicines",
                        subtitle = if (language == AppLanguage.HINDI) "जन औषधि केंद्र" else "Jan Aushadhi",
                        bgColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                        textColor = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("medicine") }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionCard(
                        icon = Icons.Default.SelfImprovement,
                        title = if (language == AppLanguage.HINDI) "मेरा कल्याण" else "My Wellness",
                        subtitle = if (language == AppLanguage.HINDI) "तनाव एवं बर्नआउट" else "Burnout & Welfare",
                        bgColor = PrimaryTeal.copy(alpha = 0.15f),
                        textColor = PrimaryTeal,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("my_wellness") }
                    )

                    QuickActionCard(
                        icon = Icons.Default.Assessment,
                        title = if (language == AppLanguage.HINDI) "स्वास्थ्य रिकॉर्ड" else "Health Records",
                        subtitle = if (language == AppLanguage.HINDI) "डिजिटल पर्चा" else "History & Triage",
                        bgColor = MaterialTheme.colorScheme.surfaceVariant,
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate("records") }
                    )
                }
            }
        }

        // Dedicated Uniformed Personnel Welfare Support Banner
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, PrimaryTeal.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = PrimaryTeal.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = PrimaryTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "कार्मिक कल्याण एवं बर्नआउट सहयोग" else "Personnel Welfare & Stress Support",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "CAPF • Police • Armed Forces",
                                    style = MaterialTheme.typography.labelSmall.copy(color = PrimaryTeal, fontWeight = FontWeight.SemiBold)
                                )
                            }
                        }

                        IconButton(onClick = { onNavigate("welfare_officer") }) {
                            Icon(
                                Icons.Default.Shield,
                                contentDescription = "Officer Dashboard",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "कल्याण सहायता, निगरानी नहीं। ड्यूटी के घंटे, शिफ्ट, फील्ड तैनाती व स्व-रिपोर्ट किए गए तनाव के आधार पर समय पर सहायता प्रदान करना।"
                        else
                            "Welfare, not surveillance. Identifies early workload fatigue & stress patterns to support uniformed personnel with confidential welfare resources.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onNavigate("my_wellness") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.SelfImprovement, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "माई वेलनेस" else "My Wellness",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { showHomeSupportDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Favorite, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "सहायता चाहिए" else "I Need Help",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Demo Scenarios Showcase (Explicitly Required for Review & Evaluation)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 18.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "🎯 डेमो परिदृश्य (Demo Scenarios)" else "🎯 Demo Scenarios",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "6 Core Flows",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val scenarios = listOf(
                        Triple(1, "🟢 Scenario 1", "Low Risk (Fever/Cold)"),
                        Triple(2, "🟡 Scenario 2", "Moderate (Diarrhea/ORS)"),
                        Triple(3, "🔴 Scenario 3", "Emergency Voice Override"),
                        Triple(4, "📴 Scenario 4", "Offline Rule Engine"),
                        Triple(5, "🇮🇳 Scenario 5", "Hindi Voice Flow"),
                        Triple(6, "💊 Scenario 6", "Medicine Availability")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(scenarios) { (num, title, desc) ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .width(160.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.triggerScenario(num) }
                                    .testTag("demo_scenario_$num")
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = title,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        ),
                                        maxLines = 2
                                    )
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
fun QuickActionCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    bgColor: Color,
    textColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = bgColor,
        border = BorderStroke(1.dp, WarmBorderStroke),
        shadowElevation = 1.dp,
        modifier = modifier
            .height(98.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = textColor,
                modifier = Modifier.size(28.dp)
            )
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        fontSize = 14.sp
                    )
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = textColor.copy(alpha = 0.8f)
                    )
                )
            }
        }
    }
}
