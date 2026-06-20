@file:androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])

package net.wearpipe.app.playback

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.drm.DrmSessionManagerProvider
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.LoadErrorHandlingPolicy
import net.wearpipe.app.network.WearPipeDownloader

class WearPipeMediaSourceFactory(context: Context) : MediaSource.Factory {
    private val dataSource = DefaultDataSource.Factory(
        context,
        DefaultHttpDataSource.Factory()
            .setUserAgent(WearPipeDownloader.USER_AGENT)
            .setAllowCrossProtocolRedirects(true)
    )
    private val fallback = DefaultMediaSourceFactory(dataSource).setLiveTargetOffsetMs(5_000)
    private val dash = DashMediaSource.Factory(dataSource)
        .setManifestParser(YoutubeDashLiveManifestParser())
        .setFallbackTargetLiveOffsetMs(5_000)
    private val hls = HlsMediaSource.Factory(dataSource).setAllowChunklessPreparation(true)

    override fun createMediaSource(mediaItem: MediaItem): MediaSource = when (
        mediaItem.localConfiguration?.mimeType
    ) {
        MimeTypes.APPLICATION_MPD -> dash.createMediaSource(mediaItem)
        MimeTypes.APPLICATION_M3U8 -> hls.createMediaSource(mediaItem)
        else -> fallback.createMediaSource(mediaItem)
    }

    override fun getSupportedTypes(): IntArray = fallback.supportedTypes

    override fun setDrmSessionManagerProvider(provider: DrmSessionManagerProvider): MediaSource.Factory {
        fallback.setDrmSessionManagerProvider(provider)
        dash.setDrmSessionManagerProvider(provider)
        hls.setDrmSessionManagerProvider(provider)
        return this
    }

    override fun setLoadErrorHandlingPolicy(policy: LoadErrorHandlingPolicy): MediaSource.Factory {
        fallback.setLoadErrorHandlingPolicy(policy)
        dash.setLoadErrorHandlingPolicy(policy)
        hls.setLoadErrorHandlingPolicy(policy)
        return this
    }
}
