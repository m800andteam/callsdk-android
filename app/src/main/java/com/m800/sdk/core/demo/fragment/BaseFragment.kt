package com.m800.sdk.core.demo.fragment

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.m800.sdk.core.demo.navigation.Page
import com.m800.sdk.core.demo.navigation.PageArgs
import com.m800.sdk.core.demo.R
import com.m800.sdk.core.demo.utils.CustomManager

abstract class BaseFragment : Fragment() {

    private val TAG = BaseFragment::class.java.simpleName
    var pageArgs: PageArgs? = null
    protected var toolbar: Toolbar? = null

    abstract fun getPage(): Page?

    open fun getColorMode(): ColorMode {
        return ColorMode.DEFAULT
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        toolbar = view.findViewById(R.id.toolbar)
        toolbar?.setOnMenuItemClickListener { menuItem ->
            onToolbarMenuClick(menuItem)
        }

        setStatusBarColor()
        setToolbarColor()
        setBackgroundColor(view)
        view.isClickable = true
        view.isFocusable = true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i(TAG, "onCreate")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
    }

    @JvmOverloads
    protected fun setupToolbar(
        title: String? = null,
        subtitle: String? = null,
        enableBack: Boolean = true,
        showCloseIconForBack: Boolean = false,
        logo: Int = 0
    ) {
        toolbar?.let {
            it.title = title
            it.subtitle = subtitle
            if (parentFragment != null || !this.isHidden) {
                setHasOptionsMenu(true)
            }
            if (!enableBack) {
                it.navigationIcon = null
            } else if (showCloseIconForBack) {
                it.setNavigationIcon(R.drawable.close)
                setToolbarColor()
            } else if (!showCloseIconForBack) {
                it.setNavigationIcon(R.drawable.menu_arrow_left_outline)
                setToolbarColor()
            }

            if (logo != 0) {
                it.setLogo(logo)
            }

            it.menu.clear()

            toolbar?.setNavigationOnClickListener {
                activity?.onBackPressed()
            }
        }
    }

    open fun onToolbarMenuClick(item: MenuItem) : Boolean {
        return true
    }

    final override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
    }

    final override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
    }

    final override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        toolbar?.setOnMenuItemClickListener(null)
        toolbar = null
        super.onDestroyView()
    }

    private fun setStatusBarColor() {
        when (getColorMode()) {
            ColorMode.CUSTOM -> {
                // Do nothing
            }

            ColorMode.DEFAULT -> {
                activity?.apply {
                    window.statusBarColor = CustomManager.getColorBackgroundLight()
                    window.decorView.systemUiVisibility =
                        window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                }
            }

            ColorMode.DARK -> {
                activity?.apply {
                    window.statusBarColor = CustomManager.getColorTitleBlack()
                    window.decorView.systemUiVisibility =
                        window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                }
            }
        }
    }

    private fun setToolbarColor() {
        when (getColorMode()) {
            ColorMode.CUSTOM -> {
                // Do nothing
            }

            ColorMode.DEFAULT -> {
                toolbar?.setBackgroundColor(CustomManager.getColorBackgroundLight())
                toolbar?.setTitleTextColor(CustomManager.getColorBlack())
                toolbar?.navigationIcon?.setTint(CustomManager.getColorIcon())
            }

            ColorMode.DARK -> {
                toolbar?.setBackgroundColor(CustomManager.getColorTitleBlack())
                toolbar?.setTitleTextColor(CustomManager.getColorWhite())
                toolbar?.navigationIcon?.setTint(CustomManager.getColorWhite())
            }
        }
    }

    private fun setBackgroundColor(view: View) {
        when (getColorMode()) {
            ColorMode.CUSTOM -> {
                // Do nothing
            }

            ColorMode.DEFAULT -> {
                view.setBackgroundColor(CustomManager.getColorWhite())
            }

            ColorMode.DARK -> {
                view.setBackgroundColor(CustomManager.getColorTitleBlack())
            }
        }
    }
}