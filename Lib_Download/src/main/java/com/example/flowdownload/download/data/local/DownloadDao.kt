package com.example.flowdownload.download.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * @author 浩楠
 * @date 2026/6/9
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 下载数据访问接口，向上层提供按状态恢复、按优先级出队和终态清理能力。
 */
@Dao
interface DownloadDao {
    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    fun observe(id: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads ORDER BY priority DESC, createdAtEpochMs ASC")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE url = :url ORDER BY priority DESC, createdAtEpochMs ASC")
    fun observeByUrl(url: String): Flow<List<DownloadEntity>>

    @Query(
        "SELECT * FROM downloads WHERE destination = :destination " +
            "ORDER BY priority DESC, createdAtEpochMs ASC",
    )
    fun observeByDestination(destination: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE tag = :tag ORDER BY priority DESC, createdAtEpochMs ASC")
    fun observeByTag(tag: String): Flow<List<DownloadEntity>>

    @Query(
        "SELECT * FROM downloads WHERE groupName = :groupName " +
            "ORDER BY priority DESC, createdAtEpochMs ASC",
    )
    fun observeByGroup(groupName: String): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE id = :id LIMIT 1")
    suspend fun get(id: String): DownloadEntity?

    @Query("SELECT * FROM downloads ORDER BY priority DESC, createdAtEpochMs ASC")
    suspend fun getAll(): List<DownloadEntity>

    @Query("SELECT * FROM downloads WHERE url = :url ORDER BY priority DESC, createdAtEpochMs ASC")
    suspend fun getByUrl(url: String): List<DownloadEntity>

    @Query(
        "SELECT * FROM downloads WHERE destination = :destination " +
            "ORDER BY priority DESC, createdAtEpochMs ASC",
    )
    suspend fun getByDestination(destination: String): List<DownloadEntity>

    @Query("SELECT * FROM downloads WHERE tag = :tag ORDER BY priority DESC, createdAtEpochMs ASC")
    suspend fun getByTag(tag: String): List<DownloadEntity>

    @Query(
        "SELECT * FROM downloads WHERE groupName = :groupName " +
            "ORDER BY priority DESC, createdAtEpochMs ASC",
    )
    suspend fun getByGroup(groupName: String): List<DownloadEntity>

    @Query("SELECT * FROM downloads WHERE status IN (:statuses) ORDER BY priority DESC, createdAtEpochMs ASC")
    suspend fun getByStatuses(statuses: List<String>): List<DownloadEntity>

    @Query(
        "SELECT * FROM downloads WHERE destination = :destination " +
            "AND status IN (:statuses) LIMIT 1",
    )
    suspend fun findByDestination(
        destination: String,
        statuses: List<String>,
    ): DownloadEntity?

    @Query("DELETE FROM downloads WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE downloads SET priority = :priority, updatedAtEpochMs = :now WHERE id = :id")
    suspend fun updatePriority(
        id: String,
        priority: Int,
        now: Long,
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadEntity)
}
