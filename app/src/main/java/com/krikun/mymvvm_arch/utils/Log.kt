package com.krikun.mymvvm_arch.utils

import android.util.Log

fun Throwable.print() {
    errorSafety(onError = {}) {
        Log.e("Exception(C)", (this as? Exception)?.toString() ?: this.message)
        printStackTrace()
    }
}

fun Any.logError(message: String) {
    Log.e(this.javaClass.canonicalName, message)
}

fun Any.logError(message: String, title: String) {
    Log.e(this.javaClass.canonicalName, "$title: $message")
}

fun Any.logInfo(message: String) {
    Log.i(this.javaClass.canonicalName, message)
}

fun Any.logDebug(message: String) {
    Log.d(this.javaClass.canonicalName, message)
}