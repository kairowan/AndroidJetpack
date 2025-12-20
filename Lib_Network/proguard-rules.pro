# 混淆规则
-keep class com.example.net.** { *; }
-keep class okhttp3.** { *; }
-keep class retrofit2.** { *; }

# 如果你使用了Gson作为Retrofit的转换器，也需要保留Gson的类
-keep class com.google.gson.** { *; }

# 保留 Retrofit 的注解
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# 保留 Retrofit 的反射使用
-keepclassmembers,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 保留 OkHttp 的注解
-dontwarn okhttp3.**
-dontwarn retrofit2.**

# HttpDNS 混淆规则
-keep class com.alibaba.sdk.android.httpdns.* {*;}
-keep class com.alibaba.sdk.android.httpdns.probe.* {*;}

# MMKV 混淆规则
-keep class com.tencent.mmkv.** {*;}

# Hilt 混淆规则
-keep class dagger.hilt.android.** { *; }
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# 保留 Hilt 注解
-keepattributes *Annotation*
#-keepclasseswithattributes *Annotation* class * {
#    @dagger.hilt.* <fields>;
#    @dagger.hilt.* <methods>;
#}

# 保留 Hilt 注解处理器生成的代码
-keepclasseswithmembers class * {
    @dagger.hilt.* <fields>;
}

