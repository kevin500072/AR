package com.example.ar.cognokinet

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.ar.core.Anchor
import com.google.ar.core.Plane
import com.google.ar.core.TrackingState
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelInstance
import io.github.sceneview.rememberModelLoader

// El personaje mide aproximadamente 1.82 m; centramos la vista en el torso.
private const val MODEL_CENTER_Y = 0.9f
private const val CAMERA_DISTANCE = 2.8f
private const val AR_MODEL_HEIGHT_METERS = 1.6f

@Composable
actual fun FBXModelViewer(
    exerciseId: Int,
    modelFileName: String,
    modifier: Modifier
) {
    var isArMode by remember(modelFileName) { mutableStateOf(false) }
    var isAnimationPlaying by remember(modelFileName) { mutableStateOf(true) }
    var arAnchor by remember(modelFileName) { mutableStateOf<Anchor?>(null) }
    val previewAnimationControl = remember(modelFileName) { arrayOfNulls<(Boolean) -> Unit>(1) }
    val arAnimationControl = remember(modelFileName) { arrayOfNulls<(Boolean) -> Unit>(1) }

    LaunchedEffect(isArMode, isAnimationPlaying, modelFileName) {
        val control = if (isArMode) arAnimationControl[0] else previewAnimationControl[0]
        control?.invoke(isAnimationPlaying)
    }
    var arStatus by remember(modelFileName) { mutableStateOf("Mueve el teléfono lentamente para detectar el piso.") }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color(0xFF141C15), RoundedCornerShape(18.dp))
            .border(2.dp, CognokinetColors.ForestDark, RoundedCornerShape(18.dp))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = {
                    isArMode = !isArMode
                    arStatus = "Mueve el teléfono lentamente para detectar el piso."
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isArMode) Color(0xFF795548) else CognokinetColors.Forest
                ),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (isArMode) "Vista 3D" else "Ver AR",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { isAnimationPlaying = !isAnimationPlaying },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF526354))
            ) {
                Text(
                    text = if (isAnimationPlaying) "⏸ Pausar" else "▶ Reproducir",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isArMode && arAnchor != null) {
                Button(
                    onClick = {
                        arAnchor?.detach()
                        arAnchor = null
                        arStatus = "Mueve el teléfono para elegir otra superficie."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest)
                ) {
                    Text("Recolocar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isArMode) 420.dp else 280.dp)
                .background(Color(0xFF202820), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isArMode) {
                ARSceneView(
                    modifier = Modifier.fillMaxSize(),
                    planeRenderer = true,
                    onSessionUpdated = { _, frame ->
                        if (arAnchor == null) {
                            val floor = frame.getUpdatedTrackables(Plane::class.java)
                                .firstOrNull { plane ->
                                    plane.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                                        plane.trackingState == TrackingState.TRACKING &&
                                        plane.subsumedBy == null
                                }
                            if (floor != null) {
                                arAnchor = runCatching { floor.createAnchor(floor.centerPose) }.getOrNull()
                                if (arAnchor != null) {
                                    arStatus = "¡Listo! El modelo está colocado en tu espacio."
                                }
                            }
                        }
                    },
                    onSessionFailed = {
                        arStatus = "No se pudo iniciar AR. Revisa el permiso de cámara y que Google Play Services para AR esté disponible."
                    }
                ) {
                    val modelInstance = rememberModelInstance(modelLoader, "models/$modelFileName")
                    arAnchor?.let { anchor ->
                        AnchorNode(anchor = anchor) {
                            modelInstance?.let {
                                ModelNode(
                                    modelInstance = it,
                                    scaleToUnits = AR_MODEL_HEIGHT_METERS,
                                    autoAnimate = true,
                                    apply = {
                                        val node = this
                                        if (!isAnimationPlaying) {
                                            repeat(animationCount) { index -> setAnimationSpeed(index, 0f) }
                                        }
                                        arAnimationControl[0] = { shouldPlay ->
                                            repeat(node.animationCount) { index ->
                                                node.setAnimationSpeed(index, if (shouldPlay) 1f else 0f)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = arStatus,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.62f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                )
            } else {
                val engine = rememberEngine()
                val modelLoader = rememberModelLoader(engine)
                val cameraNode = rememberCameraNode(engine).apply {
                    position = Position(0f, MODEL_CENTER_Y, CAMERA_DISTANCE)
                }
                val cameraManipulator = rememberCameraManipulator(
                    orbitHomePosition = Position(0f, MODEL_CENTER_Y, CAMERA_DISTANCE),
                    targetPosition = Position(0f, MODEL_CENTER_Y, 0f)
                )

                SceneView(
                    modifier = Modifier.fillMaxSize(),
                    engine = engine,
                    modelLoader = modelLoader,
                    cameraNode = cameraNode,
                    cameraManipulator = cameraManipulator
                ) {
                    val modelInstance = rememberModelInstance(modelLoader, "models/$modelFileName")
                    modelInstance?.let {
                        ModelNode(
                            modelInstance = it,
                            autoAnimate = true,
                            apply = {
                                val node = this
                                if (!isAnimationPlaying) {
                                    repeat(animationCount) { index -> setAnimationSpeed(index, 0f) }
                                }
                                previewAnimationControl[0] = { shouldPlay ->
                                    repeat(node.animationCount) { index ->
                                        node.setAnimationSpeed(index, if (shouldPlay) 1f else 0f)
                                    }
                                }
                            }
                        )
                    }
                }

                Text(
                    text = "✨ Vista 3D: $modelFileName",
                    color = Color(0xFF81C784),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(10.dp)
                        .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
