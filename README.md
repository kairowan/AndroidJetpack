# KotlinMvvm：KMP + Compose Multiplatform 脚手架

这是一个渐进式跨端示例：保留已可运行的 Android 与 iOS 应用，同时把可复用状态、导航、设计系统、网络契约和页面 UI 下沉到 KMP/CMP。新增页面优先写一次 `commonMain`，只有网络引擎、图片、播放器、方向控制和系统栏等平台能力留在各端。

> 第一次接触 KMP/CMP？直接从 [新人上手教程](docs/getting-started.md) 开始。教程包含
> Android/iOS 运行步骤、目录速查、真实页面修改流程、平台能力接入和常见问题。

## 5 分钟上手

环境基线：JDK 17、Android SDK 36、Kotlin 2.3.20、CMP 1.11.1、AGP 8.11.1、Gradle 8.13。

```bash
# Android 主应用
./gradlew :app:assembleDebug

# 共享 UI 的 Desktop、Web 与 iOS 编译
./gradlew \
  :core_ui:desktopTest \
  :core_ui:compileKotlinWasmJs \
  :core_ui:compileKotlinIosSimulatorArm64 \
  :feature_home:desktopTest \
  :feature_home:compileKotlinWasmJs \
  :feature_home:compileKotlinIosSimulatorArm64 \
  :feature_media:desktopTest \
  :feature_media:compileKotlinWasmJs \
  :feature_media:compileKotlinIosSimulatorArm64
```

iOS 使用薄 SwiftUI 入口承载共享 CMP 页面：

```bash
open iosApp/iosApp.xcodeproj
```

Xcode 会调用 `:shared_ios:embedAndSignAppleFrameworkForXcode` 构建共享 framework。

如果 Xcode 只显示项目级 `invalid reuse after initialization failure`，且没有指向 Swift
源码，先执行 `Product > Clean Build Folder`（`⇧⌘K`），再重新 Run。这是 Xcode
构建状态初始化失败，不要为此修改 iOS 入口或共享业务代码。

提交前可直接使用仓库门禁，不需要记忆旧模版参数：

```bash
# 秒级架构与平台边界检查
./.agents/skills/compose-scaffold-guardrails/scripts/check_architecture.sh

# 共享测试、跨端编译、Android Lint 与 Debug/Release 打包
./.agents/skills/compose-scaffold-guardrails/scripts/verify_scaffold.sh
```

## 一条清晰的开发链路

```text
平台入口
  Android Activity / iOS SwiftUI wrapper / Desktop main / Web main
        ↓
core_ui                通用组件、顶层导航与跨端 UI 壳层
        ↓
feature-*/commonMain    纯 Screen + 不可变 PageModel + callbacks
        ↓
domain-feed             Feed 领域角色 + 不透明分页会话
        ↓
core_state/model/...    通用状态、模型、导航、设计 token
        ↓
平台适配
  Android: ViewModel + Coil + Media3 + core_network/OkHttp
  iOS: shared_ios CMP host + core_network/Foundation + AVPlayer
```

新增一个普通页面时：

1. 在共享 Feature 中定义不可变页面模型和事件。
2. 在 `commonMain` 写纯 Compose `Screen`，只接收值与回调。
3. 在平台 `Route` 收集生命周期状态并注入图片、播放器等能力。
4. 在 `core_ui` 的顶层导航配置中注册入口。

不要在 `Screen` 创建 Repository、查找生命周期 owner 或直接访问平台 API。

## 模块职责

| 模块 | 职责 | 目标端 |
| --- | --- | --- |
| `app` | Android 组合根、Navigation 3、平台入口 | Android |
| `feature_home` | 首页 Screen、状态、Presenter 与 Android 适配 | Android、iOS、Desktop、Wasm |
| `feature_media` | Shorts/Detail Screen、状态与 Android 播放适配 | Android、iOS、Desktop、Wasm |
| `core_ui` | CMP 主题、设计 token、通用组件、UI 行为与顶层导航 | Android、iOS、Desktop、Wasm |
| `core_state` / `core_model` / `core_navigation` | 纯 KMP 状态、模型与路由规则 | Android、iOS、Desktop、Wasm |
| `domain-feed` | Feature 可依赖的仓库角色与分页会话；不包含 URL/平台类型 | Android、iOS、Desktop、Wasm |
| `core_network` | 统一 Query、请求体、响应、错误、重试、拦截器、缓存回退和 Android/iOS 网络引擎 | Android、iOS |
| `core_data` | 开眼 DTO、KMP JSON 解析、数据映射与传输 URL 安全策略 | Android、iOS |
| `core_playback` | 播放契约、预设、状态与预加载规划 | Android、iOS、Desktop、Wasm |
| `core_player` | 进程级播放器工厂、缓存和 Media3 Compose surface | Android |
| `shared_ios` | iOS CMP 组合根、Foundation 图片与 AVPlayer 插槽、Swift framework | iOS |

Android 与 iOS 都直接运行 Home/Shorts/Detail 的共享 CMP Screen；SwiftUI 只保留一个 `UIViewControllerRepresentable` 平台入口。首页相关代码集中在 `feature_home`，Shorts/Detail 集中在 `feature_media`，新人不再需要跨 `*_shared` 模块寻找状态。当前共 13 个 Gradle 模块，`core_network` 因同时服务 `core_data` 和 iOS 图片加载而保留独立边界。Desktop/Wasm 已通过共享 UI 编译门禁，但仓库里尚未放独立可执行宿主和网络驱动。需要新增这些应用时，只增加平台入口与数据驱动，不复制 `commonMain` 页面。

## 关键约束

- `Application` 只持有 `AppDependencies`，默认实现由 app 内的 `AppContainer` 装配。
- Android/iOS 组合根各创建一个 `NetworkClient`；业务模块不得直接创建 OkHttp 或 `NSURLSession`。
- `Route` 负责 ViewModel 作用域、生命周期收集和平台能力注入。
- `Screen` 是纯 UDF：`state in, events out`。
- Feature 只依赖 `FeedPageRepository`；`core_data` 只在应用组合根创建。
- 分页互斥状态位于 `core_state`，continuation 会话位于 `domain-feed`，后端 URL 只存在于数据实现。
- 顶层目的地只在 `AppTopLevelDestination` 配置一次。
- Media3、Coil、Activity、系统方向等 Android 类型不得进入 `commonMain`；播放器 surface 使用官方 Compose UI。
- 新依赖和新模块只有在平台/标准库/现有实现无法覆盖时才引入。

第一次修改项目请看 [新人上手教程](docs/getting-started.md)；新增接口前先看
[统一网络教程](docs/networking.md)；更完整的边界、迁移顺序与平台接入方式见
[架构说明](docs/architecture.md)。
