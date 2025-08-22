package com.zorro.easy.permissions

import android.Manifest
import androidx.annotation.StringRes

enum class PermissionGroup(val permissions: Array<String>, @StringRes val labelRes: Int) {
    CAMERA(
        arrayOf(Manifest.permission.CAMERA),
        R.string.permission_camera
    ),
    LOCATION(
        arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION
        ), R.string.permission_location
    ),
    PHONE(
        arrayOf(Manifest.permission.READ_PHONE_STATE),
        R.string.permission_phone
    ),
    SMS(
        arrayOf(
            Manifest.permission.READ_SMS,
        ), R.string.permission_sms
    ),
    NOTIFICATIONS(
        arrayOf(Manifest.permission.POST_NOTIFICATIONS),
        R.string.permission_notifications
    ),
    APPS(
        arrayOf(AppListPermissionUtils.GET_INSTALLED_APPS),
        R.string.permission_app_list
    )
}
