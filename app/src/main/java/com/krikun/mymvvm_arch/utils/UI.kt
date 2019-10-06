
package com.krikun.mymvvm_arch.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import com.krikun.mymvvm_arch.CommonApp
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.databinding.ObservableField
import androidx.fragment.app.Fragment
import com.transitionseverywhere.Fade
import com.transitionseverywhere.Transition
import com.transitionseverywhere.TransitionManager
import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent
import org.jetbrains.anko.toast
import kotlin.math.max

const val TRANSITION_TINY = 100L
const val TRANSITION_SHORT_TINY = 150L
const val TRANSITION_SHORT = 200L
const val TRANSITION_MEDIUM_TINY = 250L
const val TRANSITION_MEDIUM_SHORT = 300L
const val TRANSITION_MEDIUM = 400L
const val TRANSITION_LONG = 800L

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_VISIBILITY_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun ObservableField<Int>.changeVisibility(isVisible: Boolean) = set(if (isVisible) View.VISIBLE else View.GONE)

fun ObservableField<Int>.changeVisibilitySoft(isVisible: Boolean) = set(if (isVisible) View.VISIBLE else View.INVISIBLE)

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_ANIMATION_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

inline fun ViewGroup.animateInContainer(
    transition: Transition = Fade(),
    duration: Long = TRANSITION_MEDIUM,
    action: (View) -> Unit
) {
    TransitionManager.beginDelayedTransition(this, transition.setDuration(duration))
    action(this)
}

fun View.showPretty(transition: Transition = Fade(), container: ViewGroup? = null, duration: Long = TRANSITION_MEDIUM) =
    animate(transition, container, duration) { it.visible() }

fun View.hidePretty(transition: Transition = Fade(), container: ViewGroup? = null, duration: Long = TRANSITION_MEDIUM) =
    animate(transition, container, duration) { it.gone() }

inline fun View.animate(
    transition: Transition = Fade(),
    container: ViewGroup? = null,
    duration: Long = TRANSITION_MEDIUM,
    action: (View) -> Unit
) {
    if (container == null) {
        (this.parent as ViewGroup?)?.apply {
            TransitionManager.beginDelayedTransition(this, transition.setDuration(duration))
        }
    } else {
        TransitionManager.beginDelayedTransition(container, transition.setDuration(duration))
    }

    action(this)
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_KEYBOARD_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun Fragment.hideKeyboard() {
    this.activity?.currentFocus?.let {
        (this.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun Activity.onKeyboardStateChanged(onKeyboardStateChanged: (shown: Boolean) -> Unit) {
    KeyboardVisibilityEvent.setEventListener(this) {
        // some code depending on keyboard visibility status
        onKeyboardStateChanged(it)
    }
}

fun Fragment.onKeyboardStateChanged(onKeyboardStateChanged: (shown: Boolean) -> Unit) {
    activity?.onKeyboardStateChanged(onKeyboardStateChanged)
}

fun Fragment.showKeyboard(view: View) {
    (this.activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
        showSoftInput(view, 0)
    }
}


fun Activity.hideKeyboard() {
    this.currentFocus?.let {
        (this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            hideSoftInputFromWindow(it.windowToken, 0)
        }
    }
}

fun Fragment.showKeyboard() {
    this.activity?.currentFocus?.let {
        (this.activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            showSoftInput(it, 0)
        }
    }
}

fun Activity.showKeyboard() {
    this.currentFocus?.let {
        (this.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)?.apply {
            showSoftInput(it, 0)
        }
    }
}

val Activity.isKeyboardVisible
    get() = KeyboardVisibilityEvent.isKeyboardVisible(this)

val Fragment.isKeyboardVisible
    get() = KeyboardVisibilityEvent.isKeyboardVisible(activity)

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_TOASTS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

private val toastMarginTop = getAppBarSize() + getStatusBarHeight()

fun Fragment.toast(message: String?) {
    message?.let { context?.toast(message) }
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_SPAN COUNT_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun Fragment.getAvailableSpanCount(elementWidthDp: Int): Int {
    val outSize = Point()
    this.activity?.windowManager?.defaultDisplay?.getSize(outSize)
    val width =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, elementWidthDp.toFloat(), resources.displayMetrics)
    return max((outSize.x.toFloat() / width).toInt(), 1)
}

fun Context.getAvailableSpanCount(elementWidthDp: Int): Int {
    val outSize = Point()
    val wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    wm.defaultDisplay.getSize(outSize)
    val width =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, elementWidthDp.toFloat(), resources.displayMetrics)
    return max((outSize.x.toFloat() / width).toInt(), 1)
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_COLOR_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun getColor(@ColorRes color: Int) = ContextCompat.getColor(CommonApp.instance, color)

fun Context.getColorCompat(@ColorRes color: Int) = ContextCompat.getColor(this, color)

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_FOCUS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun Activity.clearFocus() {
    val currentFocus = this.currentFocus
    currentFocus?.clearFocus()
}

fun Fragment.clearFocus() {
    val currentFocus = activity?.currentFocus
    currentFocus?.clearFocus()
}