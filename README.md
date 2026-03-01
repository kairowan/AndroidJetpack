基于 **模块化+Kotlin+协程+Retrofit+Jetpack+单向数据流（MVI 风格）** 架构实现的开眼（Eyepetizer）客户端，适合学习从 0 到 1 搭建符合大型 Android 项目实践的工程结构。

|                             项目截图                             |                             项目截图                             |                             项目截图                             |
| :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
| ![](https://github.com/Gao-hao-nan/MVVM/blob/master/image/image_gif1.gif) | ![](https://github.com/Gao-hao-nan/MVVM/blob/master/image/image_gif2.gif) | ![](https://github.com/Gao-hao-nan/MVVM/blob/master/image/image_gif3.gif)
### 1. 项目架构
1. 项目采用 Kotlin 语言编写，结合 Jetpack 相关控件，`Navigation`，`Lifecycle`，`ViewModel`，`StateFlow` 等搭建单向数据流（MVI 风格）架构；历史模块中仍存在部分 MVVM 写法；
2. 通过**组件化**，**模块化**拆分，实现项目更好解耦和复用
3. 使用 **协程+Retrofit+OkHttp** 优雅地实现网络请求；
4. 通过 **mmkv**，**Room** 数据库等实现对数据缓存的管理；
5. 使用 **Glide** 完成图片加载；
6. 通过RxAppCompatActivity+RxLifecycleAndroid 封装的基类

### Compose 播放器（Media3）
当前项目已提供基于 `core_player` 的 Compose 播放器能力，支持短视频场景和详情页场景：

1. 播放能力
- 基于 `Media3 ExoPlayer` 实现播放、手势、进度同步、倍速、音量控制。
- `rememberPlayer()` 提供生命周期感知的播放器实例。

2. 预缓存能力
- 使用 `SimpleCache + CacheDataSource` 统一缓存视频数据。
- `ShortsPager` 默认预加载后续视频（可配置 `preloadCount`）。

3. 全屏能力
- 支持 `竖屏全屏` / `横屏全屏` / `退出全屏`，并联动系统栏显示隐藏。
- 详情页和短视频页均已接入 icon 化全屏控制。

4. 可插拔能力（重点）
- 默认控制层可通过以下方式替换或扩展：
  - `controlConfig` / `controlStyle` / `controlIcons` / `controlActions`
  - `surfaceContent`（替换视频渲染层）
  - `controlsContent`（替换整个控制层 UI）
  - `overlayContent`（叠加业务层）
  - `features: List<PlayerFeature>`（挂载非 UI 功能扩展）

5. 自定义控制层示例
- `feature_detail/BrandedPlayerControls.kt` 提供了完整自定义控制层示例。
- 详情页通过 `controlsContent` 已替换为品牌化控制层，并示例了 `ResumePlaybackFeature`（旋转续播）。

示例：
```kotlin
VideoPlayerView(
    url = video.playUrl,
    player = player,
    controlsContent = { p, state ->
        BrandedPlayerControls(
            player = p,
            state = state,
            title = video.title,
            onBack = onBack,
            isFullscreen = isFullscreen,
            isLandscapeFullscreen = isLandscape
        )
    },
    features = listOf(resumeFeature)
)
```

欢迎在 **Issue** 中提交对本仓库的改进建议~
有问题请联系QQ:1931672489
感谢您的阅读~

### 致谢

**API：** 开眼（Eyepetizer）接口（项目内整理）[**eyepetizer_api.md**](./eyepetizer_api.md)

**主要使用的开源框架:**

*   [**Retrofit**](https://github.com/square/retrofit)
*   [**OkHttp**](https://github.com/square/okhttp)
*   [**Glide**](https://github.com/bumptech/glide)
*   [**ARouter**](https://github.com/alibaba/ARouter)
*   [**MMKV**](https://github.com/Tencent/MMKV)
*   [**SmartRefreshLayout**](https://github.com/scwang90/SmartRefreshLayout)
