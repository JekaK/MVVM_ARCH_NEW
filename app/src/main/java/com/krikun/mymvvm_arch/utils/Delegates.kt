package com.krikun.mymvvm_arch.utils

import com.orhanobut.hawk.Hawk
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_WEAK REFERENCE_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

class WeakRef<T>(obj: T? = null) : ReadWriteProperty<Any?, T?> {

    private var wref: WeakReference<T>?

    init {
        this.wref = obj?.let { WeakReference(it) }
    }

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return wref?.get()
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        wref = value?.let { WeakReference(it) }
    }
}

fun <T> weak(obj: T? = null) = WeakRef(obj)

inline fun <T, R> T.weakable(body: WeakReference<T>.() -> R): R = WeakReference(this).body()
inline fun <T, R> WeakReference<T>.ifSelfDefined(body: T.() -> R) = get()?.run(body)

/*_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_HAWK_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_-_*/

class HawkDelegate<T>(
    private val key: String,
    private val value: T,
    private val onChange: (newValue: T) -> Unit = {}
) : ReadWriteProperty<Any?, T> {

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        return Hawk.get<T>(key, value)
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        onChange(value)
        Hawk.put(key, value)
    }
}

/**
 * Useful delegate for work with Hawk.
 * Use example:
 * private var user: User by pref("user", User.Empty)
 * ...
 * // Read from pref. Just use property
 * user
 * // Write to pref
 * user = User(name = "Vlad", age = 3)
 *
 * @param key - Key alias. Must be unique.
 * @param value - Default value in case storage is empty.
 *
 *
 * */
fun <T> pref(key: String, value: T, onChange: (newValue: T) -> Unit = {}) = HawkDelegate(key, value, onChange)


