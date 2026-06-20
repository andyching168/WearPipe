package net.wearpipe.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.AutoMigration
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PlaybackHistoryEntry::class, SearchHistoryEntry::class, QueueEntry::class],
    version = 2,
    autoMigrations = [AutoMigration(from = 1, to = 2)],
    exportSchema = true
)
abstract class WearPipeDatabase : RoomDatabase() {
    abstract fun dao(): WearPipeDao

    companion object {
        fun create(context: Context): WearPipeDatabase = Room.databaseBuilder(
            context.applicationContext,
            WearPipeDatabase::class.java,
            "wearpipe.db"
        ).build()
    }
}
