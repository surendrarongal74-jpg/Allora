package com.example.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity storing user-specific travel, weather, and approval-gate preferences offline.
 * Syncs with the shared PostgreSQL user profile when connectivity is available.
 */
@Entity(tableName = "user_preferences")
data class UserPreference(
    @PrimaryKey
    @ColumnInfo(name = "user_id")
    val userId: String = "default_user",

    @ColumnInfo(name = "preferred_currency")
    val preferredCurrency: String = "USD",

    /**
     * Temperature scale preference: "METRIC" (Celsius) or "IMPERIAL" (Fahrenheit).
     */
    @ColumnInfo(name = "temperature_unit")
    val temperatureUnit: String = "METRIC",

    /**
     * Default flight cabin preference: ECONOMY, PREMIUM_ECONOMY, BUSINESS, or FIRST.
     */
    @ColumnInfo(name = "preferred_cabin_class")
    val preferredCabinClass: String = "ECONOMY",

    @ColumnInfo(name = "home_airport_iata")
    val homeAirportIata: String = "SFO",

    @ColumnInfo(name = "preferred_hotel_tier")
    val preferredHotelTier: String = "4_STAR_PLUS",

    /**
     * Maximum USD amount eligible for pre-authorized hold flows before requiring biometric confirmation.
     */
    @ColumnInfo(name = "biometric_threshold_usd")
    val biometricThresholdUsd: Double = 250.0,

    @ColumnInfo(name = "require_explicit_approval")
    val requireExplicitApproval: Boolean = true,

    @ColumnInfo(name = "calendar_sync_enabled")
    val calendarSyncEnabled: Boolean = true,

    @ColumnInfo(name = "offline_cache_ttl_hours")
    val offlineCacheTtlHours: Int = 72,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
