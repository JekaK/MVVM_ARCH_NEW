package com.krikun.mymvvm_arch.utils

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.StringDef
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.github.florent37.runtimepermission.PermissionResult
import com.github.florent37.runtimepermission.kotlin.askPermission
import com.orhanobut.hawk.Hawk

@StringDef(PERMISSION_CAMERA, PERMISSION_STORAGE_WRITE, PERMISSION_COARSE_LOCATION, PERMISSION_FINE_LOCATION)
annotation class Permission

const val PERMISSION_CAMERA = Manifest.permission.CAMERA
const val PERMISSION_STORAGE_WRITE = Manifest.permission.WRITE_EXTERNAL_STORAGE
const val PERMISSION_COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION
const val PERMISSION_FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION

private fun isFirstTimeForeverDenied(@Permission permissions: String): Boolean = Hawk.get(permissions, true)
private fun setIsFirstTimeForeverDenied(@Permission permissions: String, isFirstTime: Boolean) =
    Hawk.put(permissions, isFirstTime)

/**
 * Use this function to make action which need a permission.
 *
 * @param goToSettingsIfDAAEnabled calls when you have at least one foreverDenied permission.
 * If you have other permissions, they must be accepted for calling goToSettingsIfDAAEnabled function.
 * return true for go to settings.
 * P.S. DAA: "Don't ask again".
 * */
fun Fragment.makeActionWithPermission(
    action: (afterPermissionAnswer: Boolean) -> Unit,
    onPermissionDenied: (deniedPermissions: List<String>, foreverDeniedPermissions: List<String>) -> Unit,
    goToSettingsIfDAAEnabled: () -> Boolean = { true },
    @Permission vararg permissions: String
) {

    fun processResult(result: PermissionResult) {
        if (result.isAccepted) {
            action(true)
        } else {
            if (result.hasForeverDenied()) {
                //handle forever denied first time call. Go to settings only if all required permission is forever denied
                if ((result.foreverDenied.size == permissions.size || !result.hasDenied())
                    && result.foreverDenied.all {
                        !isFirstTimeForeverDenied(it).apply {
                            if (this) setIsFirstTimeForeverDenied(
                                it,
                                false
                            )
                        }
                    }
                    && goToSettingsIfDAAEnabled()
                ) {
                    result.goToSettings()
                } else {
                    onPermissionDenied(
                        result.denied.onEach { setIsFirstTimeForeverDenied(it, true) },
                        result.foreverDenied
                    )
                }
            } else {
                onPermissionDenied(result.denied.onEach { setIsFirstTimeForeverDenied(it, true) }, emptyList())
            }
        }
    }

    //if all permissions is granted just make action
    var allPermissionsGranted = true
    for (permission in permissions) {
        if (context?.let {
                ContextCompat.checkSelfPermission(
                    context!!,
                    permission
                ) != PackageManager.PERMISSION_GRANTED
            } == true) {
            allPermissionsGranted = false
        } else {
            setIsFirstTimeForeverDenied(permission, true)
        }
    }
    if (allPermissionsGranted) {
        action(false)
        return
    }

    askPermission(*permissions) {
        processResult(it)
    }.onDeclined {
        processResult(it)
    }
}

/**
 * Use this function to make action which need a permission.
 *
 * @param goToSettingsIfDAAEnabled calls when you have at least one foreverDenied permission.
 * If you have other permissions, they must be accepted for calling goToSettingsIfDAAEnabled function.
 * return true for go to settings.
 * P.S. DAA: "Don't ask again".
 * */
fun FragmentActivity.makeActionWithPermission(
    action: (afterPermissionAnswer: Boolean) -> Unit,
    onPermissionDenied: (deniedPermissions: List<String>, foreverDeniedPermissions: List<String>) -> Unit,
    goToSettingsIfDAAEnabled: () -> Boolean = { true },
    @Permission vararg permissions: String
) {

    fun processResult(result: PermissionResult) {
        if (result.isAccepted) {
            action(true)
        } else {
            if (result.hasForeverDenied()) {
                //handle forever denied first time call. Go to settings only if all required permission is forever denied
                if ((result.foreverDenied.size == permissions.size || !result.hasDenied())
                    && result.foreverDenied.all { !isFirstTimeForeverDenied(it).apply { if (this) setIsFirstTimeForeverDenied(it, false) } }
                    && goToSettingsIfDAAEnabled()
                ) {
                    result.goToSettings()
                } else {
                    onPermissionDenied(
                        result.denied.onEach { setIsFirstTimeForeverDenied(it, true) },
                        result.foreverDenied
                    )
                }
            } else {
                onPermissionDenied(result.denied.onEach { setIsFirstTimeForeverDenied(it, true) }, emptyList())
            }
        }
    }

    //if all permissions is granted just make action
    var allPermissionsGranted = true
    for (permission in permissions) {
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            allPermissionsGranted = false
        } else {
            setIsFirstTimeForeverDenied(permission, true)
        }
    }
    if (allPermissionsGranted) {
        action(false)
        return
    }

    askPermission(*permissions) {
        processResult(it)
    }.onDeclined {
        processResult(it)
    }
}