
package com.krikun.mymvvm_arch.binding

import android.animation.LayoutTransition
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.method.LinkMovementMethod
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.HtmlCompat
import androidx.databinding.BindingAdapter
import androidx.databinding.BindingConversion
import androidx.databinding.InverseBindingAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.RequestOptions.bitmapTransform
import com.krikun.mymvvm_arch.utils.*
import com.krikun.mymvvm_arch.widgets.ClosableSpinner
import jp.wasabeef.glide.transformations.GrayscaleTransformation


/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_COMMON_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

@BindingAdapter("isGone")
fun bindIsGone(view: View, isGone: Boolean?) {
    if (isGone == null || isGone) {
        view.gone()
    } else {
        view.visible()
    }
}

@BindingAdapter("isVisible")
fun bindIsVisible(view: View, isVisible: Boolean?) {
    if (isVisible == null || !isVisible) {
        view.gone()
    } else {
        view.visible()
    }
}

@BindingAdapter("isVisibleSoft")
fun bindIsVisibleSoft(view: View, isVisible: Boolean?) {
    if (isVisible == null || !isVisible) {
        view.invisible()
    } else {
        view.visible()
    }
}

@BindingAdapter("isActivated")
fun bindIsActivated(view: View, isActivated: Boolean?) {
    view.isActivated = !(isActivated == null || !isActivated)
}

@BindingAdapter("isEnabled")
fun bindIsEnabled(view: View, isEnabled: Boolean?) {
    view.isActivated = !(isEnabled == null || !isEnabled)
}

@BindingConversion
fun convertColorToDrawable(color: Int) = ColorDrawable(color)

@BindingAdapter("layout_marginBottom")
fun setBottomMargin(view: View, bottomMargin: Int) {
    val layoutParams = view.layoutParams as MarginLayoutParams
    layoutParams.setMargins(layoutParams.leftMargin, layoutParams.topMargin, layoutParams.rightMargin, bottomMargin.dp)
    view.layoutParams = layoutParams
}

@BindingAdapter("animatedAlpha")
fun setAnimatedAlpha(v: View, alpha: Float) {
    v.animate().alpha(alpha)
}

@BindingAdapter("animatedTranslationX")
fun setAnimatedTranslationX(v: View, x: Int) {
    v.animate().translationX(x.dp.toFloat())
}

@BindingAdapter(value = ["android:layout_marginStart", "android:layout_marginTop", "android:layout_marginEnd", "android:layout_marginBottom"], requireAll = false)
fun setMargins(
    view: View,
    layout_marginStart: Number? = null,
    layout_marginTop: Number? = null,
    layout_margin_End: Number? = null,
    layout_margin_Bottom: Number? = null
) {
    val lp = view.layoutParams as MarginLayoutParams
    lp.setMargins(
        layout_marginStart?.toInt()?.dp ?: lp.leftMargin,
        layout_marginTop?.toInt()?.dp ?: lp.topMargin,
        layout_margin_End?.toInt()?.dp ?: lp.rightMargin,
        layout_margin_Bottom?.toInt()?.dp ?: lp.bottomMargin
    )
    view.layoutParams = lp
}

@BindingAdapter(value = ["layout_marginStartPx", "layout_marginTopPx", "layout_marginEndPx", "layout_marginBottomPx"], requireAll = false)
fun setMarginsPx(
    view: View,
    layout_marginStartPx: Number? = null,
    layout_marginTopPx: Number? = null,
    layout_marginEndPx: Number? = null,
    layout_marginBottomPx: Number? = null
) {
    val lp = view.layoutParams as MarginLayoutParams
    lp.setMargins(
        layout_marginStartPx?.toInt() ?: lp.leftMargin,
        layout_marginTopPx?.toInt() ?: lp.topMargin,
        layout_marginEndPx?.toInt() ?: lp.rightMargin,
        layout_marginBottomPx?.toInt() ?: lp.bottomMargin
    )
    view.layoutParams = lp
}

@BindingAdapter("animateWithDurationIn")
fun setTransitionAnimation(v: ViewGroup, duration: Int) {
    v.layoutTransition = LayoutTransition().apply { setDuration(duration.toLong()) }
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_EDITTEXT_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

@BindingAdapter(value = ["text", "format"], requireAll = false)
fun <T : Any> setTextFirstAppend(editText: EditText, text: T?, format: String?) {
    text ?: return

    val stringToSet = try {
        format?.format(LOCALE_DEFAULT, text) ?: text.toString()
    } catch (e: Exception) {
        e.print()
        text.toString()
    }

    if (editText.text.isBlank()) {
        editText.append(stringToSet)
    } else if (editText.value(false) != text) {
        editText.setText(stringToSet)
    }
}

@BindingAdapter("hint")
fun setEditTextHint(editText: EditText, @StringRes stringRes: Int) {
    editText.hint = editText.context.getString(stringRes)
}

@InverseBindingAdapter(attribute = "text", event = "android:textAttrChanged")
fun getTextFirstAppend(editText: EditText): String = editText.value()

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_TEXTVIEW_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

@BindingAdapter("renderHtml")
fun bindRenderHtml(view: TextView, description: String?) {
    if (description != null) {
        view.text = HtmlCompat.fromHtml(description, HtmlCompat.FROM_HTML_MODE_COMPACT)
        view.movementMethod = LinkMovementMethod.getInstance()
    } else {
        view.text = ""
    }
}

@BindingAdapter("textStyle")
fun setTypeface(v: TextView, style: String) {
    v.setTypeface(
        null, when (style) {
            "bold" -> Typeface.BOLD
            "italic" -> Typeface.ITALIC
            else -> Typeface.NORMAL
        }
    )
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_IMAGEVIEW_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

@BindingAdapter(value = ["imageUrl", "placeholder", "error", "colorless", "imageOnPost"], requireAll = false)
fun bindImageFromUrl(
    view: ImageView,
    imageUrl: String?,
    placeholder: Drawable?,
    error: Drawable?,
    colorless: Boolean? = false,
    imageOnPost: Boolean? = true
) {

    fun action() {
        Glide.with(view.context)
            .load(imageUrl)
            .apply(RequestOptions().apply {
                placeholder?.let { this.placeholder(it) }
                error?.let { this.error(it) }
                colorless?.takeIf { it }?.let {
                    this.apply(bitmapTransform(GrayscaleTransformation()))
                }
            })
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(view)
    }

    if (imageOnPost == true) {
        view.postSelf { action() }
    } else {
        action()
    }
}
/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_RECYCLERVIEW_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

@BindingAdapter("spanCount")
fun setSpanCount(recyclerView: RecyclerView, count: Int) {
    (recyclerView.layoutManager as? GridLayoutManager)?.spanCount = count
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_SPINNER_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

@BindingAdapter("adapterCustom")
fun setSpinnerAdapter(spinner: ClosableSpinner, arrayAdapter: ArrayAdapter<CharSequence>) {
    with(spinner) {
        this.adapter = arrayAdapter
        setUpDefaultSpinnerBehaviour()
    }
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_VIEWPAGER_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

@BindingAdapter("selectedItemPosition")
fun setSelectedItemPosition(v: ViewPager, pos: Int) {
    v.setCurrentItem(pos, true)
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_CONSTRAINT_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

/** View must be child of [ConstraintLayout] */
@BindingAdapter("ratio")
fun setViewRation(v: View, ratio: Float) {
    (v.layoutParams as? ConstraintLayout.LayoutParams)?.dimensionRatio = ratio.toString()
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_SWIPEREFRESH_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

@BindingAdapter("isProgress")
fun toggleSwipeRefreshProgress(v: SwipeRefreshLayout, isProgress: Boolean?) {
    v.isRefreshing = isProgress == true
}

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_CONVERSIONS_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

//@BindingConversion
//fun nullToFloat(float: Float?): Float = 0f

@BindingConversion
fun intToFloat(int: Int?): Float = int?.toFloat() ?: 0f