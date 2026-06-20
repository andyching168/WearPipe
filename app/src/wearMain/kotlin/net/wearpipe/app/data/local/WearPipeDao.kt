package net.wearpipe.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WearPipeDao {
    @Query("SELECT * FROM playback_history ORDER BY playedAt DESC LIMIT :limit")
    fun history(limit: Int = 50): Flow<List<PlaybackHistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveHistory(entry: PlaybackHistoryEntry)

    @Query("DELETE FROM playback_history WHERE sourceUrl = :sourceUrl")
    suspend fun deleteHistory(sourceUrl: String)

    @Query("DELETE FROM playback_history")
    suspend fun clearHistory()

    @Query("SELECT positionMs FROM playback_history WHERE sourceUrl = :sourceUrl LIMIT 1")
    suspend fun playbackPosition(sourceUrl: String): Long?

    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT :limit")
    fun searches(limit: Int = 12): Flow<List<SearchHistoryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSearch(entry: SearchHistoryEntry)

    @Query("DELETE FROM search_history")
    suspend fun clearSearches()

    @Query("SELECT * FROM playback_queue ORDER BY queueIndex")
    fun queue(): Flow<List<QueueEntry>>

    @Query("SELECT * FROM playback_queue ORDER BY queueIndex")
    suspend fun queueSnapshot(): List<QueueEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQueue(entries: List<QueueEntry>)

    @Query("DELETE FROM playback_queue")
    suspend fun clearQueue()

    @Query("DELETE FROM playback_queue WHERE id = :id")
    suspend fun removeQueue(id: Long)
}
