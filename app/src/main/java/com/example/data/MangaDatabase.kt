package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        MangaEntity::class,
        ReadHistory::class,
        Bookmark::class,
        TeamMember::class,
        UserPurchase::class,
        UserAccount::class,
        SystemSettingsEntity::class,
        RecruitmentApplication::class,
        StoryEntity::class,
        ChapterPurchaseRecord::class,
        SupportTicket::class,
        ChapterWork::class
    ],
    version = 3,
    exportSchema = false
)
abstract class MangaDatabase : RoomDatabase() {
    abstract fun mangaDao(): MangaDao

    companion object {
        @Volatile
        private var INSTANCE: MangaDatabase? = null

        fun getDatabase(context: Context): MangaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MangaDatabase::class.java,
                    "mangata_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
