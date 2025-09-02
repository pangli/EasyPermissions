package com.zorro.easy.permissions.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.fragment.app.Fragment

/**
 * 屏幕方向
 */
object ScreenOrientationUtil {
    /**
     * 锁定屏幕方向
     * @param activity Activity
     * @return currentOrientation Int
     */
    @SuppressLint("SourceLockedOrientationActivity")
    fun lock(activity: Activity): Int {
        val originalOrientation = activity.requestedOrientation
        try {
            if (originalOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
                val currentOrientation = activity.resources.configuration.orientation
                when (currentOrientation) {
                    Configuration.ORIENTATION_LANDSCAPE -> {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    }

                    Configuration.ORIENTATION_PORTRAIT -> {
                        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                    }

                    else -> {}
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return originalOrientation // 返回原始设置，便于恢复
    }

    /**
     * 锁定屏幕方向
     *  @param fragment Fragment
     */
    fun lock(fragment: Fragment): Int {
        return fragment.activity?.let { lock(it) } ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }

    /**
     * 恢复屏幕方向到原始值
     *  @param activity Activity
     *  @param originalOrientation Int
     */
    fun unlock(activity: Activity, originalOrientation: Int) {
        if (originalOrientation == ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED) {
            activity.requestedOrientation = originalOrientation
        }
    }

    /**
     * 恢复屏幕方向到原始值
     *  @param fragment Fragment
     *  @param originalOrientation Int
     */
    fun unlock(fragment: Fragment, originalOrientation: Int) {
        fragment.activity?.let { unlock(it, originalOrientation) }
    }
}

