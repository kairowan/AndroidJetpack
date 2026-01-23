package com.example.basemodel.base

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

open class SingleLiveEvent<T> : MutableLiveData<T?>() {
    private val pendingObservers = ConcurrentHashMap<Observer<in T?>, AtomicBoolean>()
    private val observerWrappers = ConcurrentHashMap<Observer<in T?>, Observer<T?>>()

    @MainThread
    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        val pending = AtomicBoolean(false)
        pendingObservers[observer] = pending
        val wrapper = Observer<T?> { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
        observerWrappers[observer] = wrapper
        super.observe(owner, wrapper)
    }

    @MainThread
    override fun setValue(t: T?) {
        pendingObservers.values.forEach { it.set(true) }
        super.setValue(t)
    }

    @MainThread
    override fun removeObserver(observer: Observer<in T?>) {
        val wrapper = observerWrappers.remove(observer)
        if (wrapper != null) {
            pendingObservers.remove(observer)
            super.removeObserver(wrapper)
            return
        }
        val original = observerWrappers.entries.firstOrNull { it.value == observer }?.key
        if (original != null) {
            observerWrappers.remove(original)
            pendingObservers.remove(original)
        }
        super.removeObserver(observer)
    }

    /**
     * Used for cases where T is Void, to make calls cleaner.
     */
    @MainThread
    fun call() {
        value = null
    }

}
