
package com.krikun.mymvvm_arch.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.Parcelable
import com.krikun.mymvvm_arch.CommonApp
import android.util.TypedValue
import android.view.View
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment
import androidx.paging.PagedList
import io.reactivex.Single
import org.jetbrains.anko.dip
import java.io.Serializable

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_LOGICAL_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

inline fun <T> T?.nonNullOr(nonNull: T.() -> Unit = {}, nul: () -> Unit) {
    if (this != null) {
        this.nonNull()
    } else {
        nul()
    }
}

fun Boolean.inverse(): Boolean = !this

val Boolean.inverse: Boolean get() = !this

/**
 *  Inverses value within parentheses
 * */
fun isNot(expression: Boolean) = !expression

/**
 * Useful is xml data binding
 * */
fun allShouldBeTrue(vararg statement: Boolean): Boolean = statement.all { it }

/**
 * Useful is xml data binding
 * */
fun oneShouldBeTrue(vararg statement: Boolean): Boolean = statement.any { it }

fun <T> T.oneShouldEquals(vararg statement: T): Boolean = statement.any { it == this }

inline fun errorSafety(onError: (e: Exception) -> Unit = { it.print() }, action: () -> Unit) {
    try {
        action()
    } catch (e: Exception) {
        onError(e)
    }
}

inline fun errorSafetyIgnore(action: () -> Unit) {
    try {
        action()
    } catch (ignore: Exception) {
    }
}

/** Helpful wrapper for server 0-1 boolean. */
inline class ServerBoolean(val intValue: Int) {
    companion object {
        fun from(bool: Boolean) = ServerBoolean(if (bool) 1 else 0)
    }

    val value get() = intValue == 1
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_CAST_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/


val Fragment.simpleName: String
    get() = this.javaClass.simpleName

val Activity.simpleName: String
    get() = this.javaClass.simpleName

fun Boolean.toPhpBoolean(): Int = if (this) 1 else 0

fun Int.fromPhpBoolean(): Boolean? {
    return when (this) {
        0 -> false
        1 -> true
        else -> null
    }
}

fun Long.toInteger(): Int = this.toInt()

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_SIZES AND PROPERTIES_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun getDeviceName(): String {
    fun capitalize(s: String?): String {
        if (s == null || s.isEmpty()) {
            return ""
        }
        val first = s[0]
        return if (Character.isUpperCase(first)) {
            s
        } else {
            Character.toUpperCase(first) + s.substring(1)
        }
    }

    val manufacturer = Build.MANUFACTURER
    val model = Build.MODEL
    return if (model.startsWith(manufacturer)) {
        capitalize(model)
    } else {
        capitalize(manufacturer) + " " + model
    }
}

/**
 * @return status bar height in px
 */
fun getStatusBarHeight(): Int {
    val res = CommonApp.instance.resources
    res.getIdentifier("status_bar_height", "dimen", "android").let {
        return if (it > 0) {
            res.getDimensionPixelSize(it)
        } else {
            0
        }
    }
}

/**
 * @return action bar height in px
 */
fun getAppBarSize(): Int {
    val tv = TypedValue()
    val context = CommonApp.instance
    val res = CommonApp.instance.resources
    return if (context.theme?.resolveAttribute(android.R.attr.actionBarSize, tv, true) == true) {
        TypedValue.complexToDimensionPixelSize(tv.data, res.displayMetrics)
    } else {
        0
    }
}


fun Context.isTablet(): Boolean {
    val sizeMask = this.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK
    return sizeMask == Configuration.SCREENLAYOUT_SIZE_XLARGE || sizeMask == Configuration.SCREENLAYOUT_SIZE_LARGE
}

val Context.isLand: Boolean get() = this.resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

/**
 * @return status bar height in px
 */
fun Activity.getStatusBarHeight(): Int {
    resources.getIdentifier("status_bar_height", "dimen", "android").let {
        return if (it > 0) {
            resources.getDimensionPixelSize(it)
        } else {
            0
        }
    }
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_OPERATORS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

operator fun Int?.compareTo(another: Int): Int {
    return this?.compareTo(another) ?: 0
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_OTHERS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun <T> singleWithSchedulers(action: () -> T): Single<T> {
    return Single.fromCallable { action() }.compose(applyDefaultSchedulers())
}

fun <T> PagedList<T>.onChange(action: (isEmpty: Boolean) -> Unit) {
    this.addWeakCallback(null, object : PagedList.Callback() {
        override fun onInserted(position: Int, count: Int) {
            action(this@onChange.loadedCount == 0)
        }

        override fun onRemoved(position: Int, count: Int) {
            action(this@onChange.loadedCount == 0)
        }

        override fun onChanged(position: Int, count: Int) {
            action(this@onChange.loadedCount == 0)
        }
    })
}

inline fun <reified T : Serializable?> Fragment.serializableExtra(key: String) = lazy {
    arguments?.getSerializable(key) as? T
}

inline fun <reified T : Parcelable?> Fragment.parcelableExtra(key: String) = lazy {
    arguments?.getSerializable(key) as? T
}

fun Fragment.stringExtra(key: String, default: String? = null) = lazy {
    arguments?.getString(key, default) ?: default
}

fun Fragment.intExtra(key: String, default: Int = 0) = lazy {
    arguments?.getInt(key, default) ?: default
}

fun Fragment.booleanExtra(key: String, default: Boolean = false) = lazy {
    arguments?.getBoolean(key, default) ?: default
}

/** returns dip(dp) dimension value in pixels */
val Int.dp: Int get() = CommonApp.context.dip(this)

inline fun <T : View> T.afterMeasure(crossinline f: T.() -> Unit) {
    viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            if (measuredWidth > 0 && measuredHeight > 0) {
                viewTreeObserver.removeOnGlobalLayoutListener(this)
                f()
            }
        }
    })
}
