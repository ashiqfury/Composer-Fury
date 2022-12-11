package com.example.furycomposer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.furycomposer.ui.theme.FuryComposerTheme
import com.example.furycomposer.ui.theme.Saffron
import com.example.furycomposer.ui.theme.White
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FuryComposerTheme {
                HexagonLoadingAnimation()
            }
        }
    }
}

@Composable
fun HexagonLoadingAnimation() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(White),
        contentAlignment = Alignment.Center
    ) {
        var isScanning by remember { mutableStateOf(false) }
        HexagonSection(
            isScanning = isScanning,
            onScanButtonClick = {
                isScanning = !isScanning
            },
            color = Saffron,
            backgroundColor = White,
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(0.4f)
                .aspectRatio(6 / 7f)
        )
    }
}

@Composable
fun HexagonSection(
    modifier: Modifier = Modifier,
    isScanning: Boolean,
    onScanButtonClick: () -> Unit,
    color: Color,
    backgroundColor: Color
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (isScanning) {
            Hexagon(
                isFilled = false,
                hexagonColor = color,
                backgroundColor = backgroundColor,
                modifier = Modifier
                    .fillMaxSize(),
                shouldAnimateHexagonBar = true
            )
        } else {
            Hexagon(
                isFilled = false,
                hexagonColor = color,
                backgroundColor = backgroundColor,
                modifier = Modifier
                    .fillMaxSize()
            )
        }
        Hexagon(
            isFilled = true,
            hexagonColor = color,
            backgroundColor = backgroundColor,
            icon = Icons.Default.Search,
            modifier = Modifier
                .fillMaxSize(0.68f),
            onClick = {
                onScanButtonClick()
            }
        )
    }
}

@Composable
fun Hexagon(
    modifier: Modifier = Modifier,
    isFilled: Boolean,
    icon: ImageVector? = null,
    hexagonColor: Color,
    backgroundColor: Color,
    iconTint: Color = White,
    onClick: (() -> Unit)? = null,
    shouldAnimateHexagonBar: Boolean = false
) {
    var clickAnimationOffset by remember { mutableStateOf(Offset.Zero) }
    var canvasSize by remember { mutableStateOf(Size.Zero) }
    var animationRadius by remember { mutableStateOf(0f) }
    var animationRotation by remember { mutableStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        if (shouldAnimateHexagonBar){
            animate(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = 1000,
                        delayMillis = 0,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Restart
                )
            ) { value, _ ->
                animationRotation = value
            }
        }
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectTapGestures { offset ->
                        if (onClick == null) {
                            return@detectTapGestures
                        }
                        onClick()
                        clickAnimationOffset = offset
                        coroutineScope.launch {
                            animate(
                                initialValue = 0f,
                                targetValue = canvasSize.height * 2,
                                animationSpec = tween(durationMillis = 200)
                            ) { value, _ ->
                                animationRadius = value
                            }
                            animationRadius = 0f
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            canvasSize = Size(width = width, height = height)
            
            val path = Path().apply {
                moveTo(width / 2f, 0f)
                lineTo(width, height / 4)
                lineTo(width, height / 4 * 3)
                lineTo(width / 2, height)
                lineTo(0f, height / 4 * 3)
                lineTo(0f, height / 4)
                close()
            }

            if (shouldAnimateHexagonBar) {
                clipPath(path = path) {
                    rotate(degrees = animationRotation) {
                        drawArc(
                            startAngle = 0f,
                            sweepAngle = 150f,
                            brush = Brush.sweepGradient(
                                colors = listOf(
                                    backgroundColor,
                                    backgroundColor,
                                    hexagonColor.copy(0.5f),
                                    hexagonColor.copy(0.5f),
                                    hexagonColor,
                                    hexagonColor,
                                    hexagonColor
                                )
                            ),
                            useCenter = true,
                            size = canvasSize * 1.1f
                        )
                    }
                }
            } else {
                drawPath(
                    path = path,
                    color = hexagonColor,
                    style = if (isFilled) Fill else Stroke(
                        width = 1.dp.toPx()
                    )
                )
            }

            clipPath(path = path) {
                drawCircle(
                    color = White.copy(alpha = 0.2f),
                    radius = animationRadius + 0.1f, // 0.1f prevents app from crash if animationRadius is 0f
                    center = clickAnimationOffset
                )
            }
        }

        icon?.let {
            Icon(
                imageVector = it,
                contentDescription = "hexagon_icon",
                tint = iconTint,
                modifier = Modifier
                    .fillMaxSize(0.4f)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HexagonPreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.DarkGray)
            .aspectRatio(6 / 7f)
            .padding(16.dp)
    ) {
        Hexagon(
            isFilled = true,
            hexagonColor = Saffron,
            backgroundColor = White,
            icon = Icons.Default.Search,
            shouldAnimateHexagonBar = true,
            onClick = {

            }
        )
    }
}