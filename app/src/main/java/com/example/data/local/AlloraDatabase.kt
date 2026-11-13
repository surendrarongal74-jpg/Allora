package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.BookingRecordDao
import com.example.data.local.dao.UserPreferenceDao
import com.example.data.local.entity.BookingRecord
import com.example.data.local.entity.UserPreference

@Database(
    entities = [
        BookingRecord::class,
        UserPreference::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AlloraDatabase : RoomDatabase() {
    abstract fun bookingRecordDao(): BookingRecordDao
    abstract fun userPreferenceDao(): UserPreferenceDao

    companion object {
        @Volatile
        private var INSTANCE: AlloraDatabase? = null

        fun getInstance(context: Context): AlloraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AlloraDatabase::class.java,
                    "allora_offline_cache.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
