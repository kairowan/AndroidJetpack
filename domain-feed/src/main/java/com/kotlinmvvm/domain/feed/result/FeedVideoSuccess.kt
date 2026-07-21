package com.kotlinmvvm.domain.feed.result

import com.kotlinmvvm.domain.feed.model.FeedVideo

/**
 * @author 浩楠
 * @date 2026/7/20 17:51
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 视频详情恢复成功结果，携带与指定稳定编号对应的领域视频
 */
data class FeedVideoSuccess(val video: FeedVideo) : FeedVideoResult
