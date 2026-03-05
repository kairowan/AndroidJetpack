package com.kotlinmvvm.core.ui.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * 通用 State ViewModel：
 * 1 ViewModel 暴露单一 StateFlow State
 * 2 UI 通过调用 ViewModel 方法触发业务逻辑
 * 3 状态仅通过 reduce 更新
 */
abstract class BaseViewModel<State>(
    initialState: State
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<State> = _state.asStateFlow()

    protected fun reduce(reducer: State.() -> State) {
        _state.update(reducer)
    }
}
