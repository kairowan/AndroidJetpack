package com.ghn.feature.capture.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Author: zpj
 * Date: 2023-09-04 10:44
 * Desc: 网络日志数据库子类
 */
class NetworkLogDB(context: Context) :
    SQLiteOpenHelper(context, "cp_network_capture.db", null, DB_VERSION) {
    companion object {
        private const val DB_VERSION = 1
    }

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(NetworkLogDao.createTableSql())
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {}
}