package com.zorro.easy.permissions.model

/**
 * 一次性副作用
 */
sealed class PermissionEffect : MviEffect {
    data class ShowSettingsDialog(val perms: List<String>) : PermissionEffect()
    data class Completed(val result: PermissionEvent) : PermissionEffect()
}