package com.zorro.easy.permissions.model

/**
 * 结果封装
 */
sealed class PermissionEvent {
    data class AllGranted(val granted: List<String>) : PermissionEvent()
    data class Partial(val granted: List<String>, val denied: List<String>) : PermissionEvent()
}