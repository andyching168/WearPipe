package net.wearpipe.app.playback

import android.net.Uri
import android.os.Bundle
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import net.wearpipe.app.model.PlaybackMode
import net.wearpipe.app.model.PlayableMedia

const val EXTRA_SOURCE_URL = "wearpipe.sourceUrl"
const val EXTRA_MODE = "wearpipe.mode"
const val EXTRA_DURATION_SECONDS = "wearpipe.durationSeconds"
const val EXTRA_IS_LIVE = "wearpipe.isLive"

fun PlayableMedia.toMediaItem(): MediaItem {
    val extras = Bundle().apply {
        putString(EXTRA_SOURCE_URL, sourceUrl)
        putString(EXTRA_MODE, mode.name)
        putLong(EXTRA_DURATION_SECONDS, durationSeconds)
        putBoolean(EXTRA_IS_LIVE, isLive)
    }
    val builder = MediaItem.Builder()
        .setMediaId(sourceUrl)
        .setUri(mediaUrl)
        .setMimeType(mimeType)
        .setMediaMetadata(
            MediaMetadata.Builder()
                .setTitle(title)
                .setArtist(uploader)
                .setArtworkUri(thumbnailUrl?.toUri())
                .setExtras(extras)
                .build()
        )
    if (isLive) {
        builder.setLiveConfiguration(
            MediaItem.LiveConfiguration.Builder().setTargetOffsetMs(5_000).build()
        )
    }
    return builder.build()
}

fun MediaItem.toPlayableMedia(): PlayableMedia? {
    val uri = localConfiguration?.uri ?: return null
    val extras = mediaMetadata.extras
    return PlayableMedia(
        sourceUrl = extras?.getString(EXTRA_SOURCE_URL) ?: mediaId,
        title = mediaMetadata.title?.toString().orEmpty(),
        uploader = mediaMetadata.artist?.toString().orEmpty(),
        thumbnailUrl = mediaMetadata.artworkUri?.toString(),
        durationSeconds = extras?.getLong(EXTRA_DURATION_SECONDS) ?: 0,
        mediaUrl = uri.toString(),
        mode = runCatching {
            PlaybackMode.valueOf(extras?.getString(EXTRA_MODE) ?: PlaybackMode.VIDEO.name)
        }.getOrDefault(PlaybackMode.VIDEO),
        mimeType = localConfiguration?.mimeType,
        isLive = extras?.getBoolean(EXTRA_IS_LIVE) ?: false
    )
}
