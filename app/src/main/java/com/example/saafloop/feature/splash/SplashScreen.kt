package com.example.saafloop.feature.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saafloop.core.designsystem.SaafLoopLogo
import com.example.saafloop.ui.theme.NeoElectricLime
import com.example.saafloop.ui.theme.NeoMidnightBackground
import com.example.saafloop.ui.theme.NeoMint
import com.example.saafloop.ui.theme.NeoPrimarySurface
import com.example.saafloop.ui.theme.NeoPrimaryText
import com.example.saafloop.ui.theme.NeoSecondaryText
import kotlinx.coroutines.delay

/**
 * Cinematic Live-Animated Splash Screen for SaafLoop Neo-Eco Futurism:
 * Fluid 1.8s sequence revealing the Earth Orb + Leaf SuperScript Logo,
 * branded wordmark ("Saaf" White + "Loop" Lime), tagline ("Clean Today • Green Tomorrow"),
 * and the 4 Core Pillars bar.
 */
@Composable
fun SaafLoopSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isStarted by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isStarted = true
        delay(1800L)
        onSplashComplete()
    }

    val logoScale by animateFloatAsState(
        targetValue = if (isStarted) 1.0f else 0.5f,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "LogoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1.0f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "LogoAlpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (isStarted) 1.0f else 0f,
        animationSpec = tween(durationMillis = 700, delayMillis = 400),
        label = "TextAlpha"
    )

    val textOffsetY by animateFloatAsState(
        targetValue = if (isStarted) 0f else 30f,
        animationSpec = tween(durationMillis = 700, delayMillis = 400, easing = FastOutSlowInEasing),
        label = "TextOffsetY"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        NeoPrimarySurface,
                        NeoMidnightBackground
                    ),
                    radius = 1200f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 44.dp, horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Center Logo & Wordmark
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .scale(logoScale)
                        .alpha(logoAlpha)
                        .size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SaafLoopLogo(
                        modifier = Modifier.size(150.dp),
                        progress = if (isStarted) 1.0f else 0f
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Wordmark & Tagline Reveal
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .alpha(textAlpha)
                        .offset { IntOffset(0, textOffsetY.toInt()) }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Saaf",
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoPrimaryText,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Loop",
                            style = MaterialTheme.typography.headlineLarge,
                            fontSize = 42.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeoElectricLime,
                            letterSpacing = 1.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Clean Today  •  Green Tomorrow",
                        style = MaterialTheme.typography.bodyMedium,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = NeoSecondaryText,
                        textAlign = TextAlign.Center,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            // Bottom Core 4 Pillars Bar
            AnimatedVisibility(
                visible = isStarted,
                enter = fadeIn(tween(600, delayMillis = 600)) + slideInVertically(tween(600, delayMillis = 600)) { it / 2 }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PillarItem(icon = Icons.Default.Autorenew, label = "Report Waste")
                    PillarDivider()
                    PillarItem(icon = Icons.Default.Groups, label = "Connect Volunteers")
                    PillarDivider()
                    PillarItem(icon = Icons.Default.Shield, label = "Verified Cleanups")
                    PillarDivider()
                    PillarItem(icon = Icons.Default.Park, label = "Greener Communities")
                }
            }
        }
    }
}

@Composable
private fun PillarItem(icon: ImageVector, label: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = NeoMint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium,
            color = NeoSecondaryText,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun PillarDivider() {
    Box(
        modifier = Modifier
            .height(18.dp)
            .width(1.dp)
            .background(NeoSecondaryText.copy(alpha = 0.3f))
    )
}
