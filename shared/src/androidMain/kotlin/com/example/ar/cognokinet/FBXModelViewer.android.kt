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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

@Composable
actual fun FBXModelViewer(
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
            position = Position(0f, 0f, 3.5f)
        }

        SceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            cameraNode = cameraNode
        ) {
            val modelPath = "models/$modelFileName"
            val modelInstance = modelLoader.createModelInstance(modelPath)
            if (modelInstance != null) {
                ModelNode(
                    modelInstance = modelInstance,
                    position = Position(0f, -0.9f, 0f),
                    autoAnimate = true
                )
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(10.dp)
                .background(Color.Black.copy(alpha = 0.75f), RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "✨ 3D Mixamo (Opción B): $modelFileName",
                color = Color(0xFF81C784),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
