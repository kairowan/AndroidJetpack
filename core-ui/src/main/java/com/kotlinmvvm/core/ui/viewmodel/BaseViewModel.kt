package com.kotlinmvvm.core.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * @author 浩楠
 * @date 2026/7/21 16:58
 *      _              _           _     _   ____  _             _ _
 *     / \   _ __   __| |_ __ ___ (_) __| | / ___|| |_ _   _  __| (_) ___
 *    / _ \ | '_ \ / _` | '__/ _ \| |/ _` | \___ \| __| | | |/ _` | |/ _ \
 *   / ___ \| | | | (_| | | | (_) | | (_| |  ___) | |_| |_| | (_| | | (_) |
 *  /_/   \_\_| |_|\__,_|_|  \___/|_|\__,_| |____/ \__|\__,_|\__,_|_|\___/
 * 描述: Compose 业务 ViewModel 公共基类，统一管理状态、线程安全任务、Loading 收尾与异常边界
 */
abstract class BaseViewModel<UiState : Any>(
    initialState: UiState,
    private val taskObserver: ViewModelTaskObserver = ViewModelTaskObserver.None
) : ViewModel() {
    private val mutableUiState = MutableStateFlow(initialState)

    /**
     * 页面唯一只读状态流 Route 应通过生命周期感知方式收集
     */
    val uiState: StateFlow<UiState> = mutableUiState.asStateFlow()

    /**
     * 当前页面状态快照 仅供子类判断和组合下一状态
     */
    protected val currentState: UiState
        get() = mutableUiState.value

    // Registry 只保存任务引用；所有 Job 仍是 viewModelScope 的子协程并随生命周期统一取消。
    private val taskRegistry = ViewModelTaskRegistry()

    /**
     * 使用原子转换更新页面状态 避免并发更新覆盖其他字段
     */
    protected fun updateState(transform: (UiState) -> UiState) {
        mutableUiState.update(transform)
    }

    /**
     * 整体替换页面状态 适合来源切换或完整结果恢复
     */
    protected fun setState(newState: UiState) {
        mutableUiState.value = newState
    }

    /**
     * 启动同名唯一任务 已有任务仍在执行时忽略本次调用 适合防止按钮重复请求
     * [onLoadingChanged] 只在任务真正启动和最终结束时回调 意外异常统一转换为
     * [ViewModelTaskException] 取消异常永远不会转为业务错误
     */
    private fun launchUniqueTask(
        taskKey: String,
        onLoadingChanged: (Boolean) -> Unit = {},
        onError: (ViewModelTaskException) -> Unit = ::onUnhandledTaskException,
        block: suspend () -> Unit
    ) {
        val job = taskRegistry.registerUnique(taskKey) {
            createManagedTask(taskKey, onLoadingChanged, onError, block)
        }
        if (job == null) {
            notifyTaskObserver(taskKey, ViewModelTaskEvent.SKIPPED)
            return
        }
        job.start()
    }

    /**
     * 取消同名旧任务并启动新任务 旧任务不得关闭替代任务持有的 Loading 状态
     */
    private fun launchLatestTask(
        taskKey: String,
        onLoadingChanged: (Boolean) -> Unit = {},
        onError: (ViewModelTaskException) -> Unit = ::onUnhandledTaskException,
        block: suspend () -> Unit
    ) {
        val (job, replaced) = taskRegistry.replace(taskKey) {
            createManagedTask(taskKey, onLoadingChanged, onError, block)
        }
        if (replaced) notifyTaskObserver(taskKey, ViewModelTaskEvent.REPLACED)
        job.start()
    }

    /**
     * 将单次挂起任务转换为冷流 供调用方继续组合官方 Flow 操作符
     */
    protected fun <T> taskFlow(block: suspend () -> T): Flow<T> = flow {
        emit(block())
    }

    /**
     * 将状态源的当前值同步恢复为页面状态，然后返回原 StateFlow 继续组合观察链。
     * 该函数不启动请求、不管理 Loading，有限请求必须使用独立的 [taskFlow] 链启动。
     */
    protected fun <Source> StateFlow<Source>.restoreUiState(
        mapper: (Source) -> UiState
    ): StateFlow<Source> = apply {
        setState(mapper(value))
    }

    /**
     * 使用调用方显式提供的同步快照恢复页面状态，并返回原 Flow 继续链式观察。
     * 适用于不应在仓库内为每个实体永久创建 StateFlow 的详情观察场景。
     */
    protected fun <Source> Flow<Source>.restoreUiState(
        initialValue: Source,
        mapper: (Source) -> UiState
    ): Flow<Source> = apply {
        setState(mapper(initialValue))
    }

    /**
     * 以唯一任务策略启动当前 Flow 链 已有同名任务时不订阅上游
     * 上游应使用  onEach 、 map  或  transformLatest  表达数据处理
     */
    protected fun <T> Flow<T>.launchUniqueIn(
        taskKey: String,
        onLoadingChanged: (Boolean) -> Unit = {},
        onError: (ViewModelTaskException) -> Unit = ::onUnhandledTaskException,
    ) {
        launchUniqueTask(
            taskKey = taskKey,
            onLoadingChanged = onLoadingChanged,
            onError = onError
        ) {
            collect()
        }
    }

    /**
     * 以最新任务策略启动当前 Flow 链 同名旧订阅会被取消 异常与 Loading 仍由基类收口
     */
    protected fun <T> Flow<T>.launchLatestIn(
        taskKey: String,
        onLoadingChanged: (Boolean) -> Unit = {},
        onError: (ViewModelTaskException) -> Unit = ::onUnhandledTaskException,
    ) {
        launchLatestTask(
            taskKey = taskKey,
            onLoadingChanged = onLoadingChanged,
            onError = onError
        ) {
            collect()
        }
    }

    /**
     * 处理没有被业务结果模型消费的任务异常 默认携带任务上下文继续抛出以避免静默失败
     * 子类只有在需要上报或转换为自身 UiState 时才应重写 并且不得向用户展示原始异常文本
     */
    protected open fun onUnhandledTaskException(exception: ViewModelTaskException) {
        throw exception
    }

    /**
     * 创建延迟启动的 ViewModel 子协程。
     *
     * 调用方必须先交给 [taskRegistry] 注册，再在注册表锁外启动；这个顺序保证并发的
     * 同名任务不会越过唯一或最新策略。
     */
    private fun createManagedTask(
        taskKey: String,
        onLoadingChanged: (Boolean) -> Unit,
        onError: (ViewModelTaskException) -> Unit,
        block: suspend () -> Unit
    ): Job {
        lateinit var job: Job
        job = viewModelScope.launch(start = CoroutineStart.LAZY) {
            var completed = false
            try {
                notifyTaskObserver(taskKey, ViewModelTaskEvent.STARTED)
                onLoadingChanged(true)
                block()
                completed = true
            } catch (error: CancellationException) {
                notifyTaskObserver(taskKey, ViewModelTaskEvent.CANCELLED)
                throw error
            } catch (error: Exception) {
                val taskException = ViewModelTaskException(taskKey, error)
                notifyTaskObserver(taskKey, ViewModelTaskEvent.FAILED, taskException)
                onError(taskException)
            } finally {
                val ownsTask = taskRegistry.removeIfOwned(taskKey, job)
                val loadingClosed = !ownsTask || closeLoading(
                    taskKey = taskKey,
                    onLoadingChanged = onLoadingChanged,
                    onError = onError
                )
                if (completed && loadingClosed) {
                    notifyTaskObserver(taskKey, ViewModelTaskEvent.COMPLETED)
                }
            }
        }
        return job
    }

    private fun closeLoading(
        taskKey: String,
        onLoadingChanged: (Boolean) -> Unit,
        onError: (ViewModelTaskException) -> Unit
    ): Boolean = try {
        onLoadingChanged(false)
        true
    } catch (error: CancellationException) {
        throw error
    } catch (error: Exception) {
        val taskException = ViewModelTaskException(taskKey, error)
        notifyTaskObserver(taskKey, ViewModelTaskEvent.FAILED, taskException)
        onError(taskException)
        false
    }

    private fun notifyTaskObserver(
        taskKey: String,
        event: ViewModelTaskEvent,
        exception: ViewModelTaskException? = null
    ) {
        try {
            taskObserver.onTaskEvent(taskKey, event, exception)
        } catch (_: Exception) {
            // 诊断组件失败不得改变业务任务、Loading 或异常边界。
        }
    }
}
