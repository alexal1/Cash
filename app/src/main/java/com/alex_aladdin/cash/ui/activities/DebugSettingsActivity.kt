package com.alex_aladdin.cash.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintSet.PARENT_ID
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.isInvisible
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.utils.setSelectableBackground
import com.alex_aladdin.cash.viewmodels.DebugSettingsViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout._ConstraintLayout
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.koin.android.ext.android.inject

class DebugSettingsActivity : AppCompatActivity() {

    companion object {

        fun start(activity: Activity) {
            val intent = Intent(activity, DebugSettingsActivity::class.java)
            val options = ActivityOptionsCompat.makeCustomAnimation(
                activity,
                R.anim.slide_in_up,
                R.anim.slide_out_up
            ).toBundle()

            activity.startActivity(intent, options)
        }

    }


    private val viewModel: DebugSettingsViewModel by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        constraintLayout {
            val toolbar = toolbar {
                id = View.generateViewId()
                navigationIconResource = R.drawable.ic_cross
                backgroundColorResource = R.color.deepDark

                setNavigationOnClickListener {
                    onBackPressed()
                }

                textView {
                    id = View.generateViewId()
                    textColorResource = R.color.white
                    textSize = 16f
                    backgroundColor = Color.TRANSPARENT
                    gravity = Gravity.CENTER_VERTICAL
                    includeFontPadding = false
                    textResource = R.string.debug_settings
                }.lparams(wrapContent, matchParent)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            val scrollView = scrollView {
                id = R.id.settings_scroll_view
                isVerticalScrollBarEnabled = false
                topPadding = dip(12)
                bottomPadding = dip(12)

                getScrollContent().lparams(matchParent, matchParent)
            }.lparams(matchConstraint, matchConstraint)

            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )

                connect(
                    START of scrollView to START of PARENT_ID,
                    END of scrollView to END of PARENT_ID,
                    TOP of scrollView to BOTTOM of toolbar,
                    BOTTOM of scrollView to BOTTOM of PARENT_ID
                )
            }
        }
    }

    private fun _ScrollView.getScrollContent(): ViewGroup = constraintLayout {
        val showPushItem = getSettingsItem(
            R.string.debug_settings_show_push_title,
            R.string.debug_settings_show_push_subtitle,
            true,
            controlView = space {
                id = View.generateViewId()
            },
            onClickListener = {
                viewModel.showPush()
            }
        )

        val addTransactionsToCurrentDateItem = getSettingsItem(
            R.string.debug_settings_add_transactions_to_current_date_title,
            R.string.debug_settings_add_transactions_to_current_date_subtitle,
            true,
            controlView = space {
                id = View.generateViewId()
            },
            onClickListener = {
                viewModel.addTransactionsToCurrentDate()
            }
        )

        val addTransactionsToCurrentMonthItem = getSettingsItem(
            R.string.debug_settings_add_transactions_to_current_month_title,
            R.string.debug_settings_add_transactions_to_current_month_subtitle,
            true,
            controlView = space {
                id = View.generateViewId()
            },
            onClickListener = {
                viewModel.addTransactionsToCurrentMonth()
            }
        )

        val wipeItem = getSettingsItem(
            R.string.debug_settings_wipe_title,
            R.string.debug_settings_wipe_subtitle,
            false,
            controlView = space {
                id = View.generateViewId()
            },
            onClickListener = {
                viewModel.wipe()
            }
        )

        applyConstraintSet {
            connect(
                START of showPushItem to START of PARENT_ID,
                END of showPushItem to END of PARENT_ID,
                TOP of showPushItem to TOP of PARENT_ID
            )

            connect(
                START of addTransactionsToCurrentDateItem to START of PARENT_ID,
                END of addTransactionsToCurrentDateItem to END of PARENT_ID,
                TOP of addTransactionsToCurrentDateItem to BOTTOM of showPushItem
            )

            connect(
                START of addTransactionsToCurrentMonthItem to START of PARENT_ID,
                END of addTransactionsToCurrentMonthItem to END of PARENT_ID,
                TOP of addTransactionsToCurrentMonthItem to BOTTOM of addTransactionsToCurrentDateItem
            )

            connect(
                START of wipeItem to START of PARENT_ID,
                END of wipeItem to END of PARENT_ID,
                TOP of wipeItem to BOTTOM of addTransactionsToCurrentMonthItem
            )
        }
    }

    private fun _ConstraintLayout.getSettingsItem(
        @StringRes titleRes: Int,
        @StringRes subtitleRes: Int,
        showSeparator: Boolean,
        controlView: View,
        onClickListener: () -> Unit
    ): View {
        val backgroundView = view {
            id = View.generateViewId()

            setSelectableBackground()

            setOnClickListener {
                onClickListener()
            }
        }.lparams(matchConstraint, matchConstraint)

        val title = textView {
            id = View.generateViewId()
            textColorResource = R.color.white
            textSize = 16f
            backgroundColor = Color.TRANSPARENT
            textResource = titleRes
        }.lparams(matchConstraint, wrapContent) {
            topMargin = dip(12)
            leftMargin = dip(24)
            rightMargin = dip(24)
        }

        val subtitle = textView {
            id = View.generateViewId()
            textColorResource = R.color.smoke
            textSize = 12f
            backgroundColor = Color.TRANSPARENT
            textResource = subtitleRes
        }.lparams(matchConstraint, wrapContent) {
            leftMargin = dip(24)
            rightMargin = dip(24)
        }

        val separator = view {
            id = View.generateViewId()
            backgroundColorResource = R.color.palladium_80
            isInvisible = !showSeparator
        }.lparams(matchConstraint, dip(1)) {
            topMargin = dip(12)
            leftMargin = dip(24)
        }

        applyConstraintSet {
            connect(
                START of title to START of backgroundView,
                END of title to START of controlView,
                TOP of title to TOP of backgroundView
            )

            connect(
                START of subtitle to START of backgroundView,
                END of subtitle to START of controlView,
                TOP of subtitle to BOTTOM of title
            )

            connect(
                TOP of controlView to TOP of title,
                BOTTOM of controlView to BOTTOM of subtitle,
                END of controlView to END of backgroundView
            )

            connect(
                START of separator to START of backgroundView,
                END of separator to END of backgroundView,
                TOP of separator to BOTTOM of subtitle
            )

            connect(
                BOTTOM of backgroundView to BOTTOM of separator
            )
        }

        return backgroundView
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_down, R.anim.slide_out_down)
    }

}