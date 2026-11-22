package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.model.VoiceState
import com.example.ui.theme.EmergencyRed
import com.example.ui.theme.PrimaryTeal
import com.example.ui.theme.WaveformActive
import com.example.ui.theme.WaveformGlow

@Composable
fun VoiceWaveform(
    voiceState: VoiceState,
    audioRms: Float,
    modifier: Modifier = Modifier
) {
    val barCount = 15
    val infiniteTransition = rememberInfiniteTransition(label = "waveform_anim")

    val pulse1 by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p1"
    )

    val pulse2 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "p2"
    )

    val barColor = when (voiceState) {
        VoiceState.IDLE -> MaterialTheme.colorScheme.outlineVariant
        VoiceState.LISTENING -> WaveformActive
        VoiceState.PROCESSING -> MaterialTheme.colorScheme.tertiary
        VoiceState.SPEAKING -> WaveformGlow
        VoiceState.ERROR -> EmergencyRed
    }

    Row(
        modifier = modifier.height(44.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until barCount) {
            val factor = when (voiceState) {
                VoiceState.LISTENING -> {
                    val base = if (i % 2 == 0) pulse1 else pulse2
                    (base * 0.4f + audioRms * 0.6f).coerceIn(0.15f, 1.0f)
                }
                VoiceState.PROCESSING -> {
                    // Moving sine wave effect
                    val phase = ((i * 0.2f + pulse1 * 2f) % 1f)
                    (0.25f + phase * 0.5f)
                }
                VoiceState.SPEAKING -> {
                    val centerDist = kotlin.math.abs(i - barCount / 2).toFloat() / (barCount / 2)
                    val heightMod = (1f - centerDist * 0.5f) * if (i % 2 == 0) pulse1 else pulse2
                    heightMod.coerceIn(0.2f, 1.0f)
                }
                VoiceState.ERROR -> 0.3f
                VoiceState.IDLE -> 0.15f
            }

            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight(fraction = factor)
                    .clip(RoundedCornerShape(2.dp))
                    .background(barColor)
            )
        }
    }
}
