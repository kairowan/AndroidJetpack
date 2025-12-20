package com.kt.network.bean


/**
 * @author 浩楠
 *
 * @date 2023/1/16   14:44.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述:
 */
open class BaseResult<T> {
    var success: Boolean = false
    var errorCode: Int = 0
    var errorMsg: String = ""
    var data: T? = null
    fun errorCode(): Int {
        return errorCode
    }

     fun errorMsg(): String {
        return errorMsg
    }


     fun data(): T? {
        return data
    }


     fun isSuccess(): Boolean {
        return errorCode == 0
    }

}

data class FontDataNew(
    val curPage: Int,
    val datas: ArrayList<datas>,
    val offset: Double,
    val over: Boolean,
    val pageCount: Double,
    val size: Double,
    val total: Double,
)

data class datas(
    val adminAdd: Boolean,
    val apkLink: String,
    val audit: Double,
    val author: String,
    val canEdit: Boolean,
    val chapterId: Double,
    val chapterName: String,
    val collect: Boolean,
    val courseId: Double,
    val desc: String,
    val descMd: String,
    val envelopePic: String,
    val fresh: Boolean,
    val host: String,
    val id: Double,
    val isAdminAdd: Boolean,
    val link: String,
    val niceDate: String,
    val niceShareDate: String,
    val origin: String,
    val prefix: String,
    val projectLink: String,
    val publishTime: Double,
    val realSuperChapterId: Double,
    val route: Boolean,
    val selfVisible: Double,
    val shareDate: Double,
    val shareUser: String,
    val superChapterId: Double,
    val superChapterName: String,
    val tags: Any,
    val title: String,
    val type: Double,
    val userId: Double,
    val visible: Double,
    val zan: Double,
)




