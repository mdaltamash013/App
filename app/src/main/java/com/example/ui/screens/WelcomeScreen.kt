package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.PrimaryTerracotta
import com.example.ui.theme.SecondarySage
import com.example.ui.viewmodel.HealthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WelcomeScreen(
    viewModel: HealthViewModel,
    onNavigateToHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Bottom sheet dialog states
    var showPhoneAuthSheet by remember { mutableStateOf(false) }
    var showEmailAuthSheet by remember { mutableStateOf(false) }
    var emailAuthInitialTab by remember { mutableIntStateOf(0) } // 0 = Sign In, 1 = Create Account

    // Auto navigate if already authenticated or skipped
    LaunchedEffect(authState.isAuthenticated, authState.isSkipped) {
        if (authState.isAuthenticated || authState.isSkipped) {
            onNavigateToHome()
        }
    }

    LaunchedEffect(authState.errorMessage) {
        authState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearAuthMessages()
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("welcome_screen"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Responsive container max width for tablet & desktop ergonomics
                Column(
                    modifier = Modifier.widthIn(max = 480.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Top Bar Header: Language Switcher and Trust Badge
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Trust Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "सुरक्षित स्वास्थ्य साथी" else "Trusted Health AI",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }

                        // Language Toggle (English / हिन्दी)
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .clickable { viewModel.toggleLanguage() }
                                .testTag("welcome_language_toggle")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Language",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ENGLISH) "हिन्दी" else "English",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Brand Logo & Badge
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(PrimaryTerracotta, Color(0xFF8A3619))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🩺",
                            fontSize = 38.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Brand Title
                    Text(
                        text = "LifeCare 24/7",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = (-0.5).sp
                        ),
                        textAlign = TextAlign.Center
                    )

                    // Tagline (as requested: "Your Smart Health Companion")
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "आपका स्मार्ट स्वास्थ्य साथी"
                        else
                            "Your Smart Health Companion",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "त्वरित एआई ट्राइएज, आपातकालीन सहायता और सत्यापित स्वास्थ्य सेवाएं"
                        else
                            "Voice-first triage, 24/7 emergency SOS, and verified local care",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
                    )

                    // Visual Healthcare Illustration Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 20.dp)
                    ) {
                        Column {
                            Image(
                                painter = painterResource(id = R.drawable.img_welcome_healthcare),
                                contentDescription = "Healthcare Companion Illustration",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Trust Highlights Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                WelcomeFeaturePill("⚡", if (language == AppLanguage.HINDI) "त्वरित ट्राइएज" else "Instant Triage")
                                WelcomeFeaturePill("🚑", if (language == AppLanguage.HINDI) "24x7 आपातकाल" else "24x7 SOS")
                                WelcomeFeaturePill("💊", if (language == AppLanguage.HINDI) "सत्यापित दवा" else "Jan Aushadhi")
                            }
                        }
                    }

                    // Loading Indicator if Auth is in progress
                    if (authState.isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.5.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "कृपया प्रतीक्षा करें..." else "Signing in securely...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Main Action 1: Continue with Google
                    Button(
                        onClick = {
                            viewModel.loginWithGoogle(
                                preferredName = "Health Seeker",
                                preferredEmail = "patient@lifecare247.ai"
                            ) { success ->
                                if (success) {
                                    onNavigateToHome()
                                }
                            }
                        },
                        enabled = !authState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_continue_google")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Google "G" Badge
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                shadowElevation = 1.dp,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "G",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF4285F4)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "Google के साथ जारी रखें" else "Continue with Google",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Main Action 2: Continue with Mobile Number
                    Button(
                        onClick = { showPhoneAuthSheet = true },
                        enabled = !authState.isLoading,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("btn_continue_mobile")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (language == AppLanguage.HINDI) "मोबाइल नंबर से जारी रखें" else "Continue with Mobile Number",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary Options: Sign In / Create Account via Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                emailAuthInitialTab = 0
                                showEmailAuthSheet = true
                            },
                            enabled = !authState.isLoading,
                            modifier = Modifier.testTag("btn_sign_in_email")
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "लॉगिन करें" else "Sign In",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }

                        Text(
                            text = "•",
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        TextButton(
                            onClick = {
                                emailAuthInitialTab = 1
                                showEmailAuthSheet = true
                            },
                            enabled = !authState.isLoading,
                            modifier = Modifier.testTag("btn_create_account")
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "नया खाता बनाएं" else "Create Account",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Required Action: Clearly visible "Skip for now" option
                    OutlinedButton(
                        onClick = {
                            viewModel.skipAuth {
                                onNavigateToHome()
                            }
                        },
                        enabled = !authState.isLoading,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)),
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(46.dp)
                            .testTag("btn_skip_for_now")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "अभी छोड़ें (बाद में करें) →" else "Skip for now →",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Privacy & Trust note
                    Text(
                        text = if (language == AppLanguage.HINDI)
                            "बिना लॉगिन के सभी स्वास्थ्य ट्राइएज और आपातकालीन सुविधाएं उपलब्ध हैं"
                        else
                            "All symptom assessments and emergency tools work offline & without login",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                            fontSize = 11.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }

    // Modal Bottom Sheet: Mobile Number OTP Authentication
    if (showPhoneAuthSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showPhoneAuthSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            PhoneAuthSheetContent(
                language = language,
                isLoading = authState.isLoading,
                onSendOtp = { phone, onDone ->
                    viewModel.sendPhoneOtp(phone, onDone)
                },
                onVerifyOtp = { phone, otp, onDone ->
                    viewModel.verifyPhoneOtp(phone, otp) { success, msg ->
                        onDone(success, msg)
                        if (success) {
                            showPhoneAuthSheet = false
                            onNavigateToHome()
                        }
                    }
                },
                onDismiss = { showPhoneAuthSheet = false }
            )
        }
    }

    // Modal Bottom Sheet: Email Login / Create Account
    if (showEmailAuthSheet) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showEmailAuthSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            EmailAuthSheetContent(
                initialTab = emailAuthInitialTab,
                language = language,
                isLoading = authState.isLoading,
                onLogin = { email, password, onDone ->
                    viewModel.loginWithEmail(email, password) { success, msg ->
                        onDone(success, msg)
                        if (success) {
                            showEmailAuthSheet = false
                            onNavigateToHome()
                        }
                    }
                },
                onRegister = { name, email, password, onDone ->
                    viewModel.registerWithEmail(name, email, password) { success, msg ->
                        onDone(success, msg)
                        if (success) {
                            showEmailAuthSheet = false
                            onNavigateToHome()
                        }
                    }
                },
                onDismiss = { showEmailAuthSheet = false }
            )
        }
    }
}

@Composable
private fun WelcomeFeaturePill(emoji: String, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun PhoneAuthSheetContent(
    language: AppLanguage,
    isLoading: Boolean,
    onSendOtp: (String, (Boolean, String) -> Unit) -> Unit,
    onVerifyOtp: (String, String, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var otpSent by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (language == AppLanguage.HINDI) "मोबाइल नंबर सत्यापन" else "Mobile Number Verification",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = if (language == AppLanguage.HINDI)
                        "एक बार का ओटीपी कोड भेजा जाएगा"
                    else
                        "We'll send a 6-digit OTP code to verify",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (errorMessage != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer)
                    )
                }
            }
        }

        if (statusMessage != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = LowRiskGreen.copy(alpha = 0.15f),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = LowRiskGreen,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = LowRiskGreen)
                    )
                }
            }
        }

        // Phone Input Field
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.length <= 10 && it.all { char -> char.isDigit() }) {
                    phoneNumber = it
                    errorMessage = null
                }
            },
            label = { Text(if (language == AppLanguage.HINDI) "मोबाइल नंबर" else "Mobile Number") },
            placeholder = { Text("9876543210") },
            leadingIcon = {
                Text(
                    text = "🇮🇳 +91",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 12.dp, end = 6.dp)
                )
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Phone,
                imeAction = if (!otpSent) ImeAction.Send else ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onSend = {
                    if (phoneNumber.length == 10) {
                        focusManager.clearFocus()
                        onSendOtp(phoneNumber) { ok, msg ->
                            if (ok) {
                                otpSent = true
                                statusMessage = msg
                            } else {
                                errorMessage = msg
                            }
                        }
                    }
                }
            ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_phone_number"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!otpSent) {
            Button(
                onClick = {
                    if (phoneNumber.length < 10) {
                        errorMessage = if (language == AppLanguage.HINDI)
                            "कृपया 10 अंकों का मान्य मोबाइल नंबर दर्ज करें"
                        else
                            "Please enter a valid 10-digit mobile number"
                        return@Button
                    }
                    focusManager.clearFocus()
                    errorMessage = null
                    onSendOtp(phoneNumber) { ok, msg ->
                        if (ok) {
                            otpSent = true
                            statusMessage = msg
                        } else {
                            errorMessage = msg
                        }
                    }
                },
                enabled = !isLoading && phoneNumber.length == 10,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_send_otp")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = if (language == AppLanguage.HINDI) "ओटीपी प्राप्त करें" else "Send OTP Code",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        } else {
            // OTP Code Input Field
            OutlinedTextField(
                value = otpCode,
                onValueChange = {
                    if (it.length <= 6 && it.all { char -> char.isDigit() }) {
                        otpCode = it
                        errorMessage = null
                    }
                },
                label = { Text(if (language == AppLanguage.HINDI) "6-अंकों का ओटीपी" else "6-Digit OTP Code") },
                placeholder = { Text("123456") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (otpCode.length == 6) {
                            focusManager.clearFocus()
                            onVerifyOtp(phoneNumber, otpCode) { ok, msg ->
                                if (!ok) errorMessage = msg
                            }
                        }
                    }
                ),
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_otp_code"),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (otpCode.length != 6) {
                        errorMessage = if (language == AppLanguage.HINDI)
                            "कृपया 6 अंकों का ओटीपी दर्ज करें"
                        else
                            "Please enter the complete 6-digit OTP"
                        return@Button
                    }
                    focusManager.clearFocus()
                    errorMessage = null
                    onVerifyOtp(phoneNumber, otpCode) { ok, msg ->
                        if (!ok) errorMessage = msg
                    }
                },
                enabled = !isLoading && otpCode.length == 6,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("btn_verify_otp")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text(
                        text = if (language == AppLanguage.HINDI) "सत्यापित करें और आगे बढ़ें" else "Verify & Continue",
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = {
                    otpCode = ""
                    errorMessage = null
                    onSendOtp(phoneNumber) { ok, msg ->
                        if (ok) statusMessage = if (language == AppLanguage.HINDI) "नया ओटीपी भेजा गया" else "Resent OTP successfully"
                    }
                },
                enabled = !isLoading
            ) {
                Text(
                    text = if (language == AppLanguage.HINDI) "ओटीपी दोबारा भेजें" else "Resend OTP",
                    style = MaterialTheme.typography.labelLarge.copy(color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
fun EmailAuthSheetContent(
    initialTab: Int,
    language: AppLanguage,
    isLoading: Boolean,
    onLogin: (String, String, (Boolean, String) -> Unit) -> Unit,
    onRegister: (String, String, String, (Boolean, String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedTab == 0)
                    (if (language == AppLanguage.HINDI) "खाते में लॉगिन करें" else "Sign In to Account")
                else
                    (if (language == AppLanguage.HINDI) "नया खाता बनाएं" else "Create New Account"),
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            IconButton(onClick = onDismiss) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tab Selector: Sign In vs Create Account
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.clip(RoundedCornerShape(10.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    errorMessage = null
                },
                text = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "लॉगिन" else "Sign In",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    errorMessage = null
                },
                text = {
                    Text(
                        text = if (language == AppLanguage.HINDI) "पंजीकरण" else "Sign Up",
                        fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (errorMessage != null) {
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onErrorContainer)
                    )
                }
            }
        }

        // Full Name (Only for Registration)
        if (selectedTab == 1) {
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    errorMessage = null
                },
                label = { Text(if (language == AppLanguage.HINDI) "पूरा नाम" else "Full Name") },
                placeholder = { Text("Dr. Rahul Sharma") },
                leadingIcon = {
                    Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                singleLine = true,
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_auth_name"),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text(if (language == AppLanguage.HINDI) "ईमेल आईडी" else "Email Address") },
            placeholder = { Text("you@example.com") },
            leadingIcon = {
                Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_auth_email"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text(if (language == AppLanguage.HINDI) "पासवर्ड (कम से कम 6 अक्षर)" else "Password (min 6 chars)") },
            leadingIcon = {
                Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus()
                }
            ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_auth_password"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Submit Button
        Button(
            onClick = {
                focusManager.clearFocus()
                if (selectedTab == 0) {
                    onLogin(email, password) { ok, msg ->
                        if (!ok) errorMessage = msg
                    }
                } else {
                    onRegister(name, email, password) { ok, msg ->
                        if (!ok) errorMessage = msg
                    }
                }
            },
            enabled = !isLoading,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("btn_submit_auth")
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text(
                    text = if (selectedTab == 0)
                        (if (language == AppLanguage.HINDI) "लॉगिन करें" else "Sign In")
                    else
                        (if (language == AppLanguage.HINDI) "खाता बनाएं" else "Create Account"),
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
