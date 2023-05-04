package com.m800.sdk.core.demo.call

import android.Manifest
import android.util.Log
import com.m800.sdk.CinnoxCallOptions
import com.m800.sdk.call.*
import com.m800.sdk.call.error.CinnoxCallSdkError
import com.m800.sdk.call.error.CinnoxCallSdkException
import com.m800.sdk.core.CinnoxCore
import com.m800.sdk.core.demo.utils.PermissionHelper
import com.m800.sdk.core.demo.navigation.UIManager
import com.m800.sdk.core.demo.navigation.Page
import kotlinx.coroutines.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class CallController {
    companion object {
        private const val REQUEST_CODE_CALL_PERMISSION = 1000
        private var sInstance: CallController? = null

        private val lock = ReentrantLock()

        fun uninitialize() {
            lock.withLock {
                sInstance?.deinit()
                sInstance = null
            }
        }

        fun getInstance(): CallController {
            lock.withLock {
                if (sInstance == null) {
                    sInstance = CallController()
                }
                return sInstance!!
            }
        }
    }

    interface CanHandleCallListener {
        fun onCheckResult(canHandleCall: Boolean)
    }

    private val TAG = CallController::class.java.simpleName
    private lateinit var cinnoxCore: CinnoxCore
    private lateinit var permissionHelper: PermissionHelper
    private lateinit var callManager : CinnoxCallManager
    private var currentCallSession: CinnoxCallSession? = null

    private val callManagerListener = object : CinnoxCallManagerListener {

        override fun onIncomingCall(session: CinnoxCallSession): Boolean {
            Log.i(TAG, "onIncomingCall call info: ${session.getCallInfo()}")
            setCurrentCallSession(session)
            return true
        }

        override fun onMissedCall(info: CinnoxCallManagerListener.MissedCallInfo): Boolean {
            Log.i(TAG, "onMissedCall info: $info")
            return true
        }
    }

    fun initialize(cinnoxCore: CinnoxCore, permissionHelper: PermissionHelper) {
        this.cinnoxCore = cinnoxCore
        this.permissionHelper = permissionHelper
        callManager = cinnoxCore.getCallManager() ?: throw CinnoxCallSdkException(
            CinnoxCallSdkError.SDK_INIT_ERROR,
            "call manager is null"
        )
        callManager.registerCallManagerListener(callManagerListener)
    }

    private fun deinit() {
        callManager.unregisterCallManagerListener(callManagerListener)
    }

    fun getAudioController(): CinnoxAudioController {
        return callManager.getAudioController()
    }

    fun setCurrentCallSession(callSession: CinnoxCallSession) {
        currentCallSession = callSession
        switchToCallPage()
    }

    fun getCurrentCallSession(): CinnoxCallSession? {
        return currentCallSession
    }

    fun makeOnnetCall(
        toEid: String
    ) {
        canHandleCallCheckPermission(
            false,
            object : CanHandleCallListener {
                override fun onCheckResult(canHandleCall: Boolean) {
                    if (canHandleCall) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = callManager.makeCall(
                                CinnoxCallOptions.initOnnet(
                                    toEid
                                )
                            )
                            result.callSession?.let {
                                setCurrentCallSession(it)
                            }
                        }
                    }
                }
            })
    }

    fun makeOffnetCall(
        toNumber: String,
        cliNumber: String
    ) {
        canHandleCallCheckPermission(
            false,
            object : CanHandleCallListener {
                override fun onCheckResult(canHandleCall: Boolean) {
                    if (canHandleCall) {
                        CoroutineScope(Dispatchers.IO).launch {
                            val result = callManager.makeCall(
                                CinnoxCallOptions.initOffnet(
                                    toNumber,
                                    cliNumber
                                )
                            )
                            result.callSession?.let {
                                setCurrentCallSession(it)
                            }
                        }
                    }
                }
            })
    }

    fun canHandleCallCheckPermission(
        isVideoCall: Boolean,
        callback: CanHandleCallListener
    ) {
        if (!permissionHelper.hasPermission(Manifest.permission.RECORD_AUDIO)
            || (isVideoCall && !permissionHelper.hasPermission(Manifest.permission.CAMERA))
            || !permissionHelper.hasBTPermission()) {
            val permissions = mutableListOf<String>()
            if (isVideoCall && !permissionHelper.hasPermission(Manifest.permission.CAMERA)) {
                permissions.add(Manifest.permission.CAMERA)
            }
            if (!permissionHelper.hasPermission(Manifest.permission.RECORD_AUDIO)) {
                permissions.add(Manifest.permission.RECORD_AUDIO)
            }
            if (!permissionHelper.hasBTPermission()) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            }
            permissionHelper.requestPermissions(
                REQUEST_CODE_CALL_PERMISSION, object : PermissionHelper.Callback {
                    override fun onPermissionsGranted(requestCode: Int) {
                        if (requestCode == REQUEST_CODE_CALL_PERMISSION) {
                            callback.onCheckResult(true)
                        }
                    }

                    override fun onPermissionsDenied(requestCode: Int) {
                        callback.onCheckResult(false)
                    }
                }, *permissions.toTypedArray()
            )
        } else {
            callback.onCheckResult(true)
        }
    }

    private fun switchToCallPage() {
        UIManager.getNavigator().presentPage(Page.CALL)
    }
}
