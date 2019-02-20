package com.alex_aladdin.cash

import android.os.Bundle
import android.support.constraint.ConstraintSet.PARENT_ID
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import org.jetbrains.anko.constraint.layout.ConstraintSetBuilder.Side.*
import org.jetbrains.anko.constraint.layout.applyConstraintSet
import org.jetbrains.anko.constraint.layout.constraintLayout
import org.jetbrains.anko.constraint.layout.matchConstraint
import org.jetbrains.anko.dip
import org.jetbrains.anko.recyclerview.v7.recyclerView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        constraintLayout {
            val datesRecyclerView = recyclerView {
                id = View.generateViewId()
            }.lparams(0, 0)

            val buttonGain = fancyButton {
                id = View.generateViewId()
                init(
                    ContextCompat.getColor(this@MainActivity, R.color.green),
                    ContextCompat.getColor(this@MainActivity, R.color.greenGradColor1),
                    ContextCompat.getColor(this@MainActivity, R.color.greenGradColor2)
                )
                setTextResource(R.string.gain)
            }.lparams(matchConstraint, dip(70)) {
                bottomMargin = dip(4)
                leftMargin = dip(4)
            }

            val buttonLoss = fancyButton {
                id = View.generateViewId()
                init(
                    ContextCompat.getColor(this@MainActivity, R.color.red),
                    ContextCompat.getColor(this@MainActivity, R.color.redGradColor1),
                    ContextCompat.getColor(this@MainActivity, R.color.redGradColor2)
                )
                setTextResource(R.string.loss)
            }.lparams(matchConstraint, dip(70)) {
                bottomMargin = dip(4)
                rightMargin = dip(4)
            }

            applyConstraintSet {
                connect(
                    START of datesRecyclerView to START of PARENT_ID,
                    END of datesRecyclerView to END of PARENT_ID,
                    TOP of datesRecyclerView to TOP of PARENT_ID,
                    BOTTOM of datesRecyclerView to BOTTOM of PARENT_ID
                )

                connect(
                    START of buttonGain to START of PARENT_ID,
                    END of buttonGain to START of buttonLoss,
                    BOTTOM of buttonGain to BOTTOM of PARENT_ID
                )

                connect(
                    START of buttonLoss to END of buttonGain,
                    END of buttonLoss to END of PARENT_ID,
                    BOTTOM of buttonLoss to BOTTOM of PARENT_ID
                )
            }
        }
    }

}