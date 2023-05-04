package com.m800.sdk.core.demo.utils

import android.app.Activity
import android.view.WindowManager

const val EMPTY_STRING = ""

fun enableKeepScreenOn(activity: Activity) {
    changeScreenOnSettings(activity, applyChange = true, includeKeepScreenOn = true)
}

fun disableKeepScreenOn(activity: Activity) {
    changeScreenOnSettings(activity, applyChange = false, includeKeepScreenOn = true)
}

private fun changeScreenOnSettings(activity: Activity, applyChange: Boolean, includeKeepScreenOn: Boolean) {
    var flags = WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    if (includeKeepScreenOn) {
        flags = flags or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
    }

    if (applyChange) {
        activity.window.addFlags(flags)
    } else {
        activity.window.clearFlags(flags)
    }
}