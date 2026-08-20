# ComposeScaffold 使用教程

[返回项目说明](../README.md)

Home、Shorts、Feed 和开眼接口只是可删除的示例。本教程默认走最短接入路径：先复用现有模块和公共能力，只有出现独立业务、团队或交付边界时才增加模块和层级。

## 先判断需要改多少

| 需求 | 最小改动 |
| --- | --- |
| 在现有业务中增加静态页面 | Destination、Entry、Screen |
| 页面存在业务状态或异步任务 | 再增加 UiState、ViewModel、Route |
| 现有业务增加接口 | 修改现有 domain/data，不新增 Gradle 模块 |
| 增加第二个 BaseURL | AppNetworkEndpoints 增加一个 Endpoint |
| 新增底部导航 | 路由、AppTopLevelDestination 配置项、Destination 注册 |
| 新业务仍由同一团队维护 | 小项目继续使用现有 domain/data，通过包区分业务 |
| 独立团队、独立交付或明显编译边界 | 再创建新的 feature/domain/data 模块 |

页面不等于模块，接口也不等于模块。一个商品业务可以在同一组 domain/data 中同时包含列表、详情、搜索和收藏；不要为每个页面或接口重复创建模块。

## 第一次改造脚手架

### 1. 只修改必要的项目标识

首次接入通常只需要修改：

- `app/build.gradle.kts` 的 `applicationId`。
- `app/src/main/res/values/strings.xml` 的应用名称。
- `settings.gradle.kts` 的 `rootProject.name`。

`namespace`、Application 类名和 Manifest 入口可以继续使用，只有项目确实需要统一包名时再通过 Android Studio Rename 一次性重构。

### 2. 复用或重命名示例模块

不要先创建 `module-feature-profile / module-domain-account / module-data-account`。优先选择：

- 与首页职责相近：直接改造 `module-feature-home`。
- 仍需要详情页：改造 `module-feature-detail`。
- 不需要短视频：删除 `module-feature-shorts` 和对应导航。
- 主要业务不再是 Feed：将 `module-domain-feed / module-data-feed` 重命名为真实业务名，或者小项目使用 `module-domain-app / module-data-app`。

只有当第二个业务域需要独立团队维护、独立发布或产生明显编译隔离价值时，才新增另一组 domain/data。

## 修改底部导航

顶层导航现在由 `AppTopLevelDestination` 统一提供路由、文字和图标。BottomBar、NavigationRail 和独立返回栈都会读取这份配置。

假设把首页和短视频替换为“工作台”和“我的”。

### 1. 创建路由

`DashboardDestination.kt`：

```kotlin
@Serializable
data object DashboardDestination : AppRoute
```

`ProfileDestination.kt`：

```kotlin
@Serializable
data object ProfileDestination : AppRoute
```

### 2. 修改唯一顶层导航配置

在 `AppTopLevelDestination.kt` 中替换枚举项：

```kotlin
internal enum class AppTopLevelDestination(
    val route: AppRoute,
    @param:StringRes val labelResId: Int,
    val icon: ImageVector
) {
    DASHBOARD(
        DashboardDestination,
        R.string.navigation_dashboard,
        Icons.Default.Home
    ),
    PROFILE(
        ProfileDestination,
        R.string.navigation_profile,
        Icons.Default.Person
    )
}
```

新增第三个底部导航时，只增加一个枚举项。无需再修改：

- `AppNavigationState` 的路由白名单。
- BottomBar。
- NavigationRail。
- 顶层返回栈 Map。
- 可见 Entry 的判断。

### 3. 注册目的地

每个 Feature 仍保留一个独立注册函数：

```kotlin
internal fun EntryProviderScope<NavKey>.registerProfileDestination(
    accountRepository: AccountRepository,
    taskObserver: ViewModelTaskObserver
) {
    entry<ProfileDestination> {
        ProfileRoute(
            accountRepository = accountRepository,
            taskObserver = taskObserver
        )
    }
}
```

最后在 `AppNavHost` 的 `entryProvider` 增加一次注册：

```kotlin
val provider = entryProvider<NavKey> {
    registerDashboardDestination(...)
    registerProfileDestination(accountRepository, taskObserver)
}
```

在 `strings.xml` 增加 `navigation_dashboard` 和 `navigation_profile` 即可。

## 在现有 Feature 增加页面

### 无业务状态的页面

静态说明页、协议页或只持有局部展开动画的页面，不需要 UiState 和 ViewModel。

`AboutDestination.kt`：

```kotlin
@Serializable
data object AboutDestination : AppRoute
```

`AboutDestinationEntry.kt`：

```kotlin
internal fun EntryProviderScope<NavKey>.registerAboutDestination(
    onBack: () -> Unit
) {
    entry<AboutDestination> {
        AboutScreen(onBack = onBack)
    }
}
```

`AboutScreen.kt`：

```kotlin
@Composable
fun AboutScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = stringResource(R.string.action_back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Text(
            text = stringResource(R.string.about_content),
            modifier = Modifier.padding(paddingValues)
        )
    }
}
```

页面属于现有 Feature 时直接放进该模块，不修改 `settings.gradle.kts` 和 `app/build.gradle.kts`。

### 有业务状态的页面

只有页面存在异步请求、可恢复业务状态或跨组件业务交互时，才增加：

```text
feature-existing/
└─ src/main/java/.../
   ├─ navigation/FeatureRoute.kt
   ├─ presentation/FeatureUiState.kt
   ├─ presentation/FeatureViewModel.kt
   └─ ui/FeatureScreen.kt
```

- Route 创建 ViewModel、收集 `uiState` 并连接导航回调。
- ViewModel 管理业务状态和异步任务。
- Screen 只接收不可变状态与回调。
- 滚动、动画、控件展开等局部状态继续留在 Composable。

## 在现有业务增加一个普通接口

下面以“读取个人资料”为例。默认复用已有 domain/data，不创建新模块，也不创建业务 RemoteDataSource 和 Bindings。

### 1. domain 中增加模型、错误和 Repository

`UserProfile.kt`：

```kotlin
data class UserProfile(
    val id: String,
    val displayName: String
)
```

`AccountLoadError.kt`：

```kotlin
enum class AccountLoadError {
    NO_CONNECTION,
    TIMEOUT,
    UNAUTHORIZED,
    INVALID_RESPONSE,
    UNKNOWN
}
```

`AccountRepository.kt`：

```kotlin
interface AccountRepository :
    CommonRepository<Unit, DataResult<UserProfile, AccountLoadError>>
```

公共 `DataResult` 已经提供 `DataSuccess` 和 `DataFailure`，不需要每个业务重复创建 Result、Success、Failure 三个文件。

### 2. data 中增加 DTO 和 Service

`AccountProfileDto.kt`：

```kotlin
internal data class AccountProfileDto(
    val id: String?,
    val nickname: String?
)
```

`AccountApiService.kt`：

```kotlin
internal interface AccountApiService {
    @GET("v1/profile")
    suspend fun profile(): Response<AccountProfileDto>
}
```

`AccountProfileMapper.kt`：

```kotlin
internal fun AccountProfileDto.toDomain(): UserProfile? {
    val validId = id?.takeIf(String::isNotBlank) ?: return null
    val validName = nickname?.takeIf(String::isNotBlank) ?: return null
    return UserProfile(validId, validName)
}
```

### 3. Repository 直接组合公共网络执行器

只有一个远程来源时，不需要 `AccountRemoteDataSource` 和 `RetrofitAccountRemoteDataSource`：

```kotlin
internal class DefaultAccountRepository(
    private val service: AccountApiService,
    private val networkDataSource: NetworkDataSource
) : AccountRepository {

    override suspend fun load(
        params: Unit
    ): DataResult<UserProfile, AccountLoadError> =
        when (val result = networkDataSource.execute(service::profile)) {
            is NetworkSuccess -> result.value.toDomain()
                ?.let(::DataSuccess)
                ?: DataFailure(AccountLoadError.INVALID_RESPONSE)

            is NetworkError -> DataFailure(
                result.error.toAccountLoadError()
            )
        }
}
```

网络失败到业务错误的 Mapper 仍放在 data 中，Feature 不解析 HTTP code 或异常文本。

### 4. 装配函数直接返回 Repository

只有一个 Repository 角色时，不需要额外的 Bindings 类：

```kotlin
fun createAccountRepository(
    networkClientFactory: NetworkClientFactory,
    endpoint: NetworkEndpoint,
    networkFailureObserver: NetworkFailureObserver = NetworkFailureObserver.None
): AccountRepository {
    val service = networkClientFactory.createService(
        endpoint,
        AccountApiService::class.java
    )
    return DefaultAccountRepository(
        service = service,
        networkDataSource = RetrofitNetworkDataSource(
            failureObserver = networkFailureObserver
        )
    )
}
```

在 `AppDependencies` 增加：

```kotlin
val accountRepository: AccountRepository
```

在 `AppContainer` 只装配一次：

```kotlin
override val accountRepository: AccountRepository by lazy {
    createAccountRepository(
        networkClientFactory = networkClientFactory,
        endpoint = AppNetworkEndpoints.account(environment),
        networkFailureObserver = diagnosticObserver
    )
}
```

### 5. ViewModel 发起请求

```kotlin
class ProfileViewModel(
    private val accountRepository: AccountRepository,
    taskObserver: ViewModelTaskObserver = ViewModelTaskObserver.None
) : BaseViewModel<ProfileUiState>(ProfileUiState(), taskObserver) {

    init {
        loadProfile()
    }

    fun retry() = loadProfile()

    private fun loadProfile() {
        taskFlow { accountRepository.load(Unit) }
            .onEach { result ->
                updateState { state ->
                    when (result) {
                        is DataSuccess -> state.copy(
                            profile = result.value,
                            error = null
                        )
                        is DataFailure -> state.copy(error = result.error)
                    }
                }
            }
            .launchLatestIn(
                taskKey = TASK_LOAD_PROFILE,
                onLoadingChanged = { loading ->
                    updateState { state -> state.copy(isLoading = loading) }
                }
            )
    }

    private companion object {
        const val TASK_LOAD_PROFILE = "profile.load"
    }
}
```

这条链路只有一次请求，没有刷新和下一页。只有真实需要相应能力时，才在 `AccountRepository` 增加 `refreshProfile()`、`saveProfile()` 等明确函数。

## 接入 SSE 与 WebSocket

两种长连接都从已有 `NetworkClientFactory` 创建，不要新建 OkHttpClient，也不要在
Composable 中直接持有连接。它们会复用 Endpoint 白名单、认证头、连接池和 Debug
日志；页面协程取消时，SSE Call 或 WebSocket 会同步释放。

SSE 在 data 层返回 Flow：

```kotlin
val events = networkClientFactory
    .createSseClient(endpoint)
    .events(
        relativeUrl = "v1/updates",
        lastEventId = savedEventId
    )

events.collect { event ->
    when (event) {
        is SseMessage -> saveAndApply(event.id, event.type, event.data)
        is SseRetry -> updateReconnectDelay(event.delayMillis)
    }
}
```

WebSocket 会话由一个 owner 收集事件；`send()` 返回 `false` 表示连接正在关闭、已经
关闭，或 OkHttp 的发送队列达到上限，不能忽略返回值：

```kotlin
val session = networkClientFactory
    .createWebSocketClient(endpoint)
    .connect("v1/chat")

try {
    session.events.collect { event ->
        when (event) {
            NetworkWebSocketOpen -> session.send("hello")
            is NetworkWebSocketText -> receive(event.value)
            is NetworkWebSocketBinary -> receive(event.value)
            is NetworkWebSocketClosing -> Unit
            is NetworkWebSocketClosed -> Unit
            is NetworkWebSocketFailure -> handle(event.failure)
        }
    }
} finally {
    session.cancel()
}
```

`NetworkConfig.webSocketPingIntervalSeconds` 默认 30 秒，设置为 0 可关闭传输层 Ping。
WebSocket 建连仍受普通 `callTimeoutSeconds` 限制，升级成功后才进入无整次超时的长连接。
SSE 收到 HTTP 204 时会正常结束，表示服务端要求停止当前事件流。
基础层不会自动重连：是否可重放订阅、如何恢复 Last-Event-ID、鉴权失效和退避上限都
属于具体业务协议，应在 data/Repository 中实现并测试，避免默认重连造成重复消息或
请求风暴。

## 新增第二个 BaseURL

在 `AppNetworkEndpoints` 增加一个函数：

```kotlin
fun account(environment: AppEnvironment) = NetworkEndpoint(
    name = "account-${environment.name.lowercase()}",
    baseUrl = when (environment) {
        AppEnvironment.DEVELOPMENT -> "https://dev-api.example.com/"
        AppEnvironment.STAGING -> "https://staging-api.example.com/"
        AppEnvironment.PRODUCTION -> "https://api.example.com/"
    }
)
```

然后把该 Endpoint 传给对应 Repository 装配函数。不同 Endpoint 共享同一个 OkHttp 连接池，不需要复制 Network 模块。

接口返回头像、图片或文件 URL 时，才在 `AppRemoteUrlPolicies` 登记真实 CDN Host；接口只返回普通 JSON 时不需要新增媒体策略。

## 什么时候才增加额外层级

| 层级 | 增加条件 |
| --- | --- |
| ViewModel + UiState | 页面存在业务状态、异步任务或进程恢复需求 |
| Route | 需要创建 ViewModel、注入依赖或适配导航回调 |
| Feature 模块 | 独立业务边界、独立交付或多人并行需要编译隔离 |
| domain/data 新模块 | 新业务需要被多个 Feature 使用，或者已有模块职责明显失控 |
| 业务 RemoteDataSource | Repository 需要协调多个服务、本地与远程来源，或远程能力被多个 Repository 复用 |
| Bindings | 一个装配入口需要同时返回多个 Repository 角色 |
| LocalDataSource | 真实存在缓存、数据库或文件持久化 |
| UseCase | 同一段业务编排被多个 ViewModel 或入口复用 |
| Hilt/Dagger | 手动 AppContainer 出现大量作用域样板或多人频繁冲突 |
| 独立 Activity | 页面确实需要独立任务栈、窗口策略或外部入口 |

Feed 示例需要分页、磁盘缓存、多个远程接口并同时暴露列表与详情角色，因此保留 FeedRemoteDataSource 和 FeedDataBindings。普通单接口不需要复制这些层级。

## 小、中、大型项目如何使用

### 小型项目

- 保留一个或少量 Feature 模块。
- 使用一组 `module-domain-app / module-data-app`，通过清晰包名区分账号、商品等业务。
- 使用手动 `AppContainer`。
- 不预置 UseCase、数据库、WorkManager 或 DI 框架。

### 中型项目

- 达到团队或编译边界后，将热点业务从 `module-domain-app / module-data-app` 拆成独立模块。
- 业务包名可以保持不变，只调整 Gradle 依赖，Feature API 不需要重写。
- 复杂持久化使用 Room，可靠后台任务使用 WorkManager。
- 多处复用的业务编排才增加 UseCase。

### 大型项目

- Feature 继续依赖领域 Repository，不依赖数据实现。
- 使用 Hilt/Dagger 实现 `AppDependencies`，Activity 和 Feature 构造参数保持不变。
- 只有组织或交付确实需要时才拆 API/Implementation、动态 Feature 和多进程边界。

## 新增普通二级页面

二级页面不放进 `AppTopLevelDestination`：

```kotlin
@Serializable
data class ProductDetailDestination(
    val productId: String
) : AppRoute
```

注册后从当前顶层栈跳转：

```kotlin
navigationState.navigate(ProductDetailDestination(productId))
```

返回时仍停留在原来的顶层页面和页面状态。

## 删除开眼与视频示例

自己的首个页面和接口接通后，再删除示例：

1. 在 `AppTopLevelDestination` 删除 Home、Shorts 配置并加入自己的顶层页面。
2. 从 `AppNavHost` 删除示例 Destination 注册、播放器全屏状态和视频背景判断。
3. 从 `MainActivity`、`AppDependencies`、`AppContainer` 删除 Feed 与播放器依赖。
4. 从 `app/build.gradle.kts` 和 `settings.gradle.kts` 删除不再使用的示例模块。
5. 项目完全不播放视频时再删除 `lib-core-player`；仍播放业务视频时只替换页面和资源 Host。

不要把 `FeedApiService` 改名后继续塞入账号、商品等无关接口。同一业务可以复用模块，不同业务仍应使用清晰的 Service、DTO 和 Repository 包名。
