package com.zorro.easy.permissions

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

object PermissionSettingsOpener {
    fun getSettingIntent(context: Context): Intent {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        intent.data = Uri.fromParts("package", context.packageName, null)
        return intent
    }
}
