package com.kotlinmvvm.core.ui.viewmodel

import kotlinx.coroutines.Job

/**
 * @author 浩楠
 * @date 2026/7/21 13:35
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: ViewModel 同名任务注册表，只负责线程安全的唯一、替换与所有权判断
 */
internal class ViewModelTaskRegistry {
    private val lock = Any()
    private val jobs = mutableMapOf<String, Job>()

    fun registerUnique(taskKey: String, factory: () -> Job): Job? = synchronized(lock) {
        if (jobs[taskKey]?.isCompleted == false) null else factory().also { jobs[taskKey] = it }
    }

    fun replace(taskKey: String, factory: () -> Job): Pair<Job, Boolean> = synchronized(lock) {
        val previous = jobs[taskKey]?.takeUnless(Job::isCompleted)
        previous?.cancel()
        factory().also { jobs[taskKey] = it } to (previous != null)
    }

    fun removeIfOwned(taskKey: String, job: Job): Boolean = synchronized(lock) {
        if (jobs[taskKey] === job) {
            jobs.remove(taskKey)
            true
        } else {
            false
        }
    }
}
