package server.mappers

import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.http.content.*

class KtorJsonWebResponse(
    val body: Any?,
    override val status: HttpStatusCode = HttpStatusCode.OK
) : OutgoingContent.ByteArrayContent() {
    override val contentType: ContentType =
        ContentType.Application.Json.withCharset(Charsets.UTF_8)

    override fun bytes() = Gson().toJson(body).toByteArray(
        Charsets.UTF_8
    )
}