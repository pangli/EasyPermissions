package com.zorro.easy.permissions

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class PermissionFragment : Fragment() {
    internal var grantedCallback: (() -> Unit)? = null
    internal var deniedCallback: ((List<String>) -> Unit)? = null
    private var permissions: Array<String> = emptyArray()
    private val settingsLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // 从设置返回后再检查权限
            val stillDenied = permissions.filter {
                requireContext().checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
            }
            if (stillDenied.isEmpty()) {
                grantedCallback?.invoke()
            } else {
                deniedCallback?.invoke(stillDenied)
            }
            finish()
        }
    private val launcher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            val denied = result.filterValues { !it }.keys.toList()
            when {
                denied.isEmpty() -> {
                    grantedCallback?.invoke()
                    finish()
                }

                denied.any { shouldShowRequestPermissionRationale(it) } -> {
                    deniedCallback?.invoke(denied)
                    finish()
                }

                else -> {
                    PermissionDeniedDialog.newInstance(denied.toTypedArray()) { reopen ->
                        if (reopen) {
                            settingsLauncher.launch(
                                PermissionSettingsOpener.getSettingIntent(
                                    requireContext()
                                )
                            )
                        } else {
                            deniedCallback?.invoke(denied)
                            finish()
                        }
                    }.show(parentFragmentManager, "PermissionDeniedDialog")
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
        permissions = arguments?.getStringArray(ARG_PERMISSIONS) ?: emptyArray()
        requestPermissions()
    }

    fun requestPermissions() {
        val supportedPermissions = filterSupportedPermissions(requireContext(), permissions)
        if (supportedPermissions.isEmpty()) {
            grantedCallback?.invoke()
            finish()
        } else {
            permissions = supportedPermissions
            launcher.launch(permissions)
        }
    }

    private fun finish() {
        parentFragmentManager.beginTransaction()
            .remove(this)
            .commitAllowingStateLoss()
    }

    companion object {
        private const val ARG_PERMISSIONS = "arg_permissions"
        fun newInstance(permissions: Array<String>) = PermissionFragment().apply {
            arguments = Bundle().apply { putStringArray(ARG_PERMISSIONS, permissions) }
        }
    }
}
