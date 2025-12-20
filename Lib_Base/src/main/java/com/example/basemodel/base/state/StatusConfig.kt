package com.example.basemodel.base.state

import com.ghn.lib.base.R


/**
 * @author 浩楠
 * @date 2025/6/17
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 *  描述: TODO
 */
data class StatusConfig(
    val loadingLayoutRes: Int = R.layout.layout_status_loading,
    val emptyLayoutRes: Int = R.layout.layout_status_empty,
    val errorLayoutRes: Int = R.layout.layout_status_error,
)
