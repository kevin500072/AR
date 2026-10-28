package com.example.ar.cognokinet

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import io.github.sceneview.SceneView
import io.github.sceneview.ar.ARScene
import io.github.sceneview.ar.rememberARCameraNode
import io.github.sceneview.ar.node.AnchorNode
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberModelLoader

@Composable
actual fun FBXModelViewer(
    modelFileName: String,
    modifier: Modifier
) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var useAR by remember { mutableStateOf(true) }
    var placedAnchorNode by remember(modelFileName) { mutableStateOf<AnchorNode?>(null) }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .background(Color(0xFF141C15), RoundedCornerShape(18.dp))
            .border(2.dp, CognokinetColors.ForestDark, RoundedCornerShape(18.dp)),
        contentAlignment = Alignment.Center
    ) {
        if (!hasCameraPermission) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "📷 Se requiere acceso a la cámara para Realidad Aumentada (RA)",
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = { launcher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Conceder Permiso de Cámara", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            val engine = rememberEngine()
            val modelLoader = rememberModelLoader(engine)

            if (useAR) {
                val cameraNode = rememberARCameraNode(engine)
                ARScene(
                    modifier = Modifier.fillMaxSize(),
                    engine = engine,
                    modelLoader = modelLoader,
                    cameraNode = cameraNode,
                    planeRenderer = true,
                    onSessionFailed = {
                        useAR = false
                    },
                    onTouchEvent = { _, hitResult ->
                        if (hitResult != null) {
                            val anchor = (hitResult as? com.google.ar.core.HitResult)?.createAnchor()
                            if (anchor != null) {
                                placedAnchorNode = AnchorNode(engine = engine, anchor = anchor).apply {
                                    val modelPath = "models/$modelFileName"
                                    val modelInstance = modelLoader.createModelInstance(modelPath)
                                    if (modelInstance != null) {
                                        addChildNode(
                                            ModelNode(
                                                modelInstance = modelInstance,
                                                scaleToUnits = 0.4f,
                                                autoAnimate = true
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        true
                    }
                ) {
                    placedAnchorNode?.let {
                        // Rendered in scene graph
                    }
                }
            } else {
                SceneView(
                    modifier = Modifier.fillMaxSize(),
                    engine = engine,
                    modelLoader = modelLoader
                ) {
                    val modelPath = "models/$modelFileName"
                    val modelInstance = modelLoader.createModelInstance(modelPath)
                    if (modelInstance != null) {
                        ModelNode(
                            modelInstance = modelInstance,
                            scaleToUnits = 0.4f,
                            autoAnimate = true
                        )
                    }
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
                    text = if (useAR) "🌐 RA Activa (Toca el suelo)" else "✨ 3D PBR (Modo Seguro): $modelFileName",
                    color = Color(0xFF81C784),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = { useAR = !useAR },
                colors = ButtonDefaults.buttonColors(containerColor = CognokinetColors.Forest),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(8.dp)
                    .height(34.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(
                    text = if (useAR) "Cambiar a Modo 3D Estándar" else "Intentar Realidad Aumentada",
                    fontSize = 10.sp,
                    color = Color.White
                )
            }
        }
    }
}
