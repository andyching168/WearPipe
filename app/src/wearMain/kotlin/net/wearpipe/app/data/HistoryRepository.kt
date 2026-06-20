package net.wearpipe.app.data

import kotlinx.coroutines.flow.Flow
import net.wearpipe.app.data.local.PlaybackHistoryEntry
import net.wearpipe.app.data.local.SearchHistoryEntry
import net.wearpipe.app.model.PlayableMedia

interface HistoryRepository {
    val history: Flow<List<PlaybackHistoryEntry>>
    val searches: Flow<List<SearchHistoryEntry>>
    suspend fun recordSearch(query: String)
    suspend fun recordPlayback(media: PlayableMedia, positionMs: Long)
    suspend fun deleteHistory(sourceUrl: String)
    suspend fun clearHistory()
    suspend fun clearSearches()
    suspend fun playbackPosition(sourceUrl: String): Long
}
