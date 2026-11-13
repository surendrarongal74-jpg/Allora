package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ConfirmationNumber
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.Lavender
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SuccessMint
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.WarningAmber

/**
 * 16. GlassOfferCard — Unified offer card distinguishing live vs sample providers over transparent frosted glass.
 */
@Composable
fun GlassOfferCard(
    title: String,
    subtitle: String,
    priceLabel: String,
    providerLabel: String,
    isSampleEstimate: Boolean,
    metaBadge: String,
    actionLabel: String,
    isSaved: Boolean = false,
    onToggleSave: (() -> Unit)? = null,
    onActionClick: () -> Unit,
    onCardClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        material = GlassMaterial.FROSTED,
        onClick = onCardClick,
        accentBorder = if (isSampleEstimate) WarningAmber.copy(alpha = 0.45f) else SoftCyan.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = glass.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary)
                )
            }
            if (onToggleSave != null) {
                IconButton(onClick = onToggleSave, modifier = Modifier.size(36.dp)) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (isSaved) "Remove bookmark" else "Save offer",
                        tint = if (isSaved) SoftCyan else glass.textSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = priceLabel,
                style = TabularNumberStyle.copy(
                    fontSize = 19.sp,
                    color = SuccessMint
                )
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusGlassPill(
                label = if (isSampleEstimate) "Sample Estimate" else "Adapter Ready",
                color = if (isSampleEstimate) WarningAmber else SoftCyan
            )
            StatusGlassPill(label = metaBadge, color = Lavender)
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = providerLabel,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = glass.textMuted,
                    fontFamily = FontFamily.Monospace
                ),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            GlassButton(
                text = actionLabel,
                onClick = onActionClick,
                isPrimary = true,
                material = GlassMaterial.PEARL
            )
        }
    }
}

/**
 * 16b. GlassBoardingFlightCard — Translucent aviation boarding card matching Reference 2 ("Available Flights").
 */
@Composable
fun GlassBoardingFlightCard(
    originIata: String,
    originCity: String,
    destinationIata: String,
    destinationCity: String,
    departureTime: String,
    arrivalTime: String,
    duration: String,
    airline: String,
    flightNumber: String,
    stopsLabel: String,
    cabinClass: String,
    priceUsd: Double,
    isSampleEstimate: Boolean,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onCardClick: () -> Unit,
    onStageHold: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        material = GlassMaterial.FROSTED,
        onClick = onCardClick,
        cornerRadius = 26.dp,
        contentPadding = PaddingValues(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(originIata, style = MaterialTheme.typography.titleLarge.copy(color = glass.textPrimary, fontWeight = FontWeight.Bold))
                Text(originCity, style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary))
            }

            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth().height(20.dp), contentAlignment = Alignment.Center) {
                    Canvas(modifier = Modifier.fillMaxWidth().height(2.dp)) {
                        drawLine(
                            color = Color.White.copy(alpha = 0.45f),
                            start = Offset(0f, size.height / 2f),
                            end = Offset(size.width, size.height / 2f),
                            strokeWidth = 2f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        tint = SoftCyan,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text("$duration • $stopsLabel", style = MaterialTheme.typography.labelSmall.copy(color = SoftCyan))
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(destinationIata, style = MaterialTheme.typography.titleLarge.copy(color = glass.textPrimary, fontWeight = FontWeight.Bold))
                Text(destinationCity, style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary))
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(departureTime, style = TabularNumberStyle.copy(color = glass.textPrimary, fontSize = 13.sp))
                Text(flightNumber, style = MaterialTheme.typography.labelSmall.copy(color = glass.textMuted))
            }
            StatusGlassPill(
                label = if (isSampleEstimate) "Sample • $cabinClass" else "Adapter • $cabinClass",
                color = if (isSampleEstimate) WarningAmber else SoftCyan
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(arrivalTime, style = TabularNumberStyle.copy(color = glass.textPrimary, fontSize = 13.sp))
                IconButton(onClick = onToggleSave, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Save flight",
                        tint = if (isSaved) SoftCyan else glass.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Pearl-Glass lower bar matching Reference 2 ("Global Air $520")
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(resolveGlassSurfaceBrush(GlassMaterial.PEARL, glass))
                .border(1.dp, Color.White.copy(alpha = 0.85f), RoundedCornerShape(22.dp))
                .clickable(onClick = onStageHold)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(ElectricBlue.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(15.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = airline,
                    style = MaterialTheme.typography.labelLarge.copy(color = glass.pearlInk, fontWeight = FontWeight.SemiBold)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$${priceUsd.toInt()}",
                    style = TabularNumberStyle.copy(color = glass.pearlInk, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Stage flight hold", tint = glass.pearlInk, modifier = Modifier.size(16.dp))
            }
        }
    }
}

/**
 * 16c. GlassPhotoOfferCard — Scenic thumbnail card for Hotels, Restaurants, Excursions & Cars (Reference 1, 3 & 6).
 */
@Composable
fun GlassPhotoOfferCard(
    title: String,
    subtitle: String,
    priceLabel: String,
    ratingLabel: String,
    @DrawableRes imageRes: Int,
    badgeLabel: String,
    isSampleEstimate: Boolean,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onCardClick: () -> Unit,
    onActionClick: () -> Unit,
    actionLabel: String = "Details",
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val shape = remember { RoundedCornerShape(24.dp) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(resolveGlassSurfaceBrush(GlassMaterial.FROSTED, glass), shape)
            .border(1.dp, resolveGlassBorderBrush(GlassMaterial.FROSTED, glass), shape)
            .clickable(onClick = onCardClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(RoundedCornerShape(18.dp))
        ) {
            AlloraAsyncPhoto(
                resId = imageRes,
                contentDescription = title,
                maxDimensionPx = 420,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0x8807132E))
                    .border(0.6.dp, Color.White.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(badgeLabel, style = MaterialTheme.typography.labelSmall.copy(color = PearlWhite, fontSize = 10.sp))
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onToggleSave, modifier = Modifier.size(28.dp)) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save item",
                        tint = if (isSaved) ErrorCoral else glass.textSecondary,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = priceLabel,
                        style = TabularNumberStyle.copy(color = glass.textPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (isSampleEstimate) "Sample Estimate" else "Adapter Ready",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = if (isSampleEstimate) WarningAmber else SoftCyan,
                            fontSize = 10.sp
                        )
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x5507132E))
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(ratingLabel, style = TabularNumberStyle.copy(color = PearlWhite, fontSize = 11.sp))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(resolveGlassSurfaceBrush(GlassMaterial.PEARL, glass))
                            .clickable(onClick = onActionClick)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(actionLabel, style = MaterialTheme.typography.labelSmall.copy(color = glass.pearlInk, fontWeight = FontWeight.SemiBold))
                    }
                }
            }
        }
    }
}

/**
 * 17. GlassDestinationCard — Large immersive destination photo card featuring Clear floating weather/bookmark badges,
 * Frosted glass location details surface, and Pearl CTA arrow.
 */
@Composable
fun GlassDestinationCard(
    city: String,
    country: String,
    tagline: String,
    weatherSummary: String,
    rating: Double,
    paletteSeed: Int,
    isSaved: Boolean,
    onToggleSave: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    priceEstimateLabel: String? = null
) {
    val glass = LocalAlloraGlassColors.current
    val shape = remember { RoundedCornerShape(28.dp) }
    val backdropRes = AlloraBackdropLibrary.drawableForSeed(paletteSeed)

    Box(
        modifier = modifier
            .height(276.dp)
            .clip(shape)
            .border(1.dp, resolveGlassBorderBrush(GlassMaterial.FROSTED, glass), shape)
            .clickable(onClick = onClick)
    ) {
        // Real scenic destination photograph decoded asynchronously off main thread
        AlloraAsyncPhoto(
            resId = backdropRes,
            contentDescription = "$city, $country",
            maxDimensionPx = 720,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Subtle localized vertical scrim so scenic top/mid stays vibrant while edges stay legible
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to Color(0x40040A1A),
                            0.38f to Color.Transparent,
                            0.72f to Color(0x6E040A1A),
                            1.0f to Color(0xBA040A1A)
                        )
                    )
                )
        )

        // Top Clear Glass row: Weather pill + Bookmark control
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(18.dp))
                    .background(resolveGlassSurfaceBrush(GlassMaterial.CLEAR, glass))
                    .background(Color(0x3D050D22))
                    .border(0.9.dp, resolveGlassBorderBrush(GlassMaterial.CLEAR, glass), RoundedCornerShape(18.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.WbSunny, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(5.dp))
                Text(
                    text = weatherSummary,
                    style = MaterialTheme.typography.labelSmall.copy(color = PearlWhite, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(
                onClick = onToggleSave,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(resolveGlassSurfaceBrush(GlassMaterial.CLEAR, glass))
                    .background(Color(0x45050D22))
                    .border(0.9.dp, resolveGlassBorderBrush(GlassMaterial.CLEAR, glass), CircleShape)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Save destination",
                    tint = if (isSaved) SoftCyan else PearlWhite,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Bottom Frosted location details panel + Pearl View CTA
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(12.dp)
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0x6E07132E))
                .border(0.9.dp, resolveGlassBorderBrush(GlassMaterial.FROSTED, glass), RoundedCornerShape(22.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = city,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = PearlWhite,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = country,
                        style = MaterialTheme.typography.labelMedium.copy(color = SoftCyan),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0x66040B1D))
                        .border(0.7.dp, Color.White.copy(alpha = 0.40f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(13.dp))
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$rating",
                        style = TabularNumberStyle.copy(color = PearlWhite, fontSize = 12.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodySmall.copy(color = PearlWhite.copy(alpha = 0.90f)),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = priceEstimateLabel ?: "Explore guide",
                    style = TabularNumberStyle.copy(color = PearlWhite, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(resolveGlassSurfaceBrush(GlassMaterial.PEARL, glass))
                        .border(0.8.dp, Color.White, RoundedCornerShape(18.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "View",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = glass.pearlInk,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Open $city",
                        tint = glass.pearlInk,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

/**
 * 18. GlassItineraryCard — Timeline card with time window, category pill, transit duration & cost.
 */
@Composable
fun GlassItineraryCard(
    timeRange: String,
    title: String,
    location: String,
    category: String,
    durationMinutes: Int,
    syncStatus: String,
    estimatedCostUsd: Double,
    note: String? = null,
    onMoveUp: (() -> Unit)? = null,
    onEditNote: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        cornerRadius = 22.dp,
        material = GlassMaterial.FROSTED,
        contentPadding = PaddingValues(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusGlassPill(label = timeRange, color = SoftCyan)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                StatusGlassPill(label = category, color = Lavender)
                if (estimatedCostUsd > 0) {
                    Text(
                        text = "$${estimatedCostUsd.toInt()}",
                        style = TabularNumberStyle.copy(color = SuccessMint, fontSize = 13.sp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary, fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = location,
            style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary)
        )
        if (!note.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Note: $note",
                style = MaterialTheme.typography.bodySmall.copy(color = SoftMint)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = glass.textMuted, modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${durationMinutes}m • $syncStatus",
                    style = MaterialTheme.typography.labelSmall.copy(color = glass.textMuted)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (onMoveUp != null) {
                    Text(
                        text = "Move Up",
                        style = MaterialTheme.typography.labelSmall.copy(color = SoftCyan, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable(onClick = onMoveUp).padding(4.dp)
                    )
                }
                if (onEditNote != null) {
                    Text(
                        text = "Edit",
                        style = MaterialTheme.typography.labelSmall.copy(color = Lavender, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable(onClick = onEditNote).padding(4.dp)
                    )
                }
            }
        }
    }
}

/**
 * 19. GlassBookingCard — Boarding pass / voucher card with QR symbol, PNR, and status pill.
 */
@Composable
fun GlassBookingCard(
    referenceCode: String,
    title: String,
    providerSource: String,
    providerPnr: String?,
    amountUsd: Double,
    status: String,
    timestamp: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val statusColor = when {
        status.contains("CONFIRMED", ignoreCase = true) -> SuccessMint
        status.contains("CANCELLED", ignoreCase = true) -> ErrorCoral
        status.contains("HELD", ignoreCase = true) -> WarningAmber
        else -> SoftCyan
    }

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        material = GlassMaterial.FROSTED,
        onClick = onClick,
        accentBorder = statusColor.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.ConfirmationNumber, contentDescription = null, tint = SoftCyan, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = referenceCode,
                    style = TabularNumberStyle.copy(color = SoftCyan, fontSize = 13.sp)
                )
            }
            StatusGlassPill(label = status, color = statusColor)
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary, fontWeight = FontWeight.SemiBold)
        )
        Text(
            text = providerSource,
            style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(resolveGlassSurfaceBrush(GlassMaterial.DEEP, glass))
                .border(0.6.dp, glass.glassBorder, RoundedCornerShape(14.dp))
                .padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PNR: ${providerPnr ?: "Pending Issuance"}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (providerPnr != null) glass.textPrimary else WarningAmber,
                        fontFamily = FontFamily.Monospace
                    )
                )
                Text(
                    text = timestamp,
                    style = MaterialTheme.typography.labelSmall.copy(color = glass.textMuted)
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$${amountUsd.toInt()}",
                    style = TabularNumberStyle.copy(color = glass.textPrimary, fontSize = 16.sp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Icon(Icons.Default.QrCode2, contentDescription = "View pass QR", tint = SoftCyan, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun StatusGlassPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.22f))
            .border(0.8.dp, color.copy(alpha = 0.55f), RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(color = color, fontWeight = FontWeight.SemiBold),
            maxLines = 1
        )
    }
}

/**
 * Reusable Empty, Offline, Provider Unavailable, Permission Denied & Error Glass State Card.
 */
@Composable
fun GlassStateView(
    title: String,
    message: String,
    icon: ImageVector = Icons.Default.Explore,
    accentColor: Color = SoftCyan,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    GlassCard(
        modifier = modifier.fillMaxWidth(),
        material = GlassMaterial.FROSTED,
        accentBorder = accentColor.copy(alpha = 0.5f),
        contentPadding = PaddingValues(22.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor.copy(alpha = 0.55f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(26.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary, fontWeight = FontWeight.SemiBold)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary)
            )
            if (actionLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(14.dp))
                GlassButton(text = actionLabel, onClick = onAction, isPrimary = true, material = GlassMaterial.PEARL)
            }
        }
    }
}
