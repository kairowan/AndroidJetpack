# KotlinMvvm：KMP + Compose Multiplatform 脚手架

这是一个渐进式跨端示例：保留已可运行的 Android 与 iOS 应用，同时把可复用状态、导航、设计系统和页面 UI 下沉到 KMP/CMP。新增页面优先写一次 `commonMain`，只有图片、播放器、方向控制和系统栏等平台能力留在各端。

> 第一次接触 KMP/CMP？直接从 [新人上手教程](docs/getting-started.md) 开始。教程包含
> Android/iOS 运行步骤、目录速查、真实页面修改流程、平台能力接入和常见问题。

## 5 分钟上手

环境基线：JDK 17、Android SDK 36、Kotlin 2.3.20、CMP 1.11.1、AGP 8.11.1、Gradle 8.13。

```bash
# Android 主应用
./gradlew :app:assembleDebug

# 共享 UI 的 Desktop、Web 与 iOS 编译
./gradlew \
  :shared-ui:desktopTest \
  :shared-ui:compileKotlinWasmJs \
  :shared-ui:compileKotlinIosSimulatorArm64 \
  :feature_home:desktopTest \
  :feature_home:compileKotlinWasmJs \
  :feature_home:compileKotlinIosSimulatorArm64 \
  :feature_shorts:desktopTest \
  :feature_shorts:compileKotlinWasmJs \
  :feature_shorts:compileKotlinIosSimulatorArm64 \
  :feature_detail:desktopTest \
  :feature_detail:compileKotlinWasmJs \
  :feature_detail:compileKotlinIosSimulatorArm64
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
shared-ui               顶层导航配置与跨端 UI 壳层
        ↓
feature-*/commonMain    纯 Screen + 不可变 PageModel + callbacks
        ↓
feature-*_shared        跨端 presenter 与页面快照
        ↓
domain-feed             Feed 领域角色 + 不透明分页会话
        ↓
core_state/model/...    通用状态、模型、导航、设计 token
        ↓
平台适配
  Android: ViewModel + Coil + Media3 + Retrofit
  iOS: shared_ios CMP host + Foundation + AVPlayer
```

新增一个普通页面时：

1. 在共享 Feature 中定义不可变页面模型和事件。
2. 在 `commonMain` 写纯 Compose `Screen`，只接收值与回调。
3. 在平台 `Route` 收集生命周期状态并注入图片、播放器等能力。
4. 在 `shared-ui` 的顶层配置中注册入口。

不要在 `Screen` 创建 Repository、查找生命周期 owner 或直接访问平台 API。

## 模块职责

| 模块 | 职责 | 目标端 |
| --- | --- | --- |
| `app` | Android 组合根、Navigation 3、平台入口 | Android |
| `shared-ui` | 顶层目的地与导航栏 | Android、iOS、Desktop、Wasm |
| `feature_home` | 首页纯 CMP Screen；Android Route/ViewModel 适配 | Android、iOS、Desktop、Wasm |
| `feature_detail` | 详情纯 CMP Screen；Android Media3/Coil/全屏适配 | Android、iOS、Desktop、Wasm |
| `feature_home_shared` | 首页 presenter 与页面快照 | Android、iOS、Desktop、Wasm |
| `feature_shorts` | Shorts 纯 CMP Screen；Android Media3/Coil/窗口适配 | Android、iOS、Desktop、Wasm |
| `feature_media_shared` | Shorts/Detail 共享快照、Feed 与播放状态 | Android、iOS、Desktop、Wasm |
| `core_designsystem` / `core_ui` | CMP 主题与通用组件 | Android、iOS、Desktop、Wasm |
| `core_state` / `core_model` / `core_navigation` | 纯 KMP 状态、模型与路由规则 | Android、iOS、Desktop、Wasm |
| `domain-feed` | Feature 可依赖的仓库角色与分页会话；不包含 URL/平台类型 | Android、iOS、Desktop、Wasm |
| `core_data` | `domain-feed` 的 Android/iOS 网络实现与传输 URL 安全策略 | Android、iOS |
| `core_playback` | 播放契约、预设、状态与预加载规划 | Android、iOS、Desktop、Wasm |
| `core_player` | 进程级播放器工厂、缓存和 Media3 Compose surface | Android |
| `shared_ios` | iOS CMP 组合根、Foundation 图片与 AVPlayer 插槽、Swift framework | iOS |

Android 与 iOS 都直接运行 Home/Shorts/Detail 的共享 CMP Screen；SwiftUI 只保留一个 `UIViewControllerRepresentable` 平台入口。Desktop/Wasm 已通过共享 UI 编译门禁，但仓库里尚未放独立可执行宿主和网络驱动。需要新增这些应用时，只增加平台入口与数据驱动，不复制 `commonMain` 页面。

## 关键约束

- `Application` 只持有 `AppDependencies`，默认实现由 app 内的 `AppContainer` 装配。
- `Route` 负责 ViewModel 作用域、生命周期收集和平台能力注入。
- `Screen` 是纯 UDF：`state in, events out`。
- Feature 只依赖 `FeedPageRepository`；`core_data` 只在应用组合根创建。
- 分页互斥状态位于 `core_state`，continuation 会话位于 `domain-feed`，后端 URL 只存在于数据实现。
- 顶层目的地只在 `AppTopLevelDestination` 配置一次。
- Media3、Coil、Activity、系统方向等 Android 类型不得进入 `commonMain`；播放器 surface 使用官方 Compose UI。
- 新依赖和新模块只有在平台/标准库/现有实现无法覆盖时才引入。

第一次修改项目请看 [新人上手教程](docs/getting-started.md)；更完整的边界、迁移顺序与
平台接入方式见 [架构说明](docs/architecture.md)。
