# KMP / CMP 架构说明

## 目标

本仓库采用渐进式迁移，不要求为了“看起来跨端”重写已经稳定的平台实现。共享层负责稳定、可测试的业务事实和纯 UI；平台层负责生命周期、网络引擎、图片和视频等必须校准的能力。

## 依赖方向

依赖只能从平台入口指向共享抽象：

```text
app / shared_ios / future desktopApp / future webApp
  ├─ core_ui
  ├─ feature_home / feature_media
  │    ├─ common Screen + state + presenter
  │    └─ domain-feed + core-model + core-state
  ├─ core_data → core_network
  └─ core-player（平台实现）
```

共享模块不能反向依赖应用入口。`commonMain` 不能出现 `android.*`、Media3、Coil 或 Android Lifecycle 类型。

## UI 边界

一个有状态页面拆成三层：

```text
Route(repository, navigation callbacks)
  → ViewModel / shared state holder
  → Screen(pageModel, callbacks, platform slots)
```

- `Route`：平台生命周期与依赖接线。
- `ViewModel`：Android 生命周期包装；业务状态尽量来自 KMP state holder。
- `Screen`：CMP 纯函数，不创建依赖，不持有 owner。
- platform slot：用 Composable lambda 注入网络图片、原生地图或播放器，避免在共享 UI 引入平台 SDK。

首页、Shorts 和 Detail 是当前参考实现：

- `feature_home/src/commonMain/.../HomeScreen.kt`：四端共享 UI。
- `feature_home/src/androidMain/.../HomeRoute.kt`：Android 生命周期和 Coil。
- `feature_home/src/commonMain`：同时保存首页 PageModel、presenter 和 state holder。
- `feature_media/src/commonMain/.../ShortsScreen.kt`：四端共享的竖向分页与控制层。
- `feature_media/src/androidMain/.../ShortsRoute.kt`：Media3、Coil、方向与系统栏适配。
- `feature_media/src/commonMain/.../VideoDetailScreen.kt`：四端共享的详情信息布局。
- `feature_media/src/androidMain/.../VideoDetailRoute.kt`：Media3、Coil、播放恢复与全屏适配。

模块按真正的业务/平台边界划分，不按 `Model`、`Presenter`、`Contract` 再拆模块。
设计 token、主题、通用 UI 行为和顶层导航统一归 `core_ui`，避免一次小改动跨越多个
只包含少量文件的模块。

## 应用组合根

`App` 暴露 `AppDependencies` 而不是具体容器。默认 `AppContainer` 只在 app 内可见并进程级复用 `NetworkClient`、`FeedPageRepository` 与 `VideoPlayerFactory`。测试或后续接入 DI 框架时，只替换接口实现，Activity 和 Feature 无需改变。iOS CMP 组合根同样只创建一个 `NetworkClient`，供 Repository 和图片平台插槽复用。

## 导航

`core_navigation` 保存与 UI 无关的路由和壳层状态；`core_ui` 的
`AppTopLevelDestination` 是顶层目的地、文案和顺序的唯一配置源；Android
`AppNavHost` 只负责把共享路由映射到 Navigation 3 entry。

Navigation 3 entry 使用 saveable-state 与 ViewModel-store decorator。返回到根目的地时交给平台入口处理，Android 当前调用 `Activity.finish()`。

## 分页与刷新

`PagedState`、`PagedPage` 与 `PagedStateHolder` 位于 `core_state`，只负责加载、互斥、失败恢复和完整页面快照。

`domain-feed` 定义窄接口 `FeedPageRepository` 和 `FeedPager`。分页令牌在领域层只是 `continuationToken`，不承诺它是 URL；`core_data` 才负责把服务端 `nextPageUrl` 校验并映射成令牌。这样 Feature、Desktop 和 Web 代码都不绑定网络引擎或某个后端协议。

`core_network` 是唯一允许直接接触 HTTP 引擎的模块：

```text
平台组合根
  → 创建 NetworkClient
      Android: OkHttp
      iOS: Foundation URLSession
  → 注入 core_data Repository / iOS 图片插槽
```

它统一 Query 编码、请求体、请求方法、响应、错误、超时、可配置幂等重试、服务端
`Retry-After` 退避、拦截器、标准 HTTP 缓存策略、缓存回退和协程取消。缓存未命中
在 Android/iOS 使用同一异常；网络优先回退只处理传输错误、超时、408、429 与 5xx，
不用旧缓存掩盖 4xx。没有生产调用方的请求 ID、分组任务表、轮询和竞速封装不进入
基础客户端。接口 DTO、JSON 字段和业务 URL 也不进入 `core_network`；它们继续属于
`core_data`。完整用法见 [统一网络教程](networking.md)。

一次网络尝试的总超时由 `commonMain` 持有，平台引擎负责在协程取消时终止底层
OkHttp Call 或 URLSessionTask。外层生命周期取消和调用方超时必须原样传播，不能
转成可重试的网络失败。

## 平台策略

| 能力 | Android | iOS | Desktop / Web |
| --- | --- | --- | --- |
| 共享 UI | Home/Shorts/Detail CMP | Home/Shorts/Detail CMP；SwiftUI 只包装控制器 | Home/Shorts/Detail CMP library 已编译 |
| 数据 | `core_network` + OkHttp | `core_network` + Foundation | 待具体宿主引入时增加驱动 |
| 图片 | Coil | `UIImageView` 平台插槽 | 宿主适配 |
| 视频 | Media3 Compose `ContentFrame` | `AVPlayerViewController` 平台插槽 | 宿主适配 |
| 生命周期 | Android ViewModel | Compose scope + 共享 state holder | 宿主 scope |

不要在没有产品需求时提前添加 Desktop/Web 网络库。共享 UI 编译门禁确保页面没有平台泄漏；真正新增宿主时再实现最小平台驱动。

## 新增页面

1. 先抽出不可变 PageModel 和 presenter。
2. 将纯 Compose UI 移到 `commonMain`。
3. 用 Composable lambda 隔离平台控件。
4. 保留平台 Route/ViewModel，只做接线。
5. 为共享 presenter 或状态机保留一个最小回归测试。
6. 同时跑 Android assemble 与目标端 compile task。

现有三类页面都已经保留可复用参考实现；新增页面沿用同一顺序，不要把 Media3 API“伪共享”到 KMP。
