package com.alex_aladdin.cash.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alex_aladdin.cash.R
import org.jetbrains.anko.backgroundColorResource
import org.jetbrains.anko.constraint.layout.constraintLayout

class CategoriesFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = container?.context?.constraintLayout {
        backgroundColorResource = R.color.green
    }

}