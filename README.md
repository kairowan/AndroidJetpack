# ComposeScaffold

面向真实项目扩展的 Android Compose 多模块脚手架。示例业务使用开眼视频流，工程重点不是展示几个页面，而是固定可持续维护的模块语义、页面职责、类型安全路由、单/多 Activity 承载方式、网络错误边界和 Media3 生命周期。

## 技术基线

- Kotlin 2.4.10、JDK 17、AGP 9.2.1、Gradle 9.4.1
- Compose BOM 2026.06.01、Material 3
- Navigation 3 1.1.4、目的地级 `ViewModelStore`
- `StateFlow`、单向数据流（UDF）
- OkHttp、Retrofit、Gson
- Media3 ExoPlayer、Media3 Compose UI
- Gradle convention plugins、Version Catalog、Configuration Cache

最低系统版本为 API 24，编译 SDK 为 37，目标 SDK 为 36。AGP 9 使用内置 Kotlin，Jetifier 默认关闭。

## 模块命名与职责

所有模块都直接位于仓库根目录，不再使用 `core/`、`feature/` 父目录分组。模块名前缀直接表达职责：

```text
:app                    app/

:core-data              core-data/
:core-network           core-network/
:core-designsystem      core-designsystem/
:core-ui                core-ui/
:core-player            core-player/

:domain-feed            domain-feed/
:data-feed              data-feed/

:feature-home           feature-home/
:feature-detail         feature-detail/
:feature-shorts         feature-shorts/
```

| 前缀/模块 | 定位 | 允许包含 |
| --- | --- | --- |
| `:app` | 项目装配模块 | Application、Activity、服务地址注册、依赖容器、应用路由 |
| `:core-*` | 可复用基础/工具能力 | 公共加载契约、网络、设计系统、通用 UI 与 ViewModel 任务策略、播放器 |
| `:domain-*` | 项目共享领域契约 | 领域模型、窄仓库接口、稳定业务结果与错误分类 |
| `:data-*` | 项目数据实现 | 数据图装配入口、Retrofit Service、DTO、DataSource、Repository 实现、Mapper |
| `:feature-*` | 项目页面模块 | Route、ViewModel、UiState、Screen、业务组件 |
| `build-logic` | 构建基础设施 | Android/Compose convention plugin |

依赖方向：

```text
app
 ├─ feature-home ─────┐
 ├─ feature-shorts ───┼── domain-feed ── core-data
 ├─ feature-detail ───┘       ▲
 │   └── core-ui / core-designsystem / core-player
 └─ data-feed ────────────────┴── core-network
```

Feature 只依赖 `:domain-*` 的业务契约，不直接依赖 `:data-*` 或 `:core-network`，也不能在 Composable 或 ViewModel 中创建 Retrofit、DataSource 或 Repository。这样替换网络、缓存或 Repository 实现时不会扩大到页面模块重新编译。`core-*` 不允许出现 Feed、账号等项目业务协议。

## 小、中、大型项目扩展方式

脚手架不强制所有项目一开始就引入 Hilt、UseCase 或更多 Gradle 模块，而是保持同一组稳定契约逐级扩展：

| 规模 | 默认使用方式 | 达到真实边界后再增加 |
| --- | --- | --- |
| 小型 | `AppDependencies` + 手动 `AppContainer`，Feature 直接依赖窄 Repository | 不增加 UseCase、DI 框架或 API/Impl 模块 |
| 中型 | 使用 `feature-* / domain-* / data-*` 边界、data 装配入口、约定插件和架构检查 | 复用业务编排再加 UseCase；复杂持久化再换 Room |
| 大型 | 保持 Activity/Feature 只依赖 `AppDependencies` 与 domain 角色，将依赖实现替换为 Hilt/Dagger | 团队、交付或编译隔离确有需要时再拆 API/Impl、动态 Feature、分析和安全模块 |

因此规模升级只替换装配实现，不要求重写 Screen、ViewModel、Repository 契约或路由参数。

## Feature 分包规范

每个业务模块按职责分包，不把路由、状态和所有组件放在一个文件：

```text
feature-home/src/main/java/.../home/
├─ navigation/
│  └─ HomeRoute.kt
├─ presentation/
│  ├─ HomeUiState.kt
│  └─ HomeViewModel.kt
└─ ui/
   ├─ HomeScreen.kt
   └─ component/
      ├─ FeedSourceSelector.kt
      ├─ HomeVideoCard.kt
      └─ FeedTextItem.kt
```

- `navigation`：依赖获取、目的地级 ViewModel、生命周期状态收集、页面导航回调。
- `presentation`：不可变 `UiState`、ViewModel 和页面业务状态转换。
- `ui`：只消费不可变值并发出回调的无状态页面。
- `ui/component`：页面内部可独立理解和预览的组件。
- `ui/model`：仅服务该界面的展示配置或适配模型。

标准页面链路：

```text
Navigation entry
  → FeatureRoute(repository, navigation callbacks)
      → viewModel { FeatureViewModel(repository) }
      → collectAsStateWithLifecycle()
      → FeatureScreen(uiState, callbacks)
```

## ViewModel 异步基类

`viewModelScope.launch` 返回 `Job` 是 Kotlin 协程的正常设计，Job 仍属于 ViewModel 的作用域，并会在 `ViewModel` 清理时自动取消。脚手架真正禁止的是业务方法把 `Job` 暴露给 Route 或 Screen，以及各页面自行维护互不一致的取消逻辑。

所有业务 ViewModel 继承 `:core-ui` 的 `BaseViewModel<FeatureUiState>`。ViewModel 基础能力与通用页面反馈组件同属展示基础设施，不再为四个类型单独增加 Gradle 模块。基类统一私有持有 `MutableStateFlow`，只向 Route 暴露只读 `uiState`，并向子类提供当前快照 `currentState`、原子更新 `updateState` 和整体替换 `setState`。具体状态类型、Loading/Error 字段和业务转换仍由各 Feature 定义。

公开页面动作只返回 `Unit`。中间处理直接使用 kotlinx.coroutines 官方 Flow 操作符，基类只提供一个单次任务源、一个当前状态恢复操作和两个生命周期终端：

| API | 使用场景 | 并发行为 |
| --- | --- | --- |
| `taskFlow { ... }` | 将单次 suspend 请求转换为冷流 | 只有终端订阅后才执行 |
| `StateFlow.restoreUiState` | 使用缓存源的当前值同步恢复页面 | 只恢复 UiState 并返回原 StateFlow，不隐式启动请求 |
| `Flow.restoreUiState(initialValue)` | 使用显式实体快照恢复详情首帧 | 返回原 Flow，不在仓库为每个历史 ID 永久持有状态容器 |
| `Flow.launchUniqueIn` | 刷新、重试、加载下一页 | 同名任务执行期间不订阅新的上游 |
| `Flow.launchLatestIn` | 状态观察、搜索、筛选、来源切换 | 取消同名旧订阅，只保留最新链路 |

缓存数据源按照 `observeSource(...).restoreUiState(...).filterNotNull().onEach(...).launchLatestIn(...)` 组织观察链，首次请求使用独立的 `taskFlow(...).launchLatestIn(...)` 链。Repository `StateFlow` 是页面内容的唯一事实源；请求结果只更新错误等请求元数据，不再重复写入内容。需要取消旧值处理时使用官方 `mapLatest` 或 `transformLatest`。StateFlow 和 SharedFlow 都实现 Flow，不复制两套收集方法。

每个有限请求通过任务 API 的 `onLoadingChanged` 映射到自己的 UiState：首屏使用 `isInitialLoading`，保留内容刷新使用 `isRefreshing`，分页使用 `isLoadingMore`。基类只负责在任务真正启动时打开、在 `finally` 中关闭，因此成功、失败、取消和意外异常都不会遗留 Loading；同名最新任务替换旧任务时，旧任务也不会误关新任务状态。基类不提供全局 `isLoading`，避免多个并发接口相互覆盖。

同名 Job 的检查、替换和登记由基类在同一个锁内完成，即使子类从多个线程同时调用任务终端，也不会越过 `unique/latest` 策略。可选 `ViewModelTaskObserver` 会输出启动、完成、取消、失败、忽略和替换事件，供 Debug 日志、性能统计或崩溃平台接入；观察器失败不得影响业务任务。

可预期的网络和业务失败必须先由 `NetworkExceptionHandler`、Repository 结果类型转换为 Feature 自己的 UiState。只有越过该链路的意外任务异常才由基类统一包装为 `ViewModelTaskException`：异常中包含稳定的任务标识和原始 cause，默认继续抛出，避免失败被静默吞掉；需要接入崩溃上报或页面兜底时，子类可以传入 `onError` 或重写 `onUnhandledTaskException`。异常诊断文本只用于日志和定位，不能直接显示给用户。

## 单 Activity 与多 Activity

脚手架通过构建参数明确选择承载方式，而不是让 Feature 感知宿主类型：

```bash
# 默认：全部业务页面位于 MainActivity 的 Navigation 3 返回栈
./gradlew :app:assembleDebug -PAPP_NAVIGATION_MODE=single_activity

# 多 Activity：顶层页面仍由 MainActivity 承载，视频详情使用独立 Activity
./gradlew :app:assembleDebug -PAPP_NAVIGATION_MODE=multi_activity
```

对应职责：

- `MainActivity`：单 Activity 模式承载全部目的地；多 Activity 模式承载顶层目的地。
- `VideoDetailActivity`：多 Activity 模式下的视频详情宿主，校验参数后复用相同的 `VideoDetailRoute`。
- `ComposeScaffoldApplication`：只延迟持有 `AppDependencies`，默认实现是轻量 `AppContainer`，大型项目可替换为 Hilt/Dagger 图。
- `AppDependencies`：Activity 可见的稳定依赖契约，不暴露具体数据源、Service 或装配方式。
- `AppNetworkEndpoints`：单独声明各业务服务的 Base URL，新增服务只增加一个语义清晰的 Endpoint。
- `AppContainer`：只装配共享网络基础设施，并把 Endpoint 与存储目录交给对应 data 模块的装配入口。
- `AppNavigationMode`：只解析受支持的构建值；Gradle 在构建阶段拒绝非法模式。

Feature 的 Route 和 Screen 不持有 Activity 跳转代码，因此同一页面可以在 Navigation 3 entry 或独立 Activity 中复用。

运行环境通过 `APP_ENVIRONMENT=development|staging|production` 选择，Gradle/BuildConfig 只保存环境名称，所有 Base URL 仍由 `AppNetworkEndpoints` 集中注册：

```bash
./gradlew :app:assembleDebug -PAPP_ENVIRONMENT=development
./gradlew :app:assembleRelease -PAPP_ENVIRONMENT=production
```

Debug 接受开发、预发或生产环境；Release 构建类型始终把 `APP_ENVIRONMENT` 固定为 `production`，即使调用者误传开发参数也不会连接开发服务。

## 类型安全路由

应用路由集中在以下三个边界：

```text
app/navigation/model/AppRoute.kt       可序列化 NavKey
app/navigation/AppNavigationState.kt   独立顶层返回栈、跳转、去重和返回
app/navigation/AppNavHost.kt           返回栈、宿主模式和自适应导航布局
app/navigation/destination/*Entry.kt   每个 Feature 独立的目的地装配入口
```

- 普通跳转使用 `AppNavigationState.navigate()`。
- 顶层切换使用 `navigateTopLevel()`，Home 与 Shorts 各自保留返回栈和页面状态。
- 返回统一使用 `pop()`；根页面不会被移除，并把无法继续出栈的返回事件交给 Activity 正常退出。
- Navigation 3 安装 saveable-state 和 ViewModel-store entry decorator。
- Activity 路由参数由 `VideoRouteArguments` 在边界处校验，缺少必要字段时安全结束页面。

详情路由只保存视频 ID 与数据来源。详情页面通过共享 Repository 的内存/磁盘事实源恢复数据；缓存未命中时刷新对应来源，仍未找到则返回稳定的 `NOT_FOUND` 错误。

紧凑宽度使用底部导航，中等及以上宽度自动切换为 NavigationRail；详情页在宽屏下使用播放器与元数据双栏布局。全屏与横屏请求由 Activity 宿主统一处理，Feature 不查找或强转 Activity。

## Network 与 Data 分层

Base URL 不再写入 Gradle 或 BuildConfig。应用通过类型化 Endpoint 注册任意数量的远程服务，一个进程共享 OkHttp，不同 Endpoint 分别缓存 Retrofit：

```text
AppNetworkEndpoints
  → NetworkEndpoint(feed / account / upload / ...)
  → NetworkClientFactory
      ├─ 一个共享 OkHttpClient
      └─ 每个 Endpoint 一个缓存的 Retrofit
  → createFeedDataBindings
      ├─ FeedApiService
      ├─ RetrofitFeedRemoteDataSource → RetrofitNetworkDataSource
      ├─ FileFeedLocalDataSource 原子磁盘快照
      ├─ RemoteResourceUrlPolicy → 媒体 Host/HTTPS 安全边界
      └─ DefaultFeedRepository → 内存 StateFlow 单一事实源
  → FeedDtoMapper
  → FeedPage / FeedItem
```

`:core-network` 只负责与业务无关的网络基础能力：

| 包 | 职责 |
| --- | --- |
| `config` | 共享超时/缓存配置、Base URL Endpoint 校验和远程资源 URL 安全策略 |
| `client` | 共享 OkHttp，并按 Endpoint 创建和缓存 Retrofit/Service |
| `interceptor` | 公共请求头、按请求 Host 选择令牌/租户、请求追踪 ID、Debug 安全日志 |
| `exception` | Retrofit 响应、HTTP 状态和传输异常的统一分类 |
| `datasource` | 可执行任意 Retrofit 请求的通用契约与适配实现 |
| `result` | 网络成功以及连接、超时、空响应、HTTP、解析、未知失败分类 |

`:core-data` 只提供一个不包含 Feed、账号等业务名称的 `CommonRepository<Params, Result>`，统一所有页面都能成立的单次 `load()` 语义。

观察、刷新、下一页、保存、删除和搜索等能力由对应的产品窄接口明确声明。例如 `FeedPageRepository` 继承公共加载契约，再声明 Feed 自身的观察、刷新与分页函数；普通页面不会被迫实现无用函数。

`:domain-feed` 持有页面与数据实现之间的稳定业务边界：

- `FeedPageRepository`、`FeedVideoRepository`：面向不同页面能力的窄仓库角色。
- `FeedPage`、`FeedItem`、`FeedVideo`：不依赖 Android、Retrofit 或缓存实现的领域模型。
- 领域模型不参与缓存序列化；缓存 DTO 与下一页 URL 只存在于 `data-feed`，页面只读取 `canLoadMore` 业务事实。
- `FeedLoadResult`、`FeedVideoResult`、`FeedLoadError`：页面可稳定处理的业务结果。

`:data-feed` 只持有 Feed 数据实现：

- `FeedDataGraph`：data 模块唯一装配入口，返回只含领域角色的 `FeedDataBindings`；Service、DTO、DataSource、缓存和 Repository 实现全部保持 `internal`。
- `FeedApiService`：Feed Retrofit 接口及路径。
- `Feed*Dto`：服务端可空传输模型，一类一个文件。
- `FeedRemoteDataSource`：Feed 仓库需要的远程能力契约。
- `RetrofitFeedRemoteDataSource`：组合 Feed Service 与通用 `NetworkDataSource`。
- 列表 Feature 只依赖 `domain-feed` 中的 `FeedPageRepository`，详情 Feature 只依赖 `FeedVideoRepository`；AppContainer 通过 `data-feed` 装配入口取得同一个实现，再按两个窄角色提供，不保留含糊的聚合接口，也不感知数据模块内部类型。
- Repository 持有分页令牌、并发锁、缓存新鲜度、合并去重和详情恢复逻辑。
- `observePage()` / `observeVideo(videoId, source)` 向多个页面提供同一事实数据，视频观察与恢复使用一致的“来源 + ID”身份，ViewModel 不自行拼接分页。
- 将网络失败转换成稳定的 `FeedLoadError`，UI 不解析 HTTP code 或异常文本。
- 递归清洗 DTO，跳过非正数 ID 或缺少安全播放地址的脏数据；磁盘快照恢复时重新执行相同的 ID 和媒体 URL 边界校验。
- 防止服务端重复 continuation URL 造成无限分页。
- 领域模型不保留 Retrofit、Gson 或服务端实现细节。

默认网络能力包括：

- 强制 HTTPS；只有显式开发配置才能允许明文地址。
- 服务端返回的图片和视频地址按精确 Host、HTTPS 端口校验；已确认的旧 HTTP Host 先升级为 HTTPS，未知 Host、用户信息、异常端口和本地协议直接丢弃。
- 支持多个独立 Base URL，相同 Endpoint 复用 Retrofit，不同 Endpoint 共享连接池。
- 初始 `@Url` 在认证头注入前校验，服务端分页地址和每次重定向在实际网络交换前再次校验。
- 独立连接、读取、写入、整次调用超时。
- 有界 OkHttp 磁盘缓存。
- `Accept`、`User-Agent`、`X-Request-ID` 公共请求头。
- `NetworkHeaderProvider` 可读取当前 Request，按 Host 接入不同 Token、租户或渠道。
- `NetworkExceptionHandler` 统一处理成功、空正文、HTTP、连接、超时、解析和未知异常，DataSource 不重复编写异常分支。
- TLS/证书与协议异常使用不可重试类型，避免被通用 `IOException` 分支错误重试。
- 网络校验、通用失败和日志文案全部位于 `core-network/src/main/res/values/strings.xml`。
- 保留 `CancellationException`，页面离开后请求可正确取消。
- `NetworkLoggingInterceptor` 只在 Debug 装配；Release 不注入日志拦截器。
- Debug 日志按请求/响应/异常分段展示，完整输出并美化 JSON，自动隐藏认证头、Cookie、Token、密码、Credential、Session、Signature、Secret 和 API Key。
- 请求头、Query、请求 JSON 与流式响应 JSON 共用同一套敏感字段策略，避免不同日志路径出现脱敏规则漂移。
- JSON 响应在应用正常消费时旁路写入临时文件，到达 EOF 后流式解析并按 Logcat 单条容量分段；分段只解决输出容量，不设置正文大小上限，也不省略正文。
- Debug 进程异常退出留下的旁路临时文件会在下次网络图创建时清理，不长期保留响应数据。
- 旁路记录器返回应用原始字节，提前关闭或记录失败会输出明确失败标记，日志故障不会中断真实请求；二进制和非 JSON 正文不展开。

Debug 日志示例：

```text
┌──────── 网络响应 ────────
│ 请求编号: 4c2c…
│ 请求方法: GET
│ 请求地址: https://api.example.com/feed?token=***
│ 响应状态: 200 OK
│ 请求耗时: 86 ms
│ 响应头:
│   Content-Type: application/json
│ 响应体:
│   {
│     "token": "***",
│     "result": "ok"
│   }
└────────────────────────
```

新增第二个 Base URL 时，在 `AppNetworkEndpoints` 增加对应环境函数，并把 Endpoint 传给所属 data 模块的装配入口：

```kotlin
object AppNetworkEndpoints {
    fun account(environment: AppEnvironment) = NetworkEndpoint(
        name = "account-${environment.name.lowercase()}",
        baseUrl = when (environment) {
            AppEnvironment.DEVELOPMENT -> "https://dev-account.example.com/"
            AppEnvironment.STAGING -> "https://staging-account.example.com/"
            AppEnvironment.PRODUCTION -> "https://account.example.com/"
        }
    )
}
val accountRepository = createDefaultAccountRepository(
    networkClientFactory = networkClientFactory,
    endpoint = AppNetworkEndpoints.account(environment),
    cacheDirectory = File(applicationContext.filesDir, "account_cache")
)
```

Endpoint 地址必须以 `/` 结尾并默认使用 HTTPS。测试、预发和生产地址也应集中在 `AppNetworkEndpoints` 内按明确名称选择，不再增加 `AppRuntimeConfig` 或通过全局 Gradle Base URL 限制整个应用。当前模板使用带格式版本、时间戳校验和 8 MiB 单文件上限的原子 Feed 快照；不兼容、未来时间、超限或损坏缓存会在边界失效或清除。需要查询、增量更新或复杂事务时，可在不改变 Repository 契约的前提下将 `FeedLocalDataSource` 替换为 Room。

小、中、大型项目的升级触发条件、替换边界和质量门禁见 [`docs/scaling-guide.md`](docs/scaling-guide.md)。模板不会默认强制 Hilt、Room、WorkManager、Macrobenchmark 或证书 Pin；真实风险出现时再按该指南接入。

## 默认安全策略

- Gradle Wrapper 固定官方 HTTPS 分发地址与 SHA-256；升级 Wrapper 时必须同步更新校验值，避免构建机执行被替换的分发包。
- Manifest 与 Network Security Config 双重拒绝明文流量，不为整个业务域或子域开放 HTTP。
- Android 云备份和设备迁移默认关闭，Android 11 及以下和 Android 12 及以上规则都采用 deny-all；真实产品完成数据分类后按最小路径显式放行。
- 应用自有组件中只有 Launcher Activity 对外导出；详情 Activity 保持 `exported=false`，Activity 参数在创建 Route 前校验。
- Token、租户和认证头通过 `NetworkHeaderProvider` 按 Host 动态注入，禁止写入源码、资源、Gradle 或日志。
- 网络、任务和缓存诊断使用显式稳定码；Release 只记录脱敏维度，原始异常栈仅允许 Debug 输出。
- Gson DTO 的 R8 规则由 `data-*` 模块自己的 `consumer-rules.pro` 持有，Release 混淆构建是交付门槛。
- 证书固定和 Android 16 Certificate Transparency 需要后端证书轮换、备用 Pin 和故障恢复能力，因此作为大型项目显式策略，不对公共示例域盲目启用。

## 播放器边界

- 对外契约名为 `VideoPlayerController`，不使用 Java 风格 `IPlayer`。
- Media3 实现名为 `Media3VideoPlayerController`，只存在于 `:core-player`。
- Compose API 使用 `InlineVideoPlayer`、`FullscreenVideoPlayer` 和 `PlayerSurface`，不使用误导性的 `*View` 命名。
- 视频画面使用 Media3 Compose `ContentFrame`，不通过 `AndroidView` 包装 `PlayerView`。
- 播放器由当前 Composition 持有，跟随 Lifecycle 暂停、恢复和释放。
- 播放器只通过应用注入的 `VideoPlayerFactory` 创建；Media3 与 Coil 共享受控媒体客户端，初始地址和每次重定向都重新检查资源 Host 白名单。
- 高频进度只重组控制浮层，不反复重组 Media3 画面节点。
- 对外错误使用 `PlayerFailure` 稳定分类，不泄漏 Media3 错误码和原始异常文本。
- `SimpleCache` 是进程级唯一所有者，避免同一路径被多个实例锁定。
- 预加载最多并发两个请求，页面切换会取消过期目标；释放控制器后继续访问会立即失败。

## 类描述与中文注释

生产 Kotlin 文件严格保持一个具名类型；接口、实现类、`data class`、密封接口实现和私有辅助类也必须分别建文件。公开 Compose 入口可以保留同文件私有辅助函数，但不能混入第二个具名类型。所有生产文件使用项目统一类头，日期使用创建或本次结构性修改的当前时间，描述必须说明职责和边界：

```kotlin
/**
 * @author 浩楠
 * @date 2026/7/20 09:33
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情路由入口，持有播放器、播放恢复快照和宿主窗口模式生命周期
 */
```

规范：

- 类描述和公开 KDoc 以中文为主。
- 每个公开接口函数都必须有中文 KDoc，说明用途、输入边界、返回值或失败语义；Retrofit 注解不能代替接口说明。
- 描述不能写“工具类”“公共类”“TODO”或复述类名。
- 非显然的生命周期、缓存所有权、失败回退和简化边界需要注释。
- 私有的一行映射和明显 UI 排版不写逐行注释。
- 用户可见文字必须放在资源文件。

## Skill 与静态约束

仓库内置 [`compose-scaffold-guardrails`](.agents/skills/compose-scaffold-guardrails/SKILL.md)，详细规范见 [`architecture.md`](.agents/skills/compose-scaffold-guardrails/references/architecture.md)。

静态检查覆盖：

- 嵌套模块目录、嵌套 Gradle 路径和旧式下划线模块名。
- Feature 根包堆放源码。
- 每个生产文件存在多个具名类型（包括嵌套类型）或缺少中文类头。
- 单个生产 Kotlin 文件超过 250 行且未按职责拆分。
- Feature 直接依赖 Network。
- Feature 直接依赖 Data 实现模块。
- AppContainer 直接导入 data 模块的 Service、DataSource、Mapper 或 Repository 实现。
- AppNavHost 重新堆放具体 Feature Route，而不是新增独立 destination entry。
- Manifest 开启明文流量、默认备份，或 data 模块把 DTO 混淆规则写回 app。
- Gradle/BuildConfig 中配置全局 Base URL。
- `:core-network` 出现 Feed 等项目 Service、DTO 或 DataSource。
- Network 生产代码存在硬编码中文文案、重复异常分类或直接依赖 Android Log。
- Network 日志未通过 `BuildConfig.DEBUG` 注入。
- 公开接口函数缺少中文 KDoc。
- Composable 创建 Repository。
- ExoPlayer 实现泄漏到播放器模块之外。
- 多个 `SimpleCache` 所有者。
- `IPlayer`、`VideoPlayerView` 等遗留命名。
- 临时日志、样例测试、TODO 描述。
- 商用 Network 关键边界和正式回归测试缺失。
- Release 环境未固定为 production、缓存恢复绕过 URL 校验或仓库跨来源元数据退回非线程安全容器。

运行检查：

```bash
.agents/skills/compose-scaffold-guardrails/scripts/check_architecture.sh
```

## 构建与验证

一条命令按顺序执行架构约束、全部单元测试、Lint、两种宿主模式的 AndroidTest 编译、单 Activity Debug、多 Activity Debug、带 R8 的 Release APK 和 Release AAB：

```bash
bash .agents/skills/compose-scaffold-guardrails/scripts/verify_scaffold.sh
```

等价的核心 Gradle 门禁如下；保持独立调用，避免 Lint 分析与打包任务并发争用同一份 Kotlin 中间产物：

```bash
./gradlew :app:testDebugUnitTest :core-network:testDebugUnitTest \
  :core-player:testDebugUnitTest :core-ui:testDebugUnitTest \
  :data-feed:testDebugUnitTest \
  :feature-home:testDebugUnitTest :feature-shorts:testDebugUnitTest
./gradlew lintDebug
./gradlew :app:assembleDebugAndroidTest
./gradlew :app:assembleDebug
./gradlew :app:assembleRelease
./gradlew :app:bundleRelease
```

连接真机或模拟器后，执行两种宿主模式的设备烟测：

```bash
bash .agents/skills/compose-scaffold-guardrails/scripts/verify_device_tests.sh
```

模板不会保存 JKS 或密码。真实产品由 CI 使用受保护的上传密钥签名 AAB，再校验签名产物：

```bash
bash .agents/skills/compose-scaffold-guardrails/scripts/verify_signed_bundle.sh \
  /absolute/path/to/signed-release.aab
```

正式测试覆盖：

- Network Endpoint 与媒体地址边界、真实 HTTP 请求头/重定向/认证/缓存、请求和响应完整流式日志、统一日志脱敏和原始字节保持。
- Repository 版本化有界缓存、缓存输入重校验、跨来源并发、时间回拨失效、无实体级永久 Flow 的详情恢复、分页视频去重与结构行保留。
- 首页来源恢复、来源切换和仓库事实流投影。
- 短视频分页请求与非视频条目过滤。
- Navigation 3 路由去重、双顶层返回栈保留、根页面保护和宿主退出委托。
- 单/多 Activity 构建参数解析。
- ViewModel 同名任务去重、最新任务取消、热流收集和生命周期自动取消。
- 播放状态计算、时间格式化和控制参数校验。
- 设备端使用测试依赖覆盖顶层导航、首页到详情、Activity 重建恢复、根返回以及多 Activity 参数边界，不依赖真实接口数据。

测试属于脚手架契约，需要长期保留。临时日志、探针、临时测试、APK 和构建报告在交付前删除。

## 扩展步骤

新增 Feature：

1. 在仓库根目录创建 `feature-<name>`，Gradle 路径使用 `:feature-<name>`。
2. 建立 `navigation / presentation / ui / ui.component` 包。
3. Route 获取依赖和 ViewModel；Screen 只接受状态与回调。
4. 在 `AppRoute` 增加稳定、可序列化的 `NavKey`。
5. 在 `app/navigation/destination` 新增独立 `*Entry.kt`，再由 `AppNavHost` 组合注册；Feature 不直接操作应用返回栈。
6. 为非平凡状态留下一个最小正式测试。

新增数据类型：

1. 在对应 `domain-*` 模块增加领域模型、窄仓库角色和稳定结果。
2. 在对应 `data-*` 模块的 `remote/model` 增加 DTO。
3. 在 `remote/service` 增加 Retrofit 协议，并为每个接口函数写中文 KDoc。
4. 在业务 `RemoteDataSource` 增加仓库需要的方法，通过通用 `NetworkDataSource` 执行。
5. 在业务 `mapper` 完成 DTO 校验与领域转换，由 Repository 实现领域契约且不暴露 Retrofit 类型。
6. 在业务 `di` 包提供一个最小装配入口，内部创建 Service、DataSource 与 Repository；AppContainer 只传入共享基础设施、Endpoint、资源 URL 策略和存储目录。
7. 在 data 模块自己的 `consumer-rules.pro` 维护 Retrofit/Gson/序列化所需的 Release 规则。

新增远程服务：

1. 在 `AppNetworkEndpoints` 增加独立 `NetworkEndpoint`。
2. 将 Endpoint 传给对应 data 模块的装配入口，由模块内部使用共享 `NetworkClientFactory` 创建 Service。
3. 需要不同认证信息时，在 `NetworkHeaderProvider` 中根据 Request Host 返回请求头。
4. 服务会返回图片、文件或媒体地址时，在 `AppRemoteUrlPolicies` 显式登记允许的 Host 和 HTTPS 端口。
5. 不在 Gradle、BuildConfig 或 `:core-network` 中添加项目 Base URL。

只有出现真实复用或复杂业务组合后才引入 UseCase；手动装配已经产生大量作用域样板、多人频繁冲突或多实现切换时，再让 Hilt/Dagger 实现 `AppDependencies`。
