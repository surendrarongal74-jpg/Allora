package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room entity caching confirmed, held, or pending travel bookings locally for offline itinerary access.
 * Mirrors the PostgreSQL `booking_records` table defined in the ALLORA backend schema.
 */
@Entity(
    tableName = "booking_records",
    indices = [
        Index(value = ["reference_code"], unique = true),
        Index(value = ["booking_type", "status"])
    ]
)
data class BookingRecord(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "reference_code")
    val referenceCode: String,

    /**
     * Domain category: FLIGHT, HOTEL, CAR_RENTAL, DINING, or ACTIVITY.
     */
    @ColumnInfo(name = "booking_type")
    val bookingType: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "provider_source")
    val providerSource: String,

    @ColumnInfo(name = "provider_pnr")
    val providerPnr: String?,

    @ColumnInfo(name = "origin_or_location")
    val originOrLocation: String,

    @ColumnInfo(name = "destination")
    val destination: String?,

    @ColumnInfo(name = "start_timestamp")
    val startTimestamp: Long,

    @ColumnInfo(name = "end_timestamp")
    val endTimestamp: Long?,

    @ColumnInfo(name = "total_amount_usd")
    val totalAmountUsd: Double,

    @ColumnInfo(name = "currency")
    val currency: String = "USD",

    /**
     * Two-phase commit lifecycle state: HELD, USER_APPROVED, PAYMENT_CAPTURED, CONFIRMED, or CANCELLED.
     */
    @ColumnInfo(name = "status")
    val status: String,

    @ColumnInfo(name = "sha256_audit_hash")
    val sha256AuditHash: String,

    @ColumnInfo(name = "metadata_json")
    val metadataJson: String = "{}",

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = true,

    @ColumnInfo(name = "last_synced_at")
    val lastSyncedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
