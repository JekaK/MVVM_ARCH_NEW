package com.krikun.mymvvm_arch.di

import org.koin.core.KoinComponent
import org.koin.core.module.Module

interface IDIHolder : KoinComponent {

    /** Provide modules which will be provided for app scope. (All the time) */
    fun provideAppScopeModules(): List<Module>

    /**
     * Provide api config to use main api injection.
     * Auth manager needed for auto handling 401 feature. If you don't need this feature pass null.
     */
    fun provideApiConfig(): DICommon.Api.Config?
}