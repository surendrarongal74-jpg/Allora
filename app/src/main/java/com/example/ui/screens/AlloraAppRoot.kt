package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.core.attemptAutoSignIn
import com.example.core.authStateFlow
import com.example.data.ai.AlloraAiService
import com.example.data.local.AlloraDatabase
import com.example.data.model.ApprovalStatus
import com.example.data.remote.AlloraFirestoreRepository
import com.example.data.repository.AlloraRepository
import com.example.ui.components.AlloraAtmosphericBackground
import com.example.ui.components.AlloraGlassNavigationRail
import com.example.ui.components.AlloraTab
import com.example.ui.components.AtmosphereMood
import com.example.ui.components.FloatingNavigation
import com.example.ui.theme.AlloraTheme
import com.example.ui.viewmodel.AlloraViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun AlloraAppRoot() {
    val auth = remember { Firebase.auth }
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    var pendingOnboardingDraft by remember { mutableStateOf<OnboardingPreferencesDraft?>(null) }

    val context = LocalContext.current
    val user = currentUser
    LaunchedEffect(user == null) {
        if (user == null) {
            // Allow initial WMS SurfaceSyncGroup & RenderThread window transactions to settle first
            withFrameNanos { }
            withFrameNanos { }
            delay(250L)
            val credentialManager = CredentialManager.create(context)
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = this
            )
        }
    }

    if (user == null) {
        AlloraTheme(darkTheme = true) {
            UnauthenticatedFlowScreen(
                onAuthenticated = { draft ->
                    pendingOnboardingDraft = draft
                }
            )
        }
    } else {
        val uid = user.uid
        key(uid) {
            val viewModel: AlloraViewModel = viewModel(
                key = uid,
                factory = viewModelFactory {
                    initializer {
                        val app = checkNotNull(this[APPLICATION_KEY]) {
                            "APPLICATION_KEY missing from CreationExtras"
                        }
                        val databaseId = app.getString(R.string.firestore_database_id)
                        val db = FirebaseFirestore.getInstance(databaseId)
                        AlloraViewModel(
                            localRepo = AlloraRepository(),
                            database = AlloraDatabase.getInstance(app),
                            firestoreRepo = AlloraFirestoreRepository(db, auth),
                            aiService = AlloraAiService(),
                            currentUserId = uid,
                            initialDisplayName = user.displayName ?: "Traveler",
                            initialEmail = user.email.orEmpty()
                        )
                    }
                }
            )

            LaunchedEffect(pendingOnboardingDraft) {
                val draft = pendingOnboardingDraft
                if (draft != null) {
                    val current = viewModel.userProfile.value
                    viewModel.updateUserProfile(
                        current.copy(
                            travelStyle = draft.travelStyle,
                            budgetTier = draft.budgetTier,
                            homeAirportIata = draft.homeAirport.ifBlank { "SFO" }
                        )
                    )
                    pendingOnboardingDraft = null
                }
            }

            AlloraAuthenticatedShell(viewModel = viewModel)
        }
    }
}

@Composable
private fun AlloraAuthenticatedShell(
    viewModel: AlloraViewModel
) {
    val darkGlass by viewModel.darkGlassTheme.collectAsState()
    val reducedMotion by viewModel.reducedMotion.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val approvals by viewModel.approvalRequests.collectAsState()
    val pendingApprovalsCount = approvals.count { it.status == ApprovalStatus.PENDING }

    var selectedTab by remember { mutableStateOf(AlloraTab.HOME) }
    var subDestination by remember { mutableStateOf(SubDestination.NONE) }

    BackHandler(enabled = subDestination != SubDestination.NONE) {
        subDestination = SubDestination.NONE
    }

    val mood = when {
        subDestination == SubDestination.AI_ASSISTANT -> AtmosphereMood.ASSISTANT
        subDestination == SubDestination.FLIGHTS -> AtmosphereMood.FLIGHTS
        subDestination == SubDestination.HOTELS -> AtmosphereMood.HOTELS
        subDestination == SubDestination.DINING -> AtmosphereMood.DINING
        subDestination == SubDestination.ACTIVITIES || subDestination == SubDestination.CARS -> AtmosphereMood.DISCOVER
        subDestination == SubDestination.MAP -> AtmosphereMood.MAP
        subDestination == SubDestination.APPROVALS -> AtmosphereMood.APPROVALS
        selectedTab == AlloraTab.DISCOVER -> AtmosphereMood.DISCOVER
        selectedTab == AlloraTab.TRIPS -> AtmosphereMood.TRIPS
        selectedTab == AlloraTab.BOOKINGS -> AtmosphereMood.APPROVALS
        else -> AtmosphereMood.HOME
    }

    val hideBottomCapsule = subDestination == SubDestination.AI_ASSISTANT ||
        subDestination == SubDestination.MAP ||
        subDestination == SubDestination.TRIP_WIZARD

    AlloraTheme(darkTheme = darkGlass) {
        AlloraAtmosphericBackground(mood = mood) {
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing)
            ) {
                val isExpandedWidth = maxWidth >= 720.dp

                Row(modifier = Modifier.fillMaxSize()) {
                    if (isExpandedWidth) {
                        AlloraGlassNavigationRail(
                            selectedTab = selectedTab,
                            pendingApprovalsCount = pendingApprovalsCount,
                            orbState = orbState,
                            onSelectTab = { tab ->
                                selectedTab = tab
                                subDestination = SubDestination.NONE
                            },
                            onOrbClick = { subDestination = SubDestination.AI_ASSISTANT }
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxSize(),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .widthIn(max = if (subDestination == SubDestination.MAP) 1600.dp else 860.dp)
                        ) {
                            when (subDestination) {
                                SubDestination.AI_ASSISTANT -> AiAssistantScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onNavigateSub = { subDestination = it }
                                )

                                SubDestination.NOTIFICATIONS -> NotificationsCenterScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onNavigateSub = { subDestination = it }
                                )

                                SubDestination.SAVED_ITEMS -> SavedItemsScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE }
                                )

                                SubDestination.MAP -> FullScreenMapScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onNavigateSub = { subDestination = it }
                                )

                                SubDestination.FLIGHTS -> FlightSearchScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onHoldSubmitted = { subDestination = SubDestination.APPROVALS }
                                )

                                SubDestination.HOTELS -> HotelSearchScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onHoldSubmitted = { subDestination = SubDestination.APPROVALS }
                                )

                                SubDestination.DINING -> RestaurantDiningScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onHoldSubmitted = { subDestination = SubDestination.APPROVALS }
                                )

                                SubDestination.ACTIVITIES -> ActivitiesExcursionsScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onHoldSubmitted = { subDestination = SubDestination.APPROVALS },
                                    onNavigateSub = { subDestination = it }
                                )

                                SubDestination.CARS -> CarRentalSearchScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onHoldSubmitted = { subDestination = SubDestination.APPROVALS }
                                )

                                SubDestination.TRIP_WIZARD -> TravelPlanningWizardScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onFinishToItinerary = { subDestination = SubDestination.ITINERARY }
                                )

                                SubDestination.ITINERARY -> ItineraryTimelineScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE },
                                    onNavigateSub = { subDestination = it }
                                )

                                SubDestination.BUDGET -> TravelBudgetScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE }
                                )

                                SubDestination.APPROVALS -> ApprovalCenterScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE }
                                )

                                SubDestination.AUDIT_HUB -> Phase0AuditHubScreen(
                                    viewModel = viewModel,
                                    onBack = { subDestination = SubDestination.NONE }
                                )

                                SubDestination.NONE -> {
                                    when (selectedTab) {
                                        AlloraTab.HOME -> HomeScreen(
                                            viewModel = viewModel,
                                            onNavigateSub = { subDestination = it },
                                            onOpenAssistantWithPrompt = { prompt ->
                                                viewModel.sendAiPrompt(prompt)
                                                subDestination = SubDestination.AI_ASSISTANT
                                            }
                                        )

                                        AlloraTab.DISCOVER -> DiscoverScreen(
                                            viewModel = viewModel,
                                            onNavigateSub = { subDestination = it }
                                        )

                                        AlloraTab.TRIPS -> TripsHubScreen(
                                            viewModel = viewModel,
                                            onNavigateSub = { subDestination = it }
                                        )

                                        AlloraTab.BOOKINGS -> BookingsHubScreen(
                                            viewModel = viewModel,
                                            onNavigateSub = { subDestination = it }
                                        )

                                        AlloraTab.PROFILE -> ProfileSettingsScreen(
                                            viewModel = viewModel,
                                            onNavigateSub = { subDestination = it }
                                        )
                                    }
                                }
                            }
                        }

                        if (!isExpandedWidth && !hideBottomCapsule) {
                            FloatingNavigation(
                                selectedTab = selectedTab,
                                pendingApprovalsCount = pendingApprovalsCount,
                                orbState = orbState,
                                reducedMotion = reducedMotion,
                                onSelectTab = { tab ->
                                    selectedTab = tab
                                    subDestination = SubDestination.NONE
                                },
                                onOrbClick = { subDestination = SubDestination.AI_ASSISTANT },
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                        }
                    }
                }
            }
        }
    }
}
