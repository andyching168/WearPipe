package net.wearpipe.app.data

import net.wearpipe.app.model.MediaTrack
import net.wearpipe.app.model.PlaybackMode
import net.wearpipe.app.model.PlayableMedia
import net.wearpipe.app.model.StreamDetails

class StreamSelector {
    fun selectVideo(details: StreamDetails, maxHeight: Int = 720): PlayableMedia? {
        if (details.isLive) {
            return details.liveTracks.firstOrNull()?.let { details.playable(it, PlaybackMode.VIDEO) }
        }
        val selected = details.videoTracks
            .filter { resolutionHeight(it) <= maxHeight }
            .maxByOrNull(::resolutionHeight)
            ?: details.videoTracks.minByOrNull(::resolutionHeight)
        return selected?.let { details.playable(it, PlaybackMode.VIDEO) }
    }

    fun selectAudio(details: StreamDetails): PlayableMedia? =
        (if (details.isLive) details.liveTracks.firstOrNull()
        else details.audioTracks.maxByOrNull { it.bitrateKbps ?: 0 })
            ?.let { details.playable(it, PlaybackMode.AUDIO) }

    private fun resolutionHeight(track: MediaTrack): Int =
        track.resolution?.substringBefore('p')?.filter(Char::isDigit)?.toIntOrNull() ?: 0

    private fun StreamDetails.playable(track: MediaTrack, mode: PlaybackMode) = PlayableMedia(
        sourceUrl = url,
        title = title,
        uploader = uploader,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationSeconds,
        mediaUrl = track.url,
        mode = mode,
        mimeType = track.mimeType,
        isLive = track.isLive
    )
}
