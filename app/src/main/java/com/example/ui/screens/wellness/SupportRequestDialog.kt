package com.example.ui.screens.wellness

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.window.Dialog
import com.example.data.model.AppLanguage
import com.example.data.wellness.SupportCategory
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryTeal

@Composable
fun SupportRequestDialog(
    language: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (SupportCategory, String, String, String, String, String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(SupportCategory.COUNSELING) }
    var selectedUrgency by remember { mutableStateOf("Timely") }
    var selectedContactMethod by remember { mutableStateOf("In-Person Discussion") }
    var personnelName by remember { mutableStateOf("") }
    var unitOrStation by remember { mutableStateOf("") }
    var details by remember { mutableStateOf("") }

    val urgencies = listOf("Routine", "Timely", "Urgent")
    val contactMethods = listOf("In-Person Discussion", "Secure Tele-Call", "Confidential Note")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = EmergencyRed.copy(alpha = 0.15f),
                            modifier = Modifier.size(36.dp)
                        ) {
                            IconButton(onClick = {}, enabled = false) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = EmergencyRed,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.HINDI) "कल्याण सहायता का अनुरोध" else "Request Welfare Support",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (language == AppLanguage.HINDI) "गोपनीय एवं सुरक्षित सहायता" else "Confidential & Supportive",
                                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.testTag("close_support_dialog")) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Privacy Banner
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.HINDI)
                                "यह अनुरोध पूरी तरह गोपनीय है और केवल अधिकृत कल्याण अधिकारियों द्वारा देखा जाएगा।"
                            else
                                "This request is strictly confidential and reviewed only by authorized welfare personnel. Never used for disciplinary actions.",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Selection
                Text(
                    text = if (language == AppLanguage.HINDI) "सहायता का प्रकार चुनें:" else "Select Support Category:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))

                SupportCategory.values().forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = {
                            Text(
                                text = if (language == AppLanguage.HINDI) category.titleHi else category.titleEn,
                                fontSize = 12.sp
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Urgency Selection
                Text(
                    text = if (language == AppLanguage.HINDI) "प्राथमिकता स्तर:" else "Priority Level:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    urgencies.forEach { urgency ->
                        FilterChip(
                            selected = selectedUrgency == urgency,
                            onClick = { selectedUrgency = urgency },
                            label = { Text(urgency, fontSize = 11.sp) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Contact Preference
                Text(
                    text = if (language == AppLanguage.HINDI) "संपर्क का पसंदीदा माध्यम:" else "Preferred Contact Method:",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                contactMethods.forEach { method ->
                    FilterChip(
                        selected = selectedContactMethod == method,
                        onClick = { selectedContactMethod = method },
                        label = { Text(method, fontSize = 11.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Optional Name / Alias
                OutlinedTextField(
                    value = personnelName,
                    onValueChange = { personnelName = it },
                    label = { Text(if (language == AppLanguage.HINDI) "नाम या उपनाम (वैकल्पिक)" else "Name or Alias (Optional)") },
                    placeholder = { Text(if (language == AppLanguage.HINDI) "गोपनीय रख सकते हैं" else "Can remain anonymous") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Unit / Station
                OutlinedTextField(
                    value = unitOrStation,
                    onValueChange = { unitOrStation = it },
                    label = { Text(if (language == AppLanguage.HINDI) "यूनिट या स्टेशन (वैकल्पिक)" else "Unit / Post / Station (Optional)") },
                    placeholder = { Text(if (language == AppLanguage.HINDI) "उदा. 104 बटालियन" else "e.g. 104 Bn Outpost") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Details Text Field
                OutlinedTextField(
                    value = details,
                    onValueChange = { details = it },
                    label = { Text(if (language == AppLanguage.HINDI) "संक्षिप्त विवरण या संदेश" else "Message / Notes (Confidential)") },
                    placeholder = {
                        Text(
                            if (language == AppLanguage.HINDI)
                                "आप जो भी साझा करना चाहें — कार्यभार, विश्राम, पारिवारिक चिंता आदि..."
                            else
                                "Anything you'd like to share — rest deficit, shift pressure, family matters..."
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(if (language == AppLanguage.HINDI) "रद्द करें" else "Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val finalName = if (personnelName.isNotBlank()) personnelName.trim() else "Confidential Personnel"
                            val finalUnit = if (unitOrStation.isNotBlank()) unitOrStation.trim() else "General Duty Unit"
                            onSubmit(selectedCategory, details.trim(), selectedUrgency, selectedContactMethod, finalName, finalUnit)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                        modifier = Modifier.testTag("submit_support_request_btn")
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (language == AppLanguage.HINDI) "अनुरोध भेजें" else "Submit Request")
                    }
                }
            }
        }
    }
}
