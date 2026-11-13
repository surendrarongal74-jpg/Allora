package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.AlloraGlassColors
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.Lavender
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.Violet

object GlassTokens {
    val PanelRadius = 28.dp
    val CardRadius = 24.dp
    val SmallRadius = 16.dp
    val PillRadius = 42.dp
}

fun resolveGlassSurfaceBrush(
    material: GlassMaterial,
    glass: AlloraGlassColors
): Brush = when (material) {
    GlassMaterial.CLEAR -> Brush.verticalGradient(
        listOf(
            glass.clearGlass.copy(alpha = (glass.clearGlass.alpha + 0.08f).coerceAtMost(0.45f)),
            glass.clearGlass
        )
    )
    GlassMaterial.FROSTED -> Brush.verticalGradient(
        listOf(
            glass.frostedGlass.copy(alpha = (glass.frostedGlass.alpha + 0.10f).coerceAtMost(0.84f)),
            glass.frostedGlass,
            glass.frostedGlass.copy(alpha = (glass.frostedGlass.alpha - 0.05f).coerceAtLeast(0.18f))
        )
    )
    GlassMaterial.DEEP -> Brush.verticalGradient(
        listOf(
            glass.deepGlass.copy(alpha = (glass.deepGlass.alpha + 0.10f).coerceAtMost(0.92f)),
            glass.deepGlass
        )
    )
    GlassMaterial.LUMINOUS -> Brush.linearGradient(
        listOf(
            glass.luminousGlass,
            ElectricBlue.copy(alpha = if (glass.isDark) 0.24f else 0.30f),
            SoftCyan.copy(alpha = if (glass.isDark) 0.18f else 0.26f),
            Lavender.copy(alpha = if (glass.isDark) 0.18f else 0.24f)
        )
    )
    GlassMaterial.PEARL -> Brush.verticalGradient(
        listOf(glass.pearlTop, glass.pearlBottom)
    )
}

fun resolveGlassBorderBrush(
    material: GlassMaterial,
    glass: AlloraGlassColors,
    accentBorder: Color? = null
): Brush = when {
    accentBorder != null -> Brush.linearGradient(
        listOf(
            glass.innerHighlight,
            accentBorder.copy(alpha = 0.88f),
            accentBorder.copy(alpha = 0.36f)
        )
    )
    material == GlassMaterial.LUMINOUS -> Brush.linearGradient(
        listOf(SoftCyan.copy(alpha = 0.85f), glass.innerHighlight, IridescentPink.copy(alpha = 0.55f))
    )
    material == GlassMaterial.PEARL -> Brush.linearGradient(
        listOf(Color.White, Color(0xFFC5DCFF), Color.White.copy(alpha = 0.72f))
    )
    material == GlassMaterial.CLEAR -> Brush.linearGradient(
        listOf(Color.White.copy(alpha = 0.54f), Color.White.copy(alpha = 0.20f))
    )
    material == GlassMaterial.DEEP -> Brush.linearGradient(
        listOf(glass.innerHighlight.copy(alpha = 0.60f), glass.glassBorder.copy(alpha = 0.45f), Color.White.copy(alpha = 0.14f))
    )
    else -> Brush.linearGradient(
        listOf(glass.innerHighlight, SoftCyan.copy(alpha = 0.28f), glass.glassBorder.copy(alpha = 0.26f))
    )
}

private fun glassElevationForMaterial(material: GlassMaterial): Dp = when (material) {
    GlassMaterial.CLEAR -> 4.dp
    GlassMaterial.FROSTED -> 10.dp
    GlassMaterial.LUMINOUS -> 12.dp
    GlassMaterial.DEEP -> 14.dp
    GlassMaterial.PEARL -> 14.dp
}

/**
 * 1. GlassCard — 5-tier transparent liquid glass container (Clear, Frosted, Deep, Luminous, Pearl)
 * with soft depth shadow, directional reflective border, and subtle inner upper highlight bevel.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = GlassTokens.CardRadius,
    isSecondary: Boolean = false,
    material: GlassMaterial = if (isSecondary) GlassMaterial.CLEAR else GlassMaterial.FROSTED,
    accentBorder: Color? = null,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val glass = LocalAlloraGlassColors.current
    val effectiveMaterial = if (isSecondary && material == GlassMaterial.FROSTED) GlassMaterial.CLEAR else material
    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "glassCardScale"
    )

    val bgBrush = resolveGlassSurfaceBrush(effectiveMaterial, glass)
    val borderBrush = resolveGlassBorderBrush(effectiveMaterial, glass, accentBorder)
    val shadowDp = glassElevationForMaterial(effectiveMaterial)
    val innerGlowAlpha = when (effectiveMaterial) {
        GlassMaterial.PEARL -> 0.55f
        GlassMaterial.FROSTED, GlassMaterial.LUMINOUS -> 0.22f
        GlassMaterial.CLEAR -> 0.26f
        GlassMaterial.DEEP -> 0.14f
    }

    val clickableModifier = if (onClick != null) {
        Modifier.clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .clip(shape)
            .background(bgBrush, shape)
            .drawBehind {
                val insetPx = 1.5.dp.toPx()
                val radiusPx = (cornerRadius.toPx() - insetPx).coerceAtLeast(4f)
                drawRoundRect(
                    brush = Brush.verticalGradient(
                        0.0f to Color.White.copy(alpha = innerGlowAlpha),
                        0.28f to Color.Transparent
                    ),
                    topLeft = Offset(insetPx, insetPx),
                    size = Size(size.width - insetPx * 2f, size.height - insetPx * 2f),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .border(width = 1.dp, brush = borderBrush, shape = shape)
            .then(clickableModifier)
            .padding(contentPadding),
        content = content
    )
}

/**
 * 1b. GlassCircleIconButton — Floating circular Clear/Frosted glass button matching top nav & photo actions.
 */
@Composable
fun GlassCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    material: GlassMaterial = GlassMaterial.CLEAR,
    tint: Color? = null,
    size: Dp = 44.dp
) {
    val glass = LocalAlloraGlassColors.current
    val resolvedTint = tint ?: if (material == GlassMaterial.PEARL) glass.pearlInk else glass.textPrimary
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .size(size)
            .clip(CircleShape)
            .background(resolveGlassSurfaceBrush(material, glass), CircleShape)
            .border(1.dp, resolveGlassBorderBrush(material, glass), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = resolvedTint,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * 2. GlassButton — Pearl milky pill for primary actions, Luminous/Clear/Frosted glass for secondary actions.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
    accentColor: Color = ElectricBlue,
    material: GlassMaterial? = null,
    testTag: String? = null
) {
    val glass = LocalAlloraGlassColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleState = animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "glassButtonScale"
    )
    val shape = remember { RoundedCornerShape(GlassTokens.PillRadius) }

    val resolvedMaterial = material ?: when {
        !enabled -> GlassMaterial.CLEAR
        isPrimary && accentColor == ElectricBlue -> GlassMaterial.PEARL
        isPrimary -> GlassMaterial.LUMINOUS
        else -> GlassMaterial.FROSTED
    }

    val bgBrush = when {
        !enabled -> resolveGlassSurfaceBrush(GlassMaterial.CLEAR, glass)
        resolvedMaterial == GlassMaterial.PEARL -> resolveGlassSurfaceBrush(GlassMaterial.PEARL, glass)
        isPrimary && accentColor != ElectricBlue -> Brush.horizontalGradient(
            listOf(accentColor.copy(alpha = 0.88f), accentColor.copy(alpha = 0.68f))
        )
        else -> resolveGlassSurfaceBrush(resolvedMaterial, glass)
    }

    val borderBrush = if (isPrimary && accentColor != ElectricBlue && resolvedMaterial != GlassMaterial.PEARL) {
        Brush.linearGradient(listOf(accentColor.copy(alpha = 0.9f), Color.White.copy(alpha = 0.5f)))
    } else {
        resolveGlassBorderBrush(resolvedMaterial, glass)
    }

    val contentColor = when {
        !enabled -> glass.textMuted
        resolvedMaterial == GlassMaterial.PEARL -> glass.pearlInk
        isPrimary -> PearlWhite
        else -> glass.textPrimary
    }

    Row(
        modifier = modifier
            .graphicsLayer {
                val s = scaleState.value
                scaleX = s
                scaleY = s
            }
            .heightIn(min = 50.dp)
            .clip(shape)
            .background(bgBrush, shape)
            .border(1.dp, borderBrush, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge.copy(
                color = contentColor,
                fontWeight = FontWeight.SemiBold
            )
        )
    }
}

/**
 * 3. GlassInput — Clear/Frosted glass input field with specular rim and optional password toggle.
 */
@Composable
fun GlassInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    isPassword: Boolean = false,
    errorText: String? = null,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val glass = LocalAlloraGlassColors.current
    var passwordVisible by remember { mutableStateOf(false) }
    val borderBrush = if (errorText != null) {
        Brush.linearGradient(listOf(ErrorCoral, ErrorCoral.copy(alpha = 0.6f)))
    } else {
        resolveGlassBorderBrush(GlassMaterial.FROSTED, glass)
    }
    val shape = remember { RoundedCornerShape(18.dp) }

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(color = glass.textSecondary),
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .clip(shape)
                .background(resolveGlassSurfaceBrush(GlassMaterial.FROSTED, glass), shape)
                .border(1.dp, borderBrush, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = SoftCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
            }
            Box(modifier = Modifier.weight(1f)) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium.copy(color = glass.textMuted)
                    )
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = singleLine,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = glass.textPrimary),
                    cursorBrush = SolidColor(SoftCyan),
                    visualTransformation = if (isPassword && !passwordVisible) {
                        PasswordVisualTransformation()
                    } else {
                        VisualTransformation.None
                    },
                    keyboardOptions = keyboardOptions,
                    keyboardActions = keyboardActions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (isPassword) {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        tint = glass.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            } else if (trailingContent != null) {
                trailingContent()
            }
        }
        if (errorText != null) {
            Text(
                text = errorText,
                style = MaterialTheme.typography.labelSmall.copy(color = ErrorCoral),
                modifier = Modifier.padding(start = 6.dp, top = 4.dp)
            )
        }
    }
}

/**
 * 4. GlassSearchBar — Translucent Clear/Frosted pill search bar keeping scenic backdrops visible behind it.
 */
@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    material: GlassMaterial = GlassMaterial.FROSTED,
    onVoiceClick: (() -> Unit)? = null,
    onSubmit: (() -> Unit)? = null,
    trailingAction: (@Composable () -> Unit)? = null
) {
    val glass = LocalAlloraGlassColors.current
    val shape = remember { RoundedCornerShape(30.dp) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(shape)
            .background(resolveGlassSurfaceBrush(material, glass), shape)
            .drawBehind {
                val radiusPx = 30.dp.toPx()
                drawRoundRect(
                    brush = Brush.verticalGradient(0.0f to Color.White.copy(alpha = 0.24f), 0.38f to Color.Transparent),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = 1.dp.toPx())
                )
            }
            .border(1.1.dp, resolveGlassBorderBrush(material, glass), shape)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search",
            tint = SoftCyan,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f)) {
            if (query.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium.copy(color = glass.textMuted)
                )
            }
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = MaterialTheme.typography.bodyMedium.copy(color = glass.textPrimary),
                cursorBrush = SolidColor(SoftCyan),
                keyboardActions = KeyboardActions(onDone = { onSubmit?.invoke() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onQueryChange("") },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Clear", tint = glass.textMuted, modifier = Modifier.size(16.dp))
            }
        }
        if (onVoiceClick != null) {
            IconButton(
                onClick = onVoiceClick,
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Icon(Icons.Default.Mic, contentDescription = "Voice input", tint = Lavender, modifier = Modifier.size(20.dp))
            }
        }
        if (trailingAction != null) {
            trailingAction()
        }
    }
}

/**
 * 7. GlassBottomSheet — Translucent deep-frosted modal bottom sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassBottomSheet(
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val glass = LocalAlloraGlassColors.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        containerColor = if (glass.isDark) Color(0xEB09152E) else Color(0xF5F7FAFF),
        contentColor = glass.textPrimary,
        tonalElevation = 12.dp,
        content = content
    )
}

/**
 * 8. GlassModal — Centered deep-glass dialog overlay.
 */
@Composable
fun GlassModal(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        GlassCard(
            modifier = modifier.fillMaxWidth(),
            cornerRadius = GlassTokens.PanelRadius,
            material = GlassMaterial.DEEP,
            contentPadding = PaddingValues(20.dp),
            content = content
        )
    }
}

/**
 * 9. GlassTabBar — Translucent capsule segmented switcher with Pearl Glass active pill.
 */
@Composable
fun GlassTabBar(
    tabs: List<String>,
    selectedIndex: Int,
    onSelectTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val shape = remember { RoundedCornerShape(26.dp) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(resolveGlassSurfaceBrush(GlassMaterial.CLEAR, glass), shape)
            .border(1.dp, resolveGlassBorderBrush(GlassMaterial.CLEAR, glass), shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        if (selected) {
                            resolveGlassSurfaceBrush(GlassMaterial.PEARL, glass)
                        } else {
                            Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                        }
                    )
                    .clickable { onSelectTab(index) }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = if (selected) glass.pearlInk else glass.textSecondary,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
                    ),
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 10. GlassChip — Translucent Clear glass chip when idle, Luminous or Pearl Glass pill when selected.
 */
@Composable
fun GlassChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    selectedMaterial: GlassMaterial = GlassMaterial.LUMINOUS
) {
    val glass = LocalAlloraGlassColors.current
    val shape = remember { RoundedCornerShape(22.dp) }
    val activeMat = if (selectedMaterial == GlassMaterial.PEARL) GlassMaterial.PEARL else GlassMaterial.LUMINOUS
    val activeContentColor = if (selected && activeMat == GlassMaterial.PEARL) glass.pearlInk else glass.textPrimary

    Row(
        modifier = modifier
            .heightIn(min = 38.dp)
            .clip(shape)
            .background(
                if (selected) {
                    resolveGlassSurfaceBrush(activeMat, glass)
                } else {
                    resolveGlassSurfaceBrush(GlassMaterial.CLEAR, glass)
                },
                shape
            )
            .border(
                1.dp,
                if (selected) resolveGlassBorderBrush(activeMat, glass)
                else resolveGlassBorderBrush(GlassMaterial.CLEAR, glass),
                shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (selected && activeMat == GlassMaterial.PEARL) glass.pearlInk else SoftCyan,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                color = activeContentColor,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium
            ),
            maxLines = 1
        )
    }
}

/**
 * 11. GlassSwitch — Luminous glass capsule switch with spring thumb transition.
 */
@Composable
fun GlassSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 24.dp else 4.dp,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "switchOffset"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .width(52.dp)
            .height(30.dp)
            .clip(CircleShape)
            .background(
                if (checked) {
                    Brush.horizontalGradient(listOf(ElectricBlue, SoftCyan))
                } else {
                    resolveGlassSurfaceBrush(GlassMaterial.CLEAR, glass)
                }
            )
            .border(1.dp, if (checked) SoftCyan else glass.glassBorder, CircleShape)
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(22.dp)
                .clip(CircleShape)
                .background(PearlWhite)
        )
    }
}

/**
 * 12. GlassProgressBar — Iridescent gradient progress bar.
 */
@Composable
fun GlassProgressBar(
    progress: Float,
    modifier: Modifier = Modifier,
    accentBrush: Brush = Brush.horizontalGradient(listOf(SoftCyan, ElectricBlue, Violet))
) {
    val glass = LocalAlloraGlassColors.current
    val clamped = progress.coerceIn(0f, 1f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(glass.clearGlass)
            .border(0.5.dp, glass.glassBorder, CircleShape)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(clamped)
                .height(8.dp)
                .clip(CircleShape)
                .background(accentBrush)
        )
    }
}

/**
 * 13. GlassNotification — Translucent notification card with semantic status dot.
 */
@Composable
fun GlassNotification(
    title: String,
    message: String,
    timestamp: String,
    accentColor: Color = SoftCyan,
    icon: ImageVector = Icons.Default.Check,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 20.dp,
        material = GlassMaterial.FROSTED,
        accentBorder = accentColor.copy(alpha = 0.55f),
        onClick = onClick
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.22f))
                    .border(1.dp, accentColor.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            color = glass.textPrimary,
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timestamp,
                        style = MaterialTheme.typography.labelSmall.copy(color = glass.textMuted)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary)
                )
            }
        }
    }
}

/**
 * 14. GlassLoader — Orbital progress indicator paired with genuine activity status text.
 */
@Composable
fun GlassLoader(
    statusMessage: String,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val transition = rememberInfiniteTransition(label = "glassLoader")
    val angle by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1400, easing = LinearEasing), RepeatMode.Restart),
        label = "loaderAngle"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(resolveGlassSurfaceBrush(GlassMaterial.FROSTED, glass))
            .border(1.dp, glass.glassBorder, RoundedCornerShape(22.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(modifier = Modifier.size(24.dp)) {
            drawCircle(
                color = glass.glassBorder,
                style = Stroke(width = 3.dp.toPx())
            )
            drawArc(
                brush = Brush.sweepGradient(listOf(SoftCyan, ElectricBlue, IridescentPink, SoftCyan)),
                startAngle = angle,
                sweepAngle = 220f,
                useCenter = false,
                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = statusMessage,
            style = MaterialTheme.typography.bodyMedium.copy(color = glass.textPrimary)
        )
    }
}

/**
 * 15. GlassPermissionDialog — Translucent permission rationale dialog with shield metaphor.
 */
@Composable
fun GlassPermissionDialog(
    title: String,
    rationale: String,
    permissionLabel: String,
    onAllow: () -> Unit,
    onDeny: () -> Unit
) {
    val glass = LocalAlloraGlassColors.current
    GlassModal(onDismissRequest = onDeny) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(listOf(SoftCyan.copy(alpha = 0.35f), ElectricBlue.copy(alpha = 0.15f)))
                    )
                    .border(1.dp, SoftCyan.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Shield, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(color = glass.textPrimary)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = permissionLabel,
                style = MaterialTheme.typography.labelMedium.copy(color = SoftCyan)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = rationale,
                style = MaterialTheme.typography.bodyMedium.copy(color = glass.textSecondary)
            )
            Spacer(modifier = Modifier.height(18.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassButton(
                    text = "Not now",
                    onClick = onDeny,
                    isPrimary = false,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Continue",
                    onClick = onAllow,
                    isPrimary = true,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
