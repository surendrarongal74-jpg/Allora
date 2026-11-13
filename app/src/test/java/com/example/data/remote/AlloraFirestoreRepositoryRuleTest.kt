package com.example.data.remote

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class AlloraFirestoreRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun upsertUserProfile_andObserveProfile_succeedsForOwner() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = AlloraFirestoreRepository(firestore, auth)

        val profile = UserProfileDoc(
            userId = uid,
            displayName = "Alice Vance",
            preferredCurrency = "USD",
            temperatureUnit = "METRIC",
            preferredCabinClass = "BUSINESS",
            homeAirportIata = "SFO",
            travelStyle = "Luxury",
            budgetTier = "Premium",
            biometricThresholdUsd = 500.0,
            darkGlassTheme = true
        )

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) { repo.upsertUserProfile(profile) }
        assertTrue(saveResult.isSuccess)

        val observed = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeUserProfile(uid).first { it != null }
        }
        assertNotNull(observed)
        assertEquals("Alice Vance", observed?.displayName)
        assertEquals("SFO", observed?.homeAirportIata)
    }

    @Test
    fun createTrip_andCreateBooking_emitsRealtimeUpdatesForOwner() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repo = AlloraFirestoreRepository(firestore, auth)

        val tripId = "trip_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val createTripResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createTrip(
                TripPlanDoc(
                    id = tripId,
                    title = "Tokyo Autumn Escape",
                    destination = "Tokyo, Japan",
                    startDate = "2026-11-18",
                    endDate = "2026-11-24",
                    travellersCount = 2L,
                    budgetUsd = 4800.0,
                    style = "Culture & Luxury",
                    status = "PLANNED"
                )
            )
        }
        assertTrue(createTripResult.isSuccess)

        val trips = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeTrips(uid).first { list -> list.any { it.id == tripId } }
        }
        assertTrue(trips.any { it.id == tripId && it.destination == "Tokyo, Japan" })

        val bookingId = "bk_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        val createBookingResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repo.createBooking(
                BookingDoc(
                    id = bookingId,
                    referenceCode = "ALR-901-FLT",
                    bookingType = "FLIGHT",
                    title = "ANA NH 008 HND -> SFO",
                    providerSource = "Duffel NDC",
                    originOrLocation = "HND",
                    destination = "SFO",
                    totalAmountUsd = 720.0,
                    status = "CONFIRMED",
                    sha256AuditHash = "sha256_test_hash_1234"
                )
            )
        }
        assertTrue(createBookingResult.isSuccess)

        val bookings = withTimeout(FLOW_TIMEOUT_MS) {
            repo.observeBookings(uid).first { list -> list.any { it.id == bookingId } }
        }
        assertTrue(bookings.any { it.id == bookingId && it.referenceCode == "ALR-901-FLT" })
    }

    @Test
    fun getBookingById_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = AlloraFirestoreRepository(firestore, auth)
        val bookingId = "bk_${UUID.randomUUID().toString().replace("-", "").take(12)}"
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.createBooking(
                BookingDoc(
                    id = bookingId,
                    referenceCode = "ALR-PRIV-001",
                    bookingType = "HOTEL",
                    title = "Aman Tokyo",
                    providerSource = "Amadeus Hospitality",
                    originOrLocation = "Tokyo",
                    totalAmountUsd = 1240.0,
                    status = "CONFIRMED",
                    sha256AuditHash = "sha256_private_booking"
                )
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = AlloraFirestoreRepository(firestore, auth)
        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            bobRepo.getBookingById(aliceUid, bookingId)
        }
        assertTrue(result.isFailure)
        val firestoreException = result.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", firestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    @Test
    fun observeTrips_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repo = AlloraFirestoreRepository(firestore, auth)
        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repo.observeTrips("alice_unauth_check").first()
            }
        } catch (t: Throwable) {
            caught = t
        }
        assertNotNull("Expected exception when unauthenticated", caught)
        val firestoreException = caught?.unwrapFirestoreException()
        assertNotNull("Expected nested FirebaseFirestoreException, got $caught", firestoreException)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_allora@test.com"
        const val BOB_EMAIL = "bob_allora@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
