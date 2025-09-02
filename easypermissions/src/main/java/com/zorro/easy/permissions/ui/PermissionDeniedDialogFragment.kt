package com.zorro.easy.permissions.ui

import android.app.Dialog
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.setFragmentResult
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zorro.easy.permissions.PermissionGroup
import com.zorro.easy.permissions.R
import com.zorro.easy.permissions.constant.Constants

/**
 * DialogFragment 用于提示“权限被永久拒绝”。
 */
class PermissionDeniedDialogFragment : DialogFragment() {
    private val requestKey: String by lazy {
        arguments?.getString(Constants.DIALOG_FRAGMENT_ARG_REQUEST_KEY) ?: ""
    }
    private val perms: Array<String> by lazy {
        arguments?.getStringArray(Constants.DIALOG_FRAGMENT_ARG_PERMS) ?: emptyArray()
    }

//    @Suppress("DEPRECATION")
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        retainInstance = true
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val message = buildMessage(perms)
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.permission_denied_title))
            .setMessage(getString(R.string.permission_denied_message, message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.permission_go_settings)) { _, _ ->
                setFragmentResult(
                    "${Constants.DIALOG_FRAGMENT_REQUEST_KEY}$requestKey",
                    bundleOf(Constants.DIALOG_FRAGMENT_RESULT_KEY to true)
                )
            }
            .setNegativeButton(getString(R.string.permission_cancel)) { _, _ ->
                setFragmentResult(
                    "${Constants.DIALOG_FRAGMENT_REQUEST_KEY}$requestKey",
                    bundleOf(Constants.DIALOG_FRAGMENT_RESULT_KEY to false)
                )
            }
            .create()
    }

    private fun buildMessage(permissions: Array<String>): String {
        val context = requireContext()
        val list = permissions.mapNotNull { perm ->
            PermissionGroup.findGroupByPermission(perm)?.let { group ->
                when (group) {
                    is PermissionGroup.BuiltIn -> context.getString(group.labelRes)
                    is PermissionGroup.Custom -> group.label
                }
            }
        }.distinct()
        return when (list.size) {
            0 -> ""
            1 -> list[0]
            else -> {
                val allButLast = list.dropLast(1).joinToString(getString(R.string.permission_comma))
                "$allButLast${getString(R.string.permission_and)}${list.last()}"
            }
        }
    }

    companion object {
        fun show(
            hostFragmentManager: FragmentManager,
            requestKey: String,
            perms: List<String>
        ) {
            val tag = Constants.DIALOG_FRAGMENT_TAG_PREFIX + requestKey
            // 关闭已存在的DialogFragment
            (hostFragmentManager.findFragmentByTag(tag) as? DialogFragment)?.dismissAllowingStateLoss()
            val df = PermissionDeniedDialogFragment().apply {
                arguments = Bundle().apply {
                    putStringArray(
                        Constants.DIALOG_FRAGMENT_ARG_PERMS,
                        perms.toTypedArray()
                    )
                    putString(
                        Constants.DIALOG_FRAGMENT_ARG_REQUEST_KEY,
                        requestKey
                    )
                }
            }
            df.show(hostFragmentManager, tag)
        }
    }
}

