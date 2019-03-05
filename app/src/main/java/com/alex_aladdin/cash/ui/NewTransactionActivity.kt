package com.alex_aladdin.cash.ui

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.lifecycle.ViewModelProviders
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel
import org.jetbrains.anko.*
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import java.text.SimpleDateFormat
import java.util.*

class NewTransactionActivity : AppCompatActivity() {

    companion object {

        private const val TYPE_EXTRA = "type"

        fun create(activity: Activity, type: NewTransactionViewModel.Type) {
            val intent = Intent(activity, NewTransactionActivity::class.java).apply {
                putExtra(TYPE_EXTRA, type)
            }
            activity.startActivity(intent)
        }

    }


    private val dc = DisposableCache()
    private val dateFormatter by lazy { SimpleDateFormat("d MMM yyyy", currentLocale()) }
    private val type by lazy { intent.getSerializableExtra(TYPE_EXTRA) as NewTransactionViewModel.Type }

    private lateinit var viewModel: NewTransactionViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(NewTransactionViewModel::class.java)


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
                    textSize = 18f
                    backgroundColor = Color.TRANSPARENT
                    gravity = Gravity.CENTER_VERTICAL
                    includeFontPadding = false

                    viewModel.currentDateObservable.subscribeOnUi { date ->
                        text = getTitle(type, date)
                    }.cache(dc)
                }.lparams(wrapContent, matchParent)
            }.lparams(matchConstraint, dimen(R.dimen.toolbar_height))


            applyConstraintSet {
                connect(
                    START of toolbar to START of PARENT_ID,
                    END of toolbar to END of PARENT_ID,
                    TOP of toolbar to TOP of PARENT_ID
                )
            }
        }
    }

    private fun getTitle(type: NewTransactionViewModel.Type, date: Date): SpannableStringBuilder {
        val dateReplacement = "{date}"
        val transactionType = getString(if (type == NewTransactionViewModel.Type.GAIN) R.string.new_gain_on else R.string.new_loss_on)
        val formattedDate = dateFormatter.format(date)
        val text = "$transactionType $dateReplacement"

        return text
            .asSpannableBuilder()
            .replace(dateReplacement, formattedDate, StyleSpan(Typeface.BOLD))
    }

    override fun onDestroy() {
        super.onDestroy()
        dc.drain()
    }

}