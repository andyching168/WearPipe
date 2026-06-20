package net.wearpipe.app.data

import net.wearpipe.app.model.SearchPage
import net.wearpipe.app.model.SearchPageToken
import net.wearpipe.app.model.StreamDetails

interface StreamingRepository {
    suspend fun search(query: String, page: SearchPageToken? = null): Result<SearchPage>
    suspend fun getStream(serviceId: Int, url: String): Result<StreamDetails>
}
