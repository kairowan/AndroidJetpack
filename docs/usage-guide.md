# ComposeScaffold 使用教程

[返回项目说明](../README.md)

本文档用于把仓库中的 Home、Shorts、Feed 和开眼接口示例替换成自己的真实业务。示例名称不是脚手架固定协议，可以按项目语义删除或重命名。

推荐按照“项目标识 → 顶层导航 → Feature → 领域契约 → 数据实现 → 应用装配”的顺序改造。先让自己的页面替换示例页面，再删除 Feed 和视频代码，可以避免一次删除过多内容后失去可运行入口。

## 第一步：修改项目标识

至少检查以下位置：

| 文件 | 需要修改的内容 |
| --- | --- |
| `settings.gradle.kts` | `rootProject.name` |
| `app/build.gradle.kts` | `namespace`、`applicationId`、版本号来源 |
| `app/src/main/res/values/strings.xml` | 应用名称和顶层导航文案 |
| `ComposeScaffoldApplication.kt` | 按项目语义重命名 Application |
| `AndroidManifest.xml` | Application、Activity 和应用主题入口 |

包名可以分阶段修改。先修改 `applicationId` 不会要求一次性移动所有 Kotlin 文件；确认业务模块稳定后，再使用 Android Studio 的 Rename 重构统一 `com.kotlinmvvm` 包名。

## 第二步：判断需要新增哪些模块

不要因为新增一个接口就机械创建三个模块，先判断它属于哪种情况：

| 需求 | 建议 |
| --- | --- |
| 只有静态页面或纯本地交互 | 只创建 `feature-*` |
| 新页面使用已有业务数据 | 创建 `feature-*`，复用已有 `domain-*` 仓库角色 |
| 已有业务域增加一个接口 | 在现有 `domain-* / data-*` 中增加契约和实现 |
| 完全独立的新业务域 | 创建对应的 `domain-* / data-*`，再由一个或多个 Feature 使用 |
| 多个页面复用复杂业务编排 | 在对应业务域增加 UseCase，不修改 `core-*` |

例如“商品列表”和“商品详情”通常共同使用 `domain-product / data-product`，页面可以拆成 `feature-product-list / feature-product-detail`；不要创建 `data-product-list` 和 `data-product-detail` 重复访问同一套商品接口。

## 修改底部导航

底部导航项目由四部分共同组成：可序列化路由、独立返回栈、目的地注册和导航栏 UI。以下示例把 `Home / Shorts` 替换为“工作台 / 我的”。

### 1. 新建顶层路由

在 `app/navigation/model` 下分别创建文件：

```kotlin
@Serializable
data object DashboardDestination : AppRoute
```

```kotlin
@Serializable
data object ProfileDestination : AppRoute
```

没有参数的顶层页面使用 `data object`；详情页或编辑页使用 `data class`，只携带稳定 ID、枚举名称等可序列化的小参数，不传 Repository、Bitmap 或完整响应对象。

### 2. 替换顶层返回栈

在 `rememberAppNavigationState()` 中替换示例返回栈：

```kotlin
val topLevelRouteState = rememberSerializable {
    mutableStateOf<AppRoute>(DashboardDestination)
}
val dashboardBackStack = rememberNavBackStack(DashboardDestination)
val profileBackStack = rememberNavBackStack(ProfileDestination)

return remember(topLevelRouteState, dashboardBackStack, profileBackStack) {
    AppNavigationState(
        topLevelRouteState = topLevelRouteState,
        backStacks = mapOf(
            DashboardDestination to dashboardBackStack,
            ProfileDestination to profileBackStack
        ),
        startRoute = DashboardDestination
    )
}
```

同时把 `AppNavigationState.navigateTopLevel()` 中针对 Home、Shorts 的固定判断改成返回栈成员判断：

```kotlin
fun navigateTopLevel(route: AppRoute) {
    require(route in backStacks) { "顶层路由未注册返回栈: $route" }
    if (topLevelRoute != route) topLevelRoute = route
}
```

新增第三个底部导航时，只需要再创建一个 `rememberNavBackStack()` 并放入 `backStacks`。每个顶层页面都拥有自己的返回栈，切换导航项不会丢失该页面内部的详情层级。

### 3. 注册页面目的地

每个页面在 `app/navigation/destination` 中拥有一个注册函数。以个人中心为例：

```kotlin
internal fun EntryProviderScope<NavKey>.registerProfileDestination() {
    entry<ProfileDestination> {
        ProfileRoute()
    }
}
```

然后在 `AppNavHost` 的 `entryProvider` 中注册：

```kotlin
val provider = entryProvider<NavKey> {
    registerDashboardDestination()
    registerProfileDestination()
}
```

把底部导航显示条件改成顶层路由判断，避免继续依赖 Home、Shorts 名称：

```kotlin
val showBottomBar = currentRoute in navigationState.backStacks.keys
```

项目不再包含短视频时，同时删除 `shortsFullscreen`、黑色背景和播放器窗口模式等示例判断。

再为两个顶层返回栈创建 decorated entries，并根据当前顶层路由选择展示内容：

```kotlin
val entryDecorators = listOf(
    rememberSaveableStateHolderNavEntryDecorator(),
    rememberViewModelStoreNavEntryDecorator()
)
val dashboardEntries = rememberDecoratedNavEntries(
    backStack = navigationState.backStacks.getValue(DashboardDestination),
    entryDecorators = entryDecorators,
    entryProvider = provider
)
val profileEntries = rememberDecoratedNavEntries(
    backStack = navigationState.backStacks.getValue(ProfileDestination),
    entryDecorators = entryDecorators,
    entryProvider = provider
)
val visibleEntries = when (navigationState.topLevelRoute) {
    DashboardDestination -> dashboardEntries
    ProfileDestination -> profileEntries
    else -> error("没有为当前顶层路由创建页面栈")
}
```

项目当前在 `AppBottomNavigationBar` 和 `AppNavigationRail` 中分别绘制紧凑屏幕与宽屏导航。修改导航项时两处都要同步替换：

```kotlin
NavigationBarItem(
    icon = {
        Icon(
            Icons.Default.Person,
            contentDescription = stringResource(R.string.navigation_profile)
        )
    },
    label = { Text(stringResource(R.string.navigation_profile)) },
    selected = selectedRoute == ProfileDestination,
    onClick = { onNavigate(ProfileDestination) }
)
```

最后在 `app/src/main/res/values/strings.xml` 增加 `navigation_dashboard`、`navigation_profile`。底部导航只放顶层页面；商品详情、订单详情等二级页面只注册路由，不放入 `backStacks` 和导航栏。

## 新增一个不访问网络的 Feature

下面创建 `feature-profile`，先演示最小页面模块。它不依赖 Feed，也不需要创建 Repository。

### 1. 注册模块

在 `settings.gradle.kts` 增加：

```kotlin
include(":feature-profile")
```

在 `app/build.gradle.kts` 增加：

```kotlin
implementation(project(":feature-profile"))
```

创建 `feature-profile/build.gradle.kts`：

```kotlin
plugins {
    alias(libs.plugins.kotlinmvvm.android.feature)
}

android {
    namespace = "com.example.feature.profile"
}

dependencies {
    implementation(project(":core-designsystem"))
    implementation(project(":core-ui"))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.material3)
}
```

### 2. 创建目录

```text
feature-profile/src/main/java/com/example/feature/profile/
├─ navigation/
│  └─ ProfileRoute.kt
├─ presentation/
│  ├─ ProfileUiState.kt
│  └─ ProfileViewModel.kt
└─ ui/
   ├─ ProfileScreen.kt
   └─ component/
```

### 3. 编写 UiState 和 ViewModel

`ProfileUiState.kt`：

```kotlin
data class ProfileUiState(
    val displayName: String = "",
    val notificationsEnabled: Boolean = false
)
```

`ProfileViewModel.kt`：

```kotlin
class ProfileViewModel : BaseViewModel<ProfileUiState>(ProfileUiState()) {
    fun changeDisplayName(value: String) {
        updateState { state -> state.copy(displayName = value) }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        updateState { state -> state.copy(notificationsEnabled = enabled) }
    }
}
```

页面动作返回 `Unit`，不向 Route 暴露协程 `Job`。业务状态全部通过 `updateState` 进入同一个 `uiState`。

### 4. 编写 Route 和 Screen

`ProfileRoute.kt`：

```kotlin
@Composable
fun ProfileRoute(modifier: Modifier = Modifier) {
    val viewModel: ProfileViewModel = viewModel { ProfileViewModel() }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ProfileScreen(
        uiState = uiState,
        onDisplayNameChanged = viewModel::changeDisplayName,
        onNotificationsChanged = viewModel::setNotificationsEnabled,
        modifier = modifier
    )
}
```

`ProfileScreen.kt`：

```kotlin
@Composable
fun ProfileScreen(
    uiState: ProfileUiState,
    onDisplayNameChanged: (String) -> Unit,
    onNotificationsChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier.padding(16.dp)) {
        OutlinedTextField(
            value = uiState.displayName,
            onValueChange = onDisplayNameChanged,
            label = { Text(stringResource(R.string.profile_display_name)) }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.profile_notifications))
            Switch(
                checked = uiState.notificationsEnabled,
                onCheckedChange = onNotificationsChanged
            )
        }
    }
}
```

同时创建 `feature-profile/src/main/res/values/strings.xml`：

```xml
<resources>
    <string name="profile_display_name">显示名称</string>
    <string name="profile_notifications">接收通知</string>
</resources>
```

Route 负责创建 ViewModel 和收集 Flow；Screen 只接收不可变状态与回调。以后切换到多 Activity、Preview 或 UI 测试时，Screen 不需要感知宿主和依赖容器。

## 新增一个带网络请求的业务域

假设个人中心需要请求账号服务。此时新增 `domain-account` 和 `data-account`，`feature-profile` 只依赖 `domain-account`。下面每个类型都放在自己的 Kotlin 文件中。

### 1. 注册领域与数据模块

`settings.gradle.kts`：

```kotlin
include(":domain-account")
include(":data-account")
```

`domain-account/build.gradle.kts`：

```kotlin
plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

dependencies {
    api(project(":core-data"))
}
```

`data-account/build.gradle.kts`：

```kotlin
plugins {
    alias(libs.plugins.kotlinmvvm.android.library)
}

android {
    namespace = "com.example.data.account"
    defaultConfig.consumerProguardFiles("consumer-rules.pro")
}

dependencies {
    api(project(":domain-account"))
    api(project(":core-network"))
}
```

`feature-profile/build.gradle.kts` 增加：

```kotlin
implementation(project(":domain-account"))
```

### 2. 定义稳定的领域契约

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

`AccountLoadResult.kt`、`AccountLoadSuccess.kt`、`AccountLoadFailure.kt`：

```kotlin
sealed interface AccountLoadResult
```

```kotlin
data class AccountLoadSuccess(val profile: UserProfile) : AccountLoadResult
```

```kotlin
data class AccountLoadFailure(val error: AccountLoadError) : AccountLoadResult
```

只有一次加载能力时，Repository 直接复用公共 `load()`：

```kotlin
interface AccountRepository : CommonRepository<Unit, AccountLoadResult>
```

如果账号域以后需要观察登录态、更新资料或退出登录，就把这些真实能力直接增加到 `AccountRepository`；不要为了保持“公共”而给所有 Repository 强加刷新和分页函数。

### 3. 编写 Retrofit Service 与 DTO

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

服务端字段允许为空，DTO 到领域模型时再执行完整校验：

```kotlin
internal fun AccountProfileDto.toDomain(): UserProfile? {
    val validId = id?.takeIf(String::isNotBlank) ?: return null
    val validName = nickname?.takeIf(String::isNotBlank) ?: return null
    return UserProfile(id = validId, displayName = validName)
}
```

### 4. 编写业务远程数据源

`AccountRemoteDataSource.kt`：

```kotlin
internal interface AccountRemoteDataSource {
    suspend fun loadProfile(): NetworkResult<AccountProfileDto>
}
```

`RetrofitAccountRemoteDataSource.kt`：

```kotlin
internal class RetrofitAccountRemoteDataSource(
    private val service: AccountApiService,
    private val networkDataSource: NetworkDataSource
) : AccountRemoteDataSource {
    override suspend fun loadProfile() =
        networkDataSource.execute(service::profile)
}
```

业务 DataSource 不再重复写 try/catch、HTTP code 和超时分类；统一执行逻辑由 `RetrofitNetworkDataSource` 处理。

### 5. 实现 Repository

`DefaultAccountRepository.kt`：

```kotlin
internal class DefaultAccountRepository(
    private val remoteDataSource: AccountRemoteDataSource
) : AccountRepository {
    override suspend fun load(params: Unit): AccountLoadResult =
        when (val result = remoteDataSource.loadProfile()) {
            is NetworkSuccess -> result.value.toDomain()
                ?.let(::AccountLoadSuccess)
                ?: AccountLoadFailure(AccountLoadError.INVALID_RESPONSE)

            is NetworkError -> AccountLoadFailure(result.error.toAccountLoadError())
        }
}
```

`AccountNetworkFailureMapper.kt`：

```kotlin
internal fun NetworkFailure.toAccountLoadError(): AccountLoadError = when (this) {
    NetworkConnectionFailure -> AccountLoadError.NO_CONNECTION
    NetworkTimeoutFailure -> AccountLoadError.TIMEOUT
    is NetworkHttpFailure -> if (statusCode == 401 || statusCode == 403) {
        AccountLoadError.UNAUTHORIZED
    } else {
        AccountLoadError.UNKNOWN
    }
    else -> AccountLoadError.INVALID_RESPONSE
}
```

Feature 只能看到 `AccountLoadResult`，不会接触 Retrofit `Response`、HTTP 异常或 DTO。

### 6. 在 data 模块内部装配实现

`AccountDataBindings.kt`：

```kotlin
class AccountDataBindings internal constructor(
    val repository: AccountRepository
)
```

`AccountDataGraph.kt`：

```kotlin
fun createAccountDataBindings(
    networkClientFactory: NetworkClientFactory,
    endpoint: NetworkEndpoint,
    networkFailureObserver: NetworkFailureObserver = NetworkFailureObserver.None
): AccountDataBindings {
    val service = networkClientFactory.createService(
        endpoint,
        AccountApiService::class.java
    )
    return AccountDataBindings(
        repository = DefaultAccountRepository(
            remoteDataSource = RetrofitAccountRemoteDataSource(
                service = service,
                networkDataSource = RetrofitNetworkDataSource(
                    failureObserver = networkFailureObserver
                )
            )
        )
    )
}
```

Service、DTO、Mapper、DataSource 和具体 Repository 都留在 `data-account` 内部，应用层只调用这一个装配入口。

### 7. 注册新的 Base URL

在 `AppNetworkEndpoints` 增加账号服务，而不是覆盖一个全局 Base URL：

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

商品服务、上传服务和即时通信服务可以继续增加各自的 Endpoint。它们共享同一个 OkHttp 连接池，但各自拥有独立 Retrofit 和 Base URL。

如果接口返回头像、图片或文件 URL，还要在 `AppRemoteUrlPolicies` 登记真实 CDN Host：

```kotlin
val accountMedia = RemoteResourceUrlPolicy(
    allowedHosts = setOf("cdn.example.com")
)
```

接口不返回远程资源 URL 时不需要创建这项策略。

### 8. 接入应用依赖容器

在 `AppDependencies` 增加稳定的领域角色：

```kotlin
val accountRepository: AccountRepository
```

在 `AppContainer` 中装配：

```kotlin
private val accountBindings by lazy {
    createAccountDataBindings(
        networkClientFactory = networkClientFactory,
        endpoint = AppNetworkEndpoints.account(environment),
        networkFailureObserver = diagnosticObserver
    )
}

override val accountRepository: AccountRepository
    get() = accountBindings.repository
```

`MainActivity` 从 `dependencies` 取得仓库并传给 `AppNavHost`，`AppNavHost` 再通过 `registerProfileDestination()` 交给 `ProfileRoute`。Activity 和 Feature 始终依赖 `AccountRepository`，不会依赖 `DefaultAccountRepository`。

### 9. 在 ViewModel 中发起一次请求

先让 `ProfileUiState` 表达请求需要的内容、Loading 和稳定业务错误：

```kotlin
data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = false,
    val error: AccountLoadError? = null,
    val notificationsEnabled: Boolean = false
)
```

再将 `ProfileViewModel` 改为接收仓库：

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
            .onEach(::applyResult)
            .launchLatestIn(
                taskKey = TASK_LOAD_PROFILE,
                onLoadingChanged = { loading ->
                    updateState { state -> state.copy(isLoading = loading) }
                }
            )
    }

    private fun applyResult(result: AccountLoadResult) {
        updateState { state ->
            when (result) {
                is AccountLoadSuccess -> state.copy(
                    profile = result.profile,
                    error = null
                )
                is AccountLoadFailure -> state.copy(error = result.error)
            }
        }
    }

    private companion object {
        const val TASK_LOAD_PROFILE = "profile.load"
    }
}
```

这就是“没有刷新、没有下一页、只有一次网络请求”的标准写法。只有页面真的需要刷新、搜索、保存或分页时，才在业务 Repository 中增加相应函数。

## 新增普通二级页面

不出现在底部导航的页面不需要独立顶层返回栈。例如商品详情：

```kotlin
@Serializable
data class ProductDetailDestination(
    val productId: String
) : AppRoute
```

注册目的地：

```kotlin
internal fun EntryProviderScope<NavKey>.registerProductDetailDestination(
    repository: ProductRepository,
    onBack: () -> Unit
) {
    entry<ProductDetailDestination> { destination ->
        ProductDetailRoute(
            productId = destination.productId,
            repository = repository,
            onBack = onBack
        )
    }
}
```

页面点击时统一调用：

```kotlin
navigationState.navigate(ProductDetailDestination(productId))
```

它会进入当前顶层页面的返回栈，返回时仍停留在原来的底部导航项和页面状态。

## 删除开眼与视频示例

自己的首个业务链路接通后，可以按下面顺序移除示例：

1. 从 `AppNavHost` 删除 Home、Shorts、VideoDetail 的路由注册和导航项。
2. 从 `AppNavigationState` 删除 Home、Shorts 返回栈，换成自己的顶层路由。
3. 从 `MainActivity` 和 `AppDependencies` 删除 Feed Repository、视频播放器与视频 Activity 参数。
4. 从 `AppContainer` 删除 `createFeedDataBindings()`、Feed Endpoint 和开眼媒体 Host。
5. 从 `app/build.gradle.kts` 删除不再使用的 `feature-home`、`feature-shorts`、`feature-detail`、`domain-feed`、`data-feed` 依赖。
6. 从 `settings.gradle.kts` 删除对应模块注册，再删除模块目录。
7. 项目完全不播放视频时，再删除 `core-player` 依赖和模块；仍需播放业务视频时保留它，只替换资源 Host 和业务页面。

不要把 `FeedApiService` 改名后继续塞入完全不同的账号或商品接口。新业务应拥有自己的 Service、DTO、DataSource 和 Repository，这样示例代码才能被干净删除，后续维护者也能从模块名直接判断业务归属。

## 常见扩展选择

| 场景 | 推荐做法 |
| --- | --- |
| 一次普通请求 | `CommonRepository.load()` + `taskFlow()` |
| 页面进入后持续观察数据 | 在业务 Repository 声明 `Flow/StateFlow`，ViewModel 使用 `launchLatestIn()` |
| 防止重复提交 | 使用 `launchUniqueIn()` |
| 搜索词或筛选条件变化 | 使用官方 Flow 操作符组合，再用 `launchLatestIn()` 替换旧任务 |
| 下拉刷新 | 在业务 Repository 增加明确的 `refresh*()`，使用独立刷新 Loading |
| 分页 | Repository 持有分页位置并增加 `loadNext*()`，页面不自行拼接下一页 URL |
| 保存、删除、上传 | 在业务 Repository 增加对应动词函数，不放进通用 Repository |
| 多个 ViewModel 复用同一段业务编排 | 增加 UseCase，并让 UseCase 依赖领域 Repository |
| 手动容器变得庞大 | 使用 Hilt/Dagger 实现 `AppDependencies`，Feature 和 Activity 契约保持不变 |
