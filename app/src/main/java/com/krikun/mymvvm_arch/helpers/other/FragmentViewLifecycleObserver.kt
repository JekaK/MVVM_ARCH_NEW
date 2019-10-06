package com.krikun.mymvvm_arch.helpers.other

import com.krikun.mymvvm_arch.arch.BaseFragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

@Suppress("LeakingThis")
/** Useful class which observe [BaseFragment.viewLifecycle] and triggers afterViewCreated. */
abstract class FragmentViewLifecycleObserver(baseFragment: BaseFragment<*, *>) : LifecycleObserver {

    init {
        baseFragment.viewLifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    open fun afterCreate() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    open fun afterDestroy() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    open fun afterViewCreated() {
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    open fun afterViewDestroy() {
    }

}