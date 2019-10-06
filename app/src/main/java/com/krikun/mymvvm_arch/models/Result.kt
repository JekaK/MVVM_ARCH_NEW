package com.krikun.mymvvm_arch.models

import com.krikun.mymvvm_arch.utils.getString
import com.krikun.mymvvm_arch.R

typealias OperationResult<T, R> = Result<T, R>

sealed class Result<out SuccessType : Any, out ErrorType : Any> {

    data class Success<SuccessType : Any>(var data: SuccessType?) : Result<SuccessType, Nothing>()

    data class Error<out ErrorType : Any>(
        val error: ErrorType? = null,
        var errorMessage: String = getString(R.string.error_technical),
        val exception: Exception? = null
    ) : Result<Nothing, ErrorType>()


    fun <T: Any, R: Any> map(transformer: (res: Result<SuccessType, ErrorType>) -> Result<T, R>): Result<T, R> = transformer(this)

    /* Convenient methods for get status */

    val isSuccess: Boolean get() = this is Success

    val isError: Boolean get() = this is Error

}
