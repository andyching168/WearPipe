package net.wearpipe.app.data

import net.wearpipe.app.model.MediaTrack
import net.wearpipe.app.model.PlaybackMode
import net.wearpipe.app.model.StreamDetails
import net.wearpipe.app.model.TrackKind
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StreamSelectorTest {
    private val selector = StreamSelector()

    @Test
    fun selectsHighestVideoAtOrBelowLimit() {
        val result = selector.selectVideo(details(videoTracks = listOf(video("360p"), video("720p"), video("1080p"))), 720)
        assertEquals("720p", result?.mediaUrl)
        assertEquals(PlaybackMode.VIDEO, result?.mode)
    }

    @Test
    fun fallsBackToSmallestVideoWhenAllExceedLimit() {
        assertEquals("1080p", selector.selectVideo(details(videoTracks = listOf(video("2160p"), video("1080p"))), 720)?.mediaUrl)
    }

    @Test
    fun selectsHighestBitrateAudio() {
        val tracks = listOf(audio(64), audio(160), audio(128))
        assertEquals("160", selector.selectAudio(details(audioTracks = tracks))?.mediaUrl)
    }

    @Test
    fun returnsNullWhenRequestedKindIsUnavailable() {
        assertNull(selector.selectAudio(details(videoTracks = listOf(video("360p")))))
    }

    @Test
    fun livePlaybackPrefersDashManifestForVideoAndAudioMode() {
        val dash = MediaTrack(
            "https://example.test/live.mpd",
            TrackKind.VIDEO,
            "DASH",
            mimeType = "application/dash+xml",
            isLive = true
        )
        val hls = MediaTrack(
            "https://example.test/live.m3u8",
            TrackKind.VIDEO,
            "HLS",
            mimeType = "application/x-mpegURL",
            isLive = true
        )
        val live = details(liveTracks = listOf(dash, hls), isLive = true)

        assertEquals(dash.url, selector.selectVideo(live)?.mediaUrl)
        assertEquals(dash.url, selector.selectAudio(live)?.mediaUrl)
        assertEquals(true, selector.selectVideo(live)?.isLive)
    }

    private fun video(resolution: String) = MediaTrack(resolution, TrackKind.VIDEO, "MPEG_4", resolution = resolution)
    private fun audio(bitrate: Int) = MediaTrack(bitrate.toString(), TrackKind.AUDIO, "M4A", bitrateKbps = bitrate)
    private fun details(
        videoTracks: List<MediaTrack> = emptyList(),
        audioTracks: List<MediaTrack> = emptyList(),
        liveTracks: List<MediaTrack> = emptyList(),
        isLive: Boolean = false
    ) = StreamDetails(0, "source", "Title", "Uploader", null, 60, videoTracks, audioTracks, liveTracks, isLive)
}
