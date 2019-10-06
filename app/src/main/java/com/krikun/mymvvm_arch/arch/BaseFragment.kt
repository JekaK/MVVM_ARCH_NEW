
package com.krikun.mymvvm_arch.arch

import android.content.Context
import android.os.Bundle
import com.krikun.mymvvm_arch.helpers.coroutines.CoroutineLifecycleAwareScope
import com.krikun.mymvvm_arch.managers.MessageManager
import com.krikun.mymvvm_arch.utils.hideKeyboard
import com.krikun.mymvvm_arch.utils.toast
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import kotlin.properties.Delegates

abstract class BaseFragment<ActivityType : BaseActivity<*>, ViewBindingType : ViewDataBinding> : Fragment() {
    @get:LayoutRes
    abstract val layoutRes: Int
    protected var host: ActivityType? = null
    protected lateinit var binding: ViewBindingType
    // Alert
    private var alertToClose: AlertDialog? = null
    // Coroutines
    val coroutineScope: CoroutineLifecycleAwareScope by inject { parametersOf(this.lifecycle) }
    // Lifecycle
    val viewLifecycle: LifecycleRegistry by lazy { LifecycleRegistry(this) }
    // Session time
    private var startTime: Long by Delegates.notNull()
    open val initTime = 200
    // Message manager
    private val messageManager: MessageManager by inject()
    // Defines whether auto handling message (showing toast) enabled
    protected var handleMessage = true

    @Suppress("UNCHECKED_CAST")
    override fun onAttach(context: Context) {
        super.onAttach(context)
        host = try {
            context as ActivityType
        } catch (e: ClassCastException) {
            throw error("Activity type doesn't much!")
        }
    }

    override fun onDetach() {
        super.onDetach()
        host = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTime = System.currentTimeMillis()
        //call custom onCreate
        onCreate(savedInstanceState == null)

        // Lifecycle state
        viewLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        // Handle messages - show toast by default
        if (handleMessage) {
            messageManager.messageObservable
                .subscribe({ message -> toast(message) }, {})
                .untilDestroy()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onActivityCreated()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, layoutRes, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Call abstract onViewCreated
        onViewCreated()

        // Lifecycle state
        viewLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_START)

        // Hide keyboard on start
        hideKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        alertToClose?.cancel()
        alertToClose = null

        // Lifecycle state
        viewLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyRx()
        // Lifecycle state
        viewLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    /**
     * Override this to handle back press by yourself.
     * Call super.onBackPressed() if you want to call normal behavior.*/
    open fun onBackPressed() {
        if ((System.currentTimeMillis() - startTime) < initTime) return //if we are trying to go back very early - do nothing

        //call back click task on BaseActivity
        (host as? BaseActivity<*>)?.clickBack()
    }

    open fun onCreate(initial: Boolean) {}

    open fun onActivityCreated() {}

    abstract fun onViewCreated()

    /*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_RX_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

    private val subscriptionHandler: CompositeDisposable by lazy { CompositeDisposable() }
    protected val subscriptionHandler2: CompositeDisposable by lazy { CompositeDisposable() }

    protected fun clearRx(handler: CompositeDisposable = subscriptionHandler) {
        handler.clear()
    }

    protected fun clearAll() {
        subscriptionHandler.clear()
        subscriptionHandler2.clear()
    }

    open fun destroyRx() {
        listOf(subscriptionHandler, subscriptionHandler2).forEach { clearRx(it) }
    }

    fun Disposable.untilDestroy(handler: CompositeDisposable = subscriptionHandler) = handler.add(this)

    /*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_UTILS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

    /**
     * Shows alert and closes given alert on [onDestroyView] to prevent memory leaks.
     * */
    protected fun AlertDialog.Builder.saveShow() {
        alertToClose = this.show()
    }

    /**
     * Setups [Toolbar] in [BaseFragment].
     *
     * @param title - Sets toolbar title.
     * If param is null - nothing happens(It has done for correct setting title for xml).
     *
     * @param onBackPress - Sets toolbar back action.
     * Calls [onBackPressed] by default.
     *
     * */
    protected fun Toolbar.setup(title: String? = null, onBackPress: (() -> Unit)? = { onBackPressed() }) {
        // Setup title if not null
        title?.let { this.title = title }
        // Setup back action
        onBackPress?.let { action -> setNavigationOnClickListener { action() } }
    }
}