package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [BookEntity::class, BookPageEntity::class, BookmarkEntity::class],
    version = 2,
    exportSchema = false
)
abstract class CivLibDatabase : RoomDatabase() {
    abstract fun civLibDao(): CivLibDao

    companion object {
        @Volatile
        private var INSTANCE: CivLibDatabase? = null

        fun getInstance(context: Context): CivLibDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    CivLibDatabase::class.java,
                    "civlib_repository.db"
                ).fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
