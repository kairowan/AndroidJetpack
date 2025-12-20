package com.kt.network.net

import java.io.Serializable

/**
 * @author 浩楠
 *
 * @date 2023/3/9   9:32.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
abstract class IBaseResponse<T>  {

    //后端返回的code
    abstract fun errorCode():Int
    //后端返回的信息
    abstract fun errorMsg():String
    abstract fun data():T?
    //接口请求是否成功
    abstract fun isSuccess():Boolean
}