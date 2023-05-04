package com.m800.sdk.core.demo.call

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.m800.sdk.call.*
import com.m800.sdk.core.demo.navigation.UIManager
import com.m800.sdk.core.demo.utils.CustomManager
import com.m800.sdk.core.util.NetworkQuality
import com.m800.sdk.core.demo.R
import com.m800.sdk.core.demo.databinding.LcViewCallBinding
import com.m800.sdk.core.demo.fragment.ColorMode
import com.m800.sdk.core.demo.navigation.Page

class CallFragment : CallBaseFragment(), Observer<NetworkQuality.Quality> {

    private val TAG = CallFragment::class.java.simpleName
    private var _mBinding: LcViewCallBinding? = null
    private val mBinding get() = _mBinding!!
    private lateinit var mViewModel: CallViewModel

    enum class ButtonGroupStatus(val value: Int) {
        DEFAULT(0),
        INCOMING(1),
        CONTROL(2);

        companion object {
            fun fromValue(value: Int?): ButtonGroupStatus {
                return values().firstOrNull { it.value == value } ?: DEFAULT
            }
        }
    }

    private fun displayNoNetworkToast() {
        context?.let {
            mBinding.vNetworkStatus.root.visibility = View.VISIBLE
            mBinding.vNetworkStatus.toastMessage.text =
                resources.getString(R.string.lc_call_signal_no_network)
            val frameAnimation = CustomManager.getCustomAnimation(
                listOf(
                    R.drawable.ic_3_dot_01,
                    R.drawable.ic_3_dot_02,
                    R.drawable.ic_3_dot_03
                ), 250
            ) as AnimationDrawable
            mBinding.vNetworkStatus.toastIcon.background = frameAnimation
            frameAnimation.start()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewModel = ViewModelProviders.of(this).get(CallViewModel::class.java)
        mViewModel.init()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _mBinding = LcViewCallBinding.inflate(inflater, container, false)
        mBinding.lifecycleOwner = this
        return mBinding.root
    }

    override fun onPause() {
        super.onPause()
        NetworkQuality.currentQuality.removeObserver(this)
    }

    override fun onResume() {
        super.onResume()
        NetworkQuality.currentQuality.observeForever(this)
    }

    override fun getPage(): Page {
        return Page.CALL
    }

    override fun getColorMode(): ColorMode {
        return ColorMode.DARK
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.viewModel = mViewModel
        mViewModel.getCurrentCallState().observe(viewLifecycleOwner) { callState ->
            updateCallButtonGroup(callState)
        }
        mViewModel.getCallEnd().observe(viewLifecycleOwner) { isCallEnd ->
            if (isCallEnd) {
                handleCallEnd()
            }
        }
        showLiveConnectCallInfoFragment()
        initBottomSheetView()
        setupBottomSheetDisplay()
    }

    private fun handleCallEnd() {
        UIManager.getNavigator().back()
    }

    private fun setupBottomSheetDisplay() {
        mViewModel.getAudioRouteBottomSheetDisplayed().observe(viewLifecycleOwner) { show ->
            if (show) {
                showAudioRouteBottomSheet()
            }
        }
    }

    private fun initBottomSheetView() {
        context?.let {
            initAudioRouteBottomSheet(it)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    private fun showLiveConnectCallInfoFragment() {
        childFragmentManager.run {
            beginTransaction().replace(R.id.lc_call_info, CallSingleInfoFragment()).commit()
            executePendingTransactions()
        }
    }

    private fun updateCallButtonGroup(
        callState: CinnoxCallSession.State
    ) {
        when (callState) {
            CinnoxCallSession.State.CREATED, CinnoxCallSession.State.ESTABLISHED -> {
                if (mViewModel.isIncoming()) {
                    showButtonGroup(ButtonGroupStatus.INCOMING)
                } else {
                    showButtonGroup(ButtonGroupStatus.CONTROL)
                }
            }
            else -> showButtonGroup(ButtonGroupStatus.CONTROL)
        }
        switchCallEndDim(callState == CinnoxCallSession.State.TERMINATED || callState == CinnoxCallSession.State.DESTROYED)
    }

    private fun switchCallEndDim(isEnd: Boolean) {
        mBinding.lcCallEndDim.visibility = if (isEnd) View.VISIBLE else View.GONE
        val clickListener =
            if (isEnd)
                View.OnClickListener {
                    handleCallEnd()
                }
            else
                null
        mBinding.lcCallEndDim.setOnClickListener(clickListener)
    }

    private fun showButtonGroup(status: ButtonGroupStatus) {
        when (status) {
            ButtonGroupStatus.INCOMING -> showIncomingButtonGroup()
            ButtonGroupStatus.CONTROL -> showControlButtonGroup()
            else -> {
                // do nothing
            }
        }
    }

    private fun showIncomingButtonGroup() {
        switchButtonGroup(CallButtonIncomingFragment())
    }

    private fun showControlButtonGroup() {
        switchButtonGroup(CallButtonControlFragment())
        setupKeypad()
    }

    private fun switchButtonGroup(fragment: Fragment) {
        childFragmentManager.run {
            beginTransaction().replace(R.id.lc_call_button_group, fragment)
                .commit()
            executePendingTransactions()
        }
    }

    private fun setupKeypad() {
        val keypad = mBinding.lcCallKeypad
        keypad.keypad1.setOnClickListener {
            keypad.keypadDisplay.append("1")
            mViewModel.sendDtmf("1")
        }
        keypad.keypad2.setOnClickListener {
            keypad.keypadDisplay.append("2")
            mViewModel.sendDtmf("2")
        }
        keypad.keypad3.setOnClickListener {
            keypad.keypadDisplay.append("3")
            mViewModel.sendDtmf("3")
        }
        keypad.keypad4.setOnClickListener {
            keypad.keypadDisplay.append("4")
            mViewModel.sendDtmf("4")
        }
        keypad.keypad5.setOnClickListener {
            keypad.keypadDisplay.append("5")
            mViewModel.sendDtmf("5")
        }
        keypad.keypad6.setOnClickListener {
            keypad.keypadDisplay.append("6")
            mViewModel.sendDtmf("6")
        }
        keypad.keypad7.setOnClickListener {
            keypad.keypadDisplay.append("7")
            mViewModel.sendDtmf("7")
        }
        keypad.keypad8.setOnClickListener {
            keypad.keypadDisplay.append("8")
            mViewModel.sendDtmf("8")
        }
        keypad.keypad9.setOnClickListener {
            keypad.keypadDisplay.append("9")
            mViewModel.sendDtmf("9")
        }
        keypad.keypad10.setOnClickListener {
            keypad.keypadDisplay.append("*")
            mViewModel.sendDtmf("*")
        }
        keypad.keypad11.setOnClickListener {
            keypad.keypadDisplay.append("0")
            mViewModel.sendDtmf("0")
        }
        keypad.keypad11.setOnLongClickListener {
            keypad.keypadDisplay.append("+")
            return@setOnLongClickListener true
        }
        keypad.keypad12.setOnClickListener {
            keypad.keypadDisplay.append("#")
            mViewModel.sendDtmf("#")
        }
    }

    override fun onChanged(currentQuality: NetworkQuality.Quality) {
        Log.i(TAG, "onChanged currentQuality: $currentQuality")
        if (currentQuality == NetworkQuality.Quality.DISCONNECTED) {
            displayNoNetworkToast()
        } else {
            mBinding.vNetworkStatus.root.visibility = View.GONE
        }
    }
}
