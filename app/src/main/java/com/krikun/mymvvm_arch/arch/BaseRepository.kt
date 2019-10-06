package com.krikun.mymvvm_arch.arch

import com.krikun.mymvvm_arch.helpers.connection.NetworkConnection
import com.krikun.mymvvm_arch.helpers.connection.NoInternetConnectionException
import com.krikun.mymvvm_arch.models.Result
import com.krikun.mymvvm_arch.utils.castError
import com.krikun.mymvvm_arch.utils.getString
import com.krikun.mymvvm_arch.utils.print
import com.krikun.mymvvm_arch.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.net.SocketTimeoutException

open class BaseRepository {

    /**
     * Makes request to api, handles errors and maps [Response] to [Result]
     *
     * @param request - Retrofit request
     *
     * @return [Result]
     * */
    suspend inline fun <Success : Any, reified Error : Any> processResponse(
        networkConnection: NetworkConnection,
        crossinline request: suspend () -> Response<Success>
    ): Result<Success, Error> {
        return if (networkConnection.connected) {
            return try {
                val response = withContext(Dispatchers.IO) { request() }
                return if (response.isSuccessful) {
                    Result.Success(data = response.body())
                } else {
                    Result.Error(
                            error = response.castError(),
                            exception = RuntimeException("Response error with code: ${response.code()}")
                    )
                }

            } catch (timeout: SocketTimeoutException) {
                timeout.print()
                Result.Error(null, errorMessage = getString(R.string.error_timeout), exception = timeout)

            } catch (e: Exception) {
                e.print()
                Result.Error(null, exception = e)
            }
        } else {
            Result.Error(errorMessage = getString(R.string.error_no_internet_connection), exception = NoInternetConnectionException())
        }

    }

}