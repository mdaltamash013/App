package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AssessmentRecordEntity
import com.example.data.local.EmergencyContactEntity
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HealthRecordScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val language by viewModel.language.collectAsState()
    val authState by viewModel.authState.collectAsState()
    val savedAssessments by viewModel.savedAssessments.collectAsState()
    val emergencyContacts by viewModel.emergencyContacts.collectAsState()

    var showAddContactDialog by remember { mutableStateOf(false) }

    fun dial(phone: String) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
            context.startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Section 0: User Profile & Authentication Status Card
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (authState.isAuthenticated) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f) else MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth().testTag("profile_account_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(PrimaryTeal.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (authState.isAuthenticated) Icons.Default.VerifiedUser else Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = PrimaryTeal,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (authState.isAuthenticated)
                                        (authState.user?.name ?: "User")
                                    else
                                        (if (language == AppLanguage.HINDI) "अतिथि मोड (Guest)" else "Guest Mode"),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = if (authState.isAuthenticated) {
                                        authState.user?.email ?: authState.user?.phone ?: "Logged in"
                                    } else {
                                        if (language == AppLanguage.HINDI) "खाते से जुड़ें या लॉगिन करें" else "Tap below to sign in or create account"
                                    },
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        if (authState.isAuthenticated) {
                            OutlinedButton(
                                onClick = {
                                    viewModel.logout {
                                        onNavigate("welcome")
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("btn_logout")
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "लॉगआउट" else "Log Out",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        } else {
                            Button(
                                onClick = { onNavigate("welcome") },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("btn_go_to_login")
                            ) {
                                Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.HINDI) "लॉगिन करें" else "Sign In",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Emergency Contacts Manager
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "👨‍👩‍👧 आपातकालीन संपर्क (Contacts)" else "👨‍👩‍👧 Emergency Contacts",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Button(
                            onClick = { showAddContactDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("add_contact_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (language == AppLanguage.HINDI) "जोड़ें" else "Add", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (emergencyContacts.isEmpty()) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "कोई संपर्क सहेजा नहीं गया है।" else "No emergency contacts saved yet.",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    } else {
                        for (contact in emergencyContacts) {
                            EmergencyContactRow(
                                contact = contact,
                                onCall = { dial(contact.phone) },
                                onDelete = { viewModel.deleteEmergencyContact(contact) }
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // Section 2: Saved Assessments History
        item {
            Text(
                text = if (language == AppLanguage.HINDI) "📋 पिछली स्वास्थ्य जांच (Past Assessments)" else "📋 Past Assessments & Records",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }

        if (savedAssessments.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "अभी कोई सहेजा गया रिकॉर्ड नहीं है" else "No saved health assessments yet",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI) {
                                "मुख्य स्क्रीन पर आवाज़ से लक्षण बताएं और 'सहेजें' दबाएं।"
                            } else {
                                "Speak your symptoms on the Home screen and tap 'Save' to keep records."
                            },
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                        )
                    }
                }
            }
        } else {
            items(savedAssessments) { record ->
                SavedRecordCard(record = record, language = language)
            }
        }
    }

    // Add Contact Dialog
    if (showAddContactDialog) {
        var name by remember { mutableStateOf("") }
        var relation by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddContactDialog = false },
            icon = { Icon(Icons.Default.PersonAdd, contentDescription = null, tint = PrimaryTeal) },
            title = { Text(if (language == AppLanguage.HINDI) "नया आपातकालीन संपर्क जोड़ें" else "Add Emergency Contact") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text(if (language == AppLanguage.HINDI) "नाम (उदा. आशा कार्यकर्ता/भाई)" else "Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = relation,
                        onValueChange = { relation = it },
                        label = { Text(if (language == AppLanguage.HINDI) "संबंध (उदा. ASHA Worker, Kin)" else "Relationship") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(if (language == AppLanguage.HINDI) "फोन नंबर (उदा. +91 98...)" else "Phone Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && phone.isNotBlank()) {
                            viewModel.addEmergencyContact(name, relation.ifBlank { "Contact" }, phone)
                            showAddContactDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal)
                ) {
                    Text(if (language == AppLanguage.HINDI) "सहेजें" else "Save Contact")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddContactDialog = false }) {
                    Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                }
            }
        )
    }
}

@Composable
fun EmergencyContactRow(
    contact: EmergencyContactEntity,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "${contact.relation} • ${contact.phone}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = onCall,
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.Call, contentDescription = "Call", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", fontSize = 11.sp)
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun SavedRecordCard(record: AssessmentRecordEntity, language: AppLanguage) {
    val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(record.timestamp))

    val (riskColor, riskBg) = when (record.riskLevel) {
        TriageLevel.LOW.name -> Pair(LowRiskGreen, LowRiskGreenContainer)
        TriageLevel.MODERATE.name -> Pair(ModerateYellow, ModerateYellowContainer)
        else -> Pair(EmergencyRed, EmergencyRedContainer)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = riskBg
                ) {
                    Text(
                        text = record.riskLevel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = riskColor
                        ),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = record.mainComplaint,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = record.guidanceTitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            )
        }
    }
}
