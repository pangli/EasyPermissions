package com.zorro.easy.permissions

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class State(
    val requestKey: String = "",
    val allPermissions: List<String> = emptyList(),
    val toRequest: List<String> = emptyList(),
    val launched: Boolean = false,
    val waitingSettingsReturn: Boolean = false,
    val permanentlyDenied: List<String> = emptyList()
) : Parcelable

sealed class Effect {
    object LaunchSystemRequest : Effect()
    data class ShowSettingsDialog(val perms: List<String>) : Effect()
    data class Completed(val result: PermissionResult) : Effect()
}


