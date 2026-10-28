package com.example.ar.cognokinet

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Point3D(val x: Float, val y: Float, val z: Float)

@Composable
fun Motion3DViewer(
    exerciseId: Int,
    modelFileName: String,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(true) }
    var rotationAngleDegrees by remember { mutableStateOf(30f) }
    var speedMultiplier by remember { mutableStateOf(1.0f) }

    // Infinite transition for continuous 60FPS motion keyframes
    val transition = rememberInfiniteTransition()
    val rawAnimTime by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = (1800 / speedMultiplier).toInt(),
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )

    val animTime = if (isPlaying) rawAnimTime else 1.2f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .background(Color(0xFFF9FAF8), RoundedCornerShape(16.dp))
            .border(1.5.dp, CognokinetColors.Forest.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        // Header info
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(CognokinetColors.Forest, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🧊", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        "Visualizador 3D de Movimiento",
                        color = CognokinetColors.Graphite,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "FBX: $modelFileName",
                        color = CognokinetColors.Forest,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Speed chips
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0.5f to "0.5x", 1.0f to "1x", 1.5f to "1.5x").forEach { (spVal, spLabel) ->
                    val isSelected = speedMultiplier == spVal
                    Box(
                        modifier = Modifier
                            .background(
                                if (isSelected) CognokinetColors.Forest else Color(0xFFE8E2D0),
                                CircleShape
                            )
                            .clickable { speedMultiplier = spVal }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            spLabel,
                            color = if (isSelected) Color.White else Color(0xFF5C3A21),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3D CANVAS VIEWPORT
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color(0xFF1E281F), RoundedCornerShape(14.dp))
                .border(2.dp, CognokinetColors.ForestDark, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f + 20f
                val rad = (rotationAngleDegrees * PI / 180.0).toFloat()

                // 3D Perspective Projection Function
                fun project(p: Point3D): Offset {
                    val rotX = p.x * cos(rad) + p.z * sin(rad)
                    val rotY = p.y
                    val rotZ = -p.x * sin(rad) + p.z * cos(rad)
                    val focalLength = 350f
                    val scale = focalLength / (focalLength + rotZ + 150f)
                    val screenX = cx + rotX * scale
                    val screenY = cy - rotY * scale
                    return Offset(screenX, screenY)
                }

                // Grid Floor
                val gridColor = Color(0xFF2E4031)
                for (gridZ in -100..100 step 40) {
                    val p1 = project(Point3D(-120f, -60f, gridZ.toFloat()))
                    val p2 = project(Point3D(120f, -60f, gridZ.toFloat()))
                    drawLine(gridColor, p1, p2, strokeWidth = 1.5f)
                }

                // Compute Skeleton Joints based on exercise animation
                val (head, neck, spine, leftHip, rightHip, leftKnee, rightKnee, leftFoot, rightFoot, leftShoulder, rightShoulder, leftElbow, rightElbow, leftHand, rightHand, propPos) =
                    computeExerciseJoints(exerciseId, animTime)

                val head2D = project(head)
                val neck2D = project(neck)
                val spine2D = project(spine)
                val lHip2D = project(leftHip)
                val rHip2D = project(rightHip)
                val lKnee2D = project(leftKnee)
                val rKnee2D = project(rightKnee)
                val lFoot2D = project(leftFoot)
                val rFoot2D = project(rightFoot)
                val lSh2D = project(leftShoulder)
                val rSh2D = project(rightShoulder)
                val lElb2D = project(leftElbow)
                val rElb2D = project(rightElbow)
                val lHand2D = project(leftHand)
                val rHand2D = project(rightHand)

                val boneColor = Color(0xFF81C784)
                val boneJointColor = Color(0xFFFFD54F)
                val strokeW = 6f

                // Draw Bones
                drawLine(boneColor, neck2D, spine2D, strokeWidth = strokeW)
                drawLine(boneColor, spine2D, lHip2D, strokeWidth = strokeW)
                drawLine(boneColor, spine2D, rHip2D, strokeWidth = strokeW)
                drawLine(boneColor, lHip2D, lKnee2D, strokeWidth = strokeW)
                drawLine(boneColor, lKnee2D, lFoot2D, strokeWidth = strokeW)
                drawLine(boneColor, rHip2D, rKnee2D, strokeWidth = strokeW)
                drawLine(boneColor, rKnee2D, rFoot2D, strokeWidth = strokeW)

                drawLine(boneColor, neck2D, lSh2D, strokeWidth = strokeW)
                drawLine(boneColor, lSh2D, lElb2D, strokeWidth = strokeW)
                drawLine(boneColor, lElb2D, lHand2D, strokeWidth = strokeW)

                drawLine(boneColor, neck2D, rSh2D, strokeWidth = strokeW)
                drawLine(boneColor, rSh2D, rElb2D, strokeWidth = strokeW)
                drawLine(boneColor, rElb2D, rHand2D, strokeWidth = strokeW)

                // Head
                drawCircle(Color(0xFFFFECB3), radius = 18f, center = head2D)
                drawCircle(CognokinetColors.ForestDark, radius = 18f, center = head2D, style = Stroke(3f))

                // Joints
                listOf(neck2D, spine2D, lHip2D, rHip2D, lKnee2D, rKnee2D, lFoot2D, rFoot2D, lSh2D, rSh2D, lElb2D, rElb2D, lHand2D, rHand2D).forEach { j ->
                    drawCircle(boneJointColor, radius = 5f, center = j)
                }

                // Prop Animation (Rope, Ball, etc.)
                if (propPos != null) {
                    val prop2D = project(propPos)
                    when (exerciseId) {
                        2 -> { // Jump Rope Loop Path
                            val ropePath = Path()
                            val topP = project(Point3D(0f, 110f * sin(animTime), 70f * cos(animTime)))
                            ropePath.moveTo(lHand2D.x, lHand2D.y)
                            ropePath.quadraticTo(topP.x, topP.y, rHand2D.x, rHand2D.y)
                            drawPath(ropePath, Color(0xFFFFB74D), style = Stroke(width = 4f, cap = StrokeCap.Round))
                        }
                        3, 4 -> { // Ball
                            drawCircle(Color(0xFFE65100), radius = 12f, center = prop2D)
                            drawCircle(Color.White, radius = 12f, center = prop2D, style = Stroke(2f))
                        }
                        5 -> { // Basketball
                            drawCircle(Color(0xFFEF6C00), radius = 14f, center = prop2D)
                            drawCircle(Color.Black, radius = 14f, center = prop2D, style = Stroke(2f))
                        }
                    }
                }
            }

            // Top Status Overlay Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    "▶ 60FPS Modelo 3D Activo",
                    color = Color(0xFF81C784),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Rotation Controls Slider & Play/Pause Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(if (isPlaying) CognokinetColors.Forest else Color(0xFF8C6D58), CircleShape)
                    .clickable { isPlaying = !isPlaying },
                contentAlignment = Alignment.Center
            ) {
                Text(if (isPlaying) "⏸" else "▶", color = Color.White, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text("Ángulo 3D:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF5C3A21))

            Spacer(modifier = Modifier.width(6.dp))

            Slider(
                value = rotationAngleDegrees,
                onValueChange = { rotationAngleDegrees = it },
                valueRange = -180f..180f,
                colors = SliderDefaults.colors(
                    thumbColor = CognokinetColors.Forest,
                    activeTrackColor = CognokinetColors.Forest
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                "${rotationAngleDegrees.toInt()}°",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CognokinetColors.Forest
            )
        }
    }
}

// Compute 3D Keyframe Positions for Each Exercise
private fun computeExerciseJoints(exerciseId: Int, t: Float): Skeleton3D {
    return when (exerciseId) {
        1 -> { // Salto Vertical
            val jumpPhase = sin(t)
            val crouch = (if (jumpPhase < 0) -jumpPhase else 0f) * 25f
            val height = (if (jumpPhase > 0) jumpPhase else 0f) * 45f

            val basePosY = -20f + height - crouch

            Skeleton3D(
                head = Point3D(0f, basePosY + 90f, 0f),
                neck = Point3D(0f, basePosY + 75f, 0f),
                spine = Point3D(0f, basePosY + 45f, 0f),
                leftHip = Point3D(-12f, basePosY + 35f, 0f),
                rightHip = Point3D(12f, basePosY + 35f, 0f),
                leftKnee = Point3D(-15f, basePosY + 15f + crouch * 0.3f, 10f),
                rightKnee = Point3D(15f, basePosY + 15f + crouch * 0.3f, 10f),
                leftFoot = Point3D(-12f, basePosY - 10f, 0f),
                rightFoot = Point3D(12f, basePosY - 10f, 0f),
                leftShoulder = Point3D(-22f, basePosY + 70f, 0f),
                rightShoulder = Point3D(22f, basePosY + 70f, 0f),
                leftElbow = Point3D(-28f, basePosY + 85f + height * 0.5f, -10f),
                rightElbow = Point3D(28f, basePosY + 85f + height * 0.5f, -10f),
                leftHand = Point3D(-25f, basePosY + 110f + height, 0f),
                rightHand = Point3D(25f, basePosY + 110f + height, 0f)
            )
        }
        2 -> { // Saltar la Cuerda
            val hop = (sin(t * 2f) + 1f) * 12f
            val posY = -20f + hop

            Skeleton3D(
                head = Point3D(0f, posY + 90f, 0f),
                neck = Point3D(0f, posY + 75f, 0f),
                spine = Point3D(0f, posY + 45f, 0f),
                leftHip = Point3D(-12f, posY + 35f, 0f),
                rightHip = Point3D(12f, posY + 35f, 0f),
                leftKnee = Point3D(-14f, posY + 15f, 5f),
                rightKnee = Point3D(14f, posY + 15f, 5f),
                leftFoot = Point3D(-12f, posY - 10f, 0f),
                rightFoot = Point3D(12f, posY - 10f, 0f),
                leftShoulder = Point3D(-22f, posY + 70f, 0f),
                rightShoulder = Point3D(22f, posY + 70f, 0f),
                leftElbow = Point3D(-28f, posY + 50f, 10f),
                rightElbow = Point3D(28f, posY + 50f, 10f),
                leftHand = Point3D(-32f, posY + 45f, 15f),
                rightHand = Point3D(32f, posY + 45f, 15f),
                propPos = Point3D(0f, posY + 45f, 0f)
            )
        }
        3 -> { // Lanzar
            val throwPhase = sin(t)
            val armBack = throwPhase * 30f
            val ballZ = throwPhase * 50f

            Skeleton3D(
                head = Point3D(0f, 70f, 0f),
                neck = Point3D(0f, 55f, 0f),
                spine = Point3D(0f, 25f, 0f),
                leftHip = Point3D(-12f, 15f, 0f),
                rightHip = Point3D(12f, 15f, 0f),
                leftKnee = Point3D(-14f, -5f, 0f),
                rightKnee = Point3D(14f, -5f, 10f),
                leftFoot = Point3D(-12f, -30f, 0f),
                rightFoot = Point3D(12f, -30f, 15f),
                leftShoulder = Point3D(-22f, 50f, 0f),
                rightShoulder = Point3D(22f, 50f, 0f),
                leftElbow = Point3D(-26f, 30f, 10f),
                rightElbow = Point3D(28f, 60f + armBack, -15f),
                leftHand = Point3D(-24f, 15f, 15f),
                rightHand = Point3D(30f, 70f + armBack, 20f + armBack),
                propPos = Point3D(30f, 72f + armBack, 25f + ballZ)
            )
        }
        4 -> { // Patear
            val kick = sin(t)
            val legZ = kick * 35f
            val legY = (if (kick > 0) kick else 0f) * 20f

            Skeleton3D(
                head = Point3D(0f, 70f, 0f),
                neck = Point3D(0f, 55f, 0f),
                spine = Point3D(0f, 25f, 0f),
                leftHip = Point3D(-12f, 15f, 0f),
                rightHip = Point3D(12f, 15f, 0f),
                leftKnee = Point3D(-14f, -5f, -5f),
                rightKnee = Point3D(14f, -5f + legY * 0.5f, legZ * 0.5f),
                leftFoot = Point3D(-12f, -30f, -5f),
                rightFoot = Point3D(12f, -30f + legY, legZ),
                leftShoulder = Point3D(-22f, 50f, 0f),
                rightShoulder = Point3D(22f, 50f, 0f),
                leftElbow = Point3D(-28f, 35f, 10f),
                rightElbow = Point3D(28f, 35f, -10f),
                leftHand = Point3D(-30f, 20f, 15f),
                rightHand = Point3D(30f, 20f, -15f),
                propPos = Point3D(12f, -25f, 20f + (if (legZ > 0) legZ * 1.5f else 0f))
            )
        }
        else -> { // Basquetbol Drible
            val bounce = (abs(sin(t * 3f))) * 35f

            Skeleton3D(
                head = Point3D(0f, 65f, 0f),
                neck = Point3D(0f, 50f, 0f),
                spine = Point3D(0f, 20f, 0f),
                leftHip = Point3D(-12f, 10f, 0f),
                rightHip = Point3D(12f, 10f, 0f),
                leftKnee = Point3D(-16f, -10f, 15f),
                rightKnee = Point3D(16f, -10f, 15f),
                leftFoot = Point3D(-14f, -32f, 5f),
                rightFoot = Point3D(14f, -32f, 5f),
                leftShoulder = Point3D(-22f, 45f, 0f),
                rightShoulder = Point3D(22f, 45f, 0f),
                leftElbow = Point3D(-26f, 25f, 15f),
                rightElbow = Point3D(26f, 30f, 15f),
                leftHand = Point3D(-20f, 10f, 20f),
                rightHand = Point3D(20f, 15f, 20f),
                propPos = Point3D(20f, -25f + bounce, 22f)
            )
        }
    }
}

private fun abs(value: Float): Float = if (value < 0) -value else value

private data class Skeleton3D(
    val head: Point3D,
    val neck: Point3D,
    val spine: Point3D,
    val leftHip: Point3D,
    val rightHip: Point3D,
    val leftKnee: Point3D,
    val rightKnee: Point3D,
    val leftFoot: Point3D,
    val rightFoot: Point3D,
    val leftShoulder: Point3D,
    val rightShoulder: Point3D,
    val leftElbow: Point3D,
    val rightElbow: Point3D,
    val leftHand: Point3D,
    val rightHand: Point3D,
    val propPos: Point3D? = null
)
