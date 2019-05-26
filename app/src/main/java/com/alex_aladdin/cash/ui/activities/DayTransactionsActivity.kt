package com.alex_aladdin.cash.ui.activities

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.Gravity.CENTER_VERTICAL
import android.view.Gravity.START
import android.view.View
import android.widget.LinearLayout
import android.widget.LinearLayout.VERTICAL
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.ui.*
import com.alex_aladdin.cash.ui.fragments.DayTransactionsFragment
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.viewmodels.DayTransactionsViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.LayoutParams.*
import com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX
import com.google.android.material.appbar.CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN
import com.google.android.material.tabs.TabLayout
import org.jetbrains.anko.*
import org.jetbrains.anko.appcompat.v7.navigationIconResource
import org.jetbrains.anko.support.v4.viewPager
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class DayTransactionsActivity : AppCompatActivity() {

    private val viewModel: DayTransactionsViewModel by viewModel()
    private val dc = DisposableCache()
    private val dateFormatter by lazy { SimpleDateFormat("d MMM yyyy", currentLocale()) }
    private val pageChangeListener by lazy { OnPageChangeListener() }

    private lateinit var titleTexts: LinearLayout
    private lateinit var titleText1: TextView
    private lateinit var titleText2: TextView
    private lateinit var tabLayout: TabLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        coordinatorLayout {
            id = R.id.day_transactions_coordinator

            appBarLayout {
                id = R.id.day_transcations_appbar

                collapsingToolbarLayout {
                    id = R.id.day_transactions_collapsing_toolbar
                    minimumHeight = dimen(R.dimen.toolbar_height)
                    backgroundColorResource = R.color.smoke
                    setContentScrimResource(R.color.deepDark)
                    scrimAnimationDuration = 200

                    titleTexts = linearLayout {
                        orientation = VERTICAL
                        backgroundColor = Color.TRANSPARENT

                        titleText1 = textView {
                            backgroundColor = Color.TRANSPARENT
                            textColorResource = R.color.white_80
                            textSize = 32f
                            includeFontPadding = false
                        }.lparams(wrapContent, wrapContent)

                        titleText2 = textView {
                            backgroundColor = Color.TRANSPARENT
                            textColorResource = R.color.white_80
                            textSize = 16f
                            includeFontPadding = false
                            setLineSpacing(0f, 1.4f)
                        }.lparams(wrapContent, wrapContent) {
                            topMargin = dip(8)
                        }
                    }.lparams(wrapContent, wrapContent) {
                        gravity = START or CENTER_VERTICAL
                        marginStart = dip(72)
                        collapseMode = COLLAPSE_MODE_PARALLAX
                    }

                    appCompatToolbar {
                        id = View.generateViewId()
                        navigationIconResource = R.drawable.ic_cross
                        backgroundColor = Color.TRANSPARENT

                        val toolbarTitle = textView {
                            id = View.generateViewId()
                            textColorResource = R.color.white
                            textSize = 16f
                            backgroundColor = Color.TRANSPARENT
                            gravity = CENTER_VERTICAL
                            includeFontPadding = false

                            viewModel.currentDateObservable.subscribeOnUi { date ->
                                text = getStringWithDate(R.string.day_transactions_title, date, true)
                            }.cache(dc)
                        }.lparams(matchParent, matchParent)

                        setNavigationOnClickListener {
                            finish()
                        }

                        addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { _, offset ->
                            toolbarTitle.isVisible = totalScrollRange + offset == 0
                        })
                    }.lparams(matchParent, dimen(R.dimen.toolbar_height)) {
                        collapseMode = COLLAPSE_MODE_PIN
                    }
                }.lparams(matchParent, dip(256)) {
                    scrollFlags = SCROLL_FLAG_SCROLL or SCROLL_FLAG_EXIT_UNTIL_COLLAPSED or SCROLL_FLAG_SNAP
                }

                tabLayout = tabLayout {
                    tabMode = TabLayout.MODE_FIXED
                    tabGravity = TabLayout.GRAVITY_FILL
                    backgroundColorResource = R.color.steel_gray
                }.lparams(matchParent, wrapContent)
            }.lparams(matchParent, wrapContent)

            viewPager {
                id = R.id.day_transactions_view_pager
                adapter = SectionsPagerAdapter()
                tabLayout.setupWithViewPager(this)

                addOnPageChangeListener(pageChangeListener)
                currentItem = 1
            }.lparams(matchParent, matchParent) {
                behavior = AppBarLayout.ScrollingViewBehavior()
            }
        }
    }

    private fun getStringWithDate(stringRes: Int, date: Date, isDateBold: Boolean): SpannableStringBuilder {
        val dateReplacement = "{date}"
        val text = getString(stringRes, dateReplacement)
        val formattedDate = dateFormatter.format(date)

        return text
            .asSpannableBuilder()
            .let {
                if (isDateBold) {
                    it.replace(dateReplacement, formattedDate, StyleSpan(Typeface.BOLD))
                } else {
                    it.replace(dateReplacement, formattedDate)
                }
            }
    }

    override fun onDestroy() {
        dc.drain()
        super.onDestroy()
    }


    private inner class SectionsPagerAdapter : FragmentStatePagerAdapter(supportFragmentManager) {

        override fun getItem(position: Int) = when (position) {
            0 -> DayTransactionsFragment.create(DayTransactionsFragment.Type.GAIN)
            1 -> DayTransactionsFragment.create(DayTransactionsFragment.Type.LOSS)
            else -> throw IllegalArgumentException("Unexpected position: $position")
        }

        override fun getCount() = 2

        override fun getPageTitle(position: Int): String = when (position) {
            0 -> getString(R.string.day_transactions_gain)
            1 -> getString(R.string.day_transactions_loss)
            else -> throw IllegalArgumentException("Unexpected position: $position")
        }

    }

    private inner class OnPageChangeListener : ViewPager.OnPageChangeListener {

        override fun onPageScrollStateChanged(state: Int) {
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
        }

        override fun onPageSelected(position: Int) {
            when (position) {
                0 -> {
                    viewModel.currentDateObservable.take(1).subscribeOnUi { date ->
                        titleTexts.blink {
                            titleText1.text = getString(R.string.gain_transactions_description_part1)
                            titleText2.text = getStringWithDate(R.string.gain_transactions_description_part2, date, false)
                        }
                    }.cache(dc)
                }
                1 -> {
                    viewModel.currentDateObservable.take(1).subscribeOnUi { date ->
                        titleTexts.blink {
                            titleText1.text = getString(R.string.loss_transactions_description_part1)
                            titleText2.text = getStringWithDate(R.string.loss_transactions_description_part2, date, false)
                        }
                    }.cache(dc)
                }
                else -> throw IllegalArgumentException("Unexpected position: $position")
            }
        }

    }

}