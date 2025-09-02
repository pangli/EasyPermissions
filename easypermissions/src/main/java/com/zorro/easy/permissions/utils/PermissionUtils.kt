package com.zorro.easy.permissions.utils

import android.Manifest
import android.content.Context
import android.os.Build

/**
 * 根据版本和调节过滤权限
 */
fun filterSupportedPermissions(
    context: Context,
    permissions: List<String>
): List<String> {
    return permissions.filter { perm ->
        when (perm) {
            Manifest.permission.POST_NOTIFICATIONS -> Build.VERSION.SDK_INT >= 33
            AppListPermissionUtils.GET_INSTALLED_APPS -> AppListPermissionUtils.isSupportedReadAppListPermission(
                context
            )

            else -> true
        }
    }
}