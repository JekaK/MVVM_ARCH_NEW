package com.krikun.mymvvm_arch.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent

import androidx.databinding.BindingAdapter
import androidx.viewpager.widget.ViewPager

class CustomViewPager : ViewPager {

    private var isPagingEnabled = false

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return this.isPagingEnabled && super.onTouchEvent(event)
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event)
    }

    fun setPagingEnabled(b: Boolean) {
        this.isPagingEnabled = b
    }
}

@BindingAdapter("enablePaging")
fun setEnablePaging(v: CustomViewPager, enable: Boolean) {
    v.setPagingEnabled(enable)
}