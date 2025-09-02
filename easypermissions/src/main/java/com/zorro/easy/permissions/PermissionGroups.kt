package com.zorro.easy.permissions


/**
 * 常用权限组
 */
object PermissionGroups {
    // 系统预设分组
    val CAMERA = PermissionGroup.Companion.Camera
    val LOCATION = PermissionGroup.Companion.Location
    val PHONE = PermissionGroup.Companion.Phone
    val SMS = PermissionGroup.Companion.Sms
    val NOTIFICATIONS = PermissionGroup.Companion.Notifications
    val APPS = PermissionGroup.Companion.Apps

    // 自定义权限组（可随时扩展）
    fun custom(
        permissions: Array<String>,
        label: String
    ): PermissionGroup.Custom {
        return PermissionGroup.Custom(permissions, label)
    }
}
