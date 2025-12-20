import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import com.ghn.feature.capture.db.NetworkLogDB
import com.ghn.feature.capture.db.NetworkLogDao
import com.ghn.feature.capture.entity.NetworkLog
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Author: zpj
 * Date: 2023-09-05 15:17
 * Desc: 库单例类
 */
@SuppressLint("StaticFieldLeak")
object NetworkCapture {
    var context: Context? = null
    var networkLogDB: NetworkLogDB? = null
    private const val AUTHORITY = "capture_network_provider"
    lateinit var networkLogTableUri: Uri    // 观察数据库变化用的URI
    private val singleThreadExecutor: ExecutorService = Executors.newSingleThreadExecutor()  // 单线程池
    var currentNetworkLog: NetworkLog? = null   // 当前查看的NetworkLog实例，有些请求的实体太大，无法通过Intent传
    var currentSearchText: String? = null // 当前要查找的文本

    fun init(context: Context) {
        this.context = context
        this.networkLogTableUri =
            Uri.parse("content://${this.context!!.packageName}.${AUTHORITY}/${NetworkLogDao.TABLE_NAME}")
        singleThreadExecutor.execute {
            if (networkLogDB == null) networkLogDB = NetworkLogDB(context)
        }
    }

    fun insertNetworkLog(data: NetworkLog) = singleThreadExecutor.execute {
        networkLogDB?.let { NetworkLogDao(it).insert(data) }
    }

    fun queryNetworkLog(offset: Int = 0, limit: Int = 20): ArrayList<NetworkLog> =
        singleThreadExecutor.submit(Callable {
            networkLogDB?.let { NetworkLogDao(it).query(offset, limit) } ?: arrayListOf()
        }).get()

    fun queryNetworkLogById(id: Long): NetworkLog? = singleThreadExecutor.submit(Callable {
        networkLogDB?.let { NetworkLogDao(it).queryNetworkLogById(id) }
    }).get()

    /**
     * 查询URL包含特定字符串的数据
     * */
    fun queryNetworkLogByFilter(filter: String, offset: Int = 0, limit: Int = 20): ArrayList<NetworkLog> =
        singleThreadExecutor.submit(Callable {
            networkLogDB?.let { NetworkLogDao(it).query(offset, limit, "url LIKE ?", arrayOf("%$filter%")) }
                ?: arrayListOf()
        }).get()

    fun clearNetworkLog() = singleThreadExecutor.execute {
        networkLogDB?.let { NetworkLogDao(it).clear() }
    }


}