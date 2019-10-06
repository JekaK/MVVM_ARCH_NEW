
package com.krikun.mymvvm_arch.utils

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.*
import org.jetbrains.anko.toast


fun <T : Any> CoroutineScope.ioThenMain(
    work: suspend () -> T?,
    onSuccess: (T?) -> Unit,
    onError: (e: Exception) -> Unit = { it.print() },
    finally: () -> Unit = {}
): Job =
    launch(Dispatchers.Main) {
        try {
            //try to do deferred work
            val data = async(Dispatchers.IO) work@{
                return@work work()
            }.await()

            //perform deferred onSuccess with data if everything is ok
            onSuccess(data)

        } catch (e: Exception) {
            //perform error block if error occurred
            onError(e)
        } finally {
            finally()
        }
    }

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_UI_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

fun Context?.createDeferredToast(scope: CoroutineScope?, message: String): Deferred<Toast>? {
    return this?.let { scope?.async(start = CoroutineStart.LAZY) { toast(message) } }
}

