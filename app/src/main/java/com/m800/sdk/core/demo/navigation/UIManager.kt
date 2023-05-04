package com.m800.sdk.core.demo.navigation

object UIManager {

    var isInitialized: Boolean = false
        private set

    private lateinit var navigator: Navigator

    /**
     * Call this method to initialize [UIManager] with a custom [PageCreator]
     *
     * @param pageCreator a [PageCreator] for displaying custom pages. The [DefaultPageCreator] will be used if not specified.
     */
    fun initialize(pageCreator: PageCreator = DefaultPageCreator()) {
        navigator = Navigator(pageCreator)
        isInitialized = true
    }

    /**
     * Call this method to initialize [UIManager] with a custom [Navigator]
     * now only use for business card app
     */
    fun initialize(navigator: Navigator) {
        this.navigator = navigator
        isInitialized = true
    }

    /**
     * Returns the [Navigator].
     *
     * @throws RuntimeException if [UIManager] is not initialized.
     */
    fun getNavigator(): Navigator {
        if (!isInitialized) {
            //TODO should throw M800 exception for internal debugging instead
            throw RuntimeException("LiveConnectUIManager is not initialized.")
        }
        return navigator
    }
}