package com.zorro.easy.permissions.ui

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.zorro.easy.permissions.viewmodel.PermissionFragmentViewModel
import com.zorro.easy.permissions.utils.PermissionSettingsOpener
import com.zorro.easy.permissions.constant.Constants
import com.zorro.easy.permissions.model.PermissionEvent
import com.zorro.easy.permissions.model.PermissionEffect
import kotlinx.coroutines.launch


/**
 * 无 UI 的 Host Fragment：承载权限申请逻辑。
 *
 * 使用时通过 arguments 传入:
 * - Constants.FRAGMENT_ARG_REQUEST_KEY (String)
 * - Constants.FRAGMENT_ARG_SHOW_SETTING_DIALOG (Boolean)
 * - Constants.FRAGMENT_ARG_PERMS (StringArray)
 *
 * Fragment 会向 caller 通过 FragmentResult 发送结果（使用 requestKey）。
 *
 * 关键点：
 * - 使用 ViewModel 保存状态，旋转时不丢失
 * - 不使用 ActivityResult 来打开设置页（startActivity），返回时 onResume 检查
 * - DialogFragment 使用 Manager + tag 来避免重复添加
 */
class PermissionHostFragment : Fragment() {

    private val vm: PermissionFragmentViewModel by viewModels()

    private var requestKey: String = ""
    private var showSettingDialog: Boolean = true
    private var allPerms: List<String> = emptyList()

    // launcher 用于系统权限弹框阶段
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            // 处理结果: 计算 granted / denied / permanentlyDenied
            val granted = result.filterValues { it }.keys.toList()
            val denied = result.filterValues { !it }.keys.toList()
            // permanentlyDenied detection requires shouldShowRequestPermissionRationale
            val permanentlyDenied = denied.filter { perm ->
                // If shouldShowRequestPermissionRationale == false and permission still denied -> likely permanently denied.
                // Note: On first request, shouldShowRequestPermissionRationale might be false; caller UX should handle explanations before.
                !shouldShowRequestPermissionRationale(perm)
            }
            vm.onActivityResult(showSettingDialog, granted, denied, permanentlyDenied)
        }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // keep no UI
        // retainInstance = false
        subscribeToVm()
    }

    private fun subscribeToVm() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    vm.permissionUiState.collect { st ->
                        // 处理 state
                        if (!st.launched && st.requestPermissions.isNotEmpty()) {
                            // launch system permission request
                            permissionLauncher.launch(st.requestPermissions.toTypedArray())
                            vm.markLaunched()
                        }
                    }
                }
            }
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                vm.effect.collect { eff ->
                    // 处理 effect
                    when (eff) {
                        is PermissionEffect.ShowSettingsDialog -> {
                            // show dialog (use parentFragmentManager) with unique tag
                            registerListenerFragmentResultListener()
                            PermissionDeniedDialogFragment.show(
                                parentFragmentManager,
                                requestKey,
                                eff.permanentlyDenied
                            )
                        }

                        is PermissionEffect.Completed -> {
                            // Completed -> send FragmentResult back to caller and remove self
                            val bundle = when (val r = eff.result) {
                                is PermissionEvent.AllGranted -> Bundle().apply {
                                    putBoolean(Constants.FRAGMENT_RESULT_GRANTED_STATE_KEY, true)
                                    putStringArrayList(
                                        Constants.FRAGMENT_RESULT_GRANTED_LIST_KEY,
                                        ArrayList(r.granted)
                                    )
                                    putStringArrayList(
                                        Constants.FRAGMENT_RESULT_DENIED_LIST_KEY,
                                        ArrayList()
                                    )
                                }

                                is PermissionEvent.PartialGranted -> Bundle().apply {
                                    putBoolean(Constants.FRAGMENT_RESULT_GRANTED_STATE_KEY, false)
                                    putStringArrayList(
                                        Constants.FRAGMENT_RESULT_GRANTED_LIST_KEY,
                                        ArrayList(r.granted)
                                    )
                                    putStringArrayList(
                                        Constants.FRAGMENT_RESULT_DENIED_LIST_KEY,
                                        ArrayList(r.denied)
                                    )
                                }
                            }
                            parentFragmentManager.setFragmentResult(requestKey, bundle)
                            // dismiss possible dialog
                            val dlgTag = Constants.DIALOG_FRAGMENT_TAG_PREFIX + requestKey
                            (parentFragmentManager.findFragmentByTag(dlgTag) as? DialogFragment)
                                ?.dismissAllowingStateLoss()
                            // remove self
                            parentFragmentManager.beginTransaction()
                                .remove(this@PermissionHostFragment)
                                .commitNowAllowingStateLoss()
                        }
                    }
                }
            }
        }


    }

    override fun onResume() {
        super.onResume()
        vm.onResumeCheck()
    }

    private fun registerListenerFragmentResultListener() {
        setFragmentResultListener("${Constants.DIALOG_FRAGMENT_REQUEST_KEY}$requestKey") { _, bundle ->
            val confirmed = bundle.getBoolean(Constants.DIALOG_FRAGMENT_RESULT_KEY)
            if (confirmed) {
                onConfirmFromDialog()
            } else {
                onCancelFromDialog()
            }
        }
    }

    // Dialog callbacks
    private fun onConfirmFromDialog() {
        // mark VM as waiting settings and open settings via startActivity (no ActivityResult)
        vm.openedSettings(true)
        PermissionSettingsOpener.startSettingActivity(requireContext())
    }

    private fun onCancelFromDialog() {
        val ctx = requireContext()
        val granted = allPerms.filter {
            ContextCompat.checkSelfPermission(
                ctx,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
        val denied = allPerms - granted.toSet()
        vm.openedSettings(false)
        vm.completedWith(PermissionEvent.PartialGranted(granted, denied))
    }

    fun enqueueRequest(
        requestKey: String,
        permissions: List<String>,
        showSettingDialog: Boolean
    ) {
        this.requestKey = requestKey
        this.allPerms = permissions
        this.showSettingDialog = showSettingDialog
        vm.start(requestKey, allPerms, allPerms)
    }

    companion object {
        fun newInstance() = PermissionHostFragment()
    }
}
