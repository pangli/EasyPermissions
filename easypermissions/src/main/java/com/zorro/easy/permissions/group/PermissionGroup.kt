package com.zorro.easy.permissions.group

import android.Manifest
import androidx.annotation.StringRes
import com.zorro.easy.permissions.R
import com.zorro.easy.permissions.utils.AppListPermissionUtils

/**
 * 常用权限组
 */
sealed class PermissionGroup(val permissions: Array<String>) {
    init {
        register(this)
    }

    /** 内置权限组（单例 object） */
    open class BuiltIn(
        permissions: Array<String>,
        @param:StringRes val labelRes: Int
    ) : PermissionGroup(permissions)

    /** 自定义权限组（动态字符串 label） */
    class Custom(
        permissions: Array<String>,
        val label: String
    ) : PermissionGroup(permissions)

    companion object {
        private val _all = linkedSetOf<PermissionGroup>() // 去重 + 保持顺序
        val all: List<PermissionGroup> get() = _all.toList()

        // 权限字符串 -> PermissionGroup 快速查找 Map
        private val _permToGroup = mutableMapOf<String, PermissionGroup>()

        fun findGroupByPermission(permission: String): PermissionGroup? {
            return _permToGroup[permission]
        }

        private fun register(group: PermissionGroup) {
            if (_all.add(group)) {
                group.permissions.forEach { perm ->
                    _permToGroup[perm] = group
                }
            }
        }

        object Camera : BuiltIn(
            arrayOf(Manifest.permission.CAMERA),
            R.string.permission_camera
        )

        object Location : BuiltIn(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), R.string.permission_location
        )

        object Phone : BuiltIn(
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            R.string.permission_phone
        )

        object Sms : BuiltIn(
            arrayOf(
                Manifest.permission.READ_SMS,
            ), R.string.permission_sms
        )

        object Notifications : BuiltIn(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            R.string.permission_notifications
        )

        object Apps : BuiltIn(
            arrayOf(AppListPermissionUtils.GET_INSTALLED_APPS),
            R.string.permission_app_list
        )
    }
}