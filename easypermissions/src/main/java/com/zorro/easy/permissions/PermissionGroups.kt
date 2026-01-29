package com.zorro.easy.permissions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import com.zorro.easy.permissions.group.PermissionGroup


/**
 * 常用权限组
 */
object PermissionGroups {
    // 系统预设分组
    val CAMERA = PermissionGroup.Companion.Camera
    val LOCATION = PermissionGroup.Companion.CoarseLocation
    val PHONE = PermissionGroup.Companion.Phone
    val SMS = PermissionGroup.Companion.ReadSms

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    val NOTIFICATIONS = PermissionGroup.Companion.Notifications
    val APPS = PermissionGroup.Companion.GetInstalledApps

    /**
     * 自定义权限组（可随时扩展）
     */
    fun buildPermissionGroup(
        permissions: Array<String>,
        @StringRes labelRes: Int
    ): PermissionGroup.BuiltIn {
        return PermissionGroup.BuiltIn(permissions, labelRes)
    }

    /**
     * 自定义权限组（可随时扩展）
     */
    fun buildPermissionGroup(
        permissions: Array<String>,
        labelString: String
    ): PermissionGroup.Custom {
        return PermissionGroup.Custom(permissions, labelString)
    }
}
