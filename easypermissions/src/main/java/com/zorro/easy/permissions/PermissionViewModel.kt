package com.zorro.easy.permissions

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 保存请求流程的关键状态，确保旋转时流程不丢失
 */
class PermissionViewModel(
    app: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(app) {

    private val _state =
        MutableStateFlow(savedStateHandle.get<State>("state") ?: State())
    val state = _state.asStateFlow()

    private val _effect = MutableSharedFlow<Effect>(replay = 0)
    val effect = _effect.asSharedFlow()

    private val s = savedStateHandle

    private fun setState(state: State) {
        _state.value = state
        s["state"] = state
    }

    fun start(requestKey: String, perms: List<String>, toRequest: List<String>) {
        setState(
            State(
                requestKey = requestKey,
                allPermissions = perms,
                toRequest = toRequest
            )
        )
        _effect.tryEmit(Effect.LaunchSystemRequest)
    }

    fun markLaunched() {
        val cur = _state.value
        setState(cur.copy(launched = true))
    }

    fun onSystemResult(
        grantedList: List<String>,
        deniedList: List<String>,
        permanentlyDenied: List<String>
    ) {
        // 判定顺序在 ViewModel：但真正的 ShowSettingsDialog/Completed 由 fragment 发起或由 VM 发 effect
        if (deniedList.isEmpty()) {
            _effect.tryEmit(Effect.Completed(PermissionResult.Success(grantedList)))
        } else {
            if (permanentlyDenied.isNotEmpty() && permanentlyDenied.size == deniedList.size) {
                // 全部被永久拒绝
                setState(_state.value.copy(permanentlyDenied = permanentlyDenied))
                _effect.tryEmit(Effect.ShowSettingsDialog(permanentlyDenied))
            } else {
                // 部分授予（或部分永久拒绝，但不是全部都是永久拒绝），直接返回 Partial
                _effect.tryEmit(
                    Effect.Completed(
                        PermissionResult.Partial(
                            grantedList,
                            deniedList
                        )
                    )
                )
            }
        }
    }

    fun openedSettings() {
        val cur = _state.value
        setState(cur.copy(waitingSettingsReturn = true))
    }

    fun onResumeCheck() {
        // check current permission status
        val ctx = getApplication<Application>()
        val all = _state.value.allPermissions
        val stillDenied = all.filter {
            ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
        }
        if (stillDenied.isEmpty()) {
            _effect.tryEmit(Effect.Completed(PermissionResult.Success(all)))
            return
        }
        // if all still denied are permanently denied -> show dialog again
        // Note: fragment will compute shouldShow... because VM cannot call shouldShowRequestPermissionRationale
        _effect.tryEmit(Effect.ShowSettingsDialog(stillDenied))
    }

    fun completedWith(result: PermissionResult) {
        _effect.tryEmit(Effect.Completed(result))
    }
}
