package net.wearpipe.app.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WearPipeDatabaseTest {
    private lateinit var database: WearPipeDatabase

    @Before fun createDatabase() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WearPipeDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After fun closeDatabase() = database.close()

    @Test fun historyIsSortedNewestFirstAndCanBeCleared() = runTest {
        val dao = database.dao()
        dao.saveHistory(PlaybackHistoryEntry("old", 0, "Old", "A", null, 1_000, 10, 1))
        dao.saveHistory(PlaybackHistoryEntry("new", 0, "New", "B", null, 2_000, 20, 2))
        assertEquals(listOf("new", "old"), dao.history().first().map { it.sourceUrl })
        assertEquals(20, dao.playbackPosition("new"))
        dao.clearHistory()
        assertEquals(emptyList<PlaybackHistoryEntry>(), dao.history().first())
    }
}
