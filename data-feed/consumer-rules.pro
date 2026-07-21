# 示例 DTO 依赖后端字段名，数据模块自行保护协议模型，避免把产品规则泄漏到 app。
# 当真实项目统一使用 @SerializedName 后，可收窄为仅保留注解字段的规则。
-keep,allowoptimization class com.kotlinmvvm.data.feed.remote.model.** { <fields>; }
