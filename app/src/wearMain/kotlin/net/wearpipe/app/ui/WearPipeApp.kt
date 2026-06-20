@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package net.wearpipe.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.focusable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.rotary.onRotaryScrollEvent
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.wear.compose.foundation.lazy.TransformingLazyColumn
import androidx.wear.compose.foundation.lazy.TransformingLazyColumnScope
import androidx.wear.compose.foundation.lazy.rememberTransformingLazyColumnState
import androidx.wear.compose.material3.AppScaffold
import androidx.wear.compose.material3.Button
import androidx.wear.compose.material3.FilledIconButton
import androidx.wear.compose.material3.Icon
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.ScreenScaffold
import androidx.wear.compose.material3.Text
import androidx.wear.compose.material3.TimeText
import androidx.wear.compose.navigation3.rememberSwipeDismissableSceneStrategy
import coil3.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.wearpipe.app.AppGraph
import net.wearpipe.app.R
import net.wearpipe.app.data.local.PlaybackHistoryEntry
import net.wearpipe.app.data.local.QueueEntry
import net.wearpipe.app.model.SearchPageToken
import net.wearpipe.app.model.SearchResult
import net.wearpipe.app.model.StreamDetails
import net.wearpipe.app.navigation.Destination
import net.wearpipe.app.playback.PlaybackUiState

@Composable
fun WearPipeApp(graph: AppGraph) {
    val backStack = rememberNavBackStack(Destination.Home)
    MaterialTheme {
        AppScaffold(timeText = { TimeText() }) {
            NavDisplay(
                backStack = backStack,
                sceneStrategies = listOf(rememberSwipeDismissableSceneStrategy()),
                onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                entryProvider = entryProvider {
                    entry<Destination.Home> {
                        HomeScreen(
                            graph,
                            open = { backStack.add(it) }
                        )
                    }
                    entry<Destination.Search> { destination ->
                        SearchScreen(graph, destination.query) {
                            backStack.add(Destination.Details(it.serviceId, it.url))
                        }
                    }
                    entry<Destination.Details> { destination ->
                        DetailsScreen(graph, destination.serviceId, destination.url) {
                            backStack.add(Destination.Player)
                        }
                    }
                    entry<Destination.Player> { PlayerScreen(graph) }
                    entry<Destination.Queue> { QueueScreen(graph) }
                    entry<Destination.History> {
                        HistoryScreen(graph) { backStack.add(Destination.Details(it.serviceId, it.sourceUrl)) }
                    }
                    entry<Destination.Settings> { SettingsScreen(graph) }
                }
            )
        }
    }
}

@Composable
private fun WearList(content: TransformingLazyColumnScope.() -> Unit) {
    val state = rememberTransformingLazyColumnState()
    ScreenScaffold(scrollState = state) { padding ->
        TransformingLazyColumn(
            state = state,
            contentPadding = PaddingValues(top = padding.calculateTopPadding() + 8.dp, bottom = 28.dp),
            modifier = Modifier.fillMaxSize(),
            content = content
        )
    }
}

@Composable
private fun HomeScreen(graph: AppGraph, open: (Destination) -> Unit) {
    val searches by graph.historyRepository.searches.collectAsState(initial = emptyList())
    val playback by graph.playbackRepository.state.collectAsStateWithLifecycle()
    WearList {
        item { ScreenTitle("WearPipe") }
        item { NavButton(stringResource(R.string.search_youtube), Icons.Default.Search) { open(Destination.Search()) } }
        if (playback.title.isNotBlank()) {
            item { NavButton(stringResource(R.string.continue_playing, playback.title), Icons.Default.PlayArrow) { open(Destination.Player) } }
        }
        item { NavButton(stringResource(R.string.playback_history), Icons.Default.History) { open(Destination.History) } }
        item { NavButton(stringResource(R.string.playback_queue, playback.queueSize), Icons.Default.QueueMusic) { open(Destination.Queue) } }
        item { NavButton(stringResource(R.string.settings), Icons.Default.Settings) { open(Destination.Settings) } }
        if (searches.isNotEmpty()) item { SectionLabel(stringResource(R.string.recent_searches)) }
        items(searches.size) { index ->
            NavButton(searches[index].query, Icons.Default.Search) {
                open(Destination.Search(searches[index].query))
            }
        }
    }
}

@Composable
private fun SearchScreen(graph: AppGraph, initialQuery: String, openDetails: (SearchResult) -> Unit) {
    val scope = rememberCoroutineScope()
    var query by remember { mutableStateOf(initialQuery) }
    var items by remember { mutableStateOf(emptyList<SearchResult>()) }
    var nextPage by remember { mutableStateOf<SearchPageToken?>(null) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val networkError = stringResource(R.string.network_error)
    val loadError = stringResource(R.string.load_error)

    fun search(next: Boolean = false) {
        if (query.isBlank() || loading) return
        scope.launch {
            loading = true
            error = null
            if (!next) graph.historyRepository.recordSearch(query)
            graph.streamingRepository.search(query.trim(), if (next) nextPage else null)
                .onSuccess {
                    items = if (next) items + it.items else it.items
                    nextPage = it.nextPage
                }
                .onFailure { error = friendlyError(it, networkError, loadError) }
            loading = false
        }
    }
    LaunchedEffect(initialQuery) { if (initialQuery.isNotBlank()) search() }

    WearList {
        item { ScreenTitle(stringResource(R.string.search)) }
        item {
            Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    modifier = Modifier.weight(1f).background(Color(0xFF262626), MaterialTheme.shapes.medium).padding(12.dp)
                )
                Spacer(Modifier.width(4.dp))
                FilledIconButton(onClick = { search() }, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Search, contentDescription = stringResource(R.string.search))
                }
            }
        }
        if (loading) item { CenterMessage(stringResource(R.string.loading)) }
        error?.let { message -> item { MessageButton(message, stringResource(R.string.retry)) { search() } } }
        if (!loading && error == null && items.isEmpty() && query.isNotBlank()) item { CenterMessage(stringResource(R.string.no_results)) }
        items(items.size, key = { items[it].url }) { index -> ResultButton(items[index], openDetails) }
        if (nextPage != null && !loading) item { MessageButton(stringResource(R.string.more_results), stringResource(R.string.load_more)) { search(true) } }
    }
}

@Composable
private fun DetailsScreen(graph: AppGraph, serviceId: Int, url: String, openPlayer: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val maxHeight = remember { context.getSharedPreferences("wearpipe", 0).getInt("max_video_height", 240) }
    val scope = rememberCoroutineScope()
    var details by remember(url) { mutableStateOf<StreamDetails?>(null) }
    var error by remember(url) { mutableStateOf<String?>(null) }
    var loading by remember(url) { mutableStateOf(true) }
    var resumePosition by remember(url) { mutableStateOf(0L) }
    val networkError = stringResource(R.string.network_error)
    val loadError = stringResource(R.string.load_error)
    val noVideo = stringResource(R.string.no_video_stream)
    val noAudio = stringResource(R.string.no_audio_stream)
    fun load() {
        scope.launch {
            loading = true
            error = null
            graph.streamingRepository.getStream(serviceId, url)
                .onSuccess { details = it }
                .onFailure { error = friendlyError(it, networkError, loadError) }
            resumePosition = graph.historyRepository.playbackPosition(url)
            loading = false
        }
    }
    LaunchedEffect(url) { load() }
    WearList {
        item { ScreenTitle(stringResource(R.string.video)) }
        if (loading) item { CenterMessage(stringResource(R.string.loading_stream)) }
        error?.let { item { MessageButton(it, stringResource(R.string.retry)) { load() } } }
        details?.let { stream ->
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                    AsyncImage(stream.thumbnailUrl, stream.title, Modifier.fillMaxWidth().height(82.dp))
                    Text(stream.title, maxLines = 3, overflow = TextOverflow.Ellipsis)
                    Text(stream.uploader, style = MaterialTheme.typography.labelSmall, color = Color.LightGray)
                }
            }
            item {
                NavButton(stringResource(R.string.play_video), Icons.Default.VideoFile) {
                    graph.streamSelector.selectVideo(stream, maxHeight)?.let {
                        graph.playbackRepository.play(it, startPositionMs = resumePosition)
                        openPlayer()
                    } ?: run { error = noVideo }
                }
            }
            item {
                NavButton(stringResource(R.string.play_audio), Icons.Default.AudioFile) {
                    graph.streamSelector.selectAudio(stream)?.let {
                        graph.playbackRepository.play(it, startPositionMs = resumePosition)
                        openPlayer()
                    } ?: run { error = noAudio }
                }
            }
            item {
                NavButton(stringResource(R.string.enqueue), Icons.Default.Add) {
                    graph.streamSelector.selectVideo(stream, maxHeight)?.let(graph.playbackRepository::enqueue)
                }
            }
        }
    }
}

@Composable
private fun PlayerScreen(graph: AppGraph) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val maxHeight = remember { context.getSharedPreferences("wearpipe", 0).getInt("max_video_height", 240) }
    val state by graph.playbackRepository.state.collectAsStateWithLifecycle()
    var displayedPosition by remember(state.positionMs) { mutableStateOf(state.positionMs) }
    var volume by remember { mutableFloatStateOf(0.7f) }
    var switching by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var volumeHudVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(state.isPlaying, state.positionMs) {
        displayedPosition = state.positionMs
        while (state.isPlaying) { delay(1_000); displayedPosition += 1_000 }
    }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    LaunchedEffect(controlsVisible, state.isPlaying) {
        if (controlsVisible && state.isPlaying) {
            delay(3_000)
            controlsVisible = false
        }
    }
    LaunchedEffect(volumeHudVisible) {
        if (volumeHudVisible) {
            delay(1_500)
            volumeHudVisible = false
        }
    }

    Box(
        Modifier.fillMaxSize().background(Color.Black)
            .focusRequester(focusRequester).focusable()
            .onRotaryScrollEvent {
                volume = (volume - it.verticalScrollPixels / 600f).coerceIn(0f, 1f)
                graph.playbackRepository.setVolume(volume)
                volumeHudVisible = true
                true
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    // 上滑 dragAmount < 0 → 增加音量；下滑 dragAmount > 0 → 降低音量
                    volume = (volume - dragAmount / (size.height * 0.8f)).coerceIn(0f, 1f)
                    graph.playbackRepository.setVolume(volume)
                    volumeHudVisible = true
                }
            }
    ) {
        if (state.video) {
            PlayerSurface(Modifier.fillMaxSize())
        } else {
            AsyncImage(
                model = state.artworkUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.42f)))
        }
        Box(Modifier.fillMaxSize().clickable { controlsVisible = !controlsVisible })
        // 音量 HUD
        AnimatedVisibility(
            visible = volumeHudVisible,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 10.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.72f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.height(4.dp))
                // 音量條（垂直）
                Box(
                    Modifier
                        .width(8.dp)
                        .height(80.dp)
                        .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(4.dp))
                ) {
                    Box(
                        Modifier
                            .align(Alignment.BottomCenter)
                            .width(8.dp)
                            .fillMaxHeight(volume)
                            .background(Color.White, RoundedCornerShape(4.dp))
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${(volume * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White
                )
            }
        }
        AnimatedVisibility(visible = controlsVisible, enter = fadeIn(), exit = fadeOut()) {
            Box(Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp)) {
                Column(
                    Modifier.align(Alignment.TopCenter)
                        .background(Color.Black.copy(alpha = 0.68f), MaterialTheme.shapes.large)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.title.ifBlank { stringResource(R.string.nothing_playing) }, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${formatTime(displayedPosition)} / ${formatTime(state.durationMs)}", style = MaterialTheme.typography.labelSmall)
                }
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledIconButton(onClick = graph.playbackRepository::skipPrevious, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.ArrowBack, stringResource(R.string.previous))
                    }
                    Spacer(Modifier.width(4.dp))
                    FilledIconButton(onClick = graph.playbackRepository::togglePlayPause, modifier = Modifier.size(48.dp)) {
                        Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow, stringResource(R.string.play_pause))
                    }
                    Spacer(Modifier.width(4.dp))
                    FilledIconButton(onClick = graph.playbackRepository::skipNext, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.ArrowForward, stringResource(R.string.next))
                    }
                }
                FilledIconButton(
                    onClick = {
                        val source = state.sourceUrl ?: return@FilledIconButton
                        controlsVisible = true
                        scope.launch {
                            switching = true
                            android.widget.Toast.makeText(
                                context,
                                context.getString(R.string.switching),
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                            graph.streamingRepository.getStream(0, source).onSuccess { details ->
                                val media = if (state.video) graph.streamSelector.selectAudio(details)
                                else graph.streamSelector.selectVideo(details, maxHeight)
                                media?.let { graph.playbackRepository.play(it, startPositionMs = displayedPosition) }
                            }
                            switching = false
                        }
                    },
                    modifier = Modifier.align(Alignment.BottomCenter).size(48.dp)
                ) {
                    Icon(
                        if (state.video) Icons.Default.AudioFile else Icons.Default.VideoFile,
                        if (state.video) stringResource(R.string.switch_audio) else stringResource(R.string.switch_video)
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueScreen(graph: AppGraph) {
    val queue by graph.database.dao().queue().collectAsState(initial = emptyList())
    val playback by graph.playbackRepository.state.collectAsStateWithLifecycle()
    WearList {
        item { ScreenTitle(stringResource(R.string.queue_title)) }
        if (queue.isEmpty()) item { CenterMessage(stringResource(R.string.empty_queue)) }
        items(queue.size, key = { queue[it].id }) { index ->
            val entry = queue[index]
            Button(
                onClick = { graph.playbackRepository.playAt(index) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(56.dp),
                label = { Text(if (index == playback.currentIndex) "▶ ${entry.title}" else entry.title, maxLines = 2) },
                icon = { Icon(Icons.Default.PlayArrow, "播放") }
            )
            Button(
                onClick = { graph.playbackRepository.removeAt(index) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(48.dp),
                label = { Text(stringResource(R.string.remove_queue)) },
                icon = { Icon(Icons.Default.Delete, null) }
            )
        }
    }
}

@Composable
private fun HistoryScreen(graph: AppGraph, open: (PlaybackHistoryEntry) -> Unit) {
    val history by graph.historyRepository.history.collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    WearList {
        item { ScreenTitle(stringResource(R.string.playback_history)) }
        if (history.isNotEmpty()) item { MessageButton(stringResource(R.string.history_count, history.size), stringResource(R.string.clear_all)) { scope.launch { graph.historyRepository.clearHistory() } } }
        if (history.isEmpty()) item { CenterMessage(stringResource(R.string.empty_history)) }
        items(history.size, key = { history[it].sourceUrl }) { index ->
            val entry = history[index]
            Button(
                onClick = { open(entry) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(62.dp),
                label = { Text(entry.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
                secondaryLabel = { Text("${formatTime(entry.positionMs)} · ${entry.uploader}", maxLines = 1) }
            )
        }
    }
}

@Composable
private fun SettingsScreen(graph: AppGraph) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = remember { context.getSharedPreferences("wearpipe", 0) }
    var quality by remember { mutableStateOf(preferences.getInt("max_video_height", 240)) }
    WearList {
        item { ScreenTitle(stringResource(R.string.settings)) }
        item { SectionLabel(stringResource(R.string.quality_limit)) }
        listOf(144, 240).forEach { value ->
            item {
                NavButton(if (quality == value) "✓ ${value}p" else "${value}p", Icons.Default.VideoFile) {
                    quality = value
                    preferences.edit().putInt("max_video_height", value).apply()
                }
            }
        }
        item { SectionLabel("WearPipe 0.1.0 · GPLv3") }
        item { CenterMessage(stringResource(R.string.powered_by)) }
    }
}

@Composable
private fun ScreenTitle(text: String) = Text(text, style = MaterialTheme.typography.titleMedium, modifier = Modifier.fillMaxWidth().padding(8.dp))

@Composable
private fun SectionLabel(text: String) = Text(text, color = Color.LightGray, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(8.dp))

@Composable
private fun CenterMessage(text: String) = Text(text, modifier = Modifier.fillMaxWidth().padding(12.dp), color = Color.LightGray)

@Composable
private fun NavButton(text: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(54.dp),
        icon = { Icon(icon, null) },
        label = { Text(text, maxLines = 2, overflow = TextOverflow.Ellipsis) }
    )
}

@Composable
private fun ResultButton(result: SearchResult, onClick: (SearchResult) -> Unit) {
    Button(
        onClick = { onClick(result) },
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(68.dp),
        icon = { AsyncImage(result.thumbnailUrl, null, Modifier.size(42.dp)) },
        label = { Text(result.title, maxLines = 2, overflow = TextOverflow.Ellipsis) },
        secondaryLabel = { Text("${result.uploader} · ${formatTime(result.durationSeconds * 1_000)}", maxLines = 1) }
    )
}

@Composable
private fun MessageButton(message: String, action: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).height(58.dp),
        label = { Text(message, maxLines = 2) },
        secondaryLabel = { Text(action) }
    )
}

private fun friendlyError(error: Throwable, networkError: String, loadError: String): String = when (error) {
    is java.io.IOException -> networkError
    else -> error.localizedMessage?.take(80) ?: loadError
}

private fun formatTime(milliseconds: Long): String {
    val seconds = (milliseconds / 1_000).coerceAtLeast(0)
    return "%d:%02d".format(seconds / 60, seconds % 60)
}
