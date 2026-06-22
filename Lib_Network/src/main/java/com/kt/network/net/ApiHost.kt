package com.kt.network.net

/**
 * @author 浩楠
 *
 * @date 2026/5/18
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: 标记 Retrofit 接口所属的网络 Host，避免业务模块重复传入 host 常量。
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class ApiHost(val value: NetworkHost)

/**
 * @Description: 统一定义项目中的网络 Host 类型和基础地址映射。
 */
enum class NetworkHost(
    val type: Int,
    val baseUrl: String
) {
    LOGIN(1, "http://139.224.186.198/"),
    TEST(2, "http://test2/"),
    VIDEO(3, "http://apis.juhe.cn/"),
    DEGREE(4, "http://v.juhe.cn/"),
    WAN_ANDROID(5, "https://www.wanandroid.com"),
    SURVEY(6, "http://rk.tongjidiaocha.com/");

    companion object {
        fun fromType(type: Int): NetworkHost {
            return entries.firstOrNull { it.type == type }
            ?: throw IllegalArgumentException("Unknown NetworkHost type: $type. Did you forget @ApiHost?")
        }
    }
}
