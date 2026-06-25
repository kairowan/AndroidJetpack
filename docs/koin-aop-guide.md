# Koin、AOP 与 Router 使用说明

这份文档专门说明项目里新增的几类基础能力怎么用，以及使用时最容易踩的坑。

## 1. Koin 使用约定

### 1.1 已有接入方式

项目当前使用的是：

- `Koin 4`
- `Koin Annotations`
- `KSP`

接入目标是：

- 少写手工 `module { }`
- 让业务类更接近声明式注册
- 把模块扫描入口收口到少数几个位置

### 1.2 常用注解

#### `@KoinViewModel`

用于页面 ViewModel：

```kotlin
@KoinViewModel
class ProfileViewModel(
    application: Application,
    private val repository: ProfileRepository
) : BaseViewModel(application)
```

#### `@Single`

用于单例仓库、服务、Bridge 模块等：

```kotlin
@Single
class ProfileRepository(
    private val networkApiFactory: NetworkApiFactory
)
```

#### `@Single(binds = [...])`

用于接口到实现的绑定：

```kotlin
@Single(binds = [WebBridgeModule::class])
class ProfileWebBridgeModule : AnnotatedWebBridgeModule()
```

#### `@Module + @ComponentScan`

用于模块扫描入口：

```kotlin
@Module
@ComponentScan("com.ghn.feature.profile")
class ProfileModule
```

### 1.3 模块如何真正生效

仅仅写了注解还不够，业务模块最终还需要被汇总到 `appModules`。

当前汇总入口：

```kotlin
val appModules = listOf(
    CaptureModule().module,
    LoginModule().module,
    downloadModule,
    uploadModule,
    AppModule().module
)
```

结论：

- 新增类放在已扫描包下时，通常不需要额外处理
- 新增业务模块时，记得把模块入口加入 `appModules`

### 1.4 Koin 使用建议

- Repository、Service、Bridge Module 优先用 `@Single`
- ViewModel 优先用 `@KoinViewModel`
- 页面不要自己 new 依赖
- 不要把模块入口散落到很多地方，统一在 `appModules` 汇总

## 2. AOP 使用约定

项目现在把很多高频横切逻辑都转成了注解。

### 2.1 `@PreventRepeat`

用途：

- 防重复点击
- 防短时间内重复触发同一动作

示例：

```kotlin
@PreventRepeat(
    intervalMillis = 1000L,
    key = "open_profile",
    message = "页面打开中，请勿重复操作"
)
fun openProfile() {
    AppRouter.openProfile()
}
```

建议：

- `key` 最好语义化，不要全局都用默认值
- 适合点按钮、开页面、Bridge 指令入口

### 2.2 `@LoginRequired`

用途：

- 未登录时拦截动作
- 可选登录后自动恢复原动作

示例：

```kotlin
@LoginRequired(message = "请先登录后再继续")
fun openUserKey() {
    AppRouter.openUserKey()
}
```

重要限制：

- 自动恢复只支持返回 `Unit/void` 的方法
- 如果方法需要恢复执行，尽量不要设计成有返回值的同步接口

### 2.3 `@RequireNetwork`

用途：

- 执行动作前先检查网络可用性

示例：

```kotlin
@RequireNetwork(message = "当前无网络，暂时无法提交")
fun submit() {
    // ...
}
```

适合：

- 登录
- 提交表单
- 刷新数据

### 2.4 `@TraceTime`

用途：

- 记录同步函数或挂起函数耗时
- 超过阈值时打 `WARN`

示例：

```kotlin
@TraceTime("profile_load", warnAtMillis = 24L)
suspend fun loadProfile() { ... }
```

适合：

- 启动任务
- 网络封装
- 解析逻辑
- 抓包检索

### 2.5 权限注解

当前已提供：

- `@RequireCameraPermission`
- `@RequireImageReadPermission`
- `@RequireMediaPermission`
- 更通用的 `@RequirePermission`

示例：

```kotlin
@RequireCameraPermission(tag = "profile_camera_permission")
fun openCamera() {
    // 权限通过后才会进入这里
}
```

建议：

- 页面交互优先用这些注解，不要每个页面重复写权限申请模板
- `tag` 要有业务语义，方便日志与状态识别

### 2.6 其他路由相关注解

#### `@FeatureEnabled`

用于功能开关：

```kotlin
@FeatureEnabled(
    featureKey = FeatureKeys.FONT_SETTINGS,
    message = "字体设置功能暂未开放"
)
fun openFontSettings() { ... }
```

#### `@DebugOnly`

用于 Debug 专属能力，例如调试入口、抓包入口。

#### `@NetworkCaptureAccess`

用于限制抓包能力的访问。

#### `@MarkLoginSuccess`

用于登录流程完成后恢复挂起动作。

### 2.7 AOP 使用建议

- 优先给“动作入口”加注解，而不是给内部细碎小函数到处加
- 登录、权限、防抖、网络守卫等能复用现成注解时，不要重新造一套 if/else
- 一个函数如果已经承担复杂业务，不要再叠太多职责，必要时先拆函数

## 3. Router 使用约定

### 3.1 三层结构

当前路由统一分成三层：

1. `RouterPath`：路径常量
2. `XxxRouter`：能力契约
3. `XxxNavigatorProvider`：模块内实现

对业务调用方而言，优先通过 `AppRouter` 使用。

### 3.2 推荐写法

#### 定义协议

```kotlin
interface ProfileRouter {
    fun openProfile(context: Context? = null)
}
```

#### 提供实现

```kotlin
@Singleton
@ServiceProvider(returnType = ProfileRouter::class)
class ProfileNavigatorProvider : ProfileRouter {
    override fun openProfile(context: Context?) {
        RouterPath.Profile.MAIN.navigate(context)
    }
}
```

#### 在门面中暴露

```kotlin
fun openProfile(context: Context? = null) {
    requireRouterService<ProfileRouter>().openProfile(context)
}
```

## 3.3 为什么不建议直接写字符串跳转

- 页面路径改动时影响面不可控
- 无法统一挂登录、防抖、功能开关等 AOP 守卫
- 模块间会逐步耦合到具体页面实现

## 4. 启动任务与这些能力的关系

当前启动链路里最关键的三个阻塞任务是：

- `initKoin`
- `initAutoSize`
- `initRouter`

这意味着：

- 页面级依赖注入依赖 Koin 已就绪
- 语义化路由调用依赖 Router Provider 已注册完成
- 首屏尺寸适配依赖 AutoSize 已完成初始化

如果后续新增基础能力，不要默认塞到冷启动阻塞阶段，先判断是不是首屏刚需。

## 5. 常见问题

### Q1：为什么我写了 `@Single` 还是注入不到？

优先检查：

1. 类是否在 `@ComponentScan` 覆盖的包下
2. 模块入口是否已经加入 `appModules`
3. 模块是否启用了 `ksp`

### Q2：为什么 `@LoginRequired` 没有恢复原动作？

通常有两个原因：

1. 目标方法不是 `Unit/void` 返回
2. 登录成功时原始页面/宿主已经销毁

### Q3：为什么 `@RequireNetwork` 或权限注解没有拦截？

如果切面拿不到可解析的 `Context` / `Activity` / `Fragment`，会降级跳过或无法继续，因此入口函数最好定义在页面、ViewModel 的动作入口或明确可解析的宿主对象上。

### Q4：为什么不建议在页面里直接 new Repository？

因为这会绕开：

- Koin 统一装配
- 可替换的数据依赖关系
- 模块化的可演进性

## 6. 推荐阅读顺序

1. 先看 [architecture.md](architecture.md)
2. 再看 [feature-development.md](feature-development.md)
3. 需要写依赖注入或横切逻辑时，再回到本页查细节
