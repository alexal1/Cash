package com.madewithlove.daybalance.utils

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

fun SpannableStringBuilder.setSpanForAll(substring: String, spanCreator: () -> CharacterStyle): SpannableStringBuilder {
    var startIndex = 0

    while (startIndex >= 0) {
        val spanInstance = spanCreator()
        startIndex = setSpan(startIndex, substring, spanInstance)
    }

    return this
}

private fun SpannableStringBuilder.setSpan(startIndex: Int, substring: String, span: CharacterStyle): Int {
    val index = indexOf(substring, startIndex)
    if (index < 0) {
        return index
    }

    val endIndex = index + substring.length
    setSpan(span, index, endIndex, SpannableStringBuilder.SPAN_EXCLUSIVE_EXCLUSIVE)

    return endIndex
}

fun SpannableStringBuilder.add(text: CharSequence, flags: Int, vararg spans: Any): SpannableStringBuilder {
    val startIndex = length

    append(text)

    spans.forEach {
        setSpan(it, startIndex, length, flags)
    }

    return this
}