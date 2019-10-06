package com.krikun.mymvvm_arch

import android.app.Application
import com.krikun.mymvvm_arch.di.DICommon
import com.krikun.mymvvm_arch.di.IDIHolder
import com.orhanobut.hawk.Hawk

//import com.squareup.leakcanary.LeakCanary

abstract class CommonApp : Application() {

    init {
        instance = this.provideApp()
    }

    override fun onCreate() {
        super.onCreate()
/*        // LeakCanary
        if (com.squareup.leakcanary.LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return
        }
        com.squareup.leakcanary.LeakCanary.install(this)*/
        // Hawk
        Hawk.init(this).build()
        // DI
        DICommon.init(this, provideDi())
    }

    abstract fun provideApp(): Application

    abstract fun provideDi(): IDIHolder

    companion object {
        lateinit var instance: Application

        val context get() = instance
    }

}