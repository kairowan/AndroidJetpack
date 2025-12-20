package com.kt.NetworkModel.bean



/**
 * @author 浩楠
 *
 * @date 2023/9/18-14:56.
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * @Description: TODO
 */
data class TabFrameBean(
    val data: Data,
    val errorCode: Int, // 0
    val errorMsg: String
) {
    data class Data(
        val curPage: Int, // 0
        val datas: ArrayList<Data>,
        val offset: Int, // -15
        val over: Boolean, // false
        val pageCount: Int, // 2
        val size: Int, // 15
        val total: Int // 28
    ) {
        data class Data(
            val adminAdd: Boolean, // false
            val apkLink: String,
            val audit: Int, // 1
            val author: String, // yangkun19921001
            val canEdit: Boolean, // false
            val chapterId: Int, // 367
            val chapterName: String, // 资源聚合类
            val collect: Boolean, // false
            val courseId: Int, // 13
            val desc: String, // 疫情一过，我相信将会是面试求职的高峰时期，如果此时手里有份高质量的面试宝典，那么你将得心应手面对考官各种问题。虽然不敢保证你能应聘上心仪的职位，但是能保证看完这些内容你的收获将超乎你的想象! 此份面试宝典搜集各大网络平台(如果侵权，请您告知)，在此感谢他们的用心总结，才有这份足够全面的面试宝典！
            val descMd: String,
            val envelopePic: String, // https://www.wanandroid.com/resources/image/pc/default_project_img.jpg
            val fresh: Boolean, // false
            val host: String,
            val id: Int, // 12460
            val isAdminAdd: Boolean, // false
            val link: String, // https://www.wanandroid.com/blog/show/2722
            val niceDate: String, // 2020-03-17 23:42
            val niceShareDate: String, // 2020-03-17 23:42
            val origin: String,
            val prefix: String,
            val projectLink: String, // https://github.com/yangkun19921001/Blog
            val publishTime: Long, // 1584459753000
            val realSuperChapterId: Int, // 293
            val selfVisible: Int, // 0
            val shareDate: Long, // 1584459753000
            val shareUser: String,
            val superChapterId: Int, // 294
            val superChapterName: String, // 开源项目主Tab
            val tags: List<Tag>,
            val title: String, // 面向 Android 高级工程师的一份面试宝典 (持续更新)
            val type: Int, // 0
            val userId: Int, // -1
            val visible: Int, // 1
            val zan: Int // 0
        ) {
            data class Tag(
                val name: String, // 项目
                val url: String // /project/list/1?cid=367
            )
        }
    }
}