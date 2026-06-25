package com.example.flowdownload.download.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载模块内部数据库定义，当前仅维护下载任务主表。
 */
@Database(
    entities = [DownloadEntity::class],
    version = 5,
    exportSchema = false,
)
abstract class DownloadDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN pendingStopReason TEXT",
                )
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN deletePartialOnCancel INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN nextAttemptAtEpochMs INTEGER",
                )
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN tag TEXT",
                )
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN groupName TEXT",
                )
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN extrasBlob TEXT NOT NULL DEFAULT ''",
                )
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN networkType TEXT NOT NULL DEFAULT 'CONNECTED'",
                )
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN requiresCharging INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN requiresBatteryNotLow INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN requiresStorageNotLow INTEGER NOT NULL DEFAULT 0",
                )
                database.execSQL(
                    "ALTER TABLE downloads ADD COLUMN requiresDeviceIdle INTEGER NOT NULL DEFAULT 0",
                )
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_downloads_url ON downloads(url)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_downloads_destination ON downloads(destination)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_downloads_tag ON downloads(tag)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_downloads_groupName ON downloads(groupName)",
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_downloads_status ON downloads(status)",
                )
            }
        }
    }
}
