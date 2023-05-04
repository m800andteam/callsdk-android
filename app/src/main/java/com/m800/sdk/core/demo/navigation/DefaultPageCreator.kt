package com.m800.sdk.core.demo.navigation

import com.m800.sdk.core.demo.fragment.BaseFragment
import com.m800.sdk.core.demo.call.CallFragment

open class DefaultPageCreator : PageCreator {
    
    override fun getCallFragment(): BaseFragment {
        return CallFragment()
    }
}