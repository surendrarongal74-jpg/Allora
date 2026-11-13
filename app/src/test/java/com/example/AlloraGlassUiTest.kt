package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.getBoundsInRoot
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.model.ApprovalRequest
import com.example.data.model.ApprovalStatus
import com.example.data.model.LedgerTransaction
import com.example.data.model.WeatherReport
import com.example.data.remote.UserProfileDoc
import com.example.ui.components.AlloraAiOrb
import com.example.ui.components.AlloraAtmosphericBackground
import com.example.ui.components.AlloraGlassNavigationRail
import com.example.ui.components.AlloraOrbSpringDriver
import com.example.ui.components.AlloraTab
import com.example.ui.components.AtmosphereMood
import com.example.ui.components.FloatingNavigation
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.OrbState
import com.example.ui.components.rememberAlloraOrbSpringDriver
import com.example.ui.components.toMotionProfile
import com.example.ui.screens.HomeScreenContent
import com.example.ui.screens.UnauthenticatedFlowScreen
import com.example.ui.theme.AlloraTheme
import com.example.ui.theme.GlassMaterial
import com.example.ui.theme.LocalAlloraGlassColors
import com.example.ui.viewmodel.DestinationSpotlight
import com.example.ui.viewmodel.TripWizardState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlloraGlassUiTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun sampleProfile() = UserProfileDoc(
        id = "user-1",
        userId = "user-1",
        displayName = "Martin Butler",
        temperatureUnit = "METRIC",
        homeAirportIata = "SFO"
    )

    private fun sampleWizard() = TripWizardState(
        destination = "Tokyo, Japan",
        startDate = "Dec 12, 2026",
        endDate = "Dec 17, 2026",
        travellersCount = 2,
        style = "Luxury & Culture",
        budgetUsd = 4200.0,
        generatedTitle = "Winter Tokyo Glass & Culinary Circuit"
    )

    private fun sampleDestinations() = listOf(
        DestinationSpotlight(
            id = "dest-1",
            city = "Tokyo",
            country = "Japan",
            tagline = "Neon skyline, Shibuya & winter culinary trails",
            highlights = "Ginza • Meiji",
            rating = 4.9,
            paletteSeed = 0,
            category = "City",
            estimatedFlightUsd = 740.0,
            estimatedHotelNightUsd = 310.0
        ),
        DestinationSpotlight(
            id = "dest-2",
            city = "Reykjavik",
            country = "Iceland",
            tagline = "Glacier lagoons & midnight northern lights",
            highlights = "Blue Lagoon",
            rating = 4.8,
            paletteSeed = 1,
            category = "Adventure",
            estimatedFlightUsd = 600.0,
            estimatedHotelNightUsd = 260.0
        )
    )

    private fun sampleWeather() = mapOf(
        "Tokyo" to WeatherReport("Tokyo", 14.0, "Clear", 48, 9.0, 2),
        "Reykjavik" to WeatherReport("Reykjavik", 3.0, "Aurora", 62, 16.0, 1)
    )

    private fun sampleApprovals() = listOf(
        ApprovalRequest(
            id = "apr-1",
            title = "ANA Business Hold — SFO to HND",
            category = "Flight",
            merchantOrProvider = "All Nippon Airways",
            amountUsd = 1480.0,
            riskLevel = "Low Risk",
            status = ApprovalStatus.PENDING,
            details = "Refundable 24h fare hold",
            providerPolicy = "24h Free Cancellation",
            expiresInMinutes = 120
        )
    )

    private fun sampleLedger() = listOf(
        LedgerTransaction(
            id = "ldg-1",
            referenceCode = "ALR-9042",
            title = "Park Hyatt Tokyo — 5 Nights",
            category = "Hotel",
            amountUsd = 1850.0,
            providerPnr = "PHT-88412",
            timestamp = "Dec 12, 2026",
            status = "CONFIRMED",
            sha256Hash = "8f4e2a"
        )
    )

    private fun renderHomeViewport(widthDp: Dp, heightDp: Dp = 800.dp) {
        composeRule.setContent {
            AlloraTheme(darkTheme = true) {
                Box(
                    modifier = Modifier
                        .width(widthDp)
                        .height(heightDp)
                        .testTag("viewport_root")
                ) {
                    AlloraAtmosphericBackground(mood = AtmosphereMood.HOME) {
                        if (widthDp >= 720.dp) {
                            Row(modifier = Modifier.fillMaxSize()) {
                                AlloraGlassNavigationRail(
                                    selectedTab = AlloraTab.HOME,
                                    pendingApprovalsCount = 1,
                                    orbState = OrbState.IDLE,
                                    onSelectTab = {},
                                    onOrbClick = {},
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .testTag("tablet_nav_rail")
                                )
                                HomeScreenContent(
                                    profile = sampleProfile(),
                                    orbState = OrbState.IDLE,
                                    reducedMotion = true,
                                    recentSearches = listOf("Haneda fast-track lounge", "Kyoto ryokan"),
                                    approvals = sampleApprovals(),
                                    ledger = sampleLedger(),
                                    unreadNotifs = 2,
                                    savedCount = 3,
                                    weatherMap = sampleWeather(),
                                    wizard = sampleWizard(),
                                    destinations = sampleDestinations(),
                                    isDestinationSaved = { it == "Tokyo" },
                                    onToggleDestinationSave = {},
                                    onNavigateSub = {},
                                    onOpenAssistantWithPrompt = {},
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        } else {
                            Box(modifier = Modifier.fillMaxSize()) {
                                HomeScreenContent(
                                    profile = sampleProfile(),
                                    orbState = OrbState.IDLE,
                                    reducedMotion = true,
                                    recentSearches = listOf("Haneda fast-track lounge", "Kyoto ryokan"),
                                    approvals = sampleApprovals(),
                                    ledger = sampleLedger(),
                                    unreadNotifs = 2,
                                    savedCount = 3,
                                    weatherMap = sampleWeather(),
                                    wizard = sampleWizard(),
                                    destinations = sampleDestinations(),
                                    isDestinationSaved = { it == "Tokyo" },
                                    onToggleDestinationSave = {},
                                    onNavigateSub = {},
                                    onOpenAssistantWithPrompt = {}
                                )
                                FloatingNavigation(
                                    selectedTab = AlloraTab.HOME,
                                    pendingApprovalsCount = 1,
                                    orbState = OrbState.IDLE,
                                    reducedMotion = true,
                                    onSelectTab = {},
                                    onOrbClick = {},
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                )
                            }
                        }
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    private fun assertHomeResponsiveLayout(isTablet: Boolean) {
        val rootBounds = composeRule.onNodeWithTag("viewport_root").getBoundsInRoot()
        val topBarBounds = composeRule.onNodeWithTag("home_top_bar").getBoundsInRoot()
        val orbBounds = composeRule.onNodeWithTag("home_hero_ai_orb").getBoundsInRoot()
        val searchBounds = composeRule.onNodeWithTag("home_search_bar").getBoundsInRoot()
        val phase0Bounds = composeRule.onNodeWithTag("home_phase0_chip").getBoundsInRoot()
        val notifBounds = composeRule.onNodeWithTag("home_notifications_button").getBoundsInRoot()

        assertTrue("Top bar exceeds viewport width", topBarBounds.right <= rootBounds.right + 1.dp)
        assertTrue("Hero orb exceeds viewport width", orbBounds.right <= rootBounds.right + 1.dp)
        assertTrue("Search bar exceeds viewport width", searchBounds.right <= rootBounds.right + 1.dp)
        assertTrue("Phase 0 chip overlaps notifications button", phase0Bounds.right <= notifBounds.left + 0.5.dp)
        assertTrue("Orb should sit above search bar", orbBounds.bottom <= searchBounds.top)

        if (isTablet) {
            composeRule.onNodeWithTag("tablet_nav_rail").assertIsDisplayed()
        } else {
            val navBounds = composeRule.onNodeWithTag("allora_floating_navigation").getBoundsInRoot()
            val orbPillBounds = composeRule.onNodeWithTag("floating_assistant_orb_pill").getBoundsInRoot()
            assertTrue("Floating nav capsule overflows right edge", navBounds.right <= rootBounds.right)
            assertTrue("Floating orb pill overflows right edge", orbPillBounds.right <= rootBounds.right)
            assertTrue("Floating nav overlaps orb pill", navBounds.right <= orbPillBounds.left + 0.5.dp)

            val homeTab = composeRule.onNodeWithTag("nav_tab_home").getBoundsInRoot()
            val discoverTab = composeRule.onNodeWithTag("nav_tab_discover").getBoundsInRoot()
            val tripsTab = composeRule.onNodeWithTag("nav_tab_trips").getBoundsInRoot()
            val bookingsTab = composeRule.onNodeWithTag("nav_tab_bookings").getBoundsInRoot()
            val profileTab = composeRule.onNodeWithTag("nav_tab_profile").getBoundsInRoot()
            assertTrue("Home tab overlaps Discover tab", homeTab.right <= discoverTab.left + 0.5.dp)
            assertTrue("Discover tab overlaps Trips tab", discoverTab.right <= tripsTab.left + 0.5.dp)
            assertTrue("Trips tab overlaps Bookings tab", tripsTab.right <= bookingsTab.left + 0.5.dp)
            assertTrue("Bookings tab overlaps Profile tab", bookingsTab.right <= profileTab.left + 0.5.dp)
        }

        // Scroll to Upcoming Journey card and assert clear CTAs and bottom clearance
        composeRule.onNodeWithTag("home_scroll_list")
            .performScrollToNode(hasTestTag("home_upcoming_journey_card"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_upcoming_journey_card").assertIsDisplayed()
        composeRule.onNodeWithTag("home_open_itinerary_btn").assertIsDisplayed()
        composeRule.onNodeWithTag("home_budget_tracker_btn").assertIsDisplayed()

        // Scroll to Destinations carousel and assert visibility
        composeRule.onNodeWithTag("home_scroll_list")
            .performScrollToNode(hasTestTag("home_destinations_section"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_destinations_section").assertIsDisplayed()
        composeRule.onNodeWithText("Tokyo").assertIsDisplayed()
    }

    @Test
    fun `home layout renders cleanly without overlap at 320dp ultra compact width`() {
        renderHomeViewport(widthDp = 320.dp, heightDp = 640.dp)
        assertHomeResponsiveLayout(isTablet = false)
    }

    @Test
    fun `home layout renders cleanly without overlap at 360dp compact phone width`() {
        renderHomeViewport(widthDp = 360.dp, heightDp = 760.dp)
        assertHomeResponsiveLayout(isTablet = false)
    }

    @Test
    fun `home layout renders cleanly without overlap at 412dp standard phone width`() {
        renderHomeViewport(widthDp = 412.dp, heightDp = 892.dp)
        assertHomeResponsiveLayout(isTablet = false)
    }

    @Test
    fun `home layout switches cleanly to rail at 840dp tablet width`() {
        renderHomeViewport(widthDp = 840.dp, heightDp = 1080.dp)
        assertHomeResponsiveLayout(isTablet = true)
    }

    @Test
    fun `onboarding renders ALLORA tagline and skips to Google Sign In screen`() {
        composeRule.setContent {
            AlloraTheme(darkTheme = true) {
                UnauthenticatedFlowScreen(onAuthenticated = {})
            }
        }

        composeRule.onNodeWithText("ALLORA").assertIsDisplayed()
        composeRule.onNodeWithText("Your life, effortlessly connected.").assertIsDisplayed()

        composeRule.onNodeWithTag("onboarding_skip_button").performClick()
        composeRule.onNodeWithTag("google_sign_in_button").assertIsDisplayed()
    }

    @Test
    fun `floating navigation switches tabs and triggers AI orb click`() {
        var selected = AlloraTab.HOME
        var orbTapped = false

        composeRule.setContent {
            AlloraTheme(darkTheme = true) {
                FloatingNavigation(
                    selectedTab = selected,
                    pendingApprovalsCount = 2,
                    orbState = OrbState.IDLE,
                    reducedMotion = true,
                    onSelectTab = { selected = it },
                    onOrbClick = { orbTapped = true }
                )
            }
        }

        composeRule.onNodeWithTag("nav_tab_discover").performClick()
        assertEquals(AlloraTab.DISCOVER, selected)

        composeRule.onNodeWithTag("floating_assistant_orb_pill").performClick()
        assertTrue(orbTapped)
    }

    @Test
    fun `allora theme provides dark and light glass color palettes`() {
        var darkFlag = false
        var lightFlag = true

        composeRule.setContent {
            AlloraTheme(darkTheme = true) {
                darkFlag = LocalAlloraGlassColors.current.isDark
            }
            AlloraTheme(darkTheme = false) {
                lightFlag = LocalAlloraGlassColors.current.isDark
            }
        }

        assertTrue(darkFlag)
        assertFalse(lightFlag)
    }

    @Test
    fun `glass material tiers render clear card and pearl primary cta`() {
        var clicked = false
        composeRule.setContent {
            AlloraTheme(darkTheme = true) {
                GlassCard(material = GlassMaterial.CLEAR) {
                    GlassButton(
                        text = "Book Now",
                        onClick = { clicked = true },
                        isPrimary = true,
                        material = GlassMaterial.PEARL,
                        testTag = "pearl_cta_test"
                    )
                }
            }
        }

        composeRule.onNodeWithTag("pearl_cta_test").performClick()
        assertTrue(clicked)
    }

    @Test
    fun `orb spring driver transitions fluidly across idle listening and processing states`() {
        val idleProfile = OrbState.IDLE.toMotionProfile()
        val listeningProfile = OrbState.LISTENING.toMotionProfile()
        val processingProfile = OrbState.PROCESSING.toMotionProfile()
        assertTrue(processingProfile.spinDegreesPerSec > listeningProfile.spinDegreesPerSec)
        assertTrue(listeningProfile.spinDegreesPerSec > idleProfile.spinDegreesPerSec)
        assertTrue(listeningProfile.ringExpansion > processingProfile.ringExpansion)

        composeRule.mainClock.autoAdvance = false
        var activeState by mutableStateOf(OrbState.IDLE)
        var capturedDriver: AlloraOrbSpringDriver? = null

        composeRule.setContent {
            AlloraTheme(darkTheme = true) {
                capturedDriver = rememberAlloraOrbSpringDriver(
                    state = activeState,
                    reducedMotion = false,
                    isPressed = false
                )
            }
        }

        val driver = capturedDriver!!
        repeat(15) { composeRule.mainClock.advanceTimeByFrame() }
        val idleVelocity = driver.spinVelocityAnim.value
        assertEquals(idleProfile.spinDegreesPerSec, idleVelocity, 2f)

        // Transition IDLE -> LISTENING
        activeState = OrbState.LISTENING
        composeRule.runOnIdle { }
        repeat(35) { composeRule.mainClock.advanceTimeByFrame() }
        val listeningVelocity = driver.spinVelocityAnim.value
        assertTrue("Expected spin velocity to accelerate smoothly toward LISTENING ($listeningVelocity)", listeningVelocity > 50f)
        assertTrue("Expected acoustic ring expansion during LISTENING", driver.ringExpansionAnim.value > 0.65f)

        // Transition LISTENING -> PROCESSING
        activeState = OrbState.PROCESSING
        composeRule.runOnIdle { }
        repeat(45) { composeRule.mainClock.advanceTimeByFrame() }
        val processingVelocity = driver.spinVelocityAnim.value
        assertTrue("Expected high orbital velocity during PROCESSING ($processingVelocity)", processingVelocity > 130f)

        // Transition PROCESSING -> IDLE smoothly decelerates
        activeState = OrbState.IDLE
        composeRule.runOnIdle { }
        repeat(60) { composeRule.mainClock.advanceTimeByFrame() }
        assertTrue("Expected velocity to decelerate cleanly toward IDLE", driver.spinVelocityAnim.value < processingVelocity)
    }

    @Test
    fun `allora ai orb composable steps smoothly across idle listening processing and reduced motion`() {
        composeRule.mainClock.autoAdvance = false
        var orbState by mutableStateOf(OrbState.IDLE)
        var reducedMotion by mutableStateOf(false)

        composeRule.setContent {
            AlloraTheme(darkTheme = true) {
                AlloraAiOrb(
                    state = orbState,
                    size = 148.dp,
                    reducedMotion = reducedMotion,
                    modifier = Modifier.testTag("spring_orb_test")
                )
            }
        }

        composeRule.onNodeWithTag("spring_orb_test").assertIsDisplayed()
        repeat(10) { composeRule.mainClock.advanceTimeByFrame() }

        orbState = OrbState.LISTENING
        repeat(10) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("spring_orb_test").assertIsDisplayed()

        orbState = OrbState.PROCESSING
        repeat(10) { composeRule.mainClock.advanceTimeByFrame() }
        composeRule.onNodeWithTag("spring_orb_test").assertIsDisplayed()

        reducedMotion = true
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("spring_orb_test").assertIsDisplayed()
    }
}
