package net.wearpipe.app.data

import net.wearpipe.app.data.local.PlaybackHistoryEntry
import net.wearpipe.app.data.local.SearchHistoryEntry
import net.wearpipe.app.data.local.WearPipeDao
import net.wearpipe.app.model.PlayableMedia

class RoomHistoryRepository(private val dao: WearPipeDao) : HistoryRepository {
    override val history = dao.history()
    override val searches = dao.searches()

    override suspend fun recordSearch(query: String) {
        val normalized = query.trim()
        if (normalized.isNotEmpty()) dao.saveSearch(SearchHistoryEntry(normalized, System.currentTimeMillis()))
    }

    override suspend fun recordPlayback(media: PlayableMedia, positionMs: Long) {
        dao.saveHistory(
            PlaybackHistoryEntry(
                sourceUrl = media.sourceUrl,
                serviceId = 0,
                title = media.title,
                uploader = media.uploader,
                thumbnailUrl = media.thumbnailUrl,
                durationMs = media.durationSeconds * 1_000,
                positionMs = positionMs.coerceAtLeast(0),
                playedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun deleteHistory(sourceUrl: String) = dao.deleteHistory(sourceUrl)
    override suspend fun clearHistory() = dao.clearHistory()
    override suspend fun clearSearches() = dao.clearSearches()
    override suspend fun playbackPosition(sourceUrl: String): Long = dao.playbackPosition(sourceUrl) ?: 0
}
