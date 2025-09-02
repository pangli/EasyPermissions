package com.zorro.easy.permissions.utils

import android.content.Context

/**
 * 工信部应用列表权限检查工具类
 */
object AppListPermissionUtils {
    const val GET_INSTALLED_APPS = "com.android.permission.GET_INSTALLED_APPS"

    /**
     * 工信部应用列表权限检查
     * 检查是否支持应用列表权限
     * com.android.permission.GET_INSTALLED_APPS
     * 权限是否已经在系统权限中注册
     */
    fun isSupportedReadAppListPermission(context: Context): Boolean {
        val permissionInfo = try {
            context.packageManager.getPermissionInfo(
                GET_INSTALLED_APPS,
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
        return permissionInfo != null
    }


}