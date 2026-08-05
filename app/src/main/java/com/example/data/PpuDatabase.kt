package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        NoticeEntity::class,
        ResultEntity::class,
        PyqEntity::class,
        AdmissionEntity::class,
        ScholarshipEntity::class,
        BannerEntity::class,
        UserNotificationEntity::class,
        StudentSuggestionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class PpuDatabase : RoomDatabase() {
    abstract fun ppuDao(): PpuDao

    companion object {
        @Volatile
        private var INSTANCE: PpuDatabase? = null

        fun getDatabase(context: Context): PpuDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PpuDatabase::class.java,
                    "ppu_patna_database.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
