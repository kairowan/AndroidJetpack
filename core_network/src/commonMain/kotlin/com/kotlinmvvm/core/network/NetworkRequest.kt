package com.kotlinmvvm.core.network

enum class NetworkMethod {
    GET,
    POST,
    PUT,
    PATCH,
    DELETE,
    HEAD,
    OPTIONS
}

enum class NetworkCachePolicy {
    DEFAULT,
    NETWORK_ONLY,
    CACHE_ONLY,
    NO_STORE
}

enum class NetworkResponseSource {
    NETWORK,
    CACHE,
    UNKNOWN
}

data class NetworkRetryPolicy(
    val maxRetries: Int = 0,
    val initialDelayMillis: Long = 250,
    val maxDelayMillis: Long = 2_000,
    val retryStatusCodes: Set<Int> = setOf(408, 429, 500, 502, 503, 504),
    val retryMethods: Set<NetworkMethod> = setOf(
        NetworkMethod.GET,
        NetworkMethod.PUT,
        NetworkMethod.DELETE,
        NetworkMethod.HEAD,
        NetworkMethod.OPTIONS
    ),
    val maxRetryAfterMillis: Long = 60_000
) {
    init {
        require(maxRetries >= 0) { "maxRetries must not be negative" }
        require(initialDelayMillis >= 0) { "initialDelayMillis must not be negative" }
        require(maxDelayMillis >= initialDelayMillis) {
            "maxDelayMillis must be greater than or equal to initialDelayMillis"
        }
        require(maxRetryAfterMillis >= 0) {
            "maxRetryAfterMillis must not be negative"
        }
    }
}

data class NetworkConfig(
    val baseUrl: String? = null,
    val defaultHeaders: Map<String, String> = emptyMap(),
    val timeoutMillis: Long = 15_000,
    val retryPolicy: NetworkRetryPolicy = NetworkRetryPolicy()
) {
    init {
        require(timeoutMillis > 0) { "timeoutMillis must be positive" }
        defaultHeaders.requireValidHeaders()
    }
}

class NetworkBody private constructor(
    internal val content: ByteArray,
    val contentType: String
) {
    companion object {
        fun binary(
            content: ByteArray,
            contentType: String = "application/octet-stream"
        ): NetworkBody = NetworkBody(content.copyOf(), contentType.requireContentType())

        fun text(
            content: String,
            contentType: String = "text/plain; charset=utf-8"
        ): NetworkBody = NetworkBody(
            content = content.encodeToByteArray(),
            contentType = contentType.requireContentType()
        )

        fun json(content: String): NetworkBody =
            text(content, "application/json; charset=utf-8")

        fun form(parameters: Map<String, String>): NetworkBody {
            require(parameters.keys.none(String::isBlank)) {
                "form parameter names must not be blank"
            }
            return text(
                content = parameters.entries.joinToString("&") { (name, value) ->
                    "${name.encodeFormComponent()}=${value.encodeFormComponent()}"
                },
                contentType = "application/x-www-form-urlencoded; charset=utf-8"
            )
        }
    }
}

data class NetworkRequest(
    val url: String,
    val method: NetworkMethod = NetworkMethod.GET,
    val queryParameters: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val body: NetworkBody? = null,
    val timeoutMillis: Long? = null,
    val cachePolicy: NetworkCachePolicy = NetworkCachePolicy.DEFAULT
) {
    init {
        require(url.isNotBlank()) { "url must not be blank" }
        require(queryParameters.keys.none(String::isBlank)) {
            "query parameter names must not be blank"
        }
        headers.requireValidHeaders()
        require(timeoutMillis == null || timeoutMillis > 0) {
            "timeoutMillis must be positive"
        }
        require(method != NetworkMethod.GET && method != NetworkMethod.HEAD || body == null) {
            "GET and HEAD requests cannot contain a body"
        }
    }
}

data class NetworkResponse(
    val request: NetworkRequest,
    val statusCode: Int,
    val headers: Map<String, List<String>>,
    val body: ByteArray,
    val source: NetworkResponseSource = NetworkResponseSource.UNKNOWN
) {
    val isSuccessful: Boolean
        get() = statusCode in 200..299

    fun header(name: String): String? =
        headers.entries.firstOrNull { (key, _) -> key.equals(name, ignoreCase = true) }
            ?.value
            ?.firstOrNull()

    fun bodyText(): String = body.decodeToString()
}

internal fun interface NetworkEngine {
    suspend fun execute(request: NetworkRequest): NetworkResponse
}

internal fun String.encodeUrlComponent(): String = buildString {
    encodeToByteArray().forEach { byte ->
        val value = byte.toInt() and 0xff
        if (value.isUrlUnreserved()) {
            append(value.toChar())
        } else {
            append('%')
            append(HEX_DIGITS[value ushr 4])
            append(HEX_DIGITS[value and 0x0f])
        }
    }
}

private fun String.encodeFormComponent(): String =
    encodeUrlComponent().replace("%20", "+")

private fun String.requireContentType(): String =
    trim().also {
        require(it.isNotEmpty()) { "contentType must not be blank" }
        require(it.all(Char::isVisibleAscii)) {
            "contentType must contain visible ASCII characters only"
        }
    }

private fun Map<String, String>.requireValidHeaders() {
    forEach { (name, value) ->
        require(name.isNotEmpty() && name.all(Char::isHttpTokenCharacter)) {
            "invalid HTTP header name: $name"
        }
        require(value.all { it == '\t' || it.isVisibleAscii() }) {
            "invalid HTTP header value: $name"
        }
    }
}

private fun Char.isHttpTokenCharacter(): Boolean =
    this in 'a'..'z' ||
        this in 'A'..'Z' ||
        this in '0'..'9' ||
        this in HTTP_TOKEN_PUNCTUATION

private fun Char.isVisibleAscii(): Boolean = code in 0x20..0x7e

private fun Int.isUrlUnreserved(): Boolean =
    this in 'a'.code..'z'.code ||
        this in 'A'.code..'Z'.code ||
        this in '0'.code..'9'.code ||
        this == '-'.code ||
        this == '.'.code ||
        this == '_'.code ||
        this == '~'.code

private const val HEX_DIGITS = "0123456789ABCDEF"
private const val HTTP_TOKEN_PUNCTUATION = "!#$%&'*+-.^_`|~"
