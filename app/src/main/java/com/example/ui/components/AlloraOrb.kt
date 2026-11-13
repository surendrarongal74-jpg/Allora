package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationConstants
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.withInfiniteAnimationFrameNanos
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Aqua
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.Lavender
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftMint
import com.example.ui.theme.Violet
import com.example.ui.theme.WarningAmber
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

enum class OrbState {
    IDLE,
    LISTENING,
    PROCESSING,
    RESPONDING,
    ERROR,
    OFFLINE
}

@Immutable
internal data class OrbMotionProfile(
    val spinDegreesPerSec: Float,
    val baseScale: Float,
    val breathAmplitude: Float,
    val breathRadPerSec: Float,
    val floatAmplitudeDp: Float,
    val auraMultiplier: Float,
    val ringExpansion: Float
)

internal fun OrbState.toMotionProfile(): OrbMotionProfile = when (this) {
    OrbState.IDLE -> OrbMotionProfile(
        spinDegreesPerSec = 36f,
        baseScale = 1.0f,
        breathAmplitude = 0.018f,
        breathRadPerSec = 2.2f,
        floatAmplitudeDp = 3.2f,
        auraMultiplier = 0.68f,
        ringExpansion = 0f
    )
    OrbState.LISTENING -> OrbMotionProfile(
        spinDegreesPerSec = 84f,
        baseScale = 1.032f,
        breathAmplitude = 0.048f,
        breathRadPerSec = 5.4f,
        floatAmplitudeDp = 4.4f,
        auraMultiplier = 0.96f,
        ringExpansion = 1f
    )
    OrbState.PROCESSING -> OrbMotionProfile(
        spinDegreesPerSec = 195f,
        baseScale = 1.008f,
        breathAmplitude = 0.042f,
        breathRadPerSec = 7.2f,
        floatAmplitudeDp = 2.4f,
        auraMultiplier = 1f,
        ringExpansion = 0.58f
    )
    OrbState.RESPONDING -> OrbMotionProfile(
        spinDegreesPerSec = 104f,
        baseScale = 1.024f,
        breathAmplitude = 0.036f,
        breathRadPerSec = 4.4f,
        floatAmplitudeDp = 3.6f,
        auraMultiplier = 0.90f,
        ringExpansion = 0.85f
    )
    OrbState.ERROR -> OrbMotionProfile(
        spinDegreesPerSec = 16f,
        baseScale = 0.99f,
        breathAmplitude = 0.020f,
        breathRadPerSec = 5.0f,
        floatAmplitudeDp = 1.5f,
        auraMultiplier = 0.85f,
        ringExpansion = 0.25f
    )
    OrbState.OFFLINE -> OrbMotionProfile(
        spinDegreesPerSec = 0f,
        baseScale = 0.96f,
        breathAmplitude = 0f,
        breathRadPerSec = 0f,
        floatAmplitudeDp = 0f,
        auraMultiplier = 0.35f,
        ringExpansion = 0f
    )
}

@Immutable
internal class AlloraOrbSpringDriver(
    val baseScaleAnim: Animatable<Float, AnimationVector1D>,
    val pressScaleAnim: Animatable<Float, AnimationVector1D>,
    val breathAmplitudeAnim: Animatable<Float, AnimationVector1D>,
    val breathSpeedAnim: Animatable<Float, AnimationVector1D>,
    val breathPhaseAnim: Animatable<Float, AnimationVector1D>,
    val floatAmplitudeAnim: Animatable<Float, AnimationVector1D>,
    val orbitalAngleAnim: Animatable<Float, AnimationVector1D>,
    val tiltPhaseAnim: Animatable<Float, AnimationVector1D>,
    val spinVelocityAnim: Animatable<Float, AnimationVector1D>,
    val auraIntensityAnim: Animatable<Float, AnimationVector1D>,
    val ringExpansionAnim: Animatable<Float, AnimationVector1D>
)

@Composable
internal fun rememberAlloraOrbSpringDriver(
    state: OrbState,
    reducedMotion: Boolean,
    isPressed: Boolean
): AlloraOrbSpringDriver {
    val driver = remember {
        val initial = state.toMotionProfile()
        AlloraOrbSpringDriver(
            baseScaleAnim = Animatable(initial.baseScale),
            pressScaleAnim = Animatable(1f),
            breathAmplitudeAnim = Animatable(if (reducedMotion) 0f else initial.breathAmplitude),
            breathSpeedAnim = Animatable(if (reducedMotion) 0f else initial.breathRadPerSec),
            breathPhaseAnim = Animatable(0f),
            floatAmplitudeAnim = Animatable(if (reducedMotion) 0f else initial.floatAmplitudeDp),
            orbitalAngleAnim = Animatable(0f),
            tiltPhaseAnim = Animatable(0f),
            spinVelocityAnim = Animatable(if (reducedMotion) 0f else initial.spinDegreesPerSec),
            auraIntensityAnim = Animatable(initial.auraMultiplier),
            ringExpansionAnim = Animatable(initial.ringExpansion)
        )
    }

    // Spring-backed tactile compression feedback
    LaunchedEffect(isPressed) {
        driver.pressScaleAnim.animateTo(
            targetValue = if (isPressed) 0.92f else 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    }

    // Concurrent non-blocking spring transitions across IDLE, LISTENING, PROCESSING, and other states.
    // All animateTo calls converge finitely and retain velocity smoothly across rapid state switches.
    LaunchedEffect(state, reducedMotion) {
        val profile = state.toMotionProfile()
        val motionScale = if (reducedMotion) 0f else 1f
        coroutineScope {
            launch {
                driver.baseScaleAnim.animateTo(
                    targetValue = profile.baseScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                driver.spinVelocityAnim.animateTo(
                    targetValue = profile.spinDegreesPerSec * motionScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                driver.breathAmplitudeAnim.animateTo(
                    targetValue = profile.breathAmplitude * motionScale,
                    animationSpec = spring(
                        dampingRatio = 0.72f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                driver.breathSpeedAnim.animateTo(
                    targetValue = profile.breathRadPerSec * motionScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                driver.floatAmplitudeAnim.animateTo(
                    targetValue = profile.floatAmplitudeDp * motionScale,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            }
            launch {
                driver.auraIntensityAnim.animateTo(
                    targetValue = profile.auraMultiplier,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                driver.ringExpansionAnim.animateTo(
                    targetValue = profile.ringExpansion,
                    animationSpec = spring(
                        dampingRatio = 0.62f,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
        }
    }

    // Persistent phase-continuous infinite animation loop integrating spring-smoothed velocities and phases.
    // Uses withInfiniteAnimationFrameNanos so Compose test idling resources never deadlock while live UI runs at display Hz.
    LaunchedEffect(reducedMotion) {
        if (reducedMotion) return@LaunchedEffect
        var lastFrameNanos = AnimationConstants.UnspecifiedTime
        val twoPi = (2.0 * PI).toFloat()
        while (isActive) {
            val frameNanos = withInfiniteAnimationFrameNanos { it }
            if (lastFrameNanos != AnimationConstants.UnspecifiedTime) {
                val dtSeconds = ((frameNanos - lastFrameNanos) / 1_000_000_000f).coerceIn(0f, 0.05f)
                val deltaDeg = driver.spinVelocityAnim.value * dtSeconds
                val deltaPhase = driver.breathSpeedAnim.value * dtSeconds
                if (deltaDeg != 0f) {
                    driver.orbitalAngleAnim.snapTo((driver.orbitalAngleAnim.value + deltaDeg) % 360f)
                    driver.tiltPhaseAnim.snapTo((driver.tiltPhaseAnim.value + deltaDeg * 0.58f) % 360f)
                }
                if (deltaPhase != 0f) {
                    driver.breathPhaseAnim.snapTo((driver.breathPhaseAnim.value + deltaPhase) % twoPi)
                }
            }
            lastFrameNanos = frameNanos
        }
    }

    return driver
}

/**
 * Signature ALLORA Iridescent Glass Sphere.
 * Powered by concurrent Animatable spring physics and phase-continuous frame integration,
 * reading animated values exclusively in graphicsLayer and drawWithCache (zero per-frame recompositions).
 */
@Composable
fun AlloraAiOrb(
    state: OrbState = OrbState.IDLE,
    size: Dp = 112.dp,
    reducedMotion: Boolean = false,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val driver = rememberAlloraOrbSpringDriver(
        state = state,
        reducedMotion = reducedMotion,
        isPressed = isPressed
    )

    val auraColors = remember(state) {
        when (state) {
            OrbState.ERROR -> listOf(ErrorCoral.copy(alpha = 0.55f), WarningAmber.copy(alpha = 0.28f), Color.Transparent)
            OrbState.OFFLINE -> listOf(Color(0xFF64748B).copy(alpha = 0.3f), Color.Transparent)
            OrbState.LISTENING -> listOf(SoftCyan.copy(alpha = 0.72f), IridescentPink.copy(alpha = 0.38f), Color.Transparent)
            OrbState.PROCESSING -> listOf(ElectricBlue.copy(alpha = 0.74f), Violet.copy(alpha = 0.46f), Color.Transparent)
            OrbState.RESPONDING -> listOf(Aqua.copy(alpha = 0.68f), SoftMint.copy(alpha = 0.34f), Color.Transparent)
            OrbState.IDLE -> listOf(SoftCyan.copy(alpha = 0.56f), ElectricBlue.copy(alpha = 0.38f), Violet.copy(alpha = 0.24f), Color.Transparent)
        }
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .size(size)
            .graphicsLayer {
                val phase = driver.breathPhaseAnim.value
                val floatDp = if (reducedMotion) 0f else sin(phase * 0.72f) * driver.floatAmplitudeAnim.value
                val breathScale = if (reducedMotion) 0f else sin(phase) * driver.breathAmplitudeAnim.value
                translationY = floatDp * density
                val combinedScale = (driver.baseScaleAnim.value + breathScale) * driver.pressScaleAnim.value
                scaleX = combinedScale
                scaleY = combinedScale
                alpha = if (state == OrbState.OFFLINE) 0.55f else 1f
            }
            .then(clickableModifier),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size)
                .drawWithCache {
                    val radius = this.size.minDimension / 2f
                    val center = Offset(this.size.width / 2f, this.size.height / 2f)
                    val sphereRadius = radius * 0.72f

                    val auraBrush = Brush.radialGradient(
                        colors = auraColors,
                        center = center,
                        radius = radius
                    )
                    val volumeBrush = Brush.radialGradient(
                        colors = when (state) {
                            OrbState.ERROR -> listOf(PearlWhite, ErrorCoral, WarningAmber.copy(alpha = 0.85f), Color(0xFF450A18))
                            OrbState.OFFLINE -> listOf(Color(0xFF94A3B8), Color(0xFF334155), Color(0xFF0F172A))
                            else -> listOf(
                                PearlWhite.copy(alpha = 0.96f),
                                SoftCyan.copy(alpha = 0.90f),
                                ElectricBlue.copy(alpha = 0.94f),
                                Color(0xFF282E8A),
                                Color(0xFF151240)
                            )
                        },
                        center = Offset(center.x - sphereRadius * 0.30f, center.y - sphereRadius * 0.32f),
                        radius = sphereRadius * 1.48f
                    )
                    val bottomBounceBrush = Brush.radialGradient(
                        colors = listOf(
                            IridescentPink.copy(alpha = 0.56f),
                            Violet.copy(alpha = 0.36f),
                            Color.Transparent
                        ),
                        center = Offset(center.x + sphereRadius * 0.42f, center.y + sphereRadius * 0.42f),
                        radius = sphereRadius * 0.95f
                    )
                    val primaryCausticBrush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            SoftCyan.copy(alpha = 0.85f),
                            ElectricBlue.copy(alpha = 0.90f),
                            IridescentPink.copy(alpha = 0.78f),
                            Color.Transparent
                        ),
                        center = center
                    )
                    val secondaryCausticBrush = Brush.sweepGradient(
                        colors = listOf(
                            Color.Transparent,
                            Violet.copy(alpha = 0.75f),
                            Aqua.copy(alpha = 0.78f),
                            PearlWhite.copy(alpha = 0.65f),
                            Color.Transparent
                        ),
                        center = center
                    )
                    val rimBrush = Brush.linearGradient(
                        colors = listOf(
                            PearlWhite.copy(alpha = 0.95f),
                            SoftCyan.copy(alpha = 0.75f),
                            IridescentPink.copy(alpha = 0.68f),
                            Lavender.copy(alpha = 0.72f)
                        ),
                        start = Offset(center.x - sphereRadius, center.y - sphereRadius),
                        end = Offset(center.x + sphereRadius, center.y + sphereRadius)
                    )
                    val glintCenter = Offset(center.x - sphereRadius * 0.38f, center.y - sphereRadius * 0.38f)
                    val glintBrush = Brush.radialGradient(
                        colors = listOf(Color.White.copy(alpha = 0.92f), SoftCyan.copy(alpha = 0.32f), Color.Transparent),
                        center = glintCenter,
                        radius = sphereRadius * 0.42f
                    )
                    val secondaryGlintCenter = Offset(center.x + sphereRadius * 0.36f, center.y + sphereRadius * 0.38f)
                    val secondaryGlintBrush = Brush.radialGradient(
                        colors = listOf(IridescentPink.copy(alpha = 0.65f), Color.Transparent),
                        center = secondaryGlintCenter,
                        radius = sphereRadius * 0.32f
                    )
                    val ribbonTopLeft = Offset(center.x - sphereRadius * 0.80f, center.y - sphereRadius * 0.80f)
                    val ribbonSize = Size(sphereRadius * 1.60f, sphereRadius * 1.60f)
                    val ribbonStroke = Stroke(width = (sphereRadius * 0.16f).coerceAtLeast(2.2f), cap = StrokeCap.Round)

                    val filamentTopLeft = Offset(center.x - sphereRadius * 0.60f, center.y - sphereRadius * 0.60f)
                    val filamentSize = Size(sphereRadius * 1.20f, sphereRadius * 1.20f)
                    val filamentStroke = Stroke(width = (sphereRadius * 0.08f).coerceAtLeast(1.4f), cap = StrokeCap.Round)

                    val innerCoreTopLeft = Offset(center.x - sphereRadius * 0.42f, center.y - sphereRadius * 0.42f)
                    val innerCoreSize = Size(sphereRadius * 0.84f, sphereRadius * 0.84f)
                    val innerCoreStroke = Stroke(width = 1.2.dp.toPx(), cap = StrokeCap.Round)

                    val listeningStroke = Stroke(width = 1.6.dp.toPx())
                    val rimStroke = Stroke(width = 1.8.dp.toPx())
                    val ringColor = if (state == OrbState.PROCESSING) Lavender else SoftCyan

                    onDrawBehind {
                        val effectiveAngle = if (reducedMotion) 25f else driver.orbitalAngleAnim.value
                        val secondaryPhase = if (reducedMotion) -16f else -driver.tiltPhaseAnim.value
                        val auraAlpha = driver.auraIntensityAnim.value.coerceIn(0f, 1f)
                        val ringProgress = driver.ringExpansionAnim.value.coerceIn(0f, 1.2f)

                        drawCircle(
                            brush = auraBrush,
                            radius = radius,
                            center = center,
                            alpha = auraAlpha
                        )

                        if (ringProgress > 0.02f) {
                            val ringRadius = sphereRadius + (radius * 0.94f - sphereRadius) * ringProgress
                            drawCircle(
                                color = ringColor.copy(alpha = (0.58f * ringProgress).coerceIn(0f, 0.72f)),
                                radius = ringRadius,
                                center = center,
                                style = listeningStroke
                            )
                        }

                        drawCircle(brush = volumeBrush, radius = sphereRadius, center = center)
                        drawCircle(brush = bottomBounceBrush, radius = sphereRadius, center = center)

                        rotate(degrees = effectiveAngle, pivot = center) {
                            drawArc(
                                brush = primaryCausticBrush,
                                startAngle = -15f,
                                sweepAngle = 215f,
                                useCenter = false,
                                topLeft = ribbonTopLeft,
                                size = ribbonSize,
                                style = ribbonStroke
                            )
                        }

                        rotate(degrees = secondaryPhase, pivot = center) {
                            drawArc(
                                brush = secondaryCausticBrush,
                                startAngle = 60f,
                                sweepAngle = 195f,
                                useCenter = false,
                                topLeft = filamentTopLeft,
                                size = filamentSize,
                                style = filamentStroke
                            )
                        }

                        rotate(degrees = effectiveAngle * 1.25f, pivot = center) {
                            drawArc(
                                color = PearlWhite.copy(alpha = 0.52f),
                                startAngle = 140f,
                                sweepAngle = 120f,
                                useCenter = false,
                                topLeft = innerCoreTopLeft,
                                size = innerCoreSize,
                                style = innerCoreStroke
                            )
                        }

                        drawCircle(
                            brush = rimBrush,
                            radius = sphereRadius,
                            center = center,
                            style = rimStroke
                        )

                        drawCircle(
                            brush = glintBrush,
                            radius = sphereRadius * 0.40f,
                            center = glintCenter
                        )
                        drawCircle(
                            brush = secondaryGlintBrush,
                            radius = sphereRadius * 0.30f,
                            center = secondaryGlintCenter
                        )
                    }
                }
        )
    }
}

/**
 * 6. FloatingAssistantOrb — Reference-1-inspired pill housing the luminous orb + optional contextual prompt label.
 */
@Composable
fun FloatingAssistantOrb(
    promptLabel: String,
    orbState: OrbState = OrbState.IDLE,
    reducedMotion: Boolean = false,
    showLabel: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val shape = RoundedCornerShape(GlassTokens.PillRadius)

    Row(
        modifier = modifier
            .heightIn(min = 54.dp)
            .clip(shape)
            .background(
                Brush.horizontalGradient(
                    colors = if (glass.isDark) {
                        listOf(Color(0x9C2858B8), Color(0x88153374))
                    } else {
                        listOf(Color(0xF5FFFFFF), Color(0xE0DDEAFF))
                    }
                ),
                shape
            )
            .border(
                1.dp,
                Brush.linearGradient(listOf(SoftCyan.copy(alpha = 0.85f), IridescentPink.copy(alpha = 0.45f), glass.glassBorder)),
                shape
            )
            .clickable(onClick = onClick)
            .testTag("floating_assistant_orb_pill")
            .padding(
                start = if (showLabel) 6.dp else 5.dp,
                end = if (showLabel) 14.dp else 5.dp,
                top = 4.dp,
                bottom = 4.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AlloraAiOrb(
            state = orbState,
            size = if (showLabel) 44.dp else 46.dp,
            reducedMotion = reducedMotion
        )
        if (showLabel) {
            Spacer(modifier = Modifier.width(6.dp))
            AnimatedContent(targetState = promptLabel, label = "orbPromptText") { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = glass.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1
                )
            }
        }
    }
}
