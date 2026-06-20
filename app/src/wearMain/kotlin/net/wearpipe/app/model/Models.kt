package net.wearpipe.app.model

data class SearchPageToken(val value: Any)

data class SearchResult(
    val serviceId: Int,
    val url: String,
    val title: String,
    val uploader: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long
)

data class SearchPage(
    val items: List<SearchResult>,
    val nextPage: SearchPageToken?
)

enum class TrackKind { VIDEO, AUDIO }

data class MediaTrack(
    val url: String,
    val kind: TrackKind,
    val format: String?,
    val resolution: String? = null,
    val bitrateKbps: Int? = null,
    val mimeType: String? = null,
    val isLive: Boolean = false
)

data class StreamDetails(
    val serviceId: Int,
    val url: String,
    val title: String,
    val uploader: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val videoTracks: List<MediaTrack>,
    val audioTracks: List<MediaTrack>,
    val liveTracks: List<MediaTrack> = emptyList(),
    val isLive: Boolean = false
)

enum class PlaybackMode { VIDEO, AUDIO }

data class PlayableMedia(
    val sourceUrl: String,
    val title: String,
    val uploader: String,
    val thumbnailUrl: String?,
    val durationSeconds: Long,
    val mediaUrl: String,
    val mode: PlaybackMode,
    val mimeType: String? = null,
    val isLive: Boolean = false
)

sealed interface AppError {
    data object Network : AppError
    data object NotFound : AppError
    data object NoCompatibleStream : AppError
    data class Unknown(val message: String?) : AppError
}
