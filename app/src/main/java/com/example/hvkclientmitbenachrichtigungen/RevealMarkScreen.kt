package com.example.hvkclientmitbenachrichtigungen

import android.graphics.RuntimeShader
import android.graphics.Shader
import android.os.Build
import android.os.Vibrator
import android.view.HapticFeedbackConstants
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalView
import kotlinx.coroutines.delay
import org.intellij.lang.annotations.Language
import kotlin.compareTo
import kotlin.div
import kotlin.times


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RevealMarkScreen(
    modifier: Modifier = Modifier,
    grade: String
) {
    // Try to parse the grade as an integer
    val gradeAsInt = grade.toIntOrNull()

    if (gradeAsInt != null) {
        Spacer(modifier = Modifier.height(16.dp))

        var gradePoints = remember { grade.toIntOrNull()?.coerceIn(0, 15) ?: 0 }
        var randomIndices = remember { (0 until 15).shuffled().take(gradePoints) }

        //Text(randomIndices.toString())

        var clickCount by remember { mutableIntStateOf(0) }
        var currentGrade by remember { mutableIntStateOf(0) }

        var isAnimating by remember { mutableStateOf(false) }

        // Animation states for gradient
        var showGradient by remember { mutableStateOf(false) }
        var gradientCenterX by remember { mutableStateOf(0f) }
        var gradientCenterY by remember { mutableStateOf(0f) }




        val gradientSize by animateFloatAsState(
            targetValue = if (showGradient) 1f else 0f,
            /*animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),*/
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 0,
                easing = LinearEasing
            ),
            label = "gradientSize"
        )

        val gradientAlpha by animateFloatAsState(
            targetValue = if (showGradient) 1f else 0f,
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 0,
                easing = LinearEasing
            ),
            finishedListener = {
                if (showGradient) {
                    // Start fade out after showing fully
                    showGradient = false
                }
            },
            label = "gradientAlpha"
        )

        //var textSize by remember { mutableStateOf(96.sp) }
        val fontSize by animateFloatAsState(
            targetValue = if (isAnimating) 120f else 96f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "fontSize"
        )
        LaunchedEffect(isAnimating) {
            if (isAnimating) {
                delay(300)
                isAnimating = false
            }
        }



        @Language("AGSL")
        val CUSTOM_SHADER = """
            uniform float2 resolution;
            layout(color) uniform half4 color;
            layout(color) uniform half4 color2;
        
            half4 main(in float2 fragCoord) {
                float2 uv = fragCoord/resolution.xy;
        
                float mixValue = distance(uv, vec2(0, 1));
                return mix(color, color2, mixValue);
            }
        """.trimIndent()

        val Coral = Color(0xFFF3A397)
        val LightYellow = Color(0xFFF8EE94)



        val largeRadialGradient = object : ShaderBrush() {
            override fun createShader(size: Size): Shader {
                val biggerDimension = maxOf(size.height, size.width)
                return RadialGradientShader(
                    colors = listOf(Coral, LightYellow),
                    center = size.center,
                    radius = biggerDimension / 2f,
                    colorStops = listOf(0f, 0.95f)
                )
            }
        }


        Box (
            modifier = Modifier
                .fillMaxSize()
                //.background(MaterialTheme.colorScheme.surface)
                //.background(largeRadialGradient)
                .drawWithCache {
                    val isGradientVisible = gradientAlpha > 0.01f

                    val brush = object : ShaderBrush() {
                        override fun createShader(size: Size): Shader {
                            val biggerDimension = maxOf(size.height, size.width)

                            val alphaAdjustedCoral = Coral.copy(alpha = Coral.alpha * gradientAlpha)
                            val alphaAdjustedYellow = LightYellow.copy(alpha = LightYellow.alpha * gradientAlpha)

                            /*
                            return RadialGradientShader(
                                colors = listOf(alphaAdjustedCoral, alphaAdjustedYellow),
                                //colors= listOf(Coral, LightYellow),
                                center = size.center,
                                radius = biggerDimension / 2f,
                                colorStops = listOf(0f, 0.95f)
                            )*/
                            return if (isGradientVisible) {
                                RadialGradientShader(
                                    //colors = listOf(Coral, LightYellow),
                                    //radius = biggerDimension / 2f,
                                    colors = listOf(alphaAdjustedCoral, alphaAdjustedYellow),
                                    center = size.center,
                                    radius = biggerDimension * gradientSize,
                                    colorStops = listOf(0f, 0.95f)
                                )
                            } else {
                                // Return a transparent shader when not visible
                                RadialGradientShader(
                                    colors = listOf(Color.Transparent, Color.Transparent),
                                    center = size.center,
                                    radius = biggerDimension / 2f,
                                    colorStops = listOf(0f, 1f)
                                )
                            }
                        }

                    }
                    onDrawBehind {
                        drawRect(brush)
                    }
                }



                /*.drawWithCache {
                    val shader = RuntimeShader(CUSTOM_SHADER)
                    val shaderBrush = ShaderBrush(shader)
                    shader.setFloatUniform("resolution", size.width, size.height)
                    onDrawBehind {
                        shader.setColorUniform(
                            "color",
                            android.graphics.Color.valueOf(
                                LightYellow.red, LightYellow.green,
                                LightYellow
                                    .blue,
                                LightYellow.alpha
                            )
                        )
                        shader.setColorUniform(
                            "color2",
                            android.graphics.Color.valueOf(
                                Coral.red,
                                Coral.green,
                                Coral.blue,
                                Coral.alpha
                            )
                        )
                        drawRect(shaderBrush)
                    }
                }*/



                /*.clickable {
                    if (randomIndices.contains(clickCount)) {
                        currentGrade += 1
                    }
                    clickCount += 1

                    val view = LocalView.current
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                }*/
                //laut ki: The error occurs because you're trying to access LocalView.current inside a clickable lambda, which is not a Composable context. You need to remember the view during composition first.
                .let {
                    // Remember the view during composition phase
                    val view = LocalView.current
                    val context = LocalContext.current
                    val vibrator = context.getSystemService(Vibrator::class.java)


                    it.clickable {

                        if (randomIndices.contains(clickCount)) {
                            currentGrade += 1

                            //vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 500), -1))
                            view.performHapticFeedback(HapticFeedbackConstants.DRAG_START)

                            // Trigger gradient animation on successful tap
                            gradientCenterX = 0.5f // Position at center for simplicity
                            gradientCenterY = 0.5f // (could use clickEvent.position if needed)
                            showGradient = true

                            //textSize = (textSize.value + 10).sp
                        } else {
                            //vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)

                        }
                        isAnimating = true
                        clickCount += 1
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            Row(
                //modifier = Modifier.padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 15) {
                    val isHighlighted = i in randomIndices

                    Text(
                        text = if (isHighlighted) "★" else "☆",
                        fontSize = 24.sp,
                        color = if (isHighlighted)
                            MaterialTheme.colorScheme.primary
                        else
                            MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }

            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    text = "$currentGrade",
                    //fontSize = 96.sp,
                    //fontSize = textSize,
                    fontSize = fontSize.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    text = "$clickCount / 15 mal getippt",
                )
            }


        }



    } else {
        // It's a text-based grade (like "mit gutem erfolg")
        Text(
            text = grade,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Herzlichen Glückwunsch!",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}