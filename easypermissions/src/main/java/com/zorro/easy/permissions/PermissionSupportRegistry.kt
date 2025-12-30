package com.zorro.easy.permissions

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.zorro.easy.permissions.utils.AppListPermissionUtils

typealias PermissionSupportChecker = (Context) -> Boolean

/**
 * 权限过滤规则
 */
object PermissionSupportRegistry {
    private val checkerMap = mutableMapOf<String, PermissionSupportChecker>()

    init {
        // 默认规则
        @SuppressLint("InlinedApi")
        checkerMap[Manifest.permission.POST_NOTIFICATIONS] = { Build.VERSION.SDK_INT >= 33 }
        checkerMap[AppListPermissionUtils.GET_INSTALLED_APPS] = { context ->
            AppListPermissionUtils.isSupportedReadAppListPermission(context)
        }
    }

    /**
     * 注册权限过滤规则
     * @param permission String
     * @param checker PermissionSupportChecker
     */
    fun registerChecker(permission: String, checker: PermissionSupportChecker) {
        checkerMap[permission] = checker
    }

    /**
     * 注册权限过滤规则
     * @param map Map<String, PermissionSupportChecker>
     */
    fun registerChecker(map: Map<String, PermissionSupportChecker>) {
        checkerMap.putAll(map)
    }

    /** 判断权限是否支持 */
    fun isSupported(context: Context, permission: String): Boolean {
        // 如果 Map 中没有对应规则，默认返回 true
        return checkerMap[permission]?.invoke(context) ?: true
    }

    /** 批量过滤 */
    fun filterSupportedPermissions(
        context: Context,
        permissions: Collection<String>
    ): List<String> {
        return permissions.filter { isSupported(context, it) }
    }
}
