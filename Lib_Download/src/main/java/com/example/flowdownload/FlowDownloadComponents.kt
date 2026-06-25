package com.example.flowdownload

import android.content.Context
import androidx.room.Room
import com.example.flowdownload.download.data.DownloadStore
import com.example.flowdownload.download.data.RoomDownloadStore
import com.example.flowdownload.download.data.local.DownloadDatabase
import com.example.flowdownload.download.model.Clock
import com.example.flowdownload.download.model.SystemClock
import com.example.flowdownload.download.network.HttpDownloadEngine
import com.example.flowdownload.download.network.OkHttpDownloadEngine
import com.example.flowdownload.download.network.RetrofitDownloadEngine
import com.example.flowdownload.download.storage.AndroidDownloadDestinationAccess
import com.example.flowdownload.download.storage.DownloadDestinationAccess
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载库组件装配器，负责为前台仓库和后台 Worker 构建一致的数据库、存储和网络引擎。
 */
object FlowDownloadComponents {
    fun create(
        context: Context,
        config: FlowDownloadConfig,
        clock: Clock = SystemClock,
    ): CreatedComponents {
        val database = Room.databaseBuilder(
            context.applicationContext,
            DownloadDatabase::class.java,
            config.databaseName,
        ).addMigrations(
            DownloadDatabase.MIGRATION_1_2,
            DownloadDatabase.MIGRATION_2_3,
            DownloadDatabase.MIGRATION_3_4,
            DownloadDatabase.MIGRATION_4_5,
        )
            .build()
        val store: DownloadStore = RoomDownloadStore(database.downloadDao())
        val destinationAccess: DownloadDestinationAccess = AndroidDownloadDestinationAccess(
            context = context.applicationContext,
        )
        val factory = FlowDownloadHttpComponentFactories.resolve(config)
        val okHttpClient = factory.createOkHttpClient(
            config = config,
            builder = defaultOkHttpBuilder(config),
        )
        val engine: HttpDownloadEngine = when (config.httpStack) {
            DownloadHttpStack.OKHTTP -> OkHttpDownloadEngine(
                clock = clock,
                client = okHttpClient,
                progressThrottleMs = config.progressThrottleMillis,
                destinationAccess = destinationAccess,
            )

            DownloadHttpStack.RETROFIT -> RetrofitDownloadEngine(
                clock = clock,
                retrofit = factory.createRetrofit(
                    config = config,
                    okHttpClient = okHttpClient,
                    builder = defaultRetrofitBuilder(okHttpClient),
                ),
                progressThrottleMs = config.progressThrottleMillis,
                destinationAccess = destinationAccess,
            )
        }
        return CreatedComponents(
            database = database,
            store = store,
            engine = engine,
            clock = clock,
            destinationAccess = destinationAccess,
        )
    }

    private fun defaultOkHttpBuilder(config: FlowDownloadConfig): OkHttpClient.Builder {
        return OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .connectTimeout(config.connectTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(config.readTimeoutMillis.toLong(), TimeUnit.MILLISECONDS)
    }

    private fun defaultRetrofitBuilder(okHttpClient: OkHttpClient): Retrofit.Builder {
        return Retrofit.Builder()
            .baseUrl("https://localhost/")
            .client(okHttpClient)
            .validateEagerly(true)
    }
}

/**
 * 下载库已构建组件集合，用于在不同运行时之间传递统一的依赖实例。
 */
data class CreatedComponents(
    val database: DownloadDatabase,
    val store: DownloadStore,
    val engine: HttpDownloadEngine,
    val clock: Clock,
    val destinationAccess: DownloadDestinationAccess,
)
