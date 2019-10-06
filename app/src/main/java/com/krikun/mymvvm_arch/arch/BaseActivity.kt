
package com.krikun.mymvvm_arch.arch

import android.os.Bundle
import com.krikun.mymvvm_arch.helpers.coroutines.CoroutineLifecycleAwareScope
import com.krikun.mymvvm_arch.utils.errorSafety
import com.krikun.mymvvm_arch.utils.hideKeyboard
import android.view.KeyEvent
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf

abstract class BaseActivity<ViewBindingType : ViewDataBinding> : AppCompatActivity() {

    @get:LayoutRes
    protected abstract val layoutRes: Int
    protected abstract val currentFragment: Fragment?
    protected lateinit var binding: ViewBindingType
    //coroutines
    protected val coroutineScope: CoroutineLifecycleAwareScope by inject { parametersOf(this.lifecycle) }
    //

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, layoutRes)
    }

    fun clickBack() {
        errorSafety {
            hideKeyboard()
            super.onBackPressed()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            handleBackStack()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        handleBackStack()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            handleBackStack()
            return false
        }
        return super.onKeyDown(keyCode, event)
    }

    protected open fun handleBackStack() {
        when (val fragment = currentFragment) {
            is BaseFragment<*, *> -> fragment.onBackPressed()
            else -> clickBack()
        }
    }

}