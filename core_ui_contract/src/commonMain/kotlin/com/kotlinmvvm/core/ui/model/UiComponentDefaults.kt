package com.kotlinmvvm.core.ui.model

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 共享错误和空态文案
 */
data class UiFeedbackCopy(
    val errorTitle: String = "加载失败",
    val retryLabel: String = "重试",
    val emptyMessage: String = "暂无数据",
    val noMoreMessage: String = "— 没有更多了 —"
)

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 共享分页加载策略
 */
data class PagedListBehavior(
    val prefetchDistance: Int = 3
) {
    fun shouldLoadMore(
        lastVisibleIndex: Int?,
        itemCount: Int
    ): Boolean {
        if (lastVisibleIndex == null || itemCount <= 0) {
            return false
        }
        return lastVisibleIndex >= itemCount - prefetchDistance
    }
}

/**
 * @author 浩楠
 * @date 2026-3-11
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: 共享 UI 默认配置入口
 */
object UiComponentDefaults {
    val feedbackCopy = UiFeedbackCopy()
    val pagedListBehavior = PagedListBehavior()
}
