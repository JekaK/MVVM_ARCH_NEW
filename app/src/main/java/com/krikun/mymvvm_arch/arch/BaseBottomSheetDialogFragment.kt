package com.krikun.mymvvm_arch.arch

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import com.krikun.mymvvm_arch.helpers.coroutines.CoroutineLifecycleAwareScope
import com.krikun.mymvvm_arch.utils.hideKeyboard
import com.krikun.mymvvm_arch.utils.isLand
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import kotlin.properties.Delegates

abstract class BaseBottomSheetDialogFragment<ViewBindingType : ViewDataBinding> : BottomSheetDialogFragment() {
    @get:LayoutRes
    abstract val layoutRes: Int
    protected lateinit var binding: ViewBindingType
    // Coroutines
    protected val coroutineScope: CoroutineLifecycleAwareScope by inject { parametersOf(this.lifecycle) }
    // Lifecycle
    val viewLifecycle: LifecycleRegistry by lazy { LifecycleRegistry(this) }
    // Session time
    private var startTime: Long by Delegates.notNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startTime = System.currentTimeMillis()
        //call custom onCreate
        onCreate(savedInstanceState == null)

        // Lifecycle state
        viewLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)

        // In landscape mode expand dialog from start
        if (context?.isLand == true) {
            dialog.setOnShowListener {
                // For AndroidX use: com.google.android.material.R.id.design_bottom_sheet
                val bottomSheet: FrameLayout = dialog.findViewById<View>(
                    com.google.android.material.R.id.design_bottom_sheet
                ) as FrameLayout
                val behavior = BottomSheetBehavior.from(bottomSheet)
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }

        return dialog
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

        // Lifecycle state
        viewLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyRx()
        // Lifecycle state
        viewLifecycle.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        onCancel()
    }

    /** Convenient method to show dialog. */
    fun show(manager: FragmentManager?) {
        manager?.let { mng -> super.show(mng, tag) }
    }

    open fun onCreate(initial: Boolean) {}

    open fun onActivityCreated() {}

    abstract fun onViewCreated()

    abstract fun onCancel()

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
}