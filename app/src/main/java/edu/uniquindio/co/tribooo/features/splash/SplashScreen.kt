package edu.uniquindio.co.tribooo.features.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Curva "emphasized decelerate" de Material 3, la misma del prototipo
private val EmphasizedEasing = CubicBezierEasing(0.2f, 0f, 0f, 1f)

/** Pantalla de arranque: se ve mientras la app decide si hay una sesión guardada. */
@Composable
fun SplashScreen() {
    val colors = MaterialTheme.colorScheme

    // Entrada del logo (aparece y crece) y del texto (sube con un pequeño retraso)
    val logo = remember { Animatable(0f) }
    val title = remember { Animatable(0f) }
    val subtitle = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch { logo.animateTo(1f, tween(durationMillis = 600, easing = EmphasizedEasing)) }
        launch {
            delay(120)
            title.animateTo(1f, tween(durationMillis = 500, easing = EmphasizedEasing))
        }
        launch {
            delay(200)
            subtitle.animateTo(1f, tween(durationMillis = 500, easing = EmphasizedEasing))
        }
    }
    val riseOffset = with(LocalDensity.current) { 18.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.primary)
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        alpha = logo.value
                        val scale = 0.82f + 0.18f * logo.value
                        scaleX = scale
                        scaleY = scale
                    }
                    .size(88.dp)
                    .background(colors.onPrimary, RoundedCornerShape(30.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Groups,
                    contentDescription = null,
                    tint = colors.primary,
                    modifier = Modifier.size(46.dp)
                )
            }

            Text(
                text = "Triboo",
                fontSize = 38.sp,
                lineHeight = 44.sp,
                letterSpacing = (-0.95).sp,
                color = colors.onPrimary,
                modifier = Modifier
                    .padding(top = 26.dp)
                    .graphicsLayer {
                        alpha = title.value
                        translationY = riseOffset * (1f - title.value)
                    }
            )

            Text(
                text = "Eventos comunitarios en tu barrio",
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = colors.onPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 8.dp)
                    .graphicsLayer {
                        alpha = subtitle.value
                        translationY = riseOffset * (1f - subtitle.value)
                    }
            )
        }

        Text(
            text = "ARMENIA, QUINDÍO",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 1.2.sp,
            color = colors.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        )
    }
}
