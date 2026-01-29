package com.zorro.easy.permissions.group

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
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

    /**
     * 构建权限组
     */
    open class BuiltIn(
        permissions: Array<String>,
        @param:StringRes val labelRes: Int
    ) : PermissionGroup(permissions)

    /**
     * 构建权限组
     */
    class Custom(
        permissions: Array<String>,
        val label: String
    ) : PermissionGroup(permissions)

    companion object {
        /**
         * 去重 + 保持顺序
         */
        private val _all = linkedSetOf<PermissionGroup>()

        /**
         * 权限字符串 -> PermissionGroup 快速查找 Map
         */
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

        /**
         * 相机
         */
        object Camera : BuiltIn(
            arrayOf(Manifest.permission.CAMERA),
            R.string.permission_camera
        )

        /**
         * 粗略位置
         */
        object CoarseLocation : BuiltIn(
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION
            ), R.string.permission_location
        )

        /**
         * 手机状态
         */
        object Phone : BuiltIn(
            arrayOf(Manifest.permission.READ_PHONE_STATE),
            R.string.permission_phone
        )

        /**
         * 读取短信
         */
        object ReadSms : BuiltIn(
            arrayOf(
                Manifest.permission.READ_SMS,
            ), R.string.permission_sms
        )

        /**
         * 推送
         */
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        object Notifications : BuiltIn(
            arrayOf(Manifest.permission.POST_NOTIFICATIONS),
            R.string.permission_notifications
        )

        /**
         * 工信部AppList
         */
        object GetInstalledApps : BuiltIn(
            arrayOf(AppListPermissionUtils.GET_INSTALLED_APPS),
            R.string.permission_app_list
        )
    }
}