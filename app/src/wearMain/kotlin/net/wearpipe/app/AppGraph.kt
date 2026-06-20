package net.wearpipe.app

import android.content.Context
import net.wearpipe.app.data.HistoryRepository
import net.wearpipe.app.data.NewPipeStreamingRepository
import net.wearpipe.app.data.RoomHistoryRepository
import net.wearpipe.app.data.StreamSelector
import net.wearpipe.app.data.StreamingRepository
import net.wearpipe.app.data.local.WearPipeDatabase
import net.wearpipe.app.network.WearPipeDownloader
import net.wearpipe.app.playback.Media3PlaybackRepository
import net.wearpipe.app.playback.PlaybackRepository

class AppGraph(context: Context) {
    val database = WearPipeDatabase.create(context)
    val streamingRepository: StreamingRepository = NewPipeStreamingRepository()
    val historyRepository: HistoryRepository = RoomHistoryRepository(database.dao())
    val streamSelector = StreamSelector()
    val playbackRepository: PlaybackRepository = Media3PlaybackRepository(context)

    init {
        NewPipeStreamingRepository.initialize(WearPipeDownloader())
    }
}
