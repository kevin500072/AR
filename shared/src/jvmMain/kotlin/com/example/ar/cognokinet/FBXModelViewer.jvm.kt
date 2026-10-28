package com.example.ar.cognokinet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun FBXModelViewer(
    modelFileName: String,
    modifier: Modifier
) {
    Motion3DViewer(
        exerciseId = when (modelFileName) {
            "salto_normal.fbx" -> 1
            "saltar_la_cuerda.fbx" -> 2
            "lanzar.fbx" -> 3
            "patear.fbx" -> 4
            else -> 5
        },
        modelFileName = modelFileName,
        modifier = modifier
    )
}
