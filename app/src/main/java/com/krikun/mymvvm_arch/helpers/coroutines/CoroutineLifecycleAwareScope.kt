
package com.krikun.mymvvm_arch.helpers.coroutines

import com.krikun.mymvvm_arch.utils.print
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlin.coroutines.CoroutineContext

/**
 * Provides and manage CoroutineScope for your lifecycle aware component.
 * */
class CoroutineLifecycleAwareScope(lifecycle: Lifecycle) : LifecycleObserver, CoroutineScope {

    init {
        lifecycle.addObserver(this)
    }

    private val job = Job()
    override val coroutineContext: CoroutineContext =
        job + Dispatchers.Main + CoroutineExceptionHandler { _, throwable -> throwable.print() }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun onDestroy() {
        job.cancel()
    }
}