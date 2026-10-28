package com.example.ar.cognokinet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    appState: CognokinetAppState,
    onOpenProfileSetup: () -> Unit,
    onBack: () -> Unit
) {
    val activeProfile = appState.activeProfile
    val activeProgress = appState.activeProgress

    val allBadges = BadgeCatalog.ALL_BADGES

    val unlockedCount = activeProgress.unlockedBadges.size

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CognokinetColors.SurfaceGreen)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. TOP HEADER BAR
            ProfileHeaderBar(appState = appState)

            // 2. MAIN SCROLLABLE CONTENT
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Contenedor Estilo Pergamino de Madera Natural
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(10.dp, RoundedCornerShape(24.dp))
                        .background(CognokinetColors.Parchment, RoundedCornerShape(24.dp))
                        .border(3.dp, CognokinetColors.EarthWood, RoundedCornerShape(24.dp))
                        .padding(16.dp)
                ) {
                    Column {
                        // Title Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(Color(0xFFE8F5E9), CircleShape)
                                        .border(1.dp, Color(0xFFA5D6A7), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🎖️", fontSize = 18.sp)
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Text(
                                    text = "Logros e Insignias de Roble",
                                    color = Color(0xFF2E3E23),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Color(0xFF4E342E), CircleShape)
                                    .clickable { onBack() },
                                contentAlignment = Alignment.Center
                            ) {
                                Text("✕", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // User Info Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF4F0E6), RoundedCornerShape(18.dp))
                                .border(1.dp, Color(0xFFDED5C2), RoundedCornerShape(18.dp))
                                .padding(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .background(Color(0xFFD6EED8), CircleShape)
                                        .border(2.dp, Color(0xFF81C784), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (activeProfile != null) StudentAvatars.getEmoji(activeProfile.avatarId) else "👤",
                                        fontSize = 30.sp
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        text = activeProfile?.name ?: "Sin Alumno Registrado",
                                        color = Color(0xFF263238),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (activeProfile != null) "${activeProfile.age} años · ${activeProfile.grade}" else "Crea un perfil para comenzar",
                                        color = CognokinetColors.Forest,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Box(
                                        modifier = Modifier
                                            .background(Color(0xFFE8F5E9), CircleShape)
                                            .border(1.dp, Color(0xFFA5D6A7), CircleShape)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Nivel ${activeProgress.level} · ${activeProgress.xp} XP",
                                            color = Color(0xFF1B5E20),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Badges Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Insignias Ganadas", color = Color(0xFF3E2723), fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), CircleShape)
                                    .border(1.dp, Color(0xFFC8E6C9), CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("$unlockedCount / ${CognokinetConstants.TOTAL_BADGES} Ganadas", color = CognokinetColors.Forest, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Guidance Card
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFE8F5E9), RoundedCornerShape(14.dp))
                                .border(1.dp, Color(0xFFA5D6A7), RoundedCornerShape(14.dp))
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🥾 ", fontSize = 18.sp)
                                Text(
                                    text = if (unlockedCount == 0) "Completa tu primer reto en el mapa para ganar tu primera insignia de roble." else "¡Excelente avance! Sigue completando estaciones.",
                                    color = Color(0xFF1B5E20),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3x3 Grid of 9 Badges
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (row in 0..2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    for (col in 0..2) {
                                        val index = row * 3 + col
                                        if (index < allBadges.size) {
                                            val badge = allBadges[index]
                                            val isUnlocked = activeProgress.unlockedBadges.contains(badge.id)
                                            BadgeGridCard(badge = badge, isUnlocked = isUnlocked, modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Personalize Explorer Equipment / Setup
                        Button(
                            onClick = onOpenProfileSetup,
                            colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text("👤 Administrar Perfil del Alumno", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ProfileHeaderBar(appState: CognokinetAppState) {
    val progress = appState.activeProgress
    val profile = appState.activeProfile

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(CognokinetColors.SurfaceGreen.copy(alpha = 0.9f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeaderStatBadge("🍃", "${progress.leaves}")
                HeaderStatBadge("🌰", "${progress.acorns}")
                HeaderStatBadge("🧭", "Niv. ${progress.level}")
            }

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(CognokinetColors.Forest, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (profile != null) StudentAvatars.getEmoji(profile.avatarId) else "👤",
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun HeaderStatBadge(icon: String, value: String) {
    Box(
        modifier = Modifier
            .height(34.dp)
            .background(Color.White, CircleShape)
            .border(1.dp, Color(0xFFC2E2C6), CircleShape)
            .padding(horizontal = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 13.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(value, color = CognokinetColors.ForestDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun BadgeGridCard(badge: BadgeInfo, isUnlocked: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(110.dp)
            .background(
                if (isUnlocked) Color(0xFFE8F5E9) else Color(0xFFECEFF1),
                RoundedCornerShape(16.dp)
            )
            .border(
                1.dp,
                if (isUnlocked) CognokinetColors.LeafGreen else Color(0xFFCFD8DC),
                RoundedCornerShape(16.dp)
            )
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (isUnlocked) CognokinetColors.Forest else Color(0xFFCFD8DC),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(badge.icon, fontSize = 18.sp)

                if (!isUnlocked) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(16.dp)
                            .background(Color(0xFF5D4037), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔒", fontSize = 9.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = badge.title,
                color = if (isUnlocked) CognokinetColors.ForestDark else Color(0xFF546E7A),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = badge.station,
                color = Color(0xFF78909C),
                fontSize = 9.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}


