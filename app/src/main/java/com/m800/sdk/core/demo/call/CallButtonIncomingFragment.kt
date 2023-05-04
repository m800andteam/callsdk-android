package com.m800.sdk.core.demo.call

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.m800.sdk.core.demo.databinding.LcCallButtonIncomingBinding

class CallButtonIncomingFragment : Fragment() {

    private val TAG = CallButtonIncomingFragment::class.java.simpleName
    private var _mBinding: LcCallButtonIncomingBinding? = null
    private val mBinding get() = _mBinding!!
    private lateinit var mViewModel: CallViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _mBinding = LcCallButtonIncomingBinding.inflate(inflater, container, false)
        mBinding.lifecycleOwner = this
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentFragment?.let {
            mViewModel = ViewModelProviders.of(it).get(CallViewModel::class.java)
        } ?: run {
            Log.e(TAG, "onViewCreated get LiveConnectCallViewModel error")
            return
        }
        mBinding.viewModel = mViewModel
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }
}