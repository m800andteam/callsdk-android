package com.m800.sdk.core.demo.navigation

import androidx.annotation.AnimRes
import androidx.annotation.AnimatorRes
import androidx.fragment.app.FragmentActivity

interface Navigation {

    /**
     * Call this method to set a custom activity for navigation, otherwise the default UiKit activity will be used.
     * The current navigation stack will be cleared when this method is called.
     *
     * @param activity the root activity.
     * @param containerId the view id of the container for hosting fragments.
     */
    fun setRoot(activity: FragmentActivity, containerId: Int)

    /**
     * Presents a new page.
     *
     * @param page the page to present.
     * @param pageArgs optional page arguments
     * @param enter an animation or animator resource ID used for the enter animation on the view of the fragment being added or attached.
     * @param exit an animation or animator resource ID used for the exit animation on the view of the fragment being removed or detached.
     * @param closeExisted if true, close existed instance of new page if any before present new one, to prevent multi-instance.
     * @param addToBackStack if true, add new page to back stack.
     */
    fun presentPage(page: Page,
                    pageArgs: PageArgs? = null,
                    @AnimatorRes @AnimRes enter: Int = 0,
                    @AnimatorRes @AnimRes exit: Int = 0,
                    @AnimatorRes @AnimRes popEnter: Int = 0,
                    @AnimatorRes @AnimRes popExit: Int = 0,
                    closeExisted: Boolean = true,
                    addToBackStack: Boolean = true)

    /**
     * Close current page and presents a new page.
     *
     * @param page the page to present.
     * @param pageArgs optional page arguments
     * @param enter an animation or animator resource ID used for the enter animation on the view of the fragment being added or attached.
     * @param exit an animation or animator resource ID used for the exit animation on the view of the fragment being removed or detached.
     * @param closeExisted if true, close existed instance of new page if any before present new one, to prevent multi-instance.
     * @param addToBackStack if true, add new page to back stack.
     */
    fun closeAndPresentPage(page: Page,
                            pageArgs: PageArgs? = null,
                            @AnimatorRes @AnimRes enter: Int = 0,
                            @AnimatorRes @AnimRes exit: Int = 0,
                            @AnimatorRes @AnimRes popEnter: Int = 0,
                            @AnimatorRes @AnimRes popExit: Int = 0,
                            closeExisted: Boolean = true,
                            addToBackStack: Boolean = true)

    /**
     * Add a new page.
     *
     * @param page the page to add.
     * @param pageArgs optional page arguments.
     * @param enter an animation or animator resource ID used for the enter animation on the view of the fragment being added or attached.
     * @param exit an animation or animator resource ID used for the exit animation on the view of the fragment being removed or detached.
     * @param hidePrevious if true, hide current page before add page, default is true.
     */
    fun addPage(
        page: Page,
        pageArgs: PageArgs? = null,
        @AnimatorRes @AnimRes enter: Int = 0,
        @AnimatorRes @AnimRes exit: Int = 0,
        @AnimatorRes @AnimRes popEnter: Int = 0,
        @AnimatorRes @AnimRes popExit: Int = 0,
        hidePrevious: Boolean = true
    )

    /**
     * Goes back to the previous page.
     */
    fun back()

    /**
     * Goes back to an existing page in the stack, pages above the existing page in the stack will be cleared.
     * Does nothing if no existing page exists in the stack.
     *
     * @return true if there is an existing page to go back to, false otherwise.
     */
    fun backToExistingPage(page: Page): Boolean

    /**
     * Returns the current visible page.
     *
     * @return the visible fragment, or null if there is no current page.
     */
    fun getCurrentPage(): Page?

    fun isCurrentActivityNavInstance(activity: FragmentActivity): Boolean
}