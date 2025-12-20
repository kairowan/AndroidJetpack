# 保留调试信息（崩溃日志可读）
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# 四大组件
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep class android.support.** { *; }
-keep class androidx.** { *; }

# 自定义 View 构造函数
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-dontwarn kotlin.**
-keep class kotlin.** { *; }
-keepclassmembers class kotlin.Metadata { *; }


-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keep class com.google.dagger.** { *; }
-dontwarn dagger.**
-dontwarn javax.inject.**
-dontwarn com.google.dagger.**

# Hilt 注入类保留注解信息
-keepattributes *Annotation*
-keep class * {
    @dagger.* <fields>;
    @javax.inject.* <fields>;
}

# 保留 BannerModel 及其字段（最核心）
-keep class **.WBanner$Data { *; }
-keepclassmembers class **.WBanner$Data {
    <fields>;
    public <init>(...);
}

# Gson 反射序列化字段支持
-keepclassmembers class ** {
    @com.google.gson.annotations.SerializedName <fields>;
}
# 保留 CaptureInterceptor 类及其构造函数
-keep class com.ghn.cp_network_capture.interceptor.CaptureInterceptor {
    public <init>();
}


# 如果没有使用注解也保守保留字段
-keepclassmembers class ** {
    <fields>;
}

# ViewBinding 支持
-keep class **.databinding.*Binding { *; }
-keep class **.BR { *; }



# OkHttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-dontwarn okio.**
-keep class okio.** { *; }

#RxJava & RxLifecycle
-dontwarn rx.**
-keep class rx.** { *; }
-keep class com.trello.rxlifecycle4.** { *; }
-dontwarn com.trello.rxlifecycle4.**
#Glide 4.x
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**
#Room
-keep class androidx.room.** { *; }

#Navigation
-keep class androidx.navigation.** { *; }
-dontwarn androidx.navigation.**
#BRV
-keep class com.drake.brv.** { *; }
-dontwarn com.drake.brv.**

#EasyWindow & TitleBar & XXPermissions
-keep class com.hjq.permissions.** { *; }
-dontwarn com.hjq.permissions.**

-keep class com.github.getActivity.TitleBar.** { *; }
-keep class com.github.getActivity.EasyWindow.** { *; }
-dontwarn com.github.getActivity.**
#XPopup
-keep class com.lxj.xpopup.** { *; }
-dontwarn com.lxj.xpopup.**

#Therouter
-keep class androidx.annotation.Keep
-keep @androidx.annotation.Keep class * {*;}
-keepclassmembers class * {
    @androidx.annotation.Keep *;
}
-keepclasseswithmembers class * {
    @com.therouter.router.Autowired <fields>;
}
#协程（kotlinx）
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# 保留 R 文件
-keep class **.R$* { *; }

# 防止 Serializable 丢失字段
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
}

# ViewModel 保留构造
-keepclassmembers class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
