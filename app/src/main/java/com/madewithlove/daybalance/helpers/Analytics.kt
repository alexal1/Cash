/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.helpers

import android.content.Context
import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.madewithlove.daybalance.helpers.enums.Periods
import com.madewithlove.daybalance.viewmodels.enums.Categories
import com.madewithlove.daybalance.viewmodels.enums.GainCategories
import com.madewithlove.daybalance.viewmodels.enums.LossCategories

class Analytics(context: Context) {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)


    fun dateSwipeNext() {
        val bundle = bundleOf("direction" to "next")
        firebaseAnalytics.logEvent("date_swipe", bundle)
    }

    fun dateSwipePrev() {
        val bundle = bundleOf("direction" to "prev")
        firebaseAnalytics.logEvent("date_swipe", bundle)
    }

    fun pickCalendarDate() {
        firebaseAnalytics.logEvent("pick_calendar_date", null)
    }

    fun pickCategory(category: Categories) {
        if (category is LossCategories) {
            pickLossCategory(category)
        } else if (category is GainCategories) {
            pickGainCategory(category)
        }
    }

    fun changeCategoryPeriod(category: Categories, period: Periods) {
        if (category is LossCategories) {
            changeLossCategoryPeriod(category, period)
        } else if (category is GainCategories) {
            changeGainCategoryPeriod(category, period)
        }
    }

    fun createTransaction() {
        firebaseAnalytics.logEvent("create_transaction", null)
    }

    fun deleteTransaction() {
        firebaseAnalytics.logEvent("delete_transaction", null)
    }

    fun clickOnPush() {
        firebaseAnalytics.logEvent("click_on_push", null)
    }

    fun pickChartCategory(category: Categories) {
        if (category is LossCategories) {
            pickChartLossCategory(category)
        } else if (category is GainCategories) {
            pickChartGainCategory(category)
        }
    }

    fun splashScreenTime(millis: Long) {
        val seconds = (millis / 1000).toInt()
        val bundle = bundleOf("seconds" to seconds)
        firebaseAnalytics.logEvent("splash_screen_time", bundle)
    }


    private fun pickLossCategory(lossCategory: LossCategories) {
        val bundle = bundleOf("category_id" to lossCategory.id)
        firebaseAnalytics.logEvent("pick_loss_category", bundle)
    }

    private fun pickGainCategory(gainCategory: GainCategories) {
        val bundle = bundleOf("category_id" to gainCategory.id)
        firebaseAnalytics.logEvent("pick_gain_category", bundle)
    }

    private fun changeLossCategoryPeriod(lossCategory: LossCategories, period: Periods) {
        val bundle = bundleOf(
            "category_id" to lossCategory.id,
            "period" to period.name
        )
        firebaseAnalytics.logEvent("change_loss_category_period", bundle)
    }

    private fun changeGainCategoryPeriod(gainCategory: GainCategories, period: Periods) {
        val bundle = bundleOf(
            "category_id" to gainCategory.id,
            "period" to period.name
        )
        firebaseAnalytics.logEvent("change_gain_category_period", bundle)
    }

    private fun pickChartLossCategory(lossCategory: LossCategories) {
        val bundle = bundleOf("category_id" to lossCategory.id)
        firebaseAnalytics.logEvent("pick_chart_loss_category", bundle)
    }

    private fun pickChartGainCategory(gainCategory: GainCategories) {
        val bundle = bundleOf("category_id" to gainCategory.id)
        firebaseAnalytics.logEvent("pick_chart_gain_category", bundle)
    }

}