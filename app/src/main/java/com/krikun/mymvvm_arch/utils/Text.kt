
package com.krikun.mymvvm_arch.utils

import android.os.Build
import com.krikun.mymvvm_arch.CommonApp
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.krikun.mymvvm_arch.R
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

fun getString(@StringRes string: Int): String = CommonApp.instance.getString(string)

fun getString(@StringRes string: Int, vararg params: Any): String = CommonApp.instance.getString(string, *params)

fun String.notEmptyOr(anotherString: String) = if (this.isNotBlank()) this else anotherString

fun String.notEmptyOr(@StringRes stringRes: Int) = if (this.isNotBlank()) this else getString(stringRes)

fun EditText.value(trim: Boolean = true) = this.text.toString().let { if (trim) it.trim() else it }

val EditText.value: String
    get() = value()

fun CharSequence?.isNotBlankAndNull(): Boolean = this?.isNotBlank() ?: false

fun CharSequence?.isBlankOrNull(): Boolean = this?.isBlank() ?: true

fun String.parseHtml(): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(this)
    }
}

fun String.addDescription(text: String, @ColorRes color: Int = R.color.textSecondary): Spanned {
    val html = String.format(
        LOCALE_DEFAULT,
        "%s <font color='#%s'>%s</font>",
        this, Integer.toHexString(ContextCompat.getColor(CommonApp.instance, color) and 0x00ffffff), text
    )
    return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)
    })
}

fun String.colorize(@ColorRes color: Int = R.color.textSecondary): Spanned {
    val html = String.format(
        LOCALE_DEFAULT,
        "<font color='#%s'>%s</font>",
        Integer.toHexString(ContextCompat.getColor(CommonApp.instance, color) and 0x00ffffff), this
    )
    return (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(html)
    })
}

fun String.colorizePart(partToColorize: String, @ColorRes color: Int = R.color.textSecondary): Spannable {
    return try {
        SpannableString(this)
            .apply {
                val startIndex = this.indexOf(partToColorize, 0, true)
                val endIndex = startIndex + partToColorize.length
                if (startIndex != -1 && endIndex != -1) {
                    setSpan(ForegroundColorSpan(getColor(color)), startIndex, endIndex, Spanned.SPAN_INTERMEDIATE)
                }

            }
    } catch (e: Exception) {
        e.print()
        SpannableString("")
    }
}


fun Int.addDescription(text: String) = this.toString().addDescription(text)

fun TextView.textWithClickableLinks(textWithLinks: String, vararg linksWithAction: Pair<String, (v: View) -> Unit>) {
    errorSafety {
        with(this) {
            val spannableString = SpannableString(textWithLinks)
            for ((link, click) in linksWithAction) {
                val clickableSpan = clickableSpanFrom(click)

                val startIndexOfLink = textWithLinks.indexOf(link)
                spannableString.setSpan(
                    clickableSpan,
                    startIndexOfLink,
                    startIndexOfLink + link.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            //highlightColor = ContextCompat.getColor(context, R.color.colorPrimary)
            movementMethod = LinkMovementMethod.getInstance()
            setText(spannableString, TextView.BufferType.SPANNABLE)
        }
    }

}

fun clickableSpanFrom(onClick: (widget: View) -> Unit): ClickableSpan {
    return object : ClickableSpan() {
        override fun onClick(widget: View) {
            onClick(widget)
        }

        override fun updateDrawState(ds: TextPaint) {
            super.updateDrawState(ds)
            ds.isUnderlineText = false
        }
    }
}

@Throws(java.security.NoSuchAlgorithmException::class)
fun String?.md5(): String {
    this ?: throw NoSuchAlgorithmException()
    val md = MessageDigest.getInstance("MD5")
    val digested = md.digest(toByteArray())
    return digested.joinToString("") {
        String.format("%02x", it)
    }
}