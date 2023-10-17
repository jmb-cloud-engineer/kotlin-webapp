package server.http

sealed class WebResponse {
    abstract val statusCode: Int
    abstract val headers: Map<String, List<String>>
    abstract fun copyResponse(
        statusCode: Int,
        headers: Map<String, List<String>>
    ): WebResponse

    // Function makes headers lower case and puts into list to allow for
    // duplicate headers
    fun headers(): Map<String, List<String>> =
        headers.map { it.key.lowercase() to it.value }
            .fold(mapOf()) { acc, (k, v) ->
                acc.plus(
                    Pair(
                        k,
                        acc.getOrDefault(k, listOf()).plus(v)
                    )
                )
            }

    fun header(headerName: String, headerValue: String) =
        header(headerName, listOf(headerValue))

    // Overloading of previous function, also, uses a Builder pattern
    // to allow for the chaining of multiple calls to header()
    // in a single statement.
    // This is a common pattern in Kotlin.
    // https://kotlinlang.org/docs/reference/type-safe-builders.html#overloading-a-function-with-a-builder-pattern
    // https://kotlinlang.org/docs/reference/type-safe-builders.html#chaining-multiple-calls-to-a-function-with-a-builder-pattern
    // https://kotlinlang.org/docs/reference/type-safe-builders.html#overloading-a-function-with-a-builder-pattern-and-chaining-multiple-calls-to-a-function-with-a-builder-pattern
    fun header(headerName: String, headerValue: List<String>) =
        copyResponse(
            statusCode,
            headers.plus(
                Pair(
                    headerName,
                    headers.getOrDefault(headerName, listOf())
                        .plus(headerValue)
                )
            )
        )
}

// Data class inherits properties from the "sealed" (abstract) class
data class TextWebResponse(
    val body: String,
    override val statusCode: Int = 200,
    override val headers: Map<String, List<String>> = mapOf()
) : WebResponse() {
    override fun copyResponse(
        statusCode: Int,
        headers: Map<String, List<String>>
    ) =
        copy(body = body, statusCode = statusCode, headers = headers)
}

data class JsonWebResponse(
    val body: Any?,
    override val statusCode: Int = 200,
    override val headers: Map<String, List<String>> = mapOf()
) : WebResponse() {
    override fun copyResponse(
        statusCode: Int,
        headers: Map<String, List<String>>
    ) =
        copy(body = body, statusCode = statusCode, headers = headers)
}
