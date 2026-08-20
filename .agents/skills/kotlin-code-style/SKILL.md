---
name: kotlin-code-style
description: Enforce this repository's Kotlin, Android, Compose Multiplatform, and Kotlin Multiplatform source conventions. Use whenever creating or modifying .kt files, especially classes, data classes, interfaces, objects, enums, sealed hierarchies, extension functions, inline functions, tests, or Chinese code comments.
---

# Kotlin 代码规范

在本项目新增或修改 Kotlin 代码时，保持类型、文件和职责边界清晰。先阅读目标包已有结构，再按以下规则实现；不要借普通需求批量整理无关旧代码。

## 类描述

每个新增的顶层 `class`、`data class`、`interface`、`sealed interface/class`、`object`、`enum class` 和 `annotation class`，包括测试类型，都必须在声明正上方添加：

```kotlin
/**
 * @author 浩楠
 * @date ${DATE}
 * 描述: 用一句完整中文说明该类型的职责
 */
```

- 将 `${DATE}` 替换为创建代码时的实际日期，固定使用 `yyyy/M/d` 格式，不保留占位符。
- `@date` 只允许日期，不得附加小时、分钟、秒或其他时间信息。
- `描述` 必须说明“这个类型负责什么”，不得为空，不写 `TODO`，不重复类型名称。
- 注释必须紧邻其类型；注解放在 KDoc 与类型声明之间。
- 修改已有类型时保留其原作者和日期，除非用户明确要求更新；本规则不要求批量改写历史文件。

## 中文注释

- 为新增的公开或 `internal` API 使用中文 KDoc，说明职责、关键参数、返回语义、失败方式或生命周期约束。
- 为并发、缓存、重试、安全校验、平台差异和非直观分支写中文注释，优先解释“为什么”和约束，不逐行翻译代码。
- 简单属性、显而易见的赋值、标准 getter/setter 不添加无信息量注释。
- 注释必须与实现同步；重构后删除已经失效的说明。

## 类型与文件分离

- 一个 Kotlin 文件只声明一个公开或 `internal` 顶层类型，文件名与该类型名一致。
- 每个 `data class` 单独放在自己的同名文件中，不与普通 `class`、接口、枚举、对象或其他 `data class` 混放。
- 每个普通 `class`、接口、密封类型、枚举和对象同样独立成文件；密封层级的公开或 `internal` 子类型也分别放置。
- 只服务于所属类型、不会被外部引用的 `private` 嵌套实现可以留在该类型内部。不要为了减少文件把可复用类型改成私有嵌套类型。
- 常量、私有辅助函数和 `companion object` 留在真正拥有它们的类中，不创建没有职责的工具类。

## 扩展函数与 inline 函数

- 顶层扩展函数或 `inline fun` 放入独立的职责文件，例如 `FlowExtensions.kt` 或 `NetworkInline.kt`；该文件不得再声明业务类或数据类。
- 多个函数只有在操作同一接收者或同一能力时才能共用扩展文件；无关函数继续拆分。
- 函数只服务于一个类且不需要公开复用时，优先作为该类的成员或 `private` 辅助函数放在类中。
- 仅在需要 `reified` 类型参数或确有高阶函数开销收益时使用 `inline`，不要为了形式统一而内联普通函数。

## Compose 文件边界

- Route、ViewModel、UiState、Screen 和可复用组件分别放置，不在一个页面文件中混入多个公开类型。
- 仅服务于当前 Screen 的小型 `private` Composable 可以留在 Screen 文件；可复用组件必须移动到自己的同名文件。
- Composable 负责展示和事件回调，不在其中创建网络客户端、Repository 或长期连接。

## 执行检查

完成 Kotlin 改动前逐项确认：

1. 检查每个新增顶层类型都有作者、`yyyy/M/d` 日期和非空中文描述，日期后没有时间。
2. 检查文件名与唯一的公开或 `internal` 顶层类型一致。
3. 检查 `data class`、普通类和扩展/inline 函数没有混放。
4. 检查复杂逻辑具有解释原因和约束的中文注释，且没有无效或逐行翻译式注释。
5. 运行受影响模块最小可执行测试或编译任务；不要以格式整理扩大当前任务范围。
