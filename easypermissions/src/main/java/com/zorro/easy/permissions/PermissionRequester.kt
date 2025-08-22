package com.zorro.easy.permissions

import android.content.pm.PackageManager
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.collections.addAll
import kotlin.coroutines.resume

class PermissionRequester private constructor(
    private val host: Any,
    private val permissions: Array<String>
) {
    private val tag = "PermissionFragment"
    private var onGranted: (() -> Unit)? = null
    private var onDenied: ((List<String>) -> Unit)? = null

    fun onGranted(callback: () -> Unit): PermissionRequester {
        this.onGranted = callback
        return this
    }

    fun onDenied(callback: (List<String>) -> Unit): PermissionRequester {
        this.onDenied = callback
        return this
    }

    fun request() {
        val fm = getFragmentManager(host)
        // 避免重复添加
        val fragment = fm.findFragmentByTag(tag) as? PermissionFragment
        if (fragment != null) {
            // 更新回调，确保多次请求可以生效
            fragment.grantedCallback = { onGranted?.invoke() }
            fragment.deniedCallback = { onDenied?.invoke(it) }
            return
        }
        // Fragment 不存在才创建
        PermissionFragment.newInstance(permissions).apply {
            grantedCallback = { onGranted?.invoke() }
            deniedCallback = { onDenied?.invoke(it) }
        }.also {
            fm.beginTransaction()
                .add(it, tag)
                .commitNowAllowingStateLoss()
        }
    }

    suspend fun await(): Boolean = suspendCancellableCoroutine { cont ->
        val fm = getFragmentManager(host)
        var fragment = fm.findFragmentByTag(tag) as? PermissionFragment
        if (fragment == null) {
            fragment = PermissionFragment.newInstance(permissions)
            fm.beginTransaction()
                .add(fragment, tag)
                .commitNowAllowingStateLoss()
        }
        fragment.apply {
            grantedCallback = { if (cont.isActive) cont.resume(true) }
            deniedCallback = { if (cont.isActive) cont.resume(false) }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun requestFlow(): Flow<Boolean> = callbackFlow {
        val fm = getFragmentManager(host)
        var fragment = fm.findFragmentByTag(tag) as? PermissionFragment
        if (fragment == null) {
            fragment = PermissionFragment.newInstance(permissions)
            fm.beginTransaction()
                .add(fragment, tag)
                .commitNowAllowingStateLoss()
        }
        fragment.apply {
            grantedCallback = { if (!isClosedForSend) trySend(true).isSuccess }
            deniedCallback = { if (!isClosedForSend) trySend(false).isSuccess }
        }
    }

    private fun getFragmentManager(host: Any) = when (host) {
        is FragmentActivity -> host.supportFragmentManager
        is Fragment -> host.childFragmentManager
        else -> throw IllegalArgumentException("Host must be Activity or Fragment")
    }

    companion object {
        fun from(activity: FragmentActivity, permissions: Array<String>) =
            PermissionRequester(activity, permissions)

        fun from(fragment: Fragment, permissions: Array<String>) =
            PermissionRequester(fragment, permissions)

        fun from(activity: FragmentActivity, vararg groups: PermissionGroup) = run {
            PermissionRequester(activity, buildPermission(*groups))
        }

        fun from(fragment: Fragment, vararg groups: PermissionGroup) = run {
            PermissionRequester(fragment, buildPermission(*groups))
        }

        fun buildPermission(vararg groups: PermissionGroup) = run {
            val permissions = mutableListOf<String>()
            groups.forEach { permissions.addAll(it.permissions) }
            permissions.toTypedArray()
        }

        fun hasPermissions(host: Any, permissions: Array<String>): Boolean {
            val context = when (host) {
                is FragmentActivity -> host
                is Fragment -> host.requireContext()
                else -> return false
            }
            return permissions.all {
                context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
            }
        }

        fun hasPermissions(host: Any, group: PermissionGroup): Boolean =
            hasPermissions(host, group.permissions)
    }
}
