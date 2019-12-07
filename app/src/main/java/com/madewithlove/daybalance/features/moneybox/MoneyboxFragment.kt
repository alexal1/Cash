/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.moneybox

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx

class MoneyboxFragment : Fragment() {

    companion object {

        fun create(): MoneyboxFragment = MoneyboxFragment()

    }


    private val ui: MoneyboxUI get() = moneyboxUI ?: MoneyboxUI().also { moneyboxUI = it }

    private var moneyboxUI: MoneyboxUI? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ui.createView(AnkoContext.create(ctx, this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ui.toolbar.apply {
            setNavigationOnClickListener {
                act.onBackPressed()
            }
        }
    }

    override fun onDestroyView() {
        moneyboxUI = null
        super.onDestroyView()
    }

}