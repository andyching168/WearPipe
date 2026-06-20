@file:androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])

package net.wearpipe.app.playback

import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.wearpipe.app.WearPipeApplication
import net.wearpipe.app.data.local.QueueEntry

class PlaybackService : MediaSessionService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private lateinit var player: ExoPlayer
    private var mediaSession: MediaSession? = null
    private var progressJob: Job? = null
    private val dao get() = (application as WearPipeApplication).graph.database.dao()
    private val history get() = (application as WearPipeApplication).graph.historyRepository
    private val preferences by lazy { getSharedPreferences("playback", MODE_PRIVATE) }

    private val listener = object : Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            applyPlaybackMode(mediaItem)
            persistQueue()
            persistCurrent()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            if (isPlaying) startProgressSaver() else {
                progressJob?.cancel()
                persistCurrent()
            }
        }

        override fun onTimelineChanged(timeline: androidx.media3.common.Timeline, reason: Int) {
            persistQueue()
        }
    }

    private fun applyPlaybackMode(mediaItem: MediaItem?) {
        val audioOnly = mediaItem?.mediaMetadata?.extras?.getString(EXTRA_MODE) == "AUDIO"
        player.trackSelectionParameters = player.trackSelectionParameters.buildUpon()
            .setTrackTypeDisabled(C.TRACK_TYPE_VIDEO, audioOnly)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        player = ExoPlayer.Builder(this, WearPipeMediaSourceFactory(this))
            .setHandleAudioBecomingNoisy(true)
            .build()
            .also { it.addListener(listener) }
        mediaSession = MediaSession.Builder(this, player).build()
        scope.launch(Dispatchers.IO) {
            val saved = dao.queueSnapshot()
            if (saved.isNotEmpty()) {
                val items = saved.map { it.toMediaItem() }
                launch(Dispatchers.Main) {
                    if (player.mediaItemCount == 0) {
                        val index = preferences.getInt("queue_index", 0).coerceIn(items.indices)
                        val position = preferences.getLong("position_ms", 0).coerceAtLeast(0)
                        player.setMediaItems(items, index, position)
                        player.prepare()
                    }
                }
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (!player.isPlaying) stopSelf()
    }

    override fun onDestroy() {
        persistCurrent()
        player.removeListener(listener)
        player.release()
        mediaSession?.release()
        mediaSession = null
        scope.cancel()
        super.onDestroy()
    }

    private fun startProgressSaver() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (true) {
                delay(5_000)
                persistCurrent()
            }
        }
    }

    private fun persistCurrent() {
        val media = player.currentMediaItem?.toPlayableMedia() ?: return
        val position = player.currentPosition
        preferences.edit()
            .putInt("queue_index", player.currentMediaItemIndex.coerceAtLeast(0))
            .putLong("position_ms", position.coerceAtLeast(0))
            .apply()
        scope.launch(Dispatchers.IO) { history.recordPlayback(media, position) }
    }

    private fun persistQueue() {
        val entries = (0 until player.mediaItemCount).mapNotNull { index ->
            player.getMediaItemAt(index).toPlayableMedia()?.let { media ->
                QueueEntry(
                    queueIndex = index,
                    sourceUrl = media.sourceUrl,
                    title = media.title,
                    uploader = media.uploader,
                    thumbnailUrl = media.thumbnailUrl,
                    mediaUrl = media.mediaUrl,
                    durationMs = media.durationSeconds * 1_000,
                    mode = media.mode.name,
                    mimeType = media.mimeType,
                    isLive = media.isLive
                )
            }
        }
        scope.launch(Dispatchers.IO) {
            dao.clearQueue()
            if (entries.isNotEmpty()) dao.insertQueue(entries)
        }
    }

    private fun QueueEntry.toMediaItem() = net.wearpipe.app.model.PlayableMedia(
        sourceUrl = sourceUrl,
        title = title,
        uploader = uploader,
        thumbnailUrl = thumbnailUrl,
        durationSeconds = durationMs / 1_000,
        mediaUrl = mediaUrl,
        mode = runCatching { net.wearpipe.app.model.PlaybackMode.valueOf(mode) }
            .getOrDefault(net.wearpipe.app.model.PlaybackMode.VIDEO),
        mimeType = mimeType,
        isLive = isLive
    ).toMediaItem()
}
