package com.alex_aladdin.cash.utils

import android.text.SpannableStringBuilder
import android.text.style.CharacterStyle

fun String.asSpannableBuilder() = SpannableStringBuilder(this)

fun SpannableStringBuilder.replace(replacement: String, target: String, vararg spans: CharacterStyle): SpannableStringBuilder {
    val index = indexOf(replacement)

    replace(index, index + replacement.length, target)

    spans.forEach {
        setSpan(it, index, index + target.length, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)
    }

    return this
}