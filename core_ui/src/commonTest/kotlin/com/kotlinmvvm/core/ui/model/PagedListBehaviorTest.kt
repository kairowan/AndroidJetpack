package com.kotlinmvvm.core.ui.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author 浩楠
 *
 * @date 2026-7-24 14:01
 *
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: 共享分页预加载边界回归测试
 */
class PagedListBehaviorTest {

    private val behavior = PagedListBehavior(prefetchDistance = 3)

    @Test
    fun loadsOnlyWhenVisibleItemReachesPrefetchWindow() {
        assertFalse(behavior.shouldLoadMore(lastVisibleIndex = 6, itemCount = 10))
        assertTrue(behavior.shouldLoadMore(lastVisibleIndex = 7, itemCount = 10))
    }

    @Test
    fun ignoresMissingOrEmptyLists() {
        assertFalse(behavior.shouldLoadMore(lastVisibleIndex = null, itemCount = 10))
        assertFalse(behavior.shouldLoadMore(lastVisibleIndex = 0, itemCount = 0))
    }
}
