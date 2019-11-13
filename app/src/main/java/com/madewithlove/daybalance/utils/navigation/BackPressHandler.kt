package com.madewithlove.daybalance.utils.navigation

interface BackPressHandler {

    /**
     * Return true if back press was handled, false otherwise.
     */
    fun handleBackPress(): Boolean

}