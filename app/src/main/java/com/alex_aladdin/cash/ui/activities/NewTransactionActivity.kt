package com.alex_aladdin.cash.ui.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputType
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.LinearLayout.VERTICAL
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProviders
import com.alex_aladdin.cash.R
import com.alex_aladdin.cash.utils.*
import com.alex_aladdin.cash.viewmodels.NewTransactionViewModel
import com.alex_aladdin.cash.viewmodels.enums.LossCategories
import com.jakewharton.rxbinding3.widget.textChanges
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

        linearLayout {
            orientation = VERTICAL

            toolbar {
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

                    viewModel.currentDateObservable.subscribeOnUi { date ->
                        text = getTitle(type, date)
                    }.cache(dc)
                }.lparams(wrapContent, matchParent)
            }.lparams(matchParent, dimen(R.dimen.toolbar_height))

            scrollView {
                constraintLayout {
                    val nameLabel = textView {
                        id = View.generateViewId()
                        textSize = 10f
                        textColorResource = R.color.white
                        textResource = if (type == NewTransactionViewModel.Type.GAIN) R.string.gain_name_hint else R.string.loss_name_hint
                        alpha = 0.0f
                    }.lparams(wrapContent, wrapContent) {
                        leftMargin = dip(4)
                        topMargin = dip(4)
                    }

                    val nameInput = appCompatEditText {
                        id = R.id.name_input
                        textSize = 16f
                        textColorResource = R.color.white
                        hintResource = if (type == NewTransactionViewModel.Type.GAIN) R.string.gain_name_hint else R.string.loss_name_hint
                        hintTextColor = ContextCompat.getColor(context, R.color.smoke)
                        background = null
                        inputType = InputType.TYPE_TEXT_FLAG_CAP_WORDS
                        imeOptions = EditorInfo.IME_ACTION_DONE

                        requestFocus()

                        textChanges().subscribeOnUi { text ->
                            if (text.isEmpty()) {
                                nameLabel.disappear()
                            } else {
                                nameLabel.appear()
                            }
                        }.cache(dc)
                    }.lparams(matchConstraint, wrapContent)

                    val nameSeparator = view {
                        id = View.generateViewId()
                        backgroundColorResource = R.color.palladium
                    }.lparams(matchConstraint, dip(0.5f))

                    val categoryPicker = fancyPicker {
                        id = R.id.category_picker
                        setData((0..100).map { it.toString() })
                        setData(LossCategories.values().map { getString(it.stringRes) })
                    }.lparams(matchConstraint, wrapContent)


                    applyConstraintSet {
                        connect(
                            START of nameLabel to START of PARENT_ID,
                            TOP of nameLabel to TOP of PARENT_ID
                        )

                        connect(
                            START of nameInput to START of PARENT_ID,
                            END of nameInput to END of PARENT_ID,
                            TOP of nameInput to BOTTOM of nameLabel
                        )

                        connect(
                            START of nameSeparator to START of PARENT_ID,
                            END of nameSeparator to END of PARENT_ID,
                            TOP of nameSeparator to BOTTOM of nameInput
                        )

                        connect(
                            START of categoryPicker to START of PARENT_ID,
                            END of categoryPicker to END of PARENT_ID,
                            TOP of categoryPicker to BOTTOM of nameSeparator
                        )
                    }
                }
            }.lparams(matchParent, matchParent) {
                marginStart = dimen(R.dimen.screen_border_size)
                marginEnd = dimen(R.dimen.screen_border_size)
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