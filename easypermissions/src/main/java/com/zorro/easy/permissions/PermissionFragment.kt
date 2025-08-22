package com.zorro.easy.permissions

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

/**
 * 无 UI 的 Host Fragment：承载权限申请逻辑。
 *
 * 使用时通过 arguments 传入:
 * - ARG_REQUEST_KEY (String)
 * - ARG_PERMS (StringArray)
 *
 * Fragment 会向 caller 通过 FragmentResult 发送结果（使用 requestKey）。
 *
 * 关键点：
 * - 使用 ViewModel 保存状态，旋转时不丢失
 * - 不使用 ActivityResult 来打开设置页（startActivity），返回时 onResume 检查
 * - DialogFragment 使用 Manager + tag 来避免重复添加
 */
class PermissionHostFragment : Fragment(), PermissionDeniedDialogFragment.Callback {

    private val vm: PermissionViewModel by viewModels()

    private val requestKey: String by lazy { requireArguments().getString(ARG_REQUEST_KEY) ?: "" }
    private val allPerms: List<String> by lazy {
        requireArguments().getStringArray(ARG_PERMS)?.toList().orEmpty()
    }

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

            vm.onSystemResult(granted, denied, permanentlyDenied)
        }

    @Suppress("DEPRECATION")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // keep no UI
        retainInstance = false
        setStyleNoUi()
        subscribeToVm()
        // Start when fragment first created if state not started
        if (savedInstanceState == null) {
            // version filter
            val supported = filterSupportedPermissions(requireContext(), allPerms).distinct()
            vm.start(requestKey, allPerms, supported)
        }
    }

    private fun subscribeToVm() {
        vm.state.onEach { st ->
            // when state indicates toRequest and not yet launched => launch
            if (!st.launched && st.toRequest.isNotEmpty()) {
                // launch system permission request
                permissionLauncher.launch(st.toRequest.toTypedArray())
                vm.markLaunched()
            }
            // If VM says waitingSettingsReturn etc, we handle in onResume
            // If VM has permanentlyDenied saved, ensure dialog visible (handled in effect)
        }.launchIn(lifecycleScope)

        vm.effect.onEach { eff ->
            when (eff) {
                is Effect.LaunchSystemRequest -> {
                    // handled by state subscriber above
                }

                is Effect.ShowSettingsDialog -> {
                    // show dialog (use parentFragmentManager) with unique tag
                    PermissionDeniedDialogFragment.show(
                        parentFragmentManager,
                        requestKey,
                        eff.perms
                    )
                }

                is Effect.Completed -> {
                    // Completed -> send FragmentResult back to caller and remove self
                    val bundle = when (val r = eff.result) {
                        is PermissionResult.Success -> Bundle().apply {
                            putBoolean("granted", true)
                            putStringArrayList("granted_list", ArrayList(r.granted))
                            putStringArrayList("denied_list", ArrayList())
                        }

                        is PermissionResult.Partial -> Bundle().apply {
                            putBoolean("granted", false)
                            putStringArrayList("granted_list", ArrayList(r.granted))
                            putStringArrayList("denied_list", ArrayList(r.denied))
                        }
                    }
                    parentFragmentManager.setFragmentResult(requestKey, bundle)
                    // dismiss possible dialog
                    val dlgTag = PermissionDeniedDialogFragment::class.java.name + "_" + requestKey
                    (parentFragmentManager.findFragmentByTag(dlgTag) as? DialogFragment)?.dismissAllowingStateLoss()
                    // remove self
                    parentFragmentManager.beginTransaction().remove(this@PermissionHostFragment)
                        .commitAllowingStateLoss()
                }
            }
        }.launchIn(lifecycleScope)
    }

    override fun onResume() {
        super.onResume()
        // If we opened settings (vm tells waitingSettingsReturn), or we just resumed after user may have returned,
        // re-check permissions and let VM handle evaluation.
        vm.onResumeCheck()
    }

    private fun setStyleNoUi() {
        // Ensure fragment doesn't try to draw UI
        view?.visibility = View.GONE
    }

    // Dialog callbacks
    override fun onGoToSettings() {
        // mark VM as waiting settings and open settings via startActivity (no ActivityResult)
        vm.openedSettings()
        PermissionSettingsOpener.startSettingActivity(requireContext())
    }

    override fun onCancelFromDialog() {
        // user cancelled the dialog: finish with Partial (denied)
        // compute current denied / granted
        val ctx = requireContext()
        val granted = allPerms.filter {
            ContextCompat.checkSelfPermission(
                ctx,
                it
            ) == PackageManager.PERMISSION_GRANTED
        }
        val denied = allPerms - granted
        vm.completedWith(PermissionResult.Partial(granted, denied))
    }

    companion object {
        const val ARG_REQUEST_KEY = "arg_request_key"
        const val ARG_PERMS = "arg_perms"
        fun newInstance(requestKey: String, perms: List<String>): PermissionHostFragment {
            return PermissionHostFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_REQUEST_KEY, requestKey)
                    putStringArray(ARG_PERMS, perms.toTypedArray())
                }
            }
        }
    }
}
