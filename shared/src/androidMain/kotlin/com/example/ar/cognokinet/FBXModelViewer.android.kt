package com.example.ar.cognokinet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

// El personaje mide ~1.82 m con el origen en los pies: su centro está a ~0.9 m de altura.
private const val MODEL_CENTER_Y = 0.9f
private const val CAMERA_DISTANCE = 2.8f

@Composable
actual fun FBXModelViewer(
    exerciseId: Int,
    modelFileName: String,
    modifier: Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color(0xFF141C15), RoundedCornerShape(18.dp))
            .border(2.dp, CognokinetColors.ForestDark, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        val engine = rememberEngine()
        val modelLoader = rememberModelLoader(engine)

        val cameraNode = rememberCameraNode(engine).apply {
            position = Position(0f, MODEL_CENTER_Y, CAMERA_DISTANCE)
        }

        // El manipulador es quien realmente manda sobre la cámara en cada frame.
        // Por defecto mira al origen (los pies), así que hay que decirle explícitamente
        // desde dónde mirar y hacia qué punto: el centro del personaje.
        val cameraManipulator = rememberCameraManipulator(
            orbitHomePosition = Position(0f, MODEL_CENTER_Y, CAMERA_DISTANCE),
            targetPosition = Position(0f, MODEL_CENTER_Y, 0f)
        )

        SceneView(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            event.changes.forEach { it.consume() }
                        }
                    }
                },
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode,
            cameraManipulator = cameraManipulator
        ) {
            val modelPath = "models/$modelFileName"
            val modelInstance = remember(modelFileName) {
                modelLoader.createModelInstance(modelPath)
            }
            if (modelInstance != null) {
                // Sin position: el modelo se queda en el origen. El centrado lo hace la cámara.
                ModelNode(
                    modelInstance = modelInstance,
                    autoAnimate = true
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "✨ 3D Mixamo: $modelFileName",
                color = Color(0xFF81C784),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}