package com.ghn.cocknovel.startup

import com.ghn.cocknovel.App
import com.ghn.cocknovel.di.appModules
import com.ghn.lib.base.aop.TraceTime
import com.ghn.lib.base.startup.StartupPhase
import com.ghn.lib.base.startup.StartupTask
import com.ghn.lib.base.startup.StartupTaskRunner
import com.ghn.lib.download.DownloadLibrary
import com.ghn.lib.upload.UploadLibrary
import com.kairowan.lib_ui_common.helper.ToastHelper
import com.scwang.smart.refresh.footer.ClassicsFooter
import com.scwang.smart.refresh.header.ClassicsHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import com.therouter.TheRouter
import me.jessyan.autosize.AutoSize
import me.jessyan.autosize.AutoSizeConfig
import me.jessyan.autosize.unit.Subunits
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

/**
 * @author 浩楠
 *
 * @date 2026/6/24
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 应用启动计划，统一声明主进程的初始化任务与执行阶段。
 */

object AppStartupPlan {
    @Volatile
    private var started = false

    fun start(app: App) {
        if (started) {
            return
        }
        started = true
        StartupTaskRunner.run(
            application = app,
            tasks = listOf(
                // 只有首屏创建前必须准备好的依赖才放在阻塞阶段。
                StartupTask("koin", StartupPhase.BLOCKING_MAIN) { initKoin(app) },
                StartupTask("autosize", StartupPhase.BLOCKING_MAIN) { initAutoSize(app) },
                StartupTask("router", StartupPhase.BLOCKING_MAIN) { initRouter(app) },
                // 非首屏刚需的基础设施延后到首帧后，避免继续挤占冷启动主线程。
                StartupTask("toast", StartupPhase.AFTER_FIRST_FRAME_MAIN) { initToast(app) },
                StartupTask("download-config", StartupPhase.AFTER_FIRST_FRAME_MAIN) {
                    configureDownload()
                },
                StartupTask("upload-config", StartupPhase.AFTER_FIRST_FRAME_MAIN) {
                    configureUpload()
                },
                // 这类 UI 默认配置可以首帧后再注册，不影响首页可见内容渲染。
                StartupTask("refresh-layout", StartupPhase.AFTER_FIRST_FRAME_MAIN) {
                    initRefreshLayoutDefaults()
                },
                // 传输能力只在后台预热，避免把客户端创建成本压到冷启动主线程。
                StartupTask("download-warmup", StartupPhase.AFTER_FIRST_FRAME_BACKGROUND) {
                    warmUpDownload(app)
                },
                StartupTask("upload-warmup", StartupPhase.AFTER_FIRST_FRAME_BACKGROUND) {
                    warmUpUpload(app)
                }
            )
        )
    }

    @TraceTime("startup:koin", warnAtMillis = 24L)
    private fun initKoin(app: App) {
        // 避免热重启或重复进入初始化流程时再次 startKoin 导致异常。
        if (GlobalContext.getOrNull() != null) {
            return
        }
        startKoin {
            androidContext(app)
            // 统一加载业务模块与库级绑定，保证跨模块服务都能被 Koin 发现。
            modules(appModules)
        }
    }

    @TraceTime("startup:autosize", warnAtMillis = 24L)
    private fun initAutoSize(app: App) {
        AutoSize.initCompatMultiProcess(app)
        AutoSize.checkAndInit(app)
        AutoSizeConfig.getInstance()
            .setCustomFragment(true)
            .setExcludeFontScale(true)
            .setPrivateFontScale(0.8f)
            .setLog(false)
            .setBaseOnWidth(true)
            .setUseDeviceSize(true)
            .unitsManager
            .setSupportDP(true)
            .setDesignSize(2160F, 3840F)
            .setSupportSP(true)
            .setSupportSubunits(Subunits.MM)
    }

    @TraceTime("startup:router", warnAtMillis = 24L)
    private fun initRouter(app: App) {
        // AppRouter 会在点击后立刻通过 TheRouter.get(...) 读取跨模块服务，
        // 这里改为同步初始化 routerInject，避免首屏阶段出现 provider 尚未就绪的竞态。
        TheRouter.init(app, false)
    }

    @TraceTime("startup:toast", warnAtMillis = 8L)
    private fun initToast(app: App) {
        ToastHelper.init(app)
    }

    @TraceTime("startup:download-config", warnAtMillis = 8L)
    private fun configureDownload() {
        DownloadLibrary.configure()
    }

    @TraceTime("startup:upload-config", warnAtMillis = 8L)
    private fun configureUpload() {
        UploadLibrary.configure()
    }

    @TraceTime("startup:refresh-layout", warnAtMillis = 8L)
    private fun initRefreshLayoutDefaults() {
        SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
            ClassicsHeader(context)
        }
        SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
            ClassicsFooter(context)
        }
    }

    @TraceTime("startup:download-warmup", warnAtMillis = 48L)
    private fun warmUpDownload(app: App) {
        DownloadLibrary.warmUp(app)
    }

    @TraceTime("startup:upload-warmup", warnAtMillis = 48L)
    private fun warmUpUpload(app: App) {
        UploadLibrary.warmUp(app)
    }
}
