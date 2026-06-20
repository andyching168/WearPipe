package net.wearpipe.app.data.local

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

@Entity(tableName = "playback_history")
data class PlaybackHistoryEntry(
    @PrimaryKey val sourceUrl: String,
    val serviceId: Int,
    val title: String,
    val uploader: String,
    val thumbnailUrl: String?,
    val durationMs: Long,
    val positionMs: Long,
    val playedAt: Long
)

@Entity(tableName = "search_history")
data class SearchHistoryEntry(
    @PrimaryKey val query: String,
    val searchedAt: Long
)

@Entity(tableName = "playback_queue")
data class QueueEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val queueIndex: Int,
    val sourceUrl: String,
    val title: String,
    val uploader: String,
    val thumbnailUrl: String?,
    val mediaUrl: String,
    val durationMs: Long,
    val mode: String,
    val mimeType: String?,
    @ColumnInfo(defaultValue = "0") val isLive: Boolean
)
