package net.wearpipe.app.playback

import android.content.ComponentName
import android.content.Context
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import net.wearpipe.app.model.PlayableMedia

class Media3PlaybackRepository(context: Context) : PlaybackRepository {
    private val appContext = context.applicationContext
    private val controllerFuture = MediaController.Builder(
        appContext,
        SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
    ).buildAsync()
    private var controller: MediaController? = null
    private val mutableState = MutableStateFlow(PlaybackUiState())
    override val state = mutableState.asStateFlow()

    private val listener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) = publish(player)
        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
            mutableState.value = mutableState.value.copy(error = error.localizedMessage ?: "無法播放")
        }
    }

    init {
        controllerFuture.addListener({
            runCatching { controllerFuture.get() }.onSuccess {
                controller = it
                it.addListener(listener)
                publish(it)
            }.onFailure {
                mutableState.value = mutableState.value.copy(error = it.localizedMessage)
            }
        }, ContextCompat.getMainExecutor(appContext))
    }

    override fun play(media: PlayableMedia, replaceQueue: Boolean, startPositionMs: Long) = withController {
        if (replaceQueue) setMediaItem(media.toMediaItem(), startPositionMs)
        else addMediaItem(media.toMediaItem())
        prepare()
        play()
    }

    override fun enqueue(media: PlayableMedia) = withController { addMediaItem(media.toMediaItem()) }
    override fun togglePlayPause() = withController { if (isPlaying) pause() else play() }
    override fun seekBy(deltaMs: Long) = withController {
        seekTo((currentPosition + deltaMs).coerceIn(0, duration.takeIf { it > 0 } ?: Long.MAX_VALUE))
    }
    override fun skipNext() = withController { seekToNextMediaItem() }
    override fun skipPrevious() = withController { seekToPreviousMediaItem() }
    override fun playAt(index: Int) = withController {
        if (index in 0 until mediaItemCount) {
            seekTo(index, 0)
            play()
        }
    }
    override fun removeAt(index: Int) = withController { if (index in 0 until mediaItemCount) removeMediaItem(index) }
    override fun setVolume(volume: Float) = withController { this.volume = volume.coerceIn(0f, 1f) }

    override fun release() {
        controller?.removeListener(listener)
        MediaController.releaseFuture(controllerFuture)
        controller = null
    }

    private inline fun withController(block: MediaController.() -> Unit) {
        controller?.block()
    }

    private fun publish(player: Player) {
        val metadata = player.mediaMetadata
        val mode = metadata.extras?.getString(EXTRA_MODE)
        mutableState.value = PlaybackUiState(
            connected = true,
            isPlaying = player.isPlaying,
            title = metadata.title?.toString().orEmpty(),
            uploader = metadata.artist?.toString().orEmpty(),
            artworkUrl = metadata.artworkUri?.toString(),
            sourceUrl = metadata.extras?.getString(EXTRA_SOURCE_URL),
            positionMs = player.currentPosition.coerceAtLeast(0),
            durationMs = player.duration.coerceAtLeast(0),
            queueSize = player.mediaItemCount,
            currentIndex = player.currentMediaItemIndex.coerceAtLeast(0),
            video = mode != "AUDIO"
        )
    }
}
