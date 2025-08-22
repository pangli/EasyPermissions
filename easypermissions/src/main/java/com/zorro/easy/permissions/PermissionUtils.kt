package com.zorro.easy.permissions

import android.Manifest
import android.content.Context
import android.os.Build

fun filterSupportedPermissions(
    context: Context,
    permissions: Array<String>
): Array<String> {
    return permissions.filter { perm ->
        when (perm) {
            Manifest.permission.POST_NOTIFICATIONS -> Build.VERSION.SDK_INT >= 33
            AppListPermissionUtils.GET_INSTALLED_APPS -> AppListPermissionUtils.isSupportedReadAppListPermission(
                context
            )

            else -> true
        }
    }.toTypedArray()
}