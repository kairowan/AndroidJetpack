package com.ghn.feature.capture.utils

import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.internal.bind.DateTypeAdapter
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.util.*

/**
 * Author: zpj
 * Date: 2023-09-05 15:09
 * Desc: Gson工具类
 */
object GsonHelper {
    private var gson = GsonBuilder()
        .setPrettyPrinting()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter(Date::class.java, DateTypeAdapter())
        .create()

    fun setPrettyPrinting(json: String): String {
        return try {
            gson.toJson(JsonParser().parse(json))
        } catch (e: Exception) {
            json
        }
    }

    fun toJson(ob: Any): String = gson.toJson(ob)

    fun <T> fromJson(json: String, t: Type): T = gson.fromJson(json, t)

    fun <T> fromJsonArray(json: String, clazz: Class<T>): List<T> {
        val type = ParameterizedTypeImpl(clazz)
        var ob: List<T>? = gson.fromJson<List<T>>(json, type)
        if (ob == null) ob = ArrayList()
        return ob
    }

    private class ParameterizedTypeImpl<T> constructor(val clazz: Class<T>) : ParameterizedType {
        override fun getActualTypeArguments(): Array<Type> {
            return arrayOf(clazz)
        }

        override fun getRawType(): Type {
            return List::class.java
        }

        override fun getOwnerType(): Type? {
            return null
        }
    }
}