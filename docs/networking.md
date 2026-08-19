# KMP 统一网络教程

## 1. 先记住边界

```text
app / shared_ios（组合根）
  → 创建并复用一个 NetworkClient
      Android → OkHttp
      iOS     → Foundation URLSession
  → 注入 core_data Repository 或 iOS 图片加载

core_data
  → URL、DTO、JSON 解析、领域映射

feature_*
  → 只依赖 domain-feed，不知道 HTTP 和 DTO
```

只有 `core_network` 可以直接调用 HTTP 引擎。它参考
[`liangjingkanji/Net`](https://github.com/liangjingkanji/Net) 的易用思路，但不会把
Activity、Toast、缺省页等 Android UI 行为搬进 KMP 网络层。加载状态和错误页面由
`core_state` 与 Feature 处理。

## 2. 三个文件就能读完公共网络层

按下面顺序阅读：

1. `NetworkRequest.kt`：配置、Query、请求体、请求、响应和缓存策略。
2. `NetworkException.kt`：统一错误类型。
3. `NetworkClient.kt`：URL 校验、拦截器、超时、重试和便捷 GET。

平台实现分别只有一个入口：

- Android：`AndroidNetworkClientFactory.kt`，内部使用 OkHttp。
- iOS：`IosNetworkClientFactory.kt`，内部使用 Foundation。

`NetworkEngine` 和 `NetworkClient` 构造器是模块内部实现。业务代码只能通过平台
工厂拿到客户端，因此不会意外创建一个缺少缓存或取消能力的半成品客户端。

## 3. 当前真实能力

| 能力 | API |
| --- | --- |
| GET/POST/PUT/PATCH/DELETE/HEAD/OPTIONS | `NetworkRequest.method` |
| 简单 GET 与任意方法请求 | `getText()`、`getBytes()`、`get()`、`send()` |
| 安全 Query 参数编码 | `queryParameters` |
| Binary/Text/JSON/Form 请求体 | `NetworkBody` |
| 基础域名、全局 Header、默认超时 | `NetworkConfig` |
| 单次请求超时 | `timeoutMillis` |
| 请求/响应拦截 | `NetworkInterceptor` |
| URL、HTTP、超时、传输错误 | `NetworkException` 子类 |
| 可配置方法/状态码、指数退避、`Retry-After` | `NetworkRetryPolicy` |
| 默认缓存、强制网络、只读缓存、不缓存 | `NetworkCachePolicy` |
| 缓存优先、网络失败后回退缓存 | `getCacheFirst()`、`getNetworkFirst()` |
| 网络、缓存、未知响应来源 | `NetworkResponse.source` |
| 协程和底层请求同步取消 | OkHttp `Call.cancel()` / URLSession `task.cancel()` |
| Android/iOS 共用 DTO | `core_data` 的 kotlinx.serialization |

项目没有调用方需要手动请求 ID、分组取消、竞速请求、轮询封装或全局日志事件，因此
这些能力不放进基础客户端。页面离开时取消 ViewModel/Compose scope，就会自动取消
底层请求。真正出现轮询或上传进度需求时，应围绕具体接口实现并留下对应测试。

## 4. 客户端只创建一次

Android 的 `AppContainer`：

```kotlin
private val networkClient = createAndroidNetworkClient(
    context = appContext,
    config = NetworkConfig(
        retryPolicy = NetworkRetryPolicy(maxRetries = 1)
    )
)
```

iOS 的 `IosAppController`：

```kotlin
val networkClient = remember {
    createIosNetworkClient(
        NetworkConfig(
            retryPolicy = NetworkRetryPolicy(maxRetries = 1)
        )
    )
}
```

不要在请求方法、页面重组或 Repository 中创建客户端，否则连接池和 HTTP 缓存不能
复用。

## 5. 发起请求

普通 GET 和 Query：

```kotlin
val text = networkClient.getText(
    url = "https://example.com/api/search",
    queryParameters = mapOf(
        "keyword" to "KMP & CMP",
        "page" to "1"
    )
)
val bytes = networkClient.getBytes("https://example.com/avatar.png")
val response = networkClient.get("https://example.com/api/profile")
val slowResponse = networkClient.get(
    url = "https://example.com/api/report",
    timeoutMillis = 30_000
)
```

参数会使用 UTF-8 和 RFC 3986 规则编码，不要自己拼接未编码的用户输入。

发送 JSON：

```kotlin
val response = networkClient.send(
    url = "https://example.com/api/profile",
    method = NetworkMethod.POST,
    body = NetworkBody.json("""{"nickname":"CMP"}"""),
    headers = mapOf("Accept" to "application/json"),
    cachePolicy = NetworkCachePolicy.NO_STORE
)
```

表单和其他请求体：

```kotlin
val form = NetworkBody.form(
    mapOf("username" to "cmp", "password" to password)
)
val text = NetworkBody.text("plain text")
val binary = NetworkBody.binary(fileBytes, "image/png")
```

`NetworkBody` 自动补齐正确的 `Content-Type`，显式请求 Header 可以覆盖它。
`NetworkClient` 会合并全局 Header、校验 URL、执行拦截器、应用超时和重试，并把非
2xx 响应转成 `NetworkHttpException`。

## 6. 缓存与离线回退

普通请求遵循服务端的 `Cache-Control`、`Expires`、`ETag` 等标准 HTTP 缓存协议：

```kotlin
val response = networkClient.getCacheFirst(
    "https://example.com/api/feed"
)

when (response.source) {
    NetworkResponseSource.CACHE -> Unit   // 直接命中缓存
    NetworkResponseSource.NETWORK -> Unit // 缓存未命中后访问网络
    NetworkResponseSource.UNKNOWN -> Unit // 平台没有暴露来源
}
```

- `getCacheFirst()`：先读取缓存；未命中时请求网络。
- `getNetworkFirst()`：先请求网络；遇到传输错误、超时、408、429 或 5xx 时读取缓存。
- `CACHE_ONLY` 未命中时，Android 和 iOS 都抛出 `NetworkCacheMissException`。
- 网络优先且缓存也未命中时，保留原始网络异常，方便页面展示真实失败原因。
- 401、403、404 等客户端错误不会回退到旧缓存，避免用过期数据掩盖权限或路径问题。

这不是一套私有响应数据库。服务端不允许缓存时，客户端不会绕开协议强行保存数据；
需要离线数据库的业务应在 Repository 中明确建模，而不是藏进 HTTP 客户端。Android
使用 50 MB OkHttp HTTP 缓存，iOS 使用系统 URL Cache。`DEFAULT` 请求在 iOS 上无法
可靠判断最终响应来源，因此其 `source` 可能是 `UNKNOWN`。

## 7. JSON 留在 core_data

网络层不知道业务模型：

```kotlin
@Serializable
data class ProfilePayload(
    val id: Long,
    val nickname: String
)

val payload = json.decodeFromString<ProfilePayload>(
    networkClient.getText(url)
)
```

这样 Android 与 iOS 使用同一 DTO 和解析规则，不需要维护 Retrofit 模型与
Foundation 手写解析两套实现。

## 8. 全局 Header 与 Token

使用拦截器，不要复制 `getWithToken()`、`postWithToken()`：

```kotlin
val authInterceptor = NetworkInterceptor { chain ->
    chain.proceed(
        chain.request.copy(
            headers = chain.request.headers + (
                "Authorization" to "Bearer ${tokenStore.currentToken}"
            )
        )
    )
}

createAndroidNetworkClient(
    context = context,
    interceptors = listOf(authInterceptor)
)
```

Header 名称按 HTTP 规则大小写不敏感：单次请求会覆盖同名全局 Header。
`NetworkCachePolicy.NO_STORE` 会强制使用 `Cache-Control: no-store`，不能被全局
缓存 Header 意外覆盖。Header 名称必须是 ASCII HTTP token，值只接受可见 ASCII
字符和制表符；中文等内容应放进经过编码的 Query 或请求体，不能直接塞进 Header。

刷新 Token 时要保证多个 401 不会并发刷新，并避免刷新接口再次进入同一刷新流程。

## 9. 错误与取消

```kotlin
when (error) {
    is NetworkHttpException -> error.response.statusCode
    is NetworkTimeoutException -> error.timeoutMillis
    is NetworkCacheMissException -> error.requestUrl
    is NetworkTransportException -> error.message
    is NetworkInvalidUrlException -> error.invalidUrl
}
```

Repository 返回 `Result` 时必须保留取消：

```kotlin
try {
    Result.success(load())
} catch (error: CancellationException) {
    throw error
} catch (error: Exception) {
    Result.failure(error)
}
```

`timeoutMillis` 是一次尝试的完整时间预算，由共享层统一控制；Android 的 OkHttp
不会再用另一套更短的读写超时抢先结束请求，iOS 原生超时也会映射成同一个
`NetworkTimeoutException`。如果页面或调用方在外层使用 `withTimeout`，它自己的
`TimeoutCancellationException` 会原样向外传播，不会被误报成客户端网络超时。

默认只重试 GET、PUT、DELETE、HEAD、OPTIONS；POST/PATCH 不会被自动重放。重试条件
和等待上限集中在一个策略中：

```kotlin
NetworkRetryPolicy(
    maxRetries = 2,
    initialDelayMillis = 250,
    maxDelayMillis = 2_000,
    retryStatusCodes = setOf(408, 429, 500, 502, 503, 504),
    maxRetryAfterMillis = 60_000
)
```

429/503 等响应带有数字秒格式的 `Retry-After` 时，客户端至少等待服务端要求的时长。
如果它超过 `maxRetryAfterMillis`，本次请求直接返回原始 HTTP 错误，不会长时间占用
调用方协程，也不会提前重试。HTTP-date 格式目前回退到指数退避；真实后端使用这种
格式时，再引入共享时钟和日期解析并补测试。

只有接口通过幂等键或后端协议保证可安全重放时，才显式把 POST/PATCH 加入
`retryMethods`。不要记录响应正文、Token、Cookie 或用户隐私数据。

## 10. 并发与串行

并发、串行和生命周期取消直接使用结构化协程，不再包装第二套任务 API：

```kotlin
val (profile, messages) = coroutineScope {
    val profile = async { networkClient.getText(profileUrl) }
    val messages = async { networkClient.getText(messagesUrl) }
    profile.await() to messages.await()
}
```

普通顺序调用就是串行请求。外层 scope 取消时，OkHttp Call 和 URLSessionTask 会跟随
取消。

## 11. 新增接口检查表

1. URL、DTO、JSON 解析放在 `core_data`。
2. Repository 接收已有 `NetworkClient`，Feature 只依赖领域接口。
3. Query 使用 `queryParameters`，请求体使用 `NetworkBody`，不要手写不安全拼接。
4. 敏感请求使用 `NO_STORE`，日志不记录正文和凭据。
5. 为解析、URL 白名单或新增策略留一个小测试。
6. 至少运行 `:core_network:testDebugUnitTest`、`:core_data:testDebugUnitTest`、
   iOS Simulator 编译和 `:app:assembleDebug`。
