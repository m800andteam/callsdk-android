package com.m800.sdk.core.demo.utils

import android.util.Log

object CrashHandler : Thread.UncaughtExceptionHandler {

    private val TAG = CrashHandler::class.java.simpleName

    private var defaultHandler: Thread.UncaughtExceptionHandler? = null

    override fun uncaughtException(t: Thread?, e: Throwable?) {
        e?.let {
            Log.e(TAG, "uncaughtException", e)
        }

        defaultHandler?.uncaughtException(t, e)
    }

    fun init() {
        defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler(this)
    }
}