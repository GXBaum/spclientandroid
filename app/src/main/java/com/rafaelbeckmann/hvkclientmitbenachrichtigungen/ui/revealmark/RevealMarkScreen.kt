package com.rafaelbeckmann.hvkclientmitbenachrichtigungen.ui.revealmark

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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.R
import com.rafaelbeckmann.hvkclientmitbenachrichtigungen.playback.AndroidAudioPlayer
import kotlinx.coroutines.delay
import org.intellij.lang.annotations.Language
import java.io.File


@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RevealMarkScreen(
    modifier: Modifier = Modifier,
    grade: String
) {
    val context = LocalContext.current
    val player = remember { AndroidAudioPlayer(context) }


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
        var gradientAlpha by remember { mutableStateOf(0f) }




        /*val gradientSize by animateFloatAsState(
            targetValue = showGradientAlpha
            /*animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),*/
            animationSpec = tween(
                durationMillis = 200,
                delayMillis = 0,
                easing = LinearEasing
            ),
            label = "gradientSize"
        )*/
        val gradientSize = 1f

        val animatedAlpha by animateFloatAsState(
            targetValue = gradientAlpha,
            animationSpec = tween(
                durationMillis = 400,
                delayMillis = 0,
                easing = LinearEasing
            ),
            label = "gradientAlpha"
        )

        var trueFontSize by remember { mutableFloatStateOf(96f) }
        //var textSize by remember { mutableStateOf(96.sp) }
        val fontSize by animateFloatAsState(
            targetValue = if (isAnimating) (trueFontSize * 1.2f) else trueFontSize,
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

        LaunchedEffect(gradientAlpha == 1f) {
            delay(500)
            gradientAlpha = 0f
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
                .drawWithCache {
                    val isGradientVisible = animatedAlpha > 0.01f

                    val brush = object : ShaderBrush() {
                        override fun createShader(size: Size): Shader {
                            val biggerDimension = maxOf(size.height, size.width)

                            val alphaAdjustedCoral = Coral.copy(alpha = Coral.alpha * animatedAlpha)
                            val alphaAdjustedYellow = LightYellow.copy(alpha = LightYellow.alpha * animatedAlpha)

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
                                    colors = listOf(alphaAdjustedCoral, alphaAdjustedYellow),

                                    //radius = biggerDimension / 2f,
                                    //radius = biggerDimension * gradientSize,
                                    radius = (biggerDimension * gradientSize).coerceAtLeast(0.1f),

                                    center = size.center,
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
                            gradientAlpha = 1f

                            //textSize = (textSize.value + 10).sp
                            trueFontSize = (trueFontSize + 10)

                            // play audio
                            //TODO: fix this, dass kann so kein guter code sein
                            val assetFileDescriptor = context.resources.openRawResourceFd(R.raw.bing)
                            val tempFile = File.createTempFile("audio", ".mp3", context.cacheDir)
                            assetFileDescriptor.createInputStream().use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            player.playFile(tempFile)



                        } else {
                            //vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
                            view.performHapticFeedback(HapticFeedbackConstants.REJECT)


                            //TODO: fix this, dass kann so kein guter code sein
                            val assetFileDescriptor = context.resources.openRawResourceFd(R.raw.errorsound)
                            val tempFile = File.createTempFile("audio", ".mp3", context.cacheDir)
                            assetFileDescriptor.createInputStream().use { input ->
                                tempFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            player.playFile(tempFile)

                        }
                        isAnimating = true
                        clickCount += 1
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            /*Row(
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
            }*/

            Column (
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    text = "$currentGrade",
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
        // If it's a text-based grade
        Text(
            text = "Note: $grade",
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Das ist keine Zahl",
            fontSize = 24.sp,
            textAlign = TextAlign.Center,
        )
    }

}