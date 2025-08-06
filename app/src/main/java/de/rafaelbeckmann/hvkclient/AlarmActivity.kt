package de.rafaelbeckmann.hvkclient

import android.content.Intent
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberOverscrollEffect
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes.Companion.Cookie12Sided
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import de.rafaelbeckmann.hvkclient.ui.theme.HvKClientTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class DragAnchors { Start, Center, End }

// TODO: action confirmation screen (snooze, dismiss)
class AlarmActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setShowWhenLocked(true)
        setTurnScreenOn(true)
        volumeControlStream = AudioManager.STREAM_ALARM

        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())


        val alarmMessage = intent.getStringExtra("ALARM_MESSAGE") ?: "Alarm triggered"

        setContent {
            HvKClientTheme {
                AlarmScreen(
                    message = alarmMessage,
                    onDismiss = {
                        val dismissIntent = Intent(this, AlarmReceiver::class.java).apply {
                            action = "DISMISS_ALARM"
                        }
                        sendBroadcast(dismissIntent)
                        finish()
                    },
                    // TODO: redirect the name
                    onSnooze = {
                        val snoozeIntent = Intent(this, AlarmReceiver::class.java).apply {
                            action = "SNOOZE_ALARM"
                        }
                        sendBroadcast(snoozeIntent)
                        finish()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmScreen(
    message: String,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }

    // TODO improve this
    LaunchedEffect(Unit) {
        while (true) {
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            delay(1000)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp, 128.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))
            Text(
                text = currentTime,
                fontFamily = FontFamily.Serif,
                fontStyle = FontStyle.Italic,
                fontWeight = FontWeight.Bold,
                fontSize = 130.sp,
            )

            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                fontSize = 30.sp,
                modifier = Modifier.weight(1f)
            )

            AlarmSlider(
                onSnooze = { onSnooze() },
                onDismiss = { onDismiss() },
            )
        }

    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun AlarmSlider(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onSnooze: () -> Unit
) {
    val density = LocalDensity.current
    val thumbSize = 100.dp
    val thumbSizePx = with(density) { thumbSize.toPx() }
    val iconSize = 50.dp
    val thumbSliderPadding = 8.dp

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thumbSize)
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50)
            )
    ) {
        val boxWidth = this.maxWidth
        val boxWidthPx = with(density) { boxWidth.toPx() }

        val centerOffset = (boxWidthPx / 2) - thumbSizePx / 2
        val endOffset = boxWidthPx - thumbSizePx

        val dragState = remember {
            AnchoredDraggableState(
                initialValue = DragAnchors.Center,
                // TODO: das ist deprecated, ich weiß aber nicht, wie man es jetzt macht
                //positionalThreshold = { distance -> distance * 0.5f },
                //velocityThreshold = { 125f },
                //animationSpec = spring(stiffness = Spring.StiffnessMedium)
            )
        }
        val anchors = remember(boxWidthPx, thumbSizePx) {
            // todo maybe change to https://fvilarino.medium.com/exploring-jetpack-compose-anchored-draggable-modifier-5fdb21a0c64c
            DraggableAnchors {
                DragAnchors.Start at 0f
                DragAnchors.Center at centerOffset
                DragAnchors.End at endOffset
            }
        }
        SideEffect { dragState.updateAnchors(anchors) }

        LaunchedEffect(dragState) {
            snapshotFlow { dragState.settledValue }
                .collectLatest {
                    if (it == DragAnchors.End) {
                        onDismiss()
                    } else if (it == DragAnchors.Start) {
                        onSnooze()
                    }
                }
        }

        // Lock direction on drag
        LaunchedEffect(dragState) {
            snapshotFlow { dragState.requireOffset() }
                .collectLatest { offset ->
                    if (offset > centerOffset) {
                        Log.d("test2", "rechts ($offset)")
                        dragState.updateAnchors(
                            DraggableAnchors {
                                DragAnchors.Center at centerOffset
                                DragAnchors.End at endOffset
                            }
                        )
                    } else if (offset < centerOffset) {
                        Log.d("test2", "links ($offset)")
                        dragState.updateAnchors(
                            DraggableAnchors {
                                DragAnchors.Start at 0f
                                DragAnchors.Center at centerOffset
                            }
                        )
                    } else {
                        Log.d("test2", "mitte ($offset)")
                    }
                }
        }

        var isDragging by remember { mutableStateOf(false) }
        val interactionSource = remember { MutableInteractionSource() }
        LaunchedEffect(interactionSource) {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is DragInteraction.Stop, is DragInteraction.Cancel -> {
                        // TODO: refactor und animationen hinzufügen
                        dragState.updateAnchors(anchors)
                        isDragging = false
                    }
                    is DragInteraction.Start -> {
                        isDragging = true
                    }
                }
            }
        }



        var offset by remember { mutableFloatStateOf(0f) }
        var maxOffset by remember { mutableFloatStateOf(0f) }
        var rotation by remember { mutableFloatStateOf(0f) }

        // set thumb rotation animation
        LaunchedEffect(dragState) {
            snapshotFlow { dragState.requireOffset() }
                .collectLatest {
                    offset = it
                    maxOffset = boxWidthPx - thumbSizePx
                    rotation = (offset / maxOffset) * 360f
                }
        }

        val overscroll = rememberOverscrollEffect()

        Box(
            modifier = Modifier
                .padding(thumbSliderPadding)
                .fillMaxSize()
        ){
            // Swipe hint animation
            val infiniteTransition = rememberInfiniteTransition()

            val widthFraction by if (isDragging) {
                remember { mutableFloatStateOf(0f) }
            } else {
                infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "width"
                )
            }
            val opacity by if (isDragging) {
                remember { mutableFloatStateOf(0f) }
            } else {
                infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 0f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "opacity"
                )
            }

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .graphicsLayer { alpha = opacity }
                    .background(
                        color = MaterialTheme.colorScheme.onSecondary,
                        shape = RoundedCornerShape(50)
                    )
                    .fillMaxHeight()
                    .fillMaxWidth(widthFraction)
            )


            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(thumbSliderPadding)
                    .graphicsLayer {
                        val center = maxOffset / 2f
                        if (center > 0f) {
                            alpha = 1f - (abs(offset - center) / center * 2f).coerceIn(
                                0f,
                                1f
                            ) // unsichtbar, wenn 50% der Strecke gewischt
                        }
                    },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "Schlummern")
                Text(text = "Stopp")
            }

            Box(
                modifier = Modifier
                    .anchoredDraggable(
                        state = dragState,
                        orientation = Orientation.Horizontal,
                        overscrollEffect = overscroll,
                        interactionSource = interactionSource
                    )
                    .offset {
                        IntOffset(
                            x = dragState
                                .requireOffset()
                                .roundToInt(),
                            y = 0
                        )
                    }
                    .fillMaxWidth()
                    // TODO: overscroll clipt, deswegen nicht aktiviert
                    //.overscroll(overscroll)

            ) {
                Box(
                    modifier = Modifier
                        .size(thumbSize - (2 * thumbSliderPadding))
                        .graphicsLayer {
                            rotationZ = rotation
                        }
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = Cookie12Sided.toShape()
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        modifier = Modifier
                            .size(iconSize)
                            .graphicsLayer {
                                rotationZ = -rotation
                            },
                        imageVector = Icons.Rounded.Alarm,
                        contentDescription = "Alarm",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}


@Composable
@Preview(
    name = "Alarm Screen",
    showBackground = true,
    //showSystemUi = true
)
fun AlarmScreenPreview() {
    HvKClientTheme {
        AlarmScreen(
            message = "Zeit aufzustehen!",
            onDismiss = {},
            onSnooze = {}
        )
    }
}
/*
@Composable
@Preview(
    name = "Alarm Screen - Dark",
    showBackground = true,
    //showSystemUi = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
)
fun AlarmScreenDarkPreview() {
    HvKClientTheme(darkTheme = true) {
        AlarmScreen(
            message = "Zeit aufzustehen!",
            onDismiss = {},
            onSnooze = {}
        )
    }
}*/