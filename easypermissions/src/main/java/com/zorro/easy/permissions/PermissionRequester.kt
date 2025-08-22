package com.zorro.easy.permissions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID
import kotlin.collections.addAll

/**
 * 外部调用类：支持 from(activity) / from(fragment)，DSL，callback / await / flow。
 */
class PermissionRequester private constructor(
    private val fm: FragmentManager,
    private val lifecycleOwnerForListener: Any // Activity or Fragment
) {
    private val perms = linkedSetOf<String>()
    private val tag = "PermissionFragment"

    fun permissions(vararg groups: PermissionGroup) = apply {
        groups.forEach { perms.addAll(it.permissions) }
    }

    fun permissions(vararg rawPerms: String) = apply {
        rawPerms.forEach { perms.add(it) }
    }

    /** callback 方式（DSL） */
    fun request(onResult: (PermissionResult) -> Unit) {
        val requestKey = "perm_req_" + UUID.randomUUID().toString()
        // register listener BEFORE adding fragment
        registerListener(requestKey, onResult)
        addHostFragment(requestKey)
    }

    /** suspend await 方式 */
    suspend fun await(): PermissionResult {
        val def = CompletableDeferred<PermissionResult>()
        request {
            def.complete(it)
        }
        return def.await()
    }

    /** Flow 方式（单次结果的 Flow） */
    fun asFlow(): Flow<PermissionResult> = callbackFlow {
        val requestKey = "perm_req_" + UUID.randomUUID().toString()
        registerListener(requestKey) { res ->
            trySend(res).isSuccess
            close()
        }
        addHostFragment(requestKey)
    }

    private fun registerListener(requestKey: String, cb: (PermissionResult) -> Unit) {
        // Using lifecycle owner: if activity, pass activity; if fragment, pass the fragment
        when (lifecycleOwnerForListener) {
            is FragmentActivity -> {
                fm.setFragmentResultListener(requestKey, lifecycleOwnerForListener) { _, bundle ->
                    val result = bundleToPermissionResult(bundle)
                    cb(result)
                }
            }

            is Fragment -> {
                fm.setFragmentResultListener(requestKey, lifecycleOwnerForListener) { _, bundle ->
                    val result = bundleToPermissionResult(bundle)
                    cb(result)
                }
            }

            else -> throw IllegalArgumentException("Unsupported lifecycle owner")
        }
    }

    private fun bundleToPermissionResult(bundle: Bundle): PermissionResult {
        val granted = bundle.getStringArrayList("granted_list") ?: arrayListOf()
        val denied = bundle.getStringArrayList("denied_list") ?: arrayListOf()
        val isGranted = bundle.getBoolean("granted", false)
        return if (isGranted) PermissionResult.Success(granted) else PermissionResult.Partial(
            granted,
            denied
        )
    }

    private fun addHostFragment(requestKey: String) {
        val tag = "PermissionHost_$requestKey"
        // ensure not duplicated
        if (fm.findFragmentByTag(tag) != null) return
        val host = PermissionHostFragment.newInstance(requestKey, perms.toList())
        fm.beginTransaction().add(host, tag).commitAllowingStateLoss()
    }

    companion object {
        fun from(activity: FragmentActivity): PermissionRequester {
            return PermissionRequester(activity.supportFragmentManager, activity)
        }

        fun from(fragment: Fragment): PermissionRequester {
            return PermissionRequester(fragment.childFragmentManager, fragment)
        }
    }
}
