package com.zorro.easy.permissions

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LifecycleOwner
import com.zorro.easy.permissions.constant.Constants
import com.zorro.easy.permissions.group.PermissionGroup
import com.zorro.easy.permissions.model.PermissionEvent
import com.zorro.easy.permissions.ui.PermissionHostFragment
import com.zorro.easy.permissions.utils.ScreenOrientationUtil
import com.zorro.easy.permissions.viewmodel.PermissionViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.UUID

/**
 * 外部调用类：支持 from(activity) / from(fragment)，DSL，callback / await / flow。
 */
class PermissionRequester private constructor(
    private val fm: FragmentManager,
    private val lifecycleOwner: LifecycleOwner, // Activity or Fragment
    private val originalOrientation: Int
) {
    private val perms = linkedSetOf<String>()

    fun permissions(vararg groups: PermissionGroup) = apply {
        groups.forEach { group ->
            group.permissions.forEach { perm ->
                if (PermissionSupportRegistry.isSupported(context, perm)) {
                    perms.add(perm)
                }
            }
        }
    }

    fun permissions(vararg rawPerms: String) = apply {
        rawPerms.forEach { perm ->
            if (PermissionSupportRegistry.isSupported(context, perm)) {
                perms.add(perm)
            }
        }
    }

    /** callback 方式（DSL） */
    fun request(onResult: (PermissionEvent) -> Unit) {
        val requestKey = UUID.randomUUID().toString()
        // register listener BEFORE adding fragment
        registerListener(requestKey, onResult)
        addHostFragment(requestKey)
    }

    /** suspend await 方式 */
    suspend fun await(): PermissionEvent {
        val def = CompletableDeferred<PermissionEvent>()
        request {
            def.complete(it)
        }
        return def.await()
    }

    /** Flow 方式（单次结果的 Flow） */
    fun asFlow(): Flow<PermissionEvent> = callbackFlow {
        val requestKey = UUID.randomUUID().toString()
        registerListener(requestKey) { res ->
            trySend(res).isSuccess
            close()
        }
        addHostFragment(requestKey)
        awaitClose { }
    }

    /** Flow 方式：借助 ViewModel */
    fun asFlowByViewModel(vm: PermissionViewModel) {
        vm.request(this)
    }

    private fun registerListener(requestKey: String, callback: (PermissionEvent) -> Unit) {
        // Using lifecycle owner: if activity, pass activity; if fragment, pass the fragment
        when (lifecycleOwner) {
            is FragmentActivity -> {
                fm.setFragmentResultListener(requestKey, lifecycleOwner) { _, bundle ->
                    callback(bundleToPermissionResult(bundle))
                    ScreenOrientationUtil.unlock(lifecycleOwner, originalOrientation)
                }
            }

            is Fragment -> {
                fm.setFragmentResultListener(requestKey, lifecycleOwner) { _, bundle ->
                    callback(bundleToPermissionResult(bundle))
                    ScreenOrientationUtil.unlock(lifecycleOwner, originalOrientation)
                }
            }

            else -> throw IllegalArgumentException("Unsupported lifecycle owner")
        }
    }

    private fun bundleToPermissionResult(bundle: Bundle): PermissionEvent {
        val isGranted = bundle.getBoolean(Constants.FRAGMENT_RESULT_GRANTED_STATE_KEY, false)
        val granted =
            bundle.getStringArrayList(Constants.FRAGMENT_RESULT_GRANTED_LIST_KEY) ?: arrayListOf()
        val denied =
            bundle.getStringArrayList(Constants.FRAGMENT_RESULT_DENIED_LIST_KEY) ?: arrayListOf()
        return if (isGranted) PermissionEvent.AllGranted(granted) else PermissionEvent.PartialGranted(
            granted,
            denied
        )
    }

    private fun addHostFragment(requestKey: String) {
        val tag = "${Constants.FRAGMENT_TAG_PREFIX}$requestKey"
        // ensure not duplicated
        if (fm.findFragmentByTag(tag) != null) return
        val host = PermissionHostFragment.newInstance(requestKey, perms.toList())
        fm.beginTransaction().add(host, tag).commitAllowingStateLoss()
    }

    private val context = when (lifecycleOwner) {
        is FragmentActivity -> lifecycleOwner.applicationContext   // Activity 本身就是 Context
        is Fragment -> lifecycleOwner.requireContext()  // Fragment 已 attach 时
        else -> throw IllegalArgumentException("Unsupported LifecycleOwner")
    }

    private fun unlockScreenOrientation() {
        when (lifecycleOwner) {
            is FragmentActivity -> {
                ScreenOrientationUtil.unlock(lifecycleOwner, originalOrientation)
            }

            is Fragment -> {
                ScreenOrientationUtil.unlock(lifecycleOwner, originalOrientation)
            }

            else -> throw IllegalArgumentException("Unsupported lifecycle owner")
        }
    }

    companion object {
        fun from(activity: FragmentActivity): PermissionRequester {
            val originalOrientation = ScreenOrientationUtil.lock(activity)
            return PermissionRequester(
                activity.supportFragmentManager,
                activity,
                originalOrientation
            )
        }

        fun from(fragment: Fragment): PermissionRequester {
            val originalOrientation = ScreenOrientationUtil.lock(fragment)
            return PermissionRequester(
                fragment.childFragmentManager,
                fragment,
                originalOrientation
            )
        }
    }
}
