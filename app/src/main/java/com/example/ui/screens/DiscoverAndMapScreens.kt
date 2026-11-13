package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PointOfInterest
import com.example.ui.components.AlloraAsyncPhoto
import com.example.ui.components.AlloraBackdropLibrary
import com.example.ui.components.GlassBottomSheet
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassCircleIconButton
import com.example.ui.components.GlassDestinationCard
import com.example.ui.components.GlassPhotoOfferCard
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.GlassStateView
import com.example.ui.components.StatusGlassPill
import com.example.ui.components.resolveGlassBorderBrush
import com.example.ui.components.resolveGlassSurfaceBrush
import com.example.ui.theme.Aqua
import com.example.ui.theme.BlueAtmosphere
import com.example.ui.theme.DeepOcean
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.IridescentPink
import com.example.ui.theme.Lavender
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.MidnightNavy
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftMint
import com.example.ui.theme.SuccessMint
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AlloraViewModel
import com.example.ui.viewmodel.DestinationSpotlight
import kotlin.math.roundToInt

private data class MapGlassPin(
    val id: String,
    val title: String,
    val category: String,
    val priceLabel: String,
    val subtitle: String,
    val rating: Double,
    val reviews: Int,
    val xFraction: Float,
    val yFraction: Float,
    val imageRes: Int,
    val destinationSub: SubDestination
)

private data class DiningCuratedPlace(
    val id: String,
    val name: String,
    val neighborhood: String,
    val cuisine: String,
    val pricePerGuestUsd: Double,
    val rating: Double,
    val badge: String,
    val highlightDish: String,
    val imageRes: Int
)

private data class ExcursionOffer(
    val id: String,
    val title: String,
    val location: String,
    val category: String,
    val duration: String,
    val priceUsd: Double,
    val rating: Double,
    val summary: String,
    val imageRes: Int
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DiscoverScreen(
    viewModel: AlloraViewModel,
    onNavigateSub: (SubDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val weatherMap by viewModel.weatherReports.collectAsState()
    val pois by viewModel.poiList.collectAsState()
    val hotels by viewModel.lodgingOffers.collectAsState()
    val profile by viewModel.userProfile.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()

    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedDestination by remember { mutableStateOf<DestinationSpotlight?>(null) }

    val categories = listOf("All", "Luxury", "Culture", "Nature", "City")
    val filteredDestinations = remember(query, selectedCategory) {
        viewModel.destinations.filter { dest ->
            (selectedCategory == "All" || dest.category.equals(selectedCategory, true)) &&
                (query.isBlank() || dest.city.contains(query, true) || dest.country.contains(query, true))
        }
    }

    val filteredPois = remember(query, pois) {
        pois.filter { query.isBlank() || it.name.contains(query, true) || it.address.contains(query, true) || it.category.contains(query, true) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Discover Your\nNext Journey",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = glass.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Weather-aware escapes, dining & curated mobility",
                        style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary)
                    )
                }
                GlassChip(
                    label = "Live Map",
                    selected = true,
                    leadingIcon = Icons.Default.Map,
                    onClick = { onNavigateSub(SubDestination.MAP) }
                )
            }
        }

        item {
            GlassSearchBar(
                query = query,
                onQueryChange = { query = it },
                material = GlassMaterial.CLEAR,
                placeholder = "Search Tokyo, Ginza dining, resorts, spas..."
            )
        }

        item {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    GlassChip(
                        label = cat,
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat }
                    )
                }
            }
        }

        // Booking Vertical Entry Strip (Flights, Hotels, Dining, Activities, Cars)
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    GlassChip(
                        label = "Flights",
                        selected = false,
                        leadingIcon = Icons.Default.Flight,
                        onClick = { onNavigateSub(SubDestination.FLIGHTS) }
                    )
                }
                item {
                    GlassChip(
                        label = "Hotels",
                        selected = false,
                        leadingIcon = Icons.Default.Hotel,
                        onClick = { onNavigateSub(SubDestination.HOTELS) }
                    )
                }
                item {
                    GlassChip(
                        label = "Dining",
                        selected = false,
                        leadingIcon = Icons.Default.Restaurant,
                        onClick = { onNavigateSub(SubDestination.DINING) }
                    )
                }
                item {
                    GlassChip(
                        label = "Excursions",
                        selected = false,
                        leadingIcon = Icons.Default.Explore,
                        onClick = { onNavigateSub(SubDestination.ACTIVITIES) }
                    )
                }
                item {
                    GlassChip(
                        label = "Cars",
                        selected = false,
                        leadingIcon = Icons.Default.DirectionsCar,
                        onClick = { onNavigateSub(SubDestination.CARS) }
                    )
                }
            }
        }

        // Clear-Glass Weather Radar Strip (Repo #9 adapted)
        item {
            Text(
                text = "Atmosphere & Weather Radar",
                style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary)
            )
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(weatherMap.values.toList(), key = { it.city }) { report ->
                    val tempDisplay = if (profile.temperatureUnit == "IMPERIAL") {
                        "${((report.tempCelsius * 9 / 5) + 32).toInt()}°F"
                    } else {
                        "${report.tempCelsius.toInt()}°C"
                    }
                    GlassCard(
                        modifier = Modifier.width(156.dp),
                        material = GlassMaterial.CLEAR,
                        contentPadding = PaddingValues(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WbSunny, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(report.city, style = MaterialTheme.typography.titleSmall.copy(color = glass.textPrimary))
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(tempDisplay, style = TabularNumberStyle.copy(fontSize = 22.sp, color = SoftCyan))
                        Text("${report.condition} • UV ${report.uvIndex}", style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary))
                    }
                }
            }
        }

        // Curated Photographic Destinations
        item {
            Text(
                text = "Popular Destinations",
                style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary)
            )
        }

        if (filteredDestinations.isEmpty()) {
            item {
                GlassStateView(
                    title = "No matching destinations",
                    message = "Try clearing filters or asking ALLORA AI for personalized suggestions.",
                    actionLabel = "Reset Filters",
                    onAction = {
                        query = ""
                        selectedCategory = "All"
                    }
                )
            }
        } else {
            items(filteredDestinations, key = { it.id }) { dest ->
                val weather = weatherMap[dest.city]
                val weatherStr = weather?.let { "${it.tempCelsius.toInt()}°C • ${it.condition}" } ?: "Clear"
                val isSaved = savedItems.any { it.title == dest.city && it.itemType == "DESTINATION" }
                GlassDestinationCard(
                    city = dest.city,
                    country = dest.country,
                    tagline = dest.tagline,
                    weatherSummary = weatherStr,
                    rating = dest.rating,
                    paletteSeed = dest.paletteSeed,
                    isSaved = isSaved,
                    priceEstimateLabel = "Flight from $${dest.estimatedFlightUsd.toInt()}",
                    onToggleSave = {
                        viewModel.toggleSavedItem(
                            itemType = "DESTINATION",
                            title = dest.city,
                            subtitle = dest.country,
                            category = dest.category,
                            priceLabel = "From $${dest.estimatedFlightUsd.toInt()}",
                            rating = dest.rating
                        )
                    },
                    onClick = { selectedDestination = dest }
                )
            }
        }

        // Attractions & Michelin Dining
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Attractions & Michelin Dining",
                    style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary)
                )
                Text(
                    text = "Dining",
                    style = MaterialTheme.typography.labelLarge.copy(color = SoftCyan),
                    modifier = Modifier.clickable { onNavigateSub(SubDestination.DINING) }
                )
            }
        }

        items(filteredPois, key = { it.id }) { poi ->
            val isDining = poi.category.equals("Restaurant", true)
            GlassPhotoOfferCard(
                title = poi.name,
                subtitle = poi.address,
                priceLabel = if (isDining) "$130 tasting" else "$45 pass",
                ratingLabel = "${poi.rating}",
                imageRes = if (isDining) AlloraBackdropLibrary.culinaryTerrace else AlloraBackdropLibrary.coastalIslandAerial,
                badgeLabel = poi.tag,
                isSampleEstimate = false,
                isSaved = savedItems.any { it.title == poi.name },
                onToggleSave = {
                    viewModel.toggleSavedItem(
                        itemType = "DESTINATION",
                        title = poi.name,
                        subtitle = poi.address,
                        category = poi.category,
                        priceLabel = if (isDining) "$130 tasting" else "$45 pass",
                        rating = poi.rating
                    )
                },
                onCardClick = {
                    onNavigateSub(if (isDining) SubDestination.DINING else SubDestination.ACTIVITIES)
                },
                onActionClick = {
                    viewModel.addItinerarySlot(
                        title = poi.name,
                        location = poi.address,
                        timeRange = if (isDining) "07:00 PM – 09:00 PM" else "02:00 PM – 04:00 PM",
                        category = if (isDining) "Dining" else "Sights",
                        costUsd = if (isDining) 130.0 else 45.0
                    )
                    onNavigateSub(SubDestination.ITINERARY)
                },
                actionLabel = "Add Stop"
            )
        }

        // Featured Hotels Preview
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Signature Stays",
                    style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary)
                )
                Text(
                    text = "See all stays",
                    style = MaterialTheme.typography.labelLarge.copy(color = SoftCyan),
                    modifier = Modifier.clickable { onNavigateSub(SubDestination.HOTELS) }
                )
            }
        }

        items(hotels.take(2), key = { it.id }) { hotel ->
            val saved = savedItems.any { it.title == hotel.name && it.itemType == "HOTEL" }
            GlassPhotoOfferCard(
                title = hotel.name,
                subtitle = "${hotel.location} • ${hotel.roomType}",
                priceLabel = "$${hotel.pricePerNightUsd.toInt()}/nt",
                ratingLabel = "${hotel.rating}",
                imageRes = if (hotel.id == "ht-101") AlloraBackdropLibrary.luxuryHotelSuite else AlloraBackdropLibrary.coastalIslandAerial,
                badgeLabel = "Hotel",
                isSampleEstimate = true,
                isSaved = saved,
                onToggleSave = {
                    viewModel.toggleSavedItem(
                        itemType = "HOTEL",
                        title = hotel.name,
                        subtitle = hotel.location,
                        category = "Stay",
                        priceLabel = "$${hotel.pricePerNightUsd.toInt()}/nt",
                        rating = hotel.rating
                    )
                },
                onCardClick = { onNavigateSub(SubDestination.HOTELS) },
                onActionClick = {
                    viewModel.requestLodgingHold(hotel)
                    onNavigateSub(SubDestination.APPROVALS)
                },
                actionLabel = "Hold 2 Nt"
            )
        }
    }

    selectedDestination?.let { dest ->
        GlassBottomSheet(onDismissRequest = { selectedDestination = null }) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(176.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(24.dp))
                ) {
                    AlloraAsyncPhoto(
                        resId = AlloraBackdropLibrary.drawableForSeed(dest.paletteSeed),
                        contentDescription = dest.city,
                        maxDimensionPx = 720,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xBD040A1A))))
                    )
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            StatusGlassPill(label = dest.category, color = SoftCyan)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${dest.city}, ${dest.country}",
                                style = MaterialTheme.typography.headlineMedium.copy(color = PearlWhite, fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = "${dest.rating}★",
                            style = TabularNumberStyle.copy(color = WarningAmber, fontSize = 16.sp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(dest.tagline, style = MaterialTheme.typography.bodyMedium.copy(color = glass.textSecondary))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Key Districts: ${dest.highlights}", style = MaterialTheme.typography.labelLarge.copy(color = Lavender))
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(material = GlassMaterial.CLEAR, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Est. Flight", style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary))
                            Text("$${dest.estimatedFlightUsd.toInt()}", style = TabularNumberStyle.copy(color = SoftCyan, fontSize = 18.sp))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Est. Sanctuary Stay", style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary))
                            Text("$${dest.estimatedHotelNightUsd.toInt()}/night", style = TabularNumberStyle.copy(color = SuccessMint, fontSize = 18.sp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassButton(
                        text = "Search Flights",
                        onClick = {
                            selectedDestination = null
                            onNavigateSub(SubDestination.FLIGHTS)
                        },
                        isPrimary = false,
                        material = GlassMaterial.CLEAR,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Start Trip",
                        onClick = {
                            viewModel.updateWizard { it.copy(destination = "${dest.city}, ${dest.country}") }
                            selectedDestination = null
                            onNavigateSub(SubDestination.TRIP_WIZARD)
                        },
                        isPrimary = true,
                        material = GlassMaterial.PEARL,
                        leadingIcon = Icons.Default.AutoAwesome,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RestaurantDiningScreen(
    viewModel: AlloraViewModel,
    onBack: () -> Unit,
    onHoldSubmitted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val savedItems by viewModel.savedItems.collectAsState()
    var guestCount by remember { mutableIntStateOf(2) }
    var selectedSlot by remember { mutableStateOf("07:30 PM") }
    var cuisineFilter by remember { mutableStateOf("All") }
    var selectedRestaurant by remember { mutableStateOf<DiningCuratedPlace?>(null) }

    BackHandler(onBack = onBack)

    val restaurants = remember {
        listOf(
            DiningCuratedPlace(
                id = "din-1",
                name = "Sushi Sawada",
                neighborhood = "Ginza, Tokyo",
                cuisine = "Omakase",
                pricePerGuestUsd = 130.0,
                rating = 4.9,
                badge = "Michelin 2★",
                highlightDish = "Edomae Aged Tuna & Bafun Uni Course",
                imageRes = AlloraBackdropLibrary.culinaryTerrace
            ),
            DiningCuratedPlace(
                id = "din-2",
                name = "L'Oiseau Blanc Terrace",
                neighborhood = "16th Arr., Paris",
                cuisine = "French Fine Dining",
                pricePerGuestUsd = 165.0,
                rating = 4.8,
                badge = "Eiffel View",
                highlightDish = "Line-Caught Turbot & Champagne Jus",
                imageRes = AlloraBackdropLibrary.culinaryTerrace
            ),
            DiningCuratedPlace(
                id = "din-3",
                name = "Odette Conservatory",
                neighborhood = "National Gallery, Singapore",
                cuisine = "Modern Gastronomy",
                pricePerGuestUsd = 155.0,
                rating = 4.9,
                badge = "Asia Top 10",
                highlightDish = "Kampot Pepper Miyazaki Tasting",
                imageRes = AlloraBackdropLibrary.luxuryHotelSuite
            )
        )
    }

    val filtered = remember(cuisineFilter, restaurants) {
        if (cuisineFilter == "All") restaurants else restaurants.filter { it.cuisine.contains(cuisineFilter, true) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeaderBar(
                title = "Dining Concierge",
                subtitle = "Repo #17/#37 Seat Logic • Two-Phase Table Holds",
                onBack = onBack
            )
        }

        // Translucent Party & Time Bar
        item {
            GlassCard(material = GlassMaterial.CLEAR, modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Table for $guestCount Guests", style = MaterialTheme.typography.titleSmall.copy(color = glass.textPrimary))
                        Text("Seating window: $selectedSlot", style = MaterialTheme.typography.labelSmall.copy(color = SoftCyan))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        GlassChip(label = "-1", selected = false, onClick = { if (guestCount > 1) guestCount -= 1 })
                        GlassChip(label = "+1", selected = true, onClick = { if (guestCount < 8) guestCount += 1 })
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("06:00 PM", "07:30 PM", "08:45 PM").forEach { slot ->
                        GlassChip(label = slot, selected = selectedSlot == slot, onClick = { selectedSlot = slot })
                    }
                }
            }
        }

        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Omakase", "French", "Modern").forEach { filter ->
                    GlassChip(label = filter, selected = cuisineFilter == filter, onClick = { cuisineFilter = filter })
                }
            }
        }

        items(filtered, key = { it.id }) { place ->
            val saved = savedItems.any { it.title == place.name }
            GlassPhotoOfferCard(
                title = place.name,
                subtitle = "${place.neighborhood} • ${place.highlightDish}",
                priceLabel = "$${(place.pricePerGuestUsd * guestCount).toInt()} deposit",
                ratingLabel = "${place.rating}",
                imageRes = place.imageRes,
                badgeLabel = place.badge,
                isSampleEstimate = false,
                isSaved = saved,
                onToggleSave = {
                    viewModel.toggleSavedItem(
                        itemType = "DESTINATION",
                        title = place.name,
                        subtitle = place.neighborhood,
                        category = "Dining",
                        priceLabel = "$${place.pricePerGuestUsd.toInt()}/guest",
                        rating = place.rating
                    )
                },
                onCardClick = { selectedRestaurant = place },
                onActionClick = {
                    viewModel.requestDiningHold(
                        restaurantName = place.name,
                        guests = guestCount,
                        timeSlot = selectedSlot,
                        depositUsd = place.pricePerGuestUsd * guestCount,
                        address = place.neighborhood
                    )
                    onHoldSubmitted()
                },
                actionLabel = "Hold Table"
            )
        }
    }

    selectedRestaurant?.let { place ->
        val deposit = place.pricePerGuestUsd * guestCount
        GlassBottomSheet(onDismissRequest = { selectedRestaurant = null }) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    AlloraAsyncPhoto(
                        resId = place.imageRes,
                        contentDescription = place.name,
                        maxDimensionPx = 720,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xBA040A1A))))
                    )
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                        StatusGlassPill(label = place.badge, color = SoftCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(place.name, style = MaterialTheme.typography.headlineMedium.copy(color = PearlWhite, fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("${place.neighborhood} • ${place.cuisine}", style = MaterialTheme.typography.bodySmall.copy(color = SoftCyan))
                Spacer(modifier = Modifier.height(6.dp))
                Text("Chef Tasting Highlight: ${place.highlightDish}", style = MaterialTheme.typography.bodyMedium.copy(color = glass.textPrimary))
                Spacer(modifier = Modifier.height(12.dp))
                GlassCard(material = GlassMaterial.CLEAR, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$guestCount Guests • $selectedSlot", style = MaterialTheme.typography.bodyMedium.copy(color = glass.textPrimary))
                        Text("$${deposit.toInt()} USD", style = TabularNumberStyle.copy(color = SuccessMint, fontSize = 18.sp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Refundable up to 12 hours prior • Deposit applied to final check.", style = MaterialTheme.typography.labelSmall.copy(color = glass.textSecondary))
                }
                Spacer(modifier = Modifier.height(16.dp))
                GlassButton(
                    text = "Stage Table Hold in Shield",
                    onClick = {
                        viewModel.requestDiningHold(
                            restaurantName = place.name,
                            guests = guestCount,
                            timeSlot = selectedSlot,
                            depositUsd = deposit,
                            address = place.neighborhood
                        )
                        selectedRestaurant = null
                        onHoldSubmitted()
                    },
                    isPrimary = true,
                    material = GlassMaterial.PEARL,
                    leadingIcon = Icons.Default.Restaurant,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActivitiesExcursionsScreen(
    viewModel: AlloraViewModel,
    onBack: () -> Unit,
    onHoldSubmitted: () -> Unit,
    onNavigateSub: (SubDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val savedItems by viewModel.savedItems.collectAsState()
    var filter by remember { mutableStateOf("All") }
    var selectedExcursion by remember { mutableStateOf<ExcursionOffer?>(null) }

    BackHandler(onBack = onBack)

    val excursions = remember {
        listOf(
            ExcursionOffer(
                id = "exc-1",
                title = "Meiji Jingu Private Morning Walk",
                location = "Shibuya, Tokyo",
                category = "Culture",
                duration = "2.5h",
                priceUsd = 85.0,
                rating = 4.9,
                summary = "Curator-led cedar forest sanctuary walk before peak hours.",
                imageRes = AlloraBackdropLibrary.alpineLake
            ),
            ExcursionOffer(
                id = "exc-2",
                title = "Turquoise Lagoon Yacht Excursion",
                location = "Sentosa Cove, Singapore",
                category = "Ocean",
                duration = "4.0h",
                priceUsd = 195.0,
                rating = 4.8,
                summary = "Sunlit island circuit with snorkeling gear and sunset refreshments.",
                imageRes = AlloraBackdropLibrary.coastalIslandAerial
            ),
            ExcursionOffer(
                id = "exc-3",
                title = "Aman Onsen & Holistic Reset",
                location = "Otemachi 33F, Tokyo",
                category = "Wellness",
                duration = "2.0h",
                priceUsd = 220.0,
                rating = 4.9,
                summary = "Private mineral bath circuit overlooking Mount Fuji twilight horizon.",
                imageRes = AlloraBackdropLibrary.luxuryHotelSuite
            )
        )
    }

    val visible = remember(filter, excursions) {
        if (filter == "All") excursions else excursions.filter { it.category.equals(filter, true) }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 18.dp, end = 18.dp, top = 12.dp, bottom = 144.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            ScreenHeaderBar(
                title = "Excursions & Wellness",
                subtitle = "Private Experiences • Itinerary & Shield Ready",
                onBack = onBack
            )
        }
        item {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Culture", "Ocean", "Wellness").forEach { tag ->
                    GlassChip(label = tag, selected = filter == tag, onClick = { filter = tag })
                }
            }
        }
        items(visible, key = { it.id }) { exc ->
            val saved = savedItems.any { it.title == exc.title }
            GlassPhotoOfferCard(
                title = exc.title,
                subtitle = "${exc.location} • ${exc.duration} • ${exc.summary}",
                priceLabel = "$${exc.priceUsd.toInt()}/guest",
                ratingLabel = "${exc.rating}",
                imageRes = exc.imageRes,
                badgeLabel = exc.category,
                isSampleEstimate = false,
                isSaved = saved,
                onToggleSave = {
                    viewModel.toggleSavedItem(
                        itemType = "DESTINATION",
                        title = exc.title,
                        subtitle = exc.location,
                        category = exc.category,
                        priceLabel = "$${exc.priceUsd.toInt()}",
                        rating = exc.rating
                    )
                },
                onCardClick = { selectedExcursion = exc },
                onActionClick = {
                    viewModel.requestActivityHold(
                        experienceTitle = exc.title,
                        guests = 2,
                        sessionWindow = "Tomorrow • 10:00 AM",
                        pricePerGuestUsd = exc.priceUsd,
                        location = exc.location
                    )
                    onHoldSubmitted()
                },
                actionLabel = "Stage Hold"
            )
        }
    }

    selectedExcursion?.let { exc ->
        GlassBottomSheet(onDismissRequest = { selectedExcursion = null }) {
            Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    AlloraAsyncPhoto(
                        resId = exc.imageRes,
                        contentDescription = exc.title,
                        maxDimensionPx = 720,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xBD040A1A))))
                    )
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(14.dp)) {
                        StatusGlassPill(label = "${exc.category} • ${exc.duration}", color = SoftCyan)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(exc.title, style = MaterialTheme.typography.titleLarge.copy(color = PearlWhite, fontWeight = FontWeight.Bold))
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(exc.summary, style = MaterialTheme.typography.bodyMedium.copy(color = glass.textSecondary))
                Spacer(modifier = Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    GlassButton(
                        text = "Add to Timeline",
                        onClick = {
                            viewModel.addItinerarySlot(
                                title = exc.title,
                                location = exc.location,
                                timeRange = "10:00 AM – 12:30 PM",
                                category = "Sights",
                                costUsd = exc.priceUsd
                            )
                            selectedExcursion = null
                            onNavigateSub(SubDestination.ITINERARY)
                        },
                        isPrimary = false,
                        material = GlassMaterial.CLEAR,
                        modifier = Modifier.weight(1f)
                    )
                    GlassButton(
                        text = "Hold ($${(exc.priceUsd * 2).toInt()})",
                        onClick = {
                            viewModel.requestActivityHold(
                                experienceTitle = exc.title,
                                guests = 2,
                                sessionWindow = "Tomorrow • 10:00 AM",
                                pricePerGuestUsd = exc.priceUsd,
                                location = exc.location
                            )
                            selectedExcursion = null
                            onHoldSubmitted()
                        },
                        isPrimary = true,
                        material = GlassMaterial.PEARL,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun FullScreenMapScreen(
    viewModel: AlloraViewModel,
    onBack: () -> Unit,
    onNavigateSub: (SubDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val pois by viewModel.poiList.collectAsState()
    var filter by remember { mutableStateOf("All") }
    var search by remember { mutableStateOf("") }

    BackHandler(onBack = onBack)

    // Combine real POIs and Hotel/Spa/Excursion nodes into Reference 7 interactive map price pins
    val mapPins = remember(pois) {
        listOf(
            MapGlassPin(
                id = "pin-hotel-1",
                title = "Aman Tokyo Sanctuary",
                category = "Hotel",
                priceLabel = "$620",
                subtitle = "Otemachi • Deluxe Imperial Suite",
                rating = 4.9,
                reviews = 1420,
                xFraction = 0.24f,
                yFraction = 0.34f,
                imageRes = AlloraBackdropLibrary.luxuryHotelSuite,
                destinationSub = SubDestination.HOTELS
            ),
            MapGlassPin(
                id = "pin-rest-1",
                title = pois.firstOrNull { it.category == "Restaurant" }?.name ?: "Sushi Sawada (Ginza)",
                category = "Restaurants",
                priceLabel = "$130",
                subtitle = "Ginza • Michelin 2-Star Omakase",
                rating = 4.9,
                reviews = 520,
                xFraction = 0.68f,
                yFraction = 0.42f,
                imageRes = AlloraBackdropLibrary.culinaryTerrace,
                destinationSub = SubDestination.DINING
            ),
            MapGlassPin(
                id = "pin-exc-1",
                title = pois.firstOrNull()?.name ?: "Meiji Jingu Forest Sanctuary",
                category = "Excursions",
                priceLabel = "$85",
                subtitle = "Shibuya • Private Heritage Walk",
                rating = 4.8,
                reviews = 18400,
                xFraction = 0.34f,
                yFraction = 0.56f,
                imageRes = AlloraBackdropLibrary.alpineLake,
                destinationSub = SubDestination.ACTIVITIES
            ),
            MapGlassPin(
                id = "pin-spa-1",
                title = "Otemachi Mineral Sky Spa",
                category = "SPA",
                priceLabel = "$180",
                subtitle = "33F Panoramic Onsen & Hydrotherapy",
                rating = 4.9,
                reviews = 680,
                xFraction = 0.62f,
                yFraction = 0.60f,
                imageRes = AlloraBackdropLibrary.coastalIslandAerial,
                destinationSub = SubDestination.ACTIVITIES
            )
        )
    }

    var activePin by remember { mutableStateOf(mapPins.first()) }

    val visiblePins = remember(mapPins, filter, search) {
        mapPins.filter { pin ->
            (filter == "All" || pin.category.equals(filter, true)) &&
                (search.isBlank() || pin.title.contains(search, true) || pin.subtitle.contains(search, true))
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0A244D), Color(0xFF083254), Color(0xFF061630))
                )
            )
    ) {
        // Luminous Topographic Ocean & Urban Map Canvas (Kepler.gl / VROOM inspired)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Soft bathymetric coastline / landmass shapes
            val islandA = Path().apply {
                moveTo(0f, h * 0.25f)
                cubicTo(w * 0.38f, h * 0.20f, w * 0.48f, h * 0.44f, w * 0.18f, h * 0.62f)
                lineTo(0f, h * 0.68f)
                close()
            }
            drawPath(
                path = islandA,
                color = Color(0xFF103E63).copy(alpha = 0.42f)
            )

            val islandB = Path().apply {
                moveTo(w, h * 0.32f)
                cubicTo(w * 0.65f, h * 0.36f, w * 0.54f, h * 0.58f, w * 0.85f, h * 0.72f)
                lineTo(w, h * 0.78f)
                close()
            }
            drawPath(
                path = islandB,
                color = Color(0xFF12466B).copy(alpha = 0.38f)
            )

            // Fine coordinate grid
            val cols = 6
            val rows = 10
            for (c in 1 until cols) {
                val x = w * (c.toFloat() / cols)
                drawLine(SoftCyan.copy(alpha = 0.08f), Offset(x, 0f), Offset(x, h), strokeWidth = 1.dp.toPx())
            }
            for (r in 1 until rows) {
                val y = h * (r.toFloat() / rows)
                drawLine(SoftCyan.copy(alpha = 0.08f), Offset(0f, y), Offset(w, y), strokeWidth = 1.dp.toPx())
            }

            // VROOM route arc connecting pins
            val routePath = Path().apply {
                moveTo(w * 0.24f, h * 0.34f)
                cubicTo(w * 0.48f, h * 0.26f, w * 0.56f, h * 0.46f, w * 0.68f, h * 0.42f)
                cubicTo(w * 0.74f, h * 0.54f, w * 0.50f, h * 0.64f, w * 0.34f, h * 0.56f)
            }
            drawPath(
                path = routePath,
                brush = Brush.horizontalGradient(listOf(SoftCyan, IridescentPink, Aqua)),
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f), 0f)
                )
            )
        }

        // Interactive Floating Glass Price Pins using BoxWithConstraints for responsive layout
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val maxWidthPx = constraints.maxWidth
            val maxHeightPx = constraints.maxHeight
            visiblePins.forEach { pin ->
                val isSelected = activePin.id == pin.id
                Row(
                    modifier = Modifier
                        .offset {
                            IntOffset(
                                x = ((pin.xFraction * maxWidthPx).roundToInt() - 60).coerceAtLeast(16),
                                y = (pin.yFraction * maxHeightPx).roundToInt()
                            )
                        }
                        .clip(RoundedCornerShape(22.dp))
                        .background(
                            if (isSelected) resolveGlassSurfaceBrush(GlassMaterial.PEARL, glass)
                            else resolveGlassSurfaceBrush(GlassMaterial.CLEAR, glass)
                        )
                        .border(
                            width = 1.dp,
                            brush = if (isSelected) resolveGlassBorderBrush(GlassMaterial.PEARL, glass)
                            else resolveGlassBorderBrush(GlassMaterial.CLEAR, glass),
                            shape = RoundedCornerShape(22.dp)
                        )
                        .clickable { activePin = pin }
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = when (pin.category) {
                            "Hotel" -> Icons.Default.Hotel
                            "Restaurants" -> Icons.Default.Restaurant
                            "SPA" -> Icons.Default.Spa
                            else -> Icons.Default.LocationOn
                        },
                        contentDescription = pin.title,
                        tint = if (isSelected) glass.pearlInk else SoftCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = pin.priceLabel,
                        style = TabularNumberStyle.copy(
                            color = if (isSelected) glass.pearlInk else PearlWhite,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // Top Floating Clear Search & Category Pills
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                GlassCircleIconButton(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    material = GlassMaterial.CLEAR,
                    onClick = onBack,
                    modifier = Modifier.testTag("map_back_button")
                )
                GlassSearchBar(
                    query = search,
                    onQueryChange = { search = it },
                    material = GlassMaterial.CLEAR,
                    placeholder = "Tokyo • Filter stays, dining, excursions...",
                    modifier = Modifier.weight(1f)
                )
            }
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "Hotel", "Excursions", "Restaurants", "SPA")) { f ->
                    GlassChip(label = f, selected = filter == f, onClick = { filter = f })
                }
            }
        }

        // Bottom Frosted Floating Preview Card (Reference 7 style)
        GlassCard(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(16.dp),
            cornerRadius = 28.dp,
            material = GlassMaterial.DEEP,
            accentBorder = SoftCyan.copy(alpha = 0.55f)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AlloraAsyncPhoto(
                    resId = activePin.imageRes,
                    contentDescription = activePin.title,
                    maxDimensionPx = 360,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(18.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusGlassPill(label = activePin.category, color = SoftCyan)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = WarningAmber, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text("${activePin.rating} (${activePin.reviews})", style = TabularNumberStyle.copy(color = PearlWhite, fontSize = 12.sp))
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = activePin.title,
                        style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary, fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${activePin.subtitle} • ${activePin.priceLabel}",
                        style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                GlassButton(
                    text = "Add Stop",
                    onClick = {
                        viewModel.addItinerarySlot(
                            title = activePin.title,
                            location = activePin.subtitle,
                            timeRange = "Flexible Slot",
                            category = if (activePin.category == "Restaurants") "Dining" else "Sights",
                            costUsd = activePin.priceLabel.removePrefix("$").toDoubleOrNull() ?: 65.0
                        )
                        onNavigateSub(SubDestination.ITINERARY)
                    },
                    isPrimary = false,
                    material = GlassMaterial.CLEAR,
                    modifier = Modifier.weight(1f)
                )
                GlassButton(
                    text = "Explore Now",
                    onClick = { onNavigateSub(activePin.destinationSub) },
                    isPrimary = true,
                    material = GlassMaterial.PEARL,
                    leadingIcon = Icons.AutoMirrored.Filled.ArrowForward,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
