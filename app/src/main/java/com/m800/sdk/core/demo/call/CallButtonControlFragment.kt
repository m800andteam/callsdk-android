package com.m800.sdk.core.demo.call

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.m800.sdk.call.CinnoxAudioController
import com.m800.sdk.call.CinnoxCallSession
import com.m800.sdk.core.demo.R
import com.m800.sdk.core.demo.databinding.LcCallButtonControlBinding
import com.m800.sdk.core.demo.utils.bindLayerList

class CallButtonControlFragment : Fragment() {

    private val TAG = CallButtonControlFragment::class.java.simpleName
    private var _mBinding: LcCallButtonControlBinding? = null
    private val mBinding get() = _mBinding!!
    private lateinit var mViewModel: CallViewModel
    private val audioController = CallController.getInstance().getAudioController()

    private val audioControllerListener = object : CinnoxAudioController.CinnoxAudioControllerListener {
        override fun onAudioRouteChanged(
            oldRoute: CinnoxAudioController.Route,
            newRoute: CinnoxAudioController.Route
        ) {
            updateAudioBtnIcon(newRoute)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _mBinding = LcCallButtonControlBinding.inflate(inflater, container, false)
        mBinding.lifecycleOwner = this
        audioController.registerAudioControllerListener(audioControllerListener)
        return mBinding.root
    }

    override fun onDestroyView() {
        audioController.unregisterAudioControllerListener(audioControllerListener)
        super.onDestroyView()
        _mBinding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentFragment?.let {
            mViewModel = ViewModelProviders.of(it).get(CallViewModel::class.java)
        } ?: run {
            Log.e(TAG, "onViewCreated get LiveConnectCallViewModel error")
            return
        }
        mBinding.viewModel = mViewModel

        // Set hangup/hold button UI state
        mViewModel.getCurrentCallState().observe(viewLifecycleOwner) {
            switchHoldBtnChecked(it)
            switchBtnEnabled(it)
        }

        // Set mute/speaker button UI state
        updateAudioBtnIcon(audioController.getCurrentRouting())

        mViewModel.getMute().observe(viewLifecycleOwner) {
            mBinding.lcBtnMute.isChecked = it
            mBinding.tvBtnMute.setText(if (it) R.string.lc_room_text_unmute else R.string.lc_room_text_mute)
        }

        mBinding.lcBtnKeypadPane.setOnClickListener {
            mViewModel.setKeypad(true)
        }

        mBinding.lcBtnKeypad.setOnClickListener {
            mViewModel.setKeypad(true)
        }

        mBinding.lcBtnAudio.setOnClickListener {
            mViewModel.checkAndSetAudioRouting()
        }

        mBinding.lcBtnHidePane.setOnClickListener {
            mViewModel.setKeypad(false)
        }
    }

    private fun switchHoldBtnChecked(state: CinnoxCallSession.State) {
        val isHeld = state == CinnoxCallSession.State.HELD || state == CinnoxCallSession.State.FORCE_HELD
        mBinding.lcBtnHold.isChecked = isHeld
        mBinding.tvBtnHold.setText(if (isHeld) R.string.lc_call_button_unhold else R.string.lc_call_button_hold)
    }

    private fun switchBtnEnabled(state: CinnoxCallSession.State) {
        val isAnswering = state == CinnoxCallSession.State.ANSWERING
        val isHeld = state == CinnoxCallSession.State.HELD || state == CinnoxCallSession.State.FORCE_HELD
        val isRemoteHeld = state == CinnoxCallSession.State.REMOTE_HELD

        val enableMuteBtn = !isAnswering && !isHeld && !isRemoteHeld
        mBinding.lcBtnMutePane.alpha = getBtnAlpha(enableMuteBtn)
        mBinding.lcBtnMute.isEnabled = enableMuteBtn

        val enableHoldBtn = !isAnswering && !isRemoteHeld
        mBinding.lcBtnHoldPane.alpha = getBtnAlpha(enableHoldBtn)
        mBinding.lcBtnHold.isEnabled = enableHoldBtn
    }

    private fun updateAudioBtnIcon(route: CinnoxAudioController.Route) {
        when (route) {
            CinnoxAudioController.Route.EARPIECE -> {
                mBinding.lcBtnAudio.setBackgroundResource(R.drawable.call_panel_audio_speaker_icon_light)
                mBinding.lcTvAudio.setText(R.string.lc_call_route_audio)
            }
            CinnoxAudioController.Route.SPEAKER -> {
                mBinding.lcBtnAudio.bindLayerList(R.drawable.call_panel_audio_speaker_icon_dark)
                mBinding.lcTvAudio.setText(R.string.lc_call_route_speaker)
            }
            CinnoxAudioController.Route.BLUETOOTH -> {
                mBinding.lcBtnAudio.bindLayerList(R.drawable.call_panel_audio_bluetooth_icon)
                mBinding.lcTvAudio.setText(R.string.lc_call_route_bluetooth)
            }
            CinnoxAudioController.Route.WIRED_HEADSET -> {
                mBinding.lcBtnAudio.setBackgroundResource(R.drawable.ic_earphone_black)
                mBinding.lcTvAudio.setText(R.string.lc_call_route_headphone)
            }
        }
    }

    private fun getBtnAlpha(enable: Boolean): Float {
        return if (enable) 1f else 0.4f
    }
}


