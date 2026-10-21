package edu.uniquindio.co.tribooo.features.onboarding

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Campaign
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.HowToReg
import androidx.compose.material.icons.rounded.TravelExplore
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/** Un paso de la guía inicial; los colores salen del tema para respetar el modo oscuro. */
private data class OnboardingStep(
    val icon: ImageVector,
    val title: String,
    val body: String
)

private val steps = listOf(
    OnboardingStep(
        icon = Icons.Rounded.TravelExplore,
        title = "Descubre lo que pasa a dos cuadras",
        body = "Un feed y un mapa con eventos deportivos, culturales, académicos y de voluntariado cerca de ti, filtrados por distancia y categoría."
    ),
    OnboardingStep(
        icon = Icons.Rounded.HowToReg,
        title = "Confirma tu cupo en un toque",
        body = "Reserva tu lugar, guarda eventos para decidir después y te avisamos si algo cambia de hora o se queda sin cupo."
    ),
    OnboardingStep(
        icon = Icons.Rounded.Campaign,
        title = "Publica los eventos de tu comunidad",
        body = "Crea un evento, defínele cupo y lugar, y envíalo a revisión. Sigue su estado y conversa con quienes asisten."
    )
)

/** Guía de 3 pasos para quien abre la app sin una sesión guardada. */
@Composable
fun OnboardingScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
    val pagerState = rememberPagerState(pageCount = { steps.size })
    val scope = rememberCoroutineScope()
    val current = pagerState.currentPage
    val isLastStep = current == steps.lastIndex

    fun goTo(page: Int) {
        scope.launch { pagerState.animateScrollToPage(page) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Paso ${current + 1} de ${steps.size}",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.padding(start = 8.dp)
            )
            TextButton(onClick = onNavigateToLogin) {
                Text("Omitir", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { index ->
            StepContent(step = steps[index], index = index)
        }

        Column(
            modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 20.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            StepIndicator(count = steps.size, current = current, onSelect = ::goTo)

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (current > 0) {
                    OutlinedButton(
                        onClick = { goTo(current - 1) },
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, colors.outline),
                        contentPadding = PaddingValues(horizontal = 22.dp),
                        modifier = Modifier.height(56.dp)
                    ) {
                        Text("Atrás", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = colors.onSurface)
                    }
                }

                Button(
                    onClick = { if (isLastStep) onNavigateToRegister() else goTo(current + 1) },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                ) {
                    Text(
                        text = if (isLastStep) "Crear mi cuenta" else "Siguiente",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = if (isLastStep) Icons.AutoMirrored.Rounded.ArrowForward else Icons.Rounded.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StepContent(step: OnboardingStep, index: Int) {
    val colors = MaterialTheme.colorScheme
    // Cada paso usa un contenedor distinto del tema: secundario, terciario y primario
    val (artBackground, artContent) = when (index) {
        0 -> colors.secondaryContainer to colors.onSecondaryContainer
        1 -> colors.tertiaryContainer to colors.onTertiaryContainer
        else -> colors.primaryContainer to colors.onPrimaryContainer
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(start = 28.dp, end = 28.dp, top = 8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(artBackground),
            contentAlignment = Alignment.Center
        ) {
            DiagonalStripes(color = Color.Black.copy(alpha = 0.045f))
            Icon(
                imageVector = step.icon,
                contentDescription = null,
                tint = artContent,
                modifier = Modifier.size(96.dp)
            )
        }

        Text(
            text = step.title,
            fontSize = 29.sp,
            lineHeight = 35.sp,
            letterSpacing = (-0.58).sp,
            color = colors.onSurface,
            modifier = Modifier.padding(top = 34.dp)
        )

        Text(
            text = step.body,
            fontSize = 15.sp,
            lineHeight = 24.sp,
            color = colors.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

/** Rayas diagonales suaves que ocupan el lugar de la ilustración de cada paso. */
@Composable
private fun DiagonalStripes(color: Color) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val stripe = 10.dp.toPx()
        val period = stripe * 2
        var x = -size.height
        while (x < size.width) {
            drawLine(
                color = color,
                start = Offset(x, size.height),
                end = Offset(x + size.height, 0f),
                strokeWidth = stripe
            )
            x += period * 1.41f // Separación horizontal equivalente a 20dp medidos en diagonal
        }
    }
}

/** Puntos de progreso; el del paso actual se alarga y todos permiten saltar a su paso. */
@Composable
private fun StepIndicator(count: Int, current: Int, onSelect: (Int) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(count) { index ->
            val selected = index == current
            val width by animateDpAsState(if (selected) 26.dp else 8.dp, label = "stepIndicatorWidth")
            val color by animateColorAsState(
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                label = "stepIndicatorColor"
            )
            Box(
                modifier = Modifier
                    .width(width)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
                    .clickable { onSelect(index) }
            )
        }
    }
}
