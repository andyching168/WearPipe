package net.wearpipe.app.playback

import kotlinx.coroutines.flow.StateFlow
import net.wearpipe.app.model.PlayableMedia

data class PlaybackUiState(
    val connected: Boolean = false,
    val isPlaying: Boolean = false,
    val title: String = "",
    val uploader: String = "",
    val artworkUrl: String? = null,
    val sourceUrl: String? = null,
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val queueSize: Int = 0,
    val currentIndex: Int = 0,
    val video: Boolean = true,
    val error: String? = null
)

interface PlaybackRepository {
    val state: StateFlow<PlaybackUiState>
    fun play(media: PlayableMedia, replaceQueue: Boolean = true, startPositionMs: Long = 0)
    fun enqueue(media: PlayableMedia)
    fun togglePlayPause()
    fun seekBy(deltaMs: Long)
    fun skipNext()
    fun skipPrevious()
    fun playAt(index: Int)
    fun removeAt(index: Int)
    fun setVolume(volume: Float)
    fun release()
}
