# 新增功能接入指南

这份文档面向两个目标：

1. 新增一个页面或一个业务功能时，知道应该改哪些层。
2. 新增类时能直接照着现有范式写，不需要在仓库里到处搜索拼装。

## 1. 开始前先判断归属

先回答两个问题：

### 1.1 这个功能是不是宿主级能力

如果是下面这些，优先考虑放在 `app` 或基础库：

- 首页聚合逻辑
- 宿主启动流程
- 应用级 Web Bridge 能力
- 全局特性开关
- 宿主导航门面

### 1.2 这个功能是不是独立业务域

如果功能能独立演进，优先做成业务模块，例如：

- 登录能力放在 `module_login`
- 网络抓包放在 `Feature_Capture`
- 下载、上传能力已经分别独立到 `Lib_Download`、`Lib_Upload`

## 2. 新增一个普通页面

以新增一个 `ProfileActivity` 为例，推荐路径如下。

### 2.1 创建布局

先创建页面布局，例如：

```text
app/src/main/res/layout/activity_profile.xml
```

布局层尽量只负责：

- 结构
- 控件 id
- 简单展示逻辑

不要把业务判断大量堆在 XML 表达式里。

### 2.2 创建 Activity

页面优先继承 `BaseActivity`：

```kotlin
class ProfileActivity : BaseActivity<ActivityProfileBinding, ProfileViewModel>() {

    override fun initContentView(savedInstanceState: Bundle?): ActivityProfileBinding {
        return ActivityProfileBinding.inflate(layoutInflater)
    }

    override fun initView() {
        mBinding.saveButton.setOnClickListener {
            mViewModel.saveProfile()
        }
    }

    override fun initData() {
        mViewModel.loadProfile()
    }
}
```

推荐原因：

- `ViewBinding` 已统一接入
- `TheRouter.inject(this)` 已在基类处理
- Koin ViewModel 解析已在基类处理
- 通用导航、Dialog、Toast、标题栏能力可直接复用

### 2.3 创建 Fragment

如果是 Fragment，优先继承 `BaseFragment`：

```kotlin
class ProfileFragment : BaseFragment<FragmentProfileBinding, ProfileViewModel>() {

    override fun initContentView(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): FragmentProfileBinding {
        return FragmentProfileBinding.inflate(inflater, container, false)
    }

    override fun initView() {
        mBinding.refreshButton.setOnClickListener {
            mViewModel.reload()
        }
    }

    override fun lazyLoadData() {
        mViewModel.loadProfile()
    }
}
```

## 3. 新增 ViewModel

项目当前约定优先使用 `@KoinViewModel`：

```kotlin
@KoinViewModel
class ProfileViewModel(
    application: Application,
    private val profileRepository: ProfileRepository
) : BaseViewModel(application) {

    fun loadProfile() {
        launchOnlyresult(
            block = { profileRepository.loadProfile() },
            success = { data ->
                // 更新状态
            }
        )
    }
}
```

建议：

- 页面状态与动作放在 ViewModel
- 页面跳转优先调用 `AppRouter`
- 登录、权限、防抖、网络校验等优先用 AOP 注解

### 3.1 使用 EventChannel 做跨页面或全局事件通信

适合这些场景：

- 登录成功后通知多个页面刷新
- Token 失效后广播统一退出
- 配置、语言、主题等全局状态变化

发送事件：

```kotlin
EventChannel.post(GlobalEvent.TokenExpired("token invalid"))
```

在 `ViewModel` 中订阅：

```kotlin
EventChannel.observe<GlobalEvent>(sticky = true)
    .collectIn(viewModelScope) { event ->
        handleGlobalEvent(event)
    }
```

在 `Activity` / `Fragment` 中订阅：

```kotlin
EventChannel.observeEvent<GlobalEvent>(sticky = true) { event ->
    // 更新页面
}
```

建议：

- 全局事件类型优先用 `sealed class`
- 真正需要补收最近状态时再用 `sticky = true`
- 页面销毁自动取消时，优先使用 `observeEvent` 或 `collectIn(owner)`

## 4. 新增 Repository

仓库类优先使用 `@Single`：

```kotlin
@Single
class ProfileRepository(
    networkApiFactory: NetworkApiFactory
) {
    private val api: ProfileApiService = networkApiFactory.get()

    suspend fun loadProfile() = api.profile()
}
```

建议：

- Repository 只关心数据获取与转换
- 不要把 Activity/Fragment 直接传进 Repository
- 不要在页面里自己创建 Retrofit Service，统一走 `NetworkApiFactory`

## 5. 新增 Koin 模块

### 5.1 在已有模块里新增类

如果类放在已经被 `@ComponentScan` 扫描的包下，通常不需要额外配置，只要：

- `@Single`
- `@KoinViewModel`
- `@Single(binds = [...])`

写对即可。

### 5.2 新增一个业务模块

需要补三步：

1. 新建模块入口：

```kotlin
@Module
@ComponentScan("com.ghn.feature.profile")
class ProfileModule
```

2. 确保模块启用了 `ksp`

3. 在 `appModules` 中汇总：

```kotlin
val appModules = listOf(
    ProfileModule().module,
    AppModule().module
)
```

## 6. 新增跨模块页面跳转

如果页面只在模块内使用，可以直接本地跳转。

如果页面需要被其他模块访问，按下面流程走。

### 6.1 在 `Lib_Router` 定义路由常量

```kotlin
object RouterPath {
    object Profile {
        const val MAIN = "/profile/main"
    }
}
```

### 6.2 在 `Lib_Router` 定义契约接口

```kotlin
interface ProfileRouter {
    fun openProfile(context: Context? = null)
}
```

### 6.3 在拥有页面的模块中提供实现

```kotlin
@Singleton
@ServiceProvider(returnType = ProfileRouter::class)
class ProfileNavigatorProvider : ProfileRouter {
    override fun openProfile(context: Context?) {
        RouterPath.Profile.MAIN.navigate(context)
    }
}
```

### 6.4 在 `AppRouter` 中提供语义化入口

```kotlin
fun openProfile(context: Context? = null) {
    requireRouterService<ProfileRouter>().openProfile(context)
}
```

这样之后业务层就统一用：

```kotlin
AppRouter.openProfile()
```

不建议在多个页面里直接散落 `TheRouter.build("/xxx").navigation()`。

## 7. 新增 AOP 保护

当前项目已经提供很多现成注解，优先复用：

| 场景 | 优先注解 |
| --- | --- |
| 防重复点击 | `@PreventRepeat` |
| 需要先登录 | `@LoginRequired` |
| 需要网络 | `@RequireNetwork` |
| 需要相机权限 | `@RequireCameraPermission` |
| 需要图片读取权限 | `@RequireImageReadPermission` |
| 需要耗时埋点 | `@TraceTime` |
| 仅 Debug 可用 | `@DebugOnly` |
| 功能开关控制 | `@FeatureEnabled` |

示例：

```kotlin
@LoginRequired(message = "请先登录后再查看资料")
@PreventRepeat(key = "open_profile", intervalMillis = 800L)
fun openProfile() {
    AppRouter.openProfile()
}
```

注意：

- `@LoginRequired` 的“登录后恢复”只适用于返回 `Unit/void` 的方法
- `@PreventRepeat` 的 `key` 最好有业务语义，避免不同动作互相影响
- 权限类注解需要有可解析的 `Activity/Fragment` 上下文

## 8. 新增启动任务

只有真正影响首屏能否工作的任务，才应该放进 `BLOCKING_MAIN`。

示例：

```kotlin
StartupTask("profile-preload", StartupPhase.AFTER_FIRST_FRAME_BACKGROUND) {
    profileLibrary.warmUp(it)
}
```

选择阶段时可以按这个原则：

- `BLOCKING_MAIN`：页面创建前必须准备好的能力
- `AFTER_FIRST_FRAME_MAIN`：首帧后仍需在主线程执行
- `AFTER_FIRST_FRAME_BACKGROUND`：纯预热、纯后台能力

不要把大体量初始化重新塞回 `Application.onCreate()`。

## 9. 新增 Web Bridge 能力

如果功能要给 H5 调用，优先新增一个 `AnnotatedWebBridgeModule`：

```kotlin
@Single(binds = [WebBridgeModule::class])
class ProfileWebBridgeModule : AnnotatedWebBridgeModule() {

    override val group: String = "profile"

    @BridgeHandler("profile.open")
    private fun openProfile(activity: FragmentActivity) {
        AppRouter.openProfile(activity)
    }
}
```

建议：

- 一个模块维护一组清晰的 Bridge 指令
- Bridge 内部仍然优先调用 `AppRouter`
- 登录、权限、抓包控制等继续交给 AOP 注解处理

## 10. 新类命名建议

| 类型 | 推荐命名 |
| --- | --- |
| Activity | `XxxActivity` |
| Fragment | `XxxFragment` |
| ViewModel | `XxxViewModel` |
| Repository | `XxxRepository` |
| 路由协议 | `XxxRouter` |
| 路由实现 | `XxxNavigatorProvider` |
| Koin 模块 | `XxxModule` |
| Bridge 模块 | `XxxWebBridgeModule` |

## 11. 一个完整的最小接入清单

如果你要新增一个可跨模块打开的页面，通常至少会改这些点：

1. 新建 `layout`
2. 新建 `Activity` 或 `Fragment`
3. 新建 `ViewModel`
4. 需要数据时新建 `Repository`
5. 在 `Lib_Router` 增加 `RouterPath`
6. 在 `Lib_Router` 增加 `XxxRouter`
7. 在业务模块增加 `XxxNavigatorProvider`
8. 在 `AppRouter` 增加一个语义化入口
9. 按需加 `@PreventRepeat`、`@LoginRequired`、`@RequireNetwork`

## 12. 常见误区

- 不要在页面里直接 `new Repository()` 或自己创建 Retrofit Service
- 不要把跨模块页面实现类直接暴露给别的模块
- 不要把所有初始化重新堆回 `Application.onCreate()`
- 不要在多个页面里复制登录校验、防抖、权限判断代码
- 不要让 Bridge 代码直接操作太多页面细节，优先走 Router 或 ViewModel
