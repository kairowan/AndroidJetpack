package com.kotlinmvvm.domain.feed.result

import com.kotlinmvvm.domain.feed.model.FeedPage

/**
 * @author 浩楠
 * @date 2026/7/20 17:51
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 信息流加载成功结果，携带仓库已经合并并保存的完整页面事实
 */
data class FeedLoadSuccess(val page: FeedPage) : FeedLoadResult
