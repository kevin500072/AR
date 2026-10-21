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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ShopScreen(
    appState: CognokinetAppState,
    onGoToMap: () -> Unit,
    onBack: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("gorra") }
    var previewConfig by remember { mutableStateOf(appState.currentAvatarConfig) }
    var isProcessingPurchase by remember { mutableStateOf(false) }

    val progress = appState.activeProgress
    val inventory = appState.unlockedInventoryItems

    val categoryItems = ShopCatalog.items.filter { it.category == selectedCategory }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CognokinetColors.NatureBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(CognokinetColors.ForestLight, CircleShape)
                                .clickable { onBack() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("←", color = CognokinetColors.Forest, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text("Tienda de Equipamiento", color = CognokinetColors.Graphite, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    // Balance Header
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFEF9E7), CircleShape)
                                .border(1.dp, CognokinetColors.SunAmber, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🌰 ${progress.acorns}", color = Color(0xFF5D4037), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }

                        Box(
                            modifier = Modifier
                                .background(Color(0xFFE8F5E9), CircleShape)
                                .border(1.dp, CognokinetColors.LeafGreen, CircleShape)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("🍃 ${progress.leaves}", color = CognokinetColors.Forest, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Scrollable Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. AVATAR PROBADOR DE EQUIPO (TRY-ON PREVIEW)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(6.dp, RoundedCornerShape(20.dp))
                        .background(CognokinetColors.Parchment, RoundedCornerShape(20.dp))
                        .border(2.dp, CognokinetColors.EarthWood, RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Probador de Avatar", color = Color(0xFF5C3A21), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        AvatarView(config = previewConfig, sizeDp = 110.dp)

                        Spacer(modifier = Modifier.height(6.dp))

                        if (previewConfig != appState.currentAvatarConfig) {
                            Button(
                                onClick = {
                                    appState.currentAvatarConfig = previewConfig
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("Equipar Look Actual", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 2. CATEGORY CHIPS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        "gorra" to "Gorras",
                        "camisa" to "Camisas",
                        "pantalon" to "Pantalones",
                        "calzado" to "Calzado",
                        "accesorio" to "Accesorios"
                    ).forEach { (catKey, catLabel) ->
                        val isSel = catKey == selectedCategory
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .background(
                                    if (isSel) CognokinetColors.Forest else Color(0xFFE8E2D0),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { selectedCategory = catKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = catLabel,
                                color = if (isSel) Color.White else Color(0xFF5C3A21),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. ESTADO VACÍO SI SALDO ES 0 Y NO HAY COMPRAS
                if (progress.acorns == 0 && progress.leaves == 0 && inventory.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFFEF9E7), RoundedCornerShape(16.dp))
                            .border(1.dp, CognokinetColors.SunAmber, RoundedCornerShape(16.dp))
                            .padding(14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🌰 🍃 Completa niveles para ganar bellotas y hojas", color = Color(0xFF5D4037), fontSize = 13.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = onGoToMap,
                                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("🏃 Ir al Mapa", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // 4. ITEMS GRID
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    categoryItems.forEach { item ->
                        val isPurchased = inventory.contains(item.id)
                        val isEquipped = when (item.category) {
                            "gorra" -> previewConfig.hatId == item.itemId
                            "camisa" -> previewConfig.shirtColorHex == item.colorHex
                            "pantalon" -> previewConfig.pantsColorHex == item.colorHex
                            "calzado" -> previewConfig.shoesColorHex == item.colorHex
                            "accesorio" -> previewConfig.accessoryId == item.itemId
                            else -> false
                        }
                        val canAfford = progress.acorns >= item.priceAcorns && progress.leaves >= item.priceLeaves

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(3.dp, RoundedCornerShape(16.dp))
                                .background(Color.White, RoundedCornerShape(16.dp))
                                .border(
                                    1.dp,
                                    if (isEquipped) CognokinetColors.Forest else Color(0xFFE8E1D3),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.name, color = CognokinetColors.Graphite, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text("Rareza: ${item.rarity}", color = Color(0xFF68766C), fontSize = 11.sp)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Precio: 🌰 ${item.priceAcorns} | 🍃 ${item.priceLeaves}", color = Color(0xFF5D4037), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    // Probador
                                    Button(
                                        onClick = {
                                            previewConfig = when (item.category) {
                                                "gorra" -> previewConfig.copy(hatId = item.itemId)
                                                "camisa" -> previewConfig.copy(shirtColorHex = item.colorHex ?: "#2E7D32")
                                                "pantalon" -> previewConfig.copy(pantsColorHex = item.colorHex ?: "#1565C0")
                                                "calzado" -> previewConfig.copy(shoesColorHex = item.colorHex ?: "#3E2723")
                                                "accesorio" -> previewConfig.copy(accessoryId = item.itemId)
                                                else -> previewConfig
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE8E2D0)),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.height(36.dp)
                                    ) {
                                        Text("Probar", color = Color(0xFF5C3A21), fontSize = 11.sp)
                                    }

                                    // Acción Comprar / Equipar
                                    if (isEquipped) {
                                        Box(
                                            modifier = Modifier
                                                .background(CognokinetColors.Forest, CircleShape)
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text("Equipado", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (isPurchased) {
                                        Button(
                                            onClick = {
                                                previewConfig = when (item.category) {
                                                    "gorra" -> previewConfig.copy(hatId = item.itemId)
                                                    "camisa" -> previewConfig.copy(shirtColorHex = item.colorHex ?: "#2E7D32")
                                                    "pantalon" -> previewConfig.copy(pantsColorHex = item.colorHex ?: "#1565C0")
                                                    "calzado" -> previewConfig.copy(shoesColorHex = item.colorHex ?: "#3E2723")
                                                    "accesorio" -> previewConfig.copy(accessoryId = item.itemId)
                                                    else -> previewConfig
                                                }
                                                appState.currentAvatarConfig = previewConfig
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text("Equipar", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else {
                                        Button(
                                            onClick = {
                                                if (canAfford && !isProcessingPurchase) {
                                                    isProcessingPurchase = true
                                                    appState.purchaseShopItem(item)
                                                    isProcessingPurchase = false
                                                }
                                            },
                                            enabled = canAfford && !isProcessingPurchase,
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = CognokinetColors.SunAmber,
                                                disabledContainerColor = Color(0xFFE0E0E0)
                                            ),
                                            shape = RoundedCornerShape(10.dp),
                                            modifier = Modifier.height(36.dp)
                                        ) {
                                            Text(
                                                text = if (canAfford) "Comprar" else "No alcanza",
                                                color = if (canAfford) Color(0xFF3E2723) else Color.Gray,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}
