package com.zorro.easy.permissions

/**
 * 结果封装
 */
sealed class PermissionResult {
    data class AllGranted(val granted: List<String>) : PermissionResult()
    data class Partial(val granted: List<String>, val denied: List<String>) : PermissionResult()
}
