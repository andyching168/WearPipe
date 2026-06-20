@file:androidx.annotation.OptIn(markerClass = [androidx.media3.common.util.UnstableApi::class])

package net.wearpipe.app.playback

import android.net.Uri
import androidx.media3.exoplayer.dash.manifest.DashManifest
import androidx.media3.exoplayer.dash.manifest.DashManifestParser
import androidx.media3.exoplayer.dash.manifest.Period
import androidx.media3.exoplayer.dash.manifest.ProgramInformation
import androidx.media3.exoplayer.dash.manifest.ServiceDescriptionElement
import androidx.media3.exoplayer.dash.manifest.UtcTimingElement

/** Keeps YouTube live playback at the newest available period, matching NewPipe's player fix. */
class YoutubeDashLiveManifestParser : DashManifestParser() {
    override fun buildMediaPresentationDescription(
        availabilityStartTime: Long,
        durationMs: Long,
        minBufferTimeMs: Long,
        dynamic: Boolean,
        minUpdateTimeMs: Long,
        timeShiftBufferDepthMs: Long,
        suggestedPresentationDelayMs: Long,
        publishTimeMs: Long,
        programInformation: ProgramInformation?,
        utcTiming: UtcTimingElement?,
        serviceDescription: ServiceDescriptionElement?,
        location: Uri?,
        periods: List<Period>
    ): DashManifest = super.buildMediaPresentationDescription(
        0,
        durationMs,
        minBufferTimeMs,
        dynamic,
        minUpdateTimeMs,
        timeShiftBufferDepthMs,
        suggestedPresentationDelayMs,
        publishTimeMs,
        programInformation,
        utcTiming,
        serviceDescription,
        location,
        periods
    )
}
