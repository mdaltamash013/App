package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.MedicineItem
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.LowRiskGreenContainer
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.ModerateYellowContainer
import com.example.ui.theme.PrimaryTeal
import com.example.ui.viewmodel.HealthViewModel

@Composable
fun MedicineScreen(
    viewModel: HealthViewModel,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val language by viewModel.language.collectAsState()
    val medicines by viewModel.medicines.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterRxOnly by remember { mutableStateOf<Boolean?>(null) } // null = all, false = OTC, true = Rx

    val filteredMedicines = medicines.filter { item ->
        val matchesFilter = when (filterRxOnly) {
            null -> true
            true -> item.requiresPrescription
            false -> !item.requiresPrescription
        }
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                item.genericName.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true) ||
                item.purpose.contains(searchQuery, ignoreCase = true)
        matchesFilter && matchesSearch
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    viewModel.searchMedicines(it)
                },
                placeholder = {
                    Text(if (language == AppLanguage.HINDI) "दवा, साल्ट या लक्षण खोजें (उदा. ORS, Paracetamol)..." else "Search medicine, salt, or condition...")
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("medicine_search_input")
            )
        }

        // Safety Disclaimer Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MedicalServices,
                        contentDescription = null,
                        tint = PrimaryTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (language == AppLanguage.HINDI) {
                            "⚠️ LifeCare 24/7 केवल शैक्षिक जानकारी देता है। पर्चे वाली दवाइयां (Rx) केवल प्रमाणित डॉक्टर की सलाह पर ही लें।"
                        } else {
                            "⚠️ LifeCare 24/7 provides verified information. Prescription medicines (Rx) require a certified doctor's prescription."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }

        // Filter Chips: All, OTC, Prescription Required
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = filterRxOnly == null,
                    onClick = { filterRxOnly = null },
                    label = { Text(if (language == AppLanguage.HINDI) "सभी आवश्यक दवाएं" else "All Medicines") }
                )
                FilterChip(
                    selected = filterRxOnly == false,
                    onClick = { filterRxOnly = false },
                    label = { Text(if (language == AppLanguage.HINDI) "बिना पर्चे वाली (OTC)" else "OTC (Non-Rx)") }
                )
                FilterChip(
                    selected = filterRxOnly == true,
                    onClick = { filterRxOnly = true },
                    label = { Text(if (language == AppLanguage.HINDI) "पर्चे वाली (Rx)" else "Prescription (Rx)") }
                )
            }
        }

        // Medicine Cards List
        items(filteredMedicines) { medicine ->
            MedicineDetailCard(medicine = medicine, language = language)
        }
    }
}

@Composable
fun MedicineDetailCard(medicine: MedicineItem, language: AppLanguage) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("medicine_card_${medicine.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = medicine.genericName,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (medicine.requiresPrescription) ModerateYellowContainer else LowRiskGreenContainer
                ) {
                    Text(
                        text = if (medicine.requiresPrescription) "Rx Required" else "OTC Safe",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (medicine.requiresPrescription) ModerateYellow else LowRiskGreen
                        ),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = medicine.purpose,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Precautions
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Text(
                        text = if (language == AppLanguage.HINDI) "सावधानियां एवं सेवन विधि:" else "Precautions & Directions:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    medicine.precautions.forEach { p ->
                        Text(
                            text = "• $p",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                lineHeight = 16.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Verified Pharmacy Availability
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (medicine.verifiedAvailable) LowRiskGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (medicine.verifiedAvailable) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (medicine.verifiedAvailable) LowRiskGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (medicine.verifiedAvailable) {
                                "सत्यापित उपलब्धता: ${medicine.verifiedPharmacy}"
                            } else {
                                "Live medicine availability could not be verified"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (medicine.verifiedAvailable) LowRiskGreen else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = medicine.estimatedDelivery,
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}
