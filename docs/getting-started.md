# KMP / CMP 新人上手教程

这份教程面向第一次接触 Kotlin Multiplatform（KMP）和 Compose Multiplatform
（CMP）的开发者。目标不是先学完所有概念，而是让你能安全地运行项目、找到代码、
修改页面、接入数据，并知道什么时候需要写平台代码。

> 最重要的一句话：能在 Android 和 iOS 共用的状态与 Compose UI 放在
> `commonMain`；必须调用 Android 或 iOS SDK 的代码留在对应平台目录。

## 1. 先理解三个名词

- **KMP**：让 Kotlin 业务代码可以编译到 Android、iOS、Desktop 和 Web。
- **CMP**：让 Compose UI 也可以跨平台复用。
- **宿主应用**：负责启动共享代码并提供平台能力。Android 宿主是 `app`，iOS
  宿主是 `iosApp + shared_ios`。

这个项目不是“所有代码都共享”。它有意把代码分成两类：

```text
可以共享
  页面布局、页面状态、分页、展示模型、业务规则、导航规则

必须平台实现
  Android Activity/ViewModel 生命周期、Coil、Media3、OkHttp 网络引擎
  iOS UIViewController、UIImageView、AVPlayer、Foundation 网络引擎
```

判断代码应该放哪里的最简单方法：

```text
这段代码是否 import android.*、Coil、Media3？
  是 → androidMain 或 app

这段代码是否 import platform.UIKit、Foundation、AVFoundation？
  是 → iosMain 或 iosApp

都不是，并且两端业务/界面一致？
  是 → commonMain
```

## 2. 第一次运行

### 2.1 环境要求

- macOS（运行 iOS 必需）
- JDK 17
- Android Studio 和 Android SDK 36
- Xcode

项目当前使用 Kotlin 2.3.20、Compose Multiplatform 1.11.1、AGP 8.11.1 和
Gradle 8.13。版本统一维护在
[`gradle/libs.versions.toml`](../gradle/libs.versions.toml)，不要在单个模块里重复写版本。

### 2.2 运行 Android

在 Android Studio 中打开仓库根目录，等待 Gradle Sync 完成，选择 `app`
运行配置和一个模拟器，然后点击 Run。

也可以先用命令确认工程能够构建：

```bash
./gradlew :app:assembleDebug
```

调试 APK 会生成在：

```text
app/build/outputs/apk/debug/app-debug.apk
```

### 2.3 运行 iOS

不要在 Xcode 里重新实现一套页面。iOS 的 SwiftUI 只负责承载 CMP 控制器。

```bash
open iosApp/iosApp.xcodeproj
```

在 Xcode 中：

1. Scheme 选择 `iosApp`。
2. 设备选择一个 iPhone Simulator。
3. 点击 Run。

Xcode 的构建脚本会自动执行
`:shared_ios:embedAndSignAppleFrameworkForXcode`，不需要手动复制 framework。

如果只看到 `invalid reuse after initialization failure`，且没有具体 Swift
源码报错，先执行 `Product > Clean Build Folder`（`⇧⌘K`）再运行。

## 3. 不需要读完整个项目：先看这 8 个文件

建议按下面的顺序阅读：

1. [`HomeScreen.kt`](../feature_home/src/commonMain/kotlin/com/kotlinmvvm/feature/home/HomeScreen.kt)：
   Android/iOS 共用的首页 UI。
2. [`HomeFeedPageModel.kt`](../feature_home/src/commonMain/kotlin/com/kotlinmvvm/feature/home/HomeFeedPageModel.kt)：
   首页渲染需要的不可变数据。
3. [`HomeFeedStateHolder.kt`](../feature_home/src/commonMain/kotlin/com/kotlinmvvm/feature/home/HomeFeedStateHolder.kt)：
   首页加载、刷新、分页和切换频道。
4. [`HomeRoute.kt`](../feature_home/src/androidMain/kotlin/com/kotlinmvvm/feature/home/HomeRoute.kt)：
   Android 如何把状态、Coil 图片和回调接入共享页面。
5. [`IosAppController.kt`](../shared_ios/src/iosMain/kotlin/com/kotlinmvvm/shared/ios/IosAppController.kt)：
   iOS 如何接入同一个共享页面。
6. [`FeedPageRepository.kt`](../domain-feed/src/commonMain/kotlin/com/kotlinmvvm/domain/feed/repository/FeedPageRepository.kt)：
   Feature 能看到的数据接口。
7. [`AppNavigation.kt`](../app/src/main/java/com/ghn/cocknovel/navigation/AppNavigation.kt)：
   Android 页面导航和底部栏如何组装。
8. [`NetworkClient.kt`](../core_network/src/commonMain/kotlin/com/kotlinmvvm/core/network/NetworkClient.kt)：
   Android/iOS 共用的请求、超时与重试；取消会沿协程自动传到底层请求。

读完以后，你会看到同一条数据流：

```text
Repository
    ↓ 返回领域数据
StateHolder
    ↓ 暴露 StateFlow
Presenter
    ↓ 转成 PageModel
Screen
    ↓ 用户点击产生 callback
Route / iOS Host
    ↓ 调用 StateHolder 或导航
```

这就是项目使用的 UDF（单向数据流）：**数据向下，事件向上**。

## 4. 目录怎么读

### 4.1 Source Set

每个 KMP 模块内部可能包含：

| 目录 | 放什么 | 能使用的 API |
| --- | --- | --- |
| `src/commonMain` | 共享业务、状态、模型、CMP UI | Kotlin 与跨平台依赖 |
| `src/androidMain` | Android 接线和平台实现 | Android SDK |
| `src/iosMain` | iOS 接线和平台实现 | Foundation/UIKit/AVFoundation |
| `src/commonTest` | 跨平台单元测试 | `kotlin.test` |

Android Studio 默认的 Android 视图可能隐藏 source set。找不到文件时，把左侧项目
视图切换为 **Project**。

### 4.2 模块速查

| 你要修改的内容 | 首先去哪里 |
| --- | --- |
| 首页布局 | `feature_home/src/commonMain/.../HomeScreen.kt` |
| 短视频布局 | `feature_media/src/commonMain/.../ShortsScreen.kt` |
| 详情布局 | `feature_media/src/commonMain/.../VideoDetailScreen.kt` |
| 首页加载/刷新/分页 | `feature_home/src/commonMain` |
| Shorts/详情状态 | `feature_media/src/commonMain` |
| 主题颜色和字体 | `core_ui/src/commonMain/.../designsystem/theme` |
| 通用 Compose 组件 | `core_ui/src/commonMain` |
| 导航规则 | `core_navigation`、`core_ui` |
| Android 页面接线 | Feature 的 `src/androidMain` |
| 通用网络请求、重试、缓存和错误 | `core_network/src/commonMain` |
| Android/iOS 网络引擎 | `core_network/src/androidMain`、`core_network/src/iosMain` |
| iOS 页面接线、图片和播放器 | `shared_ios/src/iosMain` |
| 接口 DTO、数据映射 | `core_data/src/commonMain` |
| Android 应用依赖装配 | `app/.../di/AppContainer.kt` |
| iOS Swift 入口 | `iosApp/iosApp/ContentView.swift` |

当前工程只有 13 个 Gradle 模块，状态、Presenter 和 Screen 已经集中在所属 Feature。
`core_network` 是明确的跨业务平台边界，同时被数据仓库和 iOS 图片加载使用。
普通 UI 调整通常只需要改一个 `commonMain` 文件。

## 5. 第一个练习：修改 Android 和 iOS 共用 UI

例如修改首页视频封面的高度：

1. 打开
   [`HomeScreen.kt`](../feature_home/src/commonMain/kotlin/com/kotlinmvvm/feature/home/HomeScreen.kt)。
2. 找到封面使用的 `height(200.dp)`。
3. 修改为需要的尺寸。
4. 分别运行 Android 和 iOS。

因为 `HomeScreen` 位于 `commonMain`，这次修改会同时作用于 Android 和 iOS。

一个共享 `Screen` 应该保持下面的形状：

```kotlin
@Composable
fun ExampleScreen(
    pageModel: ExamplePageModel,
    onRetry: () -> Unit,
    onItemClick: (ItemModel) -> Unit,
    image: @Composable (
        url: String,
        contentDescription: String,
        modifier: Modifier
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    // 只根据 pageModel 绘制 UI，并通过 callback 上报事件
}
```

共享 `Screen` 中不要：

- 创建 Repository 或网络客户端；
- 创建 Android ViewModel；
- 查找 Activity 或生命周期 owner；
- 直接使用 Coil、Media3、UIKit 或 AVPlayer；
- 在点击事件里直接改变跨页面业务状态。

## 6. 为什么图片和视频要用 platform slot

`HomeScreen` 需要显示网络图片，但 Coil 不能直接用于 iOS。因此共享页面只声明
“这里需要一张图片”：

```kotlin
image(
    video.coverUrl,
    video.title,
    Modifier.fillMaxWidth()
)
```

具体实现由平台注入：

```text
Android HomeRoute
  → Coil AsyncImage

iOS IosAppController
  → IosRemoteImage / UIImageView
```

视频同理：

```text
共享 ShortsScreen / VideoDetailScreen
  → 声明 videoSurface Composable 插槽

Android Route
  → Media3

iOS Host
  → AVPlayerViewController
```

如果以后接入地图、扫码、相机，也优先沿用这个方式：

1. 共享 `Screen` 增加一个最小 Composable lambda；
2. Android Route 注入 Android 实现；
3. `IosAppController` 注入 iOS 实现。

不要为了共享而把平台 SDK 包装进 `commonMain`。

## 7. 修改状态或业务逻辑

首页状态位于
[`HomeFeedStateHolder.kt`](../feature_home/src/commonMain/kotlin/com/kotlinmvvm/feature/home/HomeFeedStateHolder.kt)。
它由 Android ViewModel 和 iOS Compose scope 共同使用。

职责分工如下：

```text
StateHolder
  负责加载、刷新、分页、失败恢复、切换频道

Presenter
  把领域数据转换成页面容易使用的 PageModel

Screen
  只负责显示 PageModel
```

例如要增加“当前频道标题”：

1. 在 `HomeFeedPageModel` 增加不可变字段；
2. 在 `HomeFeedPagePresenter.present()` 计算该字段；
3. 在 `HomeScreen` 显示它；
4. 为 Presenter 增加或更新一个 `commonTest`。

不要在 Android `HomeViewModel` 和 iOS `IosAppController` 各计算一次。否则两端很快会
再次出现行为差异。

## 8. 修改接口数据：沿着一条链路走

当接口新增字段时，按下面的顺序修改：

```text
core_data/EyepetizerPayload.kt
  接口原始字段
        ↓
core_data/EyepetizerPayloadMapper.kt
  DTO → 领域模型
        ↓
core_model/EyepetizerModel.kt
  两端共同理解的业务数据
        ↓
feature_home 或 feature_media 中的 *Presenter.kt
  领域模型 → 页面模型
        ↓
feature_*/commonMain/*Screen.kt
  显示
```

每一层只做自己的转换：

- DTO 允许贴近服务端结构；
- 领域模型不包含 Retrofit、URL 请求对象或序列化注解；
- PageModel 直接服务 UI，例如已经格式化好的副标题；
- Screen 不解析接口结构。

如果只是改变标题拼接格式，通常从 Presenter 开始改，不需要动 DTO 和 Repository。

如果只是改变卡片间距，直接改 Screen，不需要动状态层。

### 8.1 新增网络请求

不要在 Feature、ViewModel 或 Screen 中创建 OkHttp、Retrofit、`NSURLSession`。
平台组合根已经创建并复用了同一个 `NetworkClient`，Repository 只需要接收它：

```kotlin
class ExampleRepository(
    private val networkClient: NetworkClient
) {
    suspend fun load(): String =
        networkClient.getText("https://example.com/api/example")
}
```

全局请求头使用 `NetworkInterceptor`，超时和重试使用 `NetworkConfig`；不要为每个
接口重复 `try/catch` 或重新创建客户端。详细示例、错误类型和缓存策略见
[`docs/networking.md`](networking.md)。

Query 参数通过 `queryParameters` 传入，JSON、表单、文本和二进制请求体统一使用
`NetworkBody`；不要把未经编码的用户输入直接拼进 URL。

首页秒开或弱网回退可以使用 `getCacheFirst()` / `getNetworkFirst()`；它们遵循标准
HTTP 缓存协议。缓存未命中统一抛出 `NetworkCacheMissException`，响应来源可从
`NetworkResponse.source` 读取。完整示例见
[`docs/networking.md`](networking.md)。

需要覆盖默认网络超时时，直接给 `get()`、`getText()`、`getBytes()` 或 `send()`
传入 `timeoutMillis`。它表示一次请求尝试的完整时间预算，Android 与 iOS 语义一致。

## 9. Android 与 iOS 的入口

### 9.1 Android

```text
MainActivity
  → AppRoot
  → AppNavHost
  → HomeRoute / ShortsRoute / VideoDetailRoute
  → 共享 Screen
```

Android `Route` 负责：

- 通过 Navigation 3 entry 获得正确的 ViewModel 生命周期；
- 使用 `collectAsStateWithLifecycle()` 收集状态；
- 注入 Coil、Media3、屏幕方向和系统栏；
- 把页面点击转换成导航。

Android ViewModel 只是共享 StateHolder 的生命周期包装，不应复制 reducer 或分页逻辑。

### 9.2 iOS

```text
iosAppApp.swift
  → ContentView.swift
  → IosAppController.makeViewController()
  → 共享 Screen
```

[`ContentView.swift`](../iosApp/iosApp/ContentView.swift) 应一直保持很薄。页面和业务修改
优先发生在 Kotlin 共享代码；只有调用 UIKit、Foundation 或 AVPlayer 时才修改
`shared_ios/src/iosMain`。

## 10. 新增页面的最小流程

先确认是否真的需要新模块。页面属于现有 Feature 时，直接放进现有模块；只有独立
业务边界和依赖关系明确时才新建 Feature 模块。

新增一个需要状态的共享页面，推荐顺序：

1. 定义不可变 `PageModel`；
2. 定义 Presenter，把领域数据转成 `PageModel`；
3. 定义共享 StateHolder，暴露只读 `StateFlow`；
4. 在 `commonMain` 编写纯 `Screen`；
5. 在 `androidMain` 添加薄 ViewModel/Route；
6. 在 `IosAppController` 接入同一个 StateHolder/Screen；
7. 在 `core_navigation` 定义共享路由语义；
8. 在 Android `AppNavigation` 和 iOS 组合根映射路由；
9. 为 Presenter、状态变化或导航规则留下一个最小 `commonTest`。

如果它是新的底部一级页面，还要更新
[`AppTopLevelDestination.kt`](../core_ui/src/commonMain/kotlin/com/kotlinmvvm/core/ui/navigation/AppTopLevelDestination.kt)。
这里是一级入口文案、顺序与共享路由的唯一配置位置。

不要为了一个页面增加 BaseViewModel、BaseRepository、通用 Event 总线或 DI 框架。

## 11. 修改主题和通用组件

全局颜色和排版的入口：

- [`DesignTokens.kt`](../core_ui/src/commonMain/kotlin/com/kotlinmvvm/core/designsystem/theme/DesignTokens.kt)
- [`Color.kt`](../core_ui/src/commonMain/kotlin/com/kotlinmvvm/core/designsystem/theme/Color.kt)
- [`Type.kt`](../core_ui/src/commonMain/kotlin/com/kotlinmvvm/core/designsystem/theme/Type.kt)
- [`Theme.kt`](../core_ui/src/commonMain/kotlin/com/kotlinmvvm/core/designsystem/theme/Theme.kt)

只被一个页面使用的样式先留在该页面。至少有多个页面稳定复用时，才考虑提取到
`core_ui`。这样可以避免新人为了改一个圆角被迫跳转很多层。

## 12. 添加依赖

先问三个问题：

1. Kotlin/Compose 或平台 SDK 是否已经能完成？
2. 仓库现有依赖是否已经能完成？
3. 这个依赖是否真的要在所有目标编译？

依赖版本统一加到 `gradle/libs.versions.toml`。然后按使用范围添加：

```kotlin
sourceSets {
    commonMain.dependencies {
        // 只有真正跨平台的库
    }
    androidMain.dependencies {
        // Android 专用库
    }
    iosMain.dependencies {
        // iOS 专用库
    }
}
```

不要把 Coil、Media3、OkHttp、Foundation 或 Android Lifecycle 放入普通
`commonMain`。`core_network` 的 `commonMain` 只定义平台无关契约，具体引擎分别
位于 `androidMain` 和 `iosMain`。

## 13. 测试放在哪里

优先测试不依赖 UI 截图的稳定规则：

- Presenter 的字段转换；
- StateHolder 的加载、刷新、分页和失败恢复；
- Repository mapper；
- 导航状态变化；
- 播放状态机。

跨平台规则放在 `src/commonTest`，使用 `kotlin.test`。现有例子：

- [`PagedStateHolderTest.kt`](../core_state/src/commonTest/kotlin/com/kotlinmvvm/core/state/PagedStateHolderTest.kt)
- [`FeedPagerTest.kt`](../domain-feed/src/commonTest/kotlin/com/kotlinmvvm/domain/feed/paging/FeedPagerTest.kt)
- [`AppNavigationStateTest.kt`](../core_navigation/src/commonTest/kotlin/com/kotlinmvvm/core/navigation/AppNavigationStateTest.kt)

只改一行静态间距通常不需要增加测试；新增分页、映射或导航逻辑必须留下一个能防止
回归的测试。

## 14. 常见问题

### `commonMain` 中提示找不到 Android 类

代码放错层了。把平台代码移动到 `androidMain`，再通过参数、接口或 Composable
slot 注入共享代码。

### Android 正常，iOS UI 不一致

先确认两端是否真的调用同一个 `commonMain` Screen。当前首页、Shorts、Detail
都应该复用共享页面。然后检查：

- iOS 是否在 `IosAppController` 注入了对应 slot；
- 是否错误地在 SwiftUI 又写了一套页面；
- 是否是安全区、键盘、播放器或系统栏这种平台行为；
- 图片 URL 是否经过 `core_data` 的安全映射。

不要先复制一份 UI 到 SwiftUI 微调，这会再次产生两套页面。

### 修改 Kotlin 后 Xcode 没有更新

依次检查：

1. Xcode Build Log 中是否执行了 `embedAndSignAppleFrameworkForXcode`；
2. 执行 `Product > Clean Build Folder`；
3. 关闭 App 后重新 Run；
4. 用下面的命令单独检查共享 iOS 代码：

```bash
./gradlew :shared_ios:compileKotlinIosSimulatorArm64
```

### 图片空白但页面有数据

检查顺序：

1. `EyepetizerPayloadMapper` 是否把地址映射成非空 HTTPS URL；
2. 资源域名是否在明确允许列表内；
3. Android Coil 或 iOS `IosRemoteImage` 是否收到地址；
4. ATS/网络错误是否出现在平台日志。

不要用信任所有证书或允许所有明文流量来绕过问题。

### 不知道应该改哪个模块

从正在显示内容的 `*Screen.kt` 开始向上追调用者，再向下追它使用的 PageModel。
不要从 `settings.gradle.kts` 开始逐个读模块。

## 15. 每次修改后的最小验证

只改共享 UI：

```bash
./gradlew \
  :feature_home:compileKotlinDesktop \
  :feature_home:compileKotlinIosSimulatorArm64 \
  :app:assembleDebug
```

把 `feature_home` 换成实际修改的 Feature。

修改共享状态或数据：

```bash
./gradlew :相关模块:desktopTest
```

修改 iOS 组合根或平台适配：

```bash
./gradlew :shared_ios:compileKotlinIosSimulatorArm64
```

提交前运行完整门禁：

```bash
./.agents/skills/compose-scaffold-guardrails/scripts/verify_scaffold.sh
```

如果只想快速检查代码有没有跨层：

```bash
./.agents/skills/compose-scaffold-guardrails/scripts/check_architecture.sh
```

## 16. 新人开发检查清单

开始修改前：

- [ ] 我已经找到最终显示内容的 `Screen`。
- [ ] 我知道这个需求是共享能力还是平台能力。
- [ ] 我确认项目里没有现成组件或实现可以复用。

修改过程中：

- [ ] `Screen` 只接收不可变值、回调和 platform slot。
- [ ] 业务状态只实现一次，没有在 Android/iOS 各复制一份。
- [ ] `commonMain` 没有平台 SDK import。
- [ ] 没有为了一个调用者增加 Base 类或新依赖。
- [ ] 图片和非装饰内容有可访问性描述。

提交前：

- [ ] Android 实际运行过。
- [ ] 修改共享 UI 时，iOS 也实际运行过。
- [ ] 非平凡逻辑有最小回归测试。
- [ ] 架构检查和相关编译通过。
- [ ] 临时日志、截图、APK 和调试代码已经清理。

## 17. 记住这张速查图

```text
只改布局？
  → feature_*/src/commonMain/*Screen.kt

改加载、刷新、分页、播放状态？
  → feature_home / feature_media / core_state

改接口字段？
  → core_data Payload + Mapper
  → core_model
  → Presenter
  → Screen

需要 Android SDK？
  → app / androidMain

需要 iOS SDK？
  → shared_ios/iosMain

需要两端一致？
  → 状态和 UI 只在 commonMain 实现一次
```

理解以上路径后，再阅读完整的
[`architecture.md`](architecture.md)。架构文档解释“为什么这样分层”，本教程解决
“第一次应该怎么改”。
