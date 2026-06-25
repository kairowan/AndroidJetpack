基于 **模块化+Kotlin+协程+Retrofit+Jetpack+MVVM** 架构实现的 WanAndroid 客户端。 能提供大家学习如何从0到1打造一个符合[大型Android项目的架构模式]

|                             项目截图                             |                             项目截图                             |                             项目截图                             |
| :----------------------------------------------------------: | :----------------------------------------------------------: | :----------------------------------------------------------: |
| ![](https://github.com/Gao-hao-nan/MVVM/blob/master/image/image_gif1.gif) | ![](https://github.com/Gao-hao-nan/MVVM/blob/master/image/image_gif2.gif) | ![](https://github.com/Gao-hao-nan/MVVM/blob/master/image/image_gif3.gif)
### 1. 项目架构
1. 项目采用 Kotlin 语言编写，结合 Jetpack 相关控件，`Navigation`，`Lifecyle`，`DataBinding`，`LiveData`，`ViewModel`等搭建的 **MVVM** 架构模式；
2. 通过**组件化**，**模块化**拆分，实现项目更好解耦和复用
3. 使用 **协程+Retrofit+OkHttp** 优雅地实现网络请求；
4. 通过 **mmkv**，**Room** 数据库等实现对数据缓存的管理；
5. 使用 **Glide** 完成图片加载；
6. 通过RxAppCompatActivity+RxLifecycleAndroid 封装的基类

### 2. 新增了event模块（通过flow实现的轻量级）
| 功能点                  | 方法                                                 | 说明                            |
| -------------------- | -------------------------------------------------- | ----------------------------- |
| 🔹 发送事件              | `EventChannel.post(event)`                         | 发送任意类型的事件                     |
| 🔹 订阅事件（支持粘性）        | `EventChannel.observe<T>(sticky = true)`           | 可指定是否接收历史事件                   |
| 🔹 只接收历史事件           | `EventChannel.observeOnlySticky<T>()`              | 仅接收历史事件，不监听新事件（可选）            |
| 🔹 设置粘性事件最大缓存数       | `EventChannel.setMaxStickyCacheSize<T>(size)`      | 默认最多缓存 10 条                   |
| 🔹 获取粘性历史事件          | `EventChannel.getStickyEvents<T>()`                | 返回 `List<Pair<T, timestamp>>` |
| 🔹 清除某类粘性事件          | `EventChannel.clearStickyEvents<T>()`              | 清除指定类型的事件历史                   |
| 🔹 清除全部粘性事件          | `EventChannel.clearAllStickyEvents()`              | 全局清除所有缓存事件                    |
| 🔹 生命周期感知收集          | `flow.collectIn(owner)`                            | 在 LifecycleOwner 生命周期内自动取消    |
| 🔹 Fragment 自动生命周期订阅 | `Fragment.observeEvent<T>(sticky = false, block)`  | 自动跟随 Fragment 生命周期，只在活跃时收集事件  |
| 🔹 Activity 自动生命周期订阅 | `ComponentActivity.observeEvent<T>(sticky, block)` | 自动跟随 Activity 生命周期，只在活跃时收集事件  |

### 3. 新增了ble模块（通过nordicsemi进行的一个基础封装,使用起来更加解耦）
| 功能点 | 核心类/方法 | 说明 |
| :--- | :--- | :--- |
| 🔹 **核心控制层 (Driver)** | `CBleManager` | **(核心)** 基于 Nordic 库封装的底层驱动，负责具体的 GATT 交互、Notify 队列与线程切换 |
| 🔹 **全局单例管家 (Repo)** | `BleRepository` | 全局维护唯一的蓝牙实例，对外提供连接、断开等统一入口，**业务层主要和它打交道** |
| 🔹 **协议配置中心** | `DeviceProfile` | 集中管理所有的 **Service/Char UUID** 以及具体的 **指令集 (Commands)** |
| 🔹 **状态监听接口** | `BleStateObserver` | 定义连接状态回调 (`onConnecting`, `onDeviceReady`, `onDisconnected`)，支持多端监听 |
| 🔹 **数据监听接口** | `BleDataObserver` | 定义数据接收回调 (`onDataReceived`)，自动分发 Device、Byte 和 Hex String |
| 🔹 **业务模块基类** | `BaseBleModule` | 所有业务模块（如电量、OTA）的父类，封装了数据分发 (`onReceive`) 和指令发送 (`send`) 逻辑 |
| 🔹 **工具类** | `BleUtils` | 提供 Byte 数组与 Hex 字符串的高效相互转换工具，方便日志打印与调试 |

欢迎在 **Issue** 中提交对本仓库的改进建议~
有问题请联系QQ:1931672489
感谢您的阅读~

### 致谢

**API：**  鸿洋提供的 [**WanAndroid API**](https://www.wanandroid.com/blog/show/2)

**主要使用的开源框架:**

*   [**Retrofit**](https://github.com/square/retrofit)
*   [**OkHttp**](https://github.com/square/okhttp)
*   [**Glide**](https://github.com/bumptech/glide)
*   [**ARouter**](https://github.com/alibaba/ARouter)
*   [**MMKV**](https://github.com/Tencent/MMKV)
*   [**SmartRefreshLayout**](https://github.com/scwang90/SmartRefreshLayout)
