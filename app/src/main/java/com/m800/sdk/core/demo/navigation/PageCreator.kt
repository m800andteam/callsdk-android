package com.m800.sdk.core.demo.navigation

import com.m800.sdk.core.demo.fragment.BaseFragment

interface PageCreator {

    /**
     * Returns the fragment for [Page.CALL]
     */
    fun getCallFragment(): BaseFragment
}