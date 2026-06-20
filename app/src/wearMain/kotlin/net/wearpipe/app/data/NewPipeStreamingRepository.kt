package net.wearpipe.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.wearpipe.app.model.MediaTrack
import net.wearpipe.app.model.SearchPage
import net.wearpipe.app.model.SearchPageToken
import net.wearpipe.app.model.SearchResult
import net.wearpipe.app.model.StreamDetails
import net.wearpipe.app.model.TrackKind
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.Page
import org.schabi.newpipe.extractor.StreamingService
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization
import org.schabi.newpipe.extractor.search.SearchInfo
import org.schabi.newpipe.extractor.stream.StreamInfo
import org.schabi.newpipe.extractor.stream.StreamInfoItem
import org.schabi.newpipe.extractor.stream.StreamType

class NewPipeStreamingRepository : StreamingRepository {
    private val youtube: StreamingService get() = NewPipe.getService(0)

    override suspend fun search(query: String, page: SearchPageToken?): Result<SearchPage> =
        runCatching {
            withContext(Dispatchers.IO) {
                val link = youtube.searchQHFactory.fromQuery(query, listOf("videos"), "")
                val result = if (page == null) {
                    SearchInfo.getInfo(youtube, link).let { it.relatedItems to it.nextPage }
                } else {
                    SearchInfo.getMoreItems(youtube, link, page.value as Page)
                        .let { it.items to it.nextPage }
                }
                SearchPage(
                    items = result.first.filterIsInstance<StreamInfoItem>().map { item ->
                        SearchResult(
                            serviceId = item.serviceId,
                            url = item.url,
                            title = item.name,
                            uploader = item.uploaderName.orEmpty(),
                            thumbnailUrl = item.thumbnails.firstOrNull()?.url,
                            durationSeconds = item.duration
                        )
                    },
                    nextPage = result.second?.takeIf(Page::isValid)?.let(::SearchPageToken)
                )
            }
        }

    override suspend fun getStream(serviceId: Int, url: String): Result<StreamDetails> =
        runCatching {
            withContext(Dispatchers.IO) {
                val info = StreamInfo.getInfo(NewPipe.getService(serviceId), url)
                info.toDetails()
            }
        }

    private fun StreamInfo.toDetails() = StreamDetails(
        serviceId = serviceId,
        url = url,
        title = name,
        uploader = uploaderName.orEmpty(),
        thumbnailUrl = thumbnails.firstOrNull()?.url,
        durationSeconds = duration,
        videoTracks = videoStreams.mapNotNull { stream ->
            stream.content?.takeIf(String::isNotBlank)?.let {
                MediaTrack(it, TrackKind.VIDEO, stream.format?.name, resolution = stream.resolution)
            }
        },
        audioTracks = audioStreams.mapNotNull { stream ->
            stream.content?.takeIf(String::isNotBlank)?.let {
                MediaTrack(it, TrackKind.AUDIO, stream.format?.name, bitrateKbps = stream.averageBitrate)
            }
        },
        liveTracks = buildList {
            dashMpdUrl.takeIf(String::isNotBlank)?.let {
                add(MediaTrack(it, TrackKind.VIDEO, "DASH", mimeType = "application/dash+xml", isLive = true))
            }
            hlsUrl.takeIf(String::isNotBlank)?.let {
                add(MediaTrack(it, TrackKind.VIDEO, "HLS", mimeType = "application/x-mpegURL", isLive = true))
            }
        },
        isLive = streamType == StreamType.LIVE_STREAM || streamType == StreamType.AUDIO_LIVE_STREAM
    )

    companion object {
        fun initialize(downloader: org.schabi.newpipe.extractor.downloader.Downloader) {
            NewPipe.init(downloader, Localization.DEFAULT, ContentCountry.DEFAULT)
        }
    }
}
