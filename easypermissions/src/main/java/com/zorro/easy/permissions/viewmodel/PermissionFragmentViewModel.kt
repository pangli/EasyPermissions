package com.zorro.easy.permissions.viewmodel

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.zorro.easy.permissions.model.PermissionEvent
import com.zorro.easy.permissions.model.PermissionEffect
import com.zorro.easy.permissions.model.PermissionUiState
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * 保存请求流程的关键状态，确保旋转时流程不丢失
 */
class PermissionFragmentViewModel(
    app: Application
) : AndroidViewModel(app) {

    private val _permissionUiState = MutableStateFlow(PermissionUiState())
    val permissionUiState = _permissionUiState.asStateFlow()

    // ---- Effect ---- (一次性事件，不保留 replay)
    private val _permissionEffect = MutableSharedFlow<PermissionEffect>(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effect = _permissionEffect.asSharedFlow()


    /** 发出一次性事件（保证不丢失） */
    private fun sendEffect(permissionEffect: PermissionEffect) {
        viewModelScope.launch {
            _permissionEffect.emit(permissionEffect)
        }
    }

    /**
     * 启动申请权限状态
     */
    fun start(requestKey: String, allPermissions: List<String>, requestPermissions: List<String>) {
        _permissionUiState.update {
            it.copy(
                requestKey = requestKey,
                allPermissions = allPermissions,
                requestPermissions = requestPermissions
            )
        }
    }

    /**
     * 修改是否启动了权限申请标志
     */
    fun markLaunched() {
        _permissionUiState.update {
            it.copy(
                launched = true
            )
        }
    }

    /**
     * 权限申请结果
     */
    fun onActivityResult(
        grantedList: List<String>,
        deniedList: List<String>,
        permanentlyDenied: List<String>
    ) {
        // 判定顺序在 ViewModel：但真正的 ShowSettingsDialog/Completed 由 fragment 发起或由 VM 发 effect
        if (deniedList.isEmpty()) {
            sendEffect(PermissionEffect.Completed(PermissionEvent.AllGranted(grantedList)))
        } else {
            if (permanentlyDenied.isNotEmpty() && permanentlyDenied.size == deniedList.size) {
                // 全部被永久拒绝
                _permissionUiState.update {
                    it.copy(permanentlyDenied = permanentlyDenied)
                }
                sendEffect(PermissionEffect.ShowSettingsDialog(permanentlyDenied))
            } else {
                // 部分授予（或部分永久拒绝，但不是全部都是永久拒绝），直接返回 Partial
                sendEffect(
                    PermissionEffect.Completed(
                        PermissionEvent.PartialGranted(
                            grantedList,
                            deniedList
                        )
                    )
                )
            }
        }
    }

    /**
     * 跳转到设置页
     */
    fun openedSettings(waitingSettingsReturn: Boolean) {
        _permissionUiState.update {
            it.copy(
                waitingSettingsReturn = waitingSettingsReturn
            )
        }
    }

    fun onResumeCheck() {
        // check current permission status
        if (_permissionUiState.value.waitingSettingsReturn) {
            val ctx = getApplication<Application>()
            val all = _permissionUiState.value.allPermissions
            val stillDenied = all.filter {
                ContextCompat.checkSelfPermission(ctx, it) != PackageManager.PERMISSION_GRANTED
            }
            if (stillDenied.isEmpty()) {
                sendEffect(PermissionEffect.Completed(PermissionEvent.AllGranted(all)))
                return
            }
            // if all still denied are permanently denied -> show dialog again
            // Note: fragment will compute shouldShow... because VM cannot call shouldShowRequestPermissionRationale
            sendEffect(PermissionEffect.ShowSettingsDialog(stillDenied))
        }
    }

    fun completedWith(result: PermissionEvent) {
        sendEffect(PermissionEffect.Completed(result))
    }
}