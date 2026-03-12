# KotlinMvvm（Compose + KMP 多模块版）

一个基于 **Jetpack Compose + Kotlin Multiplatform + 模块化 + 协程 + Retrofit/OkHttp + UDF** 的开眼（Eyepetizer）客户端示例工程。

> 说明：项目名历史上叫 `MVVM`，但当前 Compose 主链路已经按 **UDF（State + ViewModel 方法）** 组织。

## 项目现状（与代码一致）

- UI：Jetpack Compose + Material3
- 共享层：Kotlin Multiplatform（当前已覆盖 `core_model`、`core_data`、`core_navigation`、`core_playback`、`core_state`、`core_legacy_network`、`core_design_tokens`、`core_ui_contract`）
- 架构：UDF（`StateFlow<State>` + ViewModel methods + `reduce`）
- 导航：`Navigation3`（`androidx.navigation3.runtime/ui`）
- 图片加载：`Coil`
- 播放器：`Media3 ExoPlayer`（含缓存、预加载、可插拔控制层）
- 数据源：**开眼（Eyepetizer）**
- 当前 Compose 模块：**没有 event/ble 模块**

## 模块结构

```text
app                 // Android 应用壳层、Navigation3 路由、主入口
iosApp              // iOS SwiftUI 宿主工程
Lib_Network         // 旧 Android 网络模块（当前已退出 Compose/KMP 主链路）
core_legacy_network // KMP 共享 legacy 网络模型与错误抽象
shared_ios          // iOS 宿主消费的 KMP 桥接 framework
core_model          // KMP 共享领域模型（commonMain）
core_data           // KMP 共享仓库契约 + Android 数据实现
core_navigation     // KMP 共享路由协议与 app 壳层状态
core_playback       // KMP 共享播放器协议、状态与 Feature 契约
core_design_tokens  // KMP 共享设计 token（颜色/排版）
core_ui_contract    // KMP 共享 UI 默认文案与分页策略
core_ui             // Android Compose 组件层（消费 core_ui_contract）
core_designsystem   // Android Material3 主题层（消费 core_design_tokens）
core_state          // KMP 共享通用状态模型（UiState / PagedData）
core_player         // Compose 播放器能力（Media3）
feature_home_shared // KMP 共享首页页面快照与 source 目录
feature_media_shared// KMP 共享详情页 / Shorts 页面快照
feature_home        // 首页列表
feature_detail      // 详情页 + 品牌化控制层
feature_shorts      // 短视频流
```

## 当前 KMP 进度

- `core_model` 已迁移到 KMP：
  - `commonMain` 提供 `EyepetizerFeed`、`EyepetizerFeedItem`、`EyepetizerFeedSource`
  - `commonMain` 提供视频展示文案格式化，如作者标签、分类标签、时长文本
  - 已验证 Android 与 iOS Simulator 编译
- `core_data` 已迁移到 KMP：
  - `commonMain` 提供 `EyepetizerRepository` 仓库契约
  - `commonMain` 提供共享 `AppContainer`，统一装配仓库依赖
  - `commonMain` 提供请求 URL 构造器、共享 baseUrl 常量与共享异常模型
  - `commonMain` 提供 `PagedState`、`EyepetizerFeedPager`、`PagedFeedController`
  - `commonMain` 提供 Eyepetizer 最小响应 DTO 与共享映射逻辑
  - `androidMain` 提供 Eyepetizer 专用 Retrofit/OkHttp 运行时、服务与驱动实现，不再依赖 `Lib_Network`
  - `iosMain` 当前提供 Foundation 驱动的基础实现，已带 URL 标准化、超时和 HTTP 状态码处理，可继续替换为 Ktor 或其它多平台网络实现
- `core_playback` 已迁移到 KMP：
  - `commonMain` 提供 `PlaybackController`、`PlayState`、`PlayerState`
  - `commonMain` 提供 `PlayerDefaults`、`PlayerFeature`、`ResumePlaybackFeature`、`ShortsItem`
  - `commonMain` 提供 `GestureConfig`、`PlayerControlsConfig`、`PlayerControlActions`
  - `commonMain` 提供控制层状态机、视频缓存默认值、详情页播放器共享预设与 Shorts 预加载规划器
  - 已验证 Android 与 iOS Simulator 编译和测试
- `core_navigation` 已迁移到 KMP：
  - `commonMain` 提供 `AppRoute` 与 app 壳层共享状态 `AppShellState`
  - `commonMain` 提供 `AppNavigationState`，统一封装顶层切换、详情入栈与返回出栈规则
  - app 模块当前仅保留 Navigation3 Key 适配、Compose Saver 适配与 route back stack 同步
  - 已验证 Android 与 iOS Simulator 编译和测试
- `core_state` 已迁移到 KMP：
  - `commonMain` 提供 `UiState`、`PagedData` 与通用状态扩展函数
  - `core_ui` 当前只负责 Compose 组件与共享状态的渲染
- `core_design_tokens` 已迁移到 KMP：
  - `commonMain` 提供颜色 token、排版 token 与跨平台主题规格
  - `core_designsystem` 当前继续保留 Android Material3 包装，直接消费这些共享 token
- `core_ui_contract` 已迁移到 KMP：
  - `commonMain` 提供空态/错误态默认文案与分页预取策略
  - `core_ui` 当前继续保留 Android `BaseViewModel`、`ViewModelFactory` 与 Compose 通用组件，并直接消费这些共享 contract
- `core_legacy_network` 已迁移到 KMP：
  - `commonMain` 提供 legacy `BaseResult`、WanAndroid 相关响应模型、`ResponseThrowable`、基础网络契约、地址/日志常量以及下载异常/网络事件模型
  - `Lib_Network` 当前改为依赖该 shared 模块，不再内嵌这些纯 Kotlin 模型
- `shared_ios` 已接入 iOS 宿主：
  - 输出 `SharedIosApp` framework，供 Xcode 里的 `iosApp` 直接引用
  - `commonMain` 提供 iOS 首页桥接层 `IosHomeFeedBridge`，把共享仓库结果转成 SwiftUI 可消费的状态快照
  - `commonMain` 提供 iOS Shorts / Detail 桥接层 `IosShortsBridge`、`IosVideoDetailBridge`
  - `commonMain` 提供 `IosDesignBridge`，把共享设计 token 和共享 UI 默认文案桥接给 SwiftUI
- `feature_home_shared` 已迁移到 KMP：
  - `commonMain` 提供首页频道目录、首页列表项模型与共享页面 presenter
  - Android `feature_home` 与 iOS `IosHomeFeedBridge` 已统一消费同一套首页页面快照
- `feature_media_shared` 已迁移到 KMP：
  - `commonMain` 提供详情页页面快照与共享控制文案
  - `commonMain` 提供 Shorts 页面快照、共享卡片模型与全屏控制文案
  - Android `feature_detail` / `feature_shorts` 与 iOS `shared_ios` 已统一消费这些 presenter
- `iosApp` 当前已可构建：
  - 使用 SwiftUI 作为最小 iOS 宿主
  - 当前示例页接入 Home / Shorts / Detail 三条页面链路
  - 首页和 Shorts 已消费共享页面 presenter，详情页已消费共享 Detail presenter
  - 当前宿主已通过 `AVPlayer / VideoPlayer` 提供 iOS 原生播放能力，可直接预览详情页与 Shorts 播放效果
  - 当前示例页已复用共享设计 token 和共享 UI 默认文案
- `feature_home` / `feature_shorts` 当前已复用共享分页逻辑与共享 state holder，`feature_shorts` 的全屏模式与页索引也已进入共享 playback state holder，`ShortsPager` 已支持直接消费共享视频模型，Android 侧 ViewModel 仅保留生命周期包装，Compose UI 仍在 Android 侧
- `feature_detail` 当前已复用共享详情状态持有器，Android 侧 ViewModel 仅负责生命周期包装，Compose UI 与播放器仍在 Android 侧
  - 详情页品牌化控制层当前已复用共享 `PlayerControlsConfig` 作为行为配置，仅保留样式定制在 Android 侧
- `app` 模块当前负责 Android 壳层导航、Navigation3 Key 适配与共享容器接线，网络初始化也已通过 `core_data` 的 Android 运行时入口收口
- `core_player` 当前退回 Android Media3 实现层，通过 `api` 暴露共享 playback 协议，UI 样式与图标继续留在 Android 侧
- 当前 UI 与播放器渲染层仍以 Android Compose 为主，尚未引入 Compose Multiplatform UI
- `Lib_Network` 当前仅保留旧网络体系和遗留 WanAndroid 相关代码，不再参与 Compose/KMP 主链路

## iOS 运行

当前仓库已经带了最小 iOS 宿主工程：

1. 用 Xcode 打开 `iosApp/iosApp.xcodeproj`
2. 选择 `iosApp` scheme
3. 运行 iPhone Simulator

Xcode 构建时会自动执行：

```bash
./gradlew :shared_ios:embedAndSignAppleFrameworkForXcode
```

最近一次命令行验证通过的构建命令：

```bash
xcodebuild -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'generic/platform=iOS Simulator' \
  -derivedDataPath /tmp/iosAppDerived \
  build
```

## 架构说明

当前 Compose 页面统一采用：

1. `Screen` 只负责渲染和调用 `ViewModel` 方法  
2. `ViewModel` 负责执行业务逻辑并 `reduce` 新 `State`  
3. `Repository` 负责数据获取和映射  
4. `ApiService` 由 `RetrofitClient` 提供  

关键点：

- `core_ui_contract` 提供可复用的分页触发策略与默认 UI 文案，`core_ui` 负责把它们渲染成 Compose 组件；
- `core_data` 的 `EyepetizerRepository` 当前直接使用自身 Android 运行时与 Eyepetizer 专用 Retrofit 服务；
- `core_playback` 提供播放器共享状态、共享默认值和 feature 扩展协议；
- `app` 使用 `Navigation3` 完成 Home/Shorts/Detail 路由切换，并通过 `core_navigation` 共享导航状态统一协调路由栈、底栏和 Shorts 全屏离场；
- Compose 页面内 `viewModel(factory = viewModelFactory { ... })` 与页面生命周期绑定。

## 播放器能力（core_player）

`core_player` 已具备以下能力：

1. 预缓存/预加载  
- 基于 `SimpleCache + CacheDataSource`
- `ShortsPager` 支持按页预加载后续视频（`preloadCount`）

2. 全屏模式  
- 支持竖屏全屏、横屏全屏、退出全屏
- 全屏切换联动系统栏显示/隐藏

3. 旋转续播  
- `feature_detail` 通过 `ResumePlaybackFeature` 保存并恢复播放进度、播放状态、倍速

4. 控制层可插拔  
- 可替换视频渲染层：`surfaceContent`
- 可替换控制层 UI：`controlsContent`
- 可叠加业务层：`overlayContent`
- 可挂载功能插件：`features: List<PlayerFeature>`
- 默认控制层可通过 `controlConfig/controlStyle/controlIcons/controlActions` 配置

## 播放器使用说明（Compose + XML）

### 1) Compose 页面使用

常用导入：

```kotlin
import com.kotlinmvvm.core.player.provider.rememberPlayer
import com.kotlinmvvm.core.player.ui.VideoPlayerView
import com.kotlinmvvm.core.player.ui.FullscreenVideoPlayer
```

最小示例：

```kotlin
@Composable
fun DemoVideo(url: String) {
    val player = rememberPlayer()
    VideoPlayerView(
        url = url,
        player = player
    )
}
```

短视频流示例可使用：

```kotlin
import com.kotlinmvvm.core.player.ui.ShortsPager
import com.kotlinmvvm.core.player.ui.ShortsOverlay
import com.kotlinmvvm.core.player.model.ShortsItem
```

### 2) XML + 代码方式（直接绑定 PlayerView）

布局中使用 `androidx.media3.ui.PlayerView`：

```xml
<androidx.media3.ui.PlayerView
    android:id="@+id/playerView"
    android:layout_width="match_parent"
    android:layout_height="220dp" />
```

Fragment/Activity 中：

```kotlin
import com.kotlinmvvm.core.player.facade.PlayerFactory
import com.kotlinmvvm.core.player.facade.PlayerLifecycleBinder

val player = PlayerFactory.create(requireContext())
PlayerLifecycleBinder.bind(viewLifecycleOwner.lifecycle, player)
binding.playerView.player = player.exoPlayer
player.play(url)
```

### 3) XML 中间层方式（推荐）

布局中直接放中间层 View：

```xml
<com.kotlinmvvm.core.player.xml.VideoPlayerHostView
    android:id="@+id/videoHost"
    android:layout_width="match_parent"
    android:layout_height="220dp" />
```

代码中只做生命周期绑定和播放：

```kotlin
binding.videoHost.bindLifecycle(viewLifecycleOwner.lifecycle)
binding.videoHost.play(url)
```

可选 API：
- `createAndAttachPlayer()`
- `attachPlayer(player)`
- `setUseController(true/false)`
- `setResizeMode(...)`
- `detachPlayer(releasePlayer = true)`

## 快速运行

### 环境要求

- JDK 17
- Android Studio（建议最新稳定版）
- Android SDK：`compileSdk 36`，`minSdk 24`

### 启动步骤

1. 克隆项目并用 Android Studio 打开根目录  
2. 同步 Gradle  
3. 运行 `app` 模块  

命令行构建示例：

```bash
./gradlew :app:assembleDebug
```

## 数据接口说明

- 本项目 Compose 主链路使用 **开眼（Eyepetizer）** 接口

## 主要依赖

- [Kotlin](https://kotlinlang.org/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Navigation3](https://developer.android.com/jetpack/androidx/releases/navigation3)
- [Media3](https://developer.android.com/media/media3)
- [Retrofit](https://github.com/square/retrofit)
- [OkHttp](https://github.com/square/okhttp)
- [Coil](https://github.com/coil-kt/coil)

## 致谢

- 开眼（Eyepetizer）开放数据
- 项目内网络与基础库贡献者
