package com.m800.sdk.core.demo.utils

import java.util.*
import java.util.concurrent.TimeUnit

fun getFormattedDurationFromMills(duration: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(duration)
    val mins = TimeUnit.MILLISECONDS.toMinutes(duration) % 60
    val secs = TimeUnit.MILLISECONDS.toSeconds(duration) % 60

    return if (hours > 0) {
        String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, mins, secs)
    } else {
        String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
    }
}
