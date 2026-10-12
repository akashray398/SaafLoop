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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
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
import com.example.saafloop.ui.theme.NeoControlledCyan
import com.example.saafloop.ui.theme.NeoElectricLime
import com.example.saafloop.ui.theme.NeoMidnightBackground
import com.example.saafloop.ui.theme.NeoMint
import com.example.saafloop.ui.theme.NeoPrimarySurface
import com.example.saafloop.ui.theme.NeoPrimaryText
import com.example.saafloop.ui.theme.NeoSecondaryText
import kotlinx.coroutines.delay

/**
 * SaafLoop Master Animated Splash Screen adhering to the 5-Stage Blueprint:
 * Stage 1: The Journey Begins (Spark path)
 * Stage 2: The Loop Forms (Ribbon cycle)
 * Stage 3: The Leaf Unfolds (Nature joins the loop)
 * Stage 4: Together We Clean (Pin & Community & City skyline)
 * Stage 5: Final Splash Screen ("SaafLoop" wordmark, "Clean Today • Green Tomorrow" tagline, 4 Pillars)
 */
@Composable
fun SaafLoopSplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var stage by remember { mutableIntStateOf(1) }

    // 5-Stage Sequential Animation Timeline
    LaunchedEffect(Unit) {
        stage = 1 // The Journey Begins
        delay(450L)
        stage = 2 // The Loop Forms
        delay(550L)
        stage = 3 // The Leaf Unfolds
        delay(550L)
        stage = 4 // Together We Clean
        delay(650L)
        stage = 5 // Final Splash Screen
        delay(750L)
        onSplashComplete()
    }

    // Animated Progress & Scale
    val sparkProgress by animateFloatAsState(
        targetValue = if (stage >= 1) 1f else 0f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "SparkProgress"
    )

    val loopProgress by animateFloatAsState(
        targetValue = if (stage >= 2) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "LoopProgress"
    )

    val leafProgress by animateFloatAsState(
        targetValue = if (stage >= 3) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "LeafProgress"
    )

    val communityProgress by animateFloatAsState(
        targetValue = if (stage >= 4) 1f else 0f,
        animationSpec = tween(550, easing = FastOutSlowInEasing),
        label = "CommunityProgress"
    )

    val logoScale by animateFloatAsState(
        targetValue = if (stage >= 5) 1.0f else 0.95f,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "LogoScale"
    )

    val textAlpha by animateFloatAsState(
        targetValue = if (stage >= 5) 1.0f else 0f,
        animationSpec = tween(600),
        label = "TextAlpha"
    )

    val textOffsetY by animateFloatAsState(
        targetValue = if (stage >= 5) 0f else 25f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
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
                .padding(vertical = 40.dp, horizontal = 20.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Center Animated Logo & Wordmark
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Master Animated SaafLoop Logo Symbol
                Box(
                    modifier = Modifier
                        .scale(logoScale)
                        .size(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    SaafLoopLogo(
                        modifier = Modifier.size(150.dp),
                        stage = stage,
                        sparkProgress = sparkProgress,
                        loopProgress = loopProgress,
                        leafProgress = leafProgress,
                        communityProgress = communityProgress
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Stage 5 Wordmark & Tagline
                if (stage >= 5) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .alpha(textAlpha)
                            .offset { IntOffset(0, textOffsetY.toInt()) }
                    ) {
                        // Wordmark "Saaf" (White) + "Loop" (Electric Lime Gradient)
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

                        Spacer(modifier = Modifier.height(6.dp))

                        // Tagline: "Clean Today • Green Tomorrow"
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
                } else {
                    // Stages 1-4 Explainer Labels
                    Text(
                        text = when (stage) {
                            1 -> "The Journey Begins…"
                            2 -> "The Loop Forms…"
                            3 -> "The Leaf Unfolds…"
                            4 -> "Together We Clean…"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = NeoMint,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.alpha(0.85f)
                    )
                }
            }

            // Bottom Core 4 Pillars Bar
            AnimatedVisibility(
                visible = stage >= 5,
                enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { it / 2 }
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
