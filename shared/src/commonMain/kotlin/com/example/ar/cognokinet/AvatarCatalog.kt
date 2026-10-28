package com.example.ar.cognokinet

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.Serializable

@Serializable
data class AvatarConfig(
    val skinColorHex: String = "#FFCC80",
    val faceId: String = "smile",
    val hairStyleId: String = "short",
    val hairColorHex: String = "#5D4037",
    val hatId: String? = null,
    val shirtColorHex: String = "#2E7D32",
    val pantsColorHex: String = "#1565C0",
    val shoesColorHex: String = "#3E2723",
    val accessoryId: String? = null
)

@Serializable
data class ShopItem(
    val id: String,
    val category: String, // "gorra", "camisa", "pantalon", "calzado", "accesorio"
    val name: String,
    val priceAcorns: Int,
    val priceLeaves: Int,
    val colorHex: String? = null,
    val itemId: String? = null,
    val rarity: String = "Normal"
)

object ShopCatalog {
    val items = listOf(
        // Gorras / Sombreros
        ShopItem("hat_cap_red", "gorra", "Gorra Roja de Explorador", 30, 3, colorHex = "#E53935", itemId = "cap_red", rarity = "Rara"),
        ShopItem("hat_explorer", "gorra", "Sombrero de Expedición", 40, 5, colorHex = "#8D6E63", itemId = "explorer_hat", rarity = "Épica"),

        // Camisas
        ShopItem("shirt_gold", "camisa", "Camiseta Campeón Dorado", 35, 4, colorHex = "#FFCA28", itemId = "shirt_gold", rarity = "Rara"),
        ShopItem("shirt_river", "camisa", "Polera Azul Río", 25, 2, colorHex = "#2A9DD8", itemId = "shirt_river", rarity = "Normal"),

        // Pantalones
        ShopItem("pants_dark", "pantalon", "Pantalón Todo Terreno", 25, 2, colorHex = "#263238", itemId = "pants_dark", rarity = "Normal"),
        ShopItem("pants_forest", "pantalon", "Pantalón Verde Camo", 30, 3, colorHex = "#1B5E20", itemId = "pants_forest", rarity = "Rara"),

        // Calzado
        ShopItem("shoes_sport", "calzado", "Zapatillas Depor-Veloz", 30, 3, colorHex = "#D81B60", itemId = "shoes_sport", rarity = "Rara"),
        ShopItem("shoes_gold", "calzado", "Botas Doradas", 50, 6, colorHex = "#FFD700", itemId = "shoes_gold", rarity = "Legendaria"),

        // Accesorios
        ShopItem("acc_scarf", "accesorio", "Pañuelo Verde Zorro", 20, 2, colorHex = "#4CAF50", itemId = "scarf_fox", rarity = "Normal"),
        ShopItem("acc_glasses", "accesorio", "Lentes de Sol Deportivos", 35, 4, colorHex = "#37474F", itemId = "glasses_sun", rarity = "Épica")
    )
}
