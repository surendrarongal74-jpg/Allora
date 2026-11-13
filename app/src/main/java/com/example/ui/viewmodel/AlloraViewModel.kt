package com.example.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AlloraAiService
import com.example.data.local.AlloraDatabase
import com.example.data.local.entity.BookingRecord
import com.example.data.local.entity.UserPreference
import com.example.data.model.ApprovalRequest
import com.example.data.model.ApprovalStatus
import com.example.data.model.FlightOffer
import com.example.data.model.ItinerarySlot
import com.example.data.model.LedgerTransaction
import com.example.data.model.LodgingOffer
import com.example.data.model.PointOfInterest
import com.example.data.model.RepoAuditItem
import com.example.data.model.WeatherReport
import com.example.data.remote.AlloraFirestoreRepository
import com.example.data.remote.BookingDoc
import com.example.data.remote.SavedItemDoc
import com.example.data.remote.TripPlanDoc
import com.example.data.remote.UserProfileDoc
import com.example.data.repository.AlloraRepository
import com.example.ui.components.OrbState
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class DestinationSpotlight(
    val id: String,
    val city: String,
    val country: String,
    val tagline: String,
    val highlights: String,
    val rating: Double,
    val paletteSeed: Int,
    val category: String,
    val estimatedFlightUsd: Double,
    val estimatedHotelNightUsd: Double
)

data class CarRentalOffer(
    val id: String,
    val modelName: String,
    val vehicleClass: String,
    val seats: Int,
    val transmission: String,
    val dailyRateUsd: Double,
    val insurancePerDayUsd: Double,
    val pickupHub: String,
    val providerSource: String,
    val policySummary: String
)

data class AlloraNotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: String,
    val category: String,
    val isUnread: Boolean = true
)

data class AiChatMessage(
    val id: String,
    val isUser: Boolean,
    val text: String,
    val timestamp: String,
    val isError: Boolean = false,
    val isStreaming: Boolean = false,
    val followUpChips: List<String> = emptyList()
)

data class TripWizardState(
    val currentStep: Int = 1,
    val destination: String = "Tokyo, Japan",
    val startDate: String = "Dec 12, 2026",
    val endDate: String = "Dec 17, 2026",
    val travellersCount: Int = 2,
    val style: String = "Luxury & Culture",
    val budgetUsd: Double = 4200.0,
    val transport: String = "Business Flight + Rail",
    val lodgingType: String = "Boutique Luxury Hotel",
    val activities: List<String> = listOf("Fine Dining", "Temples & Art", "Skyline Views"),
    val generatedTitle: String = "",
    val generatedSummary: String = "",
    val isGenerating: Boolean = false,
    val generationError: String? = null
)

data class ItineraryMeta(
    val estimatedCostUsd: Double = 0.0,
    val note: String = ""
)

data class BudgetCategoryBreakdown(
    val label: String,
    val estimatedUsd: Double,
    val actualUsd: Double
)

class AlloraViewModel(
    private val localRepo: AlloraRepository,
    private val database: AlloraDatabase,
    private val firestoreRepo: AlloraFirestoreRepository,
    private val aiService: AlloraAiService,
    val currentUserId: String,
    initialDisplayName: String = "Traveler",
    initialEmail: String = ""
) : ViewModel() {

    val auditItems: StateFlow<List<RepoAuditItem>> = localRepo.auditItems
    val flightOffers: StateFlow<List<FlightOffer>> = localRepo.flightOffers
    val lodgingOffers: StateFlow<List<LodgingOffer>> = localRepo.lodgingOffers
    val poiList: StateFlow<List<PointOfInterest>> = localRepo.poiList
    val weatherReports: StateFlow<Map<String, WeatherReport>> = localRepo.weatherReports
    val approvalRequests: StateFlow<List<ApprovalRequest>> = localRepo.approvalRequests
    val ledgerTransactions: StateFlow<List<LedgerTransaction>> = localRepo.ledgerTransactions

    private val _itinerarySlots = MutableStateFlow(localRepo.itinerarySlots.value)
    val itinerarySlots: StateFlow<List<ItinerarySlot>> = _itinerarySlots.asStateFlow()

    private val _itineraryMeta = MutableStateFlow<Map<String, ItineraryMeta>>(
        mapOf(
            "it-1" to ItineraryMeta(720.0, "Priority lane voucher ready"),
            "it-2" to ItineraryMeta(110.0, "Flight arrival tracked via HND"),
            "it-3" to ItineraryMeta(260.0, "Counter seat hold pending approval"),
            "it-4" to ItineraryMeta(45.0, "Morning light recommended"),
            "it-5" to ItineraryMeta(185.0, "33F Sunset window requested")
        )
    )
    val itineraryMeta: StateFlow<Map<String, ItineraryMeta>> = _itineraryMeta.asStateFlow()

    val cachedBookings: StateFlow<List<BookingRecord>> = database.bookingRecordDao()
        .observeAllBookings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), emptyList())

    private val _remoteBookings = MutableStateFlow<List<BookingDoc>>(emptyList())
    val remoteBookings: StateFlow<List<BookingDoc>> = _remoteBookings.asStateFlow()

    private val _remoteTrips = MutableStateFlow<List<TripPlanDoc>>(emptyList())
    val remoteTrips: StateFlow<List<TripPlanDoc>> = _remoteTrips.asStateFlow()

    private val _savedItems = MutableStateFlow<List<SavedItemDoc>>(emptyList())
    val savedItems: StateFlow<List<SavedItemDoc>> = _savedItems.asStateFlow()

    private val _userProfile = MutableStateFlow(
        UserProfileDoc(
            userId = currentUserId,
            displayName = initialDisplayName.ifBlank { "Traveler" }
        )
    )
    val userProfile: StateFlow<UserProfileDoc> = _userProfile.asStateFlow()

    private val _userEmail = MutableStateFlow(initialEmail)
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _darkGlassTheme = MutableStateFlow(true)
    val darkGlassTheme: StateFlow<Boolean> = _darkGlassTheme.asStateFlow()

    private val _reducedMotion = MutableStateFlow(false)
    val reducedMotion: StateFlow<Boolean> = _reducedMotion.asStateFlow()

    private val _recentSearches = MutableStateFlow(
        listOf("Tokyo 5-day calm luxury", "SIN to LHR Business", "Aman Tokyo suites")
    )
    val recentSearches: StateFlow<List<String>> = _recentSearches.asStateFlow()

    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _aiStatusLabel = MutableStateFlow("Ready • Gemini 3.8 Flash")
    val aiStatusLabel: StateFlow<String> = _aiStatusLabel.asStateFlow()

    private val _chatMessages = MutableStateFlow(
        listOf(
            AiChatMessage(
                id = "msg-welcome",
                isUser = false,
                text = "Welcome to ALLORA. I can synthesize multi-city itineraries, inspect flight or stay trade-offs, and prepare two-phase booking holds for your explicit approval.",
                timestamp = "Now",
                followUpChips = listOf(
                    "Plan 4 days in Tokyo",
                    "Compare SIN ➔ LHR flights",
                    "Check my pending approvals"
                )
            )
        )
    )
    val chatMessages: StateFlow<List<AiChatMessage>> = _chatMessages.asStateFlow()

    private val _wizardState = MutableStateFlow(TripWizardState())
    val wizardState: StateFlow<TripWizardState> = _wizardState.asStateFlow()

    private val _notifications = MutableStateFlow(
        listOf(
            AlloraNotificationItem(
                id = "notif-1",
                title = "Approval Required • SQ 318",
                message = "Singapore Airlines Business hold ($840) expires in 48 minutes. Review policy in Shield.",
                timestamp = "4m ago",
                category = "Approval"
            ),
            AlloraNotificationItem(
                id = "notif-2",
                title = "Route Optimized via VROOM (#98)",
                message = "Day 1 Tokyo transit order updated to avoid Shibuya peak congestion.",
                timestamp = "1h ago",
                category = "Itinerary"
            ),
            AlloraNotificationItem(
                id = "notif-3",
                title = "Tokyo Weather Clear",
                message = "19°C clear sky forecast across your planned arrival window.",
                timestamp = "3h ago",
                category = "Weather"
            )
        )
    )
    val notifications: StateFlow<List<AlloraNotificationItem>> = _notifications.asStateFlow()

    private val _cancelledRefs = MutableStateFlow<Set<String>>(emptySet())
    val cancelledRefs: StateFlow<Set<String>> = _cancelledRefs.asStateFlow()

    private val _cloudSyncError = MutableStateFlow<String?>(null)
    val cloudSyncError: StateFlow<String?> = _cloudSyncError.asStateFlow()

    val destinations: List<DestinationSpotlight> = listOf(
        DestinationSpotlight(
            id = "dest-tokyo",
            city = "Tokyo",
            country = "Japan",
            tagline = "Serene temples, precision transit & sky-high culinary craft",
            highlights = "Otemachi • Ginza • Shibuya • Meiji Jingu",
            rating = 4.9,
            paletteSeed = 0,
            category = "Culture",
            estimatedFlightUsd = 720.0,
            estimatedHotelNightUsd = 620.0
        ),
        DestinationSpotlight(
            id = "dest-paris",
            city = "Paris",
            country = "France",
            tagline = "Architectural elegance, ateliers & Seine twilight salons",
            highlights = "Avenue Montaigne • Louvre • Le Marais",
            rating = 4.8,
            paletteSeed = 1,
            category = "Luxury",
            estimatedFlightUsd = 610.0,
            estimatedHotelNightUsd = 780.0
        ),
        DestinationSpotlight(
            id = "dest-singapore",
            city = "Singapore",
            country = "Singapore",
            tagline = "Biophilic skyline gardens, Marina waterfront & effortless hub",
            highlights = "Marina Bay • Gardens by the Bay • Sentosa",
            rating = 4.8,
            paletteSeed = 2,
            category = "Nature",
            estimatedFlightUsd = 840.0,
            estimatedHotelNightUsd = 450.0
        ),
        DestinationSpotlight(
            id = "dest-london",
            city = "London",
            country = "United Kingdom",
            tagline = "Historic townhouses, West End theatre & Mayfair galleries",
            highlights = "Mayfair • South Bank • Kensington",
            rating = 4.7,
            paletteSeed = 3,
            category = "City",
            estimatedFlightUsd = 580.0,
            estimatedHotelNightUsd = 510.0
        )
    )

    val carRentals: List<CarRentalOffer> = listOf(
        CarRentalOffer(
            id = "car-01",
            modelName = "Porsche Taycan Cross Turismo",
            vehicleClass = "Electric Grand Tourer",
            seats = 4,
            transmission = "Automatic • EV",
            dailyRateUsd = 215.0,
            insurancePerDayUsd = 35.0,
            pickupHub = "Tokyo Haneda VIP Terminal",
            providerSource = "Repo #45 Engine • Sample Estimate",
            policySummary = "Unlimited mileage • Free cancellation 24h prior • Requires driver verification"
        ),
        CarRentalOffer(
            id = "car-02",
            modelName = "BMW i5 eDrive40 Touring",
            vehicleClass = "Executive Sedan",
            seats = 5,
            transmission = "Automatic • EV",
            dailyRateUsd = 148.0,
            insurancePerDayUsd = 26.0,
            pickupHub = "Tokyo Station / Marunouchi",
            providerSource = "Amadeus Car Rental Ready",
            policySummary = "Full-to-full energy policy • Collision waiver optional • Zero deposit hold"
        ),
        CarRentalOffer(
            id = "car-03",
            modelName = "Range Rover Velar Autobiography",
            vehicleClass = "Luxury SUV",
            seats = 5,
            transmission = "Automatic • Hybrid",
            dailyRateUsd = 185.0,
            insurancePerDayUsd = 32.0,
            pickupHub = "Paris CDG Terminal 2E",
            providerSource = "Repo #45 Engine • Sample Estimate",
            policySummary = "Cross-border EU transit allowed • 24/7 roadside assistance"
        )
    )

    private var activeStreamJob: Job? = null

    init {
        startFirestoreObservers()
        loadInitialRoomPreferences()
    }

    private fun loadInitialRoomPreferences() {
        viewModelScope.launch {
            database.userPreferenceDao().observePreferences(currentUserId).collect { pref ->
                if (pref != null) {
                    _userProfile.update { current ->
                        current.copy(
                            preferredCurrency = pref.preferredCurrency,
                            temperatureUnit = pref.temperatureUnit,
                            preferredCabinClass = pref.preferredCabinClass,
                            homeAirportIata = pref.homeAirportIata,
                            biometricThresholdUsd = pref.biometricThresholdUsd
                        )
                    }
                }
            }
        }
    }

    private fun startFirestoreObservers() {
        viewModelScope.launch {
            firestoreRepo.observeUserProfile(currentUserId)
                .catch { error -> handleStreamException("profile", error) }
                .collect { remote ->
                    if (remote != null) {
                        _userProfile.value = remote
                        _darkGlassTheme.value = remote.darkGlassTheme
                        database.userPreferenceDao().upsertPreferences(
                            UserPreference(
                                userId = currentUserId,
                                preferredCurrency = remote.preferredCurrency,
                                temperatureUnit = remote.temperatureUnit,
                                preferredCabinClass = remote.preferredCabinClass,
                                homeAirportIata = remote.homeAirportIata,
                                biometricThresholdUsd = remote.biometricThresholdUsd
                            )
                        )
                    } else {
                        firestoreRepo.upsertUserProfile(_userProfile.value)
                    }
                }
        }

        viewModelScope.launch {
            firestoreRepo.observeTrips(currentUserId)
                .catch { error -> handleStreamException("trips", error) }
                .collect { trips -> _remoteTrips.value = trips }
        }

        viewModelScope.launch {
            firestoreRepo.observeBookings(currentUserId)
                .catch { error -> handleStreamException("bookings", error) }
                .collect { docs -> _remoteBookings.value = docs }
        }

        viewModelScope.launch {
            firestoreRepo.observeSavedItems(currentUserId)
                .catch { error -> handleStreamException("saved items", error) }
                .collect { items -> _savedItems.value = items }
        }
    }

    private fun handleStreamException(scopeLabel: String, error: Throwable) {
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val root = firestoreError ?: error
        Log.w(TAG, "Error observing $scopeLabel", root)
        _cloudSyncError.value = root.localizedMessage ?: "Cloud sync unavailable ($scopeLabel)"
    }

    fun sendAiPrompt(prompt: String) {
        val trimmed = prompt.trim()
        if (trimmed.isEmpty()) return

        _recentSearches.update { list -> (listOf(trimmed) + list).distinct().take(5) }
        val userMsg = AiChatMessage(
            id = "msg-u-${UUID.randomUUID().toString().take(8)}",
            isUser = true,
            text = trimmed,
            timestamp = "Now"
        )
        val assistantMsgId = "msg-a-${UUID.randomUUID().toString().take(8)}"
        val placeholderMsg = AiChatMessage(
            id = assistantMsgId,
            isUser = false,
            text = "",
            timestamp = "Streaming",
            isStreaming = true
        )

        val historyPairs = _chatMessages.value
            .filter { !it.isError && it.text.isNotBlank() }
            .map { it.isUser to it.text }

        _chatMessages.update { it + userMsg + placeholderMsg }
        _orbState.value = OrbState.PROCESSING
        _aiStatusLabel.value = "Thinking with Gemini 3.8 Flash..."

        activeStreamJob?.cancel()
        activeStreamJob = viewModelScope.launch {
            try {
                aiService.streamAssistantResponse(trimmed, historyPairs).collect { accumulated ->
                    _orbState.value = OrbState.RESPONDING
                    _aiStatusLabel.value = "Streaming live response..."
                    _chatMessages.update { list ->
                        list.map { msg ->
                            if (msg.id == assistantMsgId) {
                                msg.copy(text = accumulated, isStreaming = true)
                            } else msg
                        }
                    }
                }
                _orbState.value = OrbState.IDLE
                _aiStatusLabel.value = "Ready • Gemini 3.8 Flash"
                _chatMessages.update { list ->
                    list.map { msg ->
                        if (msg.id == assistantMsgId) {
                            msg.copy(
                                timestamp = "Now",
                                isStreaming = false,
                                followUpChips = listOf("Add to trip wizard", "Estimate budget", "Find flights")
                            )
                        } else msg
                    }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _orbState.value = OrbState.ERROR
                _aiStatusLabel.value = "AI service error"
                val friendlyError = e.localizedMessage ?: "Failed to connect to Gemini AI Logic."
                _chatMessages.update { list ->
                    list.map { msg ->
                        if (msg.id == assistantMsgId) {
                            msg.copy(
                                text = "Unable to complete AI response: $friendlyError",
                                timestamp = "Error",
                                isError = true,
                                isStreaming = false
                            )
                        } else msg
                    }
                }
            }
        }
    }

    fun clearAiConversation() {
        activeStreamJob?.cancel()
        _orbState.value = OrbState.IDLE
        _aiStatusLabel.value = "Ready • Gemini 3.8 Flash"
        _chatMessages.value = listOf(
            AiChatMessage(
                id = "msg-reset",
                isUser = false,
                text = "Started a fresh ALLORA workspace. Ask me about routes, weather, dining, or trip planning.",
                timestamp = "Now",
                followUpChips = listOf("Plan a weekend in Paris", "Find non-stop flights to Tokyo")
            )
        )
    }

    fun updateWizard(transform: (TripWizardState) -> TripWizardState) {
        _wizardState.update(transform)
    }

    fun generateWizardItinerary() {
        val state = _wizardState.value
        if (state.isGenerating) return

        _wizardState.update { it.copy(isGenerating = true, generationError = null) }
        _orbState.value = OrbState.PROCESSING
        _aiStatusLabel.value = "Synthesizing ${state.destination} itinerary..."

        viewModelScope.launch {
            val result = aiService.generateStructuredItinerary(
                destination = state.destination,
                startDate = state.startDate,
                endDate = state.endDate,
                travellers = state.travellersCount,
                style = state.style,
                budgetUsd = state.budgetUsd,
                transport = state.transport,
                lodgingType = state.lodgingType,
                activities = state.activities
            )
            result.onSuccess { generated ->
                _itinerarySlots.value = generated.slots
                _wizardState.update {
                    it.copy(
                        isGenerating = false,
                        generatedTitle = generated.title,
                        generatedSummary = generated.summary,
                        currentStep = 10
                    )
                }
                _orbState.value = OrbState.IDLE
                _aiStatusLabel.value = "Ready • Gemini 3.8 Flash"

                // Persist trip overview to Firestore
                firestoreRepo.createTrip(
                    TripPlanDoc(
                        title = generated.title,
                        destination = state.destination,
                        startDate = state.startDate,
                        endDate = state.endDate,
                        travellersCount = state.travellersCount.toLong(),
                        budgetUsd = state.budgetUsd,
                        style = state.style,
                        status = "PLANNED"
                    )
                )
                addNotification(
                    title = "AI Itinerary Ready",
                    message = "${generated.title} (${generated.slots.size} stops) saved to Trips.",
                    category = "Itinerary"
                )
            }.onFailure { err ->
                _orbState.value = OrbState.ERROR
                _aiStatusLabel.value = "Itinerary generation failed"
                _wizardState.update {
                    it.copy(
                        isGenerating = false,
                        generationError = err.localizedMessage ?: "Failed to generate itinerary via Gemini."
                    )
                }
            }
        }
    }

    fun moveItinerarySlotUp(slotId: String) {
        _itinerarySlots.update { current ->
            val idx = current.indexOfFirst { it.id == slotId }
            if (idx > 0) {
                val mutable = current.toMutableList()
                val item = mutable.removeAt(idx)
                mutable.add(idx - 1, item)
                mutable.mapIndexed { newIdx, slot -> slot.copy(routeSequence = newIdx + 1) }
            } else current
        }
    }

    fun updateItinerarySlotNoteAndCost(slotId: String, note: String, costUsd: Double) {
        _itineraryMeta.update { map ->
            map + (slotId to ItineraryMeta(estimatedCostUsd = costUsd.coerceAtLeast(0.0), note = note))
        }
    }

    fun addItinerarySlot(title: String, location: String, timeRange: String, category: String, costUsd: Double) {
        val id = "it-${UUID.randomUUID().toString().take(6)}"
        val nextSeq = _itinerarySlots.value.size + 1
        val newSlot = ItinerarySlot(
            id = id,
            timeRange = timeRange.ifBlank { "Flexible Window" },
            title = title.ifBlank { "Custom Activity" },
            location = location.ifBlank { _wizardState.value.destination },
            type = category,
            durationMinutes = 90,
            routeSequence = nextSeq,
            calSyncStatus = "Custom Block"
        )
        _itinerarySlots.update { it + newSlot }
        _itineraryMeta.update { it + (id to ItineraryMeta(costUsd.coerceAtLeast(0.0), "")) }
    }

    fun requestFlightHold(offer: FlightOffer) {
        localRepo.requestBookingHold(offer)
        addNotification(
            title = "Flight Hold Queued",
            message = "${offer.airline} ${offer.flightNumber} ($${offer.priceUsd.toInt()}) awaiting your Shield approval.",
            category = "Approval"
        )
    }

    fun requestLodgingHold(offer: LodgingOffer) {
        localRepo.requestLodgingHold(offer)
        addNotification(
            title = "Hotel Hold Queued",
            message = "${offer.name} ($${(offer.pricePerNightUsd * 2).toInt()} for 2 nights) awaiting Shield approval.",
            category = "Approval"
        )
    }

    fun requestDiningHold(
        restaurantName: String,
        guests: Int,
        timeSlot: String,
        depositUsd: Double,
        address: String
    ) {
        localRepo.requestCustomHold(
            title = "Dining Reservation ($restaurantName)",
            category = "Dining Reservation",
            merchantOrProvider = "$restaurantName (Table Hold)",
            amountUsd = depositUsd,
            details = "$guests guests • $timeSlot • $address",
            providerPolicy = "Deposit applies toward bill. Free cancellation 12h before seating.",
            expiresInMinutes = 45
        )
        addNotification(
            title = "Dining Hold Queued",
            message = "$restaurantName ($guests guests at $timeSlot) staged in Shield Approval.",
            category = "Approval"
        )
    }

    fun requestActivityHold(
        experienceTitle: String,
        guests: Int,
        sessionWindow: String,
        pricePerGuestUsd: Double,
        location: String
    ) {
        val total = pricePerGuestUsd * guests.coerceAtLeast(1)
        localRepo.requestCustomHold(
            title = "Experience Hold ($experienceTitle)",
            category = "Excursion & Activity",
            merchantOrProvider = "$experienceTitle (Verified Operator)",
            amountUsd = total,
            details = "$guests guest(s) • $sessionWindow • $location",
            providerPolicy = "Full refund up to 24h before excursion start.",
            expiresInMinutes = 90
        )
        addNotification(
            title = "Activity Hold Queued",
            message = "$experienceTitle ($${total.toInt()}) awaiting your Shield verification.",
            category = "Approval"
        )
    }

    fun requestCarRentalHold(offer: CarRentalOffer, days: Int, includeInsurance: Boolean) {
        val total = (offer.dailyRateUsd + if (includeInsurance) offer.insurancePerDayUsd else 0.0) * days
        addNotification(
            title = "Vehicle Hold Requested",
            message = "${offer.modelName} ($days days, $${total.toInt()}) staged in Approval Shield.",
            category = "Approval"
        )
        viewModelScope.launch {
            val ref = "ALR-${(1000..9999).random()}-CAR"
            val hash = UUID.randomUUID().toString().replace("-", "") + "c4r0"
            database.bookingRecordDao().upsertBooking(
                BookingRecord(
                    id = "car-${UUID.randomUUID().toString().take(8)}",
                    referenceCode = ref,
                    bookingType = "CAR_RENTAL",
                    title = "${offer.modelName} ($days days)",
                    providerSource = offer.providerSource,
                    providerPnr = null,
                    originOrLocation = offer.pickupHub,
                    destination = null,
                    startTimestamp = System.currentTimeMillis(),
                    endTimestamp = System.currentTimeMillis() + days * 86_400_000L,
                    totalAmountUsd = total,
                    status = "HELD",
                    sha256AuditHash = hash
                )
            )
        }
    }

    fun approveRequest(requestId: String) {
        val req = localRepo.approvalRequests.value.firstOrNull { it.id == requestId } ?: return
        localRepo.approveRequest(requestId)
        val latestTx = localRepo.ledgerTransactions.value.firstOrNull()
        val refCode = latestTx?.referenceCode ?: "ALR-${(1000..9999).random()}-OK"
        val pnr = latestTx?.providerPnr ?: "PNR-${UUID.randomUUID().toString().take(6).uppercase()}"
        val hash = latestTx?.sha256Hash ?: (UUID.randomUUID().toString().replace("-", "") + "e01a")
        val docId = "bk_${UUID.randomUUID().toString().replace("-", "").take(12)}"

        viewModelScope.launch {
            val bookingType = when {
                req.category.contains("Flight", ignoreCase = true) -> "FLIGHT"
                req.category.contains("Accommodation", ignoreCase = true) || req.category.contains("Hotel", ignoreCase = true) -> "HOTEL"
                req.category.contains("Dining", ignoreCase = true) -> "DINING"
                else -> "ACTIVITY"
            }
            database.bookingRecordDao().upsertBooking(
                BookingRecord(
                    id = docId,
                    referenceCode = refCode,
                    bookingType = bookingType,
                    title = req.title,
                    providerSource = req.merchantOrProvider,
                    providerPnr = pnr,
                    originOrLocation = req.details.take(40),
                    destination = null,
                    startTimestamp = System.currentTimeMillis(),
                    endTimestamp = null,
                    totalAmountUsd = req.amountUsd,
                    status = "CONFIRMED",
                    sha256AuditHash = hash
                )
            )
            firestoreRepo.createBooking(
                BookingDoc(
                    id = docId,
                    referenceCode = refCode,
                    bookingType = bookingType,
                    title = req.title,
                    providerSource = req.merchantOrProvider,
                    providerPnr = pnr,
                    originOrLocation = req.details.take(40),
                    totalAmountUsd = req.amountUsd,
                    status = "CONFIRMED",
                    sha256AuditHash = hash
                )
            )
        }
        addNotification(
            title = "Booking Confirmed • $pnr",
            message = "${req.title} passed Shield verification and synced to your cloud vault.",
            category = "Confirmed"
        )
    }

    fun rejectRequest(requestId: String) {
        val req = localRepo.approvalRequests.value.firstOrNull { it.id == requestId }
        localRepo.rejectRequest(requestId)
        if (req != null) {
            addNotification(
                title = "Hold Cancelled",
                message = "${req.title} was declined. Zero charge executed.",
                category = "Security"
            )
        }
    }

    fun cancelConfirmedBooking(referenceCode: String, remoteBookingId: String?) {
        _cancelledRefs.update { it + referenceCode }
        viewModelScope.launch {
            if (!remoteBookingId.isNullOrBlank()) {
                firestoreRepo.updateBookingStatus(remoteBookingId, "CANCELLED")
            }
        }
        addNotification(
            title = "Cancellation Recorded",
            message = "Reference $referenceCode marked as CANCELLED per provider policy.",
            category = "Booking"
        )
    }

    fun toggleSavedItem(
        itemType: String,
        title: String,
        subtitle: String,
        category: String,
        priceLabel: String,
        rating: Double
    ) {
        val existing = _savedItems.value.firstOrNull { it.title == title && it.itemType == itemType }
        viewModelScope.launch {
            if (existing != null) {
                firestoreRepo.deleteSavedItem(existing.id)
            } else {
                firestoreRepo.saveItem(
                    SavedItemDoc(
                        itemType = itemType,
                        title = title,
                        subtitle = subtitle,
                        category = category,
                        priceLabel = priceLabel,
                        rating = rating
                    )
                )
            }
        }
    }

    fun isItemSaved(title: String, itemType: String): Boolean {
        return _savedItems.value.any { it.title == title && it.itemType == itemType }
    }

    fun setDarkGlassTheme(enabled: Boolean) {
        _darkGlassTheme.value = enabled
        updateUserProfile(_userProfile.value.copy(darkGlassTheme = enabled))
    }

    fun setReducedMotion(enabled: Boolean) {
        _reducedMotion.value = enabled
    }

    fun updateUserProfile(updated: UserProfileDoc) {
        _userProfile.value = updated
        viewModelScope.launch {
            database.userPreferenceDao().upsertPreferences(
                UserPreference(
                    userId = currentUserId,
                    preferredCurrency = updated.preferredCurrency,
                    temperatureUnit = updated.temperatureUnit,
                    preferredCabinClass = updated.preferredCabinClass,
                    homeAirportIata = updated.homeAirportIata,
                    biometricThresholdUsd = updated.biometricThresholdUsd
                )
            )
            firestoreRepo.upsertUserProfile(updated)
        }
    }

    fun clearLocalCacheAndResync() {
        viewModelScope.launch {
            database.bookingRecordDao().clearAllBookings()
            addNotification(
                title = "Local Cache Cleared",
                message = "Room offline booking cache refreshed from cloud state.",
                category = "System"
            )
        }
    }

    fun markAllNotificationsRead() {
        _notifications.update { list -> list.map { it.copy(isUnread = false) } }
    }

    fun computeBudgetBreakdowns(): List<BudgetCategoryBreakdown> {
        val confirmedTx = ledgerTransactions.value.filter { it.referenceCode !in _cancelledRefs.value }
        val flightActual = confirmedTx.filter { it.category.contains("Flight", true) }.sumOf { it.amountUsd }
        val lodgingActual = confirmedTx.filter { it.category.contains("Lodg", true) || it.category.contains("Hotel", true) || it.category.contains("Accomm", true) }.sumOf { it.amountUsd }
        val diningActual = confirmedTx.filter { it.category.contains("Dining", true) }.sumOf { it.amountUsd }
        val itineraryExtra = _itineraryMeta.value.values.sumOf { it.estimatedCostUsd }

        val target = _wizardState.value.budgetUsd.coerceAtLeast(1000.0)
        return listOf(
            BudgetCategoryBreakdown("Flights", target * 0.35, flightActual),
            BudgetCategoryBreakdown("Lodging", target * 0.40, lodgingActual),
            BudgetCategoryBreakdown("Dining", target * 0.15, diningActual),
            BudgetCategoryBreakdown("Activities & Transit", target * 0.10, itineraryExtra * 0.25)
        )
    }

    private fun addNotification(title: String, message: String, category: String) {
        val item = AlloraNotificationItem(
            id = "notif-${UUID.randomUUID().toString().take(6)}",
            title = title,
            message = message,
            timestamp = "Just now",
            category = category,
            isUnread = true
        )
        _notifications.update { list -> listOf(item) + list }
    }

    private companion object {
        const val TAG = "AlloraViewModel"
    }
}
