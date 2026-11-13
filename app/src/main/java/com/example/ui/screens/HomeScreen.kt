package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApprovalRequest
import com.example.data.model.ApprovalStatus
import com.example.data.model.LedgerTransaction
import com.example.data.model.WeatherReport
import com.example.data.remote.UserProfileDoc
import com.example.ui.components.AlloraAiOrb
import com.example.ui.components.GlassBookingCard
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassChip
import com.example.ui.components.GlassDestinationCard
import com.example.ui.components.GlassSearchBar
import com.example.ui.components.OrbState
import com.example.ui.components.StatusGlassPill
import com.example.ui.components.resolveGlassBorderBrush
import com.example.ui.components.resolveGlassSurfaceBrush
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.theme.PearlWhite
import com.example.ui.theme.SoftCyan
import com.example.ui.theme.SoftMint
import com.example.ui.theme.TabularNumberStyle
import com.example.ui.theme.WarningAmber
import com.example.ui.viewmodel.AlloraViewModel
import com.example.ui.viewmodel.DestinationSpotlight
import com.example.ui.viewmodel.TripWizardState

enum class SubDestination {
    NONE,
    AI_ASSISTANT,
    NOTIFICATIONS,
    SAVED_ITEMS,
    MAP,
    FLIGHTS,
    HOTELS,
    DINING,
    ACTIVITIES,
    CARS,
    TRIP_WIZARD,
    ITINERARY,
    BUDGET,
    APPROVALS,
    AUDIT_HUB
}

@Composable
fun HomeScreen(
    viewModel: AlloraViewModel,
    onNavigateSub: (SubDestination) -> Unit,
    onOpenAssistantWithPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val profile by viewModel.userProfile.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()
    val approvals by viewModel.approvalRequests.collectAsState()
    val ledger by viewModel.ledgerTransactions.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val savedItems by viewModel.savedItems.collectAsState()
    val weatherMap by viewModel.weatherReports.collectAsState()
    val wizard by viewModel.wizardState.collectAsState()

    HomeScreenContent(
        profile = profile,
        orbState = orbState,
        reducedMotion = reducedMotion,
        recentSearches = recentSearches,
        approvals = approvals,
        ledger = ledger,
        unreadNotifs = notifications.count { it.isUnread },
        savedCount = savedItems.size,
        weatherMap = weatherMap,
        wizard = wizard,
        destinations = viewModel.destinations,
        isDestinationSaved = { city -> viewModel.isItemSaved(city, "DESTINATION") },
        onToggleDestinationSave = { dest ->
            viewModel.toggleSavedItem(
                itemType = "DESTINATION",
                title = dest.city,
                subtitle = dest.country,
                category = dest.category,
                priceLabel = "From $${dest.estimatedFlightUsd.toInt()}",
                rating = dest.rating
            )
        },
        onNavigateSub = onNavigateSub,
        onOpenAssistantWithPrompt = onOpenAssistantWithPrompt,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeScreenContent(
    profile: UserProfileDoc,
    orbState: OrbState,
    reducedMotion: Boolean,
    recentSearches: List<String>,
    approvals: List<ApprovalRequest>,
    ledger: List<LedgerTransaction>,
    unreadNotifs: Int,
    savedCount: Int,
    weatherMap: Map<String, WeatherReport>,
    wizard: TripWizardState,
    destinations: List<DestinationSpotlight>,
    isDestinationSaved: (String) -> Boolean,
    onToggleDestinationSave: (DestinationSpotlight) -> Unit,
    onNavigateSub: (SubDestination) -> Unit,
    onOpenAssistantWithPrompt: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    val pendingCount = approvals.count { it.status == ApprovalStatus.PENDING }
    var promptDraft by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_scroll_list"),
        contentPadding = PaddingValues(top = 12.dp, bottom = 148.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. Compact Floating Top Bar: Luminous Avatar + Greeting + Clear Controls
        item {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
                    .testTag("home_top_bar")
            ) {
                val isCompactHeader = maxWidth < 340.dp
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(if (isCompactHeader) 40.dp else 44.dp)
                                .clip(CircleShape)
                                .background(resolveGlassSurfaceBrush(GlassMaterial.LUMINOUS, glass))
                                .border(1.1.dp, resolveGlassBorderBrush(GlassMaterial.CLEAR, glass), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = profile.displayName.take(1).uppercase(),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = PearlWhite,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Good evening,",
                                style = MaterialTheme.typography.labelMedium.copy(color = glass.textSecondary),
                                maxLines = 1
                            )
                            Text(
                                text = profile.displayName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = glass.textPrimary,
                                    fontSize = if (isCompactHeader) 17.sp else 20.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        GlassChip(
                            label = if (isCompactHeader) "P0" else "Phase 0",
                            selected = false,
                            leadingIcon = Icons.Default.VerifiedUser,
                            onClick = { onNavigateSub(SubDestination.AUDIT_HUB) },
                            modifier = Modifier.testTag("home_phase0_chip")
                        )

                        Box(
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(resolveGlassSurfaceBrush(GlassMaterial.CLEAR, glass))
                                .border(1.dp, resolveGlassBorderBrush(GlassMaterial.CLEAR, glass), CircleShape)
                                .clickable { onNavigateSub(SubDestination.NOTIFICATIONS) }
                                .testTag("home_notifications_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            BadgedBox(
                                badge = {
                                    if (unreadNotifs > 0) {
                                        Badge(containerColor = ErrorCoral, contentColor = PearlWhite) {
                                            Text("$unreadNotifs", fontSize = 9.sp)
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = glass.textPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Centered Larger Holographic AI Orb + Local Readability Halo + Frosted Search Input
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp)
                    .testTag("home_hero_section"),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AlloraAiOrb(
                    state = orbState,
                    size = 152.dp,
                    reducedMotion = reducedMotion,
                    onClick = { onNavigateSub(SubDestination.AI_ASSISTANT) },
                    modifier = Modifier.testTag("home_hero_ai_orb")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Subtle local radial dark scrim directly behind center typography so mountains remain crisp everywhere else
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .drawBehind {
                            if (glass.isDark) {
                                drawCircle(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0x88040C1E),
                                            Color(0x40040C1E),
                                            Color.Transparent
                                        ),
                                        center = Offset(size.width * 0.5f, size.height * 0.48f),
                                        radius = (size.width * 0.62f).coerceAtLeast(1f)
                                    )
                                )
                            }
                        }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "What can I take care of today?",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            color = glass.textPrimary,
                            fontWeight = FontWeight.Bold
                        ),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Seamless flights, hotels, dining & verified booking holds",
                        style = MaterialTheme.typography.bodyMedium.copy(color = glass.textSecondary),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                GlassSearchBar(
                    query = promptDraft,
                    onQueryChange = { promptDraft = it },
                    placeholder = "Plan Tokyo, compare flights, check weather...",
                    material = GlassMaterial.FROSTED,
                    onVoiceClick = { onNavigateSub(SubDestination.AI_ASSISTANT) },
                    onSubmit = {
                        if (promptDraft.isNotBlank()) {
                            val text = promptDraft
                            promptDraft = ""
                            onOpenAssistantWithPrompt(text)
                        }
                    },
                    trailingAction = {
                        IconButton(
                            onClick = {
                                if (promptDraft.isNotBlank()) {
                                    val text = promptDraft
                                    promptDraft = ""
                                    onOpenAssistantWithPrompt(text)
                                } else {
                                    onNavigateSub(SubDestination.AI_ASSISTANT)
                                }
                            },
                            modifier = Modifier.minimumInteractiveComponentSize()
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send prompt",
                                tint = SoftCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    modifier = Modifier.testTag("home_search_bar")
                )
            }
        }

        // 3. Suggested Prompts (Full-bleed LazyRow — zero edge clipping)
        item {
            val suggestions = remember {
                listOf(
                    "Plan my next trip",
                    "Find the best flights",
                    "Compare hotels",
                    "Discover nearby places",
                    "Organize my travel budget"
                )
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_suggested_prompts_row")
            ) {
                items(suggestions) { suggestion ->
                    GlassChip(
                        label = suggestion,
                        selected = false,
                        leadingIcon = Icons.Default.AutoAwesome,
                        onClick = {
                            when (suggestion) {
                                "Plan my next trip" -> onNavigateSub(SubDestination.TRIP_WIZARD)
                                "Find the best flights" -> onNavigateSub(SubDestination.FLIGHTS)
                                "Compare hotels" -> onNavigateSub(SubDestination.HOTELS)
                                "Discover nearby places" -> onNavigateSub(SubDestination.MAP)
                                "Organize my travel budget" -> onNavigateSub(SubDestination.BUDGET)
                                else -> onOpenAssistantWithPrompt(suggestion)
                            }
                        }
                    )
                }
            }
        }

        // 4. Frosted Glass Travel Module Shortcuts (Full-bleed smooth horizontal row)
        item {
            val shortcuts = remember(pendingCount, savedCount) {
                listOf(
                    Triple("Flights", Icons.Default.Flight, SubDestination.FLIGHTS),
                    Triple("Hotels", Icons.Default.Hotel, SubDestination.HOTELS),
                    Triple("Dining", Icons.Default.Restaurant, SubDestination.DINING),
                    Triple("Activities", Icons.Default.Explore, SubDestination.ACTIVITIES),
                    Triple("Cars", Icons.Default.DirectionsCar, SubDestination.CARS),
                    Triple("Live Map", Icons.Default.Map, SubDestination.MAP),
                    Triple("Shield ($pendingCount)", Icons.Default.Shield, SubDestination.APPROVALS),
                    Triple("Saved ($savedCount)", Icons.Default.Bookmark, SubDestination.SAVED_ITEMS)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Travel & Concierge Modules",
                    style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary),
                    modifier = Modifier.padding(horizontal = 18.dp)
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_shortcuts_row")
                ) {
                    items(shortcuts) { (label, icon, dest) ->
                        QuickModuleTile(
                            label = label,
                            icon = icon,
                            onClick = { onNavigateSub(dest) }
                        )
                    }
                }
            }
        }

        // 5. Dedicated Frosted Upcoming Journey Panel (Destination, Dates, Travellers, Budget, Status, Primary CTAs)
        item {
            UpcomingJourneyPanel(
                wizard = wizard,
                onOpenItinerary = { onNavigateSub(SubDestination.ITINERARY) },
                onOpenBudget = { onNavigateSub(SubDestination.BUDGET) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp)
            )
        }

        // 6. Atmospheric Destinations Full-Bleed Carousel (Large scenic cards)
        item {
            Column(
                modifier = Modifier.testTag("home_destinations_section"),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Atmospheric Destinations",
                        style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary)
                    )
                    Text(
                        text = "Open Map",
                        style = MaterialTheme.typography.labelLarge.copy(color = SoftCyan),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigateSub(SubDestination.MAP) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                LazyRow(
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(destinations, key = { it.id }) { dest ->
                        val weather = weatherMap[dest.city]
                        val weatherStr = if (weather != null) {
                            if (profile.temperatureUnit == "IMPERIAL") {
                                "${((weather.tempCelsius * 9 / 5) + 32).toInt()}°F • ${weather.condition}"
                            } else {
                                "${weather.tempCelsius.toInt()}°C • ${weather.condition}"
                            }
                        } else {
                            "Clear Forecast"
                        }
                        GlassDestinationCard(
                            city = dest.city,
                            country = dest.country,
                            tagline = dest.tagline,
                            weatherSummary = weatherStr,
                            rating = dest.rating,
                            paletteSeed = dest.paletteSeed,
                            isSaved = isDestinationSaved(dest.city),
                            priceEstimateLabel = "Flights from $${dest.estimatedFlightUsd.toInt()}",
                            onToggleSave = { onToggleDestinationSave(dest) },
                            onClick = { onNavigateSub(SubDestination.FLIGHTS) },
                            modifier = Modifier.width(276.dp)
                        )
                    }
                }
            }
        }

        // 7. Pending Shield Approvals Alert (Luminous Tier)
        if (pendingCount > 0) {
            item {
                val firstPending = approvals.first { it.status == ApprovalStatus.PENDING }
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    material = GlassMaterial.LUMINOUS,
                    accentBorder = WarningAmber.copy(alpha = 0.72f),
                    onClick = { onNavigateSub(SubDestination.APPROVALS) }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = WarningAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$pendingCount Hold(s) Awaiting Approval",
                                style = MaterialTheme.typography.titleSmall.copy(color = WarningAmber, fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "$${firstPending.amountUsd.toInt()}",
                            style = TabularNumberStyle.copy(color = glass.textPrimary, fontSize = 16.sp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${firstPending.title} • Tap to inspect policy & authorize two-phase issuance.",
                        style = MaterialTheme.typography.bodySmall.copy(color = glass.textSecondary)
                    )
                }
            }
        }

        // 8. Dark Translucent (DEEP) Glass Text-Heavy Recent Queries Panel
        item {
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp),
                material = GlassMaterial.DEEP,
                cornerRadius = 24.dp,
                contentPadding = PaddingValues(16.dp)
            ) {
                Text(
                    text = "Recent AI Queries",
                    style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary)
                )
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    recentSearches.forEach { query ->
                        GlassChip(
                            label = query,
                            selected = false,
                            onClick = { onOpenAssistantWithPrompt(query) }
                        )
                    }
                }
            }
        }

        // 9. Latest Verified Reservation
        if (ledger.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Verified Reservations",
                        style = MaterialTheme.typography.titleMedium.copy(color = glass.textPrimary)
                    )
                    val latest = ledger.first()
                    GlassBookingCard(
                        referenceCode = latest.referenceCode,
                        title = latest.title,
                        providerSource = "Confirmed via ALLORA Shield",
                        providerPnr = latest.providerPnr,
                        amountUsd = latest.amountUsd,
                        status = latest.status,
                        timestamp = latest.timestamp,
                        onClick = { onNavigateSub(SubDestination.ITINERARY) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun UpcomingJourneyPanel(
    wizard: TripWizardState,
    onOpenItinerary: () -> Unit,
    onOpenBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    GlassCard(
        modifier = modifier.testTag("home_upcoming_journey_card"),
        material = GlassMaterial.FROSTED,
        accentBorder = SoftCyan.copy(alpha = 0.62f),
        onClick = onOpenItinerary,
        contentPadding = PaddingValues(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusGlassPill(label = "Upcoming Journey • Active", color = SoftMint)
            Text(
                text = wizard.style,
                style = MaterialTheme.typography.labelMedium.copy(color = SoftCyan, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = wizard.generatedTitle.ifBlank { "${wizard.destination} Curated Itinerary" },
            style = MaterialTheme.typography.titleLarge.copy(
                color = glass.textPrimary,
                fontWeight = FontWeight.Bold
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Deep Glass nested metric cells for clean contrast separation over mountain scenery
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JourneyDetailTile(
                    icon = Icons.Default.Place,
                    label = "Destination",
                    value = wizard.destination,
                    modifier = Modifier.weight(1f)
                )
                JourneyDetailTile(
                    icon = Icons.Default.CalendarMonth,
                    label = "Dates",
                    value = "${wizard.startDate} – ${wizard.endDate}",
                    modifier = Modifier.weight(1f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                JourneyDetailTile(
                    icon = Icons.Default.Group,
                    label = "Travellers",
                    value = "${wizard.travellersCount} guests",
                    modifier = Modifier.weight(1f)
                )
                JourneyDetailTile(
                    icon = Icons.Default.AccountBalanceWallet,
                    label = "Budget",
                    value = "$${wizard.budgetUsd.toInt()} USD",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            if (maxWidth < 330.dp) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    GlassButton(
                        text = "Open Itinerary",
                        onClick = onOpenItinerary,
                        isPrimary = true,
                        leadingIcon = Icons.Default.Route,
                        material = GlassMaterial.PEARL,
                        testTag = "home_open_itinerary_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                    GlassButton(
                        text = "Budget Tracker",
                        onClick = onOpenBudget,
                        isPrimary = false,
                        leadingIcon = Icons.Default.AccountBalanceWallet,
                        material = GlassMaterial.CLEAR,
                        testTag = "home_budget_tracker_btn",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassButton(
                        text = "Open Itinerary",
                        onClick = onOpenItinerary,
                        isPrimary = true,
                        leadingIcon = Icons.Default.Route,
                        material = GlassMaterial.PEARL,
                        testTag = "home_open_itinerary_btn",
                        modifier = Modifier.weight(1.15f)
                    )
                    GlassButton(
                        text = "Budget",
                        onClick = onOpenBudget,
                        isPrimary = false,
                        leadingIcon = Icons.Default.AccountBalanceWallet,
                        material = GlassMaterial.CLEAR,
                        testTag = "home_budget_tracker_btn",
                        modifier = Modifier.weight(0.85f)
                    )
                }
            }
        }
    }
}

@Composable
private fun JourneyDetailTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    val glass = LocalAlloraGlassColors.current
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(resolveGlassSurfaceBrush(GlassMaterial.DEEP, glass))
            .border(0.7.dp, resolveGlassBorderBrush(GlassMaterial.DEEP, glass), RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = SoftCyan,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(color = glass.textMuted, fontSize = 10.sp),
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium.copy(
                    color = glass.textPrimary,
                    fontWeight = FontWeight.SemiBold
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun QuickModuleTile(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    val glass = LocalAlloraGlassColors.current
    val shape = remember { RoundedCornerShape(22.dp) }
    Column(
        modifier = Modifier
            .width(94.dp)
            .clip(shape)
            .background(resolveGlassSurfaceBrush(GlassMaterial.FROSTED, glass), shape)
            .border(1.dp, resolveGlassBorderBrush(GlassMaterial.FROSTED, glass), shape)
            .clickable(onClick = onClick)
            .padding(vertical = 13.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(ElectricBlue.copy(alpha = 0.28f), SoftCyan.copy(alpha = 0.24f))))
                .border(0.9.dp, SoftCyan.copy(alpha = 0.65f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = SoftCyan, modifier = Modifier.size(19.dp))
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(color = glass.textPrimary, fontWeight = FontWeight.Medium),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
