package com.example.saafloop.feature.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.saafloop.core.designsystem.SaafLoopLogo
import com.example.saafloop.ui.theme.NeoElectricLime
import com.example.saafloop.ui.theme.NeoMidnightBackground
import com.example.saafloop.ui.theme.NeoPrimarySurface
import com.example.saafloop.ui.theme.NeoPrimaryText
import com.example.saafloop.ui.theme.NeoSecondaryText
import kotlinx.coroutines.delay

/**
 * Cinematic Live-Animated Splash Screen for SaafLoop Neo-Eco Futurism:
 * Stage 1: Awakening (Radial Background Glow)
 * Stage 2: Symbol Formation (Infinity Loop draw & scale)
 * Stage 3: Environmental Energy (Pulse)
 * Stage 4: Wordmark & Tagline Reveal
 * Stage 5: Transition callback to main app
 */
@Composable
fun SaafLoopSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var animationStage by remember { mutableStateOf(0) }

    // Sequential Animation Triggers
    LaunchedEffect(Unit) {
        animationStage = 1 // Awakening
        delay(400L)
        animationStage = 2 // Symbol Formation
        delay(600L)
        animationStage = 3 // Wordmark Reveal
        delay(800L)
        animationStage = 4 // Complete
        delay(200L)
        onSplashComplete()
    }

    // Animated Scale & Opacity Properties
    val logoScale by animateFloatAsState(
        targetValue = if (animationStage >= 2) 1.0f else 0.5f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "LogoScale"
    )

    val logoAlpha by animateFloatAsState(
        targetValue = if (animationStage >= 1) 1.0f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "LogoAlpha"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (animationStage >= 3) 1.0f else 0f,
        animationSpec = tween(durationMillis = 600),
        label = "TextAlpha"
    )

    val textOffsetPx by animateFloatAsState(
        targetValue = if (animationStage >= 3) 0f else 30f,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "TextOffset"
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
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Live Logo Symbol
            Box(
                modifier = Modifier
                    .scale(logoScale)
                    .alpha(logoAlpha)
                    .size(130.dp),
                contentAlignment = Alignment.Center
            ) {
                SaafLoopLogo(
                    modifier = Modifier.size(130.dp),
                    progress = if (animationStage >= 2) 1.0f else 0.3f
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Wordmark & Tagline Reveal
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(textAlpha)
                    .offset { IntOffset(0, textOffsetPx.toInt()) }
            ) {
                Text(
                    text = "SaafLoop",
                    style = MaterialTheme.typography.headlineLarge,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeoPrimaryText,
                    letterSpacing = 1.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Small actions. Cleaner tomorrows.",
                    style = MaterialTheme.typography.bodyMedium,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = NeoSecondaryText,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
