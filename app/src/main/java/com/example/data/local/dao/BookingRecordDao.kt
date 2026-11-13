package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.BookingRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface BookingRecordDao {
    @Query("SELECT * FROM booking_records ORDER BY start_timestamp ASC")
    fun observeAllBookings(): Flow<List<BookingRecord>>

    @Query("SELECT * FROM booking_records WHERE status = :status ORDER BY start_timestamp ASC")
    fun observeBookingsByStatus(status: String): Flow<List<BookingRecord>>

    @Query("SELECT * FROM booking_records WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: String): BookingRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBooking(record: BookingRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookings(records: List<BookingRecord>)

    @Query("DELETE FROM booking_records WHERE id = :id")
    suspend fun deleteBookingById(id: String)

    @Query("DELETE FROM booking_records")
    suspend fun clearAllBookings()
}
