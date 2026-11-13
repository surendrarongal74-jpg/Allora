package com.example.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.example.R
import com.example.ui.theme.Aqua
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.DeepSpaceBlack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.LightAtmosphereBottom
import com.example.ui.theme.LightAtmosphereMid
import com.example.ui.theme.LightAtmosphereTop
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.Violet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class AtmosphereMood {
    HOME,
    DISCOVER,
    FLIGHTS,
    HOTELS,
    DINING,
    TRIPS,
    APPROVALS,
    ASSISTANT,
    AUTH,
    MAP
}

object AlloraBackdropLibrary {
    @DrawableRes
    val alpineLake: Int = R.drawable.img_scenic_alpine_lake_1794541896496

    @DrawableRes
    val luxuryHotelSuite: Int = R.drawable.img_hotel_luxury_suite_1794541897259

    @DrawableRes
    val aviationSkyWings: Int = R.drawable.img_aviation_sky_wings_1794541898494

    @DrawableRes
    val coastalIslandAerial: Int = R.drawable.img_coastal_island_aerial_1794541898448

    @DrawableRes
    val culinaryTerrace: Int = R.drawable.img_culinary_evening_terrace_1794541899613

    @DrawableRes
    fun drawableForSeed(seed: Int): Int = when (seed.mod(5)) {
        0 -> alpineLake
        1 -> coastalIslandAerial
        2 -> luxuryHotelSuite
        3 -> culinaryTerrace
        else -> aviationSkyWings
    }

    @DrawableRes
    fun drawableForMood(mood: AtmosphereMood): Int? = when (mood) {
        AtmosphereMood.HOME -> alpineLake
        AtmosphereMood.DISCOVER -> coastalIslandAerial
        AtmosphereMood.FLIGHTS -> aviationSkyWings
        AtmosphereMood.HOTELS -> luxuryHotelSuite
        AtmosphereMood.DINING -> culinaryTerrace
        AtmosphereMood.TRIPS -> alpineLake
        AtmosphereMood.APPROVALS -> luxuryHotelSuite
        AtmosphereMood.AUTH -> aviationSkyWings
        AtmosphereMood.ASSISTANT, AtmosphereMood.MAP -> null
    }
}

private object AsyncDrawableBitmapCache {
    private val cache = LruCache<Long, ImageBitmap>(10)

    fun get(@DrawableRes resId: Int, maxDimPx: Int): ImageBitmap? =
        cache.get(cacheKey(resId, maxDimPx))

    fun put(@DrawableRes resId: Int, maxDimPx: Int, bitmap: ImageBitmap) {
        cache.put(cacheKey(resId, maxDimPx), bitmap)
    }

    private fun cacheKey(resId: Int, maxDimPx: Int): Long =
        (resId.toLong() shl 32) or (maxDimPx.toLong() and 0xffffffffL)

    fun decodeSampledBitmap(context: Context, @DrawableRes resId: Int, maxDimPx: Int): ImageBitmap? {
        return try {
            val res = context.resources
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeResource(res, resId, bounds)
            val width = bounds.outWidth
            val height = bounds.outHeight
            if (width <= 0 || height <= 0) return null

            var inSampleSize = 1
            while ((width / inSampleSize) > maxDimPx || (height / inSampleSize) > maxDimPx) {
                inSampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
            }
            BitmapFactory.decodeResource(res, resId, decodeOpts)?.asImageBitmap()
        } catch (_: Throwable) {
            null
        }
    }
}

/**
 * Decodes drawable JPEGs off the UI main thread on Dispatchers.IO with downsampling + in-memory LRU caching
 * so initial Activity Window / SurfaceSyncGroup frame submissions never stall waiting for JPEG decode.
 */
@Composable
fun AlloraAsyncPhoto(
    @DrawableRes resId: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    maxDimensionPx: Int = 960,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val cachedInitial = AsyncDrawableBitmapCache.get(resId, maxDimensionPx)
    val bitmapState by produceState<ImageBitmap?>(initialValue = cachedInitial, resId, maxDimensionPx) {
        if (value == null) {
            val decoded = withContext(Dispatchers.IO) {
                AsyncDrawableBitmapCache.decodeSampledBitmap(context, resId, maxDimensionPx)
            }
            if (decoded != null) {
                AsyncDrawableBitmapCache.put(resId, maxDimensionPx, decoded)
                value = decoded
            }
        }
    }
    val alpha by animateFloatAsState(
        targetValue = if (bitmapState != null) 1f else 0f,
        animationSpec = tween(durationMillis = 220),
        label = "asyncPhotoAlpha"
    )

    val bitmap = bitmapState
    if (bitmap != null) {
        Image(
            bitmap = bitmap,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier.graphicsLayer { this.alpha = alpha }
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.verticalGradient(listOf(MidnightNavy, DeepOcean, MidnightNavy))
            )
        )
    }
}

/**
 * Full-screen scenic photographic & holographic glass atmosphere for ALLORA.
 * Keeps real travel photography clearly visible behind Clear, Frosted, Deep, Luminous, and Pearl glass panels.
 */
@Composable
fun AlloraAtmosphericBackground(
    mood: AtmosphereMood = AtmosphereMood.HOME,
    @DrawableRes customBackdropRes: Int? = null,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = LocalAlloraGlassColors.current
    val isDark = glassColors.isDark
    val resolvedRes = customBackdropRes ?: AlloraBackdropLibrary.drawableForMood(mood)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                when {
                    mood == AtmosphereMood.ASSISTANT -> Brush.verticalGradient(
                        listOf(DeepSpaceBlack, Color(0xFF061538), DeepSpaceBlack)
                    )
                    isDark -> Brush.verticalGradient(
                        listOf(MidnightNavy, DeepOcean, MidnightNavy)
                    )
                    else -> Brush.verticalGradient(
                        listOf(LightAtmosphereTop, LightAtmosphereMid, LightAtmosphereBottom)
                    )
                }
            )
    ) {
        // Full-bleed scenic photographic backdrop decoded asynchronously off main thread
        Crossfade(
            targetState = resolvedRes,
            animationSpec = tween(durationMillis = 280),
            label = "scenicBackdrop"
        ) { drawableRes ->
            if (drawableRes != null) {
                AlloraAsyncPhoto(
                    resId = drawableRes,
                    contentDescription = null,
                    maxDimensionPx = 960,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Balanced atmospheric lighting & controlled readability scrim (keeps scenery visible!)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val w = size.width
                    val h = size.height

                    if (mood == AtmosphereMood.ASSISTANT) {
                        val orbAuraCenter = Offset(w * 0.5f, h * 0.25f)
                        val orbAuraRadius = (w * 0.85f).coerceAtLeast(1f)
                        val orbAuraBrush = Brush.radialGradient(
                            colors = listOf(
                                ElectricBlue.copy(alpha = 0.34f),
                                SoftCyan.copy(alpha = 0.16f),
                                Color.Transparent
                            ),
                            center = orbAuraCenter,
                            radius = orbAuraRadius
                        )
                        val lowerGlowCenter = Offset(w * 0.5f, h * 0.88f)
                        val lowerGlowRadius = (w * 0.8f).coerceAtLeast(1f)
                        val lowerGlowBrush = Brush.radialGradient(
                            colors = listOf(Violet.copy(alpha = 0.28f), Color.Transparent),
                            center = lowerGlowCenter,
                            radius = lowerGlowRadius
                        )
                        onDrawBehind {
                            drawCircle(brush = orbAuraBrush, center = orbAuraCenter, radius = orbAuraRadius)
                            drawCircle(brush = lowerGlowBrush, center = lowerGlowCenter, radius = lowerGlowRadius)
                        }
                    } else if (mood == AtmosphereMood.MAP) {
                        onDrawBehind { }
                    } else if (isDark) {
                        val scrimBrush = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0x9E050D21),
                                0.16f to Color(0x3D07132E),
                                0.42f to Color(0x5A061128),
                                0.65f to Color(0x3007132E),
                                0.86f to Color(0x8C050E22),
                                1.0f to Color(0xC2040917)
                            )
                        )
                        val glowColor = when (mood) {
                            AtmosphereMood.FLIGHTS -> ElectricBlue
                            AtmosphereMood.DINING -> IridescentPink
                            AtmosphereMood.DISCOVER -> Aqua
                            else -> ElectricBlue
                        }
                        val accentCenter = Offset(w * 0.5f, h * 0.20f)
                        val accentRadius = (w * 0.82f).coerceAtLeast(1f)
                        val accentBrush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 0.22f),
                                SoftCyan.copy(alpha = 0.08f),
                                Color.Transparent
                            ),
                            center = accentCenter,
                            radius = accentRadius
                        )
                        val rimCenter = Offset(w * 0.88f, h * 0.08f)
                        val rimRadius = (w * 0.55f).coerceAtLeast(1f)
                        val rimBrush = Brush.radialGradient(
                            colors = listOf(SoftCyan.copy(alpha = 0.14f), Color.Transparent),
                            center = rimCenter,
                            radius = rimRadius
                        )
                        onDrawBehind {
                            drawRect(brush = scrimBrush)
                            drawCircle(brush = accentBrush, center = accentCenter, radius = accentRadius)
                            drawCircle(brush = rimBrush, center = rimCenter, radius = rimRadius)
                        }
                    } else {
                        val lightScrim = Brush.verticalGradient(
                            colorStops = arrayOf(
                                0.0f to Color(0x78F2F6FF),
                                0.35f to Color(0x3DF5F8FF),
                                0.72f to Color(0x52EEF4FF),
                                1.0f to Color(0xA6F7FAFF)
                            )
                        )
                        onDrawBehind {
                            drawRect(brush = lightScrim)
                        }
                    }
                }
        )

        content()
    }
}
