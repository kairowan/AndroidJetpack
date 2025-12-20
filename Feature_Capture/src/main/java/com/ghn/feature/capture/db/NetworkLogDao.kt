package com.ghn.feature.capture.db

import android.content.ContentValues
import android.database.Cursor
import com.ghn.feature.capture.entity.NetworkLog

/**
 * Author: zpj
 * Date: 2023-09-05 14:14
 * Desc: 请求日志Dao
 */
class NetworkLogDao(private val db: NetworkLogDB) {
    companion object {
        const val TABLE_NAME = "network_log"    // 表名

        /**
         * 建表SQL语句
         * */
        fun createTableSql() = StringBuilder("CREATE TABLE $TABLE_NAME(").apply {
            append("id INTEGER PRIMARY KEY AUTOINCREMENT,")
            append("method TEXT,")
            append("url TEXT,")
            append("scheme TEXT,")
            append("protocol TEXT,")
            append("host TEXT,")
            append("path TEXT,")
            append("duration INTEGER,")
            append("requestTime INTEGER,")
            append("requestHeaders TEXT,")
            append("requestBody TEXT,")
            append("requestContentType TEXT,")
            append("responseCode INTEGER,")
            append("responseTime INTEGER,")
            append("responseHeaders TEXT,")
            append("responseBody TEXT,")
            append("responseMessage TEXT,")
            append("responseContentType TEXT,")
            append("responseContentLength INTEGER,")
            append("errorMsg STRING,")
            append("source STRING")
            append(")")
        }.toString()
    }


    /**
     * 插入数据
     * */
    fun insert(data: NetworkLog) {
        db.writableDatabase.insert(TABLE_NAME, null, ContentValues().apply {
            put("method", data.method)
            put("url", data.url)
            put("scheme", data.scheme)
            put("protocol", data.protocol)
            put("host", data.host)
            put("path", data.path)
            put("duration", data.duration)
            put("requestTime", data.requestTime)
            put("requestHeaders", data.requestHeaders)
            put("requestBody", data.requestBody)
            put("requestBody", data.requestBody)
            put("requestContentType", data.requestContentType)
            put("responseCode", data.responseCode)
            put("responseTime", data.responseTime)
            put("responseHeaders", data.responseHeaders)
            put("responseBody", data.responseBody)
            put("responseMessage", data.responseMessage)
            put("responseContentType", data.responseContentType)
            put("responseContentLength", data.responseContentLength)
            put("errorMsg", data.errorMsg)
            put("source", data.source)
        })
        NetworkCapture.context?.contentResolver?.notifyChange(NetworkCapture.networkLogTableUri, null)
    }

    /**
     * 查询数据
     * @param offset 第几页，从0开始
     * @param limit 分页条数
     * */
    fun query(
        offset: Int = 0,
        limit: Int = 20,
        selection: String? = null,
        selectionArgs: Array<String>? = null
    ): ArrayList<NetworkLog> {
        val cursor = db.readableDatabase.query(
            TABLE_NAME, null, selection, selectionArgs, null, null, "id DESC", "${offset * limit},${limit}"
        )
        val logList = cursor.getNetworkLogList()
        cursor.close()
        return logList
    }

    /**
     * 根据id查询记录
     * */
    fun queryNetworkLogById(id: Long): NetworkLog? {
        val cursor = db.readableDatabase.query(
            TABLE_NAME, null, "id=?", arrayOf("$id"), null, null, null, null
        )
        val logList = cursor.getNetworkLogList()
        cursor.close()
        return if (logList.isEmpty()) null else logList[0]
    }

    /**
     * 根据id删除数据
     * @param id 记录id
     * */
    fun deleteById(id: Long) {
        db.writableDatabase.delete(TABLE_NAME, "id = ?", arrayOf("$id"))
    }

    /**
     * 清空数据
     * */
    fun clear() {
        db.writableDatabase.delete(TABLE_NAME, null, null)
    }

    /**
     * 遍历游标获取请求日志列表的方法
     * */
    private fun Cursor.getNetworkLogList() = arrayListOf<NetworkLog>().apply {
        if (moveToFirst()) {
            do {
                add(NetworkLog().apply {
                    id = getLong(0)
                    method = getString(1)
                    url = getString(2)
                    scheme = getString(3)
                    protocol = getString(4)
                    host = getString(5)
                    path = getString(6)
                    duration = getLong(7)
                    requestTime = getLong(8)
                    requestHeaders = getString(9)
                    requestBody = getString(10)
                    requestContentType = getString(11)
                    responseCode = getInt(12)
                    responseTime = getLong(13)
                    responseHeaders = getString(14)
                    responseBody = getString(15)
                    responseMessage = getString(16)
                    responseContentType = getString(17)
                    responseContentLength = getLong(18)
                    errorMsg = getString(19)
                    source = getString(20)

                })
            } while (moveToNext())
        }
    }
}