# KotlinMvvm（Compose 多模块版）

一个基于 **Jetpack Compose + 模块化 + 协程 + Retrofit/OkHttp + StateFlow** 的开眼（Eyepetizer）客户端示例工程。

> 说明：项目名历史上叫 `MVVM`，但当前 Compose 主链路已经按 **单向数据流（MVI 风格）** 组织（`UiState + ViewModel + Repository`）。

## 项目现状（与代码一致）

- UI：Jetpack Compose + Material3
- 架构：MVI 风格 UDF（`UiState` + `StateFlow`）
- 导航：`Navigation3`（`androidx.navigation3.runtime/ui`）
- 图片加载：`Coil`
- 播放器：`Media3 ExoPlayer`（含缓存、预加载、可插拔控制层）
- 数据源：**开眼（Eyepetizer）**
- 当前 Compose 模块：**没有 event/ble 模块**

## 模块结构

```text
app                 // 应用壳层、Navigation3 路由、主入口
Lib_Network         // 网络基础能力（Retrofit/OkHttp/ApiService）
core_model          // 领域模型（EyepetizerFeed / EyepetizerFeedItem）
core_data           // 数据仓库层（EyepetizerRepository、BaseApiRepository）
core_ui             // UI 基类与通用组件（BaseViewModel、PagedList、UiStateContainer）
core_designsystem   // 主题与设计系统
core_player         // Compose 播放器能力（Media3）
feature_home        // 首页列表
feature_detail      // 详情页 + 品牌化控制层
feature_shorts      // 短视频流
```

## 架构说明

当前 Compose 页面统一采用：

1. `Screen` 只负责渲染和交互分发  
2. `ViewModel`（`BaseViewModel` / `BasePagedViewModel`）管理 `UiState`  
3. `Repository` 负责数据获取和映射  
4. `ApiService` 由 `RetrofitClient` 提供  

关键点：

- `core_ui` 提供可复用的分页状态管理与列表容器；
- `core_data` 的 `EyepetizerRepository` 当前走 hostType `7`（开眼域名）；
- `app` 使用 `Navigation3` 完成 Home/Shorts/Detail 路由切换；
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
- 接口整理文档见：[eyepetizer_api.md](./eyepetizer_api.md)
- 不是 WanAndroid 接口

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
