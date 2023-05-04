package com.m800.sdk.core.demo.call

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider

import com.m800.sdk.call.CinnoxCallSession
import com.m800.sdk.core.demo.databinding.LcCallSingleInfoBinding

class CallSingleInfoFragment : Fragment() {

    private val TAG = CallSingleInfoFragment::class.java.simpleName
    private var _mBinding: LcCallSingleInfoBinding? = null
    private val mBinding get() = _mBinding!!
    private lateinit var mViewModel: CallViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _mBinding = LcCallSingleInfoBinding.inflate(inflater, container, false)
        mBinding.lifecycleOwner = this
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        parentFragment?.let {
            mViewModel = ViewModelProvider(it).get(CallViewModel::class.java)
        } ?: run {
            Log.e(TAG, "onViewCreated get LiveConnectCallViewModel error")
            return
        }

        mBinding.viewModel = mViewModel
        mBinding.status = mViewModel.getStatus()

        mViewModel.getCurrentCallState().observe(viewLifecycleOwner) {
            if (it == CinnoxCallSession.State.DESTROYED) {
                showCallEndView()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }

    private fun showCallEndView() {
        val finalStatus = mBinding.status?.value
        mBinding.status = MutableLiveData(finalStatus)
        mBinding.lcCallEndDim.visibility = View.VISIBLE
        mBinding.lcPanelStatus.bringToFront()
    }
}