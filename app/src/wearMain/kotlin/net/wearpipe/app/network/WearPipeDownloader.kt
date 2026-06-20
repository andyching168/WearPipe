package net.wearpipe.app.network

import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.io.IOException
import java.util.concurrent.TimeUnit

class WearPipeDownloader(
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()
) : Downloader() {
    override fun execute(request: Request): Response {
        val body = request.dataToSend()?.toRequestBody()
        val builder = okhttp3.Request.Builder()
            .url(request.url())
            .method(request.httpMethod(), body)
            .header("User-Agent", USER_AGENT)
        request.headers().forEach { (name, values) ->
            builder.removeHeader(name)
            values.forEach { builder.addHeader(name, it) }
        }
        client.newCall(builder.build()).execute().use { response ->
            if (response.code == 429) throw ReCaptchaException("YouTube requested a challenge", request.url())
            return Response(
                response.code,
                response.message,
                response.headers.toMultimap(),
                response.body.string(),
                response.request.url.toString()
            )
        }
    }

    companion object {
        const val USER_AGENT =
            "Mozilla/5.0 (Linux; Android 14; Wear OS) AppleWebKit/537.36 Chrome/140.0 Mobile Safari/537.36"
    }
}
