package com.example.ar.cognokinet

data class BadgeInfo(
    val id: String,
    val title: String,
    val station: String,
    val icon: String,
    val requirement: String
)

object BadgeCatalog {
    val ALL_BADGES = listOf(
        BadgeInfo("badge_1", "Salto Vertical", "Estación 1", "⬆️", "Completa el Salto Vertical"),
        BadgeInfo("badge_2", "Saltar la Cuerda", "Estación 2", "🪢", "Completa Saltar la Cuerda"),
        BadgeInfo("badge_3", "Lanzar", "Estación 3", "🎯", "Completa el Lanzamiento"),
        BadgeInfo("badge_4", "Patear", "Estación 4", "⚽", "Completa el Pateo"),
        BadgeInfo("badge_5", "Basquetbol", "Estación 5", "🏀", "Completa el Drible de Basquetbol"),
        BadgeInfo("badge_6", "Primer Reto", "General", "🌟", "Aprueba tu primer ejercicio"),
        BadgeInfo("badge_7", "Tres Estrellas", "General", "⭐", "Obtén 3 estrellas en un ejercicio"),
        BadgeInfo("badge_8", "Constancia", "General", "🔥", "Logra 3 evaluaciones aprobadas"),
        BadgeInfo("badge_9", "Gran Campeón", "Final", "🏆", "Completa las 5 estaciones del sendero")
    )
}
