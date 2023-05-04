package com.m800.sdk.core.demo.call

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.m800.sdk.call.*
import com.m800.sdk.call.common.CinnoxCallResult
import com.m800.sdk.call.error.CinnoxCallSdkError
import com.m800.sdk.call.error.CinnoxCallSdkException
import java.util.*
import kotlin.concurrent.schedule
import com.m800.sdk.core.demo.R
import com.m800.sdk.core.demo.utils.EMPTY_STRING
import com.m800.sdk.core.demo.utils.getFormattedDurationFromMills

class CallViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val CALL_END_VIEW_HOLD_TIME_MS = 3000L
    }

    private val TAG = CallViewModel::class.java.simpleName
    private val callController = CallController.getInstance()
    private val audioController = CallController.getInstance().getAudioController()
    private val currentCallSession = callController.getCurrentCallSession() ?: throw CinnoxCallSdkException(
        CinnoxCallSdkError.SDK_API_ERROR,
        "current call session is null"
    )
    private val context = application.applicationContext
    private val currentCallState = MutableLiveData<CinnoxCallSession.State>()
    private val status = MutableLiveData(context.getString(R.string.lc_common_text_connecting))
    private val isAudioRouteBottomSheetDisplayed = MutableLiveData(false)
    private val isMute = MutableLiveData(false)
    private val isKeypadDisplayed: MutableLiveData<Boolean> = MutableLiveData(false)
    private val isCallEnd = MutableLiveData(false)
    private var timer: Timer? = null

    private val callSessionListener = object : CinnoxCallSession.CinnoxCallSessionListener {
        override fun onStateChange(session: CinnoxCallSession, state: CinnoxCallSession.State) {
            Log.i(TAG, "onStateChange call info: ${session.getCallInfo()}, state: $state")
            updateUiByCallState(state)
        }
    }

    override fun onCleared() {
        super.onCleared()
        currentCallSession.unregisterCallSessionListener(callSessionListener)
        timer?.cancel()
        timer?.purge()
        timer = null
        isMute.postValue(false)
    }

    fun init() {
        currentCallSession.registerCallSessionListener(callSessionListener)
        updateUiByCallState(currentCallSession.getState())
    }

    private fun updateUiByCallState(state: CinnoxCallSession.State) {
        Log.i(TAG, "updateUiByCallState state: $state")
        if (currentCallState.value == state) {
            Log.i(TAG, "same state: $state. ignore it")
            return
        }
        when (state) {
            CinnoxCallSession.State.ESTABLISHED -> {
                if (isIncoming()) {
                    status.postValue(context.getString(R.string.lc_call_text_statusIncomingAudioCall))
                } else {
                    status.postValue(context.getString(R.string.lc_call_text_statusCalling))
                }
            }

            CinnoxCallSession.State.TALKING -> {
                handleTalkingState()
            }

            CinnoxCallSession.State.TERMINATED -> {
                timer?.cancel()
                timer?.purge()
                status.postValue(getCallEndStatus())
            }

            CinnoxCallSession.State.HELD, CinnoxCallSession.State.FORCE_HELD, CinnoxCallSession.State.REMOTE_HELD -> {
                handleTalkingState()
            }

            CinnoxCallSession.State.DESTROYED -> {
                handleCallEndDelay()
            }
        }
        currentCallState.postValue(state)
        isMute.postValue(currentCallSession.isMuted())
    }

    private fun handleTalkingState() {
        timer?.cancel()
        timer?.purge()
        timer = Timer()
        timer?.schedule(0, 1000) {
            val duration = getTalkingDurationText()
            when (currentCallState.value) {
                CinnoxCallSession.State.TALKING -> {
                    status.postValue(duration)
                }
                CinnoxCallSession.State.HELD, CinnoxCallSession.State.FORCE_HELD, CinnoxCallSession.State.REMOTE_HELD -> {
                    status.postValue(context.getString(R.string.lc_call_button_hold))
                }
            }
        }
    }

    private fun handleCallEndDelay() {
        Timer().schedule(CALL_END_VIEW_HOLD_TIME_MS) {
            isCallEnd.postValue(true)
        }
    }

    private fun getCallEndStatus(): String {
        val terminatedReason = currentCallSession.getCallInfo().getTerminatedReason()
        Log.i(TAG, "getCallEndStatus terminatedReason: $terminatedReason")

        if (terminatedReason == null) {
            return EMPTY_STRING
        }

        var status = EMPTY_STRING
        when (terminatedReason.terminatedResult) {
            CinnoxCallResult.CANCEL -> {
                status =
                    context.getString(if (isIncoming()) R.string.lc_call_text_statusCallMissed else R.string.lc_call_text_statusCallCanceled)
            }

            CinnoxCallResult.REJECT ->
                status = context.getString(R.string.lc_call_text_statusCallDeclined)

            CinnoxCallResult.TIMEOUT -> {
                status =
                    context.getString(if (isIncoming()) R.string.lc_call_text_statusCallCanceled else R.string.lc_call_text_statusNoAnswer)
            }

            CinnoxCallResult.HANGUP ->
                status = context.getString(
                    R.string.lc_call_text_statusCallEndedByUser,
                    getTalkingDurationText()
                )

            CinnoxCallResult.ERROR, CinnoxCallResult.UNKNOWN, CinnoxCallResult.MEDIA_TIMEOUT, CinnoxCallResult.API_FAIL ->
                status = context.getString(R.string.lc_call_text_statusFailedToConnect)

            CinnoxCallResult.CANCEL_BY_OTHER_PERSON ->
                status = context.getString(R.string.lc_call_text_status_cancel_by_other_staff)

            CinnoxCallResult.CANCEL_BY_OTHER_DEVICE ->
                status = context.getString(R.string.lc_call_text_status_cancel_by_other_device)

            CinnoxCallResult.THE_CALL_IS_PICKED_UP ->
                status = context.getString(R.string.lc_call_text_status_the_call_is_picked_up)
        }

        Log.i(TAG,"getCallEndStatus status: $status")

        return status
    }

    fun hangup() {
        Log.i(TAG, "hangup")
        currentCallSession.hangup(isUserClickHangUp = true)
    }

    fun answer(includeLocalVideo: Boolean = false) {
        Log.i(TAG, "answer")
        callController.canHandleCallCheckPermission(
            includeLocalVideo,
            object : CallController.CanHandleCallListener {
                override fun onCheckResult(canHandleCall: Boolean) {
                    if (canHandleCall) {
                        currentCallSession.answer(includeLocalVideo)
                    }
                }
            })
    }

    fun setMute(isChecked: Boolean) {
        Log.i(TAG, "setMute isChecked: $isChecked")
        isMute.postValue(isChecked)
        currentCallSession.setMuted(isChecked)
    }

    fun setHold(isChecked: Boolean) {
        Log.i(TAG, "setHold isChecked: $isChecked")
        if (isChecked && currentCallState.value == CinnoxCallSession.State.HELD) {
            Log.i(TAG, "setHold current call state is HELD, return")
            return
        }
        currentCallSession.setHold(isChecked)
    }

    fun setKeypad(isChecked: Boolean) {
        Log.i(TAG, "setKeypad isChecked: $isChecked")
        isKeypadDisplayed.postValue(isChecked)
    }

    fun checkAndSetAudioRouting() {
        val currentRoutingList = audioController.getRoutingList()
        val routingListSize = currentRoutingList.size
        if (routingListSize <= 2) {
            val currentAudioRoute = audioController.getCurrentRouting()
            val otherAudioRoute = currentRoutingList.firstOrNull { it != currentAudioRoute }
            otherAudioRoute?.let {
                audioController.switchToRoute(otherAudioRoute)
            }
        } else {
            isAudioRouteBottomSheetDisplayed.postValue(true)
        }
    }

    private fun getTalkingDurationText(): String {
        val duration = currentCallSession.getTalkingTime()
        return getFormattedDurationFromMills(duration)
    }

    fun sendDtmf(digits: String) {
        currentCallSession.sendDtmf(digits)
    }

    fun getCurrentCallState(): MutableLiveData<CinnoxCallSession.State> {
        return currentCallState
    }

    fun getStatus(): MutableLiveData<String> {
        return status
    }

    fun getAudioRouteBottomSheetDisplayed(): MutableLiveData<Boolean> {
        return isAudioRouteBottomSheetDisplayed
    }

    fun getMute(): MutableLiveData<Boolean> {
        return isMute
    }

    fun getKeypadDisplayed(): MutableLiveData<Boolean> {
        return isKeypadDisplayed
    }

    fun getCallEnd(): MutableLiveData<Boolean> {
        return isCallEnd
    }

    fun isIncoming(): Boolean {
        return currentCallSession.getCallInfo().getDirection() == CinnoxCallSession.Direction.INCOMING
    }

    fun isOffnet(): Boolean {
        return currentCallSession.getCallInfo().getCallType() == CinnoxCallSession.CallType.OFFNET
    }
}