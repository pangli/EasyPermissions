package com.zorro.easy.permissions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zorro.easy.permissions.PermissionRequester
import com.zorro.easy.permissions.model.PermissionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

open class PermissionViewModel : ViewModel() {
    private val _permissionResult = MutableSharedFlow<PermissionEvent>(replay = 1)
    val permissionResult: SharedFlow<PermissionEvent> = _permissionResult

    fun request(requester: PermissionRequester) {
        requester.request { res ->
            viewModelScope.launch {
                _permissionResult.emit(res)
            }
        }
    }
}
