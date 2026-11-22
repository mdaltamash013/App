package com.example.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SpaceDashboard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.EmergencyRed

import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.example.ui.theme.WarmBorderSubtle

sealed class NavDestination(
    val route: String,
    val icon: ImageVector,
    val titleEn: String,
    val titleHi: String
) {
    object Home : NavDestination("home", Icons.Default.Mic, "Voice", "आवाज़")
    object Wellness : NavDestination("my_wellness", Icons.Default.SelfImprovement, "Wellness", "कल्याण")
    object Emergency : NavDestination("emergency", Icons.Default.Emergency, "SOS", "आपातकाल")
    object Facilities : NavDestination("facilities", Icons.Default.LocalHospital, "Hospital", "अस्पताल")
    object Medicines : NavDestination("medicine", Icons.Default.MedicalServices, "Meds", "दवा")
    object Doctor : NavDestination("doctor", Icons.Default.Person, "Doctor", "डॉक्टर")
    object Records : NavDestination("records", Icons.Default.Assessment, "Records", "रिकॉर्ड")
    object Admin : NavDestination("admin", Icons.Default.SpaceDashboard, "Insights", "डैशबोर्ड")
}

@Composable
fun AppBottomNav(
    currentRoute: String,
    language: AppLanguage,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        NavDestination.Home,
        NavDestination.Wellness,
        NavDestination.Emergency,
        NavDestination.Facilities,
        NavDestination.Medicines,
        NavDestination.Doctor,
        NavDestination.Records,
        NavDestination.Admin
    )

    NavigationBar(
        modifier = modifier.drawBehind {
            drawLine(
                color = WarmBorderSubtle,
                start = Offset(0f, 0f),
                end = Offset(size.width, 0f),
                strokeWidth = 1.dp.toPx()
            )
        },
        windowInsets = WindowInsets.navigationBars,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            val isEmergencyItem = item == NavDestination.Emergency

            val itemColors = if (isEmergencyItem) {
                NavigationBarItemDefaults.colors(
                    selectedIconColor = EmergencyRed,
                    selectedTextColor = EmergencyRed,
                    indicatorColor = EmergencyRed.copy(alpha = 0.15f),
                    unselectedIconColor = EmergencyRed.copy(alpha = 0.8f),
                    unselectedTextColor = EmergencyRed.copy(alpha = 0.8f)
                )
            } else {
                NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            NavigationBarItem(
                selected = isSelected,
                onClick = { onNavigate(item.route) },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.titleEn,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = if (language == AppLanguage.HINDI) item.titleHi else item.titleEn,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        maxLines = 1
                    )
                },
                colors = itemColors,
                modifier = Modifier.testTag("nav_${item.route}")
            )
        }
    }
}
