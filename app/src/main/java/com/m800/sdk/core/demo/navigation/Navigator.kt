package com.m800.sdk.core.demo.navigation

import android.content.Context
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.m800.sdk.core.demo.fragment.BaseFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception
import java.lang.ref.WeakReference

open class Navigator(private val pageCreator: PageCreator) : Navigation {

    private val TAG = Navigator::class.java.simpleName
    private lateinit var activityRef: WeakReference<FragmentActivity>
    private lateinit var fragmentManager: FragmentManager
    private var containerId: Int = 0

    private var mRequiredUpdatePage: Boolean = false
    private lateinit var mLastPage: Page
    private var mLastPageArgs: PageArgs? = null
    private var mLastUpdateByPresent: Boolean = true
    private var mLastEnterAnimotorRes: Int = 0
    private var mLastExitAnimotorRes: Int = 0
    private var mLastPopEnterAnimotorRes: Int = 0
    private var mLastPopExitAnimotorRes: Int = 0
    private var mLastCloseExisted: Boolean = true
    private var mLastAddToBackStack: Boolean = true
    private var mLastHidePrevious: Boolean = true

    override fun setRoot(activity: FragmentActivity, containerId: Int) {
        this.activityRef = WeakReference(activity)
        this.containerId = containerId
        fragmentManager = activity.supportFragmentManager

        fragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
            override fun onFragmentActivityCreated(
                fm: FragmentManager,
                f: Fragment,
                savedInstanceState: Bundle?
            ) {
                super.onFragmentActivityCreated(fm, f, savedInstanceState)
                checkLastPage()
            }

            // Dismiss keyboard when changing fragments
            override fun onFragmentViewCreated(
                fm: FragmentManager,
                fragment: Fragment,
                v: View,
                savedInstanceState: Bundle?
            ) {
                fragment.view?.rootView?.windowToken?.let {
                    val inputManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputManager.hideSoftInputFromWindow(it, 0)
                }
            }
        }, true)
    }

    override fun presentPage(page: Page,
                             pageArgs: PageArgs?,
                             @AnimatorRes @AnimRes enter: Int,
                             @AnimatorRes @AnimRes exit: Int,
                             @AnimatorRes @AnimRes popEnter: Int,
                             @AnimatorRes @AnimRes popExit: Int,
                             closeExisted: Boolean,
                             addToBackStack: Boolean) {
        Log.i(TAG, "presentPage, Page=$page")
        if (!fragmentManager.isStateSaved) {
            updatePage(page, pageArgs, enter, exit, popEnter, popExit, closeExisted, addToBackStack)
        } else {
            saveLastPage(
                page = page,
                pageArgs = pageArgs,
                updateByPresent = true,
                enter = enter,
                exit = exit,
                popEnter = popEnter,
                popExit = popExit,
                closeExisted = closeExisted,
                addToBackStack = addToBackStack
            )
        }
    }

    override fun closeAndPresentPage(page: Page,
                                     pageArgs: PageArgs?,
                                     enter: Int,
                                     exit: Int,
                                     @AnimatorRes @AnimRes popEnter: Int,
                                     @AnimatorRes @AnimRes popExit: Int,
                                     closeExisted: Boolean,
                                     addToBackStack: Boolean) {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        }

        presentPage(page, pageArgs, enter, exit, popEnter, popExit, closeExisted, addToBackStack)
    }

    override fun addPage(
        page: Page,
        pageArgs: PageArgs?,
        enter: Int,
        exit: Int,
        popEnter: Int,
        popExit: Int,
        hidePrevious: Boolean
    ) {
        if (!fragmentManager.isStateSaved) {
            val addFragment =
                (fragmentManager.findFragmentByTag(page.name) as? BaseFragment
                    ?: getFragmentForPage(page)).apply { this.pageArgs = pageArgs }
            fragmentManager
                .beginTransaction()
                .setCustomAnimations(enter, exit, popEnter, popExit)
                .also {
                    fragmentManager.fragments.forEach { fragment ->
                        if (hidePrevious && !fragment.isHidden) {
                            it.hide(fragment.parentFragment ?: fragment)
                        }
                    }
                }
                .also {
                    if (addFragment.isAdded) {
                        it.show(addFragment)
                    } else {
                        it.add(containerId, addFragment, page.name)
                    }
                }
                .addToBackStack(page.name)
                .commit()
        } else {
            saveLastPage(
                page = page,
                pageArgs = pageArgs,
                updateByPresent = false,
                enter = enter,
                exit = exit,
                popEnter = popEnter,
                popExit = popExit,
                hidePrevious = hidePrevious
            )
        }
    }

    override fun back() {
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            activityRef.get()?.finish()
        }
    }

    override fun backToExistingPage(page: Page): Boolean {
        return getExistingPage(page)?.let {
            fragmentManager.popBackStack(it::class.simpleName, 0)
            true
        } ?: false
    }

    override fun getCurrentPage(): Page? {
        return (fragmentManager.fragments.lastOrNull() as? BaseFragment)?.getPage()
    }

    private fun updatePage(page: Page,
                           pageArgs: PageArgs?,
                           @AnimatorRes @AnimRes enter: Int,
                           @AnimatorRes @AnimRes exit: Int,
                           @AnimatorRes @AnimRes popEnter: Int,
                           @AnimatorRes @AnimRes popExit: Int,
                           closeExisted: Boolean,
                           addToBackStack: Boolean) {
        fun run() {
            Log.i(TAG, "updatePage page = $page")

            checkLastPage()

            try {
                if (closeExisted) {
                    fragmentManager.popBackStack(
                        getFragmentForPage(page)::class.simpleName,
                        FragmentManager.POP_BACK_STACK_INCLUSIVE
                    )
                }

                getFragmentForPage(page).let {
                    it.pageArgs = pageArgs
                    replaceFragment(it, enter, exit, popEnter, popExit, addToBackStack)
                }
            } catch (e: Exception) {
                Log.e(TAG, "updatePage fail $e", e)
            }
        }

        if (Looper.getMainLooper() == Looper.myLooper()) {
            run()
        } else {
            CoroutineScope(Dispatchers.Main).launch {
                run()
            }
        }
    }

    private fun replaceFragment(fragment: Fragment,
                                @AnimatorRes @AnimRes enter: Int = 0,
                                @AnimatorRes @AnimRes exit: Int = 0,
                                @AnimatorRes @AnimRes popEnter: Int = 0,
                                @AnimatorRes @AnimRes popExit: Int = 0,
                                addToBackStack: Boolean) {
        fragmentManager
            .beginTransaction()
            .setCustomAnimations(enter, exit, popEnter, popExit)
            .replace(containerId, fragment)
            .also {
                if (addToBackStack) {
                    it.addToBackStack(fragment::class.simpleName)
                }
            }
            .commit()
    }

    private fun getExistingPage(page: Page): Fragment? {
        return fragmentManager.fragments.find {
            (it as? BaseFragment)?.getPage() == page
        }
    }

    private fun checkLastPage() {
        var requiredUpdatePage = false
        synchronized(this) {
            if (mRequiredUpdatePage) {
                requiredUpdatePage = true
                mRequiredUpdatePage = false
            }
        }

        if (requiredUpdatePage && ::mLastPage.isInitialized) {
            Log.i(TAG, "checkLastPage, require update last page=$mLastPage")

            if (mLastUpdateByPresent) {
                updatePage(
                    mLastPage,
                    mLastPageArgs,
                    mLastEnterAnimotorRes,
                    mLastExitAnimotorRes,
                    mLastPopEnterAnimotorRes,
                    mLastPopExitAnimotorRes,
                    mLastCloseExisted,
                    mLastAddToBackStack
                )
            } else {
                addPage(
                    mLastPage,
                    mLastPageArgs,
                    mLastEnterAnimotorRes,
                    mLastExitAnimotorRes,
                    mLastPopEnterAnimotorRes,
                    mLastPopExitAnimotorRes,
                    mLastHidePrevious
                )
            }
        }
    }

    private fun saveLastPage(
        page: Page,
        pageArgs: PageArgs?,
        updateByPresent: Boolean,
        @AnimatorRes @AnimRes enter: Int = 0,
        @AnimatorRes @AnimRes exit: Int = 0,
        @AnimatorRes @AnimRes popEnter: Int = 0,
        @AnimatorRes @AnimRes popExit: Int = 0,
        closeExisted: Boolean = true,
        addToBackStack: Boolean = true,
        hidePrevious: Boolean = true
    ) {
        synchronized(this) {
            mRequiredUpdatePage = true
        }

        Log.w(TAG, "saveLastPage, not updatePage=$page, updateByPresent=$updateByPresent")
        mLastPage = page
        mLastPageArgs = pageArgs
        mLastUpdateByPresent = updateByPresent
        mLastEnterAnimotorRes = enter
        mLastExitAnimotorRes = exit
        mLastPopEnterAnimotorRes = popEnter
        mLastPopExitAnimotorRes = popExit
        mLastCloseExisted = closeExisted
        mLastAddToBackStack = addToBackStack
        mLastHidePrevious = hidePrevious
    }

    fun getFragmentForPage(page: Page): BaseFragment {
        return when (page) {
            Page.CALL -> pageCreator.getCallFragment()
        }
    }

    override fun isCurrentActivityNavInstance(activity: FragmentActivity): Boolean {
        return activity == activityRef.get()
    }
}