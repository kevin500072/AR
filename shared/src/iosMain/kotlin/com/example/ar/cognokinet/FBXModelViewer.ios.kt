package com.example.ar.cognokinet

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun FBXModelViewer(
    exerciseId: Int,
    modelFileName: String,
    modifier: Modifier
) {
    Motion3DViewer(
        exerciseId = exerciseId,
        modelFileName = modelFileName,
        modifier = modifier
    )
}
