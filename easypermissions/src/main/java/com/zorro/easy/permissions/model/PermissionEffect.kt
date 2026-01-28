package com.zorro.easy.permissions.model

/**
 * 一次性副作用
 */
sealed class PermissionEffect : MviEffect {
    data class ShowSettingsDialog(
        val grantedList: List<String>,
        val deniedList: List<String>,
        val permanentlyDenied: List<String>
    ) : PermissionEffect()

    data class Completed(val result: PermissionEvent) : PermissionEffect()
}