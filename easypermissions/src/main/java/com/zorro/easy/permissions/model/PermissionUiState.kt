package com.zorro.easy.permissions.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * UI 渲染所需的完整状态
 */
@Parcelize
data class PermissionUiState(
    val requestKey: String = "",
    val launched: Boolean = false,
    val allPermissions: List<String> = emptyList(),
    val requestPermissions: List<String> = emptyList(),
    val permanentlyDenied: List<String> = emptyList(),
    val waitingSettingsReturn: Boolean = false,
) : MviState, Parcelable