package com.example.ar.cognokinet

import com.example.ar.cognokinet.domain.IdGenerator
import com.example.ar.cognokinet.domain.Rubric
import com.example.ar.cognokinet.domain.RubricCriterion
import com.example.ar.cognokinet.domain.RubricLevel

object RubricDefaults {
    fun forStation(stationId: Int, classId: String = ""): Rubric {
        val exercise = ExerciseCatalog.getById(stationId)
        val criteriaCount = exercise.suggestedCriteria.size.coerceAtLeast(1)
        val baseWeight = 100.0 / criteriaCount

        val criteriaList = exercise.suggestedCriteria.mapIndexed { index, criterionName ->
            // Distribute remaining rounding to last element so sum is exactly 100.0
            val weight = if (index == criteriaCount - 1) {
                100.0 - (baseWeight * (criteriaCount - 1))
            } else {
                baseWeight
            }

            RubricCriterion(
                id = IdGenerator.newId(),
                name = criterionName,
                weight = weight,
                levels = listOf(
                    RubricLevel(
                        points = 1,
                        label = "Inicial",
                        descriptor = "Demuestra un nivel inicial en $criterionName, requiere guía constante."
                    ),
                    RubricLevel(
                        points = 2,
                        label = "En proceso",
                        descriptor = "Demuestra avance en $criterionName, con ligeras imprecisiones."
                    ),
                    RubricLevel(
                        points = 3,
                        label = "Logrado",
                        descriptor = "Ejecuta correctamente y con soltura $criterionName."
                    ),
                    RubricLevel(
                        points = 4,
                        label = "Destacado",
                        descriptor = "Domina con excelencia y fluidez $criterionName."
                    )
                )
            )
        }

        return Rubric(
            id = IdGenerator.newId(),
            classId = classId,
            stationId = stationId,
            criteria = criteriaList
        )
    }

    fun forAllStations(classId: String = ""): List<Rubric> {
        return (1..CognokinetConstants.TOTAL_STATIONS).map { stationId ->
            forStation(stationId, classId)
        }
    }
}
