/*
 * Created in Sannacode by Nazar Kokhan.
 * Copyright (c) 2019.
 */

package com.krikun.mymvvm_arch.models.types

import com.krikun.mymvvm_arch.utils.getString
import com.krikun.mymvvm_arch.R

sealed class Task<out SuccessType : Any, out ErrorType : Any> {

    object Idle : Task<Nothing, Nothing>()

    object Loading : Task<Nothing, Nothing>()

    data class Success<SuccessType : Any>(var data: SuccessType?) : Task<SuccessType, Nothing>() {
        /**
         * Returns the content and prevents its use again.
         */
        fun getDataIfNotHandled(): SuccessType? {
            return if (hasBeenHandled) {
                null
            } else {
                hasBeenHandled = true
                data
            }
        }

        /**
         * Returns the content, even if it's already been handled.
         */
        fun peekData(): SuccessType? = data
    }

    data class Error<out ErrorType : Any>(
        val error: ErrorType? = null,
        var errorMessage: String = getString(R.string.error_technical),
        val exception: Exception? = null
    ) : Task<Nothing, ErrorType>()

    //

    var hasBeenHandled = false
        protected set // Allow external read but not write


    /** Provides only not handled task. */
    fun consume(): Task<SuccessType, ErrorType>? {
        return if (!hasBeenHandled) {
            this
        } else {
            null
        }
    }

    /**
     * Marks the object has been handled already
     * */
    fun onUse() {
        hasBeenHandled = true
    }

    /**
     * Body block will be run only if task has not be handled yet.
     * After execution task will be marked as handled.
     * */
    inline fun useOnce(body: Task<SuccessType, ErrorType>.() -> Unit) {
        if (!hasBeenHandled) {
            body()
            onUse()
        }
    }

    /* Convenient methods for get status */

    val isSuccess: Boolean get() = this is Success

    val isError: Boolean get() = this is Error

    val isLoading: Boolean get() = this is Loading

}

