package com.example.ui.components

import android.content.Intent
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.location.LocationErrorType
import com.example.data.location.LocationState
import com.example.data.model.AppLanguage
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryTeal

@Composable
fun LocationStatusCard(
    locationState: LocationState,
    language: AppLanguage,
    onRetryLocation: () -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (locationState) {
                is LocationState.Error -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                is LocationState.Success -> PrimaryTeal.copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (locationState) {
                is LocationState.Error -> EmergencyRed.copy(alpha = 0.3f)
                is LocationState.Success -> PrimaryTeal.copy(alpha = 0.3f)
                else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("location_status_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            when (locationState) {
                is LocationState.Detecting -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📍",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "स्थान (Location)" else "Location",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.HINDI) "स्थान खोजा जा रहा है..." else "Detecting location...",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp),
                            color = PrimaryTeal
                        )
                    }
                }

                is LocationState.Success -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "📍",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "स्थान (Location)" else "Location",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = locationState.getFormattedLocation(language),
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onRetryLocation,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("refresh_location_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = if (language == AppLanguage.HINDI) "स्थान रीफ्रेश करें" else "Refresh Location",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                is LocationState.Error -> {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "⚠️",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                val errorHeadline = when (locationState.errorType) {
                                    LocationErrorType.PERMISSION_REQUIRED -> {
                                        if (language == AppLanguage.HINDI) "स्थान अनुमति आवश्यक है" else "Location permission required"
                                    }
                                    LocationErrorType.SERVICES_DISABLED -> {
                                        if (language == AppLanguage.HINDI) "डिवाइस स्थान सेवा बंद है" else "Location services disabled"
                                    }
                                    LocationErrorType.TIMEOUT, LocationErrorType.UNAVAILABLE, LocationErrorType.UNSUPPORTED -> {
                                        if (language == AppLanguage.HINDI) "स्थान खोजने में असमर्थ" else "Unable to detect location"
                                    }
                                }
                                Text(
                                    text = errorHeadline,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                )
                                Text(
                                    text = locationState.message,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (locationState.errorType == LocationErrorType.PERMISSION_REQUIRED) {
                                OutlinedButton(
                                    onClick = onRequestPermission,
                                    modifier = Modifier.testTag("grant_location_permission_button"),
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "अनुमति दें" else "Grant Permission",
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            } else if (locationState.errorType == LocationErrorType.SERVICES_DISABLED) {
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            onRetryLocation()
                                        }
                                    },
                                    modifier = Modifier.testTag("open_location_settings_button"),
                                    contentPadding = ButtonDefaults.TextButtonContentPadding
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.HINDI) "GPS चालू करें" else "Enable GPS",
                                        fontSize = 12.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }

                            Button(
                                onClick = onRetryLocation,
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                                modifier = Modifier.testTag("retry_location_button"),
                                contentPadding = ButtonDefaults.TextButtonContentPadding
                            ) {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "पुनः प्रयास करें" else "Retry Location",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                is LocationState.Idle -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "📍",
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.HINDI) "स्थान (Location)" else "Location",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.HINDI) "स्थान का पता लगाएं" else "Detect current location",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                        Button(
                            onClick = onRetryLocation,
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryTeal),
                            modifier = Modifier.testTag("retry_location_button"),
                            contentPadding = ButtonDefaults.TextButtonContentPadding
                        ) {
                            Text(
                                text = if (language == AppLanguage.HINDI) "पता लगाएं" else "Detect",
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
