# 保留调试信息（崩溃日志可读）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Android 网络层仍通过 Gson 反射读取这组内部 DTO。
-keep class com.kotlinmvvm.core.data.eyepetizer.EyepetizerPayload** { *; }
