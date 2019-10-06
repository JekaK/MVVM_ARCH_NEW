package com.krikun.mymvvm_arch.managers

import android.content.Context
import com.krikun.mymvvm_arch.helpers.livedata.Event
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import io.reactivex.subjects.PublishSubject

/** Holder for messages handling. e.g. Can be used to emit message from not lifecycle aware components ([ViewModel], Repository, etc.)
 * Use as singleton via Koin DI.
 * */
class MessageManager(val context: Context) {

    /*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_LiveData approach_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

    private val _messageLiveData: MutableLiveData<String> = MutableLiveData()
    val messageLiveDataEvent: LiveData<Event<String>> = _messageLiveData.map { Event(it) }

    /*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_RX approach_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

    private val _messageSubject: PublishSubject<String> = PublishSubject.create()
    val messageObservable = _messageSubject.hide()

    fun emitMessage(message: String) {
        _messageLiveData.postValue(message)
        _messageSubject.onNext(message)
    }

    fun emitMessage(@StringRes messageStringId: Int) = emitMessage(context.getString(messageStringId))
}