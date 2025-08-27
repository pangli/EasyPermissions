package com.zorro.easy.permissions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class PermissionUiState(
    val requestKey: String = "",
    val launched: Boolean = false,
    val allPermissions: List<String> = emptyList(),
    val requestPermissions: List<String> = emptyList(),
    val permanentlyDenied: List<String> = emptyList(),
    val waitingSettingsReturn: Boolean = false,
) : Parcelable

sealed class Effect {
    data class ShowSettingsDialog(val perms: List<String>) : Effect()
    data class Completed(val result: PermissionResult) : Effect()
}


