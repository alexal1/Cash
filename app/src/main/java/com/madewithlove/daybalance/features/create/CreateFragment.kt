/**
 * Created by Alexander Mishchenko in 2019
 */

package com.madewithlove.daybalance.features.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.ctx

class CreateFragment : Fragment() {

    companion object {

        private const val TYPE = "type"

        fun create(type: CreateViewModel.Type): CreateFragment = CreateFragment().apply {
            arguments = bundleOf(TYPE to type)
        }

    }


    private val ui = CreateUI()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ui.createView(AnkoContext.create(ctx, this))

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}