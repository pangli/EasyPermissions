package com.zorro.easy.permissions.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zorro.easy.permissions.PermissionRequester
import com.zorro.easy.permissions.model.PermissionEvent
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class PermissionViewModel : ViewModel() {
    private val _result = MutableSharedFlow<PermissionEvent>(replay = 1)
    val result: SharedFlow<PermissionEvent> = _result

    fun request(requester: PermissionRequester) {
        requester.request { res ->
            viewModelScope.launch {
                _result.emit(res)
            }
        }
    }
}
