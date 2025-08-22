package com.zorro.easy.permissions

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PermissionDeniedDialog : DialogFragment() {

    private var deniedPermissions: Array<String> = emptyArray()
    private var callback: ((Boolean) -> Unit)? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        deniedPermissions = arguments?.getStringArray(ARG_DENIED) ?: emptyArray()
        val message = buildMessage(deniedPermissions)
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.permission_denied_title))
            .setMessage(getString(R.string.permission_denied_message, message))
            .setCancelable(false)
            .setPositiveButton(getString(R.string.permission_go_settings)) { _, _ ->
                callback?.invoke(true)
            }
            .setNegativeButton(getString(R.string.permission_cancel)) { _, _ ->
                callback?.invoke(false)
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
        private const val ARG_DENIED = "arg_denied"
        fun newInstance(denied: Array<String>, cb: (Boolean) -> Unit): PermissionDeniedDialog {
            return PermissionDeniedDialog().apply {
                arguments = Bundle().apply { putStringArray(ARG_DENIED, denied) }
                callback = cb
            }
        }
    }
}

