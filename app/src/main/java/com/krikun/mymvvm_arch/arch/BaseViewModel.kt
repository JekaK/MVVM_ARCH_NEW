
package com.krikun.mymvvm_arch.arch

import com.krikun.mymvvm_arch.helpers.connection.NetworkConnection
import com.krikun.mymvvm_arch.utils.print
import androidx.lifecycle.ViewModel
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class BaseViewModel : ViewModel(), KoinComponent{

    private val networkConnection: NetworkConnection by inject()

    /*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_RX_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

    private val subscriptionHandler: CompositeDisposable by lazy { CompositeDisposable() }
    protected val subscriptionHandler2: CompositeDisposable by lazy { CompositeDisposable() }

    protected fun clearRx(handler: CompositeDisposable = subscriptionHandler) {
        handler.clear()
    }

    protected fun clearAllRx() {
        subscriptionHandler.clear()
        subscriptionHandler2.clear()
    }

    open fun destroyRx() {
        clearAllRx()
    }

    fun Disposable.untilDestroy(handler: CompositeDisposable = subscriptionHandler) = handler.add(this)

    /*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_COROUTINES_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

    private val job: Job = SupervisorJob()
    protected val coroutineScope: CoroutineScope =
        CoroutineScope(job + Dispatchers.Main + CoroutineExceptionHandler { _, throwable -> throwable.print() })

    /*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_VIEW MODEL STUFF_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

    override fun onCleared() {
        super.onCleared()
        destroyRx() // Unsubscribe from all rx subscriptions
        job.cancel() // Cancel all active coroutines
    }
}