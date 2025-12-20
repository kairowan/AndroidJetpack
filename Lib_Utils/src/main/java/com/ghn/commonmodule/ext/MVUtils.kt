package com.ghn.commonmodule.ext

import android.os.Parcelable
import com.tencent.mmkv.MMKV

/**
 * @author 浩楠
 *
 * @date 2023/9/21-14:27.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */


class MVUtils {
    init {
        mmkv = MMKV.defaultMMKV()
    }

    companion object {
        private var mInstance: MVUtils? = null
        private lateinit var mmkv: MMKV
        val instance: MVUtils?
            get() {
                if (mInstance == null) {
                    synchronized(MVUtils::class.java) {
                        if (mInstance == null) {
                            mInstance = MVUtils()
                        }
                    }
                }
                return mInstance
            }

        /**
         * 写入基本数据类型缓存
         *
         * @param key    键
         * @param object 值
         */
        fun put(key: String?, `object`: Any) {
            if (`object` is String) {
                mmkv.encode(key, `object`)
            } else if (`object` is Int) {
                mmkv.encode(key, `object`)
            } else if (`object` is Boolean) {
                mmkv.encode(key, `object`)
            } else if (`object` is Float) {
                mmkv.encode(key, `object`)
            } else if (`object` is Long) {
                mmkv.encode(key, `object`)
            } else if (`object` is Double) {
                mmkv.encode(key, `object`)
            } else if (`object` is ByteArray) {
                mmkv.encode(key, `object`)
            } else {
                mmkv.encode(key, `object`.toString())
            }
        }

        fun putSet(key: String?, sets: Set<String?>?) {
            mmkv.encode(key, sets)
        }

        fun putParcelable(key: String?, obj: Parcelable?) {
            mmkv.encode(key, obj)
        }

        fun getInt(key: String?): Int {
            return mmkv.decodeInt(key, 0)
        }

        fun getInt(key: String?, defaultValue: Int): Int {
            return mmkv.decodeInt(key, defaultValue)
        }

        fun getDouble(key: String?): Double {
            return mmkv.decodeDouble(key, 0.00)
        }

        fun getDouble(key: String?, defaultValue: Double): Double {
            return mmkv.decodeDouble(key, defaultValue)
        }

        fun getLong(key: String?): Long {
            return mmkv.decodeLong(key, 0L)
        }

        fun getLong(key: String?, defaultValue: Long): Long {
            return mmkv.decodeLong(key, defaultValue)
        }

        fun getBoolean(key: String?): Boolean {
            return mmkv.decodeBool(key, false)
        }

        fun getBoolean(key: String?, defaultValue: Boolean): Boolean {
            return mmkv.decodeBool(key, defaultValue)
        }

        fun getFloat(key: String?): Float {
            return mmkv.decodeFloat(key, 0f)
        }

        fun getFloat(key: String?, defaultValue: Float): Float {
            return mmkv.decodeFloat(key, defaultValue)
        }

        fun getBytes(key: String?): ByteArray? {
            return mmkv.decodeBytes(key)
        }

        fun getBytes(key: String?, defaultValue: ByteArray?): ByteArray? {
            return mmkv.decodeBytes(key, defaultValue)
        }

        fun getString(key: String?): String? {
            return mmkv.decodeString(key, "")
        }

        fun getString(key: String?, defaultValue: String?): String? {
            return mmkv.decodeString(key, defaultValue)
        }

        fun getStringSet(key: String?): Set<String>? {
            return mmkv.decodeStringSet(key, emptySet())
        }

        fun getParcelable(key: String?): Parcelable? {
            return mmkv.decodeParcelable(key, null)
        }

        /**
         * 移除某个key对
         *
         * @param key
         */
        fun removeKey(key: String?) {
            mmkv.removeValueForKey(key)
        }

        /**
         * 清除所有key
         */
        fun clearAll() {
            mmkv.clearAll()
        }
    }
}