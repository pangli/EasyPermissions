package com.zorro.easy.permissions

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * DialogFragment 用于提示“权限被永久拒绝”。HostFragment 负责调用 show() 并实现回调接口。
 */
class PermissionDeniedDialogFragment : DialogFragment() {

    interface Callback {
        fun onGoToSettings()
        fun onCancelFromDialog()
    }

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        retainInstance = true
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val perms = arguments?.getStringArray(ARG_PERMS) ?: emptyArray()
        val message = buildMessage(perms)
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.permission_denied_title))
            .setMessage(getString(R.string.permission_denied_message, message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.permission_go_settings)) { _, _ ->
                (parentFragment as? Callback)?.onGoToSettings()
            }
            .setNegativeButton(getString(R.string.permission_cancel)) { _, _ ->
                (parentFragment as? Callback)?.onCancelFromDialog()
            }
            .create()
    }

    private fun buildMessage(permissions: Array<String>): String {
        val context = requireContext()
        val list = permissions.map { perm ->
            PermissionGroup.entries.firstOrNull { group ->
                group.permissions.contains(perm)
            }?.let { context.getString(it.labelRes) } ?: "Unknown"
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
        private const val ARG_PERMS = "arg_perms"
        private const val TAG_PREFIX = "PermissionDeniedDialog_"
        fun show(
            hostFragmentManager: FragmentManager,
            requestKey: String,
            perms: List<String>
        ) {
            val tag = TAG_PREFIX + requestKey
            // dismiss existing if any
            (hostFragmentManager.findFragmentByTag(tag) as? DialogFragment)?.dismissAllowingStateLoss()
            val df = PermissionDeniedDialogFragment().apply {
                arguments = Bundle().apply { putStringArray(ARG_PERMS, perms.toTypedArray()) }
            }
            df.show(hostFragmentManager, tag)
        }
    }
}

