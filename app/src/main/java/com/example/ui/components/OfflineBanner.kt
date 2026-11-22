package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.NetworkStatus
import androidx.compose.foundation.BorderStroke
import com.example.ui.theme.WarmBorderStroke

@Composable
fun OfflineBanner(
    networkStatus: NetworkStatus,
    language: AppLanguage,
    onCheckConnectivity: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = networkStatus == NetworkStatus.OFFLINE || networkStatus == NetworkStatus.WEAK,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        val isOffline = networkStatus == NetworkStatus.OFFLINE
        val bgColor = if (isOffline) {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)
        } else {
            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
        }
        val contentColor = if (isOffline) {
            MaterialTheme.colorScheme.onErrorContainer
        } else {
            MaterialTheme.colorScheme.onSecondaryContainer
        }

        Surface(
            color = bgColor,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, WarmBorderStroke),
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    imageVector = if (isOffline) Icons.Default.CloudOff else Icons.Default.SignalCellularConnectedNoInternet0Bar,
                    contentDescription = "Connectivity Notice",
                    tint = contentColor,
                    modifier = Modifier
                        .size(22.dp)
                        .padding(top = 2.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOffline) {
                            if (language == AppLanguage.HINDI) "ऑफ़लाइन मोड — बुनियादी सहायता सक्रिय" else "Offline Mode — Basic Guidance Active"
                        } else {
                            if (language == AppLanguage.HINDI) "कमजोर नेटवर्क कनेक्शन" else "Weak Network Connection"
                        },
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = contentColor
                        )
                    )
                    Text(
                        text = if (language == AppLanguage.HINDI) {
                            "आप ऑफ़लाइन हैं। बुनियादी स्वास्थ्य सहायता और सुरक्षा नियम उपलब्ध हैं। इंटरनेट आने तक अस्पताल व दवा की लाइव उपलब्धता सत्यापित नहीं हो सकती।"
                        } else {
                            "You are offline. Basic health assistance and safety rules are available. Live doctor, hospital and medicine availability cannot be verified until internet connectivity returns."
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.sp,
                            color = contentColor.copy(alpha = 0.9f),
                            lineHeight = 15.sp
                        )
                    )
                    if (onCheckConnectivity != null) {
                        Text(
                            text = if (language == AppLanguage.HINDI) "🔍 कनेक्टिविटी जांचें एवं पुनः प्रयास करें" else "🔍 Check Connectivity & Diagnostics",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = contentColor,
                                fontSize = 11.sp
                            ),
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .clickable(onClick = onCheckConnectivity)
                        )
                    }
                }
            }
        }
    }
}
