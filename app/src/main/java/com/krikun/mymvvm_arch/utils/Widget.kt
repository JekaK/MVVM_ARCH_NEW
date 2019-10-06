package com.krikun.mymvvm_arch.utils

import android.annotation.SuppressLint
import android.content.Context
import com.krikun.mymvvm_arch.widgets.ClosableSpinner
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.text.method.PasswordTransformationMethod
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import androidx.annotation.IntDef
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.RecyclerView
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexboxLayoutManager
import com.google.android.flexbox.JustifyContent
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jakewharton.rxbinding2.view.RxView
import com.trello.rxlifecycle3.kotlin.bindToLifecycle
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_VIEW_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun View.changeVisibility(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.GONE
}

fun View.changeVisibilitySoft(isVisible: Boolean) {
    visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.toggleVisibility() {
    if (isVisible) {
        gone()
    } else {
        visible()
    }
}

fun View.disable() {
    isEnabled = false
}

fun View.enable() {
    isEnabled = true
}

val View?.isVisible: Boolean
    get() {
        this ?: return false

        return visibility == View.VISIBLE
    }

fun View.getCenterX(): Int {
    val coordinates = intArrayOf(0, 0)
    getLocationOnScreen(coordinates)
    return coordinates[0] + width / 2
}

fun View.getCenterY(): Int {
    val coordinates = intArrayOf(0, 0)
    getLocationOnScreen(coordinates)
    return coordinates[1] + height / 2
}

fun View.getRadius(): Int {
    return height / 2
}

/**
 * Convenient method for clicks handling. Has 400mls throttle by default
 * */
@SuppressLint("CheckResult")
inline fun <T : View> T.onClick(throttle: Long = 400, crossinline action: (T) -> Unit) {
    if (throttle > 0) {
        RxView.clicks(this)
            .bindToLifecycle(this)
            .throttleFirst(throttle, TimeUnit.MILLISECONDS)
            .subscribe({ action(this) }, { it.print() })
    } else {
        this.setOnClickListener {
            action(this)
        }
    }
}

fun <T : View> T.onTouch(delayTime: Long = 0L, action: (T, MotionEvent) -> Unit, scope: CoroutineScope) {
    errorSafety {
        var touchJob: Job? = null
        setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                if (touchJob == null || touchJob!!.isCompleted) {
                    touchJob = scope.launch(Dispatchers.Main) {
                        action(this@onTouch, event)
                        delay(delayTime)
                    }
                }
            }
            true
        }
    }
}

fun <T : View> T.onLongClick(action: (T) -> Boolean) {
    this.setOnLongClickListener { action(this) }
}

fun <T : View?> T.postSelf(action: T.() -> Unit) = this?.post { errorSafety(action = { action() }) }

fun <T : View?> T.postDelayedSelf(action: T.() -> Unit, delay: Long) = this?.postDelayed({ errorSafety(action = { action() }) }, delay)

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_EDITTEXT_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun EditText.asPasswordField() {
    with(this) {
        inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        transformationMethod = PasswordTransformationMethod()
    }
}

fun EditText.clearText() = this.setText("")

fun EditText.textChanges(
    beforeTextChanged: (s: CharSequence?, start: Int, count: Int, after: Int) -> Unit = { _, _, _, _ -> },
    onTextChanged: (s: CharSequence?, start: Int, before: Int, count: Int) -> Unit = { _, _, _, _ -> },
    afterTextChanged: (view: EditText, value: String) -> Unit
): TextWatcher {
    val watcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged(this@textChanges, s?.toString() ?: "")
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            beforeTextChanged(s, start, count, after)
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            onTextChanged(s, start, before, count)
        }
    }
    addTextChangedListener(watcher)
    return watcher
}

fun EditText.asAmount() {
    textChanges { view, value ->
        val valueAsNumber = value.toIntOrNull()
        if (valueAsNumber == null || valueAsNumber == 0) {
            view.setText(1.toString())
        } else if (value.length > 1 && value.startsWith("0")) {
            view.setText(valueAsNumber.toString())
        }
    }
}

fun EditText.setTextWithoutEffects(textWatcher: TextWatcher, text: String) {
    errorSafety {
        removeTextChangedListener(textWatcher)
        setText(text)
        addTextChangedListener(textWatcher)
    }
}

fun EditText.clearTextWithoutEffects(textWatcher: TextWatcher) {
    errorSafety {
        removeTextChangedListener(textWatcher)
        clearText()
        addTextChangedListener(textWatcher)
    }
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_BOTTOM SHEET_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun BottomSheetBehavior<*>.expand() {
    this.state = BottomSheetBehavior.STATE_EXPANDED
}

fun BottomSheetBehavior<*>.collapse() {
    this.state = BottomSheetBehavior.STATE_COLLAPSED
}

fun BottomSheetBehavior<*>.isExpanded(): Boolean {
    return this.state == BottomSheetBehavior.STATE_EXPANDED
}

fun BottomSheetBehavior<*>.isHidden(): Boolean {
    return this.state == BottomSheetBehavior.STATE_HIDDEN
}

fun BottomSheetBehavior<*>.hide() {
    this.state = BottomSheetBehavior.STATE_HIDDEN
}

@IntDef(DEFAULT_ARROW_STYLE, HIDDEN_ARROW_STYLE)
annotation class BottomSheetArrowMode

const val DEFAULT_ARROW_STYLE = 0
const val HIDDEN_ARROW_STYLE = 1

//fun <T : View> BottomSheetBehavior<T>.setCallback(
//    arrowView: ImageView?,
//    @BottomSheetArrowMode arrowBehaviourStyle: Int = DEFAULT_ARROW_STYLE,
//    onDragging: () -> Unit = {},
//    onExpanded: () -> Unit = {},
//    onCollapsed: () -> Unit = {},
//    onHidden: () -> Unit = {},
//    onSlide: (bottomSheet: View, slideOffset: Float) -> Unit = { _, _ -> }
//) {
//    this.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//        @SuppressLint("SwitchIntDef")
//        override fun onStateChanged(bottomSheet: View, newState: Int) {
//            fun newArrowState(isActivated: Boolean, isHovered: Boolean) {
//                arrowView?.postSelf {
//                    this.isActivated = isActivated
//                    this.isHovered = isHovered
//                }
//            }
//            when (newState) {
//                BottomSheetBehavior.STATE_COLLAPSED -> onCollapsed().also {
//                    newArrowState(false, arrowBehaviourStyle != DEFAULT_ARROW_STYLE)
//                }
//                BottomSheetBehavior.STATE_DRAGGING -> onDragging().also {
//                    newArrowState(isActivated = true, isHovered = true)
//                }
//                BottomSheetBehavior.STATE_EXPANDED -> onExpanded().also {
//                    newArrowState(isActivated = true, isHovered = false)
//                }
//                BottomSheetBehavior.STATE_HIDDEN -> onHidden().also {
//                    newArrowState(isActivated = false, isHovered = false)
//                }
//            }
//        }
//
//        override fun onSlide(bottomSheet: View, slideOffset: Float) {
//            onSlide(bottomSheet, slideOffset)
//        }
//    })
//}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_DRAWER_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun DrawerLayout.close() {
    this.closeDrawer(GravityCompat.END)
}

fun DrawerLayout.open() {
    this.openDrawer(GravityCompat.END)
}

fun DrawerLayout.lock() {
    this.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
}

fun DrawerLayout.unlock() {
    this.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
}

val DrawerLayout.isOpen
    get() = this.isDrawerOpen(GravityCompat.END)

val DrawerLayout.isLocked
    get() = this.getDrawerLockMode(GravityCompat.END) == DrawerLayout.LOCK_MODE_LOCKED_CLOSED

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_RECYCLERVIEW_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun RecyclerView.preventAutoScrollOnDataChange() {
    //prevent auto scroll on adapter data change
    adapter?.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            if (positionStart == 0) {
                layoutManager?.scrollToPosition(0)
            }
        }
    })
}

val RecyclerView.Adapter<*>.lastItemIndex: Int get() = itemCount - 1

fun RecyclerView.hideFabOnScroll(fab: FloatingActionButton, additionalCondition: () -> Boolean = { true }) {
    addOnScrollListener(object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            if (dy > 0) {
                // Scroll Down
                if (fab.isShown) {
                    fab.hide()
                }
            } else if (dy < 0) {
                // Scroll Up
                if (!fab.isShown && additionalCondition()) {
                    fab.show()
                }
            }
        }
    })
}

/**
 * Useful is xml data binding
 * */
fun isEmpty(recyclerView: RecyclerView): Boolean = recyclerView.adapter?.let { it.itemCount == 0 } ?: true

/**
 * Returns span count rely to window size.*/
fun getSpanCount(context: Context, widthInDp: Int) = context.getAvailableSpanCount(widthInDp)

fun getFlexLayoutManager(context: Context): FlexboxLayoutManager {
    val layoutManager = FlexboxLayoutManager(context)
    layoutManager.flexDirection = FlexDirection.ROW
    layoutManager.justifyContent = JustifyContent.CENTER
    return layoutManager
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_RADIO AND CHECKBOX_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun CheckBox.invert() {
    this.isChecked = !this.isChecked
}

fun SwitchCompat.toggleIfEnabled() {
    if (isEnabled) toggle()
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_OTHER_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun ContentLoadingProgressBar.handleProgress(show: Boolean) {
    if (show) {
        show()
    } else {
        hide()
    }
}

fun ClosableSpinner.setUpDefaultSpinnerBehaviour() {
    postSelf {
        dropDownVerticalOffset = height
    }
    setSpinnerEventsListener(object : ClosableSpinner.OnSpinnerEventsListener {
        //make chevron anim
        override fun onSpinnerOpened(spin: Spinner) {
            spin.isActivated = true
        }

        override fun onSpinnerClosed(spin: Spinner) {
            spin.isActivated = false
        }

    })
}

fun Menu.hide() {
    for (i in 0 until size()) {
        getItem(i).isVisible = false
    }
}

fun Menu.show() {
    for (i in 0 until size()) {
        getItem(i).isVisible = true
    }
}