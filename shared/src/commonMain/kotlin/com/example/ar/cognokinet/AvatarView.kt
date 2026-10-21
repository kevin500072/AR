package com.example.ar.cognokinet

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun parseColorHex(hex: String, fallback: Color = Color.Gray): Color {
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = clean.toLong(16) or 0xFF000000
        Color(colorInt)
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun AvatarView(
    config: AvatarConfig,
    sizeDp: Dp = 100.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val skinColor = parseColorHex(config.skinColorHex, Color(0xFFFFCC80))
            val shirtColor = parseColorHex(config.shirtColorHex, Color(0xFF2E7D32))
            val pantsColor = parseColorHex(config.pantsColorHex, Color(0xFF1565C0))
            val shoesColor = parseColorHex(config.shoesColorHex, Color(0xFF3E2723))
            val hairColor = parseColorHex(config.hairColorHex, Color(0xFF5D4037))

            // LAYER 1: Body Base (Head & Arms)
            val headRadius = w * 0.22f
            val headCenter = Offset(w * 0.5f, h * 0.32f)
            drawCircle(color = skinColor, radius = headRadius, center = headCenter)

            // Neck
            drawRect(
                color = skinColor,
                topLeft = Offset(w * 0.44f, h * 0.48f),
                size = Size(w * 0.12f, h * 0.08f)
            )

            // LAYER 2: Shoes (Calzado)
            drawRoundRect(
                color = shoesColor,
                topLeft = Offset(w * 0.32f, h * 0.85f),
                size = Size(w * 0.15f, h * 0.10f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = shoesColor,
                topLeft = Offset(w * 0.53f, h * 0.85f),
                size = Size(w * 0.15f, h * 0.10f),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // LAYER 3: Pants (Pantalón)
            drawRoundRect(
                color = pantsColor,
                topLeft = Offset(w * 0.32f, h * 0.65f),
                size = Size(w * 0.36f, h * 0.22f),
                cornerRadius = CornerRadius(8f, 8f)
            )

            // LAYER 4: Shirt (Camisa)
            drawRoundRect(
                color = shirtColor,
                topLeft = Offset(w * 0.28f, h * 0.48f),
                size = Size(w * 0.44f, h * 0.22f),
                cornerRadius = CornerRadius(10f, 10f)
            )

            // LAYER 5: Face Details (Ojos y Sonrisa)
            drawCircle(color = Color(0xFF1F2D24), radius = headRadius * 0.12f, center = Offset(w * 0.43f, h * 0.30f))
            drawCircle(color = Color(0xFF1F2D24), radius = headRadius * 0.12f, center = Offset(w * 0.57f, h * 0.30f))
            // Mouth
            drawArc(
                color = Color(0xFFD84315),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(w * 0.42f, h * 0.34f),
                size = Size(w * 0.16f, h * 0.10f)
            )

            // LAYER 6: Hair (Pelo)
            drawArc(
                color = hairColor,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(w * 0.28f, h * 0.12f),
                size = Size(w * 0.44f, h * 0.22f)
            )

            // LAYER 7: Hat / Cap (Gorra si está equipada)
            if (config.hatId != null) {
                val hatColor = when (config.hatId) {
                    "cap_red" -> Color(0xFFE53935)
                    "explorer_hat" -> Color(0xFF8D6E63)
                    else -> Color(0xFFFFCA28)
                }
                drawRoundRect(
                    color = hatColor,
                    topLeft = Offset(w * 0.24f, h * 0.10f),
                    size = Size(w * 0.52f, h * 0.12f),
                    cornerRadius = CornerRadius(12f, 12f)
                )
            }

            // LAYER 8: Accessory (Pañuelo/Lentes si equipado)
            if (config.accessoryId != null) {
                val accColor = when (config.accessoryId) {
                    "scarf_fox" -> Color(0xFF4CAF50)
                    "glasses_sun" -> Color(0xFF37474F)
                    else -> Color(0xFFFF9800)
                }
                drawRoundRect(
                    color = accColor,
                    topLeft = Offset(w * 0.34f, h * 0.48f),
                    size = Size(w * 0.32f, h * 0.06f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
    }
}
