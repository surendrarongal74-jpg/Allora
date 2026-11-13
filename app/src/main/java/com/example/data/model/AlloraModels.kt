package com.example.data.model

enum class ApprovalStatus {
    PENDING,
    APPROVED,
    REJECTED
}

data class RepoAuditItem(
    val number: Int,
    val name: String,
    val category: String,
    val repoUrl: String,
    val primaryStack: String,
    val packageManager: String,
    val dependenciesSummary: String,
    val database: String,
    val authMethod: String,
    val apiIntegrations: String,
    val isRealApi: Boolean,
    val apiRealismDetails: String,
    val bookingRealism: String,
    val license: String,
    val securitySeverity: String, // "CRITICAL", "HIGH", "MEDIUM", "LOW"
    val securityFindings: String,
    val duplicateWith: String?,
    val recommendation: String, // "REUSE LOGIC", "MODIFY & ADAPT", "REPLACE CORE", "POSTPONE"
    val preservedFiles: List<String>,
    val missingCredentials: List<String>
)

data class FlightOffer(
    val id: String,
    val airline: String,
    val flightNumber: String,
    val originIata: String,
    val destinationIata: String,
    val originCity: String,
    val destinationCity: String,
    val departureTime: String,
    val arrivalTime: String,
    val duration: String,
    val priceUsd: Double,
    val cabinClass: String,
    val stops: Int,
    val providerSource: String,
    val isHoldActive: Boolean = false
)

data class LodgingOffer(
    val id: String,
    val name: String,
    val location: String,
    val rating: Double,
    val reviewCount: Int,
    val pricePerNightUsd: Double,
    val amenities: List<String>,
    val roomType: String,
    val providerSource: String,
    val isAvailable: Boolean = true
)

data class WeatherReport(
    val city: String,
    val tempCelsius: Double,
    val condition: String,
    val humidityPercent: Int,
    val windKmh: Double,
    val uvIndex: Int
)

data class PointOfInterest(
    val id: String,
    val name: String,
    val category: String, // "Attraction", "Restaurant", "Hotel", "Culture"
    val rating: Double,
    val reviewCount: Int,
    val address: String,
    val lat: Double,
    val lng: Double,
    val tag: String
)

data class ItinerarySlot(
    val id: String,
    val timeRange: String,
    val title: String,
    val location: String,
    val type: String, // "Flight", "CheckIn", "Dining", "Sights", "Transit"
    val durationMinutes: Int,
    val routeSequence: Int,
    val calSyncStatus: String // "Synced with Google Calendar", "Pending Approval"
)

data class ApprovalRequest(
    val id: String,
    val title: String,
    val category: String,
    val merchantOrProvider: String,
    val amountUsd: Double,
    val riskLevel: String, // "Low Risk", "Medium Risk", "High Risk"
    val status: ApprovalStatus,
    val details: String,
    val providerPolicy: String,
    val expiresInMinutes: Int
)

data class LedgerTransaction(
    val id: String,
    val referenceCode: String,
    val title: String,
    val category: String,
    val amountUsd: Double,
    val providerPnr: String,
    val timestamp: String,
    val status: String,
    val sha256Hash: String
)
