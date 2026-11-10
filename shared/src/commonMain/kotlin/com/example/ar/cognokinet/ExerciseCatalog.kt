package com.example.ar.cognokinet

data class ExerciseItem(
    val id: Int,
    val order: Int,
    val name: String,
    val description: String,
    val steps: List<String>,
    val metricLabel: String,
    val targetGoal: String,
    val timeSeconds: Int,
    val suggestedCriteria: List<String>,
    val baseRewardAcorns: Int,
    val baseRewardLeaves: Int,
    val modelFileName: String
)

object ExerciseCatalog {
    val list = listOf(
        ExerciseItem(
            id = 1,
            order = 1,
            name = "Salto Vertical",
            description = "Evaluación de flexión e impulso vertical con despegue simétrico.",
            steps = listOf(
                "Párate con los pies alineados a los hombros.",
                "Flexiona levemente las rodillas y balancea tus brazos.",
                "Despega con energía hacia arriba e impúlsate.",
                "Aterriza suavemente sobre las puntas de los pies."
            ),
            metricLabel = "Repeticiones",
            targetGoal = "10 saltos",
            timeSeconds = 90,
            suggestedCriteria = listOf("Técnica de impulso", "Amortiguación de caída", "Simetría articular"),
            baseRewardAcorns = 20,
            baseRewardLeaves = 3,
            modelFileName = "salto_normal.glb"
        ),
        ExerciseItem(
            id = 2,
            order = 2,
            name = "Saltar la Cuerda",
            description = "Coordinación ritmo-motriz con saltos continuos de cuerda.",
            steps = listOf(
                "Mantén la espalda erguida y la cuerda detrás de ti.",
                "Gira las muñecas para impulsar la cuerda por encima de tu cabeza.",
                "Salta con ritmo coordinado cuando la cuerda toque el suelo.",
                "Mantén una respiración constante y fluida."
            ),
            metricLabel = "Repeticiones",
            targetGoal = "15 saltos",
            timeSeconds = 90,
            suggestedCriteria = listOf("Ritmo continuo", "Coordinación mano-pie", "Mantener postura"),
            baseRewardAcorns = 30,
            baseRewardLeaves = 4,
            modelFileName = "saltar_la_cuerda.glb"
        ),
        ExerciseItem(
            id = 3,
            order = 3,
            name = "Lanzar",
            description = "Control visomotriz y puntería de lanzamiento a diana virtual.",
            steps = listOf(
                "Toma la pelota con la mano dominante.",
                "Alinea el cuerpo hacia el objetivo.",
                "Flexiona el codo y lanza con trayectoria parabólica.",
                "Acompaña la extensión completa del brazo."
            ),
            metricLabel = "Lanzamientos",
            targetGoal = "8 aciertos",
            timeSeconds = 120,
            suggestedCriteria = listOf("Puntería y precisión", "Mecánica de brazo", "Control de fuerza"),
            baseRewardAcorns = 30,
            baseRewardLeaves = 4,
            modelFileName = "lanzar.glb"
        ),
        ExerciseItem(
            id = 4,
            order = 4,
            name = "Patear",
            description = "Coordinación oculo-médica y precisión de pateo.",
            steps = listOf(
                "Colócate a dos pasos detrás de la pelota.",
                "Da un paso de apoyo firme al lado del balón.",
                "Impacta el centro de la pelota con el empeine interno.",
                "Acompaña la pierna hacia el objetivo."
            ),
            metricLabel = "Patadas",
            targetGoal = "8 aciertos",
            timeSeconds = 120,
            suggestedCriteria = listOf("Pie de apoyo firme", "Contacto del empeine", "Direccionamiento"),
            baseRewardAcorns = 40,
            baseRewardLeaves = 5,
            modelFileName = "patear.glb"
        ),
        ExerciseItem(
            id = 5,
            order = 5,
            name = "Basquetbol (Drible)",
            description = "Control dinámico de balón e integración de fuerza de drible.",
            steps = listOf(
                "Adopta postura de flexión básica de piernas.",
                "Empuja el balón contra el suelo usando las yemas de los dedos.",
                "Mantén el drible a la altura de la cintura.",
                "Alterna drible con mano izquierda y derecha."
            ),
            metricLabel = "Botes",
            targetGoal = "20 botes",
            timeSeconds = 150,
            suggestedCriteria = listOf("Uso de yemas de dedos", "Postura baja", "Alternancia de manos"),
            baseRewardAcorns = 40,
            baseRewardLeaves = 5,
            modelFileName = "basquetball.glb"
        )
    )

    fun getById(id: Int): ExerciseItem {
        return list.firstOrNull { it.id == id } ?: list[0]
    }
}
