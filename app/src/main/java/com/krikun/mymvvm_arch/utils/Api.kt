
package com.krikun.mymvvm_arch.utils

import com.krikun.mymvvm_arch.CommonApp
import com.krikun.mymvvm_arch.api.IOnUnauthorized
import com.krikun.mymvvm_arch.models.types.Task
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.krikun.mymvvm_arch.R
import io.reactivex.FlowableTransformer
import io.reactivex.ObservableTransformer
import io.reactivex.SingleTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.MultipartBody.Part.createFormData
import okhttp3.RequestBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.File
import java.net.SocketTimeoutException
import java.net.UnknownHostException

enum class HttpCode(val code: Int) {
    SUCCESS(200),

    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    NOT_FOUND(404),
    CONFLICT(409),
    FORCE_UPDATE(410),
    UNPROCESSABLE_ENTITY(422)
}

/**
 * Handles response for authorization errors. If we get 401 - onUnauthorized then
 * @return response itself
 * */
fun okhttp3.Response.handleAuthorizationErrors(authManager: IOnUnauthorized): okhttp3.Response {
    if (code() == HttpCode.UNAUTHORIZED.code) {
        authManager.onUnauthorized()
    }

    return this
}

inline fun <T : Any, reified ErrorType : Any> Response<T>.castError(): ErrorType? {
    val gson = Gson()
    return try {
        gson.fromJson(errorBody()?.string(), ErrorType::class.java)
    } catch (e: Exception) {
        e.print()
        null
    }
}

fun <T : Throwable> T.getErrorMessage(): String {
    val context = CommonApp.instance
    return when (this) {
        // Common
        is UnknownHostException, is HttpException -> context.getString(R.string.error_internet_connection)
        //
        else -> context.getString(R.string.error_sorry_something_went_wrong)
    }
}

fun <T> applyDefaultSchedulers(): SingleTransformer<T, T> {
    return SingleTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> applyDefaultSchedulersObservable(): ObservableTransformer<T, T> {
    return ObservableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun <T> applyDefaultSchedulersFlowable(): FlowableTransformer<T, T> {
    return FlowableTransformer { upstream ->
        upstream.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}

fun File.toMultipartImage(name: String): MultipartBody.Part = try {
    if (this.exists()) {
        createFormData(name, this.name, RequestBody.create(MediaType.parse("image/*"), this))
    } else {
        createPlugMultipart()
    }
} catch (e: Exception) {
    createPlugMultipart()
}

fun createPlugMultipart(name: String = "plug", value: String = ""): MultipartBody.Part = createFormData(name, value)

inline fun <T : Any, ErrorType : Any> safeApiCall(action: () -> Unit, liveData: MutableLiveData<Task<T, ErrorType>>) {
    try {
        action()
    } catch (timeout: SocketTimeoutException) {
        timeout.print()
        liveData.postValue(Task.Error(errorMessage = getString(R.string.error_timeout), exception = timeout))
    } catch (e: Exception) {
        e.print()
        liveData.postValue(Task.Error(errorMessage = getString(R.string.error_technical), exception = e))
    }
}
