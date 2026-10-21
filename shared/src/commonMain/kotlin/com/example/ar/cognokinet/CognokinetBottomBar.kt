package com.example.ar.cognokinet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CognokinetBottomBar(
    currentScreen: CognokinetScreen,
    onTabSelected: (NavigationTab) -> Unit
) {
    val selectedTab = when (currentScreen) {
        CognokinetScreen.HOME -> NavigationTab.MAP
        CognokinetScreen.MAP -> NavigationTab.MAP
        CognokinetScreen.RETO_DEMO -> NavigationTab.CHALLENGES
        CognokinetScreen.RESULTS -> NavigationTab.MAP
        CognokinetScreen.PROFILE -> NavigationTab.ACHIEVEMENTS
        CognokinetScreen.STUDENT_PROFILE -> NavigationTab.ACHIEVEMENTS
        CognokinetScreen.SHOP -> NavigationTab.SHOP
        CognokinetScreen.TEACHER_DASHBOARD -> NavigationTab.SETTINGS
        CognokinetScreen.SETTINGS -> NavigationTab.SETTINGS
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(10.dp)
            .background(Color(0xFFF8FAF6).copy(alpha = 0.95f))
            .border(1.dp, Color(0xFFD5E2D2))
            .padding(vertical = 6.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavItem("🗺️", "Mapa", NavigationTab.MAP, selectedTab, onTabSelected)
            NavItem("🏃", "Retos", NavigationTab.CHALLENGES, selectedTab, onTabSelected)
            NavItem("🎖️", "Logros", NavigationTab.ACHIEVEMENTS, selectedTab, onTabSelected)
            NavItem("🛍️", "Tienda", NavigationTab.SHOP, selectedTab, onTabSelected)
            NavItem("⚙️", "Configuración", NavigationTab.SETTINGS, selectedTab, onTabSelected)
        }
    }
}

@Composable
private fun NavItem(
    icon: String,
    label: String,
    tab: NavigationTab,
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    val isSelected = tab == selectedTab

    Box(
        modifier = Modifier
            .height(48.dp)
            .widthIn(min = 54.dp, max = 72.dp)
            .background(
                if (isSelected) CognokinetColors.Forest else Color.Transparent,
                RoundedCornerShape(14.dp)
            )
            .clickable { onTabSelected(tab) }
            .padding(horizontal = 2.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = if (isSelected) Color.White else Color(0xFF5C6E5A),
                fontSize = 9.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
