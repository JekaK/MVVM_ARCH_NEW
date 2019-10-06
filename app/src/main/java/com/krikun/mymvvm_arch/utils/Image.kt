package com.krikun.mymvvm_arch.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.PorterDuff
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import com.krikun.mymvvm_arch.CommonApp
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.krikun.mymvvm_arch.R

const val IMAGE_NAME_PREFIX = "tmp"
const val IMAGE_NAME_SUFFIX = ".jpeg"

fun Drawable.toBitmap(): Bitmap {
    val bitmap: Bitmap = if (this.intrinsicWidth <= 0 || this.intrinsicHeight <= 0) {
        Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888) // Single color bitmap will be created of 1x1 pixel
    } else {
        Bitmap.createBitmap(this.intrinsicWidth, this.intrinsicHeight, Bitmap.Config.ARGB_8888)
    }

    if (this is BitmapDrawable) {
        if (this.bitmap != null) {
            return this.bitmap
        }
    }

    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}

fun ImageView.loadImage(
    uri: Uri,
    @DrawableRes placeholder: Int? = null,
    onPost: Boolean = true
) {
    fun action() {
        Glide.with(this).setDefaultRequestOptions(RequestOptions().apply { placeholder?.let { placeholder(it) } })
            .load(uri)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(this)
    }
    if (onPost) {
        this.postSelf { action() }
    } else {
        action()
    }
}

fun ImageView.loadImage(
    uri: Uri,
    @DrawableRes placeholder: Int? = null,
    @DrawableRes fallback: Int? = null,
    onPost: Boolean = true,
    fit: Boolean = true
) {
    fun action() {
        Glide.with(this).setDefaultRequestOptions(RequestOptions().apply {
            placeholder?.let { placeholder(it) }
            fallback?.let {
                if (fit) {
                    this@loadImage.scaleType = ImageView.ScaleType.FIT_CENTER
                }

                fallback(it); error(it)
            }
        }).load(uri).into(this)
    }
    if (onPost) {
        this.postSelf { action() }
    } else {
        action()
    }
}

fun ImageView.loadImageWithProgress(
    uri: Uri, @DrawableRes fallback: Int,
    onPost: Boolean = true,
    fitOnError: Boolean = true,
    hideOnFail: Boolean = false
) {
    fun action() {
        Glide.with(this).setDefaultRequestOptions(RequestOptions().apply {
            context?.let {
                val circularProgressDrawable = CircularProgressDrawable(it)
                circularProgressDrawable.setColorFilter(
                    ContextCompat.getColor(it, R.color.colorAccent),
                    PorterDuff.Mode.SRC_ATOP
                )
                circularProgressDrawable.start()
                placeholder(circularProgressDrawable)
            }
            fallback.let {
                if (fitOnError) {
                    this@loadImageWithProgress.scaleType = ImageView.ScaleType.FIT_CENTER
                }

                fallback(it); error(it)
            }
        }).load(uri)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    return if (hideOnFail) {
                        this@loadImageWithProgress.gone()
                        false
                    } else {
                        true
                    }
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean = true
            })
            .into(this)
    }
    if (onPost) {
        this.postSelf { action() }
    } else {
        action()
    }
}

fun ImageView.loadImageWithFallback(
    uri: Uri,
    fallbackTextView: TextView,
    fallbackText: String?,
    parentView: View? = null, @DrawableRes placeholder: Int? = null,
    onPost: Boolean = true
) {
    fun action() {
        Glide.with(this).setDefaultRequestOptions(RequestOptions().apply {
            placeholder?.let { placeholder(it) }
        }).asBitmap().load(uri).listener(object : RequestListener<Bitmap> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any,
                target: Target<Bitmap>,
                isFirstResource: Boolean
            ): Boolean {
                fallbackTextView.setBackgroundColor(ContextCompat.getColor(CommonApp.instance, R.color.colorPrimary))
                fallbackTextView.text = fallbackText?.toUpperCase()
                fallbackTextView.setTextColor(ContextCompat.getColor(CommonApp.instance, android.R.color.white))
                return true
            }

            override fun onResourceReady(
                resource: Bitmap,
                model: Any,
                target: Target<Bitmap>,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                fallbackTextView.setBackgroundColor(resource.getPixel(0, 0))
                this@loadImageWithFallback.setImageBitmap(resource)
                parentView?.requestLayout()
                return true
            }
        }).into(this)
    }
    if (onPost) {
        this.postSelf { action() }
    } else {
        action()
    }
}

fun ImageView.cancelLoad(onPost: Boolean = true) {
    fun action() = Glide.with(this).clear(this)

    if (onPost) {
        this.postSelf { action() }
    } else {
        action()
    }
}


enum class DrawableSide {
    START,
    TOP,
    END,
    BOTTOM
}

fun TextView.setDrawable(@DrawableRes drawable: Int, side: DrawableSide) {
    when (side) {
        DrawableSide.START -> this.setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, 0, 0, 0)
        DrawableSide.TOP -> this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, drawable, 0, 0)
        DrawableSide.END -> this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawable, 0)
        DrawableSide.BOTTOM -> this.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, 0, drawable)
    }
}

fun View.toBitmap(): Bitmap {
    val specWidth = View.MeasureSpec.makeMeasureSpec(0 /* any */, View.MeasureSpec.UNSPECIFIED)
    measure(specWidth, specWidth)
    val b = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
    val c = Canvas(b)
    layout(0, 0, measuredWidth, measuredHeight)
    draw(c)
    return b
}
