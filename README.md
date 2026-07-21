# ComposeScaffold

面向真实项目扩展的 Android Compose 多模块脚手架。项目中的首页、短视频、视频详情和开眼接口只是演示代码，不是脚手架要求保留的业务。真正可以复用的是模块边界、页面状态、类型安全路由、单/多 Activity 承载方式、网络错误边界和可替换的应用依赖容器。

## 示例业务与脚手架边界

接入自己的项目时，不需要把业务继续命名为 `Home`、`Shorts`、`Feed`，也不需要继续请求开眼接口。应当根据自己的业务语义替换示例模块：

| 当前示例 | 在真实项目中的处理方式 |
| --- | --- |
| `feature-home` | 替换为实际首个顶层业务，例如工作台、商城或消息 |
| `feature-shorts` | 不需要短视频时直接删除，或者替换为第二个顶层业务 |
| `feature-detail` | 仅在存在视频详情时保留，否则替换为自己的详情 Feature |
| `domain-feed` | 替换为账号、商品、订单等真实领域契约 |
| `data-feed` | 替换为真实接口、DTO、Mapper、缓存和 Repository 实现 |
| `core-player` | 项目没有音视频能力时可以从依赖和源码中移除 |

页面不等于模块，接口也不等于模块。小项目可以先使用一组 `domain-app` 与 `data-app`，通过业务包名容纳登录、资料、商品等能力；只有团队、交付或编译边界真实出现时，才拆成 `domain-account`、`data-account` 等独立模块。

第一次接入请直接阅读 [`docs/usage-guide.md`](docs/usage-guide.md)，其中包含底部导航替换、新增 Feature、新增网络业务域和删除示例代码的完整步骤。

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
| `:core-*` | 可复用基础/工具能力 | 公共加载与结果契约、网络、设计系统、通用 UI 与 ViewModel 任务策略、播放器 |
| `:domain-*` | 项目共享领域契约 | 领域模型、窄仓库接口与稳定业务错误分类 |
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
| 小型 | 一个或少量 Feature + 一组聚合的 `domain-app / data-app`，使用手动 `AppContainer` | 不按页面或接口增加模块，不增加 UseCase、DI 框架或 API/Impl 模块 |
| 中型 | 将真正形成边界的热点业务拆为 `feature-* / domain-* / data-*`，其余业务继续复用聚合模块 | 复用业务编排再加 UseCase；复杂持久化再换 Room |
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
app/navigation/AppTopLevelDestination.kt 顶层路由、标题、图标的唯一配置
app/navigation/AppNavigationState.kt   独立顶层返回栈、跳转、去重和返回
app/navigation/AppNavHost.kt           返回栈、宿主模式和自适应导航布局
app/navigation/destination/*Entry.kt   每个 Feature 独立的目的地装配入口
```

- 普通跳转使用 `AppNavigationState.navigate()`。
- 顶层切换使用 `navigateTopLevel()`；`AppTopLevelDestination` 中注册的每个页面各自保留返回栈和页面状态。
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

`:core-data` 提供不包含 Feed、账号等业务名称的 `CommonRepository<Params, Result>`，以及通用的 `DataResult<Value, Error>`、`DataSuccess` 和 `DataFailure`。普通业务只定义自己的模型与错误类型，不再重复创建 Result、Success、Failure 三个文件。

观察、刷新、下一页、保存、删除和搜索等能力由对应的产品窄接口明确声明。例如 `FeedPageRepository` 继承公共加载契约，再声明 Feed 自身的观察、刷新与分页函数；普通页面不会被迫实现无用函数。

`:domain-feed` 持有页面与数据实现之间的稳定业务边界：

- `FeedPageRepository`、`FeedVideoRepository`：面向不同页面能力的窄仓库角色。
- `FeedPage`、`FeedItem`、`FeedVideo`：不依赖 Android、Retrofit 或缓存实现的领域模型。
- 领域模型不参与缓存序列化；缓存 DTO 与下一页 URL 只存在于 `data-feed`，页面只读取 `canLoadMore` 业务事实。
- `FeedLoadError`：页面可稳定处理的业务错误；成功/失败结构复用 `:core-data` 的 `DataResult`。

`:data-feed` 只持有 Feed 数据实现：

- `FeedDataGraph`：data 模块唯一装配入口。Feed 同时暴露列表与详情两个 Repository 角色，因此返回 `FeedDataBindings`；普通单角色业务直接返回 Repository，不额外创建 Bindings。
- `FeedApiService`：Feed Retrofit 接口及路径。
- `Feed*Dto`：服务端可空传输模型，一类一个文件。
- `FeedRemoteDataSource`：Feed 需要组合分页、详情恢复与缓存，因此保留业务远程能力契约；普通单服务 Repository 可直接组合 Service 与公共 `NetworkDataSource`。
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

## 使用教程

完整接入教程见 [`docs/usage-guide.md`](docs/usage-guide.md)。教程先给出静态页面、普通接口和顶层导航的最短接入路径，再说明何时才需要新增 ViewModel、Feature、DataSource、Bindings、domain/data 模块与多 BaseURL 装配。
