package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.data.model.NetworkStatus
import com.example.ui.theme.LowRiskGreen
import com.example.ui.theme.ModerateYellow
import com.example.ui.theme.PrimaryTerracotta
import com.example.ui.theme.WarmBorderStroke
import androidx.compose.foundation.BorderStroke

@Composable
fun TopBar(
    language: AppLanguage,
    networkStatus: NetworkStatus,
    isMuted: Boolean,
    isHandsFree: Boolean,
    onToggleLanguage: () -> Unit,
    onToggleMute: () -> Unit,
    onOpenDiagnostics: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, WarmBorderStroke),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // App Brand with Warm Handcrafted Character
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PrimaryTerracotta),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🌿",
                            fontSize = 20.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "LifeCare 24/7",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Text(
                            text = if (language == AppLanguage.HINDI) "आत्मीय स्वास्थ्य साथी • निरंतर सेवा" else "Empathetic Healthcare • Always Near",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Actions: Language Toggle, Network Pill, Audio Mute
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable(onClick = onToggleLanguage)
                            .testTag("language_toggle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Change Language",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ENGLISH) "हिन्दी" else "EN",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Audio Mute/Unmute
                    IconButton(
                        onClick = onToggleMute,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("mute_toggle_button")
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (isMuted) "Unmute Voice" else "Mute Voice",
                            tint = if (isMuted) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Connectivity Pill with Diagnostics Trigger
                    NetworkPill(
                        networkStatus = networkStatus,
                        onClick = onOpenDiagnostics
                    )
                }
            }

            // Hands-Free active indicator banner
            if (isHandsFree) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "🔊 Hands-Free Mode Active — Listening continuously",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NetworkPill(
    networkStatus: NetworkStatus,
    onClick: (() -> Unit)? = null
) {
    val (bgColor, textColor, label, icon) = when (networkStatus) {
        NetworkStatus.ONLINE -> Quadruple(
            LowRiskGreen.copy(alpha = 0.15f),
            LowRiskGreen,
            "Online",
            Icons.Default.Wifi
        )
        NetworkStatus.WEAK -> Quadruple(
            ModerateYellow.copy(alpha = 0.15f),
            ModerateYellow,
            "Weak",
            Icons.Default.Wifi
        )
        NetworkStatus.OFFLINE -> Quadruple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.error,
            "Offline",
            Icons.Default.WifiOff
        )
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = bgColor,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .let { m -> if (onClick != null) m.clickable(onClick = onClick) else m }
            .testTag("network_diagnostics_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(textColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    fontSize = 10.sp
                )
            )
        }
    }
}

data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
